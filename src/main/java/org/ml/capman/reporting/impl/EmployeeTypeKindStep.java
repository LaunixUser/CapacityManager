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
package org.ml.capman.reporting.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ml.tools.PropertyManager;
import org.ml.tools.logging.LoggerFactory;
import org.ml.capman.DataConfiguration;
import org.ml.capman.DataConfiguration.TypeDimension;

import static org.ml.capman.DataConfiguration.TypeDimension.One;

import org.ml.capman.DataConfiguration.TypeKind;

import static org.ml.capman.DataConfiguration.TypeKind.TypeOther;

import org.ml.capman.Employee;
import org.ml.capman.EmployeeCapacity.CapacityType;
import org.ml.capman.EmployeeData;
import org.ml.capman.IType;
import org.ml.capman.reporting.AbstractDirectTableDataStep;

import static org.ml.capman.render.AbstractTableCreator.DEFAULT_TABLE_SIZE;
import static org.ml.capman.render.AbstractTableCreator.KEY_STYLE;
import static org.ml.capman.render.RenderingType.cellCenter;
import static org.ml.capman.render.RenderingType.cellLeft;
import static org.ml.capman.render.RenderingType.cellLeftBold;

import org.ml.pf.output.TableData;
import org.ml.table.Cell;
import org.ml.table.Table;

/**
 * @author mlaux
 */
public class EmployeeTypeKindStep extends AbstractDirectTableDataStep {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmployeeTypeKindStep.class.getName());
    private DataConfiguration.TypeKind selectedKind = null;
    private Comparable selectedValue = null;

    /**
     *
     */
    public enum RequiredKey {
        kind
    }

    /**
     * @param id
     * @param comparator
     * @param propertyManager
     */
    public EmployeeTypeKindStep(String id, Comparator<Employee> comparator, PropertyManager propertyManager) {
        super(id, propertyManager);
        if (comparator == null) {
            throw new NullPointerException("comparator may not be null");
        }
        this.propertyManager.validateAllPropertyNames(RequiredKey.kind);
        this.comparator = comparator;
        setSelector();
    }

    /**
     * @param comparator
     * @param propertyManager
     */
    public EmployeeTypeKindStep(Comparator<Employee> comparator, PropertyManager propertyManager) {
        super(propertyManager);
        if (comparator == null) {
            throw new NullPointerException("comparator may not be null");
        }
        this.propertyManager.validateAllPropertyNames(RequiredKey.kind);
        this.comparator = comparator;
        setSelector();
    }

    /**
     *
     */
    private void setSelector() {
        String[] s = propertyManager.getProperty(RequiredKey.kind).split(":");
        if (s.length != 2) {
            throw new IllegalArgumentException("Property " + RequiredKey.kind + " needs to be in the format <TypeKind>:<value>");
        }
        selectedKind = DataConfiguration.TypeKind.valueOf(s[0]);
        switch (selectedKind) {
            case TypeBoolean:
                selectedValue = Boolean.valueOf(s[1]);
                break;
            case TypeDouble:
            case TypeDoublePercentage:
                selectedValue = Double.valueOf(s[1]);
                break;
            case TypeInteger:
            case TypeIntegerPercentage:
                selectedValue = Integer.valueOf(s[1]);
                break;
            case TypeString:
                selectedValue = s[1];
                break;
            case TypeOther:
                throw new UnsupportedOperationException("Do not know how to handle comparisons for TypeKind: " + TypeOther);
        }
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

        Map<String, TableData> tables = new HashMap<>();
        String setContext = propertyManager.getString(OptionalKey.setContext, "");

        TableData tableData = new TableData(propertyManager);
        if (setContext.length() > 0) {
            tableData.setTableHeader("Employee Values for TypeKind: " + selectedKind + " = " + selectedValue + " (Data Context: " + setContext + ")");
        } else {
            tableData.setTableHeader("Employee Values for TypeKind: " + selectedKind + " = " + selectedValue);
        }
        createTables(tableData, employeeData);
        String fileName = getFileName(selectedKind, selectedValue);
        tables.put(fileName, tableData);

        setDescription = propertyManager.getString(EmployeeIndexStep.OptionalKey.setDescription, "TypeKind: " + selectedKind + " = " + selectedValue);
        fileNames.put(setDescription, fileName);

        return tables;

    }

    /**
     * @param kind
     * @param selectedValue
     * @return
     */
    public static String getFileName(TypeKind kind, Comparable selectedValue) {
        if (kind == null) {
            throw new NullPointerException("kind may not be null");
        }
        return "employeeTypeKind_" + kind + "_" + selectedValue;
    }

    /**
     * @param tableData
     * @param employeeData
     */
    private void createTables(TableData tableData, EmployeeData<Employee> employeeData) {
        if (tableData == null) {
            throw new NullPointerException("tableData may not be null");
        }
        if (employeeData == null) {
            throw new IllegalArgumentException("employeeData may not be null");
        }

        //.... Get a sorted list for the employees 
        Set<Employee> employeeSet = new TreeSet<>(comparator);
        for (Employee employee : employeeData.getEmployees()) {
            employeeSet.add(employee);
        }

        //.... Create the tables
        LOGGER.log(Level.INFO, "Creating the tables");

        for (TypeDimension typeDimension : TypeDimension.values()) {
            for (IType type : dataConfiguration.get(typeDimension).values()) {
                if (type.getTypeKind().equals(selectedKind)) {
                    Table table = getTable(type, employeeSet);
                    tableData.addTable(type.getTypeName(), table);
                    tableData.setTableSubHeader(type.getTypeName(), "Employees for type: " + type.getTypeName());
                }
            }
        }

    }

    /**
     * @param type
     * @param employeeSet
     * @return
     */
    private Table getTable(IType type, Set<Employee> employeeSet) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (employeeSet == null) {
            throw new NullPointerException("employeeSet may not be null");
        }

        Table table = new Table(DEFAULT_TABLE_SIZE, DEFAULT_TABLE_SIZE);
        table.setGrow();
