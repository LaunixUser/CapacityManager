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
import org.ml.capman.Employee;
import org.ml.capman.Employee.EmployeeUrl;
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
public class EmployeeIndexStep extends AbstractDirectTableDataStep {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmployeeIndexStep.class.getName());

    /**
     * @param id
     * @param comparator
     * @param propertyManager
     */
    public EmployeeIndexStep(String id, Comparator<Employee> comparator, PropertyManager propertyManager) {
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
    public EmployeeIndexStep(Comparator<Employee> comparator, PropertyManager propertyManager) {
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

        String setContext = propertyManager.getString(OptionalKey.setContext, "");
        Map<String, TableData> tables = new HashMap<>();

        TableData tableData = new TableData(propertyManager);
        if (setContext.length() > 0) {
            tableData.setTableHeader("Employee Index (Data Context: " + setContext + ")");
        } else {
            tableData.setTableHeader("Employee Index");
        }
        tableData.addTable("tableBody", createTable(employeeData));
        String fileName = getFileName();
        tables.put(fileName, tableData);

        setDescription = propertyManager.getString(OptionalKey.setDescription, "Employee Index");
        fileNames.put(setDescription, fileName);

        return tables;

    }

    /**
     * @return
     */
    public static String getFileName() {
        return "employeeIndex";
    }

    /**
     * @param employeeData
     * @return
     */
    private Table createTable(EmployeeData<Employee> employeeData) {
        if (employeeData == null) {
            throw new IllegalArgumentException("employeeData may not be null");
        }

        //.... Get a sorted list for the employees 
        Set<Employee> employeeSet = new TreeSet<>(comparator);
        for (Employee employee : employeeData.getEmployees()) {
            employeeSet.add(employee);
        }

        //.... Create the table
        LOGGER.log(Level.INFO, "Creating the table");
        Table table = new Table(DEFAULT_TABLE_SIZE, DEFAULT_TABLE_SIZE);
        table.setGrow();
        //   table.setRenderer(new SimpleVelocityRenderer());

        int row = 0;
        int col = 0;

        //.... Header
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("#"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Employee"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("ID"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Org"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Manager"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("ID"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Org"), row, col++);
        for (IType type : outputTypes) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(type.toString()), row, col++);
        }
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(capacityType.toString()), row, col++);
        }

        //.... Data
        int n = 1;
        for (Employee employee : employeeSet) {

            row++;
            col = 0;

            table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(n++), row, col++);
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getUrl(EmployeeUrl.dataName)), row, col++);
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getUrl(EmployeeUrl.dataID)), row, col++);
            if (employee.getEmployees().size() > 0) {
                table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(employee.getUrl(EmployeeUrl.orgaX)), row, col++);
            } else {
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(""), row, col++);
            }
            if (employee.hasManager()) {
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getManager().getUrl(EmployeeUrl.dataName)), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getManager().getUrl(EmployeeUrl.dataID)), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(employee.getManager().getUrl(EmployeeUrl.orgaX)), row, col++);
            } else {
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(""), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(""), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(""), row, col++);
            }
            for (IType type : outputTypes) {
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.get(type)), row, col++);
            }
            for (CapacityType capacityType : CapacityType.values()) {
                table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(employee.getCapacity(capacityType)), row, col++);
            }
        }

        LOGGER.log(Level.INFO, "Compacting the table");
        table.compact();

        return table;
    }

}
