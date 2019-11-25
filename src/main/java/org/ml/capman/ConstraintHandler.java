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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.ml.tools.logging.LoggerFactory;

/**
 * @author mlaux
 */
public class ConstraintHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConstraintHandler.class.getName());

    private final Set<Constraint> constraints = new HashSet<>();
    private Map<String, Set<String>> mappings = new HashMap<>();

    /**
     *
     */
    public ConstraintHandler() {

    }

    /**
     * @param constraintHandler
     */
    public ConstraintHandler(ConstraintHandler constraintHandler) {
        if (constraintHandler == null) {
            throw new NullPointerException("constraintHandler may not be null");
        }
        for (Constraint constraint : constraintHandler.getConstraints()) {
            addConstraint(constraint);
        }
    }

    /**
     * @param type1
     * @param field1
     * @param type2
     * @param field2
     */
    public void addConstraint(IType type1, Comparable field1, IType type2, Comparable field2) {
        this.addConstraint(new Constraint(type1, field1, type2, field2));
    }

    /**
     * @param constraint
     */
    public void addConstraint(Constraint constraint) {
        if (constraint == null) {
            throw new NullPointerException("constraint may not be null");
        }
        constraints.add(constraint);

        //.... This is for a quick lookup if ANY constraints exist for the given types - it relies on both types being of TypeDimension.TWO
        if (!mappings.containsKey(constraint.getType1().getTypeName())) {
            mappings.put(constraint.getType1().getTypeName(), new HashSet<>());
        }
        mappings.get(constraint.getType1().getTypeName()).add(constraint.getType2().getTypeName());
    }

    /**
     * @param type1
     * @param type2
     * @return
     */
    public boolean existConstraints(IType type1, IType type2) {
        if (type1 == null) {
            throw new NullPointerException("type1 may not be null");
        }
        if (type2 == null) {
            throw new NullPointerException("type2 may not be null");
        }
        if (DataConfiguration.getInstance().isEqual(type1, type2)) {
            throw new IllegalArgumentException("The types have to be different");
        }
        if (type1.getTypeDimension().equals(DataConfiguration.TypeDimension.One)) {
            throw new IllegalArgumentException("Type 1 (" + type1 + ") dimension needs to be " + DataConfiguration.TypeDimension.Two);
        }
        if (type2.getTypeDimension().equals(DataConfiguration.TypeDimension.One)) {
            throw new IllegalArgumentException("Type 2 (" + type2 + ")  dimension needs to be " + DataConfiguration.TypeDimension.Two);
        }
        return mappings.containsKey(type1.getTypeName()) && mappings.get(type1.getTypeName()).contains(type2.getTypeName());
    }

    /**
     * @param type1
     * @param field1
     * @param type2
     * @param field2
     * @return
     */
    public boolean existsConstraint(IType type1, Comparable field1, IType type2, Comparable field2) {
        Constraint constraint = new Constraint(type1, field1, type2, field2);
        return constraints.contains(constraint);
    }

    /**
     * @return
     */
    public Set<Constraint> getConstraints() {
        return constraints;
    }

}
