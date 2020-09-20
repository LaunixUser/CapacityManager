package org.ml.capman.reporting.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.ml.tools.PropertyManager;
import org.ml.tools.logging.LoggerFactory;
import org.ml.capman.CapacitySummaryData2D;
import org.ml.capman.DataConfiguration;
import org.ml.capman.DataConfiguration.TypeDimension;

import static org.ml.capman.DataConfiguration.TypeDimension.One;
import static org.ml.capman.DataConfiguration.TypeDimension.Two;

import org.ml.capman.Employee;
import org.ml.capman.EmployeeCapacity.CapacityType;
import org.ml.capman.EmployeeData;
import org.ml.capman.IType;
import org.ml.capman.reporting.AbstractDirectTableDataStep;

import static org.ml.capman.render.RenderingType.*;

import org.ml.pf.output.TableData;
import org.ml.table.Cell;
import org.ml.table.Table;

/**
 * @author mlaux
 */
public class TypeListStep2D extends AbstractDirectTableDataStep {

    private final static Logger LOGGER = LoggerFactory.getLogger(TypeListStep2D.class.getName());

    /**
     * @param id
     * @param comparator
     * @param propertyManager
     */
    public TypeListStep2D(String id, Comparator<Employee> comparator, PropertyManager propertyManager) {
        super(id, propertyManager);
        if (comparator == null) {
            throw new NullPointerException("comparator may not be null");
        }
        this.comparator = comparator;
    }

    /**
     * @param comparator
     * @param propertyManager
     */
    public TypeListStep2D(Comparator<Employee> comparator, PropertyManager propertyManager) {
        super(propertyManager);
        if (comparator == null) {
            throw new NullPointerException("comparator may not be null");
        }
        this.comparator = comparator;
    }

    /**
     * @param employeeData
     * @return
     */
    @Override
    public Map<String, TableData> createOutputData(EmployeeData<Employee> employeeData) {
        if (employeeData == null) {
            throw new NullPointerException("employeeData may not be null");
        }

        int minimumLevel = propertyManager.getInt(OptionalKey.minimumLevel, 0);
        String setContext = propertyManager.getString(OptionalKey.setContext, "");

        DataConfiguration configuration = DataConfiguration.getInstance();

        Map<String, TableData> tables = new TreeMap<>();
        
        for (TypeDimension typeDimension1 : TypeDimension.values()) {
            for (String typeName1 : configuration.get(typeDimension1).keySet()) {
                IType type1 = configuration.get(typeDimension1).get(typeName1);
                if (type1.getLevel() >= minimumLevel) {
                    for (TypeDimension typeDimension2 : TypeDimension.values()) {
                        for (String typeName2 : configuration.get(typeDimension2).keySet()) {
                            IType type2 = configuration.get(typeDimension2).get(typeName2);
                            if (type2.getLevel() >= minimumLevel) {
                                if (!configuration.isEqual(type1, type2)) {
                                    TableData tableData = new TableData(propertyManager);
                                    if (setContext.length() > 0) {
                                        tableData.setTableHeader("Employees for " + typeName2 + " by " + typeName1 + " (Data Context: " + setContext + ")");
                                    } else {
                                        tableData.setTableHeader("Employees for " + typeName2 + " by " + typeName1);
                                    }
                                    tableData.addTable("tableBody", createTable(employeeData, type1, type2));
                                    tableData.setDescription("Employees " + typeName2 + " by " + typeName1);
                                    tables.put("employeeList2D_" + typeName2 + "_by_" + typeName1, tableData);
                                }
                            }
                        }
                    }
                }
            }
        }
        setDescription = propertyManager.getString(OptionalKey.setDescription, "Employee Lists 2D");

        return tables;

    }

    /**
     * @param typeName1
     * @param typeName2
     * @return
     */
//    public static String getFileName(String typeName1, String typeName2) {
//        if (typeName1 == null) {
//            throw new NullPointerException("typeName1 may not be null");
//        }
//        if (typeName2 == null) {
//            throw new NullPointerException("typeName2 may not be null");
//        }
//        return "employeeList2D_" + typeName1 + "_by_" + typeName2;
//    }

