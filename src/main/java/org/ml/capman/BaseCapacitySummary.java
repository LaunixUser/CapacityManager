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
 * @author mlaux
 */
public class BaseCapacitySummary {

    /**
     * @param type
     * @param employee
     * @param primaryKey
     * @return
     */
    public double computePercentage(Employee employee, IType type, Comparable primaryKey) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (employee == null) {
            throw new NullPointerException("employee may not be null");
        }
        if (primaryKey == null) {
            throw new NullPointerException("primaryKey may not be null");
        }

        return getFactor(employee, type, primaryKey);
    }

    /**
     * @param employee
     * @param type1
     * @param primaryKey1
     * @param type2
     * @param primaryKey2
     * @return
     */
    public double computePercentage(Employee employee, IType type1, Comparable primaryKey1, IType type2, Comparable primaryKey2) {
        return computePercentage(employee, type1, primaryKey1, type2, primaryKey2, false);
    }

    /**
     * @param employee
     * @param type1
     * @param primaryKey1
     * @param type2
     * @param primaryKey2
     * @param constrained
     * @return
     */
    public double computePercentage(Employee employee, IType type1, Comparable primaryKey1, IType type2, Comparable primaryKey2, boolean constrained) {
        if (employee == null) {
            throw new NullPointerException("employee may not be null");
        }
        if (type1 == null) {
            throw new NullPointerException("type1 may not be null");
        }
        if (primaryKey1 == null) {
            throw new NullPointerException("primaryKey1 may not be null");
        }
        if (type2 == null) {
            throw new NullPointerException("type2 may not be null");
        }
        if (primaryKey2 == null) {
            throw new NullPointerException("primaryKey2 may not be null");
        }

        double percentage = 0.0d;

        switch (type1.getTypeDimension()) {
            case One:
                percentage = getFactor(employee, type2, primaryKey2);
                break;
            case Two:
                switch (type2.getTypeDimension()) {
                    case One:
                        percentage = getFactor(employee, type1, primaryKey1);
                        break;
                    case Two:
                        double factor1 = 1.0d;
                        if (!constrained) {
                            factor1 = getFactor(employee, type1, primaryKey1);
                        }
                        percentage = factor1 * getFactor(employee, type2, primaryKey2);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported TypeDimension, type = " + type2);

                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported TypeDimension, type = " + type1);

        }

        return percentage;
    }

    /**
     * @param employee
     * @param type
     * @param primaryKey
     * @return
     */
    private double getFactor(Employee employee, IType type, Comparable primaryKey) {
        if (employee == null) {
            throw new NullPointerException("employee may not be null");
        }
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (primaryKey == null) {
            throw new NullPointerException("primaryKey may not be null");
        }
        double percentage = 0.0d;
        switch (type.getTypeDimension()) {
            case One:
                return 1.0d;
            case Two:
                switch (type.getTypeKind()) {
                    case TypeDoublePercentage:
                        percentage = 0.01d * (Double) employee.get(type, (String) primaryKey);
                        break;
                    case TypeIntegerPercentage:
                        percentage = 0.01d * (Integer) employee.get(type, (String) primaryKey);
                        break;
                    default:
                        int n = employee.getFields(type).size();
                        if (n > 0) {
                            percentage = 1.0d / n;
                        } else {
                            percentage = 0.0d;
                        }
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported TypeDimension, type = " + type);
        }
        return percentage;
    }

}
