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

import de.javagl.jgltf.model.NodeModel;

/**
 * An interface for describing {@link TechniqueModel} parameters
 */
public interface TechniqueParametersModel {
    /**
     * Returns the type of the parameter, as a GL constant. For example,
     * <code>GL_INT</code> or <code>GL_FLOAT_VEC3</code>
     *
     * @return The type
     */
    int getType();

    /**
     * Returns the count
     *
     * @return The count
     */
    int getCount();

    /**
     * Returns the string describing the {@link Semantic} of this parameter.
     * This may be a string that starts with an underscore <code>"_"</code>,
     * indicating a custom semantic
     *
     * @return The {@link Semantic} string
     */
    String getSemantic();

    /**
     * Returns the value of this parameter
     *
     * @return The value
     */
    Object getValue();

    /**
     * Returns the {@link NodeModel} of the node that this parameter
     * refers to. This is, for example, used for computing the
     * {@link Semantic#MODEL} matrix.
     *
     * @return The {@link NodeModel}
     */
    NodeModel getNodeModel();
}

