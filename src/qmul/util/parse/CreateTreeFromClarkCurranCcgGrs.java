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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.util.Pair;

/**
 * Create {@link Tree}s for the Clark & Curran CCG parses for the BNC
 * 
 * @author mpurver
 */
public class CreateTreeFromClarkCurranCcgGrs {

	private static TreeFactory tf = new LabeledScoredTreeFactory();

	private static HashMap<Integer, Boolean> options = null;
	public static final int INCLUDE_NO_SQUARE_BRACKET_SUBCATS = 0;
	public static final int INCLUDE_NO_PUNCTUATION = 7;

	public static final int INCLUDE_NO_PAUSE = 1;
	public static final int INCLUDE_NO_SELFREPAIR_BRACKETS = 2;
	/**
	 * INTJ backchannels, openings, closings, filled pauses, e.g. uh, uh-huh, oh, yeah, okay, well, like, right, huh,
	 * really, no, sure, hi, adios, bye
	 */
	public static final int INCLUDE_NO_INTJ = 3; // 
	public static final int INCLUDE_NO_UNCLEAR = 4;
	public static final int INCLUDE_NO_E_S = 5;
	public static final int INCLUDE_NO_TRACES = 6;
	public static final int REPAIR_SELFREPAIRS = 8;
	public static final int CATEGORIES_NOT_FUNCTIONS = 9;

	private static final String IGNORE_MARKER = "ooo";

	/**
	 * Set INCLUDE_NO_ options to default values (all true)
	 */
	public static void setDefaultOptions() {
		if (options == null) {
			options = new HashMap<Integer, Boolean>();
		}
		options.clear();
		options.put(INCLUDE_NO_SQUARE_BRACKET_SUBCATS, false); // set flag to true to remove all bracketed features
		options.put(INCLUDE_NO_PAUSE, true); // set flag to true to remove pauses
		options.put(INCLUDE_NO_SELFREPAIR_BRACKETS, true); // set to true to remove repair markers [X + Y] -> X Y
		options.put(INCLUDE_NO_INTJ, true); // set to true to remove INTJs (filled pauses, backchannels, closings)
		options.put(INCLUDE_NO_UNCLEAR, true); // set to true to remove indet,?
		options.put(INCLUDE_NO_E_S, true); // set to true to remove E_S end-S-unit markers
		options.put(INCLUDE_NO_TRACES, true); // set to true to remove *T*-2, 0 syn traces
		options.put(INCLUDE_NO_PUNCTUATION, true); // set to true to remove punctuation nodes
		options.put(REPAIR_SELFREPAIRS, true); // set to true to replace [ X + Y ] with Y
		options.put(CATEGORIES_NOT_FUNCTIONS, true); // cats are second caps item: FUNCTION,CAT
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
		return makeTree(new BufferedReader(new StringReader(string)));
	}

	/**
	 * @param string
	 *            a string representation of CCG trees
	 * @return the {@link List} of Stanford {@link Tree}s
	 */
	public static List<Tree> makeTrees(String string) {
		return makeTrees(new BufferedReader(new StringReader(string)));
	}

