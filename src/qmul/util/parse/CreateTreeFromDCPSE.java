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
 * 
 * @author chrizba
 */
public class CreateTreeFromDCPSE {

	private static TreeFactory tf = new LabeledScoredTreeFactory();

	private static HashMap<Integer, Boolean> options = null;
	public static final int INCLUDE_NO_BRACKETS = 0;
	public static final int INCLUDE_NO_PAUSE = 1;
	public static final int INCLUDE_NO_IGNORE = 2;
	/**
	 * DISMK,INTERJEC interjections & filled pauses e.g. ah, uhm, mm, mmm, uh, oh, wow, argh, woah
	 */
	public static final int INCLUDE_NO_UMM = 3;
	public static final int INCLUDE_NO_UNCLEAR = 4;
	/**
	 * DISMK,REACT backchannels e.g. yes, yeah, right, ok, mm, well, no, absolutely
	 */
	public static final int INCLUDE_NO_REACT = 5;
	public static final int CATEGORIES_NOT_FUNCTIONS = 6;
	public static final int PP_LEXICAL_FEATURES = 7; // is only actually applied in DCPSECorpus!

	private static final String IGNORE_MARKER = "ooo";

	/**
	 * Set INCLUDE_NO_ options to default values (all true)
	 */
	public static void setDefaultOptions() {
		if (options == null) {
			options = new HashMap<Integer, Boolean>();
		}
		options.clear();
		options.put(INCLUDE_NO_BRACKETS, true); // set flag to true to remove all bracketed features
		options.put(INCLUDE_NO_PAUSE, false); // set flag to true to remove pauses
		options.put(INCLUDE_NO_IGNORE, false); // set to true to remove repaired
		options.put(INCLUDE_NO_UMM, false); // set to true to remove interjections (includes oh, mm, um, er)
		options.put(INCLUDE_NO_UNCLEAR, false); // set to true to remove indet,?
		options.put(INCLUDE_NO_REACT, false); // set to true to remove backchannels (includes yes, yeah, ok, mm)
		options.put(CATEGORIES_NOT_FUNCTIONS, true); // cats are second caps item: FUNCTION,CAT
		options.put(PP_LEXICAL_FEATURES, false); // if true, add head prep word to PP as first bracketed feature
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
		return makeTree(new File("C://Tree.txt"));
		// return makeTree(new File("D://Chattool//Tree.txt"));
	}

	/**
	 * For testing: use the default file
	 * 
	 * @return the {@link List} of Stanford {@link Tree}s
	 */
	public static List<Tree> makeTrees() {
		return makeTrees(new File("C://Tree.txt"));
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
		int countspace = 0;
		int countspaceprevious = 0;
		int countspacepreviousprevious = 0;
		char c1 = 'x';
		int childWhere = Integer.MAX_VALUE;
		String gads = "";
		String otherStuff = "";
		String[] gadsWord = null;
		boolean isAword = false;// do not change
		boolean processLine = true;// do not change

		try {
			while ((n = reader.read()) != -1) {
				char c = (char) n;
				if (c == '[' && gads.matches("")) {
					processLine = false;
					// System.out.println(otherStuff);
					otherStuff = "";
				}

				if (processLine) {
					if (c == '\n') {
						if (gads.matches("^\\s+$")) {
							// we've hit a line containing only whitespace: end of the tree
							break;
						}
						if (options.get(INCLUDE_NO_PAUSE)) {
							if (gads.contains("PAUSE")) {
								gads = IGNORE_MARKER;
							}
						}
						// remove "ignored" nodes; unless we need to keep them to work out features, in which case we'll
						// remove them later in DCPSECorpus
						if (options.get(INCLUDE_NO_IGNORE) && !options.get(PP_LEXICAL_FEATURES)) {
							if (gads.contains("ignore)")) {
								gads = IGNORE_MARKER;
							}
						}
						if (options.get(INCLUDE_NO_UMM)) {
							if (gads.contains("DISMK,INTERJEC")) {
								gads = IGNORE_MARKER;
							}
						}
						if (options.get(INCLUDE_NO_REACT)) {
							if (gads.contains("DISMK,REACT")) {
								gads = IGNORE_MARKER;
							}
						}
						if (options.get(INCLUDE_NO_UNCLEAR)) {
							if (gads.contains("INDET,?")) {
								gads = IGNORE_MARKER;
							}
						}
						if (gads.contains("{")) {
							// remove all annoying browser markup
							gadsWord = gads.replaceAll("\\[.*?\\]", "").split("\\s+");
							gads = gadsWord[0];
							isAword = true;
						}
						if (options.get(INCLUDE_NO_BRACKETS)) {
							if (gads.contains("(")) {
								gads = gads.replaceAll("\\(.+\\)", "");
							}
						}
						if (options.get(CATEGORIES_NOT_FUNCTIONS) && !gads.matches(IGNORE_MARKER)) {
							gads = gads.replaceFirst(".*?,", "");
						}
						if (!gads.matches(IGNORE_MARKER)) {
							tPrev = t0;
							t0 = tf.newTreeNode(gads.trim(), children);
							if (childWhere == Integer.MAX_VALUE) {
								tAll = t0;
							} else if (childWhere >= 0) {
								// up x
								tTemp = tPrev.ancestor(childWhere + 1, tAll);
								if (tTemp == null) {
									System.out.println("c1 = " + c1);
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
							} else if (childWhere < 0) {
								// down one level
								tPrev.addChild(t0);
							}
							if (isAword) {
								tPrev = t0;
								String wordLabel = gadsWord[1];
								for (int iWord = 2; iWord < gadsWord.length; iWord++) {
									wordLabel += " " + gadsWord[iWord];
								}
								tTemp = tf.newLeaf(wordLabel.trim());
								tPrev.addChild(tTemp);
								isAword = false;
								tTemp = null;
							}
						}
						if (gads.matches(IGNORE_MARKER)) {// reset previous counter if is a line to ignore
							countspaceprevious = countspaceprevious + childWhere;

						}
						gads = "";
						c1 = 'y';
					} else if (c1 == 'y' && c == ' ') {// was just a return character and is space
						countspace++;
					} else {// not a leading space or a return character
						gads += c;
						c1 = 'x';
						if (countspace != 0) {
							childWhere = countspaceprevious - countspace;
							countspaceprevious = countspace;
							countspacepreviousprevious = countspaceprevious;
							countspace = 0;
						}
					}
				} else if (c == '\n') { // (if not processLine = True)
					processLine = true;
				} else { // processLine = false and not a return character
					otherStuff += c;
				}
			}
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
		if (tAll == null && !otherStuff.isEmpty()) {
			Tree tSpec = tf.newTreeNode("EMPTY", children);
			return tSpec;
		} else {
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
