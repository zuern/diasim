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
import java.util.HashMap;
import java.util.List;

import qmul.corpus.Dialogue;
import qmul.corpus.DialogueSentence;
import qmul.corpus.DialogueSpeaker;

/**
 * A {@link DialogueWindower} based on {@link DialogueSentence} sentences - rightWindow contains the sentence at the
 * current index, leftWindow contains all sentences from all speakers other than the speaker at the current index
 * 
 * @author mpurver
 */
public class OtherSpeakerAllOtherSentenceWindower extends SentenceWindower {

	private HashMap<DialogueSpeaker, ArrayList<DialogueSentence>> speakerSents;

	public OtherSpeakerAllOtherSentenceWindower(Dialogue dialogue, int left, int right, int step) {
		super(dialogue, left, right, step);
	}

	private void init() {
		if (speakerSents == null) {
			speakerSents = new HashMap<DialogueSpeaker, ArrayList<DialogueSentence>>();
		} else {
			for (DialogueSpeaker speaker : speakerSents.keySet()) {
				speakerSents.get(speaker).clear();
			}
			speakerSents.clear();
		}
		if (getDialogue() != null) {
			for (DialogueSpeaker speaker : getDialogue().getSpeakers()) {
				speakerSents.put(speaker, new ArrayList<DialogueSentence>());
			}
			for (DialogueSentence sent : getDialogue().getSents()) {
				for (DialogueSpeaker speaker : getDialogue().getSpeakers()) {
					if (!speaker.equals(sent.getSpeaker())) {
						speakerSents.get(sent.getSpeaker()).add(sent);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.window.DialogueWindower#setDialogue(qmul.corpus.Dialogue)
	 */
	@Override
	public void setDialogue(Dialogue dialogue) {
		super.setDialogue(dialogue);
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diet.utils.corpus.DialogueWindower#getLeftWindow()
	 */
	@Override
	public List<DialogueSentence> getLeftWindow() {
		DialogueSentence sent = getDialogue().getSents().get(getIndex());
		DialogueSpeaker speaker = sent.getSpeaker();
		ArrayList<DialogueSentence> sents = new ArrayList<DialogueSentence>(speakerSents.get(speaker));
		sents.remove(sent);
		return sents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diet.utils.corpus.DialogueWindower#getRightWindow()
	 */
	@Override
	public List<DialogueSentence> getRightWindow() {
		ArrayList<DialogueSentence> sents = new ArrayList<DialogueSentence>();
		sents.add(getDialogue().getSents().get(getIndex()));
		return sents;
	}

}
