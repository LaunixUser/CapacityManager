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
import org.ml.capman.CapacitySummaryData;
import org.ml.capman.DataConfiguration;
import org.ml.capman.DataConfiguration.TypeDimension;

import static org.ml.capman.DataConfiguration.TypeDimension.One;
import static org.ml.capman.DataConfiguration.TypeDimension.Two;

import org.ml.capman.Employee;
import org.ml.capman.EmployeeCapacity.CapacityType;
import org.ml.capman.EmployeeData;
import org.ml.capman.IType;
import org.ml.capman.reporting.AbstractDirectTableDataStep;

import static org.ml.capman.render.AbstractTableCreator.DEFAULT_TABLE_SIZE;
import static org.ml.capman.render.AbstractTableCreator.KEY_STYLE;
import static org.ml.capman.render.RenderingType.*;

import org.ml.pf.output.TableData;
import org.ml.table.Cell;
import org.ml.table.Table;

/**
 * @author mlaux
 */
public class TypeListStep extends AbstractDirectTableDataStep {

    private final static Logger LOGGER = LoggerFactory.getLogger(TypeListStep.class.getName());

    /**
     * @param id
     * @param comparator
     * @param propertyManager
     */
    public TypeListStep(String id, Comparator<Employee> comparator, PropertyManager propertyManager) {
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
    public TypeListStep(Comparator<Employee> comparator, PropertyManager propertyManager) {
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

        Map<String, TableData> tables = new HashMap<>();

        for (TypeDimension typeDimension : TypeDimension.values()) {
            for (String typeName : DataConfiguration.getInstance().get(typeDimension).keySet()) {
                IType type = DataConfiguration.getInstance().get(typeDimension).get(typeName);
                if (type.getLevel() >= minimumLevel) {
                    TableData tableData = new TableData(propertyManager);
                    if (setContext.length() > 0) {
                        tableData.setTableHeader("Employees by " + typeName + " (Data Context: " + setContext + ")");
                    } else {
                        tableData.setTableHeader("Employees by " + typeName);
                    }
                    tableData.addTable("tableBody", createTable(employeeData, type));
                    tableData.setDescription("Employees by " + typeName);
                    tables.put("employeeListBy_" + typeName, tableData);
                }
            }
        }
        setDescription = propertyManager.getString(OptionalKey.setDescription, "Employee Lists");

        return tables;

    }

    /**
     * @param employeeData
     * @param type
     * @return
     */
    private Table createTable(EmployeeData<Employee> employeeData, IType type) {
        if (employeeData == null) {
            throw new IllegalArgumentException("employeeData may not be null");
        }
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }

        CapacitySummaryData summaryData = new CapacitySummaryData(type, employeeData);

        //.... Collect employees 
        Map<Comparable, Set<Employee>> employees = new TreeMap<>();
        for (Employee employee : employeeData.getEmployees()) {

            switch (type.getTypeDimension()) {

                case One:

                    Comparable primaryKey = employee.get(type);
                    if (!employees.containsKey(primaryKey)) {
                        employees.put(primaryKey, new TreeSet<>(comparator));
                    }
                    employees.get(primaryKey).add(employee);
                    break;

                case Two:

                    for (String field : employee.getFields(type).keySet()) {
                        if (!employees.containsKey(field)) {
                            employees.put(field, new TreeSet<>(comparator));
                        }
                        employees.get(field).add(employee);
                    }
                    break;

                default:

                    throw new UnsupportedOperationException("Not supported: TypeDimension " + type.getTypeDimension());

            }
        }

        //.... Create the output table
        Table table = new Table(DEFAULT_TABLE_SIZE, DEFAULT_TABLE_SIZE);
        table.setGrow();
        //     table.setRenderer(new SimpleVelocityRenderer());

        int row = 0;
        int col = 0;

        //.... Header
        table.setCell(new Cell().setProp(KEY_STYLE, cellCenterBold).setContent("#"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(type.getTypeName()), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellCenterBold).setContent("Counter"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Employee"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("ID"), row, col++);
        for (IType t : outputTypes) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(t.toString()), row, col++);
        }
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Manager"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("ID"), row, col++);
        for (IType t : outputTypes) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(t.toString()), row, col++);
        }
        if (type.getTypeDimension().equals(TypeDimension.Two)) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(getHeaderString(type)), row, col++);
        }
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(capacityType), row, col++);
        }
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(capacityType + " (weighted)"), row, col++);
        }
        row++;

        //.... Body
        int n = 1;
        for (Comparable primaryKey : employees.keySet()) {
            int count = 1;
            for (Employee employee : employees.get(primaryKey)) {

                col = 0;

                table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(n), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(primaryKey), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(count), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getUrl(Employee.EmployeeUrl.dataName)), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getUrl(Employee.EmployeeUrl.dataID)), row, col++);
                for (IType t : outputTypes) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.get(t)), row, col++);
                }
                if (employee.hasManager()) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getManager().getUrl(Employee.EmployeeUrl.dataName)), row, col++);
                    table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(employee.getManager().getUrl(Employee.EmployeeUrl.orgaID)), row, col++);
                    for (IType t : outputTypes) {
                        table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getManager().get(t)), row, col++);
                    }
                } else {
                    for (int i = 0; i < 2 + outputTypes.size(); i++) {
                        table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(""), row, col++);
                    }
                }

                if (type.getTypeDimension().equals(TypeDimension.Two)) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(employee.get(type, (String) primaryKey)), row, col++);
                }

                for (CapacityType capacityType : CapacityType.values()) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(employee.getCapacity(capacityType)), row, col++);
                }

                double percentage = summaryData.computePercentage(employee, type, primaryKey);
                for (CapacityType capacityType : CapacityType.values()) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(percentage * employee.getCapacity(capacityType)), row, col++);
                }

                row++;
                count++;
            }
            n++;
        }

        //.... Totals
        col = 0;
        table.setCell(new Cell(1, 2).setProp(KEY_STYLE, cellLeftEmpty).setContent("Totals"), row, col++);
        col++;
        if (type.getTypeDimension().equals(One)) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellCenterEmpty).setContent(summaryData.getTotalCount()), row, col++);
        } else {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftEmpty), row, col++);
        }
        table.setCell(new Cell(1, 4).setProp(KEY_STYLE, cellLeftEmpty), row, col++);
        col += 3;
        if (type.getTypeDimension().equals(TypeDimension.Two)) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftEmpty), row, col++);
        }
        table.setCell(new Cell(1, CapacityType.values().length).setProp(KEY_STYLE, cellLeftEmpty), row, col);
        col += CapacityType.values().length;
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellCenterEmpty).setContent(summaryData.getTotal().get(capacityType)), row, col++);
        }

        table.compact();
        return table;
    }

}
