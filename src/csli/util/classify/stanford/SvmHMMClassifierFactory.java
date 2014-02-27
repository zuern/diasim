/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csli.util.Pair;
import csli.util.ShellUtils;
import edu.stanford.nlp.ling.Datum;

/**
 * A factory for producing (training) {@link SvmHMMClassifier}s
 */
public class SvmHMMClassifierFactory extends SvmLightClassifierFactory {

	public static final String BACKGROUND_LABEL = "O";

	/**
	 * A factory for producing (training) {@link SvmHMMClassifier}s
	 * 
	 * @param fileStem
	 *            the stem for the input/output data & model files
	 */
	public SvmHMMClassifierFactory(String fileStem) {
		super("util.learner.svmhmm", fileStem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.ClassifierFactory#trainClassifier(java.util.List)
	 */
	public <D extends Datum> Classifier trainClassifier(List<D> examples) {

		int nNeg = 0;
		int nPos = 0;
		HashMap<String, Integer> featureMap = (isBag() ? new HashMap<String, Integer>() : null);
		List<Pair<Double, Double>> normFactors = (isNormalize() ? ClassifierUtils.normalizeFeatures(examples,
				featureMap) : null);
		File dataFile = new File(getFileStem() + TRAIN_EXT);
		File modelFile = new File(getFileStem() + MODEL_EXT);
		if (isRegressionModel()) {
			throw new IllegalArgumentException("SVMhmm isn't a regression model ");
		}
		try {
			// if pre-trained, just check for the model file
			if (isPreTrained()) {
				if (!modelFile.exists()) {
					throw new FileNotFoundException(modelFile.getAbsolutePath());
				}
				return new SvmHMMClassifier(getClassifyCommand(), getFileStem(), featureMap, normFactors);
			}

			FileWriter out = new FileWriter(dataFile);
			// TODO advance iSeq per meeting?
			int iSeq = 1;
			int iInd = 1;
			for (Datum example : examples) {
				out.write(toString(example, featureMap, normFactors, iSeq, iInd++));
				if (example.label().equals(BACKGROUND_LABEL)) {
					nNeg++;
				} else {
					nPos++;
				}
			}
			out.close();
			// write feature map if using
			if (featureMap != null) {
				FileWriter features = new FileWriter(new File(getFileStem() + FEATURES_EXT));
				for (String feature : featureMap.keySet()) {
					features.write(feature + " " + featureMap.get(feature) + "\n");
				}
				features.close();
			}
			double myCost = getCost();
			if (myCost == 0.0) {
				myCost = (nPos > 0) ? ((double) nNeg) / ((double) nPos) : 1.0;
			}
			myCost *= getCostFactor();
			int retval = ShellUtils.execCommand(getTrainCommand() + getCLOptions(nNeg, nPos) + " "
					+ dataFile.getAbsolutePath() + " " + modelFile.getAbsolutePath());
			if (retval == 0) {
				return new SvmHMMClassifier(getClassifyCommand(), getFileStem(), featureMap, normFactors);
			} else {
				return null;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.classify.stanford.SvmLightClassifierFactory#getCLOptions(int, int)
	 */
	@Override
	protected String getCLOptions(int nNeg, int nPos) throws InvalidObjectException {
		if (isRegressionModel()) {
			throw new InvalidObjectException("SVMhmm cannot act as a regression model");
		}
		String cl = "";
		if (!Double.isNaN(getTradeOff())) {
			cl += " -c " + getTradeOff();
		} else {
			throw new InvalidObjectException("SVMhmm needs tradeoff (-c) set");
		}
		if (!Double.isNaN(getEps())) {
			cl += " -e " + getEps();
		}
		return cl;
	}

	/**
	 * Turn a data instance into a datafile line
	 * 
	 * @param datum
	 *            an instance
	 * @param featureMap
	 *            a map of feature label to feature number, or null to assume ordered numeric features
	 * @param normFactors
	 *            a list of pairs a, b such that features should be normalized f' = (f-a)*b, or null for no
	 *            normalization
	 * @param seq
	 *            the example sequence number (from 1)
	 * @param index
	 *            the index number within the example sequence (from 1)
	 * @return a line suitable for a SVMlight data file
	 */
	public static String toString(Datum datum, HashMap<String, Integer> featureMap,
			List<Pair<Double, Double>> normFactors, int seq, int index) {
		StringBuffer line = new StringBuffer(datum.label() == null ? BACKGROUND_LABEL : datum.label().toString());
		line.append(" qid:" + seq + "." + index);
		Map<Integer, Double> features = ClassifierUtils.getDoubleFeatures(datum, featureMap);
		List<Integer> indices = new ArrayList<Integer>(features.keySet());
		// SVMlight insists the features are sorted
		Collections.sort(indices);
		for (int f : indices) {
			double val = features.get(f);
			if (normFactors != null) {
				Pair<Double, Double> factors = normFactors.get(f - 1);
				val = (val - factors.a) / factors.b;
			}
			line.append(" " + f + ":" + val);
		}
		return line.toString() + "\n";
	}

}
