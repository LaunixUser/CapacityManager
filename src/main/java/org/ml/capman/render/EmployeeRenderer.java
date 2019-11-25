package org.ml.capman.render;

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

import java.util.logging.Logger;

import org.ml.tools.logging.LoggerFactory;
import org.ml.capman.DataConfiguration;

import static org.ml.capman.DataConfiguration.TypeDimension.One;

import org.ml.capman.Employee;
import org.ml.capman.IType;

import static org.ml.capman.render.AbstractTableCreator.*;
import static org.ml.capman.render.RenderingType.cellLeft;

import org.ml.table.Cell;
import org.ml.table.Table;

/**
 * @author mlaux
 */
public class EmployeeRenderer {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmployeeRenderer.class.getName());
    public static final String UNDEFINED = "(Undefined)";

    /**
     * @param employee
     * @param renderingStyle
     * @return
     */
    public Table renderEmployee(Employee employee, RenderingStyle renderingStyle) {
        if (employee == null) {
            throw new NullPointerException("employee may not be null");
        }
        if (renderingStyle == null) {
            throw new NullPointerException("renderingStyle may not be null");
        }

        Table table = new Table(DEFAULT_TABLE_SIZE, DEFAULT_TABLE_SIZE);
        table.setGrow();

        int row = 0;
        int col = 0;

        switch (renderingStyle.getDirection()) {

            case horizontal:

                table.setCell(new Cell().setProp(KEY_STYLE, renderingStyle.getHeaderStyle()).setContent("ID"), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, renderingStyle.getHeaderStyle()).setContent("Name"), row, col++);
                for (IType type : DataConfiguration.getInstance().get(One).values()) {
                    table.setCell(new Cell().setProp(KEY_STYLE, renderingStyle.getHeaderStyle()).setContent(type.getTypeName()), row, col++);
                }
                row++;
                col = 0;
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getUrl(Employee.EmployeeUrl.dataID)), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getUrl(Employee.EmployeeUrl.dataName)), row, col++);
                for (IType type : DataConfiguration.getInstance().get(One).values()) {
                    if (employee.get(type) != null) {
                        table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.get(type)), row, col++);
                    } else {
                        table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(UNDEFINED), row, col++);
                    }
                }
                break;

            case vertical:

                break;

            default:

                throw new UnsupportedOperationException("Unknown or unsupported RenderingDirection: " + renderingStyle.getDirection());
        }

        table.compact();
        return table;
    }
}
