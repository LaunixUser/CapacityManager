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
package org.ml.capman.render;

/**
 * @author mlaux
 */
public abstract class AbstractTableCreator {

    public final static String KEY_STYLE = "style";
    public final static String KEY_NAME = "name";
    public final static int DEFAULT_TABLE_SIZE = 50;
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
