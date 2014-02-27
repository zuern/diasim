/*******************************************************************************
 * Copyright (c) 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Matthew Purver, Queen Mary University of London - initial API and implementation
 ******************************************************************************/
package qmul.annotation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import qmul.corpus.BNCCorpus;
import qmul.corpus.DialogueCorpus;
import qmul.corpus.DialogueSentence;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Range;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;
import edu.stanford.nlp.classify.LinearClassifierFactory;
import edu.stanford.nlp.ling.BasicDatum;
import edu.stanford.nlp.ling.Datum;

public class SplitUtteranceTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String corpusName = "bnc-su.corpus";
		File bncFile = new File(corpusName);
		BNCCorpus bnc = (bncFile.exists() ? (BNCCorpus) DialogueCorpus.readFromFile(bncFile) : new BNCCorpus(
				"/import/imc-corpora/corpora/BNC-XML", true));
		AnnotationSet annSet = new AnnotationSet(bnc, new File(new File("/Users").exists() ? "../SCoRE/tasks/SPLIT"
				: "/import/imc-corpora/corpus-tools/SCoRE/stable/tasks/SPLIT"));
		annSet.addAll(new File(new File("/Users").exists() ? "/Users/mpurver/Documents/dyndial/corpus/annotations/auto"
				: "/import/imc/annotations.score/auto"));
		if (!bncFile.exists()) {
			bnc.writeToFile(bncFile);
		}
		boolean corpusChanged = false;

		FastVector attInfo = new FastVector();
		// Attribute labelAtt = FeatureFactory.newYNAttribute("end-complete");
		Attribute labelAtt = FeatureFactory.newYNAttribute("continues");
		attInfo.addElement(labelAtt);
		attInfo.addElement(new Attribute(FeatureFactory.LAST_POS, (FastVector) null));
		attInfo.addElement(new Attribute(FeatureFactory.LAST_POS_BIGRAM, (FastVector) null));
		attInfo.addElement(new Attribute(FeatureFactory.FIRST_POS, (FastVector) null));
		attInfo.addElement(new Attribute(FeatureFactory.FIRST_POS_BIGRAM, (FastVector) null));
		attInfo.addElement(new Attribute(FeatureFactory.LAST_LEMMA, (FastVector) null));
		attInfo.addElement(new Attribute(FeatureFactory.FIRST_LEMMA, (FastVector) null));
		attInfo.addElement(new Attribute(FeatureFactory.PARSE_PROB));

		Instances instances = new Instances("splits", attInfo, 0);
		instances.setClass(labelAtt);
		int i = annSet.getTags().indexOf("continues");
		// int i = annSet.getTags().indexOf("end-complete");
		for (String dialogue : annSet.getAnnotations().keySet()) {
			for (Integer iSent : annSet.getAnnotations(dialogue).getTaggedSents()) {
				// System.out.println("Dialogue " + dialogue + " " + iSent + " " + annSet.getTagValues(dialogue));
				Instance inst = new Instance(instances.numAttributes());
				String label = ((annSet.getTagValues(dialogue).get(iSent).size() <= i) ? "" : annSet.getTagValues(
						dialogue).get(iSent).get(i));
				FeatureFactory.setYNClass(inst, label);
				System.out.println("set val " + label + " " + dialogue + " " + iSent + " "
						+ annSet.getTagValues(dialogue).get(iSent));
				DialogueSentence sent = bnc.getDialogue(dialogue).getSent(iSent);
				if (sent == null) {
					System.out.println("No sentence found " + iSent);
				} else {
					double preProb = sent.getSyntaxProb();
					for (int a = 0; a < attInfo.size(); a++) {
						if (!inst.attribute(a).equals(labelAtt)) {
							FeatureFactory.setAttribute(inst, inst.attribute(a), sent);
						}
					}
					if (!Double.isNaN(sent.getSyntaxProb()) && (sent.getSyntaxProb() != preProb)) {
						corpusChanged = true;
					}
				}
				instances.add(inst);
			}
		}

		if (corpusChanged) {
			bnc.writeToFile(bncFile);
		}

		// weight instances?
		if (true) {
			double[] counts = new double[2];
			for (int j = 0; j < instances.numInstances(); j++) {
				counts[(int) instances.instance(j).value(labelAtt)]++;
			}
			for (int j = 0; j < instances.numInstances(); j++) {
				Instance inst = instances.instance(j);
				inst.setWeight(instances.numInstances() / counts[(int) inst.value(labelAtt)]);
			}
		}

		if (true) {
			// if (classifier instanceof edu.stanford.nlp.classify.Classifier) {
			List<Datum> data = wekaToStanford(instances);
			edu.stanford.nlp.classify.Classifier classifier = new LinearClassifierFactory().trainClassifier(data);
		}

		try {
			StringToNominal f = new StringToNominal();
			f.setAttributeRange("first-last");
			f.setInputFormat(instances);
			instances = Filter.useFilter(instances, f);
			Classifier classifier = getClassifier();
			Evaluation eval = new Evaluation(instances);
			StringBuffer predictions = new StringBuffer();
			eval.crossValidateModel(classifier, instances, 10, new Random(1), predictions, new Range("first"), true);
			System.out.println(eval.toSummaryString());
			System.out.println(eval.toClassDetailsString());
			System.out.println(eval.toMatrixString());

			// baselines
			classifier = new ZeroR();
			eval = new Evaluation(instances);
			predictions = new StringBuffer();
			eval.crossValidateModel(classifier, instances, 10, new Random(1), predictions, new Range("first"), true);
			System.out.println(eval.toSummaryString());
			System.out.println(eval.toClassDetailsString());
			System.out.println(eval.toMatrixString());
			System.exit(0);
			classifier = new JRip();
			eval = new Evaluation(instances);
			predictions = new StringBuffer();
			eval.crossValidateModel(classifier, instances, 10, new Random(1), predictions, new Range("first"), true);
			System.out.println(eval.toSummaryString());
			System.out.println(eval.toClassDetailsString());
			System.out.println(eval.toMatrixString());
			// for rule display
			// classifier.buildClassifier(instances);
			// System.out.println(classifier);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static Classifier getClassifier() {
		Classifier classifier = new BayesNet();
		try {
			String[] options = weka.core.Utils
					.splitOptions("-D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5");
			classifier.setOptions(options);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return classifier;
	}

	private static List<Datum> wekaToStanford(Instances instances) {
		ArrayList<Datum> data = new ArrayList<Datum>();
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance inst = instances.instance(i);
			ArrayList<String> features = new ArrayList<String>();
			for (int j = 0; j < inst.numAttributes(); j++) {
				if (j != inst.classIndex()) {
					Attribute att = inst.attribute(j);
					double val = inst.value(j);
					String strVal = att.value((int) val);
					if (strVal.isEmpty()) {
						features.add(att.name() + "=" + val);
					} else {
						features.add(att.name() + "=" + strVal);
					}
				}
			}
			String label = inst.classAttribute().value((int) inst.classValue());
			BasicDatum datum = new BasicDatum(features, label);
			System.out.println("datum " + datum);
			data.add(datum);
		}
		return data;
	}

}
