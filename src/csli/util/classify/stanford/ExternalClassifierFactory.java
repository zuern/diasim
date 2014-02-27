/*
 * Created on May 31, 2007 by mpurver
 */
package csli.util.classify.stanford;

import java.io.File;

import csli.util.Config;

/**
 * An extension for the standard Stanford ClassifierFactory class for use with external stand-alone classifiers.
 * Includes result cacheing, feature normalization, and mapping from feature objects to numerical indices (for external
 * classifier packages which like that sort of thing)
 * 
 * @author mpurver
 */
public abstract class ExternalClassifierFactory implements ClassifierFactory {

	private String trainCommand;

	private String classifyCommand;

	private String fileStem;

	private boolean bag = false;

	private boolean normalize = false;

	private boolean preTrained = false;

	public ExternalClassifierFactory(String keyStem, String fileStem) {
		try {
			this.trainCommand = Config.main.get(keyStem + ".train");
			this.classifyCommand = Config.main.get(keyStem + ".classify");
			String dataDir = Config.main.getFileProperty(keyStem + ".datadir").getAbsolutePath();
			File check = new File(dataDir);
			if (!check.exists()) {
				check.mkdirs();
			}
			this.fileStem = dataDir + File.separator + fileStem;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the trainCommand
	 */
	public String getTrainCommand() {
		return trainCommand;
	}

	/**
	 * @return the classifyCommand
	 */
	public String getClassifyCommand() {
		return classifyCommand;
	}

	/**
	 * @return the fileStem
	 */
	public String getFileStem() {
		return fileStem;
	}

	/**
	 * @return if true, features are to be specified as a bag of Objects or ScoredObjects; one feature per unique Object
	 *         will be created, with its value set as the number of instances of that Object in the bag, or the total
	 *         score of the ScoredObject instances. If false, features are to be specified as a list of numerical values
	 *         in consistent order. (default false)
	 */
	public boolean isBag() {
		return bag;
	}

	/**
	 * @param bag
	 *            if true, features are to be specified as a bag of Objects or ScoredObjects; one feature per unique
	 *            Object will be created, with its value set as the number of instances of that Object in the bag, or
	 *            the total score of the ScoredObject instances. If false, features are to be specified as a list of
	 *            numerical values in consistent order. (default false)
	 */
	public void setBag(boolean bag) {
		this.bag = bag;
	}

	/**
	 * @return if true, normalize features to the range 0-1
	 */
	public boolean isNormalize() {
		return normalize;
	}

	/**
	 * @param normalize
	 *            if true, normalize features to the range 0-1
	 */
	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	/**
	 * @return if true, classifier will not be trained, but instead will rely on an existing model
	 */
	public boolean isPreTrained() {
		return preTrained;
	}

	/**
	 * @param preTrained
	 *            if true, classifier will not be trained, but instead will rely on an existing model
	 */
	public void setPreTrained(boolean preTrained) {
		this.preTrained = preTrained;
	}

}
