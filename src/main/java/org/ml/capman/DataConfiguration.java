/**
 * This class manages all aspects of the types that are being supported for a given application of CapacityManager
 * It's a bit like a type registry with lots of utility methods.
 */
package org.ml.capman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ml.tools.ToolBelt;

import static org.ml.capman.DataConfiguration.TypeDimension.One;

/**
 * @author osboxes
 */
public class DataConfiguration {

    private Map<TypeDimension, Map<String, IType>> types = new HashMap<>();
    private static DataConfiguration configuration = new DataConfiguration();

    /**
     * @author mlaux
     */
    public enum TypeKind {
        TypeString(false, false, true, false, false),
        TypeIntegerPercentage(true, false, false, true, false),
        TypeInteger(true, false, false, false, false),
        TypeDouble(false, true, false, false, false),
        TypeDoublePercentage(false, true, false, true, false),
        TypeBoolean(false, false, false, false, true),
        TypeEmail(false, false, false, false, false),
        TypeOther(false, false, false, false, false);

        private boolean isInteger = false;
        private boolean isDouble = false;
        private boolean isString = false;
        private boolean isPercentage = false;
        private boolean isBoolean = false;

        /**
         * @param isInteger
         * @param isDouble
         * @param isString
         * @param isPercentage
         * @param isBoolean
         */
        TypeKind(boolean isInteger, boolean isDouble, boolean isString, boolean isPercentage, boolean isBoolean) {
            this.isInteger = isInteger;
            this.isDouble = isDouble;
            this.isString = isString;
            this.isPercentage = isPercentage;
            this.isBoolean = isBoolean;
        }

        /**
         * @return
         */
        public boolean isInteger() {
            return isInteger;
        }

        /**
         * @return
         */
        public boolean isDouble() {
            return isDouble;
        }

        /**
         * @return
         */
        public boolean isString() {
            return isString;
        }

        /**
         * @return
         */
        public boolean isPercentage() {
            return isPercentage;
        }

        /**
         * @return
         */
        public boolean isBoolean() {
            return isBoolean;
        }
    }

    /**
     *
     */
    public enum TypeDimension {
        One, Two
    }

    /**
     * Singleton
     */
    private DataConfiguration() {
        for (TypeDimension typeDimension : TypeDimension.values()) {
            types.put(typeDimension, new TreeMap<>());
        }
    }

    /**
     * @return
     */
    public static DataConfiguration getInstance() {
        return configuration;
    }

    /**
     * @param type
     */
    public void add(IType type) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (type.getTypeKind().isPercentage && type.getTypeDimension().equals(One)) {
            throw new UnsupportedOperationException("TypeKind with isPercentage() true can not be TypeDimension.One for type = " + type);
        }
        types.get(type.getTypeDimension()).put(type.getTypeName(), type);
    }

    /**
     * @param types
     */
    public void add(IType[] types) {
        if (types == null) {
            throw new NullPointerException("types may not be null");
        }
        for (IType type : types) {
            if (type.getTypeKind().isPercentage && type.getTypeDimension().equals(One)) {
                throw new UnsupportedOperationException("TypeKind with isPercentage() true can not be TypeDimension.One for type = " + type);
            }
            this.types.get(type.getTypeDimension()).put(type.getTypeName(), type);
        }
    }

    /**
     * @param type
     * @param typeDimension
     * @return
     */
    public boolean supports(IType type, TypeDimension typeDimension) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        return supports(type.getTypeName(), typeDimension);
    }

    /**
     * @param typeName
     * @param typeDimension
     * @return
     */
    public boolean supports(String typeName, TypeDimension typeDimension) {
        if (typeName == null) {
            throw new NullPointerException("typeName may not be null");
        }
        if (typeDimension == null) {
            throw new NullPointerException("typeDimension may not be null");
        }
        return get(typeDimension).containsKey(typeName);
    }

    /**
     * @param typeDimension
     * @return
     */
    public Map<String, IType> get(TypeDimension typeDimension) {
        if (typeDimension == null) {
            throw new NullPointerException("typeDimension may not be null");
        }
        return types.get(typeDimension);
    }

    /**
     * @param typeName
     * @param typeDimension
     * @return
     */
    public IType get(String typeName, TypeDimension typeDimension) {
        if (!supports(typeName, typeDimension)) {
            throw new UnsupportedOperationException("Type " + typeName + " not supported for TypeDimension " + typeDimension);
        }
        return get(typeDimension).get(typeName);
    }

    /**
     * @param typeName
     * @param typeDimensionName
     * @return
     */
    public IType get(String typeName, String typeDimensionName) {
        if (typeName == null) {
            throw new NullPointerException("typeName may not be null");
        }
        if (typeDimensionName == null) {
            throw new NullPointerException("typeDimensionName may not be null");
        }
        TypeDimension typeDimension = TypeDimension.valueOf(typeDimensionName);
        if (!supports(typeName, typeDimension)) {
            throw new UnsupportedOperationException("Type " + typeName + " not supported for TypeDimension " + typeDimension);
        }
        return get(typeDimension).get(typeName);
    }

    /**
     * Since IType is an interface, we can not readily assume that all
     * implementations will have a suitable implementation of equals() and
     * hashcode(). Therefore, we provide this helper method which is based on
     * the general concept of IType equality: two types are equal if they have
     * the same name and the same dimension
     *
     * @param type1
     * @param type2
     * @return
     */
    public boolean isEqual(IType type1, IType type2) {
        if (type1 == null) {
            throw new NullPointerException("type1 may not be null");
        }
        if (type2 == null) {
            throw new NullPointerException("type2 may not be null");
        }
        if (type1.getTypeDimension().equals(type2.getTypeDimension())) {
            return type1.getTypeName().equals(type2.getTypeName());
        } else {
            return false;
        }
    }

    /**
     * This extracts IType instances from a string that looks like the example
     * below. It specifies an order to use for the list via the first arguments
     * in brackets (which are translated to TreeMap keys). The actual IType
     * instances are then derived from the second argument via type name and
     * TypeDimension
     *
     * <property name="outputTypes">
     * ( "0", "Country|One") ; ( "1", "Location|One")
     * </property>
     *
     * @param mapString
     * @return
     */
    public static List<IType> getTypesFromMapString(String mapString) {
        if (mapString == null) {
            throw new NullPointerException("mapString may not be null");
        }
        Map<String, String> map = ToolBelt.extractMap(mapString);
        List<IType> types = new ArrayList<>();
        for (String key : map.keySet()) {
            String[] values = map.get(key).split("\\|");
            types.add(DataConfiguration.getInstance().get(values[0], values[1]));
        }
        return types;
    }
}
