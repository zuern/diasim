/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.berkeley;

import csli.util.Config;
import csli.util.InstanceFactory;

/**
 * @author mpurver
 */
public class LearnerFactory {

	static Learner learner = null;

	public static Learner getLearner() {
		if (learner == null) {
			try {
				String LearnerName = Config.main.get("util.learner");
				System.out.println("Creating machine learner of class " + LearnerName);
				learner = (Learner) InstanceFactory.newInstance(LearnerName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return learner;
	}

	public static Learner getLearner(Object o) {
		return getLearner();
	}

}
