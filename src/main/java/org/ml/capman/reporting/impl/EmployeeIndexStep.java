package org.ml.capman.reporting.impl;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
        Map<String, TableData> tables = new TreeMap<>();

        TableData tableData = new TableData(propertyManager);
        if (setContext.length() > 0) {
            tableData.setTableHeader("Employee Index (Data Context: " + setContext + ")");
        } else {
            tableData.setTableHeader("Employee Index");
        }
        setDescription = propertyManager.getString(OptionalKey.setDescription, "Employee Index");
        tableData.addTable("tableBody", createTable(employeeData));
        //.... There is only one table in this set, so the one table uses the same description as the entire set
        tableData.setDescription(setDescription);
        tables.put("employeeIndex", tableData);
        return tables;

    }

    /**
     * @return
     */
//    public static String getFileName() {
//        return "employeeIndex";
//    }
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
        Table table = new Table();
        table.setGrow();
        //   table.setRenderer(new SimpleVelocityRenderer());

        int row = 0;
        int col = 0;

        //.... Header
        table.setCell(new Cell().setStyle(cellLeftBold).setContent("#"), row, col++);
        table.setCell(new Cell().setStyle(cellLeftBold).setContent("Employee"), row, col++);
        table.setCell(new Cell().setStyle(cellLeftBold).setContent("ID"), row, col++);
        table.setCell(new Cell().setStyle(cellLeftBold).setContent("Org"), row, col++);
        table.setCell(new Cell().setStyle(cellLeftBold).setContent("Manager"), row, col++);
        table.setCell(new Cell().setStyle(cellLeftBold).setContent("ID"), row, col++);
        table.setCell(new Cell().setStyle(cellLeftBold).setContent("Org"), row, col++);
        for (IType type : outputTypes) {
            table.setCell(new Cell().setStyle(cellLeftBold).setContent(type.toString()), row, col++);
        }
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setStyle(cellLeftBold).setContent(capacityType.toString()), row, col++);
        }

        //.... Data
        int n = 1;
        for (Employee employee : employeeSet) {

            row++;
            col = 0;

            table.setCell(new Cell().setStyle(cellLeft).setContent(n++), row, col++);
            table.setCell(new Cell().setStyle(cellLeft).setContent(employee.getUrl(EmployeeUrl.DATA_NAME)), row, col++);
            table.setCell(new Cell().setStyle(cellLeft).setContent(employee.getUrl(EmployeeUrl.DATA_ID)), row, col++);
            if (employee.getEmployees().size() > 0) {
                table.setCell(new Cell().setStyle(cellCenter).setContent(employee.getUrl(EmployeeUrl.ORGA_X)), row, col++);
            } else {
                table.setCell(new Cell().setStyle(cellLeft).setContent(""), row, col++);
            }
            if (employee.hasManager()) {
                table.setCell(new Cell().setStyle(cellLeft).setContent(employee.getManager().getUrl(EmployeeUrl.DATA_NAME)), row, col++);
                table.setCell(new Cell().setStyle(cellLeft).setContent(employee.getManager().getUrl(EmployeeUrl.DATA_ID)), row, col++);
                table.setCell(new Cell().setStyle(cellCenter).setContent(employee.getManager().getUrl(EmployeeUrl.ORGA_X)), row, col++);
            } else {
                table.setCell(new Cell().setStyle(cellLeft).setContent(""), row, col++);
                table.setCell(new Cell().setStyle(cellLeft).setContent(""), row, col++);
                table.setCell(new Cell().setStyle(cellLeft).setContent(""), row, col++);
            }
            for (IType type : outputTypes) {
                table.setCell(new Cell().setStyle(cellLeft).setContent(employee.get(type)), row, col++);
            }
            for (CapacityType capacityType : CapacityType.values()) {
                table.setCell(new Cell().setStyle(cellCenter).setContent(employee.getCapacity(capacityType)), row, col++);
            }
        }

        LOGGER.log(Level.INFO, "Compacting the table");
        table.compact();

        return table;
    }

}
