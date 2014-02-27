package csli.util.classify.stanford;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csli.util.Pair;
import csli.util.Term;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.util.ScoredObject;

public abstract class ClassifierUtils {

	/**
	 * @param <D>
	 *            a subclass of Datum
	 * @param datum
	 *            the data instance
	 * @param featureMap
	 *            a map of feature label to feature index number, or null to assume ordered numeric features
	 * @return a map from feature index number to feature value
	 */
	public static <D extends Datum> Map<Integer, Double> getDoubleFeatures(D datum, Map<String, Integer> featureMap) {
		return getDoubleFeatures(datum, featureMap, false);
	}

	/**
	 * @param <D>
	 *            a subclass of Datum
	 * @param datum
	 *            the data instance
	 * @param featureMap
	 *            a map of feature label to feature index number, or null to assume ordered numeric features
	 * @param test
	 *            if true, do NOT extend the feature map (i.e. ignore features not already seen in training)
	 * @return a map from feature index number to feature value
	 */
	public static <D extends Datum> Map<Integer, Double> getDoubleFeatures(D datum, Map<String, Integer> featureMap,
			boolean test) {
		HashMap<Integer, Double> counts = new HashMap<Integer, Double>();
		if (featureMap == null) {
			// assume every feature specified in order and double-like
			int f = 1;
			for (Object feature : datum.asFeatures()) {
				counts.put(f++, new Double(feature.toString()));
			}
		} else {
			// assume features are a bag of values which we must map & count
			for (Object feature : datum.asFeatures()) {
				Pair<String, Double> vf = getValuedFeature(feature);
				if (!featureMap.containsKey(vf.a) && !test) {
					featureMap.put(vf.a, featureMap.keySet().size() + 1);
				}
				Integer f = featureMap.get(vf.a);
				if (f != null) {
					if (!counts.containsKey(f)) {
						counts.put(f, 0.0);
					}
					counts.put(f, counts.get(f) + vf.b);
				}
			}
		}
		return counts;
	}

	/**
	 * @param <D>
	 *            a subclass of Datum
	 * @param datum
	 *            the data instance
	 * @param featureMap
	 *            a map of feature label to feature index number, or null to assume ordered numeric features
	 * @return a set of feature index numbers for which the feature is true (non-zero)
	 */
	public static <D extends Datum> Set<Integer> getBooleanFeatures(D datum, Map<String, Integer> featureMap) {
		return getBooleanFeatures(datum, featureMap, false);
	}

	/**
	 * @param <D>
	 *            a subclass of Datum
	 * @param datum
	 *            the data instance
	 * @param featureMap
	 *            a map of feature label to feature index number, or null to assume ordered numeric features
	 * @param test
	 *            if true, do NOT extend the feature map (i.e. ignore features not already seen in training)
	 * @return a set of feature index numbers for which the feature is true (non-zero)
	 */
	public static <D extends Datum> Set<Integer> getBooleanFeatures(D datum, Map<String, Integer> featureMap,
			boolean test) {
		HashSet<Integer> counts = new HashSet<Integer>();
		if (featureMap == null) {
			// assume every feature specified in order and double-like
			int f = 1;
			for (Object feature : datum.asFeatures()) {
				if (new Double(feature.toString()) != 0.0) {
					counts.add(f);
				}
				f++;
			}
		} else {
			// assume features are a bag of values which we must map & check
			for (Object feature : datum.asFeatures()) {
				Pair<String, Double> vf = getValuedFeature(feature);
				if (!featureMap.containsKey(vf.a) && !test) {
					featureMap.put(vf.a, featureMap.keySet().size() + 1);
				}
				Integer f = featureMap.get(vf.a);
				if ((vf.b != 0.0) && (f != null)) {
					counts.add(f);
				}
			}
		}
		return counts;
	}

