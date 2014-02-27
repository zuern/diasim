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
import qmul.util.similarity.SimilarityMeasure;
import qmul.util.similarity.StringSimilarityMeasure;

/**
 * Lexical similarity for two sentences, using {@link StringSimilarityMeasure} on transcriptions
 * 
 * @author mpurver
 */
public class SentenceLexicalSimilarityMeasure implements SimilarityMeasure<DialogueSentence> {

	StringSimilarityMeasure sim;

	public SentenceLexicalSimilarityMeasure() {
		super();
		sim = new StringSimilarityMeasure();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#similarity(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double similarity(DialogueSentence a, DialogueSentence b) {
		String t1 = getRelevantString(a);
		String t2 = getRelevantString(b);
		return sim.similarity(t1, t2);
	}

	/**
	 * @param s
	 * @return the transcription (other classes may override this)
	 */
	protected String getRelevantString(DialogueSentence s) {
		return s.getTranscription();
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
		return sim.rawCountsA();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#rawCountsB()
	 */
	@Override
	public HashMap<? extends Object, Integer> rawCountsB() {
		return sim.rawCountsB();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#rawCountsAB()
	 */
	@Override
	public HashMap<? extends Object, Integer> rawCountsAB() {
		return sim.rawCountsAB();
	}

	public static void main(String[] args) {
		SentenceLexicalSimilarityMeasure sm = new SentenceLexicalSimilarityMeasure();
		DialogueTurn t = new DialogueTurn("t", 1, null, null);
		DialogueSentence a = new DialogueSentence("a", 1, t, "ok");
		DialogueSentence b = new DialogueSentence("b", 1, t, "ok");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		a.setTranscription("ok ok ok ok");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		a.setTranscription("that's really not ok");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		b.setTranscription("that's really not ok");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		System.out.println(sm.rawCountsA() + "\n" + sm.rawCountsB() + "\n" + sm.rawCountsAB());
		a.setTranscription("the man likes the small bear");
		b.setTranscription("jim hates the big rabbit");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		System.out.println(sm.rawCountsA() + "\n" + sm.rawCountsB() + "\n" + sm.rawCountsAB());
		
		a.setTranscription("Is larger, the upstairs one is slightly larger.");
		b.setTranscription("The upstairs one's larger?");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		System.out.println(sm.rawCountsA() + "\n" + sm.rawCountsB() + "\n" + sm.rawCountsAB());

		a.setTranscription("Well off we go, back to work.");
		b.setTranscription("Won't use so much petrol from now on anyway, to get to work will we?");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		System.out.println(sm.rawCountsA() + "\n" + sm.rawCountsB() + "\n" + sm.rawCountsAB());

		a.setTranscription("And it's Eileen's anniversary as well today.");
		b.setTranscription("Oh bugger Eileen!");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		System.out.println(sm.rawCountsA() + "\n" + sm.rawCountsB() + "\n" + sm.rawCountsAB());

		a.setTranscription("Yeah but I can't afford that kind of thing");
		b.setTranscription("How much could you afford");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		System.out.println(sm.rawCountsA() + "\n" + sm.rawCountsB() + "\n" + sm.rawCountsAB());

		a.setTranscription("Have you got all the vowels");
		b.setTranscription("I’ve got nothing but vowels");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		System.out.println(sm.rawCountsA() + "\n" + sm.rawCountsB() + "\n" + sm.rawCountsAB());

		a.setTranscription("Oh this is ridiculous");
		b.setTranscription("I’ve got nothing but vowels");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		System.out.println(sm.rawCountsA() + "\n" + sm.rawCountsB() + "\n" + sm.rawCountsAB());

		a.setTranscription("Oh this is ridiculous Have you got all the vowels ");
		b.setTranscription("I’ve got nothing but vowels");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		System.out.println(sm.rawCountsA() + "\n" + sm.rawCountsB() + "\n" + sm.rawCountsAB());
	}

}
