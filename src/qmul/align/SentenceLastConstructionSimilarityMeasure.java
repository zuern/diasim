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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import qmul.corpus.DialogueSentence;
import qmul.util.MapUtil;
import qmul.util.similarity.SimilarityMeasure;
import qmul.util.treekernel.Production;
import edu.stanford.nlp.trees.Tree;

/**
 * For a given sentence: 1 if the last occurrence of the specified phenomenon matches this one in form, 0 if it doesn't
 * match or doesn't exist. (So not a pairwise {@link SimilarityMeasure} at all really - one argument will be null)
 * 
 * @author mpurver
 */
public class SentenceLastConstructionSimilarityMeasure implements SimilarityMeasure<DialogueSentence> {

	private String bnfPattern1;
	private String bnfPattern2;
	private int lastMatchingForm = 0;
	private HashMap<String, Integer> countsA = new HashMap<String, Integer>();
	private HashMap<String, Integer> countsB = new HashMap<String, Integer>();
	private HashMap<String, Integer> countsAB = new HashMap<String, Integer>();

	/**
	 * specify the alternation - two alternative forms of the same phenomenon
	 * 
	 * @param bnfPattern1
	 *            one form of the construction we're looking for (as regexp over BNFs)
	 * @param bnfPattern2
	 *            the other form of the construction we're looking for (as regexp over BNFs)
	 */
	public SentenceLastConstructionSimilarityMeasure(String bnfPattern1, String bnfPattern2) {
		super();
		this.bnfPattern1 = bnfPattern1;
		this.bnfPattern2 = bnfPattern2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#similarity(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double similarity(DialogueSentence a, DialogueSentence b) {
		countsA.clear();
		countsB.clear();
		countsAB.clear();
		// IGNORE a, just compare b to last matched sentence ...
		double score = 0.0;
		for (String bnf : getBnfList(b.getSyntax())) {
			// System.out.println("Matching [" + bnf + "] vs [" + bnfPattern1 + "]");
			if (bnf.matches(bnfPattern1)) {
				MapUtil.increment(countsB, bnfPattern1);
				if (lastMatchingForm == 1) {
					MapUtil.increment(countsAB, bnfPattern1);
					score += 1.0;
				}
				lastMatchingForm = 1;
			} else if (bnf.matches(bnfPattern2)) {
				MapUtil.increment(countsB, bnfPattern2);
				if (lastMatchingForm == 2) {
					MapUtil.increment(countsAB, bnfPattern2);
					score += 1.0;
				}
				lastMatchingForm = 2;
			}
		}
		return score;
	}

	/**
	 * @param t
	 * @return a BNF list representing the whole tree, e.g. [S:NP:VP, NP:N, N:john, VP:V, V:snores]
	 */
	private List<String> getBnfList(Tree t) {
		ArrayList<String> list = new ArrayList<String>();
		list.add(new Production(t, true).getBnf());
		for (Tree kid : t.getChildrenAsList()) {
			list.addAll(getBnfList(kid));
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#reset()
	 */
	@Override
	public void reset() {
		// forget the last matching sentence
		lastMatchingForm = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#rawCountsA()
	 */
	@Override
	public HashMap<? extends Object, Integer> rawCountsA() {
		return countsA;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#rawCountsB()
	 */
	@Override
	public HashMap<? extends Object, Integer> rawCountsB() {
		return countsB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#rawCountsAB()
	 */
	@Override
	public HashMap<? extends Object, Integer> rawCountsAB() {
		return countsAB;
	}

}
