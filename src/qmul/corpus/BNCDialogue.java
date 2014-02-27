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

import java.util.ArrayList;
import java.util.HashSet;

/**
 * a {@link Dialogue} in the {@link BNCCorpus} - adds handling for the BNC "unknown speaker" PSUNK and "unknown group"
 * PSUGP speaker IDs
 * 
 * @author mpurver
 */
public class BNCDialogue extends Dialogue {

	private static final long serialVersionUID = 890057464782496041L;

	private ArrayList<DialogueTurn> unknownTurns;
	private ArrayList<DialogueTurn> knownTurns;

	private HashSet<DialogueSpeaker> unknownSpeakers;
	private HashSet<DialogueSpeaker> knownSpeakers;

	public BNCDialogue(DialogueCorpus corpus, String id, String genre) {
		super(corpus, id, genre);
		unknownSpeakers = new HashSet<DialogueSpeaker>();
		knownSpeakers = new HashSet<DialogueSpeaker>();
		unknownTurns = new ArrayList<DialogueTurn>();
		knownTurns = new ArrayList<DialogueTurn>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.Dialogue#addTurn(int, qmul.corpus.DialogueSpeaker)
	 */
	@Override
	public DialogueTurn addTurn(int turnNum, DialogueSpeaker speaker) {
		DialogueTurn turn = null;
		if (BNCCorpus.isUnknown(speaker)) {
			// System.out.println("speaker is unknown " + speaker);
			if (!BNCCorpus.removeUnknowns) {
				turn = super.addTurn(turnNum, speaker);
			}
			unknownTurns.add(turn);
			unknownSpeakers.add(speaker);
		} else {
			// System.out.println("speaker is known " + speaker);
			turn = super.addTurn(turnNum, speaker);
			knownTurns.add(turn);
			knownSpeakers.add(speaker);
		}
		return turn;
	}

	/**
	 * @return the unknownTurns
	 */
	public ArrayList<DialogueTurn> getUnknownTurns() {
		return unknownTurns;
	}

	/**
	 * @return the knownTurns
	 */
	public ArrayList<DialogueTurn> getKnownTurns() {
		return knownTurns;
	}

	/**
	 * @return the unknownSpeakers
	 */
	public HashSet<DialogueSpeaker> getUnknownSpeakers() {
		return unknownSpeakers;
	}

	/**
	 * @return the knownSpeakers
	 */
	public HashSet<DialogueSpeaker> getKnownSpeakers() {
		return knownSpeakers;
	}

	public int numKnownTurns() {
		return knownTurns.size();
	}

	public int numKnownSpeakers() {
		return knownSpeakers.size();
	}

	public int numUnknownTurns() {
		return unknownTurns.size();
	}

	public int numUnknownSpeakers() {
		return unknownSpeakers.size();
	}

}
