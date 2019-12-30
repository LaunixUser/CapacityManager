package org.ml.capman.render.experimental;

/**
 * @author mlaux
 */
public abstract class AbstractTableCreator {

    //public final static int DEFAULT_TABLE_SIZE = 50;
    protected final static String KEY_TOTALS = "Totals";
    protected final static String KEY_SUBTOTALS = "Subtotals";

    /**
     *
     */
    public enum OutputMode {
        TWO_COLUMNS, XY, XY_INVERTED
    }

    /**
     * @param value
     * @param sum
     * @return
     */
    public static double getPercentage(double value, double sum) {
        if (sum != 0) {
            return value / sum;
        } else {
            return 0.0;
        }
    }

    /**
     * @param value
     * @param sum
     * @return
     */
    public static double getPercentageInt(int value, int sum) {
        if (sum != 0) {
            return (double) value / (double) sum;
        } else {
            return 0;
        }
    }

}
