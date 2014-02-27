/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.berkeley;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Data structure for storing a set of training data consisting of positive and negative examples for a class.
 * 
 * @author mpurver after Heloise Hse (hwawen@eecs.berkeley.edu)
 * @version $Id: TrainingType.java,v 1.3 2006/10/25 22:19:01 niekrasz Exp $
 */
public class TrainingType implements Serializable {

	private long m_maxFeatureId = -1;

	private String m_name = null;

	private Vector m_negExamples = new Vector();

	private Vector m_posExamples = new Vector();

	private Vector m_allExamples = new Vector();

	private Vector m_allTags = new Vector();

	/**
	 * Creates a training class instance, <code>name</code>.
	 * 
	 * @param name
	 *            The name of this training class.
	 */
	public TrainingType(String name) {
		setName(name);
	}

	/**
	 * Adds an example to this training class.
	 * 
	 * @param lbl
	 *            Indicates positive or negative example.
	 * @param s
	 *            Example feature set.
	 */
	public void addExample(boolean lbl, FeatureSet s) {
		if (lbl) {
			m_posExamples.addElement(s);
			m_allExamples.addElement(s);
			m_allTags.addElement("+1");
		} else {
			m_negExamples.addElement(s);
			m_allExamples.addElement(s);
			m_allTags.addElement("-1");
		}
		m_maxFeatureId = Math.max(m_maxFeatureId, s.maxFeatureId());
	}

	/**
	 * @return The name of this training class.
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * @return The maximum feature ID number in this training class.
	 */
	public long maxFeatureId() {
		return m_maxFeatureId;
	}

	/**
	 * @return An Enumeration of the negative examples in this training class.
	 */
	public Enumeration negExamples() {
		return m_negExamples.elements();
	}

	/**
	 * @return The number of examples in this training class.
	 */
	public int numExamples() {// total # of examples
		return (numPosExamples() + numNegExamples());
	}

	/**
	 * @return The number of negative examples in this training class.
	 */
	public int numNegExamples() {
		return m_negExamples.size();
	}

	/**
	 * @return The number of positive examples in this training class.
	 */
	public int numPosExamples() {
		return m_posExamples.size();
	}

	/**
	 * @return An Enumeration of the positve examples in this training class.
	 */
	public Enumeration posExamples() {
		return m_posExamples.elements();
	}

	/**
	 * @return An Enumeration of all examples in this training class.
	 */
	public Enumeration allExamples() {
		return m_allExamples.elements();
	}

	/**
	 * @return An Enumeration of all tags in this training class.
	 */
	public Enumeration allTags() {
		return m_allTags.elements();
	}

	/**
	 * Removes all of the examples from this training class.
	 */
	public void removeAllExamples() {
		m_posExamples.removeAllElements();
		m_negExamples.removeAllElements();
		m_allExamples.removeAllElements();
		m_allTags.removeAllElements();
		m_maxFeatureId = -1;
	}

	/**
	 * Sets the type of this training class.
	 */
	public void setName(String s) {
		m_name = s;
	}

}