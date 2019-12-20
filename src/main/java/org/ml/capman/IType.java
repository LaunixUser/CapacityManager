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
