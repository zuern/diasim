/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package qmul.util.classify;

import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.UnassignedClassException;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.classify.LinearClassifierFactory;
import edu.stanford.nlp.ling.BasicDatum;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.stats.ClassicCounter;

public class StanfordMaxentClassifier extends Classifier {

	private static final long serialVersionUID = 1625528434007607372L;

	private LinearClassifier classifier;

	public StanfordMaxentClassifier() {
		// TODO set factory options?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.classifiers.Classifier#buildClassifier(weka.core.Instances)
	 */
	@Override
	public void buildClassifier(Instances data) throws Exception {
		ArrayList<Datum> stanfordData = new ArrayList<Datum>();
		for (int i = 0; i < data.numInstances(); i++) {
			stanfordData.add(wekaToStanford(data.instance(i)));
		}
		LinearClassifierFactory f = new LinearClassifierFactory();
		// f.setPrior(new LogPrior(LogPriorType.QUADRATIC, 1.0, 0.1));
		f.useQuasiNewton();
		// f.useConjugateGradientAscent();
		// f.useStochasticGradientDescent();
		// f.setTuneSigmaHeldOut();
		// f.setSigma(0.25);
		// f.setTol(1e-5);
		classifier = (LinearClassifier) f.trainClassifier(stanfordData);
		System.out.println("trained ME classifier with labels " + classifier.labels());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.classifiers.Classifier#classifyInstance(weka.core.Instance)
	 */
	@Override
	public double classifyInstance(Instance instance) throws Exception {
		Datum datum = wekaToStanford(instance);
		return (Double) classifier.classOf(datum);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.classifiers.Classifier#distributionForInstance(weka.core.Instance)
	 */
	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		Datum datum = wekaToStanford(instance);
		ClassicCounter cnt = classifier.probabilityOf(datum);
		double[] scores = new double[classifier.labels().size()];
		for (int i = 0; i < scores.length; i++) {
			scores[i] = cnt.getCount(new Double(i));
		}
		// System.out.println("Actual class " + instance.classIndex() + " " + instance.classValue() + " "
		// + Math.exp(cnt.getCount(instance.classValue())));
		// System.out.println("Returning scores " + Arrays.toString(scores));
		return scores;
	}

	private Datum wekaToStanford(Instance i) {
		boolean useRealValues = true;
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
	private BasicDatum wekaToBasic(Instance i) {
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
		BasicDatum d = (Double.isNaN(classValue) ? new BasicDatum(features) : new BasicDatum(features, new Double(
				classValue)));
		// System.out.println("new basicdatum " + d);
		return d;
	}

	/**
	 * @param i
	 * @return a {@link RVFDatum} with features as real-valued versions of instances featurelabel=value
	 */
	private RVFDatum wekaToRVF(Instance i) {
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
		RVFDatum d = (Double.isNaN(classValue) ? new RVFDatum(features)
				: new RVFDatum(features, new Double(classValue)));
		// System.out.println("new rvfdatum " + d.asFeaturesCounter());
		return d;
	}
}
