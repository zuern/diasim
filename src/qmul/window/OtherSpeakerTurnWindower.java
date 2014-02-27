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
import qmul.corpus.DialogueSpeaker;
import qmul.corpus.DialogueTurn;

/**
 * A {@link DialogueWindower} based on {@link DialogueTurn} speaker turns - rightWindow contains only turns from the
 * speaker at the current index, leftWindow contains only turns from other speakers
 * 
 * @author mpurver
 */
public class OtherSpeakerTurnWindower extends TurnWindower {

	public OtherSpeakerTurnWindower(Dialogue dialogue, int left, int right, int step) {
		super(dialogue, left, right, step);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diet.utils.corpus.DialogueWindower#getLeftWindow()
	 */
	@Override
	public List<DialogueTurn> getLeftWindow() {
		// for left window, must go backwards collecting turns, then reverse the list
		ArrayList<DialogueTurn> turns = new ArrayList<DialogueTurn>();
		int index = getIndex();
		DialogueSpeaker speaker = getDialogue().getTurns().get(index).getSpeaker();
		while ((turns.size() < getLeftWindowSize()) && (index > 0)) {
			--index;
			DialogueTurn turn = getDialogue().getTurns().get(index);
			if (!turn.getSpeaker().equals(speaker)) {
				turns.add(turn);
			}
		}
		Collections.reverse(turns);
		return turns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diet.utils.corpus.DialogueWindower#getRightWindow()
	 */
	@Override
	public List<DialogueTurn> getRightWindow() {
		// for right window, can just go forwards
		ArrayList<DialogueTurn> turns = new ArrayList<DialogueTurn>();
		int index = getIndex();
		DialogueSpeaker speaker = getDialogue().getTurns().get(index).getSpeaker();
		while ((turns.size() < getRightWindowSize()) && (index < length())) {
			DialogueTurn turn = getDialogue().getTurns().get(index);
			if (turn.getSpeaker().equals(speaker)) {
				turns.add(turn);
			}
			++index;
		}
		return turns;
	}

}
