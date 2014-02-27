/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.util.List;

import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;

/**
 * A binary classifier which uses a single n-gram model, making class decision by comparing log-likelihood with a given
 * threshold
 * 
 * @author mpurver
 */
public class NaiveNgramClassifier extends Classifier {

	private NgramModel model;

	private double threshold = 0.0;

	private Object label;

	private boolean maxLL = false;

	public NaiveNgramClassifier(NgramModel model, Object label, double threshold, boolean max) {
		this.model = model;
		this.label = label;
		this.threshold = threshold;
		this.maxLL = max;
	}

	public NaiveNgramClassifier(NgramModel model, Object label) {
		this.model = model;
		this.label = label;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public Object classOf(Datum example) {
		double ll = maxLL ? model.maxLogLikelihood((List<?>) example.asFeatures()) : model
				.normalizedLL((List<?>) example.asFeatures());
		if (ll > threshold) {
			return label;
		} else {
			return null;
		}
	}

	public ClassicCounter<?> scoresOf(Datum example) {
		double ll = maxLL ? model.maxLogLikelihood((List<?>) example.asFeatures()) : model
				.normalizedLL((List<?>) example.asFeatures());
		ClassicCounter<Object> scores = new ClassicCounter<Object>();
		scores.setCount(label, ll);
		return scores;
	}

}
