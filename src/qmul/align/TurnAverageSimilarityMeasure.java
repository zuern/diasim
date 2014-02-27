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
import qmul.util.MapUtil;
import qmul.util.parse.StanfordParser;
import qmul.util.similarity.SimilarityMeasure;
import qmul.util.treekernel.TreeKernel;

/**
 * A {@link SimilarityMeasure} for {@link DialogueTurn}s which averages over the cross-product of the sentence-wise
 * similarities: similarity between 2-sentence turns sim("A B","C D") = average( sim(A,C), sim(A,D), sim(B,C), sim(B,D)
 * ). NB this means that sim("A B","A B") is not equal to 1 unless A=B.
 * 
 * @author mpurver
 */
public class TurnAverageSimilarityMeasure implements SimilarityMeasure<DialogueTurn> {

	private SimilarityMeasure<DialogueSentence> sim;

	private boolean geometricMean = false;

	private HashMap<Object, Integer> countsA = new HashMap<Object, Integer>();
	private HashMap<Object, Integer> countsB = new HashMap<Object, Integer>();
	private HashMap<Object, Integer> countsAB = new HashMap<Object, Integer>();

	/**
	 * use the default {@link TreeKernel} kernel type (syn rule subtrees)
	 */
	public TurnAverageSimilarityMeasure(SimilarityMeasure<DialogueSentence> sim) {
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
		double sum = 0.0;
		double prd = 1.0;
		double n = 0.0;
		countsA.clear();
		countsB.clear();
		countsAB.clear();
		boolean counted = false;
		for (DialogueSentence s1 : a.getSents()) {
			for (DialogueSentence s2 : b.getSents()) {
				double score = sim.similarity(s1, s2);
				sum += score;
				prd *= score;
				n++;
				if (!counted) {
					MapUtil.addAll(countsB, sim.rawCountsB());
				}
				MapUtil.addAll(countsAB, sim.rawCountsAB());
			}
			counted = true;
			MapUtil.addAll(countsA, sim.rawCountsA());
		}
		return (geometricMean ? Math.pow(prd, 1 / n) : (sum / n));
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
		TurnAverageSimilarityMeasure sm = new TurnAverageSimilarityMeasure(new SentenceSyntacticSimilarityMeasure());
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
