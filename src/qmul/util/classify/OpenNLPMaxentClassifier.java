/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package qmul.util.classify;

import java.io.IOException;
import java.util.ArrayList;

import opennlp.maxent.GIS;
import opennlp.maxent.GISModel;
import opennlp.model.Event;
import opennlp.model.EventStream;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.UnassignedClassException;
import edu.stanford.nlp.ling.BasicDatum;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.stats.ClassicCounter;

public class OpenNLPMaxentClassifier extends Classifier {

	private static final long serialVersionUID = 1625528434007607372L;

	private double smoothingObservation = GIS.SMOOTHING_OBSERVATION;
	private boolean printMessages = GIS.PRINT_MESSAGES;
	private int iterations = 0;
	private int cutoff = 0;
	private boolean smoothing = false;
	private double gaussianSmoothing = 0.0;

	private GISModel classifier;

	public OpenNLPMaxentClassifier() {
		// TODO set factory options?
	}

	/**
	 * @return the smoothingObservation
	 */
	public double getSmoothingObservation() {
		return smoothingObservation;
	}

	/**
	 * @param smoothingObservation
	 *            the smoothingObservation to set
	 */
	public void setSmoothingObservation(double smoothingObservation) {
		this.smoothingObservation = smoothingObservation;
	}

	/**
	 * @return the printMessages
	 */
	public boolean isPrintMessages() {
		return printMessages;
	}

	/**
	 * @param printMessages
	 *            the printMessages to set
	 */
	public void setPrintMessages(boolean printMessages) {
		this.printMessages = printMessages;
	}

	/**
	 * @return the iterations
	 */
	public int getIterations() {
		return iterations;
	}

	/**
	 * @param iterations
	 *            the iterations to set
	 */
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	/**
	 * @return the cutoff
	 */
	public int getCutoff() {
		return cutoff;
	}

	/**
	 * @param cutoff
	 *            the cutoff to set
	 */
	public void setCutoff(int cutoff) {
		this.cutoff = cutoff;
	}

	/**
	 * @return the smoothing
	 */
	public boolean isSmoothing() {
		return smoothing;
	}

	/**
	 * @param smoothing
	 *            the smoothing to set
	 */
	public void setSmoothing(boolean smoothing) {
		this.smoothing = smoothing;
	}

	/**
	 * @return the gaussianSmoothing
	 */
	public double getGaussianSmoothing() {
		return gaussianSmoothing;
	}

	/**
	 * @param gaussianSmoothing
	 *            the gaussianSmoothing to set
	 */
	public void setGaussianSmoothing(double gaussianSmoothing) {
		this.gaussianSmoothing = gaussianSmoothing;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.classifiers.Classifier#buildClassifier(weka.core.Instances)
	 */
	@Override
	public void buildClassifier(Instances data) throws Exception {
		MyEventStream onlpData = new MyEventStream();
		for (int i = 0; i < data.numInstances(); i++) {
			onlpData.add(wekaToStanford(data.instance(i)));
		}
		GIS.SMOOTHING_OBSERVATION = 0.01;
		classifier = GIS.trainModel(onlpData, 50, 0, true, false);
		System.out.println("trained ME classifier with labels " + classifier);
	}

	private class MyEventStream implements EventStream {

		private ArrayList<Event> events = new ArrayList<Event>();

		public void add(Event event) {
			events.add(event);
		}

		@Override
		public Event next() throws IOException {
			return events.remove(0);
		}

		@Override
		public boolean hasNext() throws IOException {
			return !events.isEmpty();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.classifiers.Classifier#classifyInstance(weka.core.Instance)
	 */
	@Override
	public double classifyInstance(Instance instance) throws Exception {
		Event event = wekaToStanford(instance);
		return new Double(classifier.getBestOutcome(classifier.eval(event.getContext())));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.classifiers.Classifier#distributionForInstance(weka.core.Instance)
	 */
	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		Event event = wekaToStanford(instance);
		double[] eval = classifier.eval(event.getContext(), event.getValues());
		// System.out.println(classifier.getAllOutcomes(eval));
		double[] prob = new double[eval.length];
		for (int i = 0; i < prob.length; i++) {
			prob[new Double(classifier.getOutcome(i)).intValue()] = eval[i];
		}
		return prob;
		// System.out.println("Actual class " + instance.classIndex() + " " + instance.classValue() + " "
		// + Math.exp(cnt.getCount(instance.classValue())));
		// System.out.println("Returning scores " + Arrays.toString(scores));
	}

	private Event wekaToStanford(Instance i) {
		boolean useRealValues = false;
		if (useRealValues) {
			return wekaToRVF(i);
		} else {
			return wekaToBasic(i);
		}
	}

	/**
	 * @param i
	 * @return a {@link BasicDatum} with features as string versions of instances as "featurelabel=value" (for non-zero
	 *         values), values as Doubles following WEKA practise (index of class if class is string/nominal)
	 */
	private Event wekaToBasic(Instance i) {
		double classValue = Double.NaN;
		try {
			classValue = i.classValue();
		} catch (UnassignedClassException e) {
			// do nothing
		}
		ArrayList<String> features = new ArrayList<String>();
		for (int iAtt = 0; iAtt < i.numAttributes(); iAtt++) {
			if (iAtt != i.classIndex()) {
				Attribute a = i.attribute(iAtt);
				double val = i.value(iAtt);
				if (val != 0.0) {
					features.add(a.name() + "=" + val);
				}
			}
		}
		String[] f = features.toArray(new String[features.size()]);
		Event d = (Double.isNaN(classValue) ? new Event("", f) : new Event("" + classValue, f));
		// System.out.println("new basicdatum " + d);
		return d;
	}

	/**
	 * @param i
	 * @return a {@link RVFDatum} with features as real-valued versions of instances featurelabel=value
	 */
	private Event wekaToRVF(Instance i) {
		double classValue = Double.NaN;
		try {
			classValue = i.classValue();
		} catch (UnassignedClassException e) {
			// do nothing
		}
		ClassicCounter<String> features = new ClassicCounter<String>();
		for (int iAtt = 0; iAtt < i.numAttributes(); iAtt++) {
			if (iAtt != i.classIndex()) {
				Attribute a = i.attribute(iAtt);
				double val = i.value(iAtt);
				if (val != 0.0) {
					features.setCount(a.name(), val);
				}
			}
		}
		String[] f = features.keySet().toArray(new String[features.keySet().size()]);
		float[] v = new float[f.length];
		for (int j = 0; j < v.length; j++) {
			v[j] = (float) features.getCount(f[j]);
		}
		Event d = (Double.isNaN(classValue) ? new Event("", f, v) : new Event("" + classValue, f, v));
		// System.out.println("new rvfdatum " + d);
		return d;
	}
}