	/**
	 * @param <D>
	 *            a subclass of Datum
	 * @param datum
	 *            the data instance
	 * @param featureMap
	 *            a map of feature label to feature index number, or null to assume ordered numeric features
	 * @return a list of 0 or 1 for each feature
	 */
	public static <D extends Datum> List<Integer> getBooleanFeatureList(D datum, Map<String, Integer> featureMap) {
		ArrayList<Integer> counts = new ArrayList<Integer>(featureMap == null ? datum.asFeatures().size() : featureMap
				.size());
		if (featureMap == null) {
			// assume every feature specified in order and double-like
			for (Object feature : datum.asFeatures()) {
				counts.add(new Double(feature.toString()) != 0.0 ? 1 : 0);
			}
		} else {
			// assume features are a bag of values which we must map & check
			for (int i = 0; i < featureMap.size(); i++) {
				counts.add(0);
			}
			for (Object feature : datum.asFeatures()) {
				Pair<String, Double> vf = getValuedFeature(feature);
				if (!featureMap.containsKey(vf.a)) {
					featureMap.put(vf.a, featureMap.keySet().size() + 1);
				}
				int f = featureMap.get(vf.a);
				if (vf.b != 0.0) {
					counts.set(f - 1, 1);
				}
			}
		}
		return counts;
	}

	/**
	 * @param feature
	 *            a feature, either as a ScoredObject or just a plain Object
	 * @return a Pair of the String feature and its Double value (the score of a ScoredObject, or 1.0 otherwise)
	 */
	public static Pair<String, Double> getValuedFeature(Object feature) {
		String label;
		Double value;
		if (feature instanceof ScoredObject) {
			label = ((ScoredObject<?>) feature).object().toString();
			value = ((ScoredObject<?>) feature).score();
		} else {
			label = feature.toString();
			value = 1.0;
		}
		return new Pair<String, Double>(label, value);
	}

	/**
	 * @param <D>
	 *            a subclass of Datum
	 * @param examples
	 *            a List of data instances to be normalized
	 * @param featureMap
	 *            a map of feature label to feature number, or null to assume ordered numeric features
	 * @return a List of Pairs of Doubles which record the normalization: f_norm = (f_raw-a)*b
	 */
	public static <D extends Datum> List<Pair<Double, Double>> normalizeFeatures(List<D> examples,
			Map<String, Integer> featureMap) {
		List<Double> mins = new ArrayList<Double>();
		List<Double> maxs = new ArrayList<Double>();
		for (D example : examples) {
			Map<Integer, Double> features = getDoubleFeatures(example, featureMap);
			for (int f : features.keySet()) {
				if (mins.size() < f) {
					for (int i = mins.size(); i < f; i++) {
						mins.add(Double.MAX_VALUE);
						maxs.add(Double.MIN_VALUE);
					}
				}
				mins.set(f - 1, Math.min(mins.get(f - 1), features.get(f)));
				maxs.set(f - 1, Math.max(maxs.get(f - 1), features.get(f)));
			}
		}
		List<Pair<Double, Double>> factors = new ArrayList<Pair<Double, Double>>();
		for (int f = 0; f < mins.size(); f++) {
			factors.add(new Pair<Double, Double>(mins.get(f), maxs.get(f) - mins.get(f)));
		}
		return factors;
	}

	/**
	 * @param <D>
	 * @param examples
	 *            a list of Datum instances from which to prune features
	 * @param specs
	 *            a list of String pruning specifications
	 * @param posLabel
	 * @param negLabel
	 * @return the set of feature labels removed
	 */
	public static <D extends Datum> Set<String> pruneFeatures(List<D> examples, List<String> specs, String posLabel,
			String negLabel) {
		Set<String> pruned = new HashSet<String>();
		for (String specStr : specs) {
			Term spec = new Term(specStr.replaceAll(";", ","));
			if (spec.getFunctor().equalsIgnoreCase("freq")) {
				pruned.addAll(ClassifierUtils.pruneFeaturesByFreq(examples, Double.parseDouble(spec.getTerm(0)
						.getFunctor())));
			} else if (spec.getFunctor().equalsIgnoreCase("infogain")) {
				pruned.addAll(ClassifierUtils.pruneFeaturesByInfoGain(examples, Double.parseDouble(spec.getTerm(0)
						.getFunctor()), posLabel, negLabel));
			}
		}
		return pruned;
	}

