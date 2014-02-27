/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.classify.ClassifierFactory;
import edu.stanford.nlp.ling.Datum;

public class NgramClassifierFactory implements ClassifierFactory {

	private int n = 1;

	private double smoothing = 0.0;

	public NgramClassifierFactory(int n, double smoothing) {
		this.n = n;
		this.smoothing = smoothing;
	}

	public <D extends Datum> Classifier trainClassifier(List<D> examples) {

		// we must train a model for each label
		HashMap<Object, NgramModel> models = new HashMap<Object, NgramModel>();
		HashMap<Object, List<List<?>>> training = new HashMap<Object, List<List<?>>>();
		for (Datum example : examples) {
			Object label = example.label();
			if (models.get(label) == null) {
				models.put(label, new NgramModel(n));
				training.put(label, new ArrayList<List<?>>());
			}
			training.get(label).add((List<?>) example.asFeatures());
		}
		for (Object label : models.keySet()) {
			System.out.print("training for label " + label + " ... ");
			models.get(label).train(training.get(label), smoothing);
			System.out.println("done");
		}
		return new NgramClassifier(models);
	}

}
