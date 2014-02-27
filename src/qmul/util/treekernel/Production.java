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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.Tree;

public class Production {

	/**
	 * Must separate syncats with something, otherwise NP->N and N->PN are both just NPN
	 */
	public static final String SEPARATOR = ":";

	private Tree node = null;

	private String bnf = null;

	/**
	 * @param node
	 *            the Stanford-style tree representation of this node
	 * @param calcBnf
	 *            if true, calculate BNC-style production now
	 */
	public Production(Tree node, boolean calcBnf) {
		this.node = node;
		if (calcBnf) {
			setBnf();
		}
	}

	public Production(String bnf) {
		String[] fields = bnf.trim().split(Pattern.quote(SEPARATOR));
		if (fields.length < 2) {
			throw new RuntimeException("too few BNF fields " + bnf);
		}
		LabeledScoredTreeFactory tf = new LabeledScoredTreeFactory();
		ArrayList<Tree> children = new ArrayList<Tree>();
		for (int i = 1; i < fields.length; i++) {
			children.add(tf.newLeaf(fields[i]));
		}
		node = tf.newTreeNode(fields[0], children);
		setBnf();
		if (!this.bnf.equals(bnf)) {
			throw new RuntimeException("BNFs not equal: " + bnf + " vs " + this.bnf);
		}
	}

	/**
	 * Set the BNF-style production for this node
	 */
	private void setBnf() {
		List<Tree> children = node.getChildrenAsList();
		Label nodeLabel = node.label();
		// System.out.println(nodeLabel.value());
		String bnf;
		if (nodeLabel == null) {
			bnf = "";
		} else {
			bnf = nodeLabel.value();
		}
		for (Tree child : children) {
			Label childLabel = child.label();
			if (childLabel == null) {
				bnf += "";
			} else {
				bnf += SEPARATOR + childLabel.value();
			}
		}
		this.bnf = bnf;
	}

	/**
	 * @return the production for this node: mother + children in BNF form
	 */
	public String getBnf() {
		if (bnf == null) {
			setBnf();
		}
		return this.bnf;
	}

	/**
	 * @return the Stanford-style tree for this node
	 */
	public Tree getNode() {
		return this.node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.bnf + " " + node.depth();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		// System.out.println("equals method called");
		Production p;
		if (o == null)
			return false;
		if (o instanceof Production) {
			p = (Production) o;
		} else
			return false;
		boolean result = this.node.equals(p.getNode());
		// System.out.println("equals method returned"+result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return node.hashCode();
	}

}