	/**
	 * @param <D>
	 * @param examples
	 *            a list of Datum instances from which to prune features
	 * @param threshold
	 *            the value of frequency below which features will be pruned
	 * @return the set of feature labels removed
	 */
	public static <D extends Datum> Set<String> pruneFeaturesByFreq(List<D> examples, double threshold) {
		HashMap<String, Double> counts = new HashMap<String, Double>();
		for (D example : examples) {
			for (Object feature : example.asFeatures()) {
				Pair<String, Double> vf = getValuedFeature(feature);
				if (counts.get(vf.a) == null) {
					counts.put(vf.a, 0.0);
				}
				counts.put(vf.a, counts.get(vf.a) + vf.b);
			}
		}
		Set<String> toPrune = new HashSet<String>();
		for (String f : counts.keySet()) {
			if (counts.get(f) < threshold) {
				toPrune.add(f);
			}
		}
		System.out.println("Pruned " + counts.keySet().size() + " by frequency " + threshold + ", removed "
				+ toPrune.size() + " -> " + (counts.keySet().size() - toPrune.size()));
		prune(examples, toPrune);
		return toPrune;
	}

	/**
	 * @param <D>
	 * @param examples
	 *            a list of Datum instances from which to prune features
	 * @param posLabel
	 * @param negLabel
	 * @return the set of feature labels removed
	 */
	public static <D extends Datum> Set<String> pruneFeaturesByCorrelation(List<D> examples, double margin,
			String posLabel, String negLabel) {
		HashMap<String, Double> posCounts = new HashMap<String, Double>();
		HashMap<String, Double> negCounts = new HashMap<String, Double>();
		double nPos = 0;
		double nNeg = 0;
		for (D example : examples) {
			HashMap<String, Double> counts;
			if ((example.label() == null) || example.label().equals(negLabel)) {
				nNeg++;
				counts = negCounts;
			} else if (example.label().equals(posLabel)) {
				nPos++;
				counts = posCounts;
			} else {
				System.err.println("WARNING: ignoring unknown label " + example.label());
				continue;
			}
			for (Object feature : example.asFeatures()) {
				Pair<String, Double> vf = getValuedFeature(feature);
				if (counts.get(vf.a) == null) {
					negCounts.put(vf.a, 0.0);
					posCounts.put(vf.a, 0.0);
				}
				counts.put(vf.a, counts.get(vf.a) + vf.b);
			}
		}
		Set<String> toKeep = new HashSet<String>();
		double baseRatio = nPos / nNeg;
		for (String f : posCounts.keySet()) {
			double myRatio = posCounts.get(f) / negCounts.get(f);
			if (((myRatio / baseRatio) > (1 + margin)) || ((myRatio / baseRatio) < (1 - margin))) {
				toKeep.add(f);
			}
		}
		posCounts.clear();
		negCounts.clear();
		return toKeep;
	}

