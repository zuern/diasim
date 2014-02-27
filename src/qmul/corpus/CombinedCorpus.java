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
import java.util.Collection;
import java.util.HashMap;

/**
 * A corpus made of a collection of other corpora
 * 
 * @author mpurver
 */
public class CombinedCorpus extends DialogueCorpus {

	private static final long serialVersionUID = -470684937359294121L;

	private Collection<DialogueCorpus> corpora;

	public CombinedCorpus(Collection<DialogueCorpus> corpora) {
		super(makeId(corpora), corpora.iterator().next().getDir());
		this.corpora = corpora;
	}

	/**
	 * @param corpora
	 * @return an id "CORPUS1ID+CORPUS2ID+..."
	 */
	private static String makeId(Collection<DialogueCorpus> corpora) {
		String id = "";
		for (DialogueCorpus corpus : corpora) {
			id += corpus.getId() + "+";
		}
		return id.substring(0, id.length() - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#setupCorpus()
	 */
	@Override
	public boolean setupCorpus() {
		// nothing to do here
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#loadDialogue(java.lang.String)
	 */
	@Override
	public boolean loadDialogue(String name) {
		// dynamic loading not supported
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#addDialogue(java.lang.String, java.lang.String)
	 */
	@Override
	public Dialogue addDialogue(String id, String genre) {
		throw new UnsupportedOperationException("Can't add dialogues to a CombinedCorpus");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#removeDialogue(qmul.corpus.Dialogue)
	 */
	@Override
	public void removeDialogue(Dialogue d) {
		throw new UnsupportedOperationException("Can't remove dialogues from a CombinedCorpus");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#getDialogues()
	 */
	@Override
	public ArrayList<Dialogue> getDialogues() {
		ArrayList<Dialogue> dialogues = new ArrayList<Dialogue>();
		for (DialogueCorpus corpus : corpora) {
			dialogues.addAll(corpus.getDialogues());
		}
		return dialogues;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#getGenreMap()
	 */
	@Override
	public HashMap<String, String> getGenreMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		for (DialogueCorpus corpus : corpora) {
			map.putAll(corpus.getGenreMap());
		}
		return map;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#getMaxSpeakers()
	 */
	@Override
	public int getMaxSpeakers() {
		int max = Integer.MIN_VALUE;
		for (DialogueCorpus corpus : corpora) {
			max = Math.max(max, corpus.getMaxSpeakers());
		}
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#getMinSpeakers()
	 */
	@Override
	public int getMinSpeakers() {
		int min = Integer.MAX_VALUE;
		for (DialogueCorpus corpus : corpora) {
			min = Math.min(min, corpus.getMinSpeakers());
		}
		return min;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#getSpeakerMap()
	 */
	@Override
	public HashMap<String, DialogueSpeaker> getSpeakerMap() {
		HashMap<String, DialogueSpeaker> map = new HashMap<String, DialogueSpeaker>();
		for (DialogueCorpus corpus : corpora) {
			map.putAll(corpus.getSpeakerMap());
		}
		return map;
	}

	/**
	 * just for testing
	 * 
	 * @args put in your local corpus base dir if you want
	 */
	public static void main(String[] args) {
		ArrayList<DialogueCorpus> c = new ArrayList<DialogueCorpus>();
		if (args.length > 0) {
			System.out.println("Found arg, using non-default base dir " + args[0]);
			c.add(new DCPSECorpus(args[0], 2, 2, 0, 2));
			c.add(new DCPSECorpus(args[0], 3, 3, 0, 3));
		} else {
			c.add(new DCPSECorpus(2, 2, 0, 2));
			c.add(new DCPSECorpus(3, 3, 0, 3));
		}
		CombinedCorpus cc = new CombinedCorpus(c);
		System.out.println("New combined corpus " + cc.getId() + " with " + cc.numDialogues() + " dialogues, "
				+ cc.numSents() + " sents, " + cc.numTurns() + " turns");
	}
}
