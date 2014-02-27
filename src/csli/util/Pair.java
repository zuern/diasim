/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util;

import java.io.Serializable;

/**
 * Pair of objects (when you need to return two, or put them on a list)
 * 
 * @author Danilo Mirkovic
 */
public class Pair<A1, B2> implements Serializable {
	final static long serialVersionUID = 84568461;

	public A1 a;

	public B2 b;

	public boolean onlyFirstToString;

	public Pair(A1 a, B2 b) {
		this(a, b, false);
	}

	public Pair(Pair<A1, B2> p) {
		this(p.a, p.b, false);
	}

	public Pair(A1 a, B2 b, boolean onlyFirstToString) {
		this.a = a;
		this.b = b;
		this.onlyFirstToString = onlyFirstToString;
	}

	public A1 first() {
		return a;
	}

	public B2 second() {
		return b;
	}

	public String toString() {
		if (onlyFirstToString)
			return a.toString();
		return a.toString() + " ; " + b.toString();
	}

	/*
	 * public int compareTo(Object c) { if (c instanceof Pair) { if ((this.b instanceof Double) && (((Pair) c).b
	 * instanceof Double)) { return ((Double) this.b).compareTo((Double) ((Pair) c).b); } else if ((this.b instanceof
	 * Integer) && (((Pair) c).b instanceof Integer)) { return ((Integer) this.b).compareTo((Integer) ((Pair) c).b); }
	 * else { throw new ClassCastException(); } } else { throw new ClassCastException(); } }
	 */

	// compareTo Reworked by dhaley 2005-08-08
	// Implements specialized Comparator interface. Cleaner this way. Can't
	// compare
	// to other kinds of objects anyhow, and the original version did
	// essentially
	// the same type checking. (both are pairs, both pairs have values of same
	// type)
	// NOTE: Comparability all moved to class ComparablePair.
	// (old version left above)
	/**
	 * Tests for equality against another object.
	 * 
	 * <p>
	 * Two pairs are equal if and only if:
	 * <ul>
	 * <li>their first elements are equal, and:</li>
	 * <li>their second elements are equal</li>
	 * </ul>
	 * 
	 * @param obj
	 *            The object to test against.
	 * @return True if the two objects are equal.
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		// Make sure that the other object is a pair
		if (!(obj instanceof Pair))
			return false;

		Pair p = (Pair) obj;

		// Two pairs considered equal if and only if both elements are equal

		try {
			// The first thing to check is that both pairs' members are in fact
			// the same object
			// If they're not the same object, then do full .equals() checking
			// (the == check is needed if either of first() or second() is null)

			return (this.first() == p.first() && this.second() == p.second()) || this.first().equals(p.first())
					&& this.second().equals(p.second());
		} catch (NullPointerException error) {
			// A NPE means that something was null on one end, put not on the
			// other end
			// Hence the two pairs cannot be equal
			return false;
		}
	}

}
