/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package qmul.util.parse;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;

/**
 * A wrapper for the Stanford parser http://nlp.stanford.edu/software/lex-parser.shtml
 * 
 * @author arash
 * @author mpurver modified from DiET {@link ParserWrap}
 * @author Kevin modified from diasim
 */
public class StanfordParser implements TreeParser {

	private static final String DEFAULT_FILE = System.getProperty("user.dir") + File.separator + "lib" + File.separator
            + "stanford-parser-2010-08-20" + File.separator
			+ "englishPCFG.ser.gz";

	private static final String[] DEFAULT_OPTIONS = { "-maxLength", "100", "-retainTmpSubcategories" };

	LexicalizedParser lp;

	public StanfordParser() {
		this(DEFAULT_FILE, DEFAULT_OPTIONS);
	}

	public StanfordParser(String parserFileOrUrl) {
		this(parserFileOrUrl, DEFAULT_OPTIONS);
	}

	public StanfordParser(String parserFileOrUrl, String[] options) {
		lp = new LexicalizedParser(parserFileOrUrl);
		lp.setOptionFlags(options);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.parser.Parser#parse(java.util.List, java.lang.String)
	 */
	@Override
	public boolean parse(List<? extends HasWord> sentence, String goal) {
		try {
			return lp.parse(sentence, goal);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.parser.Parser#parse(java.util.List)
	 */
	@Override
	public boolean parse(List<? extends HasWord> sentence) {
		try {
			return lp.parse(sentence);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.parse.TreeParser#getBestParse()
	 */
	@Override
	public Tree getBestParse() {
		return lp.getBestParse();
	}

	public double getScore() {
		return lp.getPCFGScore();
	}

	/**
	 * Convenience method: splits utt into sentences, uses {@link LexicalizedParser}'s parse() to tokenize and parse
	 * each sentence
	 * 
	 * @param utt
	 * @return a {@link Tree} with ROOT node, with the getBestParse() trees for each sentence as children
	 */
	public Tree parse(String utt) {
		String[] sentences = utt.split("[.!?]");
		// System.out.println("there are sentences:" + sentences.length);
		// LinkedList<Tree> list=new LinkedList<Tree>();
		Label rootLabel = new StringLabel("ROOT");
		Tree concat = new LabeledScoredTreeNode(rootLabel, new LinkedList<Tree>());

		try {
			for (int i = 0; i < sentences.length; i++) {
				boolean parsed = false;
				if (sentences[i].length() > 0)
					parsed = lp.parse(sentences[i]);
				else
					continue;
				Tree t = lp.getBestParse();
				Tree rootChild;
				if (t.children().length == 1)
					rootChild = t.removeChild(0);
				else
					rootChild = t;
				concat.addChild(rootChild);
			}
			if (concat.children().length > 1)
				return concat;
			else
				return concat.removeChild(0);
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			System.out.println("Reinitializing parser because of trying to parse error " + utt);
			this.lp = null;
			Runtime r = Runtime.getRuntime();
			r.gc();
			lp = new LexicalizedParser(System.getProperty("user.dir") + File.separator + "utils" + File.separator
					+ "englishPCFG.ser.gz");
			this.lp.setOptionFlags(new String[] { "-maxLength", "100", "-retainTmpSubcategories" });
			return null;
		}

	}

	public static void main(String a[]) {

		StanfordParser pw = new StanfordParser();
		Tree t = pw
				.parse("this sentence is false. this isn't bad..... I am a good boy. You can't kill my mother. I won't let you. You think this is a game? I think I'll have to kill you first.");
		t.pennPrint();

	}
}
