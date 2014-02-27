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
import java.util.HashSet;
import java.util.Iterator;

import csli.util.Config;
import csli.util.ShellUtils;

/**
 * Interface for C4.5 decision tree learner/classifier
 * 
 * @author mpurver
 */
public class C4_5Learner implements Learner {

	private String trainCommand;

	private String classifyCommand;

	private String fileStem;

	private String nameFile;

	private String trainFile;

	private String modelFile;

	private String testFile;

	private String resultFile;

	private long[] features = null;

	private HashSet classes = new HashSet();

	/*
	 * @see csli.util.learn.Learner#init(String)
	 */
	public void init(String fileStem) {
		try {
			this.trainCommand = Config.main.getFileProperty("util.learner.c4_5.train").getAbsolutePath();
			this.classifyCommand = Config.main.getFileProperty("util.learner.c4_5.classify").getAbsolutePath();
			String dataDir = Config.main.getFileProperty("util.learner.c4_5.datadir").getAbsolutePath();
			File check = new File(dataDir);
			if (!check.exists()) {
				check.mkdirs();
			}
			this.fileStem = dataDir + File.separator + fileStem;
			this.nameFile = this.fileStem + ".names";
			this.trainFile = this.fileStem + ".data";
			this.modelFile = this.fileStem + ".tree";
			this.testFile = this.fileStem + ".test";
			this.resultFile = this.fileStem + ".labs";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * @see csli.util.learn.Learner#train(TrainingSet)
	 */
	public boolean train(TrainingSet trainingData) {
		File outputFile = new File(this.trainFile);
		try {
			FileWriter out = new FileWriter(outputFile);
			// NB: this call is important as it inits classes, features
			out.write(toString(trainingData));
			out.close();
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
		outputFile = new File(this.nameFile);
		try {
			FileWriter out = new FileWriter(outputFile);
			for (Iterator it = this.classes.iterator(); it.hasNext();) {
				out.write(it.next().toString());
				out.write(it.hasNext() ? "," : ".\n");
			}
			for (int i = 0; i < this.features.length; i++) {
				out.write(this.features[i] + ":continuous.\n");
			}
			out.close();
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
		int retval = ShellUtils.execCommand(this.trainCommand + " -f " + this.fileStem);
		return (retval == 0);
	}

	/*
	 * @see csli.util.learn.Learner#setModel(File)
	 */
	public void setModel(File modelFile) {
		// for C4.5, all files must have the same filestem
		if (modelFile.exists() && modelFile.canRead()) {
			this.fileStem = modelFile.getAbsolutePath();
			this.nameFile = this.fileStem + ".names";
			this.trainFile = this.fileStem + ".data";
			this.modelFile = this.fileStem + ".tree";
			this.testFile = this.fileStem + ".test";
			this.resultFile = this.fileStem + ".out";
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
			int retval = ShellUtils.execCommand(this.classifyCommand + " -f " + this.fileStem);
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
		// buf.append("#" + data.getName() + "\n");
		Enumeration t = data.allTags();
		for (Enumeration e = data.allExamples(); e.hasMoreElements();) {
			String cls = t.nextElement().toString();
			buf.append(toString((FeatureSet) e.nextElement()) + cls + "\n");
			this.classes.add(cls);
		}
		return buf.toString();
	}

	/*
	 * @see csli.util.learn.Learner#toString(FeatureSet)
	 */
	public String toString(FeatureSet data) {
		StringBuffer buf = new StringBuffer();
		if (this.features == null) {
			long[] tmp = data.getIds();
			this.features = new long[data.numFeatures()];
			for (int i = 0; i < data.numFeatures(); i++) {
				this.features[i] = tmp[i];
			}
		}
		for (int i = 0; i < data.numFeatures(); i++) {
			buf.append(data.getValues()[i] + ",");
		}
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