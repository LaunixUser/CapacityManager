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
