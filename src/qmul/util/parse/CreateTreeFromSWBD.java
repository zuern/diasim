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

package qmul.util.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;

/**
 * THIS IS ONLY USED FOR OPTIONS! SwitchboardCorpus now uses Stanford parser's standard Penn treebank tree-reading lib
 * 
 * @author chrizba
 */
public class CreateTreeFromSWBD {

	private static TreeFactory tf = new LabeledScoredTreeFactory();

	private static HashMap<Integer, Boolean> options = null;
	public static final int INCLUDE_NO_SELFREPAIR_BRACKETS = 0;
	/**
	 * INTJ backchannels, openings, closings, filled pauses, e.g. uh, uh-huh, oh, yeah, okay, well, like, right, huh,
	 * really, no, sure, hi, adios, bye
	 */
	public static final int INCLUDE_NO_INTJ = 1;
	public static final int INCLUDE_NO_E_S = 2;
	public static final int INCLUDE_NO_TRACES = 3;
	public static final int INCLUDE_NO_PUNCTUATION = 4;
	public static final int REPAIR_SELFREPAIRS = 5;
	public static final int SIMPLIFY_CATEGORIES = 6;

	private static final String IGNORE_MARKER = "ooo";

	/**
	 * Set INCLUDE_NO_ options to default values (all true)
	 */
	public static void setDefaultOptions() {
		if (options == null) {
			options = new HashMap<Integer, Boolean>();
		}
		options.clear();
		options.put(INCLUDE_NO_SELFREPAIR_BRACKETS, true); // set to true to remove repair markers [X + Y] -> X Y
		options.put(INCLUDE_NO_INTJ, false); // set to true to remove INTJs (filled pauses, backchannels, closings)
		options.put(INCLUDE_NO_E_S, true); // set to true to remove E_S end-S-unit markers
		options.put(INCLUDE_NO_TRACES, true); // set to true to remove *T*-2, 0 syn traces
		options.put(INCLUDE_NO_PUNCTUATION, true); // set to true to remove punctuation nodes
		options.put(REPAIR_SELFREPAIRS, false); // set to true to replace EDITED([ X +) Y ] with Y
		options.put(SIMPLIFY_CATEGORIES, false); // trim e.g. PP-LOC, NP-SBJ to PP, NP
	}

	/**
	 * Set an INCLUDE_NO_ option
	 * 
	 * @param option
	 * @param value
	 */
	public static void setOption(int option, boolean value) {
		if (options == null) {
			options = new HashMap<Integer, Boolean>();
			setDefaultOptions();
		}
		options.put(option, value);
	}

	/**
	 * Get an INCLUDE_NO_ option
	 * 
	 * @param option
	 * @return value
	 */
	public static boolean getOption(int option) {
		if (options == null) {
			options = new HashMap<Integer, Boolean>();
			setDefaultOptions();
		}
		return options.get(option);
	}

	/**
	 * For testing: use the default file
	 * 
	 * @return the Stanford {@link Tree}
	 */
	public static Tree makeTree() {
		return makeTree(new File("C://Tree1.txt"));
		// return makeTree(new File("D://Chattool//Tree.txt"));
	}

	/**
	 * For testing: use the default file
	 * 
	 * @return the {@link List} of Stanford {@link Tree}s
	 */
	public static List<Tree> makeTrees() {
		return makeTrees(new File("C://Tree1.txt"));
	}

	/**
	 * @param string
	 *            a string representation of a DCPSE tree
	 * @return the Stanford {@link Tree}
	 */
	public static Tree makeTree(String string) {
		return makeTree(new StringReader(string));
	}

	/**
	 * @param string
	 *            a string representation of DCPSE trees
	 * @return the {@link List} of Stanford {@link Tree}s
	 */
	public static List<Tree> makeTrees(String string) {
		return makeTrees(new StringReader(string));
	}

