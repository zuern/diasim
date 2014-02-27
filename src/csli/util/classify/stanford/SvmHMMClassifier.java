/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

import csli.util.Pair;
import csli.util.ShellUtils;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counters;

/**
 * A wrapper for Thorsten Joachims' SVMhmm support vector machine-based sequence classifier (see
 * http://svmlight.joachims.org/)
 * 
 * @author mpurver
 */
public class SvmHMMClassifier extends ExternalClassifier {

	/**
	 * @param classifyCommand
	 * @param fileStem
	 * @param featureMap
	 * @param normFactors
	 */
	public SvmHMMClassifier(String classifyCommand, String fileStem, HashMap<String, Integer> featureMap,
			List<Pair<Double, Double>> normFactors) {
		super(classifyCommand, fileStem, featureMap, normFactors);
		// classification is fast, and it makes little sense to cache results for individual out-of-sequence data
		setResultCacheing(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#classOf(edu.stanford.nlp.dbm.Datum[])
	 */
	@Override
	public Object[] classOf(Datum[] examples) {
		Object[] classes = new Object[examples.length];
		ClassicCounter<?>[] cs = scoresOf(examples);
		for (int i = 0; i < cs.length; i++) {
			classes[i] = Counters.argmax(cs[i]);
		}
		return classes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#scoresOf(edu.stanford.nlp.dbm.Datum[])
	 */
	@Override
	public ClassicCounter<?>[] scoresOf(Datum[] examples) {

		File dataFile = new File(getFileStem() + SvmHMMClassifierFactory.TEST_EXT);
		File modelFile = new File(getFileStem() + SvmHMMClassifierFactory.MODEL_EXT);
		File resultFile = new File(getFileStem() + SvmHMMClassifierFactory.RESULT_EXT);
		try {
			FileWriter out = new FileWriter(dataFile);
			int iSeq = 1;
			int iIndex = 1;
			for (int i = 0; i < examples.length; i++) {
				out.write(SvmHMMClassifierFactory.toString(examples[i], getFeatureMap(), getNormFactors(), iSeq,
						iIndex++));
			}
			out.close();
			int retval = ShellUtils.execCommand(getClassifyCommand() + " " + dataFile.getAbsolutePath() + " "
					+ modelFile.getAbsolutePath() + " " + resultFile.getAbsolutePath());
			if (retval != 0) {
				return null;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}

		char[] cbuf = new char[(int) resultFile.length()];
		try {
			FileReader in = new FileReader(resultFile);
			in.read(cbuf);
			in.close();
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}

		String sbuf = String.valueOf(cbuf);
		sbuf = sbuf.replaceAll("\\s*(\\{|\\})\\s*", "");
		String[] results = sbuf.split("\\s+");
		if (!(results.length == examples.length)) {
			throw new RuntimeException("SVMhmm results length " + results.length + " does not match input length "
					+ examples.length);
		}
		ClassicCounter<?>[] counters = new ClassicCounter<?>[results.length];
		for (int i = 0; i < results.length; i++) {
			ClassicCounter<String> counter = new ClassicCounter<String>();
			String tag = results[i].trim();
			counter.setCount(tag, 1.0);
			if (tag.equals(SvmHMMClassifierFactory.POS_LABEL)) {
				counter.setCount(SvmHMMClassifierFactory.NEG_LABEL, 0.0);
			} else if (tag.equals(SvmHMMClassifierFactory.NEG_LABEL)) {
				counter.setCount(SvmHMMClassifierFactory.POS_LABEL, 0.0);
			}
			if (!tag.equals(SvmHMMClassifierFactory.BACKGROUND_LABEL)) {
				counter.setCount(SvmHMMClassifierFactory.BACKGROUND_LABEL, 0.0);
			}
			counters[i] = counter;
		}
		return counters;

	}

}
