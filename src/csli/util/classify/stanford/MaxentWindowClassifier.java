/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.util.Collection;

import csli.util.dsp.FixedWindowBuffer;
import edu.stanford.nlp.ling.BasicDatum;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;

/**
 * A class which implements the standard Stanford classifier interface, but uses a buffered context to build features
 * 
 * @author mpurver
 */
public class MaxentWindowClassifier extends Classifier {

	private FixedWindowBuffer buffer;

	private Classifier classifier;

	private boolean useZeros;

	public MaxentWindowClassifier(Classifier classifier, int length, int width) {
		this(classifier, length, width, true);
	}

	public MaxentWindowClassifier(Classifier classifier, int length, int width, boolean useZeros) {
		this.classifier = classifier;
		this.useZeros = useZeros;
		this.buffer = new FixedWindowBuffer(length, width);
	}

	public ClassicCounter<?> scoresOf(Datum example) {
		Datum windowedExample = update(example);
		return classifier.scoresOf(windowedExample);
	}

	public Object classOf(Datum example) {
		Datum windowedExample = update(example);
		return classifier.classOf(windowedExample);
	}

	private Datum update(Datum example) {
		if (example != null) {
			buffer.add(MaxentWindowClassifierFactory.doubleFeatures(example.asFeatures()));
		}
		Collection<String> features = MaxentWindowClassifierFactory.bufferFeatures(buffer, useZeros);
		return new BasicDatum(features, example.labels());
	}

}
