package org.ml.capman.reporting.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ml.tools.PropertyManager;
import org.ml.tools.logging.LoggerFactory;
import org.ml.capman.DataConfiguration.TypeDimension;
import org.ml.capman.EmployeeCapacity.CapacityType;
import org.ml.capman.Employee;
import org.ml.capman.Employee.EmployeeUrl;
import org.ml.capman.EmployeeData;

import static org.ml.capman.render.RenderingType.*;
import org.ml.capman.reporting.AbstractDirectTableDataStep;

import org.ml.pf.output.TableData;
import org.ml.table.Cell;
import org.ml.table.Table;

/**
 * @author mlaux
 */
public class OrgaStep extends AbstractDirectTableDataStep {

    private final static Logger LOGGER = LoggerFactory.getLogger(OrgaStep.class.getName());
    private final static int CELL_WIDTH = 4;
    private final static int CELL_BORDER = 1;
    private final static int CELL_VSIZE = 2;
    private final static int CELL_VSPACING = 2;
    public final static String ORG_PREFIX = "h_";
    public final static String KEY_NAME = "name";

    /**
     *
     */
    public enum ContentType {
        role, detailURL, chartURL, upURL, location, country, orgsize, fte
    }

    /**
     *
     */
    public enum RequiredKey {
        typeCountry, typeLocation, typePosition, typeVacancy, typeParentalLeave
    }

    /**
     * @param propertyManager
     */
    public OrgaStep(PropertyManager propertyManager) {
        super(propertyManager);
        propertyManager.validateAllPropertyNames(RequiredKey.typeCountry);
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

        Map<String, TableData> tables = new TreeMap<>();
        String rootFileName = ORG_PREFIX + employeeData.getRootEmployee().getID();

        assembleContextData(tables, employeeData.getRootEmployee(), rootFileName);
        for (String fileName : tables.keySet()) {
            LOGGER.log(Level.INFO, "Hierarchy file to be created: {0}", fileName);
        }

        //.... A bit of a hack to make sure we have an index file, irrespective of the file name created for the root employee
        tables.put("index", tables.get(ORG_PREFIX + employeeData.getRootEmployee().getID()));

        return tables;

    }

    /**
     * @param tables
     * @param employee
     * @param rootFileName
     */
    private void assembleContextData(Map<String, TableData> tables, Employee employee, String rootFileName) {
        if (tables == null) {
            throw new IllegalArgumentException("tables may not be null");
        }
        if (employee == null) {
            throw new IllegalArgumentException("employee may not be null");
        }
        if (rootFileName == null) {
            throw new IllegalArgumentException("rootFileName may not be null");
        }
        if (!employee.getEmployees().isEmpty()) {

            TableData tableData = new TableData(propertyManager);
            String setContext = propertyManager.getString(OptionalKey.setContext, "");
            if (setContext.length() > 0) {
                tableData.setTableHeader("Org Chart (Data Context: " + setContext + ")");
            } else {
                tableData.setTableHeader("Org Chart");
            }
            tableData.addTable("tableBody", createTable(employee, rootFileName));
            tableData.setDescription(employee.getID());
            tables.put(ORG_PREFIX + employee.getID(), tableData);

            for (Employee child : employee.getEmployees().values()) {
                assembleContextData(tables, child, rootFileName);
            }

        }
        setDescription = propertyManager.getString(AbstractDirectTableDataStep.OptionalKey.setDescription, "Org Charts");
    }

