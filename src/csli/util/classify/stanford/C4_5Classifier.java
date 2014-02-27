/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import csli.util.ShellUtils;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;

/**
 * A wrapper for Ross Quinlan's C4.5 decision tree-based classifier (see http://www.rulequest.com/Personal/)
 * 
 * @author mpurver
 */
public class C4_5Classifier extends ExternalClassifier {

	/**
	 * @param classifyCommand
	 * @param fileStem
	 */
	public C4_5Classifier(String classifyCommand, String fileStem) {
		super(classifyCommand, fileStem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#classOf(edu.stanford.nlp.dbm.Datum[])
	 */
	@Override
	public Object[] classOf(Datum[] examples) {
		File dataFile = new File(getFileStem() + C4_5ClassifierFactory.testExt);
		File resultFile = new File(getFileStem() + C4_5ClassifierFactory.resultExt);
		try {
			FileWriter out = new FileWriter(dataFile);
			for (int i = 0; i < examples.length; i++) {
				out.write(C4_5ClassifierFactory.toString(examples[i]));
			}
			out.close();
			int retval = ShellUtils.execCommand(getClassifyCommand() + " -f " + getFileStem());
			if (retval != 0) {
				return null;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}

		char[] cbuf = new char[(int) resultFile.length()];
		try {
			FileReader in = new FileReader(resultFile);
			in.read(cbuf);
			in.close();
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}

		String sbuf = String.valueOf(cbuf);
		String[] stringValues = sbuf.split("\n");
		if (!(stringValues.length == examples.length)) {
			throw new RuntimeException("C4.5 results length " + stringValues.length + " does not match input length "
					+ examples.length);
		}
		Object[] classes = new Object[stringValues.length];
		for (int i = 0; i < stringValues.length; i++) {
			classes[i] = stringValues[i];
		}
		return classes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.classify.stanford.ExternalClassifier#scoresOf(edu.stanford.nlp.dbm.Datum)
	 */
	@Override
	public ClassicCounter<?> scoresOf(Datum example) {
		ClassicCounter<Object> counter = new ClassicCounter<Object>();
		counter.setCount(classOf(example), 1.0);
		return counter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.classify.stanford.ExternalClassifier#scoresOf(java.util.List)
	 */
	@Override
	public <D extends Datum> List<ClassicCounter<?>> scoresOf(List<D> examples) {
		ArrayList<ClassicCounter<?>> counters = new ArrayList<ClassicCounter<?>>(examples.size());
		Object[] clss = classOf(examples.toArray(new Datum[examples.size()]));
		for (int i = 0; i < clss.length; i++) {
			ClassicCounter<Object> c = new ClassicCounter<Object>();
			c.setCount(clss[i], 1.0);
			counters.add(i, c);
		}
		return counters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#scoresOf(edu.stanford.nlp.dbm.Datum[])
	 */
	@Override
	public ClassicCounter<?>[] scoresOf(Datum[] examples) {
		ClassicCounter<?>[] counters = new ClassicCounter<?>[examples.length];
		Object[] clss = classOf(examples);
		for (int i = 0; i < clss.length; i++) {
			ClassicCounter<Object> c = new ClassicCounter<Object>();
			c.setCount(clss[i], 1.0);
			counters[i] = c;
		}
		return counters;
	}

}