//        table.setRenderer(new SimpleVelocityRenderer());

        int row = 0;
        int col = 0;

        //.... Header
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("#"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Employee"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Org"), row, col++);
        for (IType t : outputTypes) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(t.toString()), row, col++);
        }
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Manager"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Org"), row, col++);
        for (IType t : outputTypes) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(t.toString()), row, col++);
        }
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(capacityType.toString()), row, col++);
        }

        //.... Data
        int n = 1;
        for (Employee employee : employeeSet) {

            boolean include = false;
            switch (type.getTypeDimension()) {
                case One:
                    include = employee.get(type).equals(selectedValue);
                    break;
                default:
                    throw new UnsupportedOperationException("Not yet implemented for TypeDimension other than " + One);
            }

            if (include) {
                row++;
                col = 0;

                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(n++), row, col++);

                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getUrl(Employee.EmployeeUrl.dataName)), row, col++);
                if (employee.getEmployees().size() > 0) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(employee.getUrl(Employee.EmployeeUrl.orgaX)), row, col++);
                } else {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(""), row, col++);
                }
                for (IType t : outputTypes) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.get(t)), row, col++);
                }
                if (employee.hasManager()) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getManager().getUrl(Employee.EmployeeUrl.dataName)), row, col++);
                    table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(employee.getManager().getUrl(Employee.EmployeeUrl.orgaX)), row, col++);
                    for (IType t : outputTypes) {
                        table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getManager().get(t)), row, col++);
                    }
                } else {
                    for (int i = 0; i < 2 + outputTypes.size(); i++) {
                        table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(""), row, col++);
                    }
                }
                for (CapacityType capacityType : CapacityType.values()) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(employee.getCapacity(capacityType)), row, col++);
                }
            }
        }

        table.compact();
        return table;
    }
}
