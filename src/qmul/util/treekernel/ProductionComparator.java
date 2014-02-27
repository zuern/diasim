/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package qmul.util.treekernel;

import java.util.Comparator;

public class ProductionComparator implements Comparator<Production> {

	public int compare(Production p1, Production p2) {

		// System.out.println(p1.getBnf()+" compared to"+p2.getBnf());
		if (p1.getBnf().compareTo(p2.getBnf()) < 0) {
			return -1;
		} else if (p1.getBnf().equals(p2.getBnf())) {
			return 0;
		} else {
			return 1;
		}
	}
}
