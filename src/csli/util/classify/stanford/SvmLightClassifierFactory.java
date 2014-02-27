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

import csli.util.Config;
import csli.util.Pair;
import csli.util.ShellUtils;
import edu.stanford.nlp.ling.Datum;

/**
 * A factory for producing (training) {@link SvmLightClassifier}s
 */
public class SvmLightClassifierFactory extends ExternalClassifierFactory {

	private double cost = 0.0;

	private double costFactor = 1.0;

	private double threshold = 0.0;

	private double tradeOff = Double.NaN;

	private double eps = Double.NaN;

	private int kernel = -1;

	private int dParam = -1;

	private double gammaParam = Double.NaN;

	private double sParam = Double.NaN;

	private double cParam = Double.NaN;

	private boolean regressionModel = false;

	private boolean transductive = false;

	public static final String TRAIN_EXT = ".train";

	public static final String MODEL_EXT = ".model";

	public static final String TEST_EXT = ".test";

	public static final String RESULT_EXT = ".out";

	public static final String FEATURES_EXT = ".features";

	public static final String POS_LABEL = "+1";

	public static final String NEG_LABEL = "-1";

	public static final String UNKNOWN_LABEL = "0";

	/**
	 * A factory for producing (training) {@link SvmLightClassifier}s
	 * 
	 * @param fileStem
	 *            the stem for the input/output data & model files
	 */
	public SvmLightClassifierFactory(String fileStem) {
		super("util.learner.svm", fileStem);
	}

	/**
	 * A factory for producing (training) {@link SvmLightClassifier}s
	 * 
	 * @param configKey
	 *            the {@link Config} key pointing at the external program
	 * @param fileStem
	 *            the stem for the input/output data & model files
	 */
	protected SvmLightClassifierFactory(String configKey, String fileStem) {
		super(configKey, fileStem);
	}

	/**
	 * @return true if this classifier provides a regression model
	 */
	public boolean isRegressionModel() {
		return regressionModel;
	}

	/**
	 * @param regressionModel
	 *            true if this classifier will provide a regression model, false otherwise
	 */
	public void setRegressionModel(boolean regressionModel) {
		this.regressionModel = regressionModel;
	}

	/**
	 * @return the absolute cost (SVMlight -j param), or 0.0 to calculate it automatically (default)
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * @param cost
	 *            absolute cost (SVMlight -j param), or 0.0 to calculate it automatically (default)
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * @return the classification threshold (default 0.0)
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold
	 *            the classification threshold (default 0.0)
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * @return the factor by which to multiply the automatically calculated cost - higher numbers weight positive
	 *         examples more (default 1.0)
	 */
	public double getCostFactor() {
		return costFactor;
	}

	/**
	 * @param costFactor
	 *            the factor by which to multiply the automatically calculated cost - higher numbers weight positive
	 *            examples more (default 1.0)
	 */
	public void setCostFactor(double costFactor) {
		this.costFactor = costFactor;
	}

	/**
	 * @return the error/margin tradeoff
	 */
	public double getTradeOff() {
		return tradeOff;
	}

	/**
	 * @param tradeOff
	 *            the error/margin tradeoff
	 */
	public void setTradeOff(double tradeOff) {
		this.tradeOff = tradeOff;
	}

	/**
	 * @return the termination error criterion
	 */
	public double getEps() {
		return eps;
	}

	/**
	 * @param eps
	 *            the termination error criterion
	 */
	public void setEps(double eps) {
		this.eps = eps;
	}

	/**
	 * @return the kernel type: 0=linear, 1=polynomial, 2=rbf, 3=sigmoid tanh, 4=userdef
	 */
	public int getKernel() {
		return kernel;
	}

	/**
	 * @param kernel
	 *            the kernel type: 0=linear, 1=polynomial, 2=rbf, 3=sigmoid tanh, 4=userdef
	 */
	public void setKernel(int kernel) {
		this.kernel = kernel;
	}

	/**
	 * @return the polynomial kernel d parameter
	 */
	public int getDParam() {
		return dParam;
	}

	/**
	 * @param d
	 *            the polynomial kernel d parameter
	 */
	public void setDParam(int d) {
		dParam = d;
	}

	/**
	 * @return the rbf kernel gamma parameter
	 */
	public double getGammaParam() {
		return gammaParam;
	}

