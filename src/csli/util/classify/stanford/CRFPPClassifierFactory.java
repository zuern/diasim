package csli.util.classify.stanford;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import csli.util.ShellUtils;
import edu.stanford.nlp.ling.Datum;

public class CRFPPClassifierFactory extends ExternalClassifierFactory {

	private double cost = 1.0;

	private int history = 0;

	private int future = 0;

	public static final String TRAIN_EXT = ".train";

	public static final String MODEL_EXT = ".model";

	public static final String TEMPLATE_EXT = ".template";

	public static final String TEST_EXT = ".test";

	public static final String RESULT_EXT = ".out";

	public static final String FEATURES_EXT = ".features";

	public static final String INIT_LABEL_PREFIX = "B";

	public static final String CONT_LABEL_PREFIX = "I";

	public static final String END_LABEL_PREFIX = "E";

	public static final String INSERT_LABEL_PREFIX = "N";

	public static final String BACKGROUND_LABEL = "O";

	/**
	 * A factory for producing (training) SVMlight classifiers
	 * 
	 * @param fileStem
	 *            the stem for the input/output data & model files
	 */
	public CRFPPClassifierFactory(String fileStem) {
		super("util.learner.crf++", fileStem);
	}

	/**
	 * @return the cost parameter (CRF++ -c param)
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * @param cost
	 *            the cost parameter(CRF++ -c param), or 0.0 to calculate it automatically. Default is 1.0.
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	public int getHistory() {
		return history;
	}

	public void setHistory(int history) {
		this.history = history;
	}

	public int getFuture() {
		return future;
	}

	public void setFuture(int future) {
		this.future = future;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.ClassifierFactory#trainClassifier(java.util.List)
	 */
	// @Override
	public <D extends Datum> Classifier trainClassifier(List<D> examples) {

		int nNeg = 0;
		int nPos = 0;
		HashMap<String, Integer> featureMap = (isBag() ? new HashMap<String, Integer>() : null);
		File dataFile = new File(getFileStem() + TRAIN_EXT);
		File templateFile = new File(getFileStem() + TEMPLATE_EXT);
		File modelFile = new File(getFileStem() + MODEL_EXT);
		// first initialize featureMap, as we need all columns present from the start
		for (Datum example : examples) {
			ClassifierUtils.getBooleanFeatures(example, featureMap);
		}
		System.out.println("CRF++ found " + featureMap.size() + " features in " + examples.size() + " instances");
		try {
			// if pre-trained, just check for the model file
			if (isPreTrained()) {
				if (!modelFile.exists()) {
					throw new FileNotFoundException(modelFile.getAbsolutePath());
				}
				return new CRFPPClassifier(getClassifyCommand(), getFileStem(), featureMap);
			}

			FileWriter out = new FileWriter(dataFile);
			int e = 0;
			long then = System.currentTimeMillis();
			for (Datum example : examples) {
				out.write(toString(example, featureMap, false));
				System.out.println("Done " + e++ + " " + featureMap.size());
				if (example.label().equals(BACKGROUND_LABEL)) {
					nNeg++;
				} else if (example.label().toString().startsWith(INIT_LABEL_PREFIX)
						|| example.label().toString().startsWith(CONT_LABEL_PREFIX)) {
					nPos++;
				} else {
					System.err.println("WARNING: auto-weighting requires background label " + BACKGROUND_LABEL
							+ " or positive instances beginning " + INIT_LABEL_PREFIX + "/" + CONT_LABEL_PREFIX
							+ " (not " + example.label() + ")");
				}
			}
			System.out.println("Took " + (System.currentTimeMillis() - then));
			out.close();
			// write (dummy) template file (we're already doing its work for it)
			FileWriter template = new FileWriter(templateFile);
			for (String feature : featureMap.keySet()) {
				template.write("U_" + feature + ":%x[0," + (featureMap.get(feature) - 1) + "]\n");
				for (int i = 1; i < history; i++) {
					template.write("U_" + feature + ":%x[-" + i + "," + (featureMap.get(feature) - 1) + "]\n");
				}
				for (int i = 1; i < future; i++) {
					template.write("U_" + feature + ":%x[" + i + "," + (featureMap.get(feature) - 1) + "]\n");
				}
			}
			template.close();
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
			int retval = ShellUtils.execCommand(getTrainCommand() + " -c " + myCost + " "
					+ templateFile.getAbsolutePath() + " " + dataFile.getAbsolutePath() + " "
					+ modelFile.getAbsolutePath());
			if (retval == 0) {
				return new CRFPPClassifier(getClassifyCommand(), getFileStem(), featureMap);
			} else {
				return null;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	/**
	 * Turn a data instance into a datafile line
	 * 
	 * @param datum
	 *            an instance
	 * @param featureMap
	 *            a map of feature label to feature number, or null to assume ordered numeric features
	 * @param test
	 *            if true, do NOT extend the feature map (i.e. ignore features not already seen in training)
	 * @return a line suitable for a CRF++ data file
	 */
	public static String toString(Datum datum, HashMap<String, Integer> featureMap, boolean test) {
		Set<Integer> features = ClassifierUtils.getBooleanFeatures(datum, featureMap, test);
		// CRF++ requires ALL features for each instance
		StringBuffer sb = new StringBuffer(featureMap.size() * 2);
		for (int f = 0; f < featureMap.size(); f++) {
			sb.append((features.contains(f + 1) ? "1" : "0") + " ");
		}
		sb.append((datum.label() == null ? BACKGROUND_LABEL : datum.label().toString()) + "\n");
		return sb.toString();
	}

}
