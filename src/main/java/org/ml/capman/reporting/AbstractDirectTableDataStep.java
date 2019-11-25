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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ml.tools.PropertyManager;
import org.ml.capman.DataConfiguration;
import org.ml.capman.Employee;
import org.ml.capman.EmployeeData;
import org.ml.capman.IType;
import org.ml.pf.output.TableData;
import org.ml.pf.step.AbstractDirectProcessStep;

/**
 * @author Dr. Matthias Laux
 */
public abstract class AbstractDirectTableDataStep extends AbstractDirectProcessStep<EmployeeData<Employee>, Map<String, TableData>> {

    protected Map<String, String> fileNames = new TreeMap<>();
    protected String setDescription = null;
    protected Comparator<Employee> comparator = null;
    protected DataConfiguration dataConfiguration = DataConfiguration.getInstance();
    protected List<IType> outputTypes = new ArrayList<>();

    /**
     *
     */
    public enum OptionalKey {
        //.... ITypes to be included in the output
        outputTypes,
        //.... The minimum level for IType.getLevel() to be included ion the output (defaults to 0)
        minimumLevel,
        //.... The context for this data; basically a string that can be to further describe the output created
        setContext,
        //.... A descriptive text for this data
        setDescription
    }

    /**
     * @param id
     * @param propertyManager
     */
    public AbstractDirectTableDataStep(String id, PropertyManager propertyManager) {
        super(id, propertyManager);
        if (propertyManager.containsProperty(OptionalKey.outputTypes)) {
            outputTypes = DataConfiguration.getTypesFromMapString(propertyManager.getProperty(OptionalKey.outputTypes));
        }
    }

    /**
     * @param propertyManager
     */
    public AbstractDirectTableDataStep(PropertyManager propertyManager) {
        super(propertyManager);
        if (propertyManager.containsProperty(OptionalKey.outputTypes)) {
            outputTypes = DataConfiguration.getTypesFromMapString(propertyManager.getProperty(OptionalKey.outputTypes));
        }
    }

    /**
     * #
     *
     * @return
     */
    public Map<String, String> getFileNames() {
        return fileNames;
    }

    /**
     * @return
     */
    public String getSetDescription() {
        return setDescription;
    }

    /**
     * @param type
     * @return
     */
    public String getHeaderString(IType type) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }

        switch (type.getTypeDimension()) {
            case One:
                return "";
            case Two:
                if (type.getTypeKind().isPercentage()) {
                    return "%";
                } else {
                    return "Value";
                }
            default:
                throw new UnsupportedOperationException("Unsupported TypeDimension for type = " + type);
        }
    }
}