	/**
	 * @param <D>
	 * @param examples
	 *            a list of Datum instances from which to prune features
	 * @param posLabel
	 * @param negLabel
	 * @return the set of feature labels removed
	 */
	public static <D extends Datum> Set<String> pruneFeaturesByInfoGain(List<D> examples, double margin,
			String posLabel, String negLabel) {
		final String NOT = "INFOGAIN_NOT_";
		HashMap<String, Double> posCounts = new HashMap<String, Double>();
		HashMap<String, Double> negCounts = new HashMap<String, Double>();
		HashSet<String> features = new HashSet<String>();
		// first initialize all feature labels, as we'll need to track non-occurrences of features too
		for (D example : examples) {
			for (Object feature : example.asFeatures()) {
				Pair<String, Double> vf = getValuedFeature(feature);
				features.add(vf.a);
				posCounts.put(vf.a, 0.0);
				negCounts.put(vf.a, 0.0);
				posCounts.put(NOT + vf.a, 0.0);
				negCounts.put(NOT + vf.a, 0.0);
			}
		}
		System.out.println("Found " + features.size() + " features ...");
		// now count occurrences of f/not-f in positive and negative instances
		double nPos = 0;
		double nNeg = 0;
		System.out.println("Counting labels over " + examples.size() + " instances ...");
		for (D example : examples) {
			HashMap<String, Double> counts;
			if ((example.label() == null) || example.label().equals(negLabel)) {
				nNeg++;
				counts = negCounts;
			} else if (example.label().equals(posLabel)) {
				nPos++;
				counts = posCounts;
			} else {
				System.err.println("WARNING: ignoring unknown label " + example.label());
				continue;
			}
			HashSet<String> seen = new HashSet<String>();
			for (Object feature : example.asFeatures()) {
				Pair<String, Double> vf = getValuedFeature(feature);
				counts.put(vf.a, counts.get(vf.a) + vf.b);
				seen.add(vf.a);
			}
			// we're not treating things quite correctly here - for real-valued features, count(not-f) should be
			// 1-count(f). But of course, that requires that features are normalized, which they may not be ...
			for (String f : features) {
				if (!seen.contains(f)) {
					counts.put(NOT + f, counts.get(NOT + f) + 1.0);
				}
			}
			System.out.print(".");
		}
		System.out.println();
		double pPos = nPos / (nPos + nNeg);
		double pNeg = nNeg / (nPos + nNeg);
		double baseEntropy = -(pLogP(pPos) + pLogP(pNeg));
		Set<String> toPrune = new HashSet<String>();
		for (String f : features) {
			double pF = (posCounts.get(f) + negCounts.get(f)) / (nPos + nNeg);
			double pPosGivenF = posCounts.get(f) / (posCounts.get(f) + negCounts.get(f));
			double pNegGivenF = negCounts.get(f) / (posCounts.get(f) + negCounts.get(f));
			double entropyGivenF = -(pLogP(pPosGivenF) + pLogP(pNegGivenF));
			double pPosGivenNotF = posCounts.get(NOT + f) / (posCounts.get(NOT + f) + negCounts.get(NOT + f));
			double pNegGivenNotF = negCounts.get(NOT + f) / (posCounts.get(NOT + f) + negCounts.get(NOT + f));
			double entropyGivenNotF = -(pLogP(pPosGivenNotF) + pLogP(pNegGivenNotF));
			double newEntropy = (pF * entropyGivenF) + ((1 - pF) * entropyGivenNotF);
			double infoGain = (baseEntropy - newEntropy) / baseEntropy;
			if (infoGain < margin) {
				toPrune.add(f);
				// } else {
				// System.out.println("keeping useful feature " + f + " IG " + infoGain);
			}
		}
		System.out.println("Pruned " + features.size() + " by information gain " + margin + ", removed "
				+ toPrune.size() + " -> " + (features.size() - toPrune.size()));
		prune(examples, toPrune);
		return toPrune;
	}

	/**
	 * @param <D>
	 * @param examples
	 *            a list of Datum instances from which to prune features
	 * @param toPrune
	 *            a set of feature labels to remove
	 */
	public static <D extends Datum> void prune(List<D> examples, Set<String> toPrune) {
		for (D example : examples) {
			for (Object feature : new ArrayList<Object>((Collection<?>) example.asFeatures())) {
				Pair<String, Double> vf = getValuedFeature(feature);
				if (toPrune.contains(vf.a)) {
					example.asFeatures().remove(feature);
				}
			}
		}
	}

	/**
	 * Convenience method to get round the fact that multiplying zero by -Inf gives NaN
	 * 
	 * @param p
	 *            a double probability
	 * @return p*Math.log(p), which will be 0 if p=0
	 */
	public static double pLogP(double p) {
		return (p == 0.0 ? 0.0 : p * Math.log(p));
	}

}
