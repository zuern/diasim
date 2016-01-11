/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package qmul.util.parse;

import java.util.List;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordTokenFactory;

/**
 * A wrapper for the Stanford {@link PennTreebankTokenizer} which fixes the latter's behaviour with certain
 * abbreviations (e.g. it won't separate punctuation from words that might be abbreviations, e.g. "No." even if they're
 * in single-word utterances or sentence-final)
 * 
 * @author mpurver
 */
public class PennTreebankTokenizer {

	// private DocumentPreprocessor dp = new DocumentPreprocessor(new PennTreebankLanguagePack().getTokenizerFactory());
	private DocumentPreprocessor dp = new DocumentPreprocessor(PTBTokenizer.PTBTokenizerFactory.newPTBTokenizerFactory(
			new WordTokenFactory(), "americanize=false"));

	private boolean splitPennAbbreviations = false;

	/**
	 * @param splitPennAbbreviations
	 *            false means behave exactly like the Stanford {@link PennTreebankTokenizer}, true means try to split
	 *            sentence-final punctuation
	 */
	public PennTreebankTokenizer(boolean splitPennAbbreviations) {
		this.splitPennAbbreviations = splitPennAbbreviations;
	}

	/**
	 * @param s
	 * @return a list of tokens "hello, bob." -> "[hello, ,, bob, .]"
	 */
	public List<Word> getWordsFromString(String s) {
		if (splitPennAbbreviations) {
			s = s.replaceAll("(\\w+)([.,?!;:])(\\s+|$)", "$1 $2");
		}
		List<Word> words = dp.getWordsFromString(s);
		for (Word w : words) {
			// Penn tokeniser transforms "a/b" into "a\/b"
			if (w.word().contains("/")) {
				w.setWord(w.word().replaceAll("\\\\/", "/"));
			}
		}
		return words;
	}

	/**
	 * @param s
	 * @return a tokenized version "hello, bob." -> "hello , bob ."
	 */
	public String getTokenizedString(String s) {
		String t = "";
		for (Word w : getWordsFromString(s)) {
			t += w + " ";
		}
		return t.substring(0, t.length() - 1);
	}

}
