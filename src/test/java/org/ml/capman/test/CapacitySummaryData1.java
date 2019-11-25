package org.ml.capman.test;

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


import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.ml.capman.DataConfiguration.TypeDimension;

import static org.ml.capman.DataConfiguration.TypeDimension.One;
import static org.ml.capman.DataConfiguration.TypeDimension.Two;
import static org.ml.capman.DataConfiguration.TypeKind.TypeDoublePercentage;
import static org.ml.capman.DataConfiguration.TypeKind.TypeIntegerPercentage;

import org.ml.capman.Employee;
import org.ml.capman.EmployeeCapacity.CapacityType;
import org.ml.capman.EmployeeData;
import org.ml.capman.IType;

/**
 * @author mlaux
 */
public class CapacitySummaryData1 {

    private Map<TypeDimension, Map<Comparable, Map<CapacityType, Double>>> data = new HashMap<>();
    private Map<TypeDimension, Map<CapacityType, Double>> totalData = new HashMap<>();
    private Map<TypeDimension, Map<Comparable, Integer>> count = new HashMap<>();
    private Map<TypeDimension, Integer> totalCount = new HashMap<>();

    /**
     *
     */
    public CapacitySummaryData1() {
        for (TypeDimension typeDimension : TypeDimension.values()) {
            data.put(typeDimension, new TreeMap<>());
            totalData.put(typeDimension, new HashMap<>());
            for (CapacityType capacityType : CapacityType.values()) {
                totalData.get(typeDimension).put(capacityType, 0.0d);
            }
            count.put(typeDimension, new HashMap<>());
            totalCount.put(typeDimension, 0);
        }
    }

    /**
     * @param type
     * @param employeeData
     */
    public void add(IType type, EmployeeData<Employee> employeeData) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (employeeData == null) {
            throw new NullPointerException("employeeData may not be null");
        }