	/**
	 * @param file
	 *            a file containing a DCPSE tree
	 * @return the Stanford {@link Tree}
	 */
	public static Tree makeTree(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			return makeTree(new InputStreamReader(fis));
		} catch (FileNotFoundException fnfe) {
			System.err.println("FileNotFoundException: " + fnfe.getMessage());
			return null;
		}
	}

	/**
	 * @param file
	 *            a file containing DCPSE trees
	 * @return the {@link List} of Stanford {@link Tree}s
	 */
	public static List<Tree> makeTrees(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			return makeTrees(new InputStreamReader(fis));
		} catch (FileNotFoundException fnfe) {
			System.err.println("FileNotFoundException: " + fnfe.getMessage());
			return null;
		}
	}

	/**
	 * @param reader
	 *            a {@link Reader}
	 * @return the {@link List} of Stanford {@link Tree}s
	 */
	public static List<Tree> makeTrees(Reader reader) {
		ArrayList<Tree> trees = new ArrayList<Tree>();
		Tree tree;
		do {
			tree = makeTree(reader);
			if (tree != null) {
				trees.add(tree);
			}
		} while (tree != null);
		return trees;
	}

	/**
	 * @param reader
	 *            a {@link Reader}
	 * @return the Stanford {@link Tree}
	 */
	public static Tree makeTree(Reader reader) {
		if (options == null) {
			setDefaultOptions();
		}
		List<Tree> children = new ArrayList<Tree>();
		Tree t0 = null;
		Tree tPrev = null;
		Tree tAll = null;
		Tree tTemp = null;
		int n = 0;
		String funcStr[] = { "", "" };
		int openBrackets = 0;
		int closeBrackets = 0;
		int totalBrackets = 0;
		int childWhere = Integer.MAX_VALUE;
		String gads = "";
		String otherStuff = "";
		String[] gadsWord = null;
		boolean isAword = false;// do not change
		boolean wasAword = false;// do not change
		boolean processLine = false;// do not change

		try {
			while ((n = reader.read()) != -1) {
				char c = (char) n;
				char charsToIgnore[] = { '.', ',', '?', '\n', '\t', '\r' };

				if (gads == IGNORE_MARKER) {
					if (c == '\n') {
						gads = "";
					}
				} else {
					for (int i = 0; i < charsToIgnore.length; i++) {
						if (c == charsToIgnore[i]) {
							c = '~';
						}
					}
					if (c == '(' || c == ')' || c == ' ' || c == '~') {
						if (c == '(') {
							totalBrackets++;
						} else if (c == ')') {
							totalBrackets--;
						}
						if (gads.matches("") && totalBrackets != 0) {
							// there is nothing yet to process. Collect brackets
							funcStr[0] += c;
							processLine = false;
						} else if (totalBrackets == 0) {
							processLine = true;
						} else {
							processLine = true;
							// Something needs to be put on a tree... I think
							funcStr[1] += c; // start collecting next set of function stuff
							if (funcStr[0].matches("^\\s$")) {
								// need to put something here to prevent it having a fit when multiple words and
								// also to ignore those which are part of the function
								if (c != ' ' || (c == '~' && openBrackets <= 0)) {
									isAword = true;
									if (!wasAword) {
										openBrackets++;
									} else {
										openBrackets--;
									}
								} else {
									processLine = false;
									gads += c;
								}
							} else if (openBrackets < 0 && gads.matches("^[a-zA-Z][a-z]+$")) {
								isAword = true;
								if (c == ' ') {
									processLine = false;
									gads += c;
								} else {
									// hold previous brackets and reset own...
									for (int i = 0; i < closeBrackets; i++) {
										funcStr[1] += ')';
									}
									for (int j = 0; j < openBrackets; j++) {
										funcStr[1] += '(';
									}
									closeBrackets = 0;
									for (int k = 0; k < funcStr[0].length(); k++) {
										if (funcStr[0].charAt(k) == '(') {
											openBrackets++;
										} else if (funcStr[0].charAt(k) == ')') {
											closeBrackets++;
										}
									}
								}
							} else {
								for (int j = 0; j < funcStr[0].length(); j++) {
									if (funcStr[0].charAt(j) == '(') {
										openBrackets++;
									} else if (funcStr[0].charAt(j) == ')') {
										closeBrackets++;
									}
								}
							}
						}
					} else if (c != '~') {
						gads += c;
					}
					if ((gads.matches("^\\s$") || gads.matches(""))) {
						if (totalBrackets != 0 || tAll == null) {
							processLine = false;
						}
					}
					if (gads.matches("^\\*x\\*")) {
						gads = IGNORE_MARKER;
					}
					// // this is actually done later in SwitchboardCorpus using a NodeFilter
					// if (options.get(INCLUDE_NO_INTJ)) {
					// if (gads.contains("INTJ")) {
					// gads = IGNORE_MARKER;
					// }
					// }
				}
				if (processLine) {
					if (gads.matches("E\\_S") || totalBrackets == 0) {
						// we've hit an end of segment; end the tree
						System.out.println("end of segment");
						break;
					}
					if (!gads.matches(IGNORE_MARKER)) {
						// System.out.println("gads is: " + gads);
						tPrev = t0;
						if (isAword) {
							t0 = tf.newLeaf(gads);
						} else {
							t0 = tf.newTreeNode(gads, children);
						}
						if (childWhere == Integer.MAX_VALUE) {
							// System.out.println("It is the first in the tree");
							tAll = t0;// set initially
							childWhere = 0;
						} else if (openBrackets <= closeBrackets) {
							// System.out.println("It should be going up " + (closeBrackets-openBrackets));
							// up x
							if (openBrackets < 0) {
								openBrackets++;
							}
							tTemp = tPrev.ancestor((closeBrackets - openBrackets) + 1, tAll);
							if (tTemp == null) {
								System.out.println("open = " + openBrackets);
								System.out.println("close = " + closeBrackets);
								System.out.println("gads = " + gads);
								System.out.println("t0 = ");
								t0.indentedListPrint();
								System.out.println("tPrev = ");
								tPrev.indentedListPrint();
								System.out.println("tAll = ");
								tAll.indentedListPrint();
								System.err.println("ERROR: null ancestor at " + (childWhere + 1) + " " + tAll);
							}
							tTemp.addChild(t0);
							if (isAword) {
								// System.out.println("It is a word");
								openBrackets = 0;
								closeBrackets = 0;
							}
							// tPrev.addChild(t0);
						} else if (openBrackets > closeBrackets) {
							// down one level
							if (isAword) {
								// System.out.println("It is a word");
								openBrackets--;
							}
							// System.out.println("It should be going down one");
							tPrev.addChild(t0);
						}
					}
					if (!isAword) {
						openBrackets = 0;
						closeBrackets = 0;
						wasAword = false;
					} else {
						wasAword = true;
						isAword = false;
						openBrackets--;
						// System.out.println("closeBrackets is: "+ closeBrackets);
					}
					gads = "";
					processLine = false;
					funcStr[0] = funcStr[1];
					funcStr[1] = "";
				}
			}
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
		if (tAll == null) {
			Tree tSpec = tf.newTreeNode("EMPTY", children);
			return tSpec;
		} else {
			// tAll.indentedListPrint();
			return tAll;
		}
	}

	public static void main(String[] args) {
		Tree t = makeTree();
		t.indentedListPrint();
		List<Tree> l = makeTrees();
		System.out.println("found " + l.size() + " trees");
	}
}
