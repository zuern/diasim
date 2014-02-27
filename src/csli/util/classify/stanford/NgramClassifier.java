/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;

/**
 * A classifier which uses multiple n-gram models, making class decision based on best log-likelihood
 * 
 * @author mpurver
 */
public class NgramClassifier extends Classifier {

	private HashMap<Object, NgramModel> models = new HashMap<Object, NgramModel>();

	public <O extends Object, N extends NgramModel> NgramClassifier(HashMap<O, N> models) {
		for (Object label : models.keySet()) {
			this.models.put(label, models.get(label));
		}
	}

	public Object classOf(Datum example) {
		double maxLL = Double.NEGATIVE_INFINITY;
		Object bestLabel = null;
		for (Object label : models.keySet()) {
			double myLL = models.get(label).normalizedLL((List<?>) example.asFeatures());
			if (myLL > maxLL) {
				bestLabel = label;
				maxLL = myLL;
			}
		}
		return bestLabel;
	}

	public ClassicCounter<?> scoresOf(Datum example) {
		ClassicCounter<Object> scores = new ClassicCounter<Object>();
		for (Object label : models.keySet()) {
			scores.setCount(label, models.get(label).normalizedLL((List<?>) example.asFeatures()));
		}
		return scores;
	}

}
