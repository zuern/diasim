/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.berkeley;

import java.io.File;

/**
 * @author mpurver
 */
public interface Learner {

	/**
	 * Initialise model & output files, training and classification commands.
	 * 
	 * @param fileStem
	 *            the filestem to user for model/result files
	 */
	public void init(String fileStem);

	/**
	 * Learn a model from training data. Should return false on error.
	 * 
	 * @param trainingData
	 *            the training data to learn from
	 * @author mpurver
	 */
	public boolean train(TrainingSet trainingData);

	/**
	 * Set a classification model from file (as an alternative to training - although some classifiers (e.g. Svm, C4_5)
	 * work from model files anyway, in which case initial setup does all you need). Should throw RuntimeException on
	 * error.
	 * 
	 * @param modelFile
	 *            the model file (format learner-dependent)
	 * @author mpurver
	 */
	public void setModel(File modelFile);

	/**
	 * Classify some test data. Should return NULL on error.
	 * 
	 * @param testData
	 *            the test data to classify
	 * @return the classification set
	 * @author mpurver
	 */
	public ClassificationSet[] classify(TrainingSet testData);

	/**
	 * String conversion for particular learner file format.
	 * 
	 * @param data
	 *            the data to convert
	 * @return the String representation
	 * @author mpurver
	 */
	public String toString(TrainingSet data);

	/**
	 * String conversion for particular learner file format.
	 * 
	 * @param data
	 *            the data to convert
	 * @return the String representation
	 * @author mpurver
	 */
	public String toString(TrainingType data);

	/**
	 * String conversion for particular learner file format.
	 * 
	 * @param data
	 *            the data to convert
	 * @return the String representation
	 * @author mpurver
	 */
	public String toString(FeatureSet data);

	/**
	 * String conversion for particular learner file format.
	 * 
	 * @param data
	 *            the data to convert
	 * @return the String representation
	 * @author mpurver
	 */
	public String toString(ClassificationSet data);
}