/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.util.Collection;

import csli.util.Pair;
import csli.util.dsp.FixedWindowBuffer;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;

/**
 * A class which implements the standard Stanford classifier interface, but classifies in a naive way, by applying a
 * fixed set of filter weights to the features (which must be numerical) and then checking the sum (or max) against a
 * threshold. It can hold a buffer of past feature vectors and will sum (or max) over that buffer.
 * 
 * @author mpurver
 */
public class WeightedWindowClassifier extends Classifier {

	private FixedWindowBuffer buffer;

	private double[] filter;

	private boolean max;

	private double threshold;

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
	public WeightedWindowClassifier(double[] filter, int window, double threshold, boolean max) {
		// set params
		this.buffer = new FixedWindowBuffer(window, filter.length);
		this.filter = filter.clone();
		this.threshold = threshold;
		this.max = max;
	}

	public ClassicCounter<?> scoresOf(Datum example) {
		update(example);
		Pair<String, Double> res = test();

		ClassicCounter<Object> counter = new ClassicCounter<Object>();
		counter.setCount(res.first(), res.second());
		for (int f = 0; f < filter.length; f++) {
			counter.setCount(f, buffer.getMaxInds()[f]);
		}
		return counter;
	}

	public Object classOf(Datum example) {
		update(example);
		return test().first();
	}

	private void update(Datum example) {
		if (example != null) {
			Collection<?> featureC = example.asFeatures();
			double[] featureA = new double[featureC.size()];
			int i = 0;
			for (Object feature : featureC) {
				if (feature instanceof Double) {
					featureA[i] = (Double) feature;
				} else if (feature instanceof Integer) {
					featureA[i] = (Integer) feature;
				} else if (feature instanceof Boolean) {
					featureA[i] = ((Boolean) feature ? 1.0 : 0.0);
				}
				i++;
			}
			buffer.add(featureA);
		}
	}

	// now check for success
	private Pair<String, Double> test() {
		double[] scores = (max ? buffer.getMaxs() : buffer.getSums());
		double tot = 0;
		for (int f = 0; f < filter.length; f++) {
			tot += scores[f] * filter[f];
		}
		String label = (tot >= threshold) ? WeightedWindowClassifierFactory.posLabel
				: WeightedWindowClassifierFactory.negLabel;
		return new Pair<String, Double>(label, tot);
	}
}
