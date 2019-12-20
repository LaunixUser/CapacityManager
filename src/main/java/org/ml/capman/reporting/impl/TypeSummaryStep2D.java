package org.ml.capman.reporting.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.ml.tools.PropertyManager;
import org.ml.tools.logging.LoggerFactory;
import org.ml.capman.CapacitySummaryData2D;
import org.ml.capman.DataConfiguration;
import org.ml.capman.DataConfiguration.TypeDimension;

import static org.ml.capman.DataConfiguration.TypeDimension.One;

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

import static org.ml.table.render.IRenderer.HINT_PERCENTAGE;

/**
 * @author mlaux
 */
public class TypeSummaryStep2D extends AbstractDirectTableDataStep {

    private final static Logger LOGGER = LoggerFactory.getLogger(TypeSummaryStep2D.class.getName());

    /**
     * @param id
     * @param propertyManager
     */
    public TypeSummaryStep2D(String id, PropertyManager propertyManager) {
        super(id, propertyManager);
    }

    /**
     * @param propertyManager
     */
    public TypeSummaryStep2D(PropertyManager propertyManager) {
        super(propertyManager);
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
                                    TableData tableData = new TableData(propertyManager);
                                    if (setContext.length() > 0) {
                                        tableData.setTableHeader("Capacity for " + typeName1 + " by " + typeName2 + " (Data Context: " + setContext + ")");
                                    } else {
                                        tableData.setTableHeader("Capacity for " + typeName1 + " by " + typeName2);
                                    }
                                    tableData.addTable("tableBody", createTable(employeeData, type1, type2));                                    
                                    tableData.setDescription("Type " + typeName1 + " by " + typeName2);
                                    tables.put("typeSummary2D_" + typeName1 + "_by_" + typeName2, tableData);
                                }
                            }
                        }
                    }
                }
            }
        }
        setDescription = propertyManager.getString(TypeSummaryStep.OptionalKey.setDescription, "Capacity Summary 2D");

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
//        return "typeSummary2D_" + typeName1 + "_by_" + typeName2;
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

        //.... Create the output table
        Table table = new Table(DEFAULT_TABLE_SIZE, DEFAULT_TABLE_SIZE);
        table.setGrow();
        //   table.setRenderer(new SimpleVelocityRenderer());

        int row = 0;
        int col = 0;

        //.... Header
        table.setCell(new Cell().setProp(KEY_STYLE, cellCenterBold).setContent("#1"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(type1.getTypeName()), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("#2"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(type2.getTypeName()), row, col++);
        if (type1.getTypeDimension().equals(One) && type2.getTypeDimension().equals(One)) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Count"), row, col++);
        }
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(capacityType), row, col++);
        }
        if (type1.getTypeDimension().equals(One) && type2.getTypeDimension().equals(One)) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Count %"), row, col++);
        }
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(capacityType + " %"), row, col++);
        }

        //.... Body
        int n = 1;
        Map<CapacityType, Double> subtotals = new HashMap<>();
        int subcount = 0;
        for (Comparable primaryKey1 : summaryData.get().keySet()) {

            int m = 1;
            for (CapacityType capacityType : CapacityType.values()) {
                subtotals.put(capacityType, 0.0);
            }
            subcount = 0;

            for (Comparable primaryKey2 : summaryData.get().get(primaryKey1).keySet()) {

                row++;
                col = 0;

                Map<CapacityType, Double> data = summaryData.get(primaryKey1, primaryKey2);

                Map<CapacityType, Double> percentData = summaryData.getPercent(primaryKey1, primaryKey2);
                table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(n), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(primaryKey1), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(m), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(primaryKey2), row, col++);
                if (type1.getTypeDimension().equals(One) && type2.getTypeDimension().equals(One)) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(summaryData.getCount(primaryKey1, primaryKey2)), row, col++);
                }
                for (CapacityType capacityType : CapacityType.values()) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(data.get(capacityType)), row, col++);
                    double d = subtotals.get(capacityType);
                    subtotals.put(capacityType, data.get(capacityType) + d);
                }
                subcount += summaryData.getCount(primaryKey1, primaryKey2);

                if (type1.getTypeDimension().equals(One) && type2.getTypeDimension().equals(One)) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(100.0d * summaryData.getCountPercent(primaryKey1, primaryKey2)), row, col++);
                }
                for (CapacityType capacityType : CapacityType.values()) {
                    table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(100.0d * percentData.get(capacityType)), row, col++);
                }
                m++;
            }

            //.... Subtotals
            row++;
            col = 0;
            table.setCell(new Cell(1, 4).setProp(KEY_STYLE, cellLeftEmpty).setContent("Subtotals"), row, col);
            col += 4;
            if (type1.getTypeDimension().equals(One) && type2.getTypeDimension().equals(One)) {
                table.setCell(new Cell().setProp(KEY_STYLE, cellCenterEmpty).setContent(subcount), row, col++);
            }
            for (CapacityType capacityType : CapacityType.values()) {
                table.setCell(new Cell().setProp(KEY_STYLE, cellCenterEmpty).setContent(subtotals.get(capacityType)), row, col++);
            }
            if (type1.getTypeDimension().equals(One) && type2.getTypeDimension().equals(One)) {
                table.setCell(new Cell().setProp(KEY_STYLE, cellCenterEmpty).setContent((double) subcount / (double) summaryData.getTotalCount()).setType(HINT_PERCENTAGE), row, col++);
            }
            for (CapacityType capacityType : CapacityType.values()) {
                table.setCell(new Cell().setProp(KEY_STYLE, cellCenterEmpty).setContent(subtotals.get(capacityType) / summaryData.getTotal().get(capacityType)).setType(HINT_PERCENTAGE), row, col++);
            }

            n++;
        }

        //.... Totals
        row++;
        col = 0;
        table.setCell(new Cell(1, 4).setProp(KEY_STYLE, cellLeftEmpty).setContent("Totals"), row, col);
        col += 4;
        Map<CapacityType, Double> totalData = summaryData.getTotal();
        if (type1.getTypeDimension().equals(One) && type2.getTypeDimension().equals(One)) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellCenterEmpty).setContent(summaryData.getTotalCount()), row, col++);
        }
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellCenterEmpty).setContent(totalData.get(capacityType)), row, col++);
        }
        if (type1.getTypeDimension().equals(One) && type2.getTypeDimension().equals(One)) {
            table.setCell(new Cell(1, 1).setProp(KEY_STYLE, cellCenterEmpty), row, col++);
        }
        table.setCell(new Cell(1, CapacityType.values().length).setProp(KEY_STYLE, cellCenterEmpty), row, col);

        table.compact();
        return table;
    }

}
