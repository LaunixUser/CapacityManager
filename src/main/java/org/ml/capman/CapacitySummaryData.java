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
import java.util.TreeMap;

import static org.ml.capman.DataConfiguration.TypeDimension.One;
import static org.ml.capman.DataConfiguration.TypeDimension.Two;

import org.ml.capman.EmployeeCapacity.CapacityType;

/**
 * @author mlaux
 */
public class CapacitySummaryData extends BaseCapacitySummary {

    private Map<Comparable, Map<CapacityType, Double>> data = new TreeMap<>();
    private Map<CapacityType, Double> totalData = new HashMap<>();
    private Map<Comparable, Integer> count = new HashMap<>();
    private int totalCount = 0;

    /**
     * @param type
     * @param employeeData
     */
    public CapacitySummaryData(IType type, EmployeeData<Employee> employeeData) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (employeeData == null) {
            throw new NullPointerException("employeeData may not be null");
        }

        for (CapacityType capacityType : CapacityType.values()) {
            totalData.put(capacityType, 0.0d);
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

        if (!data.containsKey(primaryKey)) {
            data.put(primaryKey, new HashMap<>());
            for (CapacityType capacityType : CapacityType.values()) {
                data.get(primaryKey).put(capacityType, 0.0d);
            }
        }

        switch (type.getTypeDimension()) {

            case One:

                for (CapacityType capacityType : CapacityType.values()) {
                    double d = data.get(primaryKey).get(capacityType) + employee.getCapacity(capacityType);
                    double td = totalData.get(capacityType) + employee.getCapacity(capacityType);
                    data.get(primaryKey).put(capacityType, d);
                    totalData.put(capacityType, td);
                }
                break;

            case Two:

                double percentage = computePercentage(employee, type, primaryKey);

                for (CapacityType capacityType : CapacityType.values()) {
                    double c = percentage * employee.getCapacity(capacityType);
                    double d = data.get(primaryKey).get(capacityType) + c;
                    double td = totalData.get(capacityType) + c;
                    data.get(primaryKey).put(capacityType, d);
                    totalData.put(capacityType, td);
                }

        }

        //.... Data count for this primary key
        if (!count.containsKey(primaryKey)) {
            count.put(primaryKey, 0);
        }
        int c = count.get(primaryKey) + 1;
        count.put(primaryKey, c);

        // Data count for all primary keys
        totalCount++;
    }

    /**
     * @param primaryKey
     * @return
     */
    public Map<CapacityType, Double> get(Comparable primaryKey) {
        if (primaryKey == null) {
            throw new NullPointerException("primaryKey may not be null");
        }
        return data.get(primaryKey);
    }

    /**
     * @return
     */
    public Map<Comparable, Map<CapacityType, Double>> get() {
        return data;
    }

    /**
     * @return
     */
    public Map<CapacityType, Double> getTotal() {
        return totalData;
    }

    /**
     * @param primaryKey
     * @return
     */
    public Map<CapacityType, Double> getPercent(Comparable primaryKey) {
        if (primaryKey == null) {
            throw new NullPointerException("primaryKey may not be null");
        }
        Map<CapacityType, Double> d = new HashMap<>();
        for (CapacityType capacityType : CapacityType.values()) {
            d.put(capacityType, data.get(primaryKey).get(capacityType) / totalData.get(capacityType));
        }
        return d;
    }

    /**
     * @param primaryKey
     * @return
     */
    public Integer getCount(Comparable primaryKey) {
        if (primaryKey == null) {
            throw new NullPointerException("primaryKey may not be null");
        }
        return count.get(primaryKey);
    }

    /**
     * @return
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * @param primaryKey
     * @return
     */
    public double getCountPercent(Comparable primaryKey) {
        if (primaryKey == null) {
            throw new NullPointerException("primaryKey may not be null");
        }
        return (double) count.get(primaryKey) / (double) totalCount;
    }

}
