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
import org.ml.capman.DataConfiguration;
import org.ml.capman.DataConfiguration.TypeDimension;

import static org.ml.capman.DataConfiguration.TypeDimension.One;

import org.ml.capman.Employee;
import org.ml.capman.EmployeeData;
import org.ml.capman.IType;
import org.ml.capman.reporting.AbstractDirectTableDataStep;

import static org.ml.capman.render.AbstractTableCreator.KEY_STYLE;
import static org.ml.capman.render.RenderingType.*;

import org.ml.pf.output.TableData;
import org.ml.table.Cell;
import org.ml.table.Table;

/**
 * @author mlaux
 */
public class TypeClusterView2D extends AbstractDirectTableDataStep {

    private final static Logger LOGGER = LoggerFactory.getLogger(TypeClusterView2D.class.getName());
    private Map<Comparable, Map<Comparable, Set<Employee>>> employees = new TreeMap<>();
    public static final String UNASSIGNED = "(unassigned)";
    private int typesDelta = 0;

    /**
     * @param id
     * @param comparator
     * @param propertyManager
     */
    public TypeClusterView2D(String id, Comparator<Employee> comparator, PropertyManager propertyManager) {
        super(id, propertyManager);
        if (comparator == null) {
            throw new NullPointerException("comparator may not be null");
        }
        this.comparator = comparator;
        typesDelta = outputTypes.size();
    }

