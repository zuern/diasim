/*
 * Created on Oct 31, 2007 by mpurver
 */
package csli.util.dsp;

import java.util.List;

/**
 * A 2-dimensional buffer in which each row is a (historical) instance, and each column a feature. Data is added one row
 * (instance) at a time. Number of columns (features) must be fixed; number of rows may not be. Efficiently calculates
 * the column rows/sums during the shift/add operation.
 * 
 * @author mpurver
 */
public interface WindowBuffer {

    /**
     * Clear the buffer, filling with zeros where appropriate
     */
    public abstract void clear();

    /**
     * Add a new instance (row) to the end of the buffer (shifting others down one if the buffer length is fixed).
     * 
     * @param newValues
     *            a data list of length ncolumns
     */
    public abstract void add(List<Double> newValues);

    /**
     * Add a new instance (row) to the end of the buffer (shifting others down one if the buffer length is fixed).
     * 
     * @param newValues
     *            a data array of length ncolumns
     */
    public abstract void add(Double[] newValues);

    /**
     * Add a new instance (row) to the end of the buffer (shifting others down one if the buffer length is fixed).
     * 
     * @param newValues
     *            a data array of length ncolumns
     */
    public abstract void add(double[] newValues);

    /**
     * @param i
     *            the row (historical data instance, oldest = 0)
     * @param j
     *            the column (feature)
     * @return the value for a particular row & column
     */
    public abstract double getValue(int i, int j);

    /**
     * @return the sums for each column (feature)
     */
    public abstract double[] getSums();

    /**
     * @return the products for each column (feature)
     */
    public abstract double[] getProducts();

    /**
     * @return the maximum values for each column (feature)
     */
    public abstract double[] getMaxs();

    /**
     * @return the row (instance) indices of the maximum values for each column (feature)
     */
    public abstract int[] getMaxInds();

    /**
     * @return a per-column array of row index arrays which contain the indices of the sorted (highest first) values for
     *         each column
     */
    public abstract Integer[][] getSortInds();

    /**
     * @return the length of the buffer (number of rows/data instances)
     */
    public abstract int length();

    /**
     * @return the width of the buffer (number of columns/features)
     */
    public abstract int width();

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public abstract String toString();

}