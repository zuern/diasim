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

/**
 * 
 * @author Arash
 */
public class PositiveFragmentFilter {

	public String phraseTypeFilter;
	public int lengthFilter;

	// could add others
	public PositiveFragmentFilter(String a, int b) {
		this.phraseTypeFilter = a;
		this.lengthFilter = b;

	}

}
