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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import qmul.util.FilenameToolkit;
import qmul.util.parse.CreateTreeFromSWBD;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeReader;
import edu.stanford.nlp.util.Filter;

/**
 * A {@link DialogueCorpus} implementation for SWBD as in the Penn Treebank: 1,126 dialogues with parse trees
 * 
 * @author mpurver
 */
public class SwitchboardCorpus extends DialogueCorpus {

	private static final long serialVersionUID = 1556878031093741728L;

	private static final String ID = "SWBD";

	private static final String BASE_DIR = "/import/imc-corpora/corpora/kcl/ldc/treebank_3";
	private static final String DATA_DIR = "parsed/mrg/swbd";
	private static final String METADATA_DIR = "../"; // rel to DATA_DIR

	private static final String DEFAULT_GENRE = "SWBD_DEFAULT";

	/**
	 * Create a SWBD corpus, reading data files from the default (unix) directory
	 */
	public SwitchboardCorpus() {
		this(BASE_DIR);
	}

	/**
	 * Create a SWBD corpus, reading data files from disk
	 * 
	 * @param baseDir
	 *            override the default (unix) path with your own
	 */
	public SwitchboardCorpus(String baseDir) {
		super(ID, new File(baseDir, DATA_DIR));
	}

	/**
	 * Create a SWBD corpus, reading data files from the default (unix) directory
	 * 
	 * @param minSpeakers
	 *            discard any dialogue with fewer than this number of speakers (0 to allow all)
	 * @param maxSpeakers
	 *            discard any dialogue with more than this number of speakers (0 to allow all)
	 * @param minGenreCount
	 *            discard any dialogue whose genre appears in fewer than this number of dialogues (0 to allow all)
	 * @param maxDialogues
	 *            only read in at most this number of dialogues (0 to allow all)
	 */
	public SwitchboardCorpus(int minSpeakers, int maxSpeakers, int minGenreCount, int maxDialogues) {
		this(BASE_DIR, minSpeakers, maxSpeakers, minGenreCount, maxDialogues);
	}

	/**
	 * Create a SWBD corpus, reading data files from disk
	 * 
	 * @param baseDir
	 *            override the default (unix) path with your own
	 * @param minSpeakers
	 *            discard any dialogue with fewer than this number of speakers (0 to allow all)
	 * @param maxSpeakers
	 *            discard any dialogue with more than this number of speakers (0 to allow all)
	 * @param minGenreCount
	 *            discard any dialogue whose genre appears in fewer than this number of dialogues (0 to allow all)
	 * @param maxDialogues
	 *            only read in at most this number of dialogues (0 to allow all)
	 */
	public SwitchboardCorpus(String baseDir, int minSpeakers, int maxSpeakers, int minGenreCount, int maxDialogues) {
		super(ID, new File(baseDir, DATA_DIR), minSpeakers, maxSpeakers, minGenreCount, maxDialogues);
	}

