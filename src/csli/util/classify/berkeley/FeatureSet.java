/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.berkeley;

import java.io.Serializable;

/**
 * A data structure for storing feature/value pairs for an example.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 * @version $Id: FeatureSet.java,v 1.3 2006/10/25 22:19:01 niekrasz Exp $
 */
public class FeatureSet implements Serializable {

	private int LIMIT = 1;

	private long[] m_ids = new long[LIMIT];

	private double[] m_values = new double[LIMIT];

	private int m_size = 0;

	private long m_maxFeatureId = -1;

	/**
	 * @return An array of feature ID numbers.
	 */
	public long[] getIds() {
		return m_ids;
	}

	/**
	 * @return An array of feature values.
	 */
	public double[] getValues() {
		return m_values;
	}

	/**
	 * Adds a feature/value pair to the set.
	 * 
	 * @param i
	 *            The feature ID.
	 * @param val
	 *            The feature value.
	 */
	public void addFeature(long i, double val) {
		if (m_size == LIMIT) {
			LIMIT *= 2;

			long tmpIds[] = new long[LIMIT];
			double tmpValues[] = new double[LIMIT];

			System.arraycopy(m_ids, 0, tmpIds, 0, m_size);
			System.arraycopy(m_values, 0, tmpValues, 0, m_size);

			m_ids = tmpIds;
			m_values = tmpValues;
		}
		m_ids[m_size] = i;
		m_values[m_size] = val;
		m_size++;
		m_maxFeatureId = Math.max(m_maxFeatureId, i);
	}

	/**
	 * Gets the number of features in the set
	 * 
	 * @return The number of features in the set.
	 */
	public int numFeatures() {
		return m_size;
	}

	/**
	 * @return The maximum feature ID number in the set.
	 */
	public long maxFeatureId() {
		return m_maxFeatureId;
	}

}