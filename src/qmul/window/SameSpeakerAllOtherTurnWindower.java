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
import qmul.corpus.DialogueSpeaker;
import qmul.corpus.DialogueTurn;

/**
 * A {@link DialogueWindower} based on {@link DialogueTurn} speaker turns - rightWindow contains the turn at the current
 * index, leftWindow contains all other turns from the speaker at the current index
 * 
 * @author mpurver
 */
public class SameSpeakerAllOtherTurnWindower extends TurnWindower {

	private HashMap<DialogueSpeaker, ArrayList<DialogueTurn>> speakerTurns;

	public SameSpeakerAllOtherTurnWindower(Dialogue dialogue, int left, int right, int step) {
		super(dialogue, left, right, step);
	}

	private void init() {
		if (speakerTurns == null) {
			speakerTurns = new HashMap<DialogueSpeaker, ArrayList<DialogueTurn>>();
		} else {
			for (DialogueSpeaker speaker : speakerTurns.keySet()) {
				speakerTurns.get(speaker).clear();
			}
			speakerTurns.clear();
		}
		if (getDialogue() != null) {
			for (DialogueSpeaker speaker : getDialogue().getSpeakers()) {
				speakerTurns.put(speaker, new ArrayList<DialogueTurn>());
			}
			for (DialogueTurn turn : getDialogue().getTurns()) {
				speakerTurns.get(turn.getSpeaker()).add(turn);
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
	public List<DialogueTurn> getLeftWindow() {
		DialogueTurn turn = getDialogue().getTurns().get(getIndex());
		DialogueSpeaker speaker = turn.getSpeaker();
		ArrayList<DialogueTurn> turns = new ArrayList<DialogueTurn>(speakerTurns.get(speaker));
		turns.remove(turn);
		return turns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diet.utils.corpus.DialogueWindower#getRightWindow()
	 */
	@Override
	public List<DialogueTurn> getRightWindow() {
		ArrayList<DialogueTurn> turns = new ArrayList<DialogueTurn>();
		turns.add(getDialogue().getTurns().get(getIndex()));
		return turns;
	}

}
