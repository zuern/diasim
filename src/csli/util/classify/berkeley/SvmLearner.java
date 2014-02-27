/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.berkeley;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Enumeration;

import csli.util.Config;
import csli.util.ShellUtils;

/**
 * Interface for SVMlight support vector machine learner/classifier
 * 
 * @author mpurver
 */
public class SvmLearner implements Learner {

	private String trainCommand;

	private String classifyCommand;

	private String trainFile;

	private String modelFile;

	private String testFile;

	private String resultFile;

	/*
	 * @see csli.util.learn.Learner#init(String)
	 */
	public void init(String fileStem) {
		try {
			this.trainCommand = Config.main.get("util.learner.svm.train");
			this.classifyCommand = Config.main.get("util.learner.svm.classify");
			String dataDir = Config.main.getFileProperty("util.learner.svm.datadir").getAbsolutePath();
			File check = new File(dataDir);
			if (!check.exists()) {
				check.mkdirs();
			}
			String absFileStem = dataDir + File.separator + fileStem;
			this.trainFile = absFileStem + ".train";
			this.modelFile = absFileStem + ".model";
			this.testFile = absFileStem + ".test";
			this.resultFile = absFileStem + ".out";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * @see csli.util.learn.Learner#train(TrainingSet)
	 */
	public boolean train(TrainingSet trainingData) {
		int nNeg = 0;
		int nPos = 0;
		for (Enumeration e = trainingData.types(); e.hasMoreElements();) {
			TrainingType tt = (TrainingType) e.nextElement();
			nNeg += tt.numNegExamples();
			nPos += tt.numPosExamples();
		}
		double cost = ((double) nNeg) / ((double) nPos);
		File outputFile = new File(this.trainFile);
		try {
			FileWriter out = new FileWriter(outputFile);
			out.write(toString(trainingData));
			out.close();
			int retval = ShellUtils.execCommand(this.trainCommand + " -j " + cost + " " + this.trainFile + " "
					+ this.modelFile);
			return (retval == 0);
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	/*
	 * @see csli.util.learn.Learner#setModel(File)
	 */
	public void setModel(File modelFile) {
		if (modelFile.exists() && modelFile.canRead()) {
			// classifier reads model from file, so only need to set name
			this.modelFile = modelFile.getAbsolutePath();
		} else {
			throw new RuntimeException("Can't read " + modelFile.getAbsolutePath());
		}
	}

	/*
	 * @see csli.util.learn.Learner#classify(TrainingSet)
	 */
	public ClassificationSet[] classify(TrainingSet testData) {
		File outputFile = new File(this.testFile);
		try {
			FileWriter out = new FileWriter(outputFile);
			out.write(toString(testData));
			out.close();
			int retval = ShellUtils.execCommand(this.classifyCommand + " " + this.testFile + " " + this.modelFile + " "
					+ this.resultFile);
			if (retval != 0) {
				return null;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}

		File inputFile = new File(this.resultFile);
		char[] cbuf = new char[(int) inputFile.length()];
		try {
			FileReader in = new FileReader(inputFile);
			in.read(cbuf);
			in.close();
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}

		String sbuf = String.valueOf(cbuf);
		String[] stringValues = sbuf.split("\n");
		ClassificationSet[] res = new ClassificationSet[stringValues.length];
		for (int i = 0; i < stringValues.length; i++) {
			double val = Double.parseDouble(stringValues[i]);
			String cls = (val > 0) ? "+1" : "-1"; // could be any string
			Classification cl = new Classification(cls, val);
			res[i] = new ClassificationSet();
			res[i].addElement(cl);
		}
		return res;
	}

	/*
	 * @see csli.util.learn.Learner#toString(TrainingSet)
	 */
	public String toString(TrainingSet data) {
		StringBuffer buf = new StringBuffer();
		for (Enumeration e = data.types(); e.hasMoreElements();) {
			buf.append(toString((TrainingType) e.nextElement()));
		}
		return buf.toString();
	}

	/*
	 * @see csli.util.learn.Learner#toString(TrainingType)
	 */
	public String toString(TrainingType data) {
		StringBuffer buf = new StringBuffer();
		buf.append("#" + data.getName() + "\n");
		Enumeration t = data.allTags();
		for (Enumeration e = data.allExamples(); e.hasMoreElements();) {
			buf.append(t.nextElement() + " ");
			buf.append(toString((FeatureSet) e.nextElement()) + "\n");
		}
		// for (Enumeration e = data.posExamples(); e.hasMoreElements();) {
		// buf.append("+1 ");
		// buf.append(toString((FeatureSet) e.nextElement()) + "\n");
		// }
		// for (Enumeration e = data.negExamples(); e.hasMoreElements();) {
		// buf.append("-1 ");
		// buf.append(toString((FeatureSet) e.nextElement()) + "\n");
		// }
		return buf.toString();
	}

	/*
	 * @see csli.util.learn.Learner#toString(FeatureSet)
	 */
	public String toString(FeatureSet data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.numFeatures(); i++) {
			buf.append(data.getIds()[i] + ":" + data.getValues()[i] + " ");
		}
		buf.setLength(buf.length() - 1);
		return buf.toString();
	}

	/*
	 * @see csli.util.learn.Learner#toString(ClassificationSet)
	 */
	public String toString(ClassificationSet data) {
		Classification cf;
		StringBuffer buf = new StringBuffer();
		for (Enumeration e = data.elements(); e.hasMoreElements();) {
			cf = (Classification) e.nextElement();
			buf.append(cf.getType() + "[" + cf.getConfidence() + "]\t");
		}
		buf.setLength(buf.length() - 1);
		buf.append("\n");
		return buf.toString();
	}
}