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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A collection of {@link Dialogue}s
 * 
 * @author mpurver
 */
public abstract class DialogueCorpus implements Serializable {

	private static final long serialVersionUID = 1087872150946458894L;

	private String id;
	private File dir;
	private boolean dynamic;
	private ArrayList<Dialogue> dialogues;
	private HashMap<String, DialogueSpeaker> speakerMap;
	private HashMap<String, String> genreMap;
	private HashMap<String, Integer> genreCounts;

	private int minSpeakers = 0;
	private int maxSpeakers = 0;
	private int maxDialogues = 0;
	private int minGenreCount = 0;

	/**
	 * @deprecated just for serialization
	 */
	@Deprecated
	public DialogueCorpus() {
		super();
		this.dynamic = false;
		this.dialogues = new ArrayList<Dialogue>();
		this.speakerMap = new HashMap<String, DialogueSpeaker>();
		this.genreMap = new HashMap<String, String>();
		this.genreCounts = new HashMap<String, Integer>();
	}

	/**
	 * @param id
	 *            a name for this corpus
	 * @param dir
	 *            the base dir for the corpus data
	 */
	public DialogueCorpus(String id, File dir) {
		this(id, dir, false);
	}

	/**
	 * @param id
	 *            a name for this corpus
	 * @param dir
	 *            the base dir for the corpus data
	 * @param dynamic
	 *            if true, corpus will read dialogues from file as required, rather than reading all data in when
	 *            constructed
	 */
	public DialogueCorpus(String id, File dir, boolean dynamic) {
		this(id, dir, 0, 0, 0, 0, dynamic);
	}

	/**
	 * @param id
	 *            a name for this corpus
	 * @param dir
	 *            the base dir for the corpus data
	 * @param minSpeakers
	 *            discard any dialogue with fewer than this number of speakers (0 to allow all)
	 * @param maxSpeakers
	 *            discard any dialogue with more than this number of speakers (0 to allow all)
	 * @param minGenreCount
	 *            discard any dialogue whose genre appears in fewer than this number of dialogues (0 to allow all)
	 * @param maxDialogues
	 *            only read in at most this number of dialogues (0 to allow all)
	 */
	public DialogueCorpus(String id, File dir, int minSpeakers, int maxSpeakers, int minGenreCount, int maxDialogues) {
		this(id, dir, minSpeakers, maxSpeakers, minGenreCount, maxDialogues, false);
	}

	/**
	 * @param id
	 *            a name for this corpus
	 * @param dir
	 *            the base dir for the corpus data
	 * @param minSpeakers
	 *            discard any dialogue with fewer than this number of speakers (0 to allow all)
	 * @param maxSpeakers
	 *            discard any dialogue with more than this number of speakers (0 to allow all)
	 * @param minGenreCount
	 *            discard any dialogue whose genre appears in fewer than this number of dialogues (0 to allow all)
	 * @param maxDialogues
	 *            only read in at most this number of dialogues (0 to allow all)
	 * @param dynamic
	 *            if true, corpus will read dialogues from file as required, rather than reading all data in when
	 *            constructed
	 */
	public DialogueCorpus(String id, File dir, int minSpeakers, int maxSpeakers, int minGenreCount, int maxDialogues,
			boolean dynamic) {
		this();
		this.id = id;
		this.dynamic = dynamic;
		this.dir = dir;
		if (!(dir.exists() && dir.canRead())) {
			throw new RuntimeException("Can't read corpus dir " + dir);
		}
		this.minSpeakers = minSpeakers;
		this.maxSpeakers = maxSpeakers;
		this.minGenreCount = minGenreCount;
		this.maxDialogues = maxDialogues;
		System.out.println("Limits: minSpeakers=" + minSpeakers + ", maxSpeakers=" + maxSpeakers + ", minGenreCount="
				+ minGenreCount + ", maxDialogues=" + maxDialogues);
		if (!setupCorpus()) {
			throw new RuntimeException("Failed to set up corpus");
		}
	}

