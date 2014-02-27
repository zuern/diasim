/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.dsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A {@link WindowBuffer} with a variable length (number of rows/historical instances). Adding new rows just increases
 * the length, no old data is dropped until the buffer is cleared.
 * 
 * @author mpurver
 */
public class VariableWindowBuffer implements WindowBuffer {

	private List<double[]> buffer;

	private double[] bufsum;

	private double[] bufprd;

	private double[] bufmax;

	private int[] bufmaxind;

	private List<?>[] bufsortind;

	private MyComparator[] comps;

	/**
	 * A 2-dimensional buffer with variable length.
	 * 
	 * @param j
	 *            the (fixed) width of the buffer (number of columns/features per instance)
	 */
	public VariableWindowBuffer(int j) {
		this(0, j);
	}

	/**
	 * A 2-dimensional buffer with variable length.
	 * 
	 * @param i
	 *            the initial length capacity of the buffer (number of rows/stored instances we're expecting)
	 * @param j
	 *            the (fixed) width of the buffer (number of columns/features per instance)
	 */
	public VariableWindowBuffer(int i, int j) {
		buffer = new ArrayList<double[]>(i);
		bufsum = new double[j];
		bufprd = new double[j];
		bufmax = new double[j];
		bufmaxind = new int[j];
		bufsortind = new ArrayList<?>[j];
		comps = new MyComparator[j];
		for (int k = 0; k < j; k++) {
			comps[k] = new MyComparator(k);
			bufsortind[k] = new ArrayList<Integer>(i);
		}
		clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#clear()
	 */
	public void clear() {
		buffer.clear();
		for (int j = 0; j < bufsum.length; j++) {
			bufsum[j] = 0.0;
			bufprd[j] = 1.0;
			bufmax[j] = Double.NEGATIVE_INFINITY;
			bufmaxind[j] = -1;
			bufsortind[j].clear();
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
		if (!(newValues.length == bufsum.length)) {
			throw new RuntimeException("Length mismatch: " + newValues.length + " " + bufsum.length);
		}
		// add to buffer, summing as we go
		int i = buffer.size();
		buffer.add(new double[bufsum.length]);
		for (int j = 0; j < bufsum.length; j++) {
			// fill
			buffer.get(i)[j] = newValues[j];
			((List<Integer>) bufsortind[j]).add(i);
			// store max for this subgroup (if tied, prefer more recent)
			if (newValues[j] >= bufmax[j]) {
				bufmax[j] = newValues[j];
				bufmaxind[j] = i;
			}
			// sum/multiply scores over the window
			bufsum[j] += newValues[j];
			bufprd[j] *= newValues[j];
		}
		// get sort indices
		for (int j = 0; j < bufsum.length; j++) {
			Collections.sort((List<Integer>) bufsortind[j], comps[j]);
		}
	}

	private class MyComparator implements Comparator<Integer> {

		private int ref;

		public MyComparator(int j) {
			ref = j;
		}

		public int compare(Integer i1, Integer i2) {
			return Double.compare(buffer.get(i2)[ref], buffer.get(i1)[ref]);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#getValue(int, int)
	 */
	public double getValue(int i, int j) {
		return buffer.get(i)[j];
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
		Integer[][] bsi = new Integer[bufsum.length][buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			for (int j = 0; j < bufsum.length; j++) {
				bsi[j][i] = (Integer) bufsortind[j].get(i);
			}
		}
		return bsi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#length()
	 */
	public int length() {
		return buffer.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.dsp.WindowBuffer#width()
	 */
	public int width() {
		return bufsum.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String out = "";
		for (int i = 0; i < buffer.size(); i++) {
			out += "Row " + i + ":";
			for (int j = 0; j < bufsum.length; j++) {
				out += " " + buffer.get(i)[j];
			}
			out += "\n";
		}
		return out;
	}

}
