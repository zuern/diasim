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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.util.Filter;

/**
 * Create {@link Tree}s for the Clark & Curran CCG parser output, by using the "prolog" output printer as this gives
 * explicit derivation trees
 * 
 * @author mpurver
 */
public class CreateTreeFromClarkCurranCCGProlog {

	private static TreeFactory tf = new LabeledScoredTreeFactory();

	private static HashMap<Integer, Boolean> options = null;
	public static final int REMOVE_SQUARE_BRACKET_SUBCATS = 0;
	public static final int REMOVE_PUNCTUATION = 1;

	/**
	 * Set REMOVE_ options to default values (all false)
	 */
	public static void setDefaultOptions() {
		if (options == null) {
			options = new HashMap<Integer, Boolean>();
		}
		options.clear();
		options.put(REMOVE_SQUARE_BRACKET_SUBCATS, false); // set flag to true to remove all bracketed features
		options.put(REMOVE_PUNCTUATION, false); // set to true to remove punctuation nodes
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

	private static final Pattern RULE_PAT = Pattern
			.compile("^(fa|ba|fc|bc|gfc|gbc|fx|bx|gfx|gbx|rp|lp|tr|ltc|rtc|funny)\\(\\s*'(.+)',$");
	private static final Pattern LEXR_PAT = Pattern.compile("^(lex)\\(\\s*'(.+)',\\s*'(.+)',$");
	private static final Pattern CONJ_PAT = Pattern.compile("^(conj)\\(\\s*'(.+)',\\s*'(.+)',\\s*'(.+)',$");
	private static final Pattern LEAF_PAT = Pattern.compile("^lf\\(\\s*(\\d+),\\s*(\\d+),\\s*'(.+)'\\)");
	private static final Pattern WORD_PAT = Pattern.compile("^w\\(\\s*(\\d+),\\s*(\\d+),\\s*'(.+?)',");

	/**
	 * @param reader
	 *            a {@link BufferedReader}
	 * @return the Stanford {@link Tree}
	 */
	public static Tree makeTree(BufferedReader reader) {
		if (options == null) {
			setDefaultOptions();
		}

		NodeFilter nodeFilter = new NodeFilter();
		String line = null;
		boolean doingTree = false;
		boolean doingWords = false;
		HashMap<Integer, Tree> leaves = new HashMap<Integer, Tree>();
		Tree currentNode = null;
		Tree rootNode = null;
		int treeLevel = 0;
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				// first we need to get the ccg/2 tree structure
				if (line.startsWith("ccg(")) {
					doingTree = true;
					doingWords = false;
					treeLevel = 1;
					// nothing useful on the actual ccg functor line
					continue;
				}
				// next the w/8 word definitions
				if (line.startsWith("w(")) {
					if (!doingTree && !doingWords) {
						// if we've hit the word definitions without seeing a tree, stop
						return null;
					}
					doingTree = false;
					doingWords = true;
				}
				if (doingTree) {
					Matcher m = LEAF_PAT.matcher(line);
					if (m.find()) {
						// System.out.println("matched leaf " + line);
						Tree nonTerminal = tf.newTreeNode(getSynLabel(m.group(3)), new ArrayList<Tree>());
						if (rootNode == null) {
							rootNode = nonTerminal;
						} else {
							currentNode.addChild(nonTerminal);
						}
						Tree leaf = tf.newLeaf("DUMMY");
						nonTerminal.addChild(leaf);
						leaves.put(Integer.parseInt(m.group(2)), leaf);
						// adjust currentNode
						int numOpening = line.replaceAll("[^(]", "").length();
						int numClosing = line.replaceAll("\\)\\.$", "").replaceAll("[^)]", "").length();
						int levelChange = numOpening - numClosing;
						if (levelChange > 0) {
							throw new RuntimeException("deepening with leaf node!");
						} else if (levelChange < 0) {
							do {
								// System.out.println("cu node " + currentNode.label());
								currentNode = currentNode.parent(rootNode);
								// System.out.println("up node " + (currentNode == null ? null : currentNode.label()));
								treeLevel--;
								levelChange++;
							} while (levelChange < 0);
						}
						continue;
					}
					m = RULE_PAT.matcher(line);
					if (m.find()) {
						// System.out.println("matched rule " + line);
						treeLevel++;
						Tree node = tf.newTreeNode(getSynLabel(m.group(2)), new ArrayList<Tree>());
						if (rootNode == null) {
							rootNode = node;
						}
						if (currentNode != null) {
							currentNode.addChild(node);
						}
						currentNode = node;
						// System.out.println("current node " + node.label());
						continue;
					}
					m = LEXR_PAT.matcher(line);
					if (m.find()) {
						// System.out.println("matched lexr " + line);
						treeLevel++;
						Tree node = tf.newTreeNode(getSynLabel(m.group(3)), new ArrayList<Tree>());
						if (rootNode == null) {
							rootNode = node;
						}
						if (currentNode != null) {
							currentNode.addChild(node);
						}
						currentNode = node;
						// System.out.println("current node " + node.label());
						continue;
					}
					m = CONJ_PAT.matcher(line);
					if (m.find()) {
						// System.out.println("matched conj " + line);
						treeLevel++;
						Tree node = tf.newTreeNode(getSynLabel(m.group(4)), new ArrayList<Tree>());
						if (rootNode == null) {
							rootNode = node;
						}
						if (currentNode != null) {
							currentNode.addChild(node);
						}
						currentNode = node;
						// System.out.println("current node " + node.label());
						continue;
					}
					throw new RuntimeException("no match for line " + line);
				}
				if (doingWords) {
					Matcher m = WORD_PAT.matcher(line);
					if (m.find()) {
						Tree leaf = leaves.get(Integer.parseInt(m.group(2)));
						if (leaf == null) {
							throw new RuntimeException("Missing leaf " + m.group(2));
						}
						leaf.setLabel(new StringLabel(m.group(3)));
						leaves.remove(Integer.parseInt(m.group(2)));
					} else {
						if (line.isEmpty()) {
							doingWords = false;
							if (!leaves.isEmpty()) {
								throw new RuntimeException("unmatched leaves " + leaves);
							}
							continue;
						} else {
							throw new RuntimeException("strange word line " + line);
						}
					}
					continue;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		// prune to (optionally) remove punctuation nodes etc, then flatten to remove their dedicated parents
		if (rootNode != null) {
//			System.out.println();
//			System.out.println("raw tree " + rootNode.pennString());
//			System.out.println("pru tree " + rootNode.prune(nodeFilter).pennString());
//			System.out.println("fla tree " + rootNode.prune(nodeFilter).flatten().pennString());
//			rootNode = rootNode.prune(nodeFilter).flatten();
		}
		return rootNode;
	}

	/**
	 * @param str
	 * @return a {@link StringLabel} based on the string, optionally removing [subcats] i.e. S[dcl]/S[b] -> S/S
	 */
	private static Label getSynLabel(String str) {
		if (getOption(REMOVE_SQUARE_BRACKET_SUBCATS)) {
			str = str.replaceAll("\\[(.*?)\\]", "");
		}
		return new StringLabel(str);
	}

	/**
	 * For removing empty nodes with no proper children
	 * 
	 * @author mpurver
	 */
	public static class NodeFilter implements Filter<Tree> {

		private static final long serialVersionUID = 4477196338075916207L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see edu.stanford.nlp.util.Filter#accept(java.lang.Object)
		 */
		@Override
		public boolean accept(Tree obj) {
			if (obj == null) {
				return true;
			}
			if (obj.label() == null) {
				return true;
			}
			if (obj.label().value() == null) {
				return true;
			}
			if (obj.label().value().matches("^[,.:?!;]$") && getOption(REMOVE_PUNCTUATION)) {
				return false;
			}
			return true;
		}

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