	/**
	 * @param file
	 * @return success saving this corpus to file
	 */
	public boolean writeToFile(File file) {
		ObjectOutputStream out;
		try {
			OutputStream outs = new FileOutputStream(file);
			if (file.getName().endsWith(".gz")) {
				outs = new GZIPOutputStream(outs);
			}
			out = new ObjectOutputStream(outs);
			out.writeObject(this);
			System.out.println("Saved corpus to file " + file);
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param file
	 * @return a corpus read from a file previously saved by writeToFile(), null if the file doesn't exist or can't be
	 *         read
	 */
	public static DialogueCorpus readFromFile(File file) {
		try {
			InputStream ins = new FileInputStream(file);
			if (file.getName().endsWith(".gz")) {
				ins = new GZIPInputStream(ins);
			}
			ObjectInputStream in = new ObjectInputStream(ins);
			System.out.print("Reading corpus from file " + file + " ... ");
			DialogueCorpus c = (DialogueCorpus) in.readObject();
			System.out.println("done.");
			System.out.println("Read corpus with " + c.numDialogues() + " dialogues, " + c.numTurns() + " turns, "
					+ c.numSents() + " sentences, " + c.numWords() + " words.");
			in.close();
			return c;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Set up the corpus, reading all dialogues and relevant data in from file. If dynamic, corpus will read dialogues
	 * from file as required later, rather than reading all data in when constructed now
	 * 
	 * @return success
	 */
	public abstract boolean setupCorpus();

	/**
	 * Read a dialogue in from disk
	 * 
	 * @param name
	 *            the name of the file (slightly corpus-dependent whether this includes path, suffix etc)
	 * @return success
	 */
	public abstract boolean loadDialogue(String name);

	/**
	 * Create a new {@link Dialogue} and add it to the appropriate list
	 * 
	 * @param id
	 *            the name of the dialogue
	 * @param genre
	 *            the genre of the dialogue (use null if your corpus doesn't deal in genres)
	 * @return the new {@link Dialogue}
	 */
	public Dialogue addDialogue(String id, String genre) {
		Dialogue dialogue = new Dialogue(this, id, genre);
		this.getDialogues().add(dialogue);
		this.getGenreMap().put(id, genre);
		return dialogue;
	}

	/**
	 * Remove a {@link Dialogue} from the appropriate list
	 * 
	 * @param d
	 *            the dialogue to remove
	 */
	public void removeDialogue(Dialogue d) {
		this.getDialogues().remove(d);
	}

	private int t = 0;
	private int s = 0;
	private int d = 0;

	/**
	 * Check dialogue is acceptable (number of speakers), increment counts if so; otherwise remove
	 * 
	 * @param dialogue
	 * @return true if we should carry on and get another dialogue, false otherwise
	 */
	protected boolean checkDialogue(Dialogue dialogue) {
		System.out.println("Got dialogue " + dialogue.getId() + " with " + dialogue.numTurns() + " turns, "
				+ dialogue.numSents() + " sentences, " + dialogue.numSpeakers() + " speakers, " + dialogue.getGenre()
				+ " genre");
		if (!Float.isNaN(dialogue.getStartTime())) {
			System.out.println("Start time " + dialogue.getStartTime() + " end time " + dialogue.getEndTime());
		}
		if (badNumSpeakers(dialogue)) {
			removeDialogue(dialogue);
			System.out.println("Removing dialogue " + dialogue.getId() + " - bad number of speakers "
					+ dialogue.numSpeakers());
		} else if (getGenreCounts().get(dialogue.getGenre()) < getMinGenreCount()) {
			removeDialogue(dialogue);
			System.out.println("Removing dialogue " + dialogue.getId() + " - bad genre " + dialogue.getGenre()
					+ " count " + getGenreCounts().get(dialogue.getGenre()));
		} else {
			s += dialogue.numSents();
			t += dialogue.numTurns();
			d++;
		}
		if ((d >= getMaxDialogues()) && (getMaxDialogues() > 0)) {
			System.out.println("Got enough dialogues, stopping: " + d);
			return false;
		}
		return true;
	}

	/**
	 * @return true if the number of speakers is unacceptable. Can be overridden if there are speakers we don't care
	 *         about (e.g. the "unknown" speakers in the BNC)
	 */
	protected boolean badNumSpeakers(Dialogue dialogue) {
		return ((dialogue.numSpeakers() < getMinSpeakers()) || ((dialogue.numSpeakers() > getMaxSpeakers()) && (getMaxSpeakers() > 0)));
	}

	/**
	 * Check the numbers add up after reading in the {@link Dialogue}s
	 * 
	 * @return true if the check succeeds, false otherwise
	 */
	protected boolean sanityCheck() {
		System.out.println("Built corpus with " + numDialogues() + " dialogues, " + numTurns() + " turns, "
				+ numSents() + " sents");
		System.out.println("Total number of word tokens: " + numWords());
		System.out.println("Sanity check: " + d + " dialogues, " + t + " turns, " + s + " sents");
		return ((d == numDialogues()) && (t == numTurns()) || (s == numSents()));
	}

	/**
	 * Fill the sentIndices maps with original corpus number -> index in sents list
	 */
	public void setupSentIndices() {
		for (Dialogue d : dialogues) {
			d.setupSentIndices();
		}
	}

	/**
	 * @return the unique ID of this corpus
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the data dir
	 */
	public File getDir() {
		return dir;
	}

	/**
	 * @param dir
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setDir(File dir) {
		this.dir = dir;
	}

	/**
	 * @return if true, corpus will read dialogues from file as required, rather than reading all data in when
	 *         constructed
	 */
	public boolean isDynamic() {
		return dynamic;
	}

	/**
	 * @param dynamic
	 *            if true, corpus will read dialogues from file as required, rather than reading all data in when
	 *            constructed
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	/**
	 * @return the {@link Dialogue}s
	 */
	public ArrayList<Dialogue> getDialogues() {
		return dialogues;
	}

	/**
	 * @param dialogues
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setDialogues(ArrayList<Dialogue> dialogues) {
		this.dialogues = dialogues;
	}

	/**
	 * @return the map of speaker IDs to speaker info
	 */
	public HashMap<String, DialogueSpeaker> getSpeakerMap() {
		return speakerMap;
	}

	/**
	 * @param speakerMap
	 *            the speakerMap to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setSpeakerMap(HashMap<String, DialogueSpeaker> speakerMap) {
		this.speakerMap = speakerMap;
	}

	/**
	 * @return the map of dialogue IDs to genres
	 */
	public HashMap<String, String> getGenreMap() {
		return genreMap;
	}

	/**
	 * @param genreMap
	 *            the genreMap to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setGenreMap(HashMap<String, String> genreMap) {
		this.genreMap = genreMap;
	}

	/**
	 * @return the map of genres to number of dialogues
	 */
	public HashMap<String, Integer> getGenreCounts() {
		return genreCounts;
	}

	/**
	 * @param genreCounts
	 *            the genreCounts to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setGenreCounts(HashMap<String, Integer> genreCounts) {
		this.genreCounts = genreCounts;
	}

	/**
	 * @return the min number of speakers in a dialogue when reading in from disk
	 */
	public int getMinSpeakers() {
		return minSpeakers;
	}

	/**
	 * @return the max number of speakers in a dialogue when reading in from disk
	 */
	public int getMaxSpeakers() {
		return maxSpeakers;
	}

	/**
	 * @return the max number of dialogues to allow when reading in from disk
	 */
	public int getMaxDialogues() {
		return maxDialogues;
	}

	/**
	 * @return the min number of dialogues in a particular genre when reading in from disk
	 */
	public int getMinGenreCount() {
		return minGenreCount;
	}

	/**
	 * @param minSpeakers
	 *            the minSpeakers to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setMinSpeakers(int minSpeakers) {
		this.minSpeakers = minSpeakers;
	}

	/**
	 * @param maxSpeakers
	 *            the maxSpeakers to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setMaxSpeakers(int maxSpeakers) {
		this.maxSpeakers = maxSpeakers;
	}

	/**
	 * @param maxDialogues
	 *            the maxDialogues to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setMaxDialogues(int maxDialogues) {
		this.maxDialogues = maxDialogues;
	}

	/**
	 * @param minGenreCount
	 *            the minGenreCount to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setMinGenreCount(int minGenreCount) {
		this.minGenreCount = minGenreCount;
	}

	/**
	 * @return the set of 10 most common syntactic rules
	 */
	public HashSet<String> topTenSynProductions() {
		return null;
	}

	/**
	 * @return the total number of {@link Dialogue}s in the corpus
	 */
	public int numDialogues() {
		return getDialogues().size();
	}

	/**
	 * @return the total number of {@link DialogueTurn}s in the corpus
	 */
	public int numTurns() {
		int n = 0;
		for (Dialogue d : getDialogues()) {
			n += d.numTurns();
		}
		return n;
	}

	/**
	 * @return the total number of {@link DialogueSentence}s in the corpus
	 */
	public int numSents() {
		int n = 0;
		for (Dialogue d : getDialogues()) {
			n += d.numSents();
		}
		return n;
	}

	/**
	 * @return the total number of words (tokens) in the corpus
	 */
	public int numWords() {
		int n = 0;
		for (Dialogue d : getDialogues()) {
			n += d.numWords();
		}
		return n;
	}

	/**
	 * @param id
	 * @return the dialogue with this id, or null if we can't find one or load it dynamically
	 */
	public Dialogue getDialogue(String id) {
		for (Dialogue d : getDialogues()) {
			if (d.getId().equals(id)) {
				return d;
			}
		}
		if (isDynamic() && loadDialogue(id)) {
			for (Dialogue d : getDialogues()) {
				if (d.getId().equals(id)) {
					return d;
				}
			}
		}
		return null;
	}

}
