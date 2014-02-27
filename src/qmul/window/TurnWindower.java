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
import java.util.List;

import qmul.corpus.Dialogue;
import qmul.corpus.DialogueTurn;

/**
 * A {@link DialogueWindower} based on {@link DialogueTurn} speaker turns
 * 
 * @author mpurver
 */
public class TurnWindower extends DialogueWindower<DialogueTurn> {

	public TurnWindower(Dialogue dialogue, int left, int right, int step) {
		super(dialogue, left, right, step);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diet.utils.corpus.DialogueWindower#length()
	 */
	@Override
	public int length() {
		return getDialogue().getTurns().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diet.utils.corpus.DialogueWindower#getLeftWindow()
	 */
	@Override
	public List<DialogueTurn> getLeftWindow() {
		ArrayList<DialogueTurn> turns = new ArrayList<DialogueTurn>();
		int start = getIndex() > getLeftWindowSize() ? getIndex() - getLeftWindowSize() : 0;
		int end = getIndex();
		for (DialogueTurn turn : getDialogue().getTurns().subList(start, end)) {
			turns.add(turn);
		}
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
		int start = getIndex();
		int end = getIndex() < (length() - getRightWindowSize()) ? getIndex() + getRightWindowSize() : length();
		for (DialogueTurn turn : getDialogue().getTurns().subList(start, end)) {
			turns.add(turn);
		}
		return turns;
	}

}
