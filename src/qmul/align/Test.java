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
package qmul.align;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Test {

	private static Random random = new Random();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<Integer> l = new ArrayList<Integer>();
		for (int i = 0; i < 20; i++) {
			l.add(i);
		}
		System.out.println(l);
		for (int i = 0; i < 1000; i++) {
			Collections.shuffle(l, random);
			System.out.println(l);
		}
	}

}
