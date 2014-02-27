/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.berkeley;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * A TreeMap extension for feature data - a map from keys to FeatureSets (values for a set of features). Keys are
 * Objects or small-d doubles (intended to be times or utterance start times). Values are FeatureSets (intended to be
 * the corresponding set of values for a set of features at this time). As it's a TreeMap, keys will be returned in
 * ascending order.
 * 
 * @author mpurver
 * 
 */
public class FeatureSetMap extends TreeMap {

	/**
	 * Initialise as an empty hash
	 */
	public FeatureSetMap() {
		this.clear();
	}

	/**
	 * Convenient interface to TreeMap for small-d doubles
	 * 
	 * @param key
	 * @param value
	 */
	public void put(double key, FeatureSet value) {
		this.put(new Double(key), value);
	}

	/**
	 * Convenient interface to TreeMap for small-d doubles
	 * 
	 * @param key
	 * @return value
	 */
	public FeatureSet get(double key) {
		return ((FeatureSet) this.get(new Double(key)));
	}

	/**
	 * Add a FeatureMap (vector of keyed values for one feature) to this FeatureSetMap
	 */
	public boolean add(FeatureMap map) {
		// must contain all keys or none
		boolean empty = this.isEmpty();

		for (Iterator it = map.keySet().iterator(); it.hasNext();) {
			double key = ((Double) it.next()).doubleValue();
			FeatureSet fs = (empty ? new FeatureSet() : this.get(key));
			if (fs == null) {
				return false;
			}
			fs.addFeature(fs.numFeatures() + 1, map.get(key));
			this.put(key, fs);
		}

		return true;
	}

}