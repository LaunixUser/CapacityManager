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
package org.ml.capman;

import java.util.HashMap;
import java.util.Map;

import org.ml.capman.DataConfiguration.TypeDimension;

import static org.ml.capman.DataConfiguration.TypeDimension.*;

import org.ml.capman.DataConfiguration.TypeKind;

import static org.ml.capman.DataConfiguration.TypeKind.TypeDouble;
import static org.ml.capman.DataConfiguration.TypeKind.TypeDoublePercentage;
import static org.ml.capman.DataConfiguration.TypeKind.TypeInteger;
import static org.ml.capman.DataConfiguration.TypeKind.TypeIntegerPercentage;
import static org.ml.capman.DataConfiguration.TypeKind.TypeString;

/**
 * @author osboxes
 */
public class DataContainer {

    private Map<String, Comparable> data1D = new HashMap<>();
    private Map<String, Map<String, Comparable>> data2D = new HashMap<>();
    private Map<String, String> minField2D = new HashMap<>();
    private Map<String, Comparable> minValue2D = new HashMap<>();
    private Map<String, String> maxField2D = new HashMap<>();
    private Map<String, Comparable> maxValue2D = new HashMap<>();
    private DataConfiguration configuration;

    /**
     *
     */
    public DataContainer() {
        this.configuration = DataConfiguration.getInstance();
    }

