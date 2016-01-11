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

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Word;

/**
 * A speaker turn. May be made up of zero or more {@link DialogueSentence}s
 * 
 * @author mpurver
 */
public class DialogueWord<W extends Word> extends DialogueUnit implements HasWord, Serializable {

	private static final long serialVersionUID = 7838291201401444920L;

	private W word;

	/**
	 * @deprecated just for serialization, use full constructor instead
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private DialogueWord() {
		super();
	}

	public DialogueWord(String id, int num, Dialogue dialogue, DialogueSpeaker speaker, W word) {
		super(id, num, dialogue, speaker);
		this.word = word;
	}

	public W getWord() {
		return word;
	}

	/**
	 * @param word
	 *            the word to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setWord(W word) {
		this.word = word;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = word + "," + getStartTime() + "-" + getEndTime();
		return s;
	}

	@Override
	public String word() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWord(String word) {
		this.word.setWord(word);
	}

}
