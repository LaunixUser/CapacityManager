package org.ml.capman;

import java.util.Objects;

import org.ml.capman.DataConfiguration.TypeDimension;

/**
 * @author Dr. Matthias Laux
 */
public class Constraint implements Comparable<Constraint> {

    private IType type1;
    private Comparable field1;
    private IType type2;
    private Comparable field2;
    private String signature;

    /**
     * @param type1
     * @param field1
     * @param type2
     * @param field2
     */
    public Constraint(IType type1, Comparable field1, IType type2, Comparable field2) {
        if (type1 == null) {
            throw new NullPointerException("type1 may not be null");
        }
        if (field1 == null) {
            throw new NullPointerException("field1 may not be null");
        }
        if (type2 == null) {
            throw new NullPointerException("type2 may not be null");
        }
        if (field2 == null) {
            throw new NullPointerException("field2 may not be null");
        }
        if (DataConfiguration.getInstance().isEqual(type1, type2)) {
            throw new IllegalArgumentException("The types have to be different");
        }
        if (type1.getTypeDimension().equals(TypeDimension.One)) {
            throw new IllegalArgumentException("Type 1 (" + type1 + ") dimension needs to be " + TypeDimension.Two);
        }
        if (type2.getTypeDimension().equals(TypeDimension.One)) {
            throw new IllegalArgumentException("Type 2 (" + type2 + ")  dimension needs to be " + TypeDimension.Two);
        }
        this.type1 = type1;
        this.field1 = field1;
        this.type2 = type2;
        this.field2 = field2;
        signature = type1.getTypeName() + "-" + field1 + "-" + type2.getTypeName() + "-" + field2;
    }

    /**
     * @return the type1
     */
    public IType getType1() {
        return type1;
    }

    /**
     * @return the field1
     */
    public Comparable getField1() {
        return field1;
    }

    /**
     * @return the type2
     */
    public IType getType2() {
        return type2;
    }

    /**
     * @return the field2
     */
    public Comparable getField2() {
        return field2;
    }

    /**
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.signature);
        return hash;
    }

    /**
     * @param otherConstraint
     * @return
     */
    @Override
    public boolean equals(Object otherConstraint) {
        if (otherConstraint == null) {
            return false;
        }
        if (!(otherConstraint instanceof Constraint)) {
            return false;
        }
        return signature.equals(((Constraint) otherConstraint).signature);
    }

    /**
     * @param otherConstraint
     * @return
     */
    @Override
    public int compareTo(Constraint otherConstraint) {
        if (otherConstraint == null) {
            throw new NullPointerException("otherConstraint may not be null");
        }
        return signature.compareTo(otherConstraint.signature);
    }

    /**
     * @return
     */
    @Override
    public String toString() {
        return signature;
    }
}
