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

import java.util.Random;

import qmul.corpus.DialogueSentence;
import qmul.util.similarity.StringSimilarityMeasure;
import edu.stanford.nlp.ling.HasWord;

/**
 * Lexical similarity for two sentences, using {@link StringSimilarityMeasure} on tokens not standard transcriptions
 * (i.e. as used for parsing) but with UNCLEAR given a random integer suffix so it will never match another UNCLEAR
 * 
 * @author mpurver
 */
public class SentenceLexicalTokenSimilarityMeasure extends SentenceLexicalSimilarityMeasure {

	Random random = new Random();

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.align.SentenceLexicalSimilarityMeasure#getRelevantString(qmul.corpus.DialogueSentence)
	 */
	@Override
	protected String getRelevantString(DialogueSentence s) {
		String str = "";
		if (s.getTokens() == null) {
			return str;
		}
		for (HasWord tok : s.getTokens()) {
			str += " " + tok.word() + (tok.word().equals("UNCLEAR") ? random.nextInt() : "");
		}
		return str.trim();
	}
}
