/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;

/** @author Dan Klein, mpurver */

public abstract class Classifier implements edu.stanford.nlp.classify.Classifier {

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#labels()
	 */
	@Override
	public Collection labels() {
		// TODO Auto-generated method stub
		return null;
	}

	public abstract Object classOf(Datum example);

	/**
	 * Convenience class for classifying a List of data. May be overridden by classifiers which e.g. have a high startup
	 * overhead, such as those which have to read/write large files
	 * 
	 * @param examples
	 *            a List of things which extend Datum
	 * @return a List of classified label Objects
	 */
	public <D extends Datum> List<Object> classOf(List<D> examples) {
		ArrayList<Object> objects = new ArrayList<Object>();
		for (D example : examples) {
			objects.add(classOf(example));
		}
		return objects;
	}

	/**
	 * Convenience class for classifying an array of data. May be overridden by classifiers which e.g. have a high
	 * startup overhead, such as those which have to read/write large files
	 * 
	 * @param examples
	 *            an array of things which extend Datum
	 * @return an array of classified label Objects
	 */
	public Object[] classOf(Datum[] examples) {
		Object[] objects = new Object[examples.length];
		for (int i = 0; i < examples.length; i++) {
			objects[i] = classOf(examples[i]);
		}
		return objects;
	}

	public abstract ClassicCounter<?> scoresOf(Datum example);

	/**
	 * Convenience class for classifying a List of data. May be overridden by classifiers which e.g. have a high startup
	 * overhead, such as those which have to read/write large files
	 * 
	 * @param examples
	 *            a List of things which extend Datum
	 * @return a List of ClassicCounters
	 */
	public <D extends Datum> List<ClassicCounter<?>> scoresOf(List<D> examples) {
		ArrayList<ClassicCounter<?>> counters = new ArrayList<ClassicCounter<?>>();
		for (D example : examples) {
			counters.add(scoresOf(example));
		}
		return counters;
	}

	/**
	 * Convenience class for classifying an array of data. May be overridden by classifiers which e.g. have a high
	 * startup overhead, such as those which have to read/write large files
	 * 
	 * @param examples
	 *            an array of things which extend Datum
	 * @return an array of ClassicCounters
	 */
	public ClassicCounter<?>[] scoresOf(Datum[] examples) {
		ClassicCounter<?>[] counters = new ClassicCounter<?>[examples.length];
		for (int i = 0; i < examples.length; i++) {
			counters[i] = scoresOf(examples[i]);
		}
		return counters;
	}
}