    /**
     * @param employee
     * @param rootFileName
     * @return
     */
    private Table createTable(Employee employee, String rootFileName) {
        if (employee == null) {
            throw new IllegalArgumentException("employee may not be null");
        }
        if (rootFileName == null) {
            throw new IllegalArgumentException("rootFileName may not be null");
        }

        Table table = new Table();
        table.setGrow();

        //.... Check whether we have an employee with only staff reports (i. e. no managers)
        boolean hasManagersAsReports = false;
        for (Employee child : employee.getEmployees().values()) {
            if (child.getEmployees().size() > 0) {
                hasManagersAsReports = true;
                break;
            }
        }

        //.... For managers with at least two levels below them
        int maxRow = 0;
        int row = CELL_BORDER;
        int col;
        if (hasManagersAsReports) {

            int n = employee.getEmployees().size();
            col = (CELL_WIDTH + 2 * CELL_BORDER + 1) * ((n - 1) / 2) + CELL_BORDER;   // Center the top employee

            setupCell(table, employee, row, col, true);
            frameCell1(table, row, col);

            col = CELL_BORDER;
            for (Employee employeeL2 : sortStaffFunctionsToEndOfList(employee)) {

                row = CELL_VSIZE + CELL_VSPACING + 3 * CELL_BORDER;
                setupCell(table, employeeL2, row, col, false);
                frameCell1(table, row, col);

                row += CELL_VSIZE + CELL_VSPACING + 2 * CELL_BORDER;
                for (Employee employeeL3 : sortStaffFunctionsToEndOfList(employeeL2)) {

                    setupCell(table, employeeL3, row, col, false);
                    frameCell2(table, row, col);

                    row += CELL_VSIZE + CELL_BORDER;

                    if (row > maxRow) {
                        maxRow = row;
                    }

                }
                if (employeeL2.getEmployees().size() > 0) {
                    setupFrameCell(table, row - CELL_BORDER, col - CELL_BORDER, CELL_BORDER, CELL_WIDTH + 2 * CELL_BORDER);
                }
                col += CELL_WIDTH + 2 * CELL_BORDER + 1;
            }

            //.... Managers with no managers reporting to them - display all staff in one column
        } else {

            row = CELL_BORDER;
            col = CELL_BORDER;
            setupCell(table, employee, row, CELL_BORDER, true);
            frameCell1(table, row, col);

            row += CELL_VSIZE + CELL_VSPACING + 2 * CELL_BORDER;
            for (Employee employeeL2 : sortStaffFunctionsToEndOfList(employee)) {

                setupCell(table, employeeL2, row, CELL_BORDER, false);
                frameCell2(table, row, col);

                row += CELL_VSIZE + CELL_BORDER;

            }
            setupFrameCell(table, row - CELL_BORDER, col - CELL_BORDER, CELL_BORDER, CELL_WIDTH + 2 * CELL_BORDER);
            maxRow = row;
        }

        //.... Footer
        table.compact();
        return table;
    }

    /**
     * Add a frame around all four edges of the cell
     *
     * @param table
     * @param row
     * @param col
     */
    private void frameCell1(Table table, int row, int col) {
        if (table == null) {
            throw new IllegalArgumentException("table may not be null");
        }
        frameCell2(table, row, col);
        setupFrameCell(table, row + CELL_VSIZE, col - CELL_BORDER, CELL_BORDER, CELL_WIDTH + 2 * CELL_BORDER);
    }

    /**
     * Add a frame around all three edges of the cell (all except lower edge)
     *
     * @param table
     * @param row
     * @param col
     */
    private void frameCell2(Table table, int row, int col) {
        if (table == null) {
            throw new IllegalArgumentException("table may not be null");
        }
        setupFrameCell(table, row - CELL_BORDER, col - CELL_BORDER, CELL_BORDER, CELL_WIDTH + 2 * CELL_BORDER);
        setupFrameCell(table, row, col - CELL_BORDER, CELL_VSIZE, CELL_BORDER);
        setupFrameCell(table, row, col + CELL_WIDTH, CELL_VSIZE, CELL_BORDER);
    }

    /**
     * @param table
     * @param employee
     * @param row
     * @param col
     */
    private void setupCell(Table table, Employee employee, int row, int col, boolean topLevel) {
        if (employee.getEmployees().size() > 0) {
            table.setCell(createEmployeeCell(employee, topLevel), row, col);
            table.setCell(createCountryCell(employee, managerCountry), row + 1, col);
            table.setCell(createFTECell(employee, managerFTE), row + 1, col + 1);
            table.setCell(createOrgsizeCell(employee, managerOrgsize), row + 1, col + 2);
            table.setCell(createLocationCell(employee, managerLocation), row + 1, col + 3);
        } else {
            table.setCell(createEmployeeCell(employee, topLevel), row, col);
            table.setCell(createCountryCell(employee, employeeCountry), row + 1, col);
            table.setCell(createFTECell(employee, employeeFTE), row + 1, col + 1);
            table.setCell(createOrgsizeCell(employee, employeeOrgsize), row + 1, col + 2);
            table.setCell(createLocationCell(employee, employeeLocation), row + 1, col + 3);
        }
    }

    /**
     * @param rows
     * @param cols
     * @return
     */
    private void setupFrameCell(Table table, int row, int col, int rows, int cols) {
        Cell cell = new Cell(rows, cols);
        cell.setStyle(cellFrame);
        table.setCell(cell, row, col);
    }

