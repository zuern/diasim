/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
/*
 * StringOperations.java
 *
 * Created on 12 December 2007, 17:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package qmul.util;

/**
 * 
 * @author user
 */
public class StringOperations {

	public static String returnOtherString(String searchString, String s1, String s2) {
		if (s1.equalsIgnoreCase(searchString)) {
			return s2;
		} else if (s2.equalsIgnoreCase(searchString)) {
			return s1;
		}
		System.err.println("NONE OF THE 2 STRINGS MATCH " + searchString + "//" + s1 + "//" + s2);
		System.exit(-123456);
		return null;

	}

}
