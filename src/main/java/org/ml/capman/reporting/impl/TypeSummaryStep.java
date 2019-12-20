package org.ml.capman.reporting.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.ml.tools.PropertyManager;
import org.ml.tools.logging.LoggerFactory;
import org.ml.capman.CapacitySummaryData;
import org.ml.capman.DataConfiguration;
import org.ml.capman.DataConfiguration.TypeDimension;

import static org.ml.capman.DataConfiguration.TypeDimension.One;

import org.ml.capman.Employee;
import org.ml.capman.EmployeeCapacity.CapacityType;
import org.ml.capman.EmployeeData;
import org.ml.capman.IType;

import static org.ml.capman.render.AbstractTableCreator.DEFAULT_TABLE_SIZE;
import static org.ml.capman.render.AbstractTableCreator.KEY_STYLE;
import static org.ml.capman.render.RenderingType.*;

import org.ml.capman.reporting.AbstractDirectTableDataStep;
import org.ml.pf.output.TableData;
import org.ml.table.Cell;
import org.ml.table.Table;

/**
 * @author mlaux
 */
public class TypeSummaryStep extends AbstractDirectTableDataStep {

    private final static Logger LOGGER = LoggerFactory.getLogger(TypeSummaryStep.class.getName());

    /**
     * @param id
     * @param propertyManager
     */
    public TypeSummaryStep(String id, PropertyManager propertyManager) {
        super(id, propertyManager);
    }

    /**
     * @param propertyManager
     */
    public TypeSummaryStep(PropertyManager propertyManager) {
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

        Map<String, TableData> tables = new HashMap<>();

        for (TypeDimension typeDimension : TypeDimension.values()) {
            for (String typeName : DataConfiguration.getInstance().get(typeDimension).keySet()) {
                IType type = DataConfiguration.getInstance().get(typeDimension).get(typeName);
                if (type.getLevel() >= minimumLevel) {
                    TableData tableData = new TableData(propertyManager);
                    if (setContext.length() > 0) {
                        tableData.setTableHeader("Capacity by " + typeName + " (Data Context: " + setContext + ")");
                    } else {
                        tableData.setTableHeader("Capacity by " + typeName);
                    }
                    tableData.addTable("tableBody", createTable(employeeData, type));
                    tableData.setDescription("Type " + typeName);
                    tables.put("typeSummaryBy" + typeName, tableData);
                }
            }
        }
        setDescription = propertyManager.getString(OptionalKey.setDescription, "Capacity Summary by Type");

        return tables;

    }

    /**
     * @param typeName
     * @return
     */
//    public static String getFileName(String typeName) {
//        if (typeName == null) {
//            throw new NullPointerException("typeName may not be null");
//        }
//        return "typeSummaryBy" + typeName;
//    }

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

        //.... Collect capacity summary data for the given type
        CapacitySummaryData summaryData = new CapacitySummaryData(type, employeeData);

        //.... Create the output table
        Table table = new Table(DEFAULT_TABLE_SIZE, DEFAULT_TABLE_SIZE);
        table.setGrow();
        //    table.setRenderer(new SimpleVelocityRenderer());

        int row = 0;
        int col = 0;

        //.... Header
        table.setCell(new Cell().setProp(KEY_STYLE, cellCenterBold).setContent("#"), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(type.getTypeName()), row, col++);
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Count"), row, col++);
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(capacityType), row, col++);
        }
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent("Count %"), row, col++);
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(capacityType + " %"), row, col++);
        }
        //.... Body
        int n = 1;
        for (Comparable primaryKey : summaryData.get().keySet()) {
            row++;
            col = 0;
            Map<CapacityType, Double> data = summaryData.get(primaryKey);
            Map<CapacityType, Double> percentData = summaryData.getPercent(primaryKey);
            table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(n), row, col++);
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(primaryKey), row, col++);
            table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(summaryData.getCount(primaryKey)), row, col++);
            for (CapacityType capacityType : CapacityType.values()) {
                table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(data.get(capacityType)), row, col++);
            }
            table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(100.0d * summaryData.getCountPercent(primaryKey)), row, col++);
            for (CapacityType capacityType : CapacityType.values()) {
                table.setCell(new Cell().setProp(KEY_STYLE, cellCenter).setContent(100.0d * percentData.get(capacityType)), row, col++);
            }
            n++;
        }

        row++;
        col = 0;
        table.setCell(new Cell(1, 2).setProp(KEY_STYLE, cellLeftEmpty).setContent("Totals"), row, col++);
        col++;
        if (type.getTypeDimension().equals(One)) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellCenterEmpty).setContent(summaryData.getTotalCount()), row, col++);
        } else {
            table.setCell(new Cell().setProp(KEY_STYLE, cellCenterEmpty), row, col++);
        }
        for (CapacityType capacityType : CapacityType.values()) {
            table.setCell(new Cell().setProp(KEY_STYLE, cellCenterEmpty).setContent(summaryData.getTotal().get(capacityType)), row, col++);
        }
        table.setCell(new Cell(1, CapacityType.values().length + 1).setProp(KEY_STYLE, cellCenterEmpty), row, col++);

        table.compact();
        return table;
    }

}