	/**
	 * Get the speaker & genre metadata from the text files
	 */
	private void getMetaData() {
		File metaDataDir = new File(getDir(), METADATA_DIR);
		if (!metaDataDir.exists() || !metaDataDir.canRead()) {
			throw new RuntimeException("Error reading metadata dir " + metaDataDir);
		}
		getGenreCounts().put(DEFAULT_GENRE, Integer.MAX_VALUE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#setupCorpus()
	 */
	@Override
	public boolean setupCorpus() {
		getMetaData();
		File[] subdirs = getDir().listFiles();
		FilenameToolkit.sortByFileNameIgnoreCase(subdirs);
		boolean success = true;
		System.out.println("Limiting number of dialogues: " + getMaxDialogues());
		DIR: for (File subdir : subdirs) {
			File[] files = subdir.listFiles();
			FilenameToolkit.sortByFileNameIgnoreCase(files);
			System.out.println("Subdir " + subdir + ", found " + files.length + " corpus files ...");
			for (File file : files) {
				if (!processFile(file)) {
					// failure below this may be due to hitting the dialogue limit
					success = (numDialogues() >= getMaxDialogues());
					break DIR;
				}
			}
		}
		if (!sanityCheck()) {
			new RuntimeException("Failed sanity check!").printStackTrace();
			System.exit(0);
		}
		return success;
	}

	/**
	 * @param file
	 * @return whether to carry on or not
	 */
	private boolean processFile(File file) {
		Pattern p = Pattern.compile("(?i)(.+)\\.mrg");
		Matcher m = p.matcher(file.getName());
		if (m.matches()) {
			String dialogueName = m.group(1).toUpperCase();
			// String genre = getGenreMap().get(dialogueName);
			String genre = DEFAULT_GENRE; // TODO genre information in SWBD?
			if (genre == null) {
				throw new RuntimeException("No metadata for dialogue " + dialogueName);
			}
			PennTreeReader reader;
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				reader = new PennTreeReader(br, new LabeledScoredTreeFactory());
			} catch (FileNotFoundException e) {
				System.err.println("Error reading SWBD corpus file " + file + ": " + e.getMessage());
				return false;
			}
			System.out.println("Reading SWBD corpus file " + file + " ...");
			if (!getSentences(dialogueName, genre, reader)) {
				return false;
			}
		} else {
			System.out.println("WARNING: NOT processing non-matching corpus file " + file);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#loadDialogue(java.lang.String)
	 */
	@Override
	public boolean loadDialogue(String name) {
		File file = new File(getDir(), name + ".mrg");
		return processFile(file);
	}

	/**
	 * @param dialogueName
	 * @param genre
	 * @param reader
	 * @return whether to carry on or not
	 */
	private boolean getSentences(String dialogueName, String genre, TreeReader reader) {
		Pattern p = Pattern.compile("\\(CODE\\s+(?:\\([A-Z]+\\s+)?Speaker([A-Za-z]+)(\\d+)");
		try {
			Dialogue dialogue = null;
			DialogueSpeaker speaker = null;
			DialogueSpeaker lastSpeaker = null;
			DialogueTurn currentTurn = null;
			int currentSubdialogue = -1;
			int turnNum = -1;
			Tree tree = reader.readTree();
			Filter<Tree> nodeFilter = new NodeFilter();
			while (tree != null) {
				Matcher m = p.matcher(tree.toString());
				if (m.find()) {
					// get the metadata
					turnNum = Integer.parseInt(m.group(2));
					int subDialogue = 0; // apparently no subdialogues in SWBD ...
					String spk = m.group(1).toUpperCase();
					// start new dialogue if subdialogue changed
					if (subDialogue != currentSubdialogue) {
						if (dialogue != null) {
							if (!checkDialogue(dialogue)) {
								return false;
							}
						}
						// dialogue = addDialogue(dialogueName + ":" + subDialogue, genre);
						dialogue = addDialogue(dialogueName, genre);
						// TODO genre in SWBD?
						getGenreMap().put(dialogueName, genre);
					}
					currentSubdialogue = subDialogue;
					// set up speaker
					String spkId = dialogue.getId() + ":" + spk;
					if (!getSpeakerMap().containsKey(spkId)) {
						// TODO speaker info in SWBD?
						getSpeakerMap().put(spkId, new DialogueSpeaker(spkId, "", "", "", "", ""));
						// System.out.println("added new speaker " + spkId);
					}
					speaker = getSpeakerMap().get(spkId);
				} else {
					// get the tree and extract the transcription
					String trans = "";
					// SWBD embeds trees within an extra unlabelled level ((S etc))
					if (((tree.label() == null) || (tree.label().value() == null)) && (tree.children().length == 1)) {
						tree = tree.getChild(0);
					}
					if (tree != null) {
						tree = tree.prune(nodeFilter);
						if (tree != null) {
							for (Tree leaf : tree.getLeaves()) {
								trans += leaf.label() + " ";
							}
							trans = trans.substring(0, trans.length() - 1);
							// start new turn if speaker has changed
							if ((lastSpeaker == null) || !speaker.equals(lastSpeaker) || (currentTurn == null)) {
								currentTurn = dialogue.addTurn(turnNum, speaker);
								// System.out.println("new turn " + turnNum + ", " + speaker + " " + currentTurn);
								lastSpeaker = speaker;
							}
							// add sentence
							dialogue.addSent(-1, currentTurn, trans, tree);
							// DialogueSentence s = dialogue.addSent(-1, currentTurn, trans, tree);
							// System.out.println("new sent " + s);
							// System.out.println(s.getSyntax().pennString());
						}
					}
				}
				tree = reader.readTree();
			}
			return checkDialogue(dialogue);
		} catch (IOException e) {
			System.err.println("Error reading sentence line" + e.getMessage());
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#topTenSynProductions()
	 */
	@Override
	public HashSet<String> topTenSynProductions() {
		return new HashSet<String>(Arrays.asList("NP:PRP", "S:NP:VP", "INTJ:UH", "PP:IN:NP", "ADVP:RB", "NP:DT:NN",
				"VP:VB:NP", "VP:VB", "S:VP", "NP:NN"));
	}

	/**
	 * just for testing
	 * 
	 * @args put in your local corpus base dir if you want
	 */
	public static void main(String[] args) {
		SwitchboardCorpus c = null;
		if ((args.length > 0) && !args[0].equals("dummy")) {
			System.out.println("Found arg, using non-default base dir " + args[0]);
			c = new SwitchboardCorpus(args[0]);
		} else {
			c = new SwitchboardCorpus();
		}
		c.writeToFile(new File("swbd.corpus.gz"));
	}

	/**
	 * For removing certain kinds of node based on options
	 * 
	 * @author mpurver
	 */
	private class NodeFilter implements Filter<Tree> {

		private static final long serialVersionUID = -2118859608177695349L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see edu.stanford.nlp.util.Filter#accept(java.lang.Object)
		 */
		@Override
		public boolean accept(Tree obj) {
			if (obj == null) {
				return true;
			}
			if (obj.label() == null) {
				return true;
			}
			if (obj.label().value() == null) {
				return true;
			}
			if (obj.label().value().matches("^(E|N)_S$") && getOption(CreateTreeFromSWBD.INCLUDE_NO_E_S)) {
				return false;
			}
			if (obj.label().value().equals("-NONE-") && getOption(CreateTreeFromSWBD.INCLUDE_NO_TRACES)) {
				return false;
			}
			if (obj.label().value().equals("0") && getOption(CreateTreeFromSWBD.INCLUDE_NO_TRACES)) {
				return false;
			}
			if (obj.label().value().matches("^\\*((T\\*)?-\\d+)?$") && getOption(CreateTreeFromSWBD.INCLUDE_NO_TRACES)) {
				return false;
			}
			if (obj.label().value().matches("^[,.:?!;]$") && getOption(CreateTreeFromSWBD.INCLUDE_NO_PUNCTUATION)) {
				return false;
			}
			if (obj.label().value().matches("^\\\\[\\[\\+\\]]$")
					&& getOption(CreateTreeFromSWBD.INCLUDE_NO_SELFREPAIR_BRACKETS)) {
				return false;
			}
			if (!obj.isLeaf() && obj.label().value().matches("^INTJ$") && getOption(CreateTreeFromSWBD.INCLUDE_NO_INTJ)) {
				return false;
			}
			if (!obj.isLeaf() && obj.label().value().matches("^EDITED$")
					&& getOption(CreateTreeFromSWBD.REPAIR_SELFREPAIRS)) {
				return false;
			}
			// naughty - modifying values when we're really just supposed to be filtering ...
			if (getOption(CreateTreeFromSWBD.SIMPLIFY_CATEGORIES) && !obj.isLeaf()) {
				// remove detailed cats NP=1, NP-TMP=2, NP-SBJ-1, ADVP-LOC etc etc
				if (obj.label().value().matches(".+(-|=).+")) {
					obj.label().setValue(obj.label().value().replaceFirst("(-|=).+", ""));
				}
				// remove ^ marker for second half of compounds
				if (obj.label().value().matches("\\^.+")) {
					obj.label().setValue(obj.label().value().replaceFirst("\\^(.+)", "$1"));
				}
				// any verb PoS tag -> VB
				if (obj.label().value().matches("VB(Z|G|D|P|N)")) {
					obj.label().setValue(obj.label().value().replaceFirst("(VB)(Z|G|D|P|N)", "$1"));
				}
				// the BES tag for "'s" contracted verb "is" -> VB (other "'s" are separately tagged e.g. POS)
				if (obj.label().value().matches("BES")) {
					obj.label().setValue(obj.label().value().replaceFirst("BES", "VB"));
				}
				// plural NN(P)S same as sing NN(P)
				if (obj.label().value().matches("NNP?(S)")) {
					obj.label().setValue(obj.label().value().replaceFirst("(NNP?)(S)", "$1"));
				}
				// comp/sup adjectives/adverbs same as normal adj/adv
				if (obj.label().value().matches("(JJ|RB)(R|S)")) {
					obj.label().setValue(obj.label().value().replaceFirst("(JJ|RB)(R|S)", "$1"));
				}
				// possessive pronouns same as normal pronouns
				if (obj.label().value().matches("PRP(\\$)")) {
					obj.label().setValue(obj.label().value().replaceFirst("(PRP)(\\$)", "$1"));
				}
			}
			return true;
		}

		private boolean getOption(int o) {
			return CreateTreeFromSWBD.getOption(o);
		}

	}

}
