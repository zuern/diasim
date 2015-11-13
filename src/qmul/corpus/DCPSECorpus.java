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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import qmul.util.FilenameToolkit;
import qmul.util.parse.CreateTreeFromDCPSE;
import csli.util.FileUtils;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Filter;

/**
 * A {@link DialogueCorpus} implementation for the DCPSE
 * 
 * @author mpurver
 */
public class DCPSECorpus extends DialogueCorpus {

	private static final long serialVersionUID = 5561017857124993357L;

	private static final String ID = "DCPSE";

	private static final String BASE_DIR = "/import/imc-corpora/corpora/dcpse";
	private static final String DATA_DIR = "dcpse/data";
	private static final String METADATA_DIR = "../text"; // rel to DATA_DIR
	private static final String MAPPING_FILE = "Mapping.txt";
	private static final String GENRE_FILE = "Texts.txt";
	private static final String SPEAKER_FILE = "Dcpse.txt";

	/**
	 * Create a DCPSE corpus, reading data files from the default (unix) directory
	 */
	public DCPSECorpus() {
		this(BASE_DIR);
	}

	/**
	 * Create a DCPSE corpus, reading data files from disk
	 * 
	 * @param baseDir
	 *            override the default (unix) path with your own
	 */
	public DCPSECorpus(String baseDir) {
		super(ID, new File(baseDir, DATA_DIR));
	}

