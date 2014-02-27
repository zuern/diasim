/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import csli.util.dsp.FixedWindowBuffer;
import edu.stanford.nlp.classify.LinearClassifierFactory;
import edu.stanford.nlp.ling.BasicDatum;
import edu.stanford.nlp.ling.Datum;

/**
 * A class which implements the standard Stanford classifier interface, but uses a buffered context to build features
 * 
 * @author mpurver
 */
public class MaxentWindowClassifierFactory implements ClassifierFactory {

	private int window;

	private boolean useZeros;

	private LinearClassifierFactory factory;

	/**
	 * A class which implements the standard Stanford classifier interface, but uses a buffered context to build
	 * features
	 * 
	 * @param window
	 *            the history buffer size (set to 1 for no history, but in that case use the standard MaxentClassifer
	 *            instead)
	 */
	public MaxentWindowClassifierFactory(int window, double sigma) {
		this(window, true, sigma);
	}

	/**
	 * A class which implements the standard Stanford classifier interface, but uses a buffered context to build
	 * features
	 * 
	 * @param window
	 *            the history buffer size (set to 1 for no history, but in that case use the standard MaxentClassifer
	 *            instead)
	 */
	public MaxentWindowClassifierFactory(int window, double sigma, double tol) {
		this(window, true, sigma, tol);
	}

	/**
	 * A class which implements the standard Stanford classifier interface, but uses a buffered context to build
	 * features
	 * 
	 * @param window
	 *            the history buffer size (set to 1 for no history, but in that case use the standard MaxentClassifer
	 *            instead)
	 * @param useZeros
	 *            if false, only add features
	 */
	public MaxentWindowClassifierFactory(int window, boolean useZeros, double sigma) {
		this.window = window;
		this.useZeros = useZeros;
		this.factory = new LinearClassifierFactory();
		factory.setSigma(sigma);
		factory.setVerbose(true);
	}

	/**
	 * A class which implements the standard Stanford classifier interface, but uses a buffered context to build
	 * features
	 * 
	 * @param window
	 *            the history buffer size (set to 1 for no history, but in that case use the standard MaxentClassifer
	 *            instead)
	 * @param useZeros
	 *            if false, only add features
	 */
	public MaxentWindowClassifierFactory(int window, boolean useZeros, double sigma, double tol) {
		this.window = window;
		this.useZeros = useZeros;
		this.factory = new LinearClassifierFactory();
		factory.setSigma(sigma);
		factory.setTol(tol);
		factory.setVerbose(true);
	}

	public <D extends Datum> Classifier trainClassifier(List<D> examples) {
		int width = examples.get(0).asFeatures().size();
		List<BasicDatum> windowedExamples = new ArrayList<BasicDatum>(examples.size());
		FixedWindowBuffer buffer = new FixedWindowBuffer(window, width);
		for (Datum example : examples) {
			buffer.add(doubleFeatures(example.asFeatures()));
			Collection<String> features = bufferFeatures(buffer, useZeros);
			windowedExamples.add(new BasicDatum(features, example.labels()));
		}
		return new MaxentWindowClassifier(factory.trainClassifier(windowedExamples), window, width, useZeros);
	}

	public static double[] doubleFeatures(Collection<?> featureC) {
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
		return featureA;
	}

	public static Collection<String> bufferFeatures(FixedWindowBuffer buffer, boolean useZeros) {
		Collection<String> features = new HashSet<String>();
		for (int i = 0; i < buffer.length(); i++) {
			for (int j = 0; j < buffer.width(); j++) {
				if (useZeros || (buffer.getValue(i, j) > 0)) {
					features.add(i + ":" + j + ":" + buffer.getValue(i, j));
				}
			}
		}
		return features;
	}

}
