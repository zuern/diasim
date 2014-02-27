/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.berkeley;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Data structure for storing multiple sets of training data. Each set contains the data for a class.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 * @version $Id: TrainingSet.java,v 1.3 2006/10/25 22:19:01 niekrasz Exp $
 */
public class TrainingSet implements Serializable {

	private Hashtable m_types = null;

	/**
	 * Class constructor.
	 */
	public TrainingSet() {
		m_types = new Hashtable();
	}

	/**
	 * Adds an example to the class, <code>type</code>.
	 * 
	 * @param type
	 *            Name of the class that this example belongs to.
	 * @param lbl
	 *            Indicate positive or negative examples.
	 * @param s
	 *            Example feature set.
	 */
	public void addExample(String type, boolean lbl, FeatureSet s) {
		TrainingType tc = getType(type);
		if (tc == null) {
			tc = new TrainingType(type);
			addType(tc);
		}
		tc.addExample(lbl, s);
	}

	/**
	 * Adds a set of training data for a particular class.
	 * 
	 * @param te
	 *            The training data to be added.
	 */
	public void addType(TrainingType te) {
		m_types.put(te.getName(), te);
	}

	/**
	 * @return The set of training data for class <code>s</code>.
	 */
	public TrainingType getType(String s) {
		return ((TrainingType) m_types.get(s));
	}

	/**
	 * @return Number of training data sets.
	 */
	public int numTypes() {
		return m_types.size();
	}

	/**
	 * Removes the set of training data for class <code>s</code>.
	 * 
	 * @param s
	 *            The name of the class to be removed.
	 */
	public void removeType(String s) {
		m_types.remove(s);
	}

	/**
	 * @return An enumeration of training data sets.
	 */
	public Enumeration types() {
		return m_types.elements();
	}

}