    /**
     * TableRenderer method to create one employee cell and fill it with all
     * required data
     *
     * @param employee
     * @return
     */
    private Cell createEmployeeCell(Employee employee, boolean topLevel) {
        if (employee == null) {
            throw new IllegalArgumentException("employee may not be null");
        }

        Cell cell = new Cell(1, CELL_WIDTH);
        cell.setContent(KEY_NAME, (String) employee.getName());
        if (employee.getEmployees().size() > 0) {
            cell.setStyle(managerStyle);
        } else {
            cell.setStyle(employeeStyle);
        }

        cell.setContent(ContentType.role, employee.get(dataConfiguration.get(propertyManager.getProperty(RequiredKey.typePosition), TypeDimension.One)));
        cell.setContent(ContentType.detailURL, employee.getUrl(EmployeeUrl.DATA_ID).getAddress());
        if (!employee.getEmployees().isEmpty() && !topLevel) {
            cell.setContent(ContentType.chartURL, ORG_PREFIX + employee.getID());
        }
        if (topLevel && employee.hasManager()) {
            cell.setContent(ContentType.upURL, ORG_PREFIX + employee.getManager().getID());
        }
        return cell;
    }

    /**
     * @param employee
     * @param t
     * @return
     */
    private Cell createCountryCell(Employee employee, Enum t) {
        if (employee == null) {
            throw new IllegalArgumentException("employee may not be null");
        }
        if (t == null) {
            throw new IllegalArgumentException("t may not be null");
        }
        Cell cell = new Cell(1, 1);
        cell.setContent(ContentType.country, employee.get(dataConfiguration.get(propertyManager.getProperty(RequiredKey.typeCountry), TypeDimension.One)));
        cell.setStyle(t.toString());
        return cell;
    }

    /**
     *
     */
    private Cell createFTECell(Employee employee, Enum t) {
        if (employee
                == null) {
            throw new IllegalArgumentException("employee may not be null");
        }
        if (t == null) {
            throw new IllegalArgumentException("t may not be null");
        }
        Cell cell = new Cell(1, 1);
        cell.setContent(ContentType.fte, String.format("%.2f", employee.getCapacity(CapacityType.FTE)));
        cell.setStyle(t.toString());
        return cell;
    }

    /**
     * @param employee
     * @param t
     * @return
     */
    private Cell createOrgsizeCell(Employee employee, Enum t) {
        if (employee == null) {
            throw new IllegalArgumentException("employee may not be null");
        }
        if (t == null) {
            throw new IllegalArgumentException("t may not be null");
        }
        Cell cell = new Cell(1, 1);

        //.... Get the organisation sizes by employees as such and by actual HC count - also add the employee itself
        int s = employee.organisationSize() + 1;
        int shc = employee.organisationSizeByHC() + employee.getCapacity().getAsInt(CapacityType.HC);
        cell.setContent(ContentType.orgsize, s + "/" + shc);

        //... Set the cell style
        cell.setStyle(t.toString());

        if (employee.is(dataConfiguration.get(propertyManager.getProperty(RequiredKey.typeVacancy), TypeDimension.One))) {
            if (t.equals(employeeOrgsize)) {
                cell.setStyle(employeeOrgsizeVac);
            } else {
                cell.setStyle(managerOrgsizeVac);
            }
        }

        if (employee.is(dataConfiguration.get(propertyManager.getProperty(RequiredKey.typeParentalLeave), TypeDimension.One))) {
            if (t.equals(employeeOrgsize)) {
                cell.setStyle(employeeOrgsizePar);
            } else {
                cell.setStyle(managerOrgsizePar);
            }
        }

        return cell;
    }

    /**
     * @param employee
     * @param t
     * @return
     */
    private Cell createLocationCell(Employee employee, Enum t) {
        if (employee == null) {
            throw new IllegalArgumentException("employee may not be null");
        }
        if (t == null) {
            throw new IllegalArgumentException("t may not be null");
        }
        Cell cell = new Cell(1, 1);
        cell.setContent(ContentType.location, employee.get(dataConfiguration.get(propertyManager.getProperty(RequiredKey.typeLocation), TypeDimension.One)));
        cell.setStyle(t.toString());
        return cell;
    }

    /**
     * Order such that the sequence as such is retained, but staff functions are
     * moved to the end
     *
     * @param employee
     * @return
     */
    private List<Employee> sortStaffFunctionsToEndOfList(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("employee may not be null");
        }
        List<Employee> withReports = new ArrayList<>();
        List<Employee> staffFunction = new ArrayList<>();
        for (Employee child : employee.getEmployees().values()) {
            if (child.getEmployees().size() > 0) {
                withReports.add(child);
            } else {
                staffFunction.add(child);
            }
        }
        withReports.addAll(staffFunction);
        return withReports;
    }

}
