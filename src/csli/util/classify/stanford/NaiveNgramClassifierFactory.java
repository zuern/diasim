/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.classify.ClassifierFactory;
import edu.stanford.nlp.ling.Datum;

public class NaiveNgramClassifierFactory implements ClassifierFactory {

	private int maxN = 1;

	private int minN = -1;

	private double smoothing = 0.0;

	private double threshold = 0.0;

	private Object label = null;

	private boolean maxLL = false;

	public NaiveNgramClassifierFactory(int minN, int maxN, double smoothing, Object label, double threshold,
			boolean maxLL) {
		this.minN = minN;
		this.maxN = maxN;
		this.smoothing = smoothing;
		this.label = label;
		this.threshold = threshold;
		this.maxLL = maxLL;
	}

	public NaiveNgramClassifierFactory(int n, double smoothing, Object label, double threshold, boolean maxLL) {
		this.maxN = n;
		this.smoothing = smoothing;
		this.label = label;
		this.threshold = threshold;
		this.maxLL = maxLL;
	}

	public <D extends Datum> Classifier trainClassifier(List<D> examples) {

		// we must train a model for each label
		NgramModel model = new NgramModel(minN, maxN);
		List<List<?>> training = new ArrayList<List<?>>();
		for (Datum example : examples) {
			if (this.label.equals(example.label())) {
				training.add((List<?>) example.asFeatures());
			}
		}
		System.out.print("training for label " + label + " ... ");
		model.train(training, smoothing);
		System.out.println("done");
		return new NaiveNgramClassifier(model, label, threshold, maxLL);
	}

}
