/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package qmul.util.classify;

import java.io.File;
import java.io.IOException;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.ArffLoader;
import weka.core.converters.LibSVMSaver;
import weka.core.converters.SVMLightSaver;

public class ArffConverter {

	private static class MySVMLightSaver extends SVMLightSaver {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * turns the instance into a svm light row. Same as inherited version but swaps -1 and +1 classes around so that
		 * 0 -> -1, 1 -> +1
		 * 
		 * @param inst
		 *            the instance to transform
		 * @return the generated svm light row
		 */
		@Override
		protected String instanceToSvmlight(Instance inst) {
			StringBuffer result;
			int i;

			result = new StringBuffer();

			// class
			if (inst.classAttribute().isNominal()) {
				if (inst.classValue() == 1)
					result.append("1");
				else if (inst.classValue() == 0)
					result.append("-1");
			} else {
				result.append("" + Utils.doubleToString(inst.classValue(), MAX_DIGITS));
			}

			// attributes
			for (i = 0; i < inst.numAttributes(); i++) {
				if (i == inst.classIndex())
					continue;
				if (inst.value(i) == 0)
					continue;
				result.append(" " + (i + 1) + ":" + Utils.doubleToString(inst.value(i), MAX_DIGITS));
			}

			return result.toString();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String flag = args[0];
		int arg = 1;
		if (!flag.startsWith("-")) {
			flag = "-libsvm";
			arg = 0;
		}

		File file1 = new File(args[arg]);
		File file2 = new File(args[arg + 1]);

		try {

			ArffLoader loader = new ArffLoader();
			loader.setSource(file1);
			Instances data = loader.getDataSet();
			data.setClassIndex(data.numAttributes() - 1);
			AbstractFileSaver saver;
			if (flag.equalsIgnoreCase("-libsvm")) {
				saver = new LibSVMSaver();
			} else if (flag.equalsIgnoreCase("-svmlight")) {
				saver = new MySVMLightSaver();
			} else if (flag.equalsIgnoreCase("-svmlight2")) {
				saver = new SVMLightSaver();
			} else {
				throw new IOException("unknown flag " + flag);
			}
			saver.setInstances(data);
			saver.setFile(file2);
			saver.writeBatch();

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

	}
}
