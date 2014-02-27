/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package qmul.util;

import java.util.Collection;
import java.util.Collections;

/**
 * Some basic math stuff
 * 
 * @author mpurver
 */
public class MathUtil {

	/**
	 * @param data
	 * @return the sum
	 */
	public static Double sum(Collection<Double> data) {
		Double sum = 0.0;
		for (Double datum : data) {
			sum += datum;
		}
		return sum;
	}

	/**
	 * @param data
	 * @return the sum
	 */
	public static double sum(double[] data) {
		double sum = 0.0;
		for (double datum : data) {
			sum += datum;
		}
		return sum;
	}

	/**
	 * @param data
	 * @return the mean
	 */
	public static Double mean(Collection<Double> data) {
		return (sum(data) / (double) data.size());
	}

	/**
	 * @param data
	 * @return the mean
	 */
	public static double mean(double[] data) {
		return (sum(data) / (double) data.length);
	}

	/**
	 * @param data
	 * @return the min
	 */
	public static Double min(Collection<Double> data) {
		return Collections.min(data);
	}

	/**
	 * @param data
	 * @return the min
	 */
	public static double min(double[] data) {
		double min = Double.POSITIVE_INFINITY;
		for (double datum : data) {
			min = Math.min(min, datum);
		}
		return min;
	}

	/**
	 * @param data
	 * @return the max
	 */
	public static Double max(Collection<Double> data) {
		return Collections.max(data);
	}

	/**
	 * @param data
	 * @return the max
	 */
	public static double max(double[] data) {
		double max = Double.NEGATIVE_INFINITY;
		for (double datum : data) {
			max = Math.max(max, datum);
		}
		return max;
	}

}