    /**
     * @param type
     * @param value
     */
    public void set(IType type, Comparable value) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value may not be null");
        }
        if (!configuration.supports(type, One)) {
            throw new IllegalArgumentException("type name '" + type.getTypeName() + "' not supported with type dimension " + One);
        }
        if (!validateType(type, value, One)) {
            throw new IllegalArgumentException("type validation failed for type name '" + type.getTypeName() + "' with type dimension " + One);
        }
        data1D.put(type.getTypeName(), value);
    }

    /**
     * @param type
     * @return
     */
    public Comparable get(IType type) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (!configuration.supports(type, One)) {
            throw new IllegalArgumentException("type name '" + type.getTypeName() + "' not supported with type dimension " + One);
        }
        return data1D.get(type.getTypeName());
    }

    /**
     * @param type
     * @param field
     * @param value
     */
    public void set(IType type, String field, Comparable value) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (field == null) {
            throw new NullPointerException("field may not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value may not be null");
        }
        if (!configuration.supports(type, Two)) {
            throw new IllegalArgumentException("type name '" + type.getTypeName() + "' not supported with type dimension " + Two);
        }
        if (!validateType(type, value, Two)) {
            throw new IllegalArgumentException("type validation failed for type name '" + type.getTypeName() + "' with type dimension " + Two);
        }

        String name = type.getTypeName();

        if (!data2D.containsKey(name)) {
            data2D.put(name, new HashMap<>());
        }
        //.... Need to make sure we deal with value updates correctly for the min/max calculations
        boolean updateValue = data2D.get(name).containsKey(field);

        data2D.get(name).put(field, value);

        //.... Update min/max data for convenience methods
        if (minField2D.containsKey(name)) {
            if (updateValue) {
                //.... A known name/field which provided a value previiusly - as this may have changed we need to redo the min/max analysis from scratch
                minField2D.remove(name);
                minValue2D.remove(name);
                maxField2D.remove(name);
                maxValue2D.remove(name);

                for (String f : data2D.get(name).keySet()) {
                    Comparable v = data2D.get(name).get(f);
                    if (!minField2D.containsKey(name)) {
                        minField2D.put(name, f);
                        minValue2D.put(name, v);
                        maxField2D.put(name, f);
                        maxValue2D.put(name, v);
                    } else {
                        if (v.compareTo(minValue2D.get(name)) < 0) {
                            minField2D.put(name, f);
                            minValue2D.put(name, v);
                        }
                        if (v.compareTo(maxValue2D.get(name)) > 0) {
                            maxField2D.put(name, f);
                            maxValue2D.put(name, v);
                        }
                    }
                }

            } else {
                //.... A new field for a known name; we do the comparisons to the stored min/max data
                if (value.compareTo(minValue2D.get(name)) < 0) {
                    minField2D.put(name, field);
                    minValue2D.put(name, value);
                }
                if (value.compareTo(maxValue2D.get(name)) > 0) {
                    maxField2D.put(name, field);
                    maxValue2D.put(name, value);
                }
            }
        } else {
            //.... A new name which has not been seen before, this we just use the values of the new entry 
            minField2D.put(name, field);
            minValue2D.put(name, value);
            maxField2D.put(name, field);
            maxValue2D.put(name, value);
        }
    }

    /**
     * @param type
     * @param field
     * @return
     */
    public Comparable get(IType type, String field) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (field == null) {
            throw new NullPointerException("field may not be null");
        }
        if (!configuration.supports(type, Two)) {
            throw new IllegalArgumentException("type name '" + type.getTypeName() + "' not supported with type dimension " + Two);
        }
        //.... Make sure we at least have the map established since it is a supported type, the field has just not been set (yet)
        if (!data2D.containsKey(type.getTypeName())) {
            data2D.put(type.getTypeName(), new HashMap<>());
        }
        return data2D.get(type.getTypeName()).get(field);
    }

    /**
     * Convenience method to get the value for a boolean type
     *
     * @param type
     * @return
     */
    public boolean is(IType type) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (!configuration.supports(type, TypeDimension.One)) {
            throw new IllegalArgumentException("Unknown type name: " + type.getTypeName());
        }
        if (!type.getTypeKind().isBoolean()) {
            throw new IllegalArgumentException("Type " + type.getTypeName() + " is not of TypeKind boolean");
        }
        return (boolean) get(type);
    }

    /**
     * Convenience method to get the value of a field for a boolean type
     *
     * @param type
     * @param field
     * @return
     */
    public boolean is(IType type, String field) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (field == null) {
            throw new NullPointerException("field may not be null");
        }
        if (!configuration.supports(type, TypeDimension.Two)) {
            throw new IllegalArgumentException("Unknown type name: " + type.getTypeName());
        }
        if (!type.getTypeKind().isBoolean()) {
            throw new IllegalArgumentException("Type " + type.getTypeName() + " is not of TypeKind boolean");
        }
        return (boolean) get(type, field);
    }

    /**
     * @param type
     * @return
     */
    public Map<String, Comparable> getFields(IType type) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (!configuration.supports(type, Two)) {
            throw new IllegalArgumentException("type name '" + type.getTypeName() + "' not supported with type dimension " + Two);
        }
        if (!data2D.containsKey(type.getTypeName())) {
            data2D.put(type.getTypeName(), new HashMap<>());
        }
        return data2D.get(type.getTypeName());
    }

    /**
     * @param type
     * @return
     */
    public String getMinField(IType type) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (!configuration.supports(type, Two)) {
            throw new IllegalArgumentException("type name '" + type.getTypeName() + "' not supported with type dimension " + Two);
        }
        return minField2D.get(type.getTypeName());
    }

    /**
     * @param type
     * @return
     */
    public Comparable getMinValue(IType type) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (!configuration.supports(type, Two)) {
            throw new IllegalArgumentException("type name '" + type.getTypeName() + "' not supported with type dimension " + Two);
        }
        return minValue2D.get(type.getTypeName());
    }

    /**
     * @param type
     * @return
     */
    public String getMaxField(IType type) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (!configuration.supports(type, Two)) {
            throw new IllegalArgumentException("type name '" + type.getTypeName() + "' not supported with type dimension " + Two);
        }
        return maxField2D.get(type.getTypeName());
    }

    /**
     * @param type
     * @return
     */
    public Comparable getMaxValue(IType type) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (!configuration.supports(type, Two)) {
            throw new IllegalArgumentException("type name '" + type.getTypeName() + "' not supported with type dimension " + Two);
        }
        return maxValue2D.get(type.getTypeName());
    }

    /**
     * Check whether a value to be stored is of the required (defined) kind for
     * the given type
     *
     * @param type
     * @param value
     * @param typeDimension
     * @return
     */
    private boolean validateType(IType type, Comparable value, TypeDimension typeDimension) {
        TypeKind requiredType = configuration.get(typeDimension).get(type.getTypeName()).getTypeKind();
        switch (requiredType) {
            case TypeDouble:
            case TypeDoublePercentage:
                return value instanceof Double;
            case TypeInteger:
            case TypeIntegerPercentage:
                return value instanceof Integer;
            case TypeString:
                return value instanceof String;
            default:
                return true;
        }
    }

    /**
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("TypeDimension: ");
        sb.append(TypeDimension.One);
        sb.append("\n");
        for (String typeName : DataConfiguration.getInstance().get(TypeDimension.One).keySet()) {
            IType type = DataConfiguration.getInstance().get(TypeDimension.One).get(typeName);
            sb.append(typeName);
            sb.append(" : ");
            sb.append(get(type));
            sb.append("\n");
        }
        sb.append("TypeDimension: ");
        sb.append(TypeDimension.Two);
        sb.append("\n");
        for (String typeName : DataConfiguration.getInstance().get(TypeDimension.Two).keySet()) {
            IType type = DataConfiguration.getInstance().get(TypeDimension.One).get(typeName);
            sb.append(typeName);
            sb.append(":\n");
            for (String field : getFields(type).keySet()) {
                sb.append(field);
                sb.append(" : ");
                sb.append(getFields(type).get(field));
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
