package com.modularwarfare.common.network;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.armor.ArmorType;
import com.modularwarfare.common.armor.ItemSpecialArmor;
import com.modularwarfare.common.capability.extraslots.CapabilityExtra;
import com.modularwarfare.common.capability.extraslots.IExtraItemHandler;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.common.guns.manager.ShotValidation;
import com.modularwarfare.utility.RayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketExpGunFire extends PacketBase {

    public int entityId;

    public String internalname;
    public String hitboxType;

    public int fireTickDelay;

    public float recoilPitch;
    public float recoilYaw;

    public float recoilAimReducer;

    public float bulletSpread;

    private double posX;
    private double posY;
    private double posZ;
    private EnumFacing facing;

    public PacketExpGunFire() {
    }

    public PacketExpGunFire(int entityId, String internalname, String hitboxType, int fireTickDelay, float recoilPitch, float recoilYaw, float recoilAimReducer, float bulletSpread, double x, double y, double z) {
        this(entityId, internalname, hitboxType, fireTickDelay, recoilPitch, recoilYaw, recoilAimReducer, bulletSpread, x, y, z, null);
    }

    public PacketExpGunFire(int entityId, String internalname, String hitboxType, int fireTickDelay, float recoilPitch, float recoilYaw, float recoilAimReducer, float bulletSpread, double x, double y, double z, EnumFacing facing) {
        this.entityId = entityId;
        this.internalname = internalname;
        this.hitboxType = hitboxType;

        this.fireTickDelay = fireTickDelay;
        this.recoilPitch = recoilPitch;
        this.recoilYaw = recoilYaw;
        this.recoilAimReducer = recoilAimReducer;
        this.bulletSpread = bulletSpread;

        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.facing = facing;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(data, this.internalname);
        ByteBufUtils.writeUTF8String(data, this.hitboxType);

        data.writeInt(this.fireTickDelay);
        data.writeFloat(this.recoilPitch);
        data.writeFloat(this.recoilYaw);
        data.writeFloat(this.recoilAimReducer);
        data.writeFloat(this.bulletSpread);

        data.writeDouble(this.posX);
        data.writeDouble(this.posY);
        data.writeDouble(this.posZ);
        if (this.facing == null) {
            data.writeInt(-1);
        } else {
            data.writeInt(this.facing.ordinal());
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        this.entityId = data.readInt();
        this.internalname = ByteBufUtils.readUTF8String(data);
        this.hitboxType = ByteBufUtils.readUTF8String(data);

        this.fireTickDelay = data.readInt();
        this.recoilPitch = data.readFloat();
        this.recoilYaw = data.readFloat();
        this.recoilAimReducer = data.readFloat();
        this.bulletSpread = data.readFloat();

        this.posX = data.readDouble();
        this.posY = data.readDouble();
        this.posZ = data.readDouble();
        int enumFacing = data.readInt();
        if (enumFacing != -1) {
            this.facing = EnumFacing.values()[enumFacing];
        }
    }

    @Override
    /*
     * need to support hit/fire event
     * */
    public void handleServerSide(EntityPlayerMP entityPlayer) {
        IThreadListener mainThread = (WorldServer) entityPlayer.world;
        mainThread.addScheduledTask(new Runnable() {
            public void run() {
                if (entityPlayer.ping > 100 * 20) {
                    entityPlayer.sendMessage(new TextComponentString(TextFormatting.GRAY + "[" + TextFormatting.RED + "ModularWarfare" + TextFormatting.GRAY + "] Your ping is too high, shot not registered."));
                    return;
                }
                if (entityPlayer != null) {
                    if (entityPlayer.getHeldItemMainhand() != null) {
                        if (entityPlayer.getHeldItemMainhand().getItem() instanceof ItemGun) {

                            if (ModularWarfare.gunTypes.get(internalname) != null) {
                                ItemGun itemGun = ModularWarfare.gunTypes.get(internalname);

                                if (entityPlayer.getHeldItemMainhand().getItem() != itemGun) {
                                    return;
                                }

                                if (entityId != -1) {
                                    Entity target = entityPlayer.world.getEntityByID(entityId);
                                    WeaponFireMode fireMode = GunType.getFireMode(entityPlayer.getHeldItemMainhand());
                                    if (fireMode == null)
                                        return;
                                    IExtraItemHandler extraSlots = null;
                                    ItemStack plate = null;
                                    if (ShotValidation.verifShot(entityPlayer, entityPlayer.getHeldItemMainhand(), itemGun, fireMode, fireTickDelay, recoilPitch, recoilYaw, recoilAimReducer, bulletSpread)) {
                                        if (target != null) {
                                            float damage = itemGun.type.gunDamage;
                                            if (target instanceof EntityPlayer && hitboxType != null) {
                                                if (hitboxType.contains("body")) {
                                                    EntityPlayer player = (EntityPlayer) target;
                                                    if (player.hasCapability(CapabilityExtra.CAPABILITY, null)) {
                                                        extraSlots = player.getCapability(CapabilityExtra.CAPABILITY, null);
                                                        plate = extraSlots.getStackInSlot(1);
                                                        if (plate != null) {
                                                            if (plate.getItem() instanceof ItemSpecialArmor) {
                                                                ArmorType armorType = ((ItemSpecialArmor) plate.getItem()).type;
                                                                damage = (float) (damage - (damage * armorType.defense));
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            //BULLET START

                                            ItemBullet bulletItem = ItemGun.getUsedBullet(entityPlayer.getHeldItemMainhand(), itemGun.type);

                                            if (target instanceof EntityLivingBase) {
                                                EntityLivingBase targetELB = (EntityLivingBase) target;
                                                if (bulletItem != null) {
                                                    if (bulletItem.type != null) {
                                                        if (bulletItem.type.bulletProperties != null) {
                                                            if (!bulletItem.type.bulletProperties.isEmpty()) {
                                                                BulletProperty bulletProperty = bulletItem.type.bulletProperties.get(targetELB.getName()) != null ? bulletItem.type.bulletProperties.get(targetELB.getName()) : bulletItem.type.bulletProperties.get("All");
                                                                if (bulletProperty.potionEffects != null) {
                                                                    for (PotionEntry potionEntry : bulletProperty.potionEffects) {
                                                                        targetELB.addPotionEffect(new PotionEffect(potionEntry.potionEffect.getPotion(), potionEntry.duration, potionEntry.level));
                                                                    }
                                                                }
                                                                if(bulletProperty.fireLevel>0) {
                                                                    targetELB.setFire(bulletProperty.fireLevel);  
                                                                }
                                                                if(bulletProperty.explosionLevel>0) {
                                                                    targetELB.world.createExplosion(null, targetELB.posX, targetELB.posY+1, targetELB.posZ, bulletProperty.explosionLevel, bulletProperty.explosionBroken);
                                                                }
                                                                if(bulletProperty.knockLevel>0) {
                                                                    targetELB.knockBack(entityPlayer, bulletProperty.knockLevel, entityPlayer.posX-targetELB.posX, entityPlayer.posZ-targetELB.posZ);
                                                                }
                                                                if(bulletProperty.banShield) {
                                                                    if (targetELB instanceof EntityPlayer)
                                                                    {
                                                                        EntityPlayer ep = (EntityPlayer)targetELB;
                                                                        ItemStack itemstack1 = ep.isHandActive() ? ep.getActiveItemStack() : ItemStack.EMPTY;

                                                                        if ((!itemstack1.isEmpty())&&itemstack1.getItem().isShield(itemstack1, ep))
                                                                        {
                                                                            ep.getCooldownTracker().setCooldown(itemstack1.getItem(), 100);
                                                                            ep.world.setEntityState(ep, (byte)30);
                                                                        }
                                                                    }
                                                                }
                                                                
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            damage *= bulletItem.type.bulletDamageFactor;

                                            //BULLET END
                                            boolean flag = false;
                                            DamageSource damageSource = DamageSource.causePlayerDamage(entityPlayer).setProjectile();


                                            if (bulletItem.type.isFireDamage) {
                                                damageSource.setFireDamage();
                                            }
                                            if (bulletItem.type.isAbsoluteDamage) {
                                                damageSource.setDamageIsAbsolute();
                                            }
                                            if (bulletItem.type.isBypassesArmorDamage) {
                                                damageSource.setDamageBypassesArmor();
                                            }
                                            if (bulletItem.type.isExplosionDamage) {
                                                damageSource.setExplosion();
                                            }
                                            if (bulletItem.type.isMagicDamage) {
                                                damageSource.setMagicDamage();
                                            }
                                            if (!ModConfig.INSTANCE.shots.knockbackEntityDamage) {
                                                flag = RayUtil.attackEntityWithoutKnockback(target, damageSource, (hitboxType.contains("head") ? damage + itemGun.type.gunDamageHeadshotBonus : damage));
                                            } else {
                                                flag = target.attackEntityFrom(damageSource, (hitboxType.contains("head") ? damage + itemGun.type.gunDamageHeadshotBonus : damage));
                                            }
                                            target.hurtResistantTime = 0;
                                            if (flag) {
                                                if (plate != null) {
                                                    plate.attemptDamageItem(1, entityPlayer.getRNG(), entityPlayer);
                                                    //entityPlayer.sendMessage(new TextComponentString(plate.getItemDamage()+"/"+plate.getMaxDamage()));
                                                    if (plate.getItemDamage() >= plate.getMaxDamage()) {
                                                        extraSlots.setStackInSlot(1, ItemStack.EMPTY);
                                                    } else {
                                                        extraSlots.setStackInSlot(1, plate);
                                                    }
                                                }
                                            }

                                            if (entityPlayer instanceof EntityPlayerMP) {
                                                ModularWarfare.NETWORK.sendTo(new PacketPlayHitmarker(hitboxType.contains("head")), entityPlayer);
                                                ModularWarfare.NETWORK.sendTo(new PacketPlaySound(target.getPosition(), "flyby", 1f, 1f), (EntityPlayerMP) target);

                                                if (ModConfig.INSTANCE.hud.snap_fade_hit) {
                                                    ModularWarfare.NETWORK.sendTo(new PacketPlayerHit(), (EntityPlayerMP) target);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    BlockPos blockPos = new BlockPos(posX, posY, posZ);
                                    ItemGun.playImpactSound(entityPlayer.world, blockPos, itemGun.type);
                                    itemGun.type.playSoundPos(blockPos, entityPlayer.world, WeaponSoundType.Crack, entityPlayer, 1.0f);
                                    ItemGun.doHit(posX, posY, posZ, facing, entityPlayer);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {

    }

}