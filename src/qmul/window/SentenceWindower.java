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
import qmul.corpus.DialogueSentence;

/**
 * A {@link DialogueWindower} based on {@link DialogueSentence} sentences
 * 
 * @author mpurver
 */
public class SentenceWindower extends DialogueWindower<DialogueSentence> {

	public SentenceWindower(Dialogue dialogue, int left, int right, int step) {
		super(dialogue, left, right, step);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diet.utils.corpus.DialogueWindower#length()
	 */
	@Override
	public int length() {
		return getDialogue().getSents().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diet.utils.corpus.DialogueWindower#getLeftWindow()
	 */
	@Override
	public List<DialogueSentence> getLeftWindow() {
		ArrayList<DialogueSentence> sents = new ArrayList<DialogueSentence>();
		int start = getIndex() > getLeftWindowSize() ? getIndex() - getLeftWindowSize() : 0;
		int end = getIndex();
		for (DialogueSentence sent : getDialogue().getSents().subList(start, end)) {
			sents.add(sent);
		}
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
		int start = getIndex();
		int end = getIndex() < (length() - getRightWindowSize()) ? getIndex() + getRightWindowSize() : length();
		for (DialogueSentence sent : getDialogue().getSents().subList(start, end)) {
			sents.add(sent);
		}
		return sents;
	}

}
