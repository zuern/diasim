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

/**
 * A speaker turn. May be made up of zero or more {@link DialogueSentence}s
 * 
 * @author mpurver
 */
public class DialogueTurn extends DialogueUnit implements Serializable {

	private static final long serialVersionUID = -8070986570621790073L;

	private ArrayList<DialogueSentence> sents;

	/**
	 * @deprecated just for serialization, use full constructor instead
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private DialogueTurn() {
		super();
	}

	public DialogueTurn(String id, int num, Dialogue dialogue, DialogueSpeaker speaker) {
		super(id, num, dialogue, speaker);
		this.sents = new ArrayList<DialogueSentence>();
	}

	public ArrayList<DialogueSentence> getSents() {
		return sents;
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
	 * @return the number of words (tokens) in the standard transcription. TODO current splits transcription - need to
	 *         remove transcriber notes, non-vocal sounds etc
	 */
	public int numWords() {
		int n = 0;
		for (DialogueSentence s : sents) {
			n += s.numWords();
		}
		return n;
	}

	/**
	 * @return the number of words (tokens) in the tokenised transcription. This should exclude transcriber notes,
	 *         non-vocal sounds etc
	 */
	public int numTokens() {
		int n = 0;
		for (DialogueSentence s : sents) {
			n += s.numTokens();
		}
		return n;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = getId() + ":";
		for (DialogueSentence sent : getSents()) {
			s += "\n" + sent;
		}
		return s;
	}

}
