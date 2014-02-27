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
 * Data structure for storing a set of confidence values in descending order.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 * @version $Id: ClassificationSet.java,v 1.3 2006/10/25 22:19:01 niekrasz Exp $
 */
public class ClassificationSet implements Serializable {

	private Vector m_set = new Vector();

	/**
	 * Class constructor.
	 */
	public ClassificationSet() {
	}

	/**
	 * Adds a Classification item to the set.
	 */
	public void addElement(Classification c) {
		double val = c.getConfidence();
		boolean inserted = false;
		for (int i = 0; i < m_set.size(); i++) {
			Classification cf = (Classification) m_set.elementAt(i);
			if (val > cf.getConfidence()) {
				m_set.insertElementAt(c, i);
				inserted = true;
				break;
			}
		}
		if (!inserted) {
			m_set.addElement(c);
		}
	}

	/**
	 * @return An enumeration of Classification items.
	 */
	public Enumeration elements() {
		return m_set.elements();
	}

	/**
	 * @param s
	 *            The type name.
	 * @return The classification information of type <code>s</code> in this set.
	 */
	public Classification getClassification(String s) {
		for (Enumeration e = elements(); e.hasMoreElements();) {
			Classification cf = (Classification) e.nextElement();
			if (cf.getType().compareTo(s) == 0) {
				return cf;
			}
		}
		return null;
	}

	/**
	 * @return The <code>Classification</code> item with the highest confidence value.
	 */
	public Classification getHighest() {
		if (m_set.size() > 0) {
			return (Classification) m_set.firstElement();
		} else {
			return null;
		}
	}

	/**
	 * @return number of Classification items in this set.
	 */
	public int size() {
		return m_set.size();
	}

}