/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import csli.util.Pair;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;

/**
 * An extension for the standard Stanford Classifier class for use with external stand-alone classifiers. Includes
 * result cacheing, feature normalization, and mapping from feature objects to numerical indices (for external
 * classifier packages which like that sort of thing)
 * 
 * @author mpurver
 */
public abstract class ExternalClassifier extends Classifier {

	private String classifyCommand;

	private String fileStem;

	// will increase memory usage, but saves on file I/O
	private boolean resultCacheing = true;

	private HashMap<String, Integer> featureMap;

	private List<Pair<Double, Double>> normFactors;

	private HashMap<Datum, ClassicCounter<?>> scoreResults = new HashMap<Datum, ClassicCounter<?>>();

	private HashMap<Datum, Object> classResults = new HashMap<Datum, Object>();

	public ExternalClassifier(String classifyCommand, String fileStem) {
		this.classifyCommand = classifyCommand;
		this.fileStem = fileStem;
	}

	public ExternalClassifier(String classifyCommand, String fileStem, HashMap<String, Integer> featureMap,
			List<Pair<Double, Double>> normFactors) {
		this(classifyCommand, fileStem);
		this.featureMap = featureMap;
		this.normFactors = normFactors;
	}

	/**
	 * @return the classifyCommand
	 */
	public String getClassifyCommand() {
		return classifyCommand;
	}

	/**
	 * @param classifyCommand
	 *            the classifyCommand to set
	 */
	public void setClassifyCommand(String classifyCommand) {
		this.classifyCommand = classifyCommand;
	}

	/**
	 * @return the fileStem
	 */
	public String getFileStem() {
		return fileStem;
	}

	/**
	 * @param fileStem
	 *            the fileStem to set
	 */
	public void setFileStem(String fileStem) {
		this.fileStem = fileStem;
	}

	/**
	 * @return true if this classifier caches its scoresOf results
	 */
	public boolean isResultCacheing() {
		return resultCacheing;
	}

	/**
	 * @param resultCacheing
	 *            true if this classifier should cache its scoresOf results, false otherwise
	 */
	public void setResultCacheing(boolean resultCacheing) {
		this.resultCacheing = resultCacheing;
	}

	public HashMap<String, Integer> getFeatureMap() {
		return featureMap;
	}

	/**
	 * @return the normFactors
	 */
	public List<Pair<Double, Double>> getNormFactors() {
		return normFactors;
	}

	/**
	 * @return the cached class results
	 */
	public HashMap<Datum, Object> getClassResults() {
		return classResults;
	}

	/**
	 * Set the cached class result for datum to cls
	 * 
	 * @param datum
	 * @param cls
	 */
	public void setClassResult(Datum datum, Object cls) {
		classResults.put(datum, cls);
	}

	/**
	 * @return the cached score results
	 */
	public HashMap<Datum, ClassicCounter<?>> getScoreResults() {
		return scoreResults;
	}

	/**
	 * Set the cached score result for datum to cnt
	 * 
	 * @param datum
	 * @param cnt
	 */
	public void setScoreResult(Datum datum, ClassicCounter<?> cnt) {
		scoreResults.put(datum, cnt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#classOf(edu.stanford.nlp.dbm.Datum)
	 */
	@Override
	// check cache first
	public Object classOf(Datum example) {
		// first check if we've already saved this one
		if (classResults.containsKey(example)) {
			return classResults.get(example);
		}

		// otherwise use the array method
		Datum[] examples = { example };
		String cls = (String) classOf(examples)[0];

		// store these so that we don't have to do too much file I/O
		if (isResultCacheing()) {
			classResults.put(example, cls);
		}
		return cls;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#classOf(java.util.List)
	 */
	@Override
	// use the array method, not the default Datum method
	public <D extends Datum> List<Object> classOf(List<D> examples) {
		ArrayList<Object> classes = new ArrayList<Object>(examples.size());
		Object[] clss = classOf(examples.toArray(new Datum[examples.size()]));
		for (int i = 0; i < clss.length; i++) {
			classes.add(i, clss[i]);
		}
		return classes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#scoresOf(edu.stanford.nlp.dbm.Datum)
	 */
	@Override
	// check cache first
	public ClassicCounter<?> scoresOf(Datum example) {
		// first check if we've already saved this one
		if (scoreResults.containsKey(example)) {
			return scoreResults.get(example);
		}

		// otherwise use the array method
		Datum[] examples = { example };
		ClassicCounter<?> counter = scoresOf(examples)[0];

		// store these so that we don't have to do too much file I/O
		if (isResultCacheing()) {
			scoreResults.put(example, counter);
		}
		return counter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#scoresOf(java.util.List)
	 */
	@Override
	// use the array method, not the default Datum method
	public <D extends Datum> List<ClassicCounter<?>> scoresOf(List<D> examples) {
		ArrayList<ClassicCounter<?>> counters = new ArrayList<ClassicCounter<?>>(examples.size());
		ClassicCounter<?>[] cnts = scoresOf(examples.toArray(new Datum[examples.size()]));
		for (int i = 0; i < cnts.length; i++) {
			counters.add(i, cnts[i]);
		}
		return counters;
	}

	/**
	 * In {@link HierarchicalDiscourseClassifier.train()}, classOf() and scoreOf() classify the same data set. the
	 * method is to eliminate classifying the same data twice by Jia Huang Aug 20,2008
	 * 
	 * @param cnts
	 * @return
	 */
	public List<ClassicCounter<?>> scoresOf(ClassicCounter<?>[] cnts) {
		ArrayList<ClassicCounter<?>> counters = new ArrayList<ClassicCounter<?>>(cnts.length);
		for (int i = 0; i < cnts.length; i++) {
			counters.add(i, cnts[i]);
		}
		return counters;
	}

}