    /**
     * @param comparator
     * @param propertyManager
     */
    public TypeClusterView2D(Comparator<Employee> comparator, PropertyManager propertyManager) {
        super(propertyManager);
        if (comparator == null) {
            throw new NullPointerException("comparator may not be null");
        }
        this.comparator = comparator;
        typesDelta = outputTypes.size();
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

        Map<String, TableData> tables = new HashMap<>();
        for (TypeDimension typeDimension1 : TypeDimension.values()) {
            for (String typeName1 : configuration.get(typeDimension1).keySet()) {
                IType type1 = configuration.get(typeDimension1).get(typeName1);
                if (type1.getLevel() >= minimumLevel) {
                    for (TypeDimension typeDimension2 : TypeDimension.values()) {
                        for (String typeName2 : configuration.get(typeDimension2).keySet()) {
                            IType type2 = configuration.get(typeDimension2).get(typeName2);
                            if (type2.getLevel() >= minimumLevel) {
                                if (!configuration.isEqual(type1, type2)) {

                                    //.... Current limitation: we only cover 1D x 1D for now
                                    if (type1.getTypeDimension().equals(One) && type2.getTypeDimension().equals(One)) {
                                        TableData tableData = new TableData(propertyManager);
                                        if (setContext.length() > 0) {
                                            tableData.setTableHeader("Cluster View for " + typeName2 + " by " + typeName1 + " (Data Context: " + setContext + ")");
                                        } else {
                                            tableData.setTableHeader("Cluster View for " + typeName2 + " by " + typeName1);
                                        }
                                        tableData.addTable("tableBody", createTable(employeeData, type1, type2));
                                        tableData.setDescription("Type " + typeName2 + " by " + typeName1);
                                        tables.put("clusterView_" + typeName2 + "_by_" + typeName1, tableData);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        setDescription = propertyManager.getString(OptionalKey.setDescription, "Cluster View");

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
//        return "clusterView_" + typeName2 + "_by_" + typeName1;
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

        //.... Collect employees 
        Set<Comparable> primaryKeys1 = new TreeSet<>();
        Map<Comparable, Set<Comparable>> primaryKeys2 = new HashMap<>();

        for (Employee employee : employeeData.getEmployees()) {
            Comparable primaryKey1 = employee.get(type1);
            if (!employees.containsKey(primaryKey1)) {
                employees.put(primaryKey1, new TreeMap<>());
            }
            primaryKeys1.add(primaryKey1);

            Comparable primaryKey2 = employee.get(type2);
            if (!employees.get(primaryKey1).containsKey(primaryKey2)) {
                employees.get(primaryKey1).put(primaryKey2, new TreeSet<>(comparator));
            }
            if (!primaryKeys2.containsKey(primaryKey1)) {
                primaryKeys2.put(primaryKey1, new TreeSet<>());
            }
            primaryKeys2.get(primaryKey1).add(primaryKey2);

            employees.get(primaryKey1).get(primaryKey2).add(employee);
        }

        int maxPrimaryKey2 = 0;
        for (Comparable primaryKey1 : primaryKeys2.keySet()) {
            maxPrimaryKey2 = Math.max(maxPrimaryKey2, primaryKeys2.get(primaryKey1).size());
        }

        //.... Create the output table
        Table table = new Table(1, 1);
        table.setGrow();
        //      table.setRenderer(new SimpleVelocityRenderer());

        int row = 0;
        int col = 0;
        int numberOfTables = Math.min(3, maxPrimaryKey2);
        int internalTableColumns = 1 + 2 * (2 + typesDelta);

        int headerWidth = numberOfTables * (internalTableColumns + 1) - 1;

        for (Comparable primaryKey1 : primaryKeys1) {

            //.... This may require generalization for other data types ... this just avoids empty headers
            if (primaryKey1 instanceof String) {
                if (((String) primaryKey1).length() == 0) {
                    table.setCell(new Cell(1, headerWidth).setProp(KEY_STYLE, cellLeftEmpty).setContent(UNASSIGNED), row++, col);
                } else {
                    table.setCell(new Cell(1, headerWidth).setProp(KEY_STYLE, cellLeftEmpty).setContent(primaryKey1), row++, col);
                }
            } else {
                table.setCell(new Cell(1, headerWidth).setProp(KEY_STYLE, cellLeftEmpty).setContent(primaryKey1), row++, col);
            }
            table.setCell(new Cell(1, headerWidth).setProp(KEY_STYLE, cellLeftEmpty), row++, col);
            table.setCell(new Cell(1, headerWidth), row++, col);

            int tableCount = 0;
            int remainder = primaryKeys2.get(primaryKey1).size();
            for (Comparable primaryKey2 : primaryKeys2.get(primaryKey1)) {

                table.addTable(createSubTable(primaryKey1, primaryKey2), row, col);
                remainder--;
                col += internalTableColumns;

                tableCount++;
                if (tableCount == numberOfTables) {
                    tableCount = 0;
                    row = table.getRowEnd() + 1;
                    col = 0;
                    if (remainder > 0) {
                        table.setCell(new Cell(1, headerWidth), row++, col);
                    }
                } else {
                    table.setCell(new Cell(), row, col++);
                }
            }
            row = table.getRowEnd() + 1;
            col = 0;
            table.setCell(new Cell(1, headerWidth), row++, col);

        }

        table.compact();
        return table;
    }

    /**
     * @param primaryKey1
     * @param primaryKey2
     * @return
     */
    private Table createSubTable(Comparable primaryKey1, Comparable primaryKey2) {
        if (primaryKey1 == null) {
            throw new NullPointerException("primaryKey1 may not be null");
        }
        if (primaryKey2 == null) {
            throw new NullPointerException("primaryKey2 may not be null");
        }

        //.... Build the table
        Table table = new Table(10, 10);
        table.setGrow();

        int row = 0;
        int col = 0;

        int n = 1;

        //.... This may require generalization for other data types ... this just avoids empty headers
        int w1 = 2 + typesDelta;
        int w2 = 1 + 2 * w1;

        //.... Header of subtable
        if (primaryKey2 instanceof String) {
            if (((String) primaryKey2).length() == 0) {
                table.setCell(new Cell(1, w2).setProp(KEY_STYLE, cellLeftBold).setContent(UNASSIGNED), row++, col);
            } else {
                table.setCell(new Cell(1, w2).setProp(KEY_STYLE, cellLeftBold).setContent(primaryKey2), row++, col);
            }
        } else {
            table.setCell(new Cell(1, w2).setProp(KEY_STYLE, cellLeftBold).setContent(primaryKey2), row++, col);
        }

        //.... Columns of subtable        
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftLight).setContent("#"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftLight).setContent("Employee"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftLight).setContent("ID"), row, col++);
        for (IType type : outputTypes) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftLight).setContent(type.toString()), row, col++);
        }
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftLight).setContent("Manager"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftLight).setContent("ID"), row, col++);
        for (IType type : outputTypes) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftLight).setContent(type.toString()), row, col++);
        }
        row++;

        //.... Subtable data
        col = 0;
        for (Employee employee : employees.get(primaryKey1).get(primaryKey2)) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(n++), row, col++);
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getUrl(Employee.EmployeeUrl.dataName)), row, col++);
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getUrl(Employee.EmployeeUrl.dataID)), row, col++);
            for (IType type : outputTypes) {
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.get(type)), row, col++);
            }
            if (employee.hasManager()) {
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getManager().getUrl(Employee.EmployeeUrl.dataName)), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(employee.getManager().getUrl(Employee.EmployeeUrl.orgaID)), row, col++);
                for (IType type : outputTypes) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getManager().get(type)), row, col++);
                }
            } else {
                for (int i = 0; i < w1; i++) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(""), row, col++);
                }
            }
            row++;
            col = 0;
        }

        table.compact();
        return table;
    }

}
