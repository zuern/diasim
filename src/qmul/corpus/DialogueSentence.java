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
package qmul.corpus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.Tree;

/**
 * A sentence within a {@link DialogueTurn} speaker turn. May or may not have syntactic annotation
 * 
 * @author mpurver
 */
public class DialogueSentence extends DialogueUnit implements Serializable {

	// private static final long serialVersionUID = -1316541675361610716L;
	private static final long serialVersionUID = 8552331449948881591L;

	private DialogueTurn turn;

	private String transcription;

	private ArrayList<HasWord> tokens;

	private ArrayList<TaggedWord> taggedWords;

	private ArrayList<TaggedWord> taggedLemmas;

	private Tree syntax;

	private double syntaxProb;

	/**
	 * @deprecated just for serialization, use full constructor instead
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private DialogueSentence() {
		super();
	}

	public DialogueSentence(String id, int num, DialogueTurn turn, String transcription) {
		super(id, num, turn.getDialogue(), turn.getSpeaker());
		this.turn = turn;
		this.transcription = transcription;
		this.syntax = null;
		this.syntaxProb = Double.NaN;
		this.tokens = null;
		this.taggedWords = null;
		this.taggedLemmas = null;
	}

	public DialogueSentence(String id, int num, DialogueTurn turn, String transcription, Tree syntax) {
		this(id, num, turn, transcription);
		this.syntax = syntax;
	}

	public DialogueSentence(String id, int num, DialogueTurn turn, String transcription, Tree syntax, double syntaxProb) {
		this(id, num, turn, transcription, syntax);
		this.syntaxProb = syntaxProb;
	}

	public Tree getSyntax() {
		return syntax;
	}

	public void setSyntax(Tree syntax) {
		this.syntax = syntax;
	}

	public double getSyntaxProb() {
		return syntaxProb;
	}

	public void setSyntaxProb(double syntaxProb) {
		this.syntaxProb = syntaxProb;
	}

	public String getTranscription() {
		return transcription;
	}

	/**
	 * @param transcription
	 *            the transcription to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setTranscription(String transcription) {
		this.transcription = transcription;
	}

	public DialogueTurn getTurn() {
		return turn;
	}

	/**
	 * @param turn
	 *            the turn to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setTurn(DialogueTurn turn) {
		this.turn = turn;
	}

	/**
	 * @return the tokenised transcription (for use e.g. by parsers). This should exclude transcriber notes, non-vocal
	 *         sounds etc
	 */
	public ArrayList<HasWord> getTokens() {
		return tokens;
	}

	/**
	 * @param tokens
	 *            the tokenised transcription (for use e.g. by parsers). This should exclude transcriber notes,
	 *            non-vocal sounds etc
	 */
	public void setTokens(List<? extends HasWord> tokens) {
		this.tokens = new ArrayList<HasWord>(tokens);
	}

	/**
	 * @return the PoS-tagged and tokenised transcription. This should exclude transcriber notes, non-vocal sounds etc.
	 *         Not just a reference to getTokens() as we may want to leave more out here e.g. UNCLEAR words the
	 */
	public ArrayList<TaggedWord> getTaggedWords() {
		return taggedWords;
	}

	/**
	 * @param taggedWords
	 *            the PoS-tagged and tokenised transcription. This should exclude transcriber notes, non-vocal sounds
	 *            etc. Not just a reference to getTokens() as we may want to leave more out here e.g. UNCLEAR words the
	 */
	public void setTaggedWords(List<? extends TaggedWord> taggedWords) {
		this.taggedWords = new ArrayList<TaggedWord>(taggedWords);
	}

	/**
	 * @return the PoS-tagged and stemmed transcription. This should exclude transcriber notes, non-vocal sounds etc.
	 *         Not just a reference to getTokens() as we may want to leave more out here e.g. UNCLEAR words the
	 */
	public ArrayList<TaggedWord> getTaggedLemmas() {
		return taggedLemmas;
	}

	/**
	 * @param taggedLemmas
	 *            the PoS-tagged and stemmed transcription. This should exclude transcriber notes, non-vocal sounds etc.
	 *            Not just a reference to getTokens() as we may want to leave more out here e.g. UNCLEAR words the
	 */
	public void setTaggedLemmas(List<? extends TaggedWord> taggedLemmas) {
		this.taggedLemmas = new ArrayList<TaggedWord>(taggedLemmas);
	}

	/**
	 * @return the number of words (tokens) in the standard transcription. TODO current splits transcription - need to
	 *         remove transcriber notes, non-vocal sounds etc
	 */
	public int numWords() {
		return transcription.split("\\s+").length;
	}

	/**
	 * @return the number of words (tokens) in the tokenised transcription. This should exclude transcriber notes,
	 *         non-vocal sounds etc
	 */
	public int numTokens() {
		return ((tokens == null) ? 0 : tokens.size());
	}

	/**
	 * @return the number of PoS-tagged words in the tagged-tokenised transcription. This should exclude transcriber
	 *         notes, non-vocal sounds etc
	 */
	public int numTaggedWords() {
		return ((taggedWords == null) ? 0 : taggedWords.size());
	}

	/**
	 * @return the number of PoS-tagged words in the tagged-stemmed transcription. This should exclude transcriber
	 *         notes, non-vocal sounds etc
	 */
	public int numTaggedLemmas() {
		return ((taggedLemmas == null) ? 0 : taggedLemmas.size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getNum() + ": " + getTranscription();
	}

}
