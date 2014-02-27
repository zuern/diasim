/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A pair of an Object and a double, with comparison via the double value and natural sort order being highest-scored
 * first.
 * 
 * @author mpurver
 */
public class Scored<X> implements Comparable<Scored<?>> {

	private X myObj;

	private double myScore;

	public Scored(X obj, double score) {
		this.myObj = obj;
		this.myScore = score;
	}

	public Scored(Pair<X, Double> pair) {
		this.myObj = pair.first();
		this.myScore = pair.second();
	}

	/**
	 * @return the object that is being scored
	 */
	public X getObject() {
		return this.myObj;
	}

	/**
	 * @return the score associated with this object
	 */
	public double getScore() {
		return this.myScore;
	}

	/**
	 * Set the score for this object
	 * 
	 * @param score
	 */
	public void setScore(double score) {
		this.myScore = score;
	}

	/**
	 * To allow arrays/collections of ScoredObjects to be sorted by score, highest first
	 */
	public int compareTo(Scored<?> o) {
		double yourScore = o.getScore();
		return (myScore == yourScore ? 0 : (myScore < yourScore ? +1 : -1));
	}

	/**
	 * String representation is score:obj
	 */
	public String toString() {
		return this.myScore + ":" + this.myObj.toString();
	}

	/**
	 * @param str
	 *            the standard score:obj representation as produced by toString()
	 * @return a {@link ScoredObject&lt;String&gt;}
	 */
	public static Scored<String> parse(String str) {
		Matcher m = Pattern.compile("([\\d\\.]+):(.+)").matcher(str);
		if (m.matches()) {
			String objStr = m.group(2);
			double score = Double.parseDouble(m.group(1));
			return new Scored<String>(objStr, score);
		} else {
			throw new RuntimeException("Invalid format " + str);
		}
	}

}
