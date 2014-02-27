/*******************************************************************************
 * Copyright (c) 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Matthew Purver, Queen Mary University of London - initial API and implementation
 ******************************************************************************/
package qmul.distr;

import java.util.ArrayList;
import java.util.HashMap;

public class LabelDistribution extends HashMap<String, Double> {

	private static final long serialVersionUID = 935378696352833144L;

	public LabelDistribution() {
		super();
	}

	public double add(String label) {
		return add(label, 1.0);
	}

	public double add(String label, double score) {
		if (!containsKey(label)) {
			put(label, 0.0);
		}
		double s = get(label) + score;
		put(label, s);
		return s;
	}

	public void add(LabelDistribution distr) {
		add(distr, 1.0);
	}

	public void add(LabelDistribution distr, double weight) {
		for (String label : distr.keySet()) {
			add(label, distr.get(label) * weight);
		}
	}

	public LabelDistribution clone() {
		return (LabelDistribution) super.clone();
	}

	public String toM(ArrayList<String> sortLabels) {
		StringBuffer sb = new StringBuffer("[");
		for (String l : sortLabels) {
			sb.append((containsKey(l) ? get(l) : "0") + ",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]");
		return sb.toString();
	}

}
