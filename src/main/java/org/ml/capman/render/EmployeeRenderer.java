package org.ml.capman.render;

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
 * This is currently experimental and not finally developed. The idea is that
 * the actual rendering process should become more configurable
 *
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
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getUrl(Employee.EmployeeUrl.DATA_ID)), row, col++);
                table.setCell(new Cell().setProp(KEY_STYLE, cellLeft).setContent(employee.getUrl(Employee.EmployeeUrl.DATA_NAME)), row, col++);
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
