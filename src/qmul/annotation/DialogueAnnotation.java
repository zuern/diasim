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
package qmul.annotation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import qmul.corpus.BNCCorpus;
import qmul.corpus.Dialogue;
import csli.util.FileUtils;

/**
 * An annotation of a single {@link Dialogue}
 * 
 * @author mpurver
 */
public class DialogueAnnotation {

	private Dialogue dialogue;
	private String annotator;
	private ArrayList<Integer> taggedSents;
	private HashMap<Integer, ArrayList<String>> tagValues;

	public DialogueAnnotation(Dialogue dialogue, String annotator) {
		this.dialogue = dialogue;
		this.annotator = annotator;
		this.taggedSents = new ArrayList<Integer>();
		this.tagValues = new HashMap<Integer, ArrayList<String>>();
	}

	/**
	 * @return the dialogue
	 */
	public Dialogue getDialogue() {
		return dialogue;
	}

	/**
	 * @return the annotator
	 */
	public String getAnnotator() {
		return annotator;
	}

	/**
	 * @return the taggedSents
	 */
	public ArrayList<Integer> getTaggedSents() {
		return taggedSents;
	}

	/**
	 * @return the tagValues
	 */
	public HashMap<Integer, ArrayList<String>> getTagValues() {
		return tagValues;
	}

	public static boolean read(AnnotationSet annSet, File file) {
		// get dialogue
		String[] f = file.getName().split("-");
		String annotator = f[0];
		if (!annSet.getTask().equals(f[1])) {
			throw new RuntimeException("Non-matching tasks " + f[1] + " " + annSet.getTask());
		}
		ArrayList<Dialogue> dialogues = new ArrayList<Dialogue>();
		int iD = 0;
		int iS = 0;
		String dialogueName = f[2];
		Dialogue dialogue = annSet.getCorpus().getDialogue(dialogueName);
		if (dialogue == null) {
			// no matching ID? probably a subdialogue, need to check all
			dialogues.addAll(annSet.getCorpus().getDialogues());
			for (Dialogue d : annSet.getCorpus().getDialogues()) {
				if (!d.getId().contains(dialogueName)) {
					dialogues.remove(d);
				}
			}
		} else {
			// a single one? make life more efficient
			dialogues.add(dialogue);
		}
		if (dialogues.isEmpty()) {
			return false;
		}
		ArrayList<String> lines = new ArrayList<String>();
		try {
			lines = (ArrayList<String>) FileUtils.getFileLines(file, lines);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Annotated lines: " + lines.size());
		DialogueAnnotation da = new DialogueAnnotation(dialogues.get(iD), annotator);
		ArrayList<String> tagValues = new ArrayList<String>();
		int start = -1;
		int end = -1;
		for (String line : lines) {
			if (!line.trim().isEmpty()) {
				f = line.split("\\t");
				Integer lineNum = new Integer(f[0]);
				if (annSet.getCorpus() instanceof BNCCorpus) {
					// BNC numbers in java corpus are 10*originals, to allow unnumbered sents to be interposed
					lineNum *= 10;
				}
				// System.out.println("Looking for line " + lineNum);
				boolean found = false;
				while (!found) {
					// System.out.println("Next sent " + iS + " in dialogue " + dialogues.get(iD).getId() + " "
					// + dialogues.get(iD).getSents().get(iS).getNum());
					int sentNum = dialogues.get(iD).getSents().get(iS).getNum();
					if (sentNum == lineNum) {
						found = true;
						// System.out.println("Found line at " + iS);
						if (start < 0) {
							start = sentNum;
						}
						end = sentNum;
						da.getTaggedSents().add(sentNum);
						da.getTagValues().put(sentNum, new ArrayList<String>(Arrays.asList(f).subList(1, f.length)));
					}
					if (++iS >= dialogues.get(iD).numSents()) {
						if (++iD >= dialogues.size()) {
							throw new RuntimeException("run out of dialogues at " + line);
						}
						if (da.getTagValues().size() > 0) {
							annSet.add(da);
							String id = dialogues.get(iD - 1).getId();
							if (!annSet.getStarts().containsKey(id)) {
								annSet.getStarts().put(id, start);
							}
							if (!annSet.getEnds().containsKey(id)) {
								annSet.getEnds().put(id, end);
							}
							System.out.println("Added DA " + da.dialogue.getId() + " " + da.tagValues.size());
						}
						da = new DialogueAnnotation(dialogues.get(iD), annotator);
						start = -1;
						end = -1;
						iS = 0;
					}
				}
			}
		}
		if (da.getTagValues().size() > 0) {
			annSet.add(da);
			String id = dialogues.get(iD).getId();
			if (!annSet.getStarts().containsKey(id)) {
				annSet.getStarts().put(id, start);
			}
			if (!annSet.getEnds().containsKey(id)) {
				annSet.getEnds().put(id, end);
			}
			System.out.println("Added DA " + da.dialogue.getId() + " " + da.tagValues.size());
		}
		return (da.getTagValues().size() > 0);
	}

}