	/**
	 * Create a DCPSE corpus, reading data files from the default (unix) directory
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
	public DCPSECorpus(int minSpeakers, int maxSpeakers, int minGenreCount, int maxDialogues) {
		this(BASE_DIR, minSpeakers, maxSpeakers, minGenreCount, maxDialogues);
	}

	/**
	 * Create a DCPSE corpus, reading data files from disk
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
	public DCPSECorpus(String baseDir, int minSpeakers, int maxSpeakers, int minGenreCount, int maxDialogues) {
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
		// first get the rather-unnecessary-seeming map of (old?) dialogue names to (new?) dialogue names
		HashMap<String, String> map = new HashMap<String, String>();
		String[] files = { MAPPING_FILE, GENRE_FILE, SPEAKER_FILE };
		int[] widths = { 2, 5, 15 };
		for (int i = 0; i < files.length; i++) {

			File currentFile = new File(metaDataDir, files[i]);
			ArrayList<String> lines = new ArrayList<String>();
			try {
				FileUtils.getFileLines(currentFile, lines);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
			int l = 0;
			for (String line : lines) {
				String[] f = line.split("\t", -1);
				if (f.length == widths[i]) {
					if (l == 0) {
						// ignore the header line
					} else {
						if (files[i].equals(MAPPING_FILE)) {
							map.put(f[1].toUpperCase(), f[0].toUpperCase());
						} else {
							if (files[i].equals(GENRE_FILE)) {
								String d = map.get(f[1]);
								if (d == null) {
									throw new RuntimeException("No mapping for old dialogue name " + f[1]);
								}
								int numSubDialogues = Integer.parseInt(f[2]);
								String genre = f[3].toUpperCase();
								Integer numSoFar = getGenreCounts().get(genre);
								getGenreCounts().put(genre, numSubDialogues + (numSoFar == null ? 0 : numSoFar));
								getGenreMap().put(d, genre);
								// System.out.println("Got genre " + f[3] + " for " + d);
							} else if (files[i].equals(SPEAKER_FILE)) {
								String[] subf = f[1].split(":", -1);
								if (subf.length == 1) {
									// DIALOGUE is just a header line for the subdialogue entries - ignore
								} else if (subf.length == 2) {
									// DIALOGUE:SUBDIALOGUE is just a header line for the speaker entries - ignore
								} else if (subf.length == 3) {
									// dialogue IDs here are DIALOGUE:SUBDIALOGUE:SPEAKER
									getSpeakerMap().put(f[1].toUpperCase(),
											new DialogueSpeaker(f[1].toUpperCase(), f[11], f[10], f[12], f[13], f[14]));
									// System.out.println("Got speaker " + getSpeakerMap().get(f[1].toUpperCase())
									// + " for " + f[1]);
								} else {
									throw new RuntimeException("Strange speaker file line " + line);
								}
							}
						}
					}
				} else {
					throw new RuntimeException("Strange " + currentFile + " file line: " + f.length + " " + widths[i]
							+ " " + line);
				}
				l++;
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#setupCorpus()
	 */
	@Override
	public boolean setupCorpus() {
		getMetaData();
		File[] files = getDir().listFiles();
		FilenameToolkit.sortByFileNameIgnoreCase(files);
		System.out.println("Found " + files.length + " corpus files ...");
		boolean success = true;
		System.out.println("Limiting number of dialogues: " + getMaxDialogues());
		for (File file : files) {
			if (!processFile(file)) {
				// failure below this may be due to hitting the dialogue limit
				success = (numDialogues() >= getMaxDialogues());
				break;
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
		Pattern p = Pattern.compile("(?i)(.+)\\.cor");
		Matcher m = p.matcher(file.getName());
		if (m.matches()) {
			String dialogueName = m.group(1).toUpperCase();
			String genre = getGenreMap().get(dialogueName);
			if (genre == null) {
				throw new RuntimeException("No metadata for dialogue " + dialogueName);
			}
			// Dialogue dialogue = addDialogue(dialogueName, genre);
			BufferedReader reader;
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			} catch (FileNotFoundException e) {
				System.err.println("Error reading DCPSE corpus file " + file + ": " + e.getMessage());
				return false;
			}
			System.out.println("Reading DCPSE corpus file " + file + " ...");
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
		File file = new File(getDir(), name + ".cor");
		return processFile(file);
	}

	/**
	 * @param dialogueName
	 * @param genre
	 * @param reader
	 * @return whether to carry on or not
	 */
	private boolean getSentences(String dialogueName, String genre, BufferedReader reader) {
		Pattern p = Pattern.compile("<#(\\d+):(\\d+):(\\w+)>\\s+<sent>");
		try {
			Dialogue dialogue = null;
			DialogueSpeaker lastSpeaker = null;
			DialogueTurn currentTurn = null;
			int currentSubdialogue = -1;
			Filter<Tree> nodeFilter = new NodeFilter();
			String line = reader.readLine();
			while (line != null) {
				Matcher m = p.matcher(line);
				if (m.find()) {
					// get the metadata
					int sentNum = Integer.parseInt(m.group(1));
					int subDialogue = Integer.parseInt(m.group(2));
					String spk = m.group(3).toUpperCase();
					// start new dialogue if subdialogue changed
					if (subDialogue != currentSubdialogue) {
						if (dialogue != null) {
							if (!checkDialogue(dialogue)) {
								return false;
							}
						}
						dialogue = addDialogue(dialogueName + ":" + subDialogue, genre);
					}
					currentSubdialogue = subDialogue;
					// set up speaker
					String spkId = dialogue.getId() + ":" + spk;
					DialogueSpeaker speaker = getSpeakerMap().get(spkId);
					// System.out.println("Getting tree for sent " + sentNum + " spk [" + spkId + "]=[" + speaker + "] "
					// + line);
					// get the tree and extract the transcription
					Tree tree = CreateTreeFromDCPSE.makeTree(reader);
					String trans = "";
					if (tree != null) {
						tree = tree.prune(nodeFilter);
						if (tree != null) {
							for (Tree leaf : tree.getLeaves()) {
								String label = leaf.label().toString();
								label = label.replaceAll("^\\s*\\{(.*)\\}\\s*$", "$1");
								label = label.replaceAll("^\\s*<([,.:;?!]+)>\\s*$", "$1");
								trans += label + " ";
							}
							trans = trans.substring(0, trans.length() - 1);
							// start new turn if speaker has changed
							if ((lastSpeaker == null) || !speaker.equals(lastSpeaker) || (currentTurn == null)) {
								currentTurn = dialogue.addTurn(-1, speaker);
								// System.out.println(currentTurn);
							}
							// add sentence
							dialogue.addSent(sentNum, currentTurn, trans, tree);
							// System.out.println(sent);
							lastSpeaker = speaker;
						}
					}
				}
				line = reader.readLine();
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
		return new HashSet<String>(Arrays.asList("NP:PRON", "VP:V", "AVP:ADV", "PP:PREP:NP", "DTP:ART", "NP:DTP:N",
				"NP:N", "VP:AUX:V", "AJP:ADJ", "DTP:PRON"));
	}

	/**
	 * For removing empty nodes with no proper children
	 * 
	 * @author mpurver
	 */
	private class NodeFilter implements Filter<Tree> {

		private static final long serialVersionUID = 4477196338075916207L;

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
			if (obj.label().value().matches("^\\s*NONCL\\s*$") && obj.getChildrenAsList().isEmpty()) {
				return false;
			}
			// naughty - modifying values when we're really just supposed to be filtering ...
			if (!CreateTreeFromDCPSE.getOption(CreateTreeFromDCPSE.INCLUDE_NO_BRACKETS)
					&& CreateTreeFromDCPSE.getOption(CreateTreeFromDCPSE.PP_LEXICAL_FEATURES)) {
				// add head prep word to PP mother as the first bracketed feature
				if (obj.label().value().matches("^PP\\b.*")) {
					String label = obj.label().value();
					boolean foundPrep = false;
					for (Tree kid : obj.getChildrenAsList()) {
						if (kid.label().value().matches("^PREP\\b.*") && (kid.numChildren() > 0)) {
							Tree grandkid = kid.getChild(0);
							String word = grandkid.label().value().replaceAll("\\{|\\}", "");
							if (label.equals("PP")) {
								obj.label().setValue("PP(" + word + ")");
							} else if (label.matches("PP\\(.*\\)")) {
								obj.label().setValue(label.replaceFirst("\\(", "(" + word + ","));
							} else {
								throw new RuntimeException("strange PP label " + label);
							}
							// System.out.println("PP change " + obj.pennString());
							foundPrep = true;
							break;
						}
					}
					// sometimes we get ADV tags e.g. "[on X]", "nothing [but X]" so use that instead
					if (!foundPrep) {
						for (Tree kid : obj.getChildrenAsList()) {
							if (kid.label().value().matches("^ADV\\b.*") && (kid.numChildren() > 0)) {
								Tree grandkid = kid.getChild(0);
								String word = grandkid.label().value().replaceAll("\\{|\\}", "");
								if (label.equals("PP")) {
									obj.label().setValue("PP(" + word + ")");
								} else if (label.matches("PP\\(.*\\)")) {
									obj.label().setValue(label.replaceFirst("\\(", "(" + word + ","));
								} else {
									throw new RuntimeException("strange PP label " + label);
								}
								System.out.println("PP with ADV head " + obj.pennString());
								foundPrep = true;
								break;
							}
						}
					}
					// we get PP(coord) with PP heads, but otherwise should have found a PREP/ADV by now
					if (!foundPrep && !obj.getChild(0).label().value().matches("^PP\\b.*")) {
						new RuntimeException("PP with no PREP head: " + obj.pennString()).printStackTrace();
					}
				}

			}
			if (CreateTreeFromDCPSE.getOption(CreateTreeFromDCPSE.INCLUDE_NO_IGNORE)) {
				if (obj.label().value().contains("ignore)")) {
					return false;
				}
			}
			return true;
		}

	}

	/**
	 * just for testing
	 * 
	 * @args put in your local corpus base dir if you want
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("Found arg, using non-default base dir " + args[0]);
			new DCPSECorpus(args[0]);
		} else {
			new DCPSECorpus();
		}
	}

}
