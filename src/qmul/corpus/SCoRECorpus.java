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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import csli.util.FileUtils;

/**
 * A {@link DialogueCorpus} implementation for the format SCoRE uses for DiET and AHILab data
 * 
 * @author mpurver
 */
public class SCoRECorpus extends DialogueCorpus {

	/**
	 * @param id
	 *            an ID for this dataset
	 * @param dir
	 *            the dir to read all files from
	 */
	public SCoRECorpus(String id, String dir) {
		super(id, new File(dir));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#loadDialogue(java.lang.String)
	 */
	@Override
	public boolean loadDialogue(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#setupCorpus()
	 */
	@Override
	public boolean setupCorpus() {
		Pattern pTurn = Pattern.compile("^##(\\d+)##(([^\\s#][^#]*)##)?(.*)$");
		FilenameFilter tmp = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".exp");
			}
		};
		for (File file : getDir().listFiles(tmp)) {
			System.out.println("File: " + file);
			ArrayList<String> lines = new ArrayList<String>();
			try {
				lines = (ArrayList<String>) FileUtils.getFileLines(file, lines);
				String genre = "default";
				Dialogue d = addDialogue(file.getName(), genre);
				getGenreCounts()
						.put(genre, (getGenreCounts().get(genre) == null ? 0 : getGenreCounts().get(genre)) + 1);
				DialogueTurn turn = null;
				for (String line : lines) {
					line = line.trim();
					Matcher m = pTurn.matcher(line);
					if (m.matches()) {
						if ((m.group(2) != null) && !m.group(2).isEmpty()) {
							// new speaker turn
							DialogueSpeaker spk = getSpeakerMap().get(m.group(2));
							if (spk == null) {
								spk = new DialogueSpeaker(m.group(2), m.group(2), null, null, null, null);
								getSpeakerMap().put(m.group(2), spk);
							}
							d.getSpeakers().add(spk);
							turn = d.addTurn(-1, spk);
							d.addSent(new Integer(m.group(1)), turn, m.group(4), null);
						} else {
							// new sentence in existing speaker turn
							d.addSent(new Integer(m.group(1)), turn, m.group(4), null);
						}
					} else if (!line.isEmpty()) {
						System.out.println("skip line " + line);
					}
				}
				if (!checkDialogue(d)) {
					return false;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sanityCheck();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new SCoRECorpus("diet", "/Users/mpurver/Documents/imc-data/diet/2009-Tangram-SameDifferent");
	}

}