	/**
	 * @param gamma
	 *            the rbf kernel gamma parameter
	 */
	public void setGammaParam(double gamma) {
		this.gammaParam = gamma;
	}

	/**
	 * @return the sigmoid/polynomial kernel s parameter
	 */
	public double getSParam() {
		return sParam;
	}

	/**
	 * @param s
	 *            the sigmoid/polynomial kernel s parameter
	 */
	public void setSParam(double s) {
		sParam = s;
	}

	/**
	 * @return the sigmoid/polynomial kernel c parameter
	 */
	public double getCParam() {
		return cParam;
	}

	/**
	 * @param c
	 *            the sigmoid/polynomial kernel c parameter
	 */
	public void setCParam(double c) {
		cParam = c;
	}

	/**
	 * @return true if this classifier is intended to be transductive. This doesn't do anything by itself - you will
	 *         need to supply training data supplemented with the test data marked with UNKNOWN_LABEL
	 */
	public boolean isTransductive() {
		return transductive;
	}

	/**
	 * @param transductive
	 *            true if this classifier is intended to be transductive. This doesn't do anything by itself - you will
	 *            need to supply training data supplemented with the test data marked with UNKNOWN_LABEL
	 */
	public void setTransductive(boolean transductive) {
		this.transductive = transductive;
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
		try {
			// if pre-trained, just check for the model file
			if (isPreTrained()) {
				if (!modelFile.exists()) {
					throw new FileNotFoundException(modelFile.getAbsolutePath());
				}
				return new SvmLightClassifier(getClassifyCommand(), getFileStem(), getThreshold(), featureMap,
						normFactors);
			}

			FileWriter out = new FileWriter(dataFile);
			for (Datum example : examples) {
				out.write(toString(example, featureMap, normFactors));
				if (isRegressionModel()) {
					if (!(example.label() instanceof Number)) {
						throw new InvalidObjectException("SVMlight regression requires numerical label (not "
								+ example.label() + ")");
					}
				} else {
					if (example.label().equals(POS_LABEL)) {
						nPos++;
					} else if (example.label().equals(NEG_LABEL)) {
						nNeg++;
					} else if (!example.label().equals(UNKNOWN_LABEL)) {
						throw new InvalidObjectException("SVMlight classification requires label " + POS_LABEL + ", "
								+ NEG_LABEL + " or " + UNKNOWN_LABEL + " (not " + example.label() + ")");
					}
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
			int retval = ShellUtils.execCommand(getTrainCommand() + getCLOptions(nNeg, nPos) + " "
					+ dataFile.getAbsolutePath() + " " + modelFile.getAbsolutePath());
			if (retval == 0) {
				return new SvmLightClassifier(getClassifyCommand(), getFileStem(), getThreshold(), featureMap,
						normFactors);
			} else {
				return null;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	/**
	 * @param nNeg
	 *            number of negative instances in training data
	 * @param nPos
	 *            number of positive instances in training data
	 * @return the command-line option string for the svm_learn variant
	 */
	protected String getCLOptions(int nNeg, int nPos) throws InvalidObjectException {
		String cl = "";
		String myType = isRegressionModel() ? "r" : "c";
		cl += " -z " + myType;
		double myCost = getCost();
		if (myCost == 0.0) {
			myCost = (nPos > 0) ? ((double) nNeg) / ((double) nPos) : 1.0;
		}
		myCost *= getCostFactor();
		cl += " -j " + myCost;
		if (!Double.isNaN(getTradeOff())) {
			cl += " -c " + getTradeOff();
		}
		if (!Double.isNaN(getEps())) {
			cl += " -e " + getEps();
		}
		if (getKernel() > -1) {
			cl += " -t " + getKernel();
		}
		if (getDParam() > -1) {
			cl += " -d " + getDParam();
		}
		if (!Double.isNaN(getGammaParam())) {
			cl += " -g " + getGammaParam();
		}
		if (!Double.isNaN(getSParam())) {
			cl += " -s " + getSParam();
		}
		if (!Double.isNaN(getCParam())) {
			cl += " -r " + getCParam();
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
	 * @return a line suitable for a SVMlight data file
	 */
	public static String toString(Datum datum, HashMap<String, Integer> featureMap,
			List<Pair<Double, Double>> normFactors) {
		StringBuffer line = new StringBuffer(datum.label() == null ? NEG_LABEL : datum.label().toString());
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
