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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import csli.util.Pair;
import csli.util.ShellUtils;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;

/**
 * A wrapper for Thorsten Joachims' SVMlight support vector machine-based classifier (see http://svmlight.joachims.org/)
 * 
 * @author mpurver
 */
public class SvmLightClassifier extends ExternalClassifier {

	private double threshold;

	/**
	 * @param classifyCommand
	 * @param fileStem
	 * @param threshold
	 * @param featureMap
	 * @param normFactors
	 */
	public SvmLightClassifier(String classifyCommand, String fileStem, double threshold,
			HashMap<String, Integer> featureMap, List<Pair<Double, Double>> normFactors) {
		super(classifyCommand, fileStem, featureMap, normFactors);
		this.threshold = threshold;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#classOf(edu.stanford.nlp.dbm.Datum)
	 */
	@Override
	public Object classOf(Datum example) {
		double val = scoresOf(example).getCount(SvmLightClassifierFactory.POS_LABEL);
		String cls = (val >= threshold) ? SvmLightClassifierFactory.POS_LABEL : SvmLightClassifierFactory.NEG_LABEL;
		return cls;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#classOf(java.util.List)
	 */
	@Override
	public <D extends Datum> List<Object> classOf(List<D> examples) {
		ArrayList<Object> classes = new ArrayList<Object>(examples.size());
		ClassicCounter<?>[] cnts = scoresOf(examples.toArray(new Datum[examples.size()]));
		for (int i = 0; i < cnts.length; i++) {
			double val = cnts[i].getCount(SvmLightClassifierFactory.POS_LABEL);
			String cls = (val >= threshold) ? SvmLightClassifierFactory.POS_LABEL : SvmLightClassifierFactory.NEG_LABEL;
			classes.add(i, cls);
		}
		return classes;
	}

	/**
	 * 
	 * @param cnts
	 * @return
	 */
	public List<Object> classOf(ClassicCounter<?>[] cnts) {
		ArrayList<Object> classes = new ArrayList<Object>(cnts.length);
		for (int i = 0; i < cnts.length; i++) {
			double val = cnts[i].getCount(SvmLightClassifierFactory.POS_LABEL);
			String cls = (val >= threshold) ? SvmLightClassifierFactory.POS_LABEL : SvmLightClassifierFactory.NEG_LABEL;
			classes.add(i, cls);
		}
		return classes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#classOf(edu.stanford.nlp.dbm.Datum[])
	 */
	@Override
	public Object[] classOf(Datum[] examples) {
		Object[] classes = new Object[examples.length];
		ClassicCounter<?>[] cnts = scoresOf(examples);
		for (int i = 0; i < cnts.length; i++) {
			double val = cnts[i].getCount(SvmLightClassifierFactory.POS_LABEL);
			String cls = (val >= threshold) ? SvmLightClassifierFactory.POS_LABEL : SvmLightClassifierFactory.NEG_LABEL;
			classes[i] = cls;
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

		File dataFile = new File(getFileStem() + SvmLightClassifierFactory.TEST_EXT);
		File modelFile = new File(getFileStem() + SvmLightClassifierFactory.MODEL_EXT);
		File resultFile = new File(getFileStem() + SvmLightClassifierFactory.RESULT_EXT);
		try {
			FileWriter out = new FileWriter(dataFile);
			for (int i = 0; i < examples.length; i++) {
				out.write(SvmLightClassifierFactory.toString(examples[i], getFeatureMap(), getNormFactors()));
			}
			out.close();
			int retval = 1;
			int numberTry = 3;
			while (retval != 0 && numberTry > 0) {
				retval = ShellUtils.execCommand(getClassifyCommand() + " " + dataFile.getAbsolutePath() + " "
						+ modelFile.getAbsolutePath() + " " + resultFile.getAbsolutePath());
				if (retval != 0) {
					System.out.println("external process terminated, try again\n");
					numberTry--;
				}
			}
			if (retval != 0) {
				return null;
			}
		} catch (Throwable t) {
			System.out.println("Failed to run external command");
			t.printStackTrace();
			return null;
		}

		char[] cbuf = new char[(int) resultFile.length()];
		try {
			FileReader in = new FileReader(resultFile);
			in.read(cbuf);
			in.close();
		} catch (Throwable t) {
			System.out.println("Unable to read result file");
			t.printStackTrace();
			return null;
		}

		String sbuf = String.valueOf(cbuf);
		String[] stringValues = sbuf.split("\n");
		if (!(stringValues.length == examples.length)) {
			throw new RuntimeException("SVM results length " + stringValues.length + " does not match input length "
					+ examples.length);
		}
		ClassicCounter<?>[] counters = new ClassicCounter<?>[stringValues.length];
		for (int i = 0; i < stringValues.length; i++) {
			double val = Double.parseDouble(stringValues[i]);
			ClassicCounter<String> counter = new ClassicCounter<String>();
			counter.setCount(SvmLightClassifierFactory.POS_LABEL, val);
			counter.setCount(SvmLightClassifierFactory.NEG_LABEL, -val);
			counters[i] = counter;
		}
		return counters;
	}

	/**
	 * added by JH on Aug 21,2008 It take List of Datum as parameter
	 * 
	 * @param examples
	 * @return
	 */
	public ClassicCounter<?>[] scoresOnly(List<Datum> examples) {
		Datum[] array = new Datum[examples.size()];
		for (int i = 0; i < examples.size(); i++) {
			array[i] = examples.get(i);
		}
		return scoresOf(array);
	}

}
