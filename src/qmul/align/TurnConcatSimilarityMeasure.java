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

import qmul.corpus.DialogueSentence;
import qmul.corpus.DialogueTurn;
import qmul.util.parse.StanfordParser;
import qmul.util.similarity.SimilarityMeasure;
import qmul.util.treekernel.TreeKernel;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;

/**
 * A {@link SimilarityMeasure} for {@link DialogueTurn}s which treats them as the concatenation of their constituent
 * sentences: similarity between 2-sentence turns sim("A B","C D") = sim(A+B,C+D). NB this means that sim("A B","A B")
 * is equal to 1 regardless of whether A=B.
 * 
 * @author mpurver
 */
public class TurnConcatSimilarityMeasure implements SimilarityMeasure<DialogueTurn> {

	private SimilarityMeasure<DialogueSentence> sim;

	private boolean geometricMean = false;

	private HashMap<Object, Integer> countsA = new HashMap<Object, Integer>();
	private HashMap<Object, Integer> countsB = new HashMap<Object, Integer>();
	private HashMap<Object, Integer> countsAB = new HashMap<Object, Integer>();

	/**
	 * use the default {@link TreeKernel} kernel type (syn rule subtrees)
	 */
	public TurnConcatSimilarityMeasure(SimilarityMeasure<DialogueSentence> sim) {
		super();
		this.sim = sim;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#similarity(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double similarity(DialogueTurn a, DialogueTurn b) {
		return sim.similarity(concatTurn(a), concatTurn(b));
	}

	/**
	 * @param t
	 * @return the result of concatenating all sentences linearly for transcription, as daughters of a "TURN" mother for
	 *         syntax (unless there's just one, in which case it's just copied without a TURN mother, to prevent false
	 *         positive similarity between single-sent turns)
	 */
	private DialogueSentence concatTurn(DialogueTurn t) {
		DialogueSentence cs = new DialogueSentence(null, 0, t, "");
		System.out.print("Concatenating sentences for turn " + t.getId());
		for (DialogueSentence s : t.getSents()) {
			System.out.print(".");
			if (s.getTranscription() != null) {
				cs.setTranscription((cs.getTranscription() + " " + s.getTranscription()).trim());
			}
			if (s.getTokens() != null) {
				if (cs.getTokens() == null) {
					cs.setTokens(s.getTokens());
				} else {
					cs.getTokens().addAll(s.getTokens());
				}
			}
			if (s.getSyntax() != null) {
				Tree tree;
				if (cs.getSyntax() == null) {
					tree = s.getSyntax();
				} else {
					ArrayList<Tree> dtrs = new ArrayList<Tree>();
					if (cs.getSyntax().label().value().equals("TURN")) {
						for (Tree child : cs.getSyntax().getChildrenAsList()) {
							dtrs.add(child);
						}
					} else {
						dtrs.add(cs.getSyntax());
					}
					dtrs.add(s.getSyntax());
					tree = new LabeledScoredTreeNode(new StringLabel("TURN"), dtrs);
				}
				cs.setSyntax(tree);
			}
			if (!Double.isNaN(s.getSyntaxProb())) {
				cs.setSyntaxProb(Double.isNaN(cs.getSyntaxProb()) ? s.getSyntaxProb() : (cs.getSyntaxProb() * s
						.getSyntaxProb()));
			}
		}
		System.out.println(" done.");
		return cs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#reset()
	 */
	@Override
	public void reset() {
		sim.reset();
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

	/**
	 * @return the geometricMean
	 */
	public boolean isGeometricMean() {
		return geometricMean;
	}

	/**
	 * @param geometricMean
	 *            the geometricMean to set
	 */
	public void setGeometricMean(boolean geometricMean) {
		this.geometricMean = geometricMean;
	}

	public static void main(String[] args) {
		// TurnSimilarityMeasure sm = new TurnSimilarityMeasure(new SentenceLexicalSimilarityMeasure());
		TurnConcatSimilarityMeasure sm = new TurnConcatSimilarityMeasure(new SentenceSyntacticSimilarityMeasure());
		StanfordParser p = new StanfordParser();
		DialogueTurn a = new DialogueTurn("a", 1, null, null);
		DialogueTurn b = new DialogueTurn("b", 1, null, null);
		DialogueSentence a1 = new DialogueSentence("a1", 1, a, "ok");
		a.getSents().add(a1);
		DialogueSentence b1 = new DialogueSentence("b1", 1, b, "ok");
		b.getSents().add(b1);
		a1.setSyntax(p.parse(a1.getTranscription()));
		b1.setSyntax(p.parse(b1.getTranscription()));
		System.out.println("" + a + " " + a1.getSyntax() + "\n" + b + " " + b1.getSyntax() + "\n" + "sim = "
				+ sm.similarity(a, b));
		DialogueSentence a2 = new DialogueSentence("a2", 2, a, "ok");
		a.getSents().add(a2);
		a2.setSyntax(p.parse(a2.getTranscription()));
		System.out.println("" + a + " " + a1.getSyntax() + "\n" + b + " " + b1.getSyntax() + "\n" + "sim = "
				+ sm.similarity(a, b));
		a1.setTranscription("ok ok ok ok");
		a1.setSyntax(p.parse(a1.getTranscription()));
		System.out.println("" + a + " " + a1.getSyntax() + "\n" + b + " " + b1.getSyntax() + "\n" + "sim = "
				+ sm.similarity(a, b));
		a1.setTranscription("that's really not ok");
		a1.setSyntax(p.parse(a1.getTranscription()));
		System.out.println("" + a + " " + a1.getSyntax() + "\n" + b + " " + b1.getSyntax() + "\n" + "sim = "
				+ sm.similarity(a, b));
		b1.setTranscription("that's really not ok");
		b1.setSyntax(p.parse(b1.getTranscription()));
		System.out.println("" + a + " " + a1.getSyntax() + "\n" + b + " " + b1.getSyntax() + "\n" + "sim = "
				+ sm.similarity(a, b));
		a1.setTranscription("john likes the small bear");
		a1.setSyntax(p.parse(a1.getTranscription()));
		b1.setTranscription("jim hates the big rabbit");
		b1.setSyntax(p.parse(b1.getTranscription()));
		System.out.println("" + a + " " + a1.getSyntax() + "\n" + b + " " + b1.getSyntax() + "\n" + "sim = "
				+ sm.similarity(a, b) + "\n" + sm.rawCountsA() + "\n" + sm.rawCountsB() + "\n" + sm.rawCountsAB());
		a1.setTranscription("the man likes the small bear");
		a1.setSyntax(p.parse(a1.getTranscription()));
		System.out.println("" + a + " " + a1.getSyntax() + "\n" + b + " " + b1.getSyntax() + "\n" + "sim = "
				+ sm.similarity(a, b) + "\n" + sm.rawCountsA() + "\n" + sm.rawCountsB() + "\n" + sm.rawCountsAB());
	}

}
