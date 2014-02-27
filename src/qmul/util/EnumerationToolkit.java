/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
/*
 * EnumerationToolkit.java
 *
 * Created on 16 January 2008, 18:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package qmul.util;

import java.util.Enumeration;
import java.util.Vector;

/**
 * 
 * @author user
 */
public class EnumerationToolkit {
	public static Object[] convertEnumerationToArray(Enumeration e) {
		Vector v = new Vector();
		while (e.hasMoreElements()) {
			Object o = e.nextElement();
			v.addElement(o);
		}
		return v.toArray();
	}
}
