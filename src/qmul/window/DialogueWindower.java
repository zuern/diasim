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

import java.util.List;

import qmul.corpus.Dialogue;
import qmul.corpus.DialogueUnit;

/**
 * 
 * @author mpurver
 */
public abstract class DialogueWindower<D extends DialogueUnit> {

	private Dialogue dialogue;

	private int index;

	private int left;

	private int right;

	private int step;

	public DialogueWindower(Dialogue dialogue, int left, int right, int step) {
		this.left = left;
		this.right = right;
		this.step = step;
		setDialogue(dialogue);
	}

	public abstract List<D> getLeftWindow();

	public abstract List<D> getRightWindow();

	/**
	 * @return the length of the dialogue, in the relevant units (e.g. sentences, turns)
	 */
	public abstract int length();

	/**
	 * Advance the pointer by the step size
	 * 
	 * @return true if pointer is still within the length of the dialogue, false otherwise
	 */
	public boolean advance() {
		index += step;
		return (index < length());
	}

	/**
	 * The current position of the pointer
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Set the current position of the pointer
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Reset the pointer to the beginning of the dialogue
	 */
	public void resetIndex() {
		setIndex(0);
	}

	/**
	 * The dialogue to window
	 */
	public Dialogue getDialogue() {
		return dialogue;
	}

	/**
	 * Set a new dialogue to window, and reset the pointer
	 * 
	 * @param dialogue
	 */
	public void setDialogue(Dialogue dialogue) {
		this.dialogue = dialogue;
		resetIndex();
	}

	/**
	 * The size of the backward-looking window (in relevant units)
	 */
	public int getLeftWindowSize() {
		return left;
	}

	/**
	 * The size of the forward-looking window (in relevant units)
	 */
	public int getRightWindowSize() {
		return right;
	}

	/**
	 * The step size (in relevant units)
	 */
	public int getStepSize() {
		return step;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getClass().getName() + "(" + left + "," + right + "," + step + ")";
	}

}
