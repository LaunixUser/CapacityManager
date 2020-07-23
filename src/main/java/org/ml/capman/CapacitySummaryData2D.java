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
public class CapacitySummaryData2D extends BaseCapacitySummary {

    private Map<Comparable, Map<Comparable, Map<CapacityType, Double>>> data = new TreeMap<>();
    private Map<CapacityType, Double> totalData = new HashMap<>();
    private Map<Comparable, Map<Comparable, Integer>> count = new HashMap<>();
    private int totalCount = 0;

    /**
     * @param type1
     * @param type2
     * @param employeeData
     */
    public CapacitySummaryData2D(IType type1, IType type2, EmployeeData<Employee> employeeData) {
        if (type1 == null) {
            throw new NullPointerException("type1 may not be null");
        }
        if (type2 == null) {
            throw new NullPointerException("type2 may not be null");
        }
        if (employeeData == null) {
            throw new NullPointerException("employeeData may not be null");
        }

        for (CapacityType capacityType : CapacityType.values()) {
            totalData.put(capacityType, 0.0d);
        }

        switch (type1.getTypeDimension()) {

            case One:

                switch (type2.getTypeDimension()) {

                    case One:

                        for (Employee employee : employeeData.getEmployees()) {
                            update(employee, employee.get(type1), type1, employee.get(type2), type2);
                        }
                        break;

                    case Two:

                        for (Employee employee : employeeData.getEmployees()) {
                            for (Comparable primaryKey2 : employee.getFields(type2).keySet()) {
                                update(employee, employee.get(type1), type1, primaryKey2, type2);
                            }
                        }
                        break;
                }
                break;

            case Two:

                switch (type2.getTypeDimension()) {

                    case One:

                        for (Employee employee : employeeData.getEmployees()) {
                            for (Comparable primaryKey1 : employee.getFields(type1).keySet()) {
                                update(employee, primaryKey1, type1, employee.get(type2), type2);
                            }
                        }
                        break;

                    case Two:

                        if (employeeData.getConstraintHandler().existConstraints(type1, type2)) {

                            for (Employee employee : employeeData.getEmployees()) {
                                for (Comparable primaryKey1 : employee.getFields(type1).keySet()) {
                                    for (Comparable primaryKey2 : employee.getFields(type2).keySet()) {
                                        if (employeeData.getConstraintHandler().existsConstraint(type1, primaryKey1, type2, primaryKey2)) {
                                            update(employee, primaryKey1, type1, primaryKey2, type2, true);
                                        }
                                    }
                                }
                            }

                        } else {

                            for (Employee employee : employeeData.getEmployees()) {
                                for (Comparable primaryKey1 : employee.getFields(type1).keySet()) {
                                    for (Comparable primaryKey2 : employee.getFields(type2).keySet()) {
                                        update(employee, primaryKey1, type1, primaryKey2, type2);
                                    }
                                }
                            }

                        }
                        break;
                }
                break;

        }
    }

    /**
     * @param employee
     * @param primaryKey1
     * @param type1
     * @param primaryKey2
     * @param type2
     */
    private void update(Employee employee, Comparable primaryKey1, IType type1, Comparable primaryKey2, IType type2) {
        update(employee, primaryKey1, type1, primaryKey2, type2, false);
    }

