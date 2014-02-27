/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.dsp;

/**
 * @author mpurver
 */
public class Smoothing {

	/**
	 * Conglomerate nearby nonzero values within a window distance from one another into a conglomerate
	 * 
	 * @author alexgru
	 */
	public static double[] conglomerate(double[] vals, int window) {
		double newvals[] = (double[]) vals.clone();

		for (int i = 0; i < vals.length; i++) {
			if (vals[i] > 0) {
				int left = (i - window < 0) ? 0 : i - window;
				for (int j = left; j < i; j++) {
					if (vals[j] > 0) {
						for (int k = j; k < i; k++) {
							newvals[k] = vals[j];
						}
						break;
					}
				}

				int right = (i + window > vals.length) ? vals.length : i + window;

				for (int j = right - 1; j > i; j--) {
					if (vals[j] > 0) {
						for (int k = j; k > i; k--) {
							newvals[k] = vals[j];
						}
						break;
					}
				}
			}
		}

		return newvals;
	}

	/**
	 * Conglomerate nearby nonzero values within a window distance from one another into a conglomerate
	 * 
	 * @author alexgru
	 */
	public static int[] conglomerate(int[] vals, int window) {
		int newvals[] = (int[]) vals.clone();

		for (int i = 0; i < vals.length; i++) {
			if (vals[i] > 0) {
				int left = (i - window < 0) ? 0 : i - window;
				for (int j = left; j < i; j++) {
					if (vals[j] > 0) {
						for (int k = j; k < i; k++) {
							newvals[k] = vals[j];
						}
						break;
					}
				}

				int right = (i + window > vals.length) ? vals.length : i + window;

				for (int j = right - 1; j > i; j--) {
					if (vals[j] > 0) {
						for (int k = j; k > i; k--) {
							newvals[k] = vals[j];
						}
						break;
					}
				}
			}
		}

		return newvals;
	}

	/**
	 * @param raw
	 * @return the same sequence with isolated non-zero values (surrounded by zeros) removed
	 */
	public static double[] removeSingletons(double[] raw) {
		double[] cooked = new double[raw.length];
		for (int i = 0; i < raw.length; i++) {
			cooked[i] = raw[i];
			boolean found = false;
			// check backward
			if ((i > 0) && (raw[i - 1] > 0)) {
				found = true;
			}
			// check forward
			if ((i < (raw.length - 1)) && (raw[i + 1] > 0)) {
				found = true;
			}
			// no neighbour? remove
			if (!found) {
				cooked[i] = 0;
			}
		}
		return cooked;
	}

	/**
	 * @param raw
	 * @return the same sequence with isolated non-zero values (surrounded by zeros) removed
	 */
	public static int[] removeSingletons(int[] raw) {
		int[] cooked = new int[raw.length];
		for (int i = 0; i < raw.length; i++) {
			cooked[i] = raw[i];
			boolean found = false;
			// check backward
			if ((i > 0) && (raw[i - 1] > 0)) {
				found = true;
			}
			// check forward
			if ((i < (raw.length - 1)) && (raw[i + 1] > 0)) {
				found = true;
			}
			// no neighbour? remove
			if (!found) {
				cooked[i] = 0;
			}
		}
		return cooked;
	}

	/**
	 * Smooth an array of doubles with a given FIR filter
	 * 
	 * @author mpurver
	 * @param raw
	 *            the unsmoothed input series
	 * @param filter
	 *            the filter (coeff 0 applied to current input, 1..n applied to previous input)
	 * @return the smoothed output series
	 */
	public static double[] smoothFIR(double[] raw, double[] filter) {
		double[] cooked = new double[raw.length];
		for (int i = 0; i < raw.length; i++) {
			cooked[i] = 0.0;
			for (int j = 0; j < filter.length; j++) {
				int ind = (i - j);
				cooked[i] += filter[j] * (ind < 0 ? 0.0 : raw[ind]);
			}
		}
		return cooked;
	}

	/**
	 * Smooth an array of doubles with a given IIR filter
	 * 
	 * @author mpurver
	 * @param raw
	 *            the unsmoothed input series
	 * @param filter
	 *            the filter (coeff 0 applied to current input, 1..n applied to previous output)
	 * @return the smoothed output series
	 */
	public static double[] smoothIIR(double[] raw, double[] filter) {
		double[] cooked = new double[raw.length];
		for (int i = 0; i < raw.length; i++) {
			cooked[i] = filter[0] * raw[i];
			for (int j = 1; j < filter.length; j++) {
				int ind = (i - j);
				cooked[i] += filter[j] * (ind < 0 ? 0.0 : cooked[ind]);
			}
		}
		return cooked;
	}

	/**
	 * Smooth an array of doubles by convolution with a given kernel
	 * 
	 * @author mpurver
	 * @param raw
	 *            the unsmoothed input series
	 * @param kernel
	 *            the 1-dimensional kernel
	 * @return the smoothed output series
	 */
	public static double[] smooth(double[] raw, double[] kernel) {
		int x = raw.length;
		int k = kernel.length;
		int n = (k - 1) / 2;
		double[] cooked = new double[x];
		for (int i = 0; i < x; i++) {
			cooked[i] = 0.0;
			for (int j = 0; j < k; j++) {
				double tmp;
				if ((i + j - n) <= 0) {
					tmp = raw[0];
				} else if ((i + j - n) >= x) {
					tmp = raw[x - 1];
				} else {
					tmp = raw[i + j - n];
				}
				cooked[i] += kernel[j] * tmp;
			}
		}
		return cooked;
	}

	/**
	 * Produce a 1-dimensional Gaussian convolution kernel
	 * 
	 * @author mpurver
	 * @param n
	 *            produces a kernel of length 2n+1
	 * @return the kernel array
	 */
	public static double[] gaussianKernel(int n) {
		double[] h = new double[2 * n + 1];
		double sigma = ((double) n) / 3;
		double sum = 0.0;

		for (int i = 0; i < h.length; i++) {
			double x = i - n;
			h[i] = Math.exp(-(x * x) / (2 * sigma * sigma));
			sum += h[i];
		}
		for (int i = 0; i < h.length; i++) {
			h[i] = h[i] / sum;
		}
		return h;
	}

	public static void main(String[] args) {
		double[] ds = new double[10];

		for (int window = 0; window < 4; window++) {
			ds[0] = 1;
			ds[1] = 0;
			ds[2] = 1;
			ds[3] = 1;
			ds[4] = 0;
			ds[5] = 0;
			ds[6] = 1;
			ds[7] = 0;
			ds[8] = 0;
			ds[9] = 1;

			ds = conglomerate(ds, window);
			System.out.println();
			System.out.println("window=" + window);
			for (int i = 0; i < 10; i++) {
				System.out.println(ds[i]);
			}
		}

	}

}