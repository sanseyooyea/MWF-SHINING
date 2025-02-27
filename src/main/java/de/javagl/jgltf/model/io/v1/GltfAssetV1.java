/*
 * www.javagl.de - JglTF
 *
 * Copyright 2015-2017 Marco Hutter - http://www.javagl.de
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package de.javagl.jgltf.model.io.v1;

import de.javagl.jgltf.impl.v1.Buffer;
import de.javagl.jgltf.impl.v1.GlTF;
import de.javagl.jgltf.impl.v1.Image;
import de.javagl.jgltf.impl.v1.Shader;
import de.javagl.jgltf.model.Optionals;
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.GltfAsset;
import de.javagl.jgltf.model.io.GltfReference;
import de.javagl.jgltf.model.io.IO;
import de.javagl.jgltf.model.v1.BinaryGltfV1;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Implementation of the {@link GltfAsset} interface for glTF 1.0.
 */
public final class GltfAssetV1 implements GltfAsset {
    /**
     * The {@link GlTF}
     */
    private final GlTF gltf;

    /**
     * The optional binary data
     */
    private final ByteBuffer binaryData;

    /**
     * The mapping from (relative) URI strings to the associated external data
     */
    private final Map<String, ByteBuffer> referenceDatas;

    /**
     * Creates a new instance
     *
     * @param gltf       The {@link GlTF}
     * @param binaryData The optional binary data
     */
    public GltfAssetV1(GlTF gltf, ByteBuffer binaryData) {
        this.gltf = Objects.requireNonNull(gltf, "The gltf may not be null");
        this.binaryData = binaryData;
        this.referenceDatas = new ConcurrentHashMap<String, ByteBuffer>();
    }

    /**
     * Store the given byte buffer under the given (relative) URI string
     *
     * @param uriString  The URI string
     * @param byteBuffer The byte buffer
     */
    void putReferenceData(String uriString, ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            referenceDatas.remove(uriString);
        } else {
            referenceDatas.put(uriString, byteBuffer);
        }
    }

    @Override
    public GlTF getGltf() {
        return gltf;
    }

    @Override
    public ByteBuffer getBinaryData() {
        return Buffers.createSlice(binaryData);
    }

    @Override
    public List<GltfReference> getReferences() {
        List<GltfReference> references = new ArrayList<GltfReference>();
        references.addAll(getBufferReferences());
        references.addAll(getImageReferences());
        references.addAll(getShaderReferences());
        return references;
    }

    /**
     * Create a list containing all {@link GltfReference} objects for the
     * buffers that are contained in this model.
     *
     * @return The references
     */
    public List<GltfReference> getBufferReferences() {
        List<GltfReference> references = new ArrayList<GltfReference>();
        Map<String, Buffer> buffers = Optionals.of(gltf.getBuffers());
        for (Entry<String, Buffer> entry : buffers.entrySet()) {
            String bufferId = entry.getKey();
            if (BinaryGltfV1.isBinaryGltfBufferId(bufferId)) {
                continue;
            }
            Buffer buffer = buffers.get(bufferId);
            String uri = buffer.getUri();
            if (!IO.isDataUriString(uri)) {
                Consumer<ByteBuffer> target =
                        byteBuffer -> putReferenceData(uri, byteBuffer);
                GltfReference reference =
                        new GltfReference(bufferId, uri, target);
                references.add(reference);
            }
        }
        return references;
    }

    /**
     * Create a list containing all {@link GltfReference} objects for the
     * images that are contained in this model.
     *
     * @return The references
     */
    public List<GltfReference> getImageReferences() {
        List<GltfReference> references = new ArrayList<GltfReference>();
        Map<String, Image> images = Optionals.of(gltf.getImages());
        for (Entry<String, Image> entry : images.entrySet()) {
            String imageId = entry.getKey();
            Image image = entry.getValue();
            if (BinaryGltfV1.hasBinaryGltfExtension(image)) {
                continue;
            }
            String uri = image.getUri();
            if (!IO.isDataUriString(uri)) {
                Consumer<ByteBuffer> target =
                        byteBuffer -> putReferenceData(uri, byteBuffer);
                GltfReference reference =
                        new GltfReference(imageId, uri, target);
                references.add(reference);
            }
        }
        return references;
    }

    /**
     * Create a list containing all {@link GltfReference} objects for the
     * shaders that are contained in this model.
     *
     * @return The references
     */
    public List<GltfReference> getShaderReferences() {
        List<GltfReference> references = new ArrayList<GltfReference>();
        Map<String, Shader> shaders = Optionals.of(gltf.getShaders());
        for (Entry<String, Shader> entry : shaders.entrySet()) {
            String shaderId = entry.getKey();
            Shader shader = entry.getValue();
            if (BinaryGltfV1.hasBinaryGltfExtension(shader)) {
                continue;
            }
            String uri = shader.getUri();
            if (!IO.isDataUriString(uri)) {
                Consumer<ByteBuffer> target =
                        byteBuffer -> putReferenceData(uri, byteBuffer);
                GltfReference reference =
                        new GltfReference(shaderId, uri, target);
                references.add(reference);
            }
        }
        return references;
    }

    @Override
    public ByteBuffer getReferenceData(String uriString) {
        return Buffers.createSlice(referenceDatas.get(uriString));
    }

    @Override
    public Map<String, ByteBuffer> getReferenceDatas() {
        return Collections.unmodifiableMap(referenceDatas);
    }

}
