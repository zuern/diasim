/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.berkeley;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * A TreeMap extension for feature data - an indexed vector of values for a single feature. Keys are Objects (intended
 * to be times or utterance start times). Values are Objects (intended to be the corresponding values for this feature).
 * As it's a TreeMap, keys will be returned in ascending order, which is what we want when writing out to file.
 * 
 * @author mpurver
 * 
 */
public class FeatureMap extends TreeMap<Double, Double> {

	private static final long serialVersionUID = -4758327858330001107L;
	private static NumberFormat nf = NumberFormat.getInstance();

	/**
	 * Initialise as an empty hash
	 */
	public FeatureMap() {
		this.clear();
	}

	/**
	 * Initialise from an input feature data file
	 * 
	 * @param inputFile
	 *            the input feature data file
	 */
	public FeatureMap(File inputFile) {
		loadFile(inputFile);
	}

	/**
	 * (Re-)Initialise from an input feature data file
	 * 
	 * @param file
	 *            the input feature data file
	 */
	public void loadFile(File file) {
		char[] cbuf = new char[(int) file.length()];
		try {
			FileReader in = new FileReader(file);
			in.read(cbuf);
			in.close();
		} catch (Throwable t) {
			t.printStackTrace();
			return;
		}

		this.clear();

		String sbuf = String.valueOf(cbuf);
		String[] lines = sbuf.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String[] bits = lines[i].split("\\s+");
			if (bits.length != 2) {
				this.clear();
				return;
			}
			this.put(new Double(bits[0]), new Double(bits[1]));
		}

	}

	/**
	 * Write feature data out to a feature data file
	 * 
	 * @param file
	 *            the output feature data file
	 */
	public void toFile(File file) {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for (Iterator it = this.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				writer.println(nf.format(key) + " " + this.get(key));
			}
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Convenient interface to TreeMap for small-d doubles
	 * 
	 * @param key
	 * @return value
	 */
	public double get(double key) {
		return ((Double) this.get(new Double(key))).doubleValue();
	}

	/**
	 * Convenient interface to TreeMap for small-d doubles
	 * 
	 * @param key
	 * @param value
	 */
	public void put(double key, double value) {
		this.put(new Double(key), new Double(value));
	}

	{
		nf.setMaximumIntegerDigits(15);
		nf.setGroupingUsed(false);
	}
}