	/**
	 * @param file
	 *            a file containing a CCG tree
	 * @return the Stanford {@link Tree}
	 */
	public static Tree makeTree(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			return makeTree(new BufferedReader(new InputStreamReader(fis)));
		} catch (FileNotFoundException fnfe) {
			System.err.println("FileNotFoundException: " + fnfe.getMessage());
			return null;
		}
	}

	/**
	 * @param file
	 *            a file containing CCG trees
	 * @return the {@link List} of Stanford {@link Tree}s
	 */
	public static List<Tree> makeTrees(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			return makeTrees(new BufferedReader(new InputStreamReader(fis)));
		} catch (FileNotFoundException fnfe) {
			System.err.println("FileNotFoundException: " + fnfe.getMessage());
			return null;
		}
	}

	/**
	 * @param reader
	 *            a {@link BufferedReader}
	 * @return the {@link List} of Stanford {@link Tree}s
	 */
	public static List<Tree> makeTrees(BufferedReader reader) {
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

	// looks like we're going to need a shift-reduce parser to parse the parses ...
	private static List<Tree> treeStack = new ArrayList<Tree>();
	private static List<String> tagStack = new ArrayList<String>();

	/**
	 * @param reader
	 *            a {@link BufferedReader}
	 * @return the Stanford {@link Tree}
	 */
	public static Tree makeTree(BufferedReader reader) {
		if (options == null) {
			setDefaultOptions();
		}

		String line = null;
		treeStack.clear();
		tagStack.clear();
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				// empty line is the gap between sentences - stop and return what we've built so far
				if (line.isEmpty() && !treeStack.isEmpty()) {
					break;
				}
				// wait for <c> line
				if (!line.startsWith("<c>")) {
					continue;
				}
				String[] chunks = line.split("\\s+");
				for (String chunk : chunks) {
					if (chunk.equals("<c>")) {
						continue;
					}
					String[] fields = chunk.split("\\|");
					if (fields.length != 6) {
						throw new IllegalArgumentException("strange chunk " + chunk + " " + fields.length);
					}
					String word = fields[0];
					String stem = fields[1];
					String postag = fields[2];
					String tag1 = fields[3];
					String tag2 = fields[4];
					String supertag = fields[5];
					if (supertag.matches("^[,.:;!?]+$")) {
						continue;
					}
					// each word gets a lexical leaf node plus a non-terminal supertag node
					Tree synNode = tf.newTreeNode(supertag, new ArrayList<Tree>());
					synNode.addChild(tf.newLeaf(word));
					shift(synNode, supertag);
					reduce();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		reduce();
		if (treeStack.isEmpty()) {
			System.out.println("No nodes, return null");
			return null;
		} else if (treeStack.size() == 1) {
			System.out.println("1 nodes, return " + treeStack.get(0));
			return treeStack.get(0);
		} else {
			Tree tree = tf.newTreeNode("ROOT", treeStack);
			System.out.println(treeStack.size() + " nodes, return " + tree);
			return tree;
		}
	}

	private static void shift(Tree node, String tag) {
		treeStack.add(node);
		tagStack.add(tag);
		System.out.println("shift " + tagStack);
	}

	private static void reduce() {
		int n = (tagStack.size() - 1);
		if (n < 1) {
			return;
		}
		System.out.println("reduce " + tagStack);
		String tag = null;
		if (tag == null) {
			tag = matchRight(tagStack.get(n - 1), tagStack.get(n));
		}
		if (tag == null) {
			tag = matchLeft(tagStack.get(n - 1), tagStack.get(n));
		}
		if (tag == null) {
			return;
		}
		tagStack.remove(n);
		tagStack.remove(n - 1);
		tagStack.add(tag);
		Tree node = tf.newTreeNode(tag, new ArrayList<Tree>());
		node.addChild(treeStack.get(n - 1));
		node.addChild(treeStack.get(n));
		treeStack.remove(n);
		treeStack.remove(n - 1);
		treeStack.add(node);
		reduce();
		return;
	}

	/**
	 * @param tag1
	 * @param tag2
	 * @return the new parent tag: X if tag1 = X/Y and tag2 = Y; X/Z if tag1 = X/Y and tag2 = Y/Z
	 */
	private static String matchRight(String tag1, String tag2) {
		Pair<String, String> halves1 = split(tag1, '/');
		if (halves1 == null) {
			return null;
		}
		System.out.println("MR " + halves1.first + " " + halves1.second);
		// standard forward functional composition
		if (halves1.second().equals(tag2)) {
			return halves1.first();
		}
		// forward composition with B
		Pair<String, String> halves2 = split(tag2, '/');
		if (halves2 == null) {
			return null;
		}
		if (halves1.second().equals(halves2.first())) {
			return halves1.first() + "/" + halves2.second();
		}
		return null;
	}

	/**
	 * @param tag1
	 * @param tag2
	 * @return the new parent tag: X if tag1 = Y and tag2 = X\Y; X\Z if tag1 = Y\Z and tag2 = X\Y
	 */
	private static String matchLeft(String tag1, String tag2) {
		Pair<String, String> halves2 = split(tag2, '\\');
		if (halves2 == null) {
			return null;
		}
		System.out.println("ML " + halves2.first + " " + halves2.second);
		// standard backward functional composition
		if (tag1.equals(halves2.second())) {
			return halves2.first();
		}
		// backward composition with B
		Pair<String, String> halves1 = split(tag1, '\\');
		if (halves1 == null) {
			return null;
		}
		if (halves1.first().equals(halves2.second())) {
			return halves2.first() + "\\" + halves1.second();
		}
		return null;
	}

	/**
	 * @param str
	 * @param separator
	 * @return the halves of str either side of separator, with grouping () brackets taken into consideration; or null
	 *         if str does not contain separator
	 */
	private static Pair<String, String> split(String str, char separator) {
		int numBrackets = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '(') {
				numBrackets++;
			} else if (str.charAt(i) == ')') {
				numBrackets--;
			} else if ((numBrackets == 0) && (str.charAt(i) == separator)) {
				return new Pair<String, String>(str.substring(0, i).replaceFirst("^\\((.*)\\)$", "$1"), str.substring(
						i + 1).replaceFirst("^\\((.*)\\)$", "$1"));
			}
		}
		return null;
	}

	public static void main(String[] args) {
		// String test =
		// "<c> Right|right|RB|I-ADVP|I-TIM|S/S ,|,|,|O|O|, hello|hello|UH|I-INTJ|O|S/S ,|,|,|O|O|, yeah|yeah|UH|I-INTJ|O|S/S ,|,|,|O|O|, we|we|PRP|I-NP|O|NP 're|'re|VBP|I-VP|O|(S[dcl]\\NP)/(S[adj]\\NP) back|back|RB|I-ADVP|O|S[adj]\\NP .|.|.|O|O|.";
		File test = new File("c:/Documents and Settings/mpurver/My Documents/dyndial/align/parsedno_idnewclean_ks_ksw");
		Tree t = makeTree(test);
		t.indentedListPrint();
		List<Tree> l = makeTrees(test);
		System.out.println("found " + l.size() + " trees");
	}
}
