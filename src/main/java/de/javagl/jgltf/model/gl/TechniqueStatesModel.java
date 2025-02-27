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
package de.javagl.jgltf.model.gl;

import de.javagl.jgltf.model.GltfConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Interface for technique states.
 */
public interface TechniqueStatesModel {
    /**
     * Returns a list containing all possible states that may be contained
     * in a <code>technique.states.enable</code> list.
     *
     * @return All possible states
     */
    public static List<Integer> getAllStates() {
        List<Integer> allStates = Arrays.asList(
                GltfConstants.GL_BLEND,
                GltfConstants.GL_CULL_FACE,
                GltfConstants.GL_DEPTH_TEST,
                GltfConstants.GL_POLYGON_OFFSET_FILL,
                GltfConstants.GL_SAMPLE_ALPHA_TO_COVERAGE,
                GltfConstants.GL_SCISSOR_TEST
        );
        return allStates;
    }

    /**
     * Returns an unmodifiable list containing the enabled states,
     * or <code>null</code> if only the default states should be
     * enabled.
     *
     * @return The enabled states
     */
    List<Integer> getEnable();

    /**
     * Returns the {@link TechniqueStatesFunctionsModel}, or <code>null</code>
     * if the default technique states functions should be used.
     *
     * @return The {@link TechniqueStatesFunctionsModel}
     */
    TechniqueStatesFunctionsModel getTechniqueStatesFunctionsModel();
}
