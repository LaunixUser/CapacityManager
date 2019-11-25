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
import java.util.Map;

/**
 * @author mlaux
 */
public class EmployeeCapacity {

    private Map<CapacityType, Double> data = new HashMap<>();
    public final static double NOT_SET = -1.0d;
    private boolean checkConstraint = false;
    private final static double DEFAULT_LOWER_BOUND = 0.0d;
    private final static double DEFAULT_UPPER_BOUND = 1.0d;
    private double lowerBound = DEFAULT_LOWER_BOUND;
    private double upperBound = DEFAULT_UPPER_BOUND;

    /**
     *
     */
    public enum CapacityType {
        HC, FTE, EffectiveFTE
    }

    /**
     *
     */
    public EmployeeCapacity() {
        for (CapacityType capacityType : CapacityType.values()) {
            data.put(capacityType, NOT_SET);
        }
    }

    /**
     * @param checkConstraint
     */
    public EmployeeCapacity(boolean checkConstraint) {
        this();
        this.checkConstraint = checkConstraint;
    }

    /**
     * @param checkConstraint
     * @param lowerBound
     * @param upperBound
     */
    public EmployeeCapacity(boolean checkConstraint, double lowerBound, double upperBound) {
        this();
        if (lowerBound >= upperBound) {
            throw new IllegalArgumentException("lower bound needs to be below upper bound");
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * @param type
     * @param value
     */
    public void set(CapacityType type, double value) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (checkConstraint) {
            if (value < lowerBound || value > upperBound) {
                throw new IllegalArgumentException("value '" + value + "' is out of bounds [" + lowerBound + "/" + upperBound + "]");
            }
        }
        data.put(type, value);
    }

    /**
     * @param capacityType
     * @return
     */
    public double get(CapacityType capacityType) {
        if (capacityType == null) {
            throw new NullPointerException("capacityType may not be null");
        }
        return data.get(capacityType);
    }

    /**
     * @param capacityType
     * @return
     */
    public int getAsInt(CapacityType capacityType) {
        if (capacityType == null) {
            throw new NullPointerException("capacityType may not be null");
        }
        return (int) Math.round(data.get(capacityType));
    }

    /**
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(300);

        for (CapacityType capacityType : CapacityType.values()) {
            sb.append("CapacityType ");
            sb.append(capacityType);
            sb.append(" : ");
            sb.append(data.get(capacityType));
            sb.append("\n");
        }
        return sb.toString();
    }
}
