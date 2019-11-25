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
package org.ml.capman.reporting;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.ml.tools.PropertyManager;
import org.ml.tools.logging.LoggerFactory;

import static org.ml.capman.render.AbstractTableCreator.DEFAULT_TABLE_SIZE;
import static org.ml.capman.render.AbstractTableCreator.KEY_STYLE;
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

        Map<String, TableData> tables = new HashMap<>();
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
            tableData.addTable(key, createTable(stepID, step));
            n++;
        }
        tables.put(getFileName(), tableData);

        return tables;

    }

    /**
     * @return
     */
    public static String getFileName() {
        return "reporting";
    }

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

        Table table = new Table(DEFAULT_TABLE_SIZE, DEFAULT_TABLE_SIZE);
        table.setGrow();
//        table.setRenderer(new SimpleVelocityRenderer());

        int row = 0;
        int col = 0;

        //.... Header
        table.setCell(new Cell().setProp(KEY_STYLE, cellLeftBold).setContent(step.getSetDescription()), row++, 0);

        //.... Data
        int n = 1;
        for (String key : step.getFileNames().keySet()) {
            String url = relativePath + stepID + File.separatorChar + step.getFileNames().get(key).replaceAll("\\..+$", "");
            UrlContent urlContent = new UrlContent(url, key);
            table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(urlContent), row++, 0);
        }

        table.compact();
        return table;
    }
}
