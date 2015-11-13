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
import java.util.HashMap;
import java.util.HashSet;

import edu.stanford.nlp.trees.Tree;

/**
 * A single dialogue made up of several {@link DialogueTurn}s
 * 
 * @author mpurver
 */
public class Dialogue implements Serializable {

	private static final long serialVersionUID = 8642286173870170225L;

	private DialogueCorpus corpus;
	private String id;
	private String genre;
	private ArrayList<DialogueTurn> turns;
	private ArrayList<DialogueSentence> sents;
	private HashMap<Integer, Integer> sentIndices;
	private HashSet<DialogueSpeaker> speakers;
	private int turnNum;
	private int sentNum;
	private float startTime = Float.NaN;
	private float endTime = Float.NaN;

	/**
	 * @deprecated default constructor for serialization
	 */
	@Deprecated
	public Dialogue() {
		super();
		this.turns = new ArrayList<DialogueTurn>();
		this.turnNum = 0;
		this.sents = new ArrayList<DialogueSentence>();
		this.sentIndices = new HashMap<Integer, Integer>();
		this.sentNum = 0;
		this.speakers = new HashSet<DialogueSpeaker>();
	}

	/**
	 * Create a new {@link Dialogue} as part of the given {@link DialogueCorpus}
	 * 
	 * @param corpus
	 *            the {@link DialogueCorpus} that this dialogue belongs to
	 * @param id
	 *            some identifying name
	 * @param genre
	 *            some genre identifier (just use null if your corpus doesn't deal in genres)
	 */
	public Dialogue(DialogueCorpus corpus, String id, String genre) {
		this();
		this.corpus = corpus;
		this.id = id;
		this.genre = genre;
	}

	/**
	 * Create a new {@link DialogueTurn} and add it and the speaker to the appropriate lists
	 * 
	 * @param turnNum
	 *            the id number. If negative, do auto-numbering from 1 (or last explicitly set number)
	 * @param speaker
	 * @return the new turn
	 */
	public DialogueTurn addTurn(int turnNum, DialogueSpeaker speaker) {
		if (turnNum < 0) {
			turnNum = ++this.turnNum;
		} else {
			this.turnNum = turnNum;
		}
		DialogueTurn turn = new DialogueTurn(getId() + "T" + turnNum, turnNum, this, speaker);
		getTurns().add(turn);
		getSpeakers().add(speaker);
		return turn;
	}

	/**
	 * Remove this {@link DialogueTurn} and its constituent {@link DialogueSentence}s
	 * 
	 * @param turn
	 *            The turn to remove
	 * @return true if all removals succeeded
	 */
	public boolean removeTurn(DialogueTurn turn) {
		boolean success = true;
		for (DialogueSentence sent : new ArrayList<DialogueSentence>(turn.getSents())) {
			if (!getSents().remove(sent)) {
				success = false;
				System.out.println("Failed to remove sentence " + sent);
			}
		}
		if (!getTurns().remove(turn)) {
			success = false;
			System.out.println("Failed to remove turn " + turn);
		}
		DialogueSpeaker spk = turn.getSpeaker();
		boolean found = false;
		for (DialogueTurn t : getTurns()) {
			if (t.getSpeaker().equals(spk)) {
				found = true;
				break;
			}
		}
		if (!found) {
			if (!getSpeakers().remove(spk)) {
				success = false;
				System.out.println("Failed to remove speaker " + spk);
			}
		}
		return success;
	}

	/**
	 * Create a new {@link DialogueSentence} and add it to the appropriate lists
	 * 
	 * @param sentNum
	 *            the id number. If negative, do auto-numbering from 1 (or last explicitly set number)
	 * @param turn
	 *            the {@link DialogueTurn} that this sentence is part of
	 * @param transcription
	 *            the sentence transcription
	 * @param tree
	 *            the syntactic tree (set to null if not available)
	 * @return the new sentence
	 */
	public DialogueSentence addSent(int sentNum, DialogueTurn turn, String transcription, Tree tree) {
		if (sentNum < 0) {
			sentNum = ++this.sentNum;
		} else {
			this.sentNum = sentNum;
		}
		DialogueSentence sent = new DialogueSentence(getId() + "S" + sentNum, sentNum, turn, transcription, tree);
		turn.getSents().add(sent);
		getSentIndices().put(sentNum, getSents().size());
		getSents().add(sent);
		return sent;
	}

	public DialogueCorpus getCorpus() {
		return corpus;
	}

	public String getId() {
		return id;
	}

	public String getGenre() {
		return genre;
	}

	public ArrayList<DialogueTurn> getTurns() {
		return turns;
	}

	public ArrayList<DialogueSentence> getSents() {
		return sents;
	}

	/**
	 * @return the sentIndices
	 */
	public HashMap<Integer, Integer> getSentIndices() {
		return sentIndices;
	}

	public HashSet<DialogueSpeaker> getSpeakers() {
		return speakers;
	}

	/**
	 * @param sentNum
	 * @return the {@link DialogueSentence} with this (original corpus) number. If you just want the Nth sentence, use
	 *         getSents().get(N)
	 */
	public DialogueSentence getSent(int sentNum) {
		if (sentIndices.isEmpty()) {
			setupSentIndices();
		}
		return sents.get(sentIndices.get(sentNum));
	}

	/**
	 * Fill the sentIndices map with original corpus number -> index in sents list
	 */
	public void setupSentIndices() {
		if (!sentIndices.isEmpty()) {
			System.err.println("WARNING: resetting existing sentIndices");
		}
		for (int i = 0; i < sents.size(); i++) {
			sentIndices.put(sents.get(i).getNum(), i);
		}
	}

	public int numTurns() {
		return turns.size();
	}

	public int numSents() {
		return sents.size();
	}

	public int numSpeakers() {
		return speakers.size();
	}

	/**
	 * @return the total number of words (tokens) in the dialogue based on the standard transcription
	 */
	public int numWords() {
		int n = 0;
		for (DialogueSentence s : sents) {
			n += s.numWords();
		}
		return n;
	}

	/**
	 * @return the total number of words (tokens) in the dialogue based on the tokenised transcription
	 */
	public int numTokens() {
		int n = 0;
		for (DialogueSentence s : sents) {
			n += s.numTokens();
		}
		return n;
	}

	/**
	 * @param corpus
	 *            the corpus to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setCorpus(DialogueCorpus corpus) {
		this.corpus = corpus;
	}

	/**
	 * @param id
	 *            the id to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param genre
	 *            the genre to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setGenre(String genre) {
		this.genre = genre;
	}

	/**
	 * @param turns
	 *            the turns to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setTurns(ArrayList<DialogueTurn> turns) {
		this.turns = turns;
	}

	/**
	 * @param sents
	 *            the sents to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setSents(ArrayList<DialogueSentence> sents) {
		this.sents = sents;
	}

	/**
	 * @param sentIndices
	 *            the sentIndices to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setSentIndices(HashMap<Integer, Integer> sentIndices) {
		this.sentIndices = sentIndices;
	}

	/**
	 * @param speakers
	 *            the speakers to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setSpeakers(HashSet<DialogueSpeaker> speakers) {
		this.speakers = speakers;
	}

	/**
	 * @return the startTime
	 */
	public float getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setStartTime(float startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public float getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setEndTime(float endTime) {
		this.endTime = endTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = "Dialogue " + getId() + "\n---BEGIN---\n";
		for (DialogueTurn t : getTurns()) {
			s += t + "\n";
		}
		return s + "\n---END---\n";
	}

}