    /**
     * @param employeeData
     * @param type1
     * @param type2
     * @return
     */
    private Table createTable(EmployeeData<Employee> employeeData, IType type1, IType type2) {
        if (employeeData == null) {
            throw new IllegalArgumentException("employeeData may not be null");
        }
        if (type1 == null) {
            throw new NullPointerException("type may not be null");
        }
        if (type2 == null) {
            throw new NullPointerException("type2 may not be null");
        }

        //.... Collect capacity summary data for the given types
        CapacitySummaryData2D summaryData = new CapacitySummaryData2D(type1, type2, employeeData);

        //.... Collect employees 
        Map<Comparable, Map<Comparable, Set<Employee>>> employees = new TreeMap<>();
        for (Employee employee : employeeData.getEmployees()) {

            switch (type1.getTypeDimension()) {

                case One:

                    Comparable primaryKey1 = employee.get(type1);
                    if (!employees.containsKey(primaryKey1)) {
                        employees.put(primaryKey1, new TreeMap<>());
                    }

                    switch (type2.getTypeDimension()) {
                        case One:
                            Comparable primaryKey2 = employee.get(type2);
                            if (!employees.get(primaryKey1).containsKey(primaryKey2)) {
                                employees.get(primaryKey1).put(primaryKey2, new TreeSet<>(comparator));
                            }
                            employees.get(primaryKey1).get(primaryKey2).add(employee);
                            break;
                        case Two:
                            for (String field2 : employee.getFields(type2).keySet()) {
                                if (!employees.get(primaryKey1).containsKey(field2)) {
                                    employees.get(primaryKey1).put(field2, new TreeSet<>(comparator));
                                }
                                employees.get(primaryKey1).get(field2).add(employee);
                            }
                            break;
                    }

                    break;

                case Two:

                    for (String field1 : employee.getFields(type1).keySet()) {
                        if (!employees.containsKey(field1)) {
                            employees.put(field1, new TreeMap<>());
                        }
                    }

                    switch (type2.getTypeDimension()) {
                        case One:
                            Comparable primaryKey2 = employee.get(type2);
                            for (String field1 : employee.getFields(type1).keySet()) {
                                if (!employees.get(field1).containsKey(primaryKey2)) {
                                    employees.get(field1).put(primaryKey2, new TreeSet<>(comparator));
                                }
                                employees.get(field1).get(primaryKey2).add(employee);
                            }
                            break;
                        case Two:

                            if (employeeData.getConstraintHandler().existConstraints(type1, type2)) {

                                for (String field1 : employee.getFields(type1).keySet()) {
                                    for (String field2 : employee.getFields(type2).keySet()) {
                                        if (employeeData.getConstraintHandler().existsConstraint(type1, field1, type2, field2)) {
                                            if (!employees.get(field1).containsKey(field2)) {
                                                employees.get(field1).put(field2, new TreeSet<>(comparator));
                                            }
                                            employees.get(field1).get(field2).add(employee);
                                        }
                                    }
                                }

                            } else {

                                for (String field1 : employee.getFields(type1).keySet()) {
                                    for (String field2 : employee.getFields(type2).keySet()) {
                                        if (!employees.get(field1).containsKey(field2)) {
                                            employees.get(field1).put(field2, new TreeSet<>(comparator));
                                        }
                                        employees.get(field1).get(field2).add(employee);
                                    }
                                }

                            }
                            break;
                    }

                    break;

                default:

                    throw new UnsupportedOperationException("Not supported: TypeDimension " + type1.getTypeDimension());

            }
        }

        //.... Create the output table
        Table table = new Table();
        table.setGrow();
        //    table.setRenderer(new SimpleVelocityRenderer());

        int row = 0;
        int col = 0;

        //.... Header
        table.setCell(new Cell().setStyle(cellCenterBold).setContent("#"), row, col++);
        table.setCell(new Cell().setStyle(cellLeftBold).setContent(type1.getTypeName()), row, col++);
        table.setCell(new Cell().setStyle(cellCenterBold).setContent("Counter"), row, col++);
        table.setCell(new Cell().setStyle(cellLeftBold).setContent(type2.getTypeName()), row, col++);
        table.setCell(new Cell().setStyle(cellCenterBold).setContent("Counter"), row, col++);
        table.setCell(new Cell().setStyle(cellLeftBold).setContent("Employee"), row, col++);
        table.setCell(new Cell().setStyle(cellLeftBold).setContent("ID"), row, col++);
        for (IType t : outputTypes) {
            table.setCell(new Cell().setStyle(cellLeftBold).setContent(t.toString()), row, col++);
        }
        table.setCell(new Cell().setStyle(cellLeftBold).setContent("Manager"), row, col++);
        table.setCell(new Cell().setStyle(cellLeftBold).setContent("ID"), row, col++);
        for (IType t : outputTypes) {
            table.setCell(new Cell().setStyle(cellLeftBold).setContent(t.toString()), row, col++);
        }

        if (type2.getTypeDimension().equals(TypeDimension.Two)) {
            table.setCell(new Cell().setStyle(cellLeftBold).setContent(getHeaderString(type2)), row, col++);
        }
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setStyle(cellLeftBold).setContent(capacityType), row, col++);
        }
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setStyle(cellLeftBold).setContent(capacityType + " (weighted)"), row, col++);
        }

        row++;

        //.... Body
        int n = 1;
        for (Comparable primaryKey1 : employees.keySet()) {

            int count1 = 1;

            for (Comparable primaryKey2 : employees.get(primaryKey1).keySet()) {

                int count2 = 1;
                boolean checkForConstraint = type1.getTypeDimension().equals(TypeDimension.Two) && type2.getTypeDimension().equals(TypeDimension.Two);

                for (Employee employee : employees.get(primaryKey1).get(primaryKey2)) {

                    col = 0;

                    table.setCell(new Cell().setStyle(cellCenter).setContent(n), row, col++);
                    table.setCell(new Cell().setStyle(cellLeft).setContent(primaryKey1), row, col++);
                    table.setCell(new Cell().setStyle(cellCenter).setContent(count1), row, col++);
                    table.setCell(new Cell().setStyle(cellLeft).setContent(primaryKey2), row, col++);
                    table.setCell(new Cell().setStyle(cellCenter).setContent(count2), row, col++);
                    table.setCell(new Cell().setStyle(cellLeft).setContent(employee.getUrl(Employee.EmployeeUrl.DATA_NAME)), row, col++);
                    table.setCell(new Cell().setStyle(cellLeft).setContent(employee.getUrl(Employee.EmployeeUrl.DATA_ID)), row, col++);
                    for (IType t : outputTypes) {
                        table.setCell(new Cell().setStyle(cellLeft).setContent(employee.get(t)), row, col++);
                    }
                    if (employee.hasManager()) {
                        table.setCell(new Cell().setStyle(cellLeft).setContent(employee.getManager().getUrl(Employee.EmployeeUrl.DATA_NAME)), row, col++);
                        table.setCell(new Cell().setStyle(cellCenter).setContent(employee.getManager().getUrl(Employee.EmployeeUrl.ORGA_ID)), row, col++);
                        for (IType t : outputTypes) {
                            table.setCell(new Cell().setStyle(cellLeft).setContent(employee.getManager().get(t)), row, col++);
                        }
                    } else {
                        for (int i = 0; i < 2 + outputTypes.size(); i++) {
                            table.setCell(new Cell().setStyle(cellLeft).setContent(""), row, col++);
                        }
                    }

                    if (type2.getTypeDimension().equals(TypeDimension.Two)) {
                        table.setCell(new Cell().setStyle(cellCenter).setContent(employee.get(type2, (String) primaryKey2)), row, col++);
                    }

                    for (CapacityType capacityType : CapacityType.values()) {
                        table.setCell(new Cell().setStyle(cellCenter).setContent(employee.getCapacity(capacityType)), row, col++);
                    }

                    double percentage;
                    if (checkForConstraint) {
                        percentage = summaryData.computePercentage(employee, type1, primaryKey1, type2, primaryKey2, employeeData.getConstraintHandler().existsConstraint(type1, primaryKey1, type2, primaryKey2));
                    } else {
                        percentage = summaryData.computePercentage(employee, type1, primaryKey1, type2, primaryKey2);
                    }

                    for (CapacityType capacityType : CapacityType.values()) {
                        table.setCell(new Cell().setStyle(cellCenter).setContent(percentage * employee.getCapacity(capacityType)), row, col++);
                    }

                    row++;
                    count2++;
                }
                count1++;
            }
            n++;
        }

        //.... Totals
        col = 0;
        table.setCell(new Cell(1, 9).setStyle(cellLeftEmpty).setContent("Totals"), row, col);
        col += 9;
        if (type2.getTypeDimension().equals(TypeDimension.Two)) {
            table.setCell(new Cell().setStyle(cellLeftEmpty), row, col++);
        }
        table.setCell(new Cell(1, CapacityType.values().length).setStyle(cellLeftEmpty), row, col);
        col += CapacityType.values().length;
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setStyle(cellCenterEmpty).setContent(summaryData.getTotal().get(capacityType)), row, col++);
        }

        table.compact();
        return table;
    }

}
