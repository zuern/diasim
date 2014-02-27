/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.util.List;

import edu.stanford.nlp.ling.Datum;

/**
 * A class which implements the standard Stanford classifier interface, but classifies in a naive way, by applying a
 * fixed set of filter weights to the features (which must be numerical) and then checking the sum (or max) against a
 * threshold. It can hold a buffer of past feature vectors and will sum (or max) over that buffer.
 * 
 * @author mpurver
 */
public class WeightedWindowClassifierFactory implements ClassifierFactory {

	private double[] filter;

	private int window;

	private boolean max;

	private double threshold;

	public static final String posLabel = "+1";

	public static final String negLabel = "-1";

	/**
	 * A class which implements the standard Stanford classifier interface, but classifies in a naive way, by applying a
	 * fixed set of filter weights to the features (which must be numerical) and then checking the sum (or max) against
	 * a threshold. It can hold a buffer of past feature vectors and will sum (or max) over that buffer.
	 * 
	 * @param window
	 *            the history buffer size (set to 1 for no history)
	 * @param max
	 *            if true, take the maximum weighted value over the buffer, rather than the sum
	 */
	public WeightedWindowClassifierFactory(int window, boolean max) {
		this.window = window;
		this.max = max;
	}

	/**
	 * A class which implements the standard Stanford classifier interface, but classifies in a naive way, by applying a
	 * fixed set of filter weights to the features (which must be numerical) and then checking the sum (or max) against
	 * a threshold. It can hold a buffer of past feature vectors and will sum (or max) over that buffer.
	 * 
	 * @param filter
	 *            the array of weights, one per feature in feature order
	 * @param window
	 *            the history buffer size (set to 1 for no history)
	 * @param threshold
	 *            the classification threshold
	 * @param max
	 *            if true, take the maximum weighted value over the buffer, rather than the sum
	 */
	public WeightedWindowClassifierFactory(double[] filter, int window, double threshold, boolean max) {
		this.filter = filter.clone();
		this.window = window;
		this.threshold = threshold;
		this.max = max;
	}

	/**
	 * A class which implements the standard Stanford classifier interface, but classifies in a naive way, by applying a
	 * fixed set of filter weights to the features (which must be numerical) and then checking the sum (or max) against
	 * a threshold. It can hold a buffer of past feature vectors and will sum (or max) over that buffer.
	 * 
	 * @param filter
	 *            the list of weights, one per feature in feature order
	 * @param window
	 *            the history buffer size (set to 1 for no history)
	 * @param threshold
	 *            the classification threshold
	 * @param max
	 *            if true, take the maximum weighted value over the buffer, rather than the sum
	 */
	public WeightedWindowClassifierFactory(List<Double> filter, int window, double threshold, boolean max) {
		this.filter = new double[filter.size()];
		for (int f = 0; f < filter.size(); f++) {
			this.filter[f] = filter.get(f);
		}
		this.window = window;
		this.threshold = threshold;
		this.max = max;
	}

	public <D extends Datum> Classifier trainClassifier(List<D> examples) {
		// if filter/threshold not yet specified, learn them
		if (filter == null) {
			int size = examples.get(0).asFeatures().size();
			filter = new double[size];
			double[] negSum = new double[size];
			double[] posSum = new double[size];
			for (int i = 0; i < size; i++) {
				negSum[i] = 0;
				posSum[i] = 0;
			}
			double nPos = 0;
			double nNeg = 0;
			for (Datum example : examples) {
				if (example.asFeatures().size() != size) {
					System.err.println("data size doesn't match " + example.asFeatures().size() + " " + size);
					return null;
				}
				if (example.label().equals(posLabel)) {
					for (int i = 0; i < size; i++) {
						posSum[i] += (Double) ((List<?>) example.asFeatures()).get(i);
						nPos++;
					}
				} else {
					for (int i = 0; i < size; i++) {
						negSum[i] += (Double) ((List<?>) example.asFeatures()).get(i);
						nNeg++;
					}
				}
			}
			for (int i = 0; i < size; i++) {
				double avPos = (posSum[i] / nPos);
				double avNeg = (negSum[i] / nNeg);
				filter[i] = (avPos - avNeg) * 2 / (avPos + avNeg);
				System.out.println("Filter " + i + " = " + filter[i]);
			}
			threshold = size;
		}
		return new WeightedWindowClassifier(filter, window, threshold, max);
	}

}
