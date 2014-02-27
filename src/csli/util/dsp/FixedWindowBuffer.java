/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.dsp;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A {@link WindowBuffer} with a fixed length (number of rows/historical instances). Adding new rows causes the oldest
 * row to be dropped.
 * 
 * @author mpurver
 */
public class FixedWindowBuffer implements WindowBuffer {

	private double[][] buffer;

	private double[] bufsum;

	private double[] bufprd;

	private double[] bufmax;

	private int[] bufmaxind;

	private Integer[][] bufsortind;

	private MyComparator[] comps;

	/**
	 * A 2-dimensional buffer
	 * 
	 * @param i
	 *            the length of the buffer (number of rows/stored instances)
	 * @param j
	 *            the width of the buffer (number of columns/features per instance)
	 */
	public FixedWindowBuffer(int i, int j) {
		buffer = new double[i][j];
		bufsum = new double[j];
		bufprd = new double[j];
		bufmax = new double[j];
		bufmaxind = new int[j];
		bufsortind = new Integer[j][i];
		comps = new MyComparator[j];
		for (int k = 0; k < j; k++) {
			comps[k] = new MyComparator(k);
			for (int l = 0; l < i; l++) {
				bufsortind[k][l] = l;
			}
		}
		clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#clear()
	 */
	public void clear() {
		for (int i = 0; i < buffer.length; i++) {
			for (int j = 0; j < buffer[i].length; j++) {
				buffer[i][j] = 0.0;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#add(java.util.List)
	 */
	public void add(List<Double> newValues) {
		add(newValues.toArray(new Double[newValues.size()]));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#add(java.lang.Double[])
	 */
	public void add(Double[] newValues) {
		double[] nv = new double[newValues.length];
		for (int i = 0; i < nv.length; i++) {
			nv[i] = newValues[i];
		}
		add(nv);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#add(double[])
	 */
	public void add(double[] newValues) {
		if (!(newValues.length == buffer[0].length)) {
			throw new RuntimeException("Length mismatch: " + newValues.length + " " + buffer[0].length);
		}
		for (int j = 0; j < buffer[0].length; j++) {
			bufsum[j] = 0.0;
			bufprd[j] = 1.0;
			bufmax[j] = Double.NEGATIVE_INFINITY;
			bufmaxind[j] = -1;
		}
		// shift and refill buffer, summing as we go
		for (int i = 0; i < buffer.length; i++) {
			for (int j = 0; j < buffer[i].length; j++) {
				// shift/refill
				if (i < (buffer.length - 1)) {
					// shift
					buffer[i][j] = buffer[i + 1][j];
				} else {
					// fill
					buffer[i][j] = newValues[j];
				}
				// store max for this subgroup (if tied, prefer more recent)
				if (buffer[i][j] >= bufmax[j]) {
					bufmax[j] = buffer[i][j];
					bufmaxind[j] = i;
				}
				// sum/multiply scores over the window
				bufsum[j] += buffer[i][j];
				bufprd[j] *= buffer[i][j];
			}
		}
		// get sort indices
		for (int j = 0; j < buffer[0].length; j++) {
			Arrays.sort(bufsortind[j], comps[j]);
		}
	}

	private class MyComparator implements Comparator<Integer> {

		private int ref;

		public MyComparator(int j) {
			ref = j;
		}

		public int compare(Integer i1, Integer i2) {
			return Double.compare(buffer[i2][ref], buffer[i1][ref]);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#getValue(int, int)
	 */
	public double getValue(int i, int j) {
		return buffer[i][j];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#getSums()
	 */
	public double[] getSums() {
		return bufsum;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#getProducts()
	 */
	public double[] getProducts() {
		return bufprd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#getMaxs()
	 */
	public double[] getMaxs() {
		return bufmax;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#getMaxInds()
	 */
	public int[] getMaxInds() {
		return bufmaxind;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#getSortInds()
	 */
	public Integer[][] getSortInds() {
		return bufsortind;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#length()
	 */
	public int length() {
		return buffer.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#width()
	 */
	public int width() {
		return buffer[0].length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String out = "";
		for (int i = 0; i < buffer.length; i++) {
			out += "Row " + i + ":";
			for (int j = 0; j < buffer[i].length; j++) {
				out += " " + buffer[i][j];
			}
			out += "\n";
		}
		return out;
	}

}
