/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package qmul.util.similarity;

import java.util.HashMap;

public interface SimilarityMeasure<X> {

	/**
	 * @param a
	 * @param b
	 * @return the similarity value between a and b
	 */
	double similarity(X a, X b);

	/**
	 * Do any resetting that needs doing for a new corpus/dialogue
	 */
	void reset();

	/**
	 * @return a map of relevant objects (strings, syntactic constructions etc) to raw counts seen in LHS units so far
	 */
	HashMap<? extends Object, Integer> rawCountsA();

	/**
	 * @return a map of relevant objects (strings, syntactic constructions etc) to raw counts seen in RHS units so far
	 */
	HashMap<? extends Object, Integer> rawCountsB();

	/**
	 * @return a map of relevant objects (strings, syntactic constructions etc) to raw counts seen in common (LHS & RHS)
	 *         units so far
	 */
	HashMap<? extends Object, Integer> rawCountsAB();

}
