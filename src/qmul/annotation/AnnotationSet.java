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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import qmul.corpus.DialogueCorpus;
import csli.util.FileUtils;

/**
 * A set of annotations on a {@link DialogueCorpus}
 * 
 * @author mpurver
 */
public class AnnotationSet {

	private DialogueCorpus corpus;
	private String task;
	private ArrayList<String> tags;
	private HashMap<String, Integer> starts;
	private HashMap<String, Integer> ends;
	private HashMap<String, DialogueAnnotation> annotations;

	public AnnotationSet(DialogueCorpus corpus, File taskFile) {
		this.corpus = corpus;
		this.annotations = new HashMap<String, DialogueAnnotation>();
		this.task = taskFile.getName();
		this.tags = new ArrayList<String>();
		this.starts = new HashMap<String, Integer>();
		this.ends = new HashMap<String, Integer>();
		try {
			ArrayList<String> lines = new ArrayList<String>();
			lines = (ArrayList<String>) FileUtils.getFileLines(taskFile, lines);
			String corpusName = lines.remove(0);
			boolean inHeader = true;
			for (String line : lines) {
				if (line.startsWith("+") || line.startsWith("?")) {
					// requirement/display, rather than actual annotation
					continue;
				} else if (line.trim().isEmpty()) {
					// end of header
					inHeader = false;
					continue;
				} else if (inHeader) {
					String[] f = line.split("\\s+");
					tags.add(f[0]);
				} else {
					String[] f = line.split("\\s+");
					// if (f.length > 1) {
					starts.put(f[0], new Integer(f[1]));
					ends.put(f[0], new Integer(f[2]));
					// }
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * @param da
	 * @return true if added (corpus matches), false otherwise
	 */
	public boolean add(DialogueAnnotation da) {
		if (da.getDialogue().getCorpus().equals(corpus)) {
			annotations.put(da.getDialogue().getId(), da);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Add the annotations from the given file
	 * 
	 * @param file
	 * @return true if added (corpus matches, file found etc), false otherwise
	 */
	public boolean add(File file) {
		try {
			return DialogueAnnotation.read(this, file);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Add all annotations matching the task in the given dir
	 * 
	 * @param dir
	 * @return true if added at least one file, false otherwise
	 */
	public boolean addAll(File dir) {
		boolean found = false;
		for (File file : dir.listFiles(new FilenameFilter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
			 */
			@Override
			public boolean accept(File dir, String name) {
				return name.contains("-" + task + "-");
			}
		})) {
			System.out.print("Adding file " + file + " ... ");
			if (add(file)) {
				System.out.println("success.");
				found = true;
			} else {
				System.out.println("failed.");
			}
		}
		return found;
	}

	/**
	 * @return the corpus
	 */
	public DialogueCorpus getCorpus() {
		return corpus;
	}

	/**
	 * @return the task
	 */
	public String getTask() {
		return task;
	}

	/**
	 * @return the tags
	 */
	public ArrayList<String> getTags() {
		return tags;
	}

	/**
	 * @return the annotations, map from dialogue ID to {@link DialogueAnnotation}s
	 */
	public HashMap<String, DialogueAnnotation> getAnnotations() {
		return annotations;
	}

	/**
	 * @return the {@link DialogueAnnotation} for a given dialogue
	 */
	public DialogueAnnotation getAnnotations(String dialogueId) {
		return annotations.get(dialogueId);
	}

	/**
	 * @return the annotations for a given dialogue
	 */
	public HashMap<Integer, ArrayList<String>> getTagValues(String dialogueId) {
		return annotations.get(dialogueId).getTagValues();
	}

	/**
	 * @return the starts
	 */
	public HashMap<String, Integer> getStarts() {
		return starts;
	}

	/**
	 * @return the ends
	 */
	public HashMap<String, Integer> getEnds() {
		return ends;
	}

}
