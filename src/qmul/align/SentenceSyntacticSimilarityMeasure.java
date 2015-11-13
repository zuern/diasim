/*******************************************************************************
 * Copyright (c) 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Matthew Purver, Queen Mary University of London - initial API and implementation
 ******************************************************************************/
package qmul.align;

import java.util.HashMap;

import qmul.corpus.DialogueSentence;
import qmul.corpus.DialogueTurn;
import qmul.util.parse.PennTreebankTokenizer;
import qmul.util.parse.StanfordParser;
import qmul.util.similarity.SimilarityMeasure;
import qmul.util.treekernel.TreeKernel;
import edu.stanford.nlp.trees.Tree;

public class SentenceSyntacticSimilarityMeasure implements SimilarityMeasure<DialogueSentence> {

	private int kernelType = TreeKernel.SYN_TREES;

	/**
	 * use the default {@link TreeKernel} kernel type (syn rule subtrees)
	 */
	public SentenceSyntacticSimilarityMeasure() {
		super();
		reset();
	}

	/**
	 * @param kernelType
	 *            specify the {@link TreeKernel} kernel type (subtrees/subset trees/syn rules)
	 */
	public SentenceSyntacticSimilarityMeasure(int kernelType) {
		this();
		this.kernelType = kernelType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#similarity(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double similarity(DialogueSentence a, DialogueSentence b) {
		Tree t1 = a.getSyntax();
		Tree t2 = b.getSyntax();
		if ((t1 == null) || (t2 == null)) {
			return 0.0;
		}
		return TreeKernel.resetAndCompute(t1, t2, kernelType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#reset()
	 */
	@Override
	public void reset() {
//		TreeKernel.clearAllowedProductions();
//		TreeKernel.clearBannedProductions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#rawCountsA()
	 */
	@Override
	public HashMap<? extends Object, Integer> rawCountsA() {
		return TreeKernel.getL1counts();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#rawCountsB()
	 */
	@Override
	public HashMap<? extends Object, Integer> rawCountsB() {
		return TreeKernel.getL2counts();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#rawCountsAB()
	 */
	@Override
	public HashMap<? extends Object, Integer> rawCountsAB() {
		return TreeKernel.getCommonCounts();
	}

	public static void main(String[] args) {
		SentenceSyntacticSimilarityMeasure sm = new SentenceSyntacticSimilarityMeasure();
		PennTreebankTokenizer tok = new PennTreebankTokenizer(true);
		StanfordParser p = new StanfordParser();
		// TreeParser p = new RASPParser();
		// String s1 = "Hello , how are you ?";
		// String s2 = "UNCLEAR what can I do for this lady today ?";
		String s1 = "You're full of catarrh.";
		String s2 = "Lot of wax in it, right enough.";
		p.parse(tok.getWordsFromString(s1));
		Tree t1 = p.getBestParse();
		p.parse(tok.getWordsFromString(s2));
		Tree t2 = p.getBestParse();
		System.out.println(s1);
		System.out.println(t1.pennString());
		System.out.println(s2);
		System.out.println(t2.pennString());
		System.out.println(TreeKernel.resetAndCompute(t1, t2, TreeKernel.SYN_TREES));
		System.out.println(TreeKernel.resetAndCompute(t1, t2, TreeKernel.SUB_TREES));
		System.out.println(TreeKernel.resetAndCompute(t1, t2, TreeKernel.SUBSET_TREES));
		// System.exit(0);

		DialogueTurn t = new DialogueTurn("t", 1, null, null);
		DialogueSentence a = new DialogueSentence("a", 1, t, "ok");
		DialogueSentence b = new DialogueSentence("b", 1, t, "ok");
		a.setSyntax(p.parse(a.getTranscription()));
		b.setSyntax(p.parse(b.getTranscription()));
		System.out.println("" + a + " " + a.getSyntax() + "\n" + b + " " + b.getSyntax() + "\n" + "sim = "
				+ sm.similarity(a, b));
		a.setTranscription("ok ok ok ok");
		a.setSyntax(p.parse(a.getTranscription()));
		System.out.println("" + a + " " + a.getSyntax() + "\n" + b + " " + b.getSyntax() + "\n" + "sim = "
				+ sm.similarity(a, b));
		a.setTranscription("that's really not ok");
		a.setSyntax(p.parse(a.getTranscription()));
		System.out.println("" + a + " " + a.getSyntax() + "\n" + b + " " + b.getSyntax() + "\n" + "sim = "
				+ sm.similarity(a, b));
		b.setTranscription("that's really not ok");
		b.setSyntax(p.parse(b.getTranscription()));
		System.out.println("" + a + " " + a.getSyntax() + "\n" + b + " " + b.getSyntax() + "\n" + "sim = "
				+ sm.similarity(a, b));
		a.setTranscription("john likes the small bear");
		a.setSyntax(p.parse(a.getTranscription()));
		b.setTranscription("jim hates the big rabbit");
		b.setSyntax(p.parse(b.getTranscription()));
		// TreeKernel.setIncludeWords(true);
		System.out.println("" + a + " " + a.getSyntax() + "\n" + b + " " + b.getSyntax() + "\n" + "sim = "
				+ sm.similarity(a, b) + "\n" + sm.rawCountsA() + "\n" + sm.rawCountsB() + "\n" + sm.rawCountsAB());
		a.setTranscription("the man likes the small bear");
		a.setSyntax(p.parse(a.getTranscription()));
		// TreeKernel.setIncludeWords(true);
		System.out.println("" + a + " " + a.getSyntax() + "\n" + b + " " + b.getSyntax() + "\n" + "sim = "
				+ sm.similarity(a, b) + "\n" + sm.rawCountsA() + "\n" + sm.rawCountsB() + "\n" + sm.rawCountsAB());
	}

}
