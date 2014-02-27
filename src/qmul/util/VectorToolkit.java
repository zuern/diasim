/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
/*
 * VectorToolkit.java
 *
 * Created on 28 October 2007, 13:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package qmul.util;

import java.util.Vector;

/**
 * 
 * @author user
 */
public class VectorToolkit {

	public static Vector appendVector2ToVector1(Vector v1, Vector v2) {
		for (int i = 0; i < v2.size(); i++) {
			v1.addElement(v2.elementAt(i));
		}
		return v1;
	}

	public static boolean vectorOfStringsContains(Vector v, String s) {
		for (int i = 0; i < v.size(); i++) {
			String thing = (String) v.elementAt(i);
			if (thing.equalsIgnoreCase(s))
				return true;
		}
		return false;
	}

	public static String createStringRepresentation(Vector v, String divisor) {
		String s = "";
		for (int i = 0; i < v.size(); i++) {
			String s2 = (String) v.elementAt(i);
			s = s + s2 + divisor;
		}
		return s;

	}

	public static Vector sublist(Vector v, int lowest, int highest) {
		Vector v2 = new Vector();
		for (int i = lowest; i < highest; i++) {
			v2.addElement(v.elementAt(i));
		}
		return v2;
	}

	public static Vector getCopy(Vector input) {
		Vector v = new Vector();
		for (int i = 0; i < input.size(); i++) {
			v.addElement(input.elementAt(i));
		}
		return v;
	}
}
