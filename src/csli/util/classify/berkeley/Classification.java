/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.berkeley;

import java.io.Serializable;

/**
 * Data structure for storing the confidence value when an example is classified.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 * @version $Id: Classification.java,v 1.2 2006/10/25 22:19:01 niekrasz Exp $
 */
public class Classification implements Serializable {

	private double m_confidence;

	private String m_type;

	/**
	 * Creates an instance with type <code>type</code> and confidence value <code>confidence</code>.
	 * 
	 * @param type
	 *            name of the model
	 * @param confidence
	 *            value represents how confident the algorithm thinks that the example should be classified as
	 *            <code>type</code> model.
	 */
	public Classification(String type, double confidence) {
		m_type = type;
		m_confidence = confidence;
	}

	/**
	 * @return The confidence value
	 */
	public double getConfidence() {
		return m_confidence;
	}

	/**
	 * @return The name of the classifier.
	 */
	public String getType() {
		return m_type;
	}

	/**
	 * Sets the confidence value.
	 * 
	 * @param d
	 *            The confidence value.
	 */
	public void setConfidence(double d) {
		m_confidence = d;
	}

	/**
	 * Sets the name of the classifier.
	 * 
	 * @param s
	 *            The name of the classifier.
	 */
	public void setType(String s) {
		m_type = s;
	}

}