    /**
     * @param employee
     * @param primaryKey1
     * @param type1
     * @param primaryKey2
     * @param type2
     * @param constrained
     */
    private void update(Employee employee, Comparable primaryKey1, IType type1, Comparable primaryKey2, IType type2, boolean constrained) {
        if (employee == null) {
            throw new NullPointerException("employee may not be null");
        }
        if (primaryKey1 == null) {
            throw new NullPointerException("primaryKey1 may not be null");
        }
        if (type1 == null) {
            throw new NullPointerException("type1 may not be null");
        }
        if (primaryKey2 == null) {
            throw new NullPointerException("primaryKey2 may not be null");
        }
        if (type2 == null) {
            throw new NullPointerException("type2 may not be null");
        }

        if (!data.containsKey(primaryKey1)) {
            data.put(primaryKey1, new TreeMap<>());
        }
        if (!data.get(primaryKey1).containsKey(primaryKey2)) {
            data.get(primaryKey1).put(primaryKey2, new HashMap<>());
            for (CapacityType capacityType : CapacityType.values()) {
                data.get(primaryKey1).get(primaryKey2).put(capacityType, 0.0d);
            }
        }
        if (!count.containsKey(primaryKey1)) {
            count.put(primaryKey1, new HashMap<>());
        }
        if (!count.get(primaryKey1).containsKey(primaryKey2)) {
            count.get(primaryKey1).put(primaryKey2, 0);
        }

        switch (type1.getTypeDimension()) {

            case One:

                switch (type2.getTypeDimension()) {

                    case One:

                        for (CapacityType capacityType : CapacityType.values()) {
                            double d = data.get(primaryKey1).get(primaryKey2).get(capacityType) + employee.getCapacity(capacityType);
                            double td = totalData.get(capacityType) + employee.getCapacity(capacityType);
                            data.get(primaryKey1).get(primaryKey2).put(capacityType, d);
                            totalData.put(capacityType, td);
                        }
                        break;

                    case Two:

                        for (CapacityType capacityType : CapacityType.values()) {
                            double percentage = computePercentage(employee, type2, primaryKey2);
                            double val = percentage * employee.getCapacity(capacityType);
                            double d = data.get(primaryKey1).get(primaryKey2).get(capacityType) + val;
                            double td = totalData.get(capacityType) + val;
                            data.get(primaryKey1).get(primaryKey2).put(capacityType, d);
                            totalData.put(capacityType, td);
                        }
                        break;
                }
                break;

            case Two:

                switch (type2.getTypeDimension()) {

                    case One:

                        for (CapacityType capacityType : CapacityType.values()) {
                            double percentage = computePercentage(employee, type1, primaryKey1);
                            double val = percentage * employee.getCapacity(capacityType);
                            double d = data.get(primaryKey1).get(primaryKey2).get(capacityType) + val;
                            double td = totalData.get(capacityType) + val;
                            data.get(primaryKey1).get(primaryKey2).put(capacityType, d);
                            totalData.put(capacityType, td);
                        }
                        break;

                    case Two:

                        for (CapacityType capacityType : CapacityType.values()) {
                            double percentage1 = 1.0d;
                            if (!constrained) {
                                percentage1 = computePercentage(employee, type1, primaryKey1);
                            }
                            double percentage2 = computePercentage(employee, type2, primaryKey2);
                            double val = percentage1 * percentage2 * employee.getCapacity(capacityType);
                            //                            System.out.println("XX " + employee.getName() + " " + type1 + " " + type2 + ": " + primaryKey1 + " - " + percentage1 + " / " + primaryKey2 + " - " + percentage2 + " / " + val);
                            double d = data.get(primaryKey1).get(primaryKey2).get(capacityType) + val;
                            double td = totalData.get(capacityType) + val;
                            data.get(primaryKey1).get(primaryKey2).put(capacityType, d);
                            totalData.put(capacityType, td);
                        }
                        break;
                }
                break;

        }

        //.... Data count
        int c = count.get(primaryKey1).get(primaryKey2) + 1;
        count.get(primaryKey1).put(primaryKey2, c);

        // Data count for all primary keys
        totalCount++;
    }

    /**
     * @return
     */
    public Map<Comparable, Map<Comparable, Map<CapacityType, Double>>> get() {
        return data;
    }

    /**
     * @param primaryKey1
     * @return
     */
    public Map<Comparable, Map<CapacityType, Double>> get(Comparable primaryKey1) {
        if (primaryKey1 == null) {
            throw new NullPointerException("primaryKey1 may not be null");
        }
        return data.get(primaryKey1);
    }

    /**
     * @param primaryKey1
     * @param primaryKey2
     * @return
     */
    public Map<CapacityType, Double> get(Comparable primaryKey1, Comparable primaryKey2) {
        if (primaryKey1 == null) {
            throw new NullPointerException("primaryKey1 may not be null");
        }
        if (primaryKey2 == null) {
            throw new NullPointerException("primaryKey2 may not be null");
        }
        return data.get(primaryKey1).get(primaryKey2);
    }

    /**
     * @return
     */
    public Map<CapacityType, Double> getTotal() {
        return totalData;
    }

    /**
     * @param primaryKey1
     * @param primaryKey2
     * @return
     */
    public Map<CapacityType, Double> getPercent(Comparable primaryKey1, Comparable primaryKey2) {
        if (primaryKey1 == null) {
            throw new NullPointerException("primaryKey1 may not be null");
        }
        if (primaryKey2 == null) {
            throw new NullPointerException("primaryKey2 may not be null");
        }
        Map<CapacityType, Double> d = new HashMap<>();
        for (CapacityType capacityType : CapacityType.values()) {
            d.put(capacityType, data.get(primaryKey1).get(primaryKey2).get(capacityType) / totalData.get(capacityType));
        }
        return d;
    }

    /**
     * @param primaryKey1
     * @param primaryKey2
     * @return
     */
    public Integer getCount(Comparable primaryKey1, Comparable primaryKey2) {
        if (primaryKey1 == null) {
            throw new NullPointerException("primaryKey1 may not be null");
        }
        if (primaryKey2 == null) {
            throw new NullPointerException("primaryKey2 may not be null");
        }
        return count.get(primaryKey1).get(primaryKey2);
    }

    /**
     * @return
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * @param primaryKey1
     * @param primaryKey2
     * @return
     */
    public double getCountPercent(Comparable primaryKey1, Comparable primaryKey2) {
        if (primaryKey1 == null) {
            throw new NullPointerException("primaryKey1 may not be null");
        }
        if (primaryKey2 == null) {
            throw new NullPointerException("primaryKey2 may not be null");
        }
        return (double) count.get(primaryKey1).get(primaryKey2) / (double) totalCount;
    }

}
