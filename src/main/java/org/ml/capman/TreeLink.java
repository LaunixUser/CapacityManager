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

/**
 * @author Dr. Matthias Laux
 */
public class TreeLink {

    private String parentNodeID;
    private String childNodeID;
    private String parentEmployeeID;

    public TreeLink(String childNodeID, String parentNodeID, String parentEmployeeID) {
        if (childNodeID == null) {
            throw new IllegalArgumentException("childNodeID may not be null");
        }
        if (parentNodeID == null) {
            throw new IllegalArgumentException("parentNodeID may not be null");
        }

        if (parentEmployeeID == null) {
            throw new IllegalArgumentException("parentEmployeeID may not be null");
        }
        this.childNodeID = childNodeID;
        this.parentNodeID = parentNodeID;
        this.parentEmployeeID = parentEmployeeID;
    }

    /**
     * @return the parentImporterID
     */
    public String getParentNodeID() {
        return parentNodeID;
    }

    /**
     * @return the childImporterID
     */
    public String getChildNodeID() {
        return childNodeID;
    }

    /**
     * @return the parentEmployeeID
     */
    public String getParentEmployeeID() {
        return parentEmployeeID;
    }

}
