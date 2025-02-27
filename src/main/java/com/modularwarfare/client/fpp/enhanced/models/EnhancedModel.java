package com.modularwarfare.client.fpp.enhanced.models;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.IMWModel;
import com.modularwarfare.client.fpp.enhanced.configs.EnhancedRenderConfig;
import com.modularwarfare.common.type.BaseType;
import de.javagl.jgltf.model.NodeModel;
import mchhui.hegltf.DataAnimation;
import mchhui.hegltf.DataAnimation.Transform;
import mchhui.hegltf.DataNode;
import mchhui.hegltf.GltfDataModel;
import mchhui.hegltf.GltfRenderModel;
import mchhui.hegltf.GltfRenderModel.NodeAnimationBlender;
import mchhui.hegltf.GltfRenderModel.NodeAnimationMapper;
import mchhui.hegltf.GltfRenderModel.NodeState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.HashSet;

public class EnhancedModel implements IMWModel {
    private static final FloatBuffer MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final HashMap<ResourceLocation, GltfDataModel> modelCache=new HashMap<ResourceLocation, GltfDataModel>();
    public EnhancedRenderConfig config;
    public BaseType baseType;
    public GltfRenderModel model;
    public boolean initCal = false;
    private HashMap<String, Matrix4f> invMatCache = new HashMap<String, Matrix4f>();

    public EnhancedModel(EnhancedRenderConfig config, BaseType baseType) {
        this.config = config;
        this.baseType = baseType;
        if(!modelCache.containsKey(getModelLocation())) {
            modelCache.put(getModelLocation(), GltfDataModel.load(getModelLocation()));
        }
        model = new GltfRenderModel(modelCache.get(getModelLocation()));
    }
    
    public static void clearCache() {
        modelCache.values().forEach((model)->{
            model.delete();
        });
        modelCache.clear();
    }

    public ResourceLocation getModelLocation() {
        return new ResourceLocation(ModularWarfare.MOD_ID,
                "gltf/" + baseType.getAssetDir() + "/" + this.config.modelFileName);
    }

    public void loadAnimation(EnhancedModel other, boolean skin) {
        if (model == null || other == null || other.model == null) {
            return;
        }
        model.loadAnimation(other.model, skin);
    }

    public void updateAnimation(float time, boolean skin) {
        invMatCache.clear();
        initCal = model.updateAnimation(time, skin || !initCal);
    }

    public Transform findLocalTransform(String name, float time) {
        if (model == null) {
            return null;
        }
        DataNode node = model.geoModel.nodes.get(name);
        if (node == null) {
            return null;
        }
        DataAnimation ani = model.geoModel.animations.get(name);
        if (ani == null) {
            return null;
        }
        return model.geoModel.animations.get(name).findTransform(time, node.pos, node.size, node.rot);
    }

    public void setAnimationCalBlender(NodeAnimationBlender blender) {
        model.setNodeAnimationCalBlender(blender);
    }

    public void setAnimationLoadMapper(NodeAnimationMapper mapper) {
        model.setNodeAnimationLoadMapper(mapper);
    }

    /**
     * 兼容旧版 请勿使用
     */
    @Deprecated
    public void updateAnimation(float time) {
        updateAnimation(time, true);
    }

    public boolean existPart(String part) {
        return model.geoModel.nodes.containsKey(part);
    }

    /**
     * 兼容旧版 请勿使用
     */
    @Deprecated
    public NodeModel getPart(String part) {
        DataNode node = model.geoModel.nodes.get(part);
        if (node == null) {
            return null;
        }
        return node.unsafeNode;
    }

    @Override
    public void renderPart(String part, float scale) {
        if (!initCal) {
            return;
        }
        model.renderPart(part);
    }

    public void renderPart(String part) {
        if (!initCal) {
            return;
        }
        model.renderPart(part);
    }

    public void renderPartExcept(HashSet<String> set) {
        if (!initCal) {
            return;
        }
        model.renderExcept(set);
    }

    public void renderPart(String[] only) {
        if (!initCal) {
            return;
        }
        model.renderOnly(only);
    }

    public Matrix4f getGlobalTransform(String name) {
        if (!initCal) {
            return new Matrix4f();
        }
        NodeState state = model.nodeStates.get(name);
        if (state == null) {
            return new Matrix4f();
        }
        return state.mat;
    }

    public void applyGlobalTransformToOther(String binding, Runnable run) {
        if (!initCal) {
            return;
        }
        NodeState state = model.nodeStates.get(binding);
        if(state==null) {
            return;
        }
        GlStateManager.pushMatrix();
        if (state != null) {
            GlStateManager.multMatrix(state.mat.get(MATRIX_BUFFER));
        }
        run.run();

        GlStateManager.popMatrix();
    }

    public void applyGlobalInverseTransformToOther(String binding, Runnable run) {
        if (!initCal) {
            return;
        }
        NodeState state = model.nodeStates.get(binding);
        GlStateManager.pushMatrix();
        if (state != null) {
            Matrix4f invmat = invMatCache.get(binding);
            if (invmat == null) {
                invmat = new Matrix4f(state.mat).invert();
                invMatCache.put(binding, invmat);
            }
            GlStateManager.multMatrix(invmat.get(MATRIX_BUFFER));
        }
        run.run();

        GlStateManager.popMatrix();
    }

}
