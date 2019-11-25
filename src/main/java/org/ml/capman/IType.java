/*
 * The MIT License
 *
 * Copyright 2019 Dr. Matthias Laux.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.ml.capman;

import org.ml.capman.DataConfiguration.TypeKind;
import org.ml.capman.DataConfiguration.TypeDimension;

/**
 * @author Dr. Matthias Laux
 */
public interface IType {

    /**
     * @return
     */
    String getTypeName();

    /**
     * @return
     */
    TypeKind getTypeKind();

    /**
     * @return
     */
    TypeDimension getTypeDimension();

    /**
     * IType instances can implement a level concept which allows for a bit of a
     * hierarchy within types if needed. This can come in handy when there's a
     * large number of types and some of them have a different semantic, so we
     * can use levels to cluster them
     *
     * @return
     */
    default int getLevel() {
        return 0;
    }

    /**
     * Default comparison of types. The primary sort key is the dimension here,
     * secondary the name
     *
     * @param otherType
     * @return
     */
    /**
     * @Override default int compareTo(IType otherType) { if (otherType == null)
     * { throw new NullPointerException("otherType may not be null"); } if
     * (this.getTypeDimension().equals(otherType.getTypeDimension())) { if
     * (this.getTypeName().equals(otherType.getTypeName())) { return 0; } else {
     * return this.getTypeName().compareTo(otherType.getTypeName()); } } else {
     * return this.getTypeDimension().compareTo(otherType.getTypeDimension()); }
     * }
     */
}
