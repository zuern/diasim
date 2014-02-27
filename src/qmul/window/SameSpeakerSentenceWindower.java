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
package qmul.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import qmul.corpus.Dialogue;
import qmul.corpus.DialogueSentence;
import qmul.corpus.DialogueSpeaker;

/**
 * A {@link DialogueWindower} based on {@link DialogueSentence} sentences - both leftWindow and rightWindow contain only
 * sentences from the speaker at the current index
 * 
 * @author mpurver
 */
public class SameSpeakerSentenceWindower extends SentenceWindower {

	public SameSpeakerSentenceWindower(Dialogue dialogue, int left, int right, int step) {
		super(dialogue, left, right, step);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diet.utils.corpus.DialogueWindower#getLeftWindow()
	 */
	@Override
	public List<DialogueSentence> getLeftWindow() {
		// for left window, must go backwards collecting turns, then reverse the list
		ArrayList<DialogueSentence> sents = new ArrayList<DialogueSentence>();
		int index = getIndex();
		DialogueSpeaker speaker = getDialogue().getSents().get(index).getSpeaker();
		while ((sents.size() < getLeftWindowSize()) && (index > 0)) {
			--index;
			DialogueSentence sent = getDialogue().getSents().get(index);
			if (sent.getSpeaker().equals(speaker)) {
				sents.add(sent);
			}
		}
		Collections.reverse(sents);
		return sents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diet.utils.corpus.DialogueWindower#getRightWindow()
	 */
	@Override
	public List<DialogueSentence> getRightWindow() {
		// for right window, can just go forwards
		ArrayList<DialogueSentence> sents = new ArrayList<DialogueSentence>();
		int index = getIndex();
		DialogueSpeaker speaker = getDialogue().getSents().get(index).getSpeaker();
		while ((sents.size() < getRightWindowSize()) && (index < length())) {
			DialogueSentence sent = getDialogue().getSents().get(index);
			if (sent.getSpeaker().equals(speaker)) {
				sents.add(sent);
			}
			++index;
		}
		return sents;
	}

}