        switch (type.getTypeDimension()) {
            case One:
                for (Employee employee : employeeData.getEmployees()) {
                    update(employee, employee.get(type), type);
                }
                break;
            case Two:
                for (Employee employee : employeeData.getEmployees()) {
                    for (String primaryKey : employee.getFields(type).keySet()) {
                        update(employee, primaryKey, type);
                    }
                }
        }
    }

    /**
     * @param employee
     * @param primaryKey
     * @param type
     */
    private void update(Employee employee, Comparable primaryKey, IType type) {
        if (employee == null) {
            throw new NullPointerException("employee may not be null");
        }
        if (primaryKey == null) {
            throw new NullPointerException("primaryKey may not be null");
        }
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }

        TypeDimension typeDimension = type.getTypeDimension();

        if (!data.get(typeDimension).containsKey(primaryKey)) {
            data.get(typeDimension).put(primaryKey, new HashMap<>());
            for (CapacityType capacityType : CapacityType.values()) {
                data.get(typeDimension).get(primaryKey).put(capacityType, 0.0d);
            }
        }

        switch (typeDimension) {

            case One:

                for (CapacityType capacityType : CapacityType.values()) {
                    double d = data.get(typeDimension).get(primaryKey).get(capacityType) + employee.getCapacity(capacityType);
                    double td = totalData.get(typeDimension).get(capacityType) + employee.getCapacity(capacityType);
                    data.get(typeDimension).get(primaryKey).put(capacityType, d);
                    totalData.get(typeDimension).put(capacityType, td);
                }
                break;

            case Two:

                double percentage = 0.0d;

                switch (type.getTypeKind()) {
                    case TypeDoublePercentage:
                        percentage = 0.01d * (Double) employee.get(type, (String) primaryKey);
                        break;
                    case TypeIntegerPercentage:
                        percentage = 0.01d * (Integer) employee.get(type, (String) primaryKey);
                        break;
                    default:

                        //.... Since we don't have percentage-based weights for the capacity types, we assume an 
                        //     equal distribution across the number of field values for a given type. This is clearly just an approximation
                        //     but the best we can do here
                        int n = employee.getFields(type).size();
                        if (n > 0) {
                            percentage = 1.0d / n;
                        } else {
                            percentage = 0.0d;
                        }

                }

                for (CapacityType capacityType : CapacityType.values()) {
                    double c = percentage * employee.getCapacity(capacityType);
                    double d = data.get(typeDimension).get(primaryKey).get(capacityType) + c;
                    double td = totalData.get(typeDimension).get(capacityType) + c;
                    data.get(typeDimension).get(primaryKey).put(capacityType, d);
                    totalData.get(typeDimension).put(capacityType, td);
                }

        }

        //.... Data count for this primary key
        if (!count.get(typeDimension).containsKey(primaryKey)) {
            count.get(typeDimension).put(primaryKey, 0);
        }
        int c = count.get(typeDimension).get(primaryKey) + 1;
        count.get(typeDimension).put(primaryKey, c);

        // Data count for all primary keys
        int t = totalCount.get(typeDimension) + 1;
        totalCount.put(typeDimension, t);
    }

    /**
     * @param typeDimension
     * @param primaryKey
     * @return
     */
    public Map<CapacityType, Double> get(TypeDimension typeDimension, Comparable primaryKey) {
        if (typeDimension == null) {
            throw new NullPointerException("typeDimension may not be null");
        }
        if (primaryKey == null) {
            throw new NullPointerException("primaryKey may not be null");
        }
        return data.get(typeDimension).get(primaryKey);
    }

    /**
     * @param typeDimension
     * @return
     */
    public Map<Comparable, Map<CapacityType, Double>> get(TypeDimension typeDimension) {
        if (typeDimension == null) {
            throw new NullPointerException("typeDimension may not be null");
        }
        return data.get(typeDimension);
    }

    /**
     * @param typeDimension
     * @return
     */
    public Map<CapacityType, Double> getTotal(TypeDimension typeDimension) {
        if (typeDimension == null) {
            throw new NullPointerException("typeDimension may not be null");
        }
        return totalData.get(typeDimension);
    }

    /**
     * @param typeDimension
     * @param primaryKey
     * @return
     */
    public Map<CapacityType, Double> getPercent(TypeDimension typeDimension, Comparable primaryKey) {
        if (typeDimension == null) {
            throw new NullPointerException("typeDimension may not be null");
        }
        if (primaryKey == null) {
            throw new NullPointerException("primaryKey may not be null");
        }
        Map<CapacityType, Double> d = new HashMap<>();
        for (CapacityType capacityType : CapacityType.values()) {
            d.put(capacityType, data.get(typeDimension).get(primaryKey).get(capacityType) / totalData.get(typeDimension).get(capacityType));
        }
        return d;
    }

    /**
     * @param typeDimension
     * @param primaryKey
     * @return
     */
    public Integer getCount(TypeDimension typeDimension, Comparable primaryKey) {
        if (primaryKey == null) {
            throw new NullPointerException("primaryKey may not be null");
        }
        if (typeDimension == null) {
            throw new NullPointerException("typeDimension may not be null");
        }
        return count.get(typeDimension).get(primaryKey);
    }

    /**
     * @param typeDimension
     * @return
     */
    public Integer getTotalCount(TypeDimension typeDimension) {
        if (typeDimension == null) {
            throw new NullPointerException("typeDimension may not be null");
        }
        return totalCount.get(typeDimension);
    }

    /**
     * @param typeDimension
     * @param primaryKey
     * @return
     */
    public double getCountPercent(TypeDimension typeDimension, Comparable primaryKey) {
        if (primaryKey == null) {
            throw new NullPointerException("primaryKey may not be null");
        }
        if (typeDimension == null) {
            throw new NullPointerException("typeDimension may not be null");
        }
        return (double) count.get(typeDimension).get(primaryKey) / (double) totalCount.get(typeDimension);
    }

}
