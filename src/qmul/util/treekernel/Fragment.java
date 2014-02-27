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

public class Fragment {

	public int length;
	public String phraseType;
	public String content;

	public Fragment(String phraseType, String content) {
		this.phraseType = phraseType;

		this.content = content;
		this.length = content.split(" ").length;
	}

	public boolean equals(Object frag) {

		Fragment fragment;
		if (frag instanceof Fragment) {
			fragment = (Fragment) frag;
		} else
			return false;
		return fragment.content.equalsIgnoreCase(this.content) && fragment.phraseType.equalsIgnoreCase(this.phraseType);
	}

	public String toString() {
		return phraseType + "-->" + content;
	}

}
