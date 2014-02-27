/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.dsp;

import java.util.List;

/**
 * A generic interface for smoothing routines.
 * 
 * @author mpurver
 */
public interface Smoother {

	/**
	 * @param raw
	 *            a data series
	 * @return a smoothed version
	 */
	public double[] smooth(double[] raw);

	/**
	 * @param raw
	 *            a data series
	 * @return a smoothed version
	 */
	public List<Double> smooth(List<Double> raw);

}
