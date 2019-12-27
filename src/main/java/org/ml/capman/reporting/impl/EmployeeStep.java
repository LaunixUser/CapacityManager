package org.ml.capman.reporting.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ml.tools.PropertyManager;
import org.ml.tools.logging.LoggerFactory;
import org.ml.capman.DataConfiguration;

import static org.ml.capman.DataConfiguration.TypeDimension.*;

import org.ml.capman.Employee;
import org.ml.capman.EmployeeCapacity.CapacityType;
import org.ml.capman.EmployeeData;
import org.ml.capman.IType;
import org.ml.capman.reporting.AbstractDirectTableDataStep;

import static org.ml.capman.render.AbstractTableCreator.*;
import static org.ml.capman.render.RenderingType.*;

import org.ml.pf.output.TableData;
import org.ml.table.Cell;
import org.ml.table.Table;

/**
 * @author mlaux
 */
public class EmployeeStep extends AbstractDirectTableDataStep {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmployeeStep.class.getName());
    public static final String UNDEFINED = "(Undefined)";

    /**
     * @param id
     * @param propertyManager
     */
    public EmployeeStep(String id, PropertyManager propertyManager) {
        super(id, propertyManager);
    }

    /**
     * @param propertyManager
     */
    public EmployeeStep(PropertyManager propertyManager) {
        super(propertyManager);
    }

    /**
     * @param employeeData
     * @return
     */
    @Override
    public Map<String, TableData> createOutputData(EmployeeData<Employee> employeeData) {
        if (employeeData == null) {
            throw new IllegalArgumentException("employeeData may not be null");
        }

        Map<String, TableData> tables = new HashMap<>();
        for (Employee employee : employeeData.getEmployees()) {

            TableData tableData = new TableData(propertyManager);
            tableData.setTableHeader("Detailed Data for Employee: " + employee.getName());

            Table[] t = createTables(employee);

            int ind = 0;
            tableData.setTableSubHeader("tableBody" + ind, "Scalar Type Values");
            tableData.addTable("tableBody" + ind, t[ind++]);
            tableData.setTableSubHeader("tableBody" + ind, "Capacity");
            tableData.addTable("tableBody" + ind, t[ind++]);
            tableData.setTableSubHeader("tableBody" + ind, "Field Type Values");
            tableData.addTable("tableBody" + ind, t[ind++]);
            if (employee.getEmployees().size() > 0) {
                tableData.setTableSubHeader("tableBody" + ind, "Direct Reports");
                tableData.addTable("tableBody" + ind, t[ind++]);
            }

            tables.put(employee.getID(), tableData);
            //.... There is only one table in this set, so the one table uses the same description as the entire set
            tableData.setDescription(employee.getID());

        }
        setDescription = propertyManager.getString(OptionalKey.setDescription, "Employee");
        return tables;
    }

    /**
     * @param employee
     * @return
     */
    private Table[] createTables(Employee employee) {
        if (employee == null) {
            throw new NullPointerException("employee may not be null");
        }

        //.... Create the output tables - starting with 1D values
        Table table1 = new Table();
        table1.setGrow();

        int row = 0;
        int col = 0;

        table1.setCell(new Cell().addStyle(cellLeftBold).setContent("ID"), row, col++);
        table1.setCell(new Cell().addStyle(cellLeftBold).setContent("Employee"), row, col++);
        for (IType type : DataConfiguration.getInstance().get(One).values()) {
            table1.setCell(new Cell().addStyle(cellLeftBold).setContent(type.getTypeName()), row, col++);
        }
        row++;
        col = 0;
        table1.setCell(new Cell().addStyle(cellLeft).setContent(employee.getUrl(Employee.EmployeeUrl.DATA_ID)), row, col++);
        table1.setCell(new Cell().addStyle(cellLeft).setContent(employee.getUrl(Employee.EmployeeUrl.DATA_NAME)), row, col++);
        for (IType type : DataConfiguration.getInstance().get(One).values()) {
            if (employee.get(type) != null) {
                table1.setCell(new Cell().addStyle(cellLeft).setContent(employee.get(type)), row, col++);
            } else {
                table1.setCell(new Cell().addStyle(cellLeft).setContent(UNDEFINED), row, col++);
            }
        }

        //.... Capacity data
        Table table2 = new Table();
        table1.setGrow();

        row = 0;
        col = 0;

        for (CapacityType capacityType : CapacityType.values()) {
            table2.setCell(new Cell().addStyle(cellLeftBold).setContent(capacityType), row, col++);
        }
        row++;
        col = 0;
        for (CapacityType capacityType : CapacityType.values()) {
            table2.setCell(new Cell().addStyle(cellCenter).setContent(employee.getCapacity(capacityType)), row, col++);
        }

        //.... 2D Values
        Table table3 = new Table();
        table3.setGrow();
        //    table2.setRenderer(new SimpleVelocityRenderer());

        row = 0;
        col = 0;

        table3.setCell(new Cell().addStyle(cellLeftBold).setContent("Type"), row, col++);
        table3.setCell(new Cell().addStyle(cellLeftBold).setContent("Field"), row, col++);
        table3.setCell(new Cell().addStyle(cellLeftBold).setContent("Value"), row, col++);

        row++;
        col = 0;
        for (IType type : DataConfiguration.getInstance().get(Two).values()) {
            for (String field : employee.getFields(type).keySet()) {

                table3.setCell(new Cell().addStyle(cellLeft).setContent(type.getTypeName()), row, col++);
                table3.setCell(new Cell().addStyle(cellLeft).setContent(field), row, col++);

                if (employee.get(type, field) != null) {

                    if (type.getTypeKind().isPercentage()) {
                        table3.setCell(new Cell().addStyle(cellLeft).setContent(employee.get(type, field) + "%"), row, col++);
                    } else {
                        table3.setCell(new Cell().addStyle(cellLeft).setContent(employee.get(type, field)), row, col++);
                    }
                } else {
                    table3.setCell(new Cell().addStyle(cellLeft).setContent(UNDEFINED), row, col++);
                }

                row++;
                col = 0;
            }
        }
        //.... Direct report list
        if (employee.getEmployees().size() > 0) {
            Table table4 = new Table();
            table4.setGrow();
            //       table3.setRenderer(new SimpleVelocityRenderer());

            row = 0;
            col = 0;

            table4.setCell(new Cell().addStyle(cellLeftBold).setContent("#"), row, col++);
            table4.setCell(new Cell().addStyle(cellLeftBold).setContent("ID"), row, col++);
            table4.setCell(new Cell().addStyle(cellLeftBold).setContent("Employee"), row, col++);
            for (IType type : DataConfiguration.getInstance().get(One).values()) {
                table4.setCell(new Cell().addStyle(cellLeftBold).setContent(type.getTypeName()), row, col++);
            }
            row++;

            int n = 1;
            for (String employeeID : employee.getEmployees().keySet()) {
                Employee staffMember = employee.getEmployees().get(employeeID);
                col = 0;
                table4.setCell(new Cell().addStyle(cellLeft).setContent(n++), row, col++);
                table4.setCell(new Cell().addStyle(cellLeft).setContent(staffMember.getUrl(Employee.EmployeeUrl.DATA_ID)), row, col++);
                table4.setCell(new Cell().addStyle(cellLeft).setContent(staffMember.getUrl(Employee.EmployeeUrl.DATA_NAME)), row, col++);
                for (IType type : DataConfiguration.getInstance().get(One).values()) {
                    if (staffMember.get(type) != null) {
                        table4.setCell(new Cell().addStyle(cellLeft).setContent(staffMember.get(type)), row, col++);
                    } else {
                        table4.setCell(new Cell().addStyle(cellLeft).setContent(UNDEFINED), row, col++);
                    }
                }
                row++;
            }

            table1.compact();
            table2.compact();
            table3.compact();
            table4.compact();

            return new Table[]{table1, table2, table3, table4};

        } else {

            table1.compact();
            table2.compact();
            table3.compact();
            return new Table[]{table1, table2, table3};

        }
    }

    /**
     * @param employee
     * @return
     */
//    public static String getFileName(Employee employee) {
//        return employee.getID();
//    }
}
