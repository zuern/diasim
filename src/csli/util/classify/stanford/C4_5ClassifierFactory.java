/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import csli.util.ShellUtils;
import edu.stanford.nlp.ling.Datum;

public class C4_5ClassifierFactory extends ExternalClassifierFactory {

	private HashSet<String> classes = new HashSet<String>();

	private ArrayList<String> features;

	private HashMap<String, HashSet<String>> featureValues = new HashMap<String, HashSet<String>>();

	public static final String trainExt = ".data";

	public static final String modelExt = ".tree";

	public static final String testExt = ".test";

	public static final String resultExt = ".labs";

	public static final String nameExt = ".names";

	private static final String CONTINUOUS_FEATURE = "continuous";

	public C4_5ClassifierFactory(String fileStem) {
		this(fileStem, new ArrayList<String>());
	}

	public C4_5ClassifierFactory(String fileStem, ArrayList<String> features) {
		super("util.learner.c4_5", fileStem);
	}

	public <D extends Datum> Classifier trainClassifier(List<D> examples) {

		if (features.size() == 0) {
			for (int i = 0; i < examples.get(0).asFeatures().size(); i++) {
				features.add(i, "feature" + i);
			}
		}

		File dataFile = new File(getFileStem() + trainExt);
		File nameFile = new File(getFileStem() + nameExt);
		try {
			FileWriter out = new FileWriter(dataFile);
			for (Datum example : examples) {
				out.write(toString(example));
				// add class if unseen
				classes.add(example.label().toString());
				// check uniform feature vector length
				assert (example.asFeatures().size() == features.size());
				assert (example.asFeatures() instanceof List);
				// add feature values if unseen
				for (int i = 0; i < features.size(); i++) {
					String f = features.get(i);
					Object v = ((List<?>) example.asFeatures()).get(i);
					if (featureValues.get(f) == null) {
						featureValues.put(f, new HashSet<String>());
					}
					if (!(featureValues.get(f).size() == 1 && featureValues.get(f).contains(CONTINUOUS_FEATURE))) {
						if (v instanceof Double) {
							featureValues.get(f).add(CONTINUOUS_FEATURE);
						} else {
							featureValues.get(f).add(v.toString());
						}
					}
				}
			}
			out.close();
			out = new FileWriter(nameFile);
			for (Iterator<String> it = classes.iterator(); it.hasNext();) {
				out.write(it.next() + (it.hasNext() ? ", " : ".\n"));
			}
			for (String fe : features) {
				out.write(fe + ": ");
				for (Iterator<String> it = featureValues.get(fe).iterator(); it.hasNext();) {
					out.write(it.next() + (it.hasNext() ? ", " : ".\n"));
				}
			}
			out.close();
			int retval = ShellUtils.execCommand(getTrainCommand() + " -f " + getFileStem());
			if (retval == 0) {
				return new C4_5Classifier(getClassifyCommand(), getFileStem());
			} else {
				return null;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	public static String toString(Datum datum) {
		String line = "";
		for (Object feature : datum.asFeatures()) {
			line += feature + ", ";
		}
		line += datum.label();
		return line + "\n";
	}

}
