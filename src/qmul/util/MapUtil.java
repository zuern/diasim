/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package qmul.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Some operations for {@link Map}s
 * 
 * @author mpurver
 */
public class MapUtil {

	/**
	 * Increment the value associated with key by 1, or set it to 1 if there was no mapping for key
	 * 
	 * @param <X>
	 * @param m
	 * @param key
	 * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also
	 *         indicate that the map previously associated null with key.)
	 */
	public static <X> Integer increment(HashMap<X, Integer> m, X key) {
		return m.put(key, ((m.get(key) == null) ? 1 : (m.get(key) + 1)));
	}

	/**
	 * For each key in m2, add the value to the value for key in m1, copying it unchanged if m1 had no value for key
	 * 
	 * @param <X>
	 * @param m1
	 * @param m2
	 */
	public static <X> void addAll(HashMap<X, Integer> m1, HashMap<? extends X, Integer> m2) {
		for (X key : m2.keySet()) {
			if (m1.containsKey(key)) {
				m1.put(key, m1.get(key) + m2.get(key));
			} else {
				m1.put(key, m2.get(key));
			}
		}
	}

	/**
	 * A comparator that can be used to sort map keys in ascending order of the values they map to
	 * 
	 * @param <X>
	 * @author mpurver
	 */
	public class AscendingComparator<X> implements Comparator<X> {

		private Map<X, Integer> map;

		public AscendingComparator(Map<X, Integer> map) {
			this.map = map;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(X o1, X o2) {
			return Double.compare(map.get(o1), map.get(o2));
		}

	}

	/**
	 * A comparator that can be used to sort map keys in descending order of the values they map to
	 * 
	 * @param <X>
	 * @author mpurver
	 */
	public static class DescendingComparator<X> implements Comparator<X> {

		private Map<X, Integer> map;

		public DescendingComparator(Map<X, Integer> map) {
			this.map = map;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(X o1, X o2) {
			return Double.compare(map.get(o2), map.get(o1));
		}

	}

}
