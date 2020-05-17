package org.ml.capman.reporting.impl;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ml.capman.reporting.AbstractDirectTableDataStep;

import org.ml.tools.PropertyManager;
import org.ml.tools.logging.LoggerFactory;

import static org.ml.capman.render.RenderingType.cellLeft;
import static org.ml.capman.render.RenderingType.cellLeftBold;

import org.ml.pf.output.TableData;
import org.ml.pf.step.AbstractDirectProcessStep;
import org.ml.table.Cell;
import org.ml.table.Table;
import org.ml.table.content.UrlContent;

/**
 * @author mlaux
 */
public class ReportingOverviewStep extends AbstractDirectProcessStep<Map<String, AbstractDirectTableDataStep>, Map<String, TableData>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ReportingOverviewStep.class.getName());
    private String relativePath = "";

    public enum OptionalKey {

        //.... The context for this data; basically a string that can used be to further describe the output created
        setContext,
        //.... Optional relative path for the links added to other files
        relativePath
    }

    /**
     * @param id
     * @param propertyManager
     */
    public ReportingOverviewStep(String id, PropertyManager propertyManager) {
        super(id, propertyManager);
    }

    /**
     * @param propertyManager
     */
    public ReportingOverviewStep(PropertyManager propertyManager) {
        super(propertyManager);
    }

    /**
     * @param steps
     * @return
     */
    @Override
    public Map<String, TableData> createOutputData(Map<String, AbstractDirectTableDataStep> steps) {
        if (steps == null) {
            throw new NullPointerException("steps may not be null");
        }

        String setContext = propertyManager.getString(OptionalKey.setContext, "");
        relativePath = propertyManager.getString(OptionalKey.relativePath, "");

        Map<String, TableData> tables = new TreeMap<>();
        TableData tableData = new TableData(propertyManager);
        if (setContext.length() > 0) {
            tableData.setTableHeader("Reports (Data Context: " + setContext + ")");
        } else {
            tableData.setTableHeader("Reports");
        }
        int n = 1;
        for (String stepID : steps.keySet()) {
            AbstractDirectTableDataStep step = steps.get(stepID);
            String key = n + ": " + step.getSetDescription();
            LOGGER.log(Level.INFO, "Adding table data for step ''{0}'' with index {1}", new Object[]{step.getClass(), n});
            tableData.addTable(key, createTable(stepID, step));
            n++;
        }
        tables.put("reporting", tableData);

        return tables;

    }

    /**
     * @return
     */
//    public static String getFileName() {
//        return "reporting";
//    }
    /**
     * @param stepID
     * @param step
     * @return
     */
    private Table createTable(String stepID, AbstractDirectTableDataStep step) {
        if (step == null) {
            throw new NullPointerException("step may not be null");
        }
        if (stepID == null) {
            throw new NullPointerException("stepID may not be null");
        }

        Table table = new Table();
        table.setGrow();

        int row = 0;
        int col = 0;

        //.... Header
        table.setCell(new Cell().setStyle(cellLeftBold).setContent(step.getSetDescription()), row++, 0);

        //.... Data
        int n = 1;
        Map<String, TableData> dataCache = step.getOutputDataCache();
        for (String key : dataCache.keySet()) {
            String url = relativePath + stepID + File.separatorChar + key.replaceAll("\\..+$", "");
            UrlContent urlContent = new UrlContent(url, dataCache.get(key).getDescription());
            table.setCell(new Cell().setStyle(cellLeft).setContent(urlContent), row++, 0);
        }

        table.compact();
        return table;
    }
}
