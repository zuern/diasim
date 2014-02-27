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

import qmul.corpus.DialogueCorpus;
import qmul.corpus.DialogueSentence;
import qmul.corpus.SCoRECorpus;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Range;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToNominal;
import edu.stanford.nlp.classify.LinearClassifierFactory;
import edu.stanford.nlp.ling.BasicDatum;
import edu.stanford.nlp.ling.Datum;

/**
 * Classifier experiments for Laura Thompson's next-turn-repair-initiator annotations
 * 
 * @author mpurver
 */
public class NTRITester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String corpusName = "ntri.corpus";
		File bncFile = new File(corpusName);
		SCoRECorpus bnc = (bncFile.exists() ? (SCoRECorpus) DialogueCorpus.readFromFile(bncFile) : new SCoRECorpus(
				"NTRI", (new File("/Users").exists() ? "/Users/mpurver/Documents/imc-data/ntri/laura"
						: "/import/imc-data/ntri/laura")));
		AnnotationSet annSet = new AnnotationSet(bnc, new File(new File("/Users").exists() ? "../SCoRE/tasks/NTRI"
				: "/import/imc-corpora/corpus-tools/SCoRE/unstable/tasks/NTRI"));
		annSet.addAll(new File(
				new File("/Users").exists() ? "/Users/mpurver/Documents/dyndial/corpus/annotations/laura"
						: "/import/imc/annotations.score/laura"));
		if (!bncFile.exists()) {
			bnc.writeToFile(bncFile);
		}
		boolean corpusChanged = false;

		FastVector attInfo = new FastVector();
		Attribute labelAtt = FeatureFactory.newYNAttribute("p2ntri");
		attInfo.addElement(labelAtt);
		attInfo.addElement(FeatureFactory.newAttribute(FeatureFactory.PATIENT));
		attInfo.addElement(FeatureFactory.newAttribute(FeatureFactory.WORD_COUNT));
		attInfo.addElement(FeatureFactory.newAttribute(FeatureFactory.MIRROR_WORD_PROPORTION));
		// attInfo.addElement(FeatureFactory.newAttribute(FeatureFactory.FIRST_WORD));
		// attInfo.addElement(FeatureFactory.newAttribute(FeatureFactory.LAST_WORD));
		attInfo.addElement(FeatureFactory.newAttribute(FeatureFactory.CR_KEYWORDS));

		Instances instances = new Instances("p2ntri", attInfo, 0);
		instances.setClass(labelAtt);
		int i = annSet.getTags().indexOf("p2ntri");
		// int i = annSet.getTags().indexOf("end-complete");
		for (String dialogue : annSet.getAnnotations().keySet()) {
			DialogueSentence prevSent = null;
			for (DialogueSentence sent : bnc.getDialogue(dialogue).getSents()) {
				Integer iSent = sent.getNum();
				// remove punctuation
				sent.setTranscription(sent.getTranscription().replaceAll("[.,?!;:\\(\\)\\[\\]]", ""));
				// System.out.println("Dialogue " + dialogue + " " + iSent + " " + annSet.getTagValues(dialogue));
				Instance inst = new Instance(instances.numAttributes());
				inst.setDataset(instances);
				String label = (((annSet.getTagValues(dialogue).get(iSent) == null) || (annSet.getTagValues(dialogue)
						.get(iSent).size() <= i)) ? "" : annSet.getTagValues(dialogue).get(iSent).get(i));
				FeatureFactory.setYNClass(inst, label);
				// System.out.println("set val " + label + " " + dialogue + " " + iSent + " "
				// + annSet.getTagValues(dialogue).get(iSent) + " " + inst.classValue());
				// DialogueSentence sent = bnc.getDialogue(dialogue).getSent(iSent);
				if (sent == null) {
					System.out.println("No sentence found " + iSent);
				} else {
					double preProb = sent.getSyntaxProb();
					for (int a = 0; a < attInfo.size(); a++) {
						if (!inst.attribute(a).equals(labelAtt)) {
							FeatureFactory.setAttribute(inst, inst.attribute(a), sent, prevSent, null);
						}
					}
					if (!Double.isNaN(sent.getSyntaxProb()) && (sent.getSyntaxProb() != preProb)) {
						corpusChanged = true;
					}
				}
				if (inst.classValue() == 0) {
					System.out.println(sent.getTranscription() + " " + prevSent.getTranscription() + " " + inst);
				}
				instances.add(inst);
				prevSent = sent;
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

		if (false) {
			// if (classifier instanceof edu.stanford.nlp.classify.Classifier) {
			List<Datum> data = wekaToStanford(instances);
			edu.stanford.nlp.classify.Classifier classifier = new LinearClassifierFactory().trainClassifier(data);
		}

		try {
			ArffSaver as = new ArffSaver();
			as.setInstances(instances);
			as.setFile(new File("tmp.arff"));
			as.writeBatch();
			StringToNominal f = new StringToNominal();
			f.setAttributeRange("first-last");
			f.setInputFormat(instances);
			instances = Filter.useFilter(instances, f);
			NumericToNominal f2 = new NumericToNominal();
			f2.setAttributeIndices("first-last");
			f2.setInputFormat(instances);
			instances = Filter.useFilter(instances, f2);
			// Classifier classifier = new BayesNet();
			Classifier classifier = new JRip();
			Evaluation eval = new Evaluation(instances);
			StringBuffer predictions = new StringBuffer();
			eval.crossValidateModel(classifier, instances, 10, new Random(1), predictions, new Range("first"), true);
			System.out.println(eval.toSummaryString());
			System.out.println(eval.toClassDetailsString());
			System.out.println(eval.toMatrixString());
			System.out.println(predictions);
			// for rule display
			classifier.buildClassifier(instances);
			System.out.println(classifier);

			// baselines
			classifier = new ZeroR();
			eval = new Evaluation(instances);
			predictions = new StringBuffer();
			eval.crossValidateModel(classifier, instances, 10, new Random(1), predictions, new Range("first"), true);
			System.out.println(eval.toSummaryString());
			System.out.println(eval.toClassDetailsString());
			System.out.println(eval.toMatrixString());
			// System.exit(0);
			classifier = new JRip();
			eval = new Evaluation(instances);
			predictions = new StringBuffer();
			eval.crossValidateModel(classifier, instances, 10, new Random(1), predictions, new Range("first"), true);
			System.out.println(eval.toSummaryString());
			System.out.println(eval.toClassDetailsString());
			System.out.println(eval.toMatrixString());
			// for rule display
			classifier.buildClassifier(instances);
			System.out.println(classifier);
		} catch (Exception e) {
			e.printStackTrace();
		}

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
