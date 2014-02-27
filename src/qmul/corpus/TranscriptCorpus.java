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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import csli.util.FileUtils;

/**
 * Simple corpora created from transcript files output from NOMOS
 * 
 * @author mpurver
 */
public abstract class TranscriptCorpus extends DialogueCorpus {

	private static final Pattern LINE_PAT = Pattern.compile("^(\\S*)\\s+(\\w+)_\\d+_\\d+\\s+\\[(.*)\\]\\s*$");

	public TranscriptCorpus(String id, File file, boolean dynamic) {
		super(id, file, dynamic);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#loadDialogue(java.lang.String)
	 */
	@Override
	public boolean loadDialogue(String name) {
		System.out.println("Load dialogue " + name);
		File file = new File(getDir(), name);
		ArrayList<String> lines = new ArrayList<String>();
		try {
			FileUtils.getFileLines(file, lines);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		String id = name.replaceAll("\\.txt", "");
		Dialogue d = addDialogue(id, getGenre());
		for (String line : lines) {
			if (line.isEmpty()) {
				continue;
			}
			Matcher m = LINE_PAT.matcher(line);
			if (m.matches()) {
				String daTags = m.group(1);
				String spkId = m.group(2);
				String trans = m.group(3).trim();
				DialogueSpeaker spk = getSpeakerMap().get(spkId);
				if (spk == null) {
					spk = new DialogueSpeaker(spkId, null, null, null, null, null);
					getSpeakerMap().put(spkId, spk);
				}
				DialogueTurn t = d.addTurn(-1, spk);
				DialogueSentence s = d.addSent(-1, t, trans, null);
				if (daTags != null && !daTags.isEmpty()) {
					for (String daTag : daTags.split(",")) {
						s.getDaTags().add(daTag);
						t.getDaTags().add(daTag);
					}
				}
				// System.out.println(s.getId() + " " + s.getDaTags());
			} else {
				System.err.println("WARNING strange line " + line);
			}
		}
		return checkDialogue(d);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#setupCorpus()
	 */
	@Override
	public boolean setupCorpus() {
		getGenreCounts().put(getGenre(), Integer.MAX_VALUE);
		for (File file : getDir().listFiles()) {
			if (!loadDialogue(file.getName())) {
				return false;
			}
		}
		if (!sanityCheck()) {
			new RuntimeException("Failed sanity check!").printStackTrace();
			System.exit(0);
		}
		return true;
	}

	/**
	 * @return the default genre for this corpus
	 */
	protected abstract String getGenre();

}
