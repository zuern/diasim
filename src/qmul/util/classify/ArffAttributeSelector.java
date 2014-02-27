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

import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

public class ArffAttributeSelector {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		File file1 = new File(args[0]);
		File file2 = new File(args[1]);

		try {

			ArffLoader loader = new ArffLoader();
			loader.setSource(file1);
			Instances data = loader.getDataSet();
			data.setClassIndex(data.numAttributes() - 1);

			AttributeSelection as = new AttributeSelection();
			as.setEvaluator(new CfsSubsetEval());
			as.setSearch(new BestFirst());
			try {
				as.setInputFormat(data);
				data = Filter.useFilter(data, as);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}

			ArffSaver saver = new ArffSaver();
			saver.setInstances(data);
			saver.setFile(file2);
			saver.writeBatch();

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

	}
}
