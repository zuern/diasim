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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import qmul.util.FilenameToolkit;
import qmul.util.parse.ClarkCurranParser;
import qmul.util.parse.CreateTreeFromClarkCurranCCGProlog;
import qmul.util.parse.PennTreebankTokenizer;
import qmul.util.parse.RASPParser;
import qmul.util.parse.StanfordParser;
import csli.util.FileUtils;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.Parser;
import edu.stanford.nlp.util.Pair;

/**
 * A {@link DialogueCorpus} implementation for the BNC. Yes, I know the C already stands for "corpus".
 * 
 * @author mpurver
 */
public class BNCCorpus extends DialogueCorpus {

	private static final long serialVersionUID = -8038353458122999575L;

	private static final String ID = "BNC";

	private static final String BASE_DIR = "/import/imc-corpora/corpora/bnc/bnc-xml";
	private static final String DATA_DIR = "Texts";
	private static final String TIMING_DIR = "/import/imc-corpora/corpora/bnc/bnc-audio";
	// private static final String TIMING_DIR = "/Users/mpurver/Documents/imc-corpora/corpora/bnc/bnc-audio";

	private static final PennTreebankTokenizer tok = new PennTreebankTokenizer(true);

	public static final String UNKNOWN_SINGLE_SPEAKER = "PSUNK";
	public static final String UNKNOWN_GROUP_SPEAKER = "PSUGP";
	public static final String SGML_UNKNOWN_SINGLE_SPEAKER = "PS000";
	public static final String SGML_UNKNOWN_GROUP_SPEAKER = "PS001";

	protected static boolean removeUnknowns = false;
	private static boolean assignUnknowns = false;
	private static int maxNumUnknownTurns = Integer.MAX_VALUE;
	private static double maxPropUnknownTurns = 1.0;

	protected static boolean removeFilledPauses = false;
	protected static boolean removeBackchannels = false;
	protected static boolean removePunctuationTokens = false;
	protected static boolean replaceTruncatedTokens = false; // must be false if using getTimings

	private static boolean includeMonologue = false;
	public static final String DIALOGUE_TYPE = "spolog2";
	public static final String MONOLOGUE_TYPE = "spolog1";

	public static final boolean getTimings = true;

	/**
	 * Create a BNC corpus, reading data files from the default (unix) directory
	 */
	public BNCCorpus() {
		this(BASE_DIR);
	}

	/**
	 * Create a BNC corpus, reading data files from disk
	 * 
	 * @param baseDir
	 *            override the default (unix) path with your own
	 */
	public BNCCorpus(String baseDir) {
		this(baseDir, false);
	}

	/**
	 * Create a BNC corpus, reading data files from disk
	 * 
	 * @param baseDir
	 *            override the default (unix) path with your own
	 * @param dynamic
	 *            if true, corpus will read dialogues from file as required, rather than reading all data in when
	 *            constructed
	 */
	public BNCCorpus(String baseDir, boolean dynamic) {
		super(ID, new File(baseDir, DATA_DIR), dynamic);
	}

	/**
	 * Create a BNC corpus, reading data files from the default (unix) directory
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
	public BNCCorpus(int minSpeakers, int maxSpeakers, int minGenreCount, int maxDialogues) {
		this(BASE_DIR, minSpeakers, maxSpeakers, minGenreCount, maxDialogues);
	}

	/**
	 * Create a BNC corpus, reading data files from disk
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
	public BNCCorpus(String baseDir, int minSpeakers, int maxSpeakers, int minGenreCount, int maxDialogues) {
		super(ID, new File(baseDir, DATA_DIR), minSpeakers, maxSpeakers, minGenreCount, maxDialogues);
	}

	/**
	 * @param removeUnknowns
	 *            the removeUnknowns to set
	 */
	public static void setRemoveUnknowns(boolean removeUnknowns) {
		BNCCorpus.removeUnknowns = removeUnknowns;
	}

	/**
	 * @param replaceTruncatedTokens
	 *            the replaceTruncatedTokens to set
	 */
	public static void setReplaceTruncatedTokens(boolean replaceTruncatedTokens) {
		BNCCorpus.replaceTruncatedTokens = replaceTruncatedTokens;
	}

	/**
	 * @param assignUnknowns
	 *            the assignUnknowns to set
	 */
	public static void setAssignUnknowns(boolean assignUnknowns) {
		BNCCorpus.assignUnknowns = assignUnknowns;
	}

	/**
	 * @param maxNumUnknownTurns
	 *            the maxNumUnknownTurns to set
	 */
	public static void setMaxNumUnknownTurns(int maxNumUnknownTurns) {
		BNCCorpus.maxNumUnknownTurns = maxNumUnknownTurns;
	}

	/**
	 * @param maxPropUnkownTurns
	 *            the maxPropUnkownTurns to set
	 */
	public static void setMaxPropUnknownTurns(double maxPropUnkownTurns) {
		BNCCorpus.maxPropUnknownTurns = maxPropUnkownTurns;
	}

	/**
	 * @param removeFilledPauses
	 */
	public static void setRemoveFilledPauses(boolean removeFilledPauses) {
		BNCCorpus.removeFilledPauses = removeFilledPauses;
	}

	/**
	 * @param removeBackchannels
	 */
	public static void setRemoveBackchannels(boolean removeBackchannels) {
		BNCCorpus.removeBackchannels = removeBackchannels;
	}

	/**
	 * @param monologue
	 *            if true, include monologue as well as dialogue
	 */
	public static void setIncludeMonologue(boolean monologue) {
		BNCCorpus.includeMonologue = monologue;
	}

	/**
	 * @param removePunctuationTokens
	 */
	public static void setRemovePunctuationTokens(boolean removePunctuationTokens) {
		BNCCorpus.removePunctuationTokens = removePunctuationTokens;
	}

	private static final DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
	private static DocumentBuilder docBuilder;

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#setupCorpus()
	 */
	@Override
	public boolean setupCorpus() {
		try {
			docBuilder = fact.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			System.exit(0);
		}
		// getMetaData();
		System.out.println("Limiting number of dialogues: " + getMaxDialogues());
		if (!isDynamic()) {
			if (!processDir(getDir())) {
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
	 * Read in all files in dir recursively
	 * 
	 * @param dir
	 * @return success
	 */
	private boolean processDir(File dir) {
		File[] files = dir.listFiles();
		FilenameToolkit.sortByFileNameIgnoreCase(files);
		for (File file : files) {
			if (file.isDirectory()) {
				if (!processDir(file)) {
					// failure below this may be due to hitting the dialogue limit
					return (numDialogues() >= getMaxDialogues());
				}
			} else if (file.getName().matches("\\w{3}\\.xml")) {
				if (!processFile(file)) {
					return false;
				}
			} else {
				System.out.println("WARNING: NOT processing non-matching corpus file " + file);
			}
		}
		return true;
	}

	/**
	 * @param file
	 * @return success
	 */
	private boolean processFile(File file) {
		Document xml = null;
		System.out.println("Reading BNC corpus file " + file + " ...");
		try {
			xml = docBuilder.parse(file);
		} catch (SAXException e1) {
			e1.printStackTrace();
			return false;
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		String dialogueName = file.getName().replaceAll("\\.xml", "").toUpperCase();
		System.out.println("Processing BNC XML doc " + file + " ...");
		if (!getXML(dialogueName, xml)) {
			// failure below this may be due to hitting the dialogue limit
			return (numDialogues() >= getMaxDialogues());
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
		File file = new File(getDir(), name);
		if (!file.exists()) {
			file = new File(getDir(), name.replaceFirst("(((.).).)", "$3" + File.separator + "$2" + File.separator
					+ "$1" + ".xml"));
		}
		if (!file.exists()) {
			throw new RuntimeException("File not found " + file.getAbsolutePath());
		}
		if (!processFile(file)) {
			return false;
		}
		if (!sanityCheck()) {
			new RuntimeException("Failed sanity check!").printStackTrace();
			System.exit(0);
		}
		return true;
	}

	/**
	 * @param dialogueName
	 * @param xml
	 * @return whether to carry on or not
	 */
	private boolean getXML(String dialogueName, Document xml) {
		NodeList list = xml.getChildNodes();
		if ((list.getLength() != 1) || !xml.getFirstChild().getNodeName().equals("bncDoc")) {
			System.err.println("strange top node " + xml.getFirstChild());
			return false;
		}
		Node bncDoc = xml.getFirstChild();
		list = bncDoc.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals("teiHeader")) {
				if (!getMetadata(dialogueName, list.item(i).getChildNodes())) {
					System.out.println("Ignoring dialogue " + dialogueName);
					return true;
				}
			} else if (list.item(i).getNodeName().equals("stext")) {
				if (!getDivs(dialogueName, list.item(i).getChildNodes())) {
					if (numDialogues() < getMaxDialogues()) {
						System.err.println("error reading divs " + dialogueName);
					}
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Set speaker information for speaker IDs
	 * 
	 * @param dialogueName
	 * @param list
	 * @return success
	 */
	private boolean getMetadata(String dialogueName, NodeList list) {
		boolean success = false;
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals("profileDesc")) {
				NodeList profile = list.item(i).getChildNodes();
				for (int j = 0; j < profile.getLength(); j++) {
					if (profile.item(j).getNodeName().equals("particDesc")) {
						NodeList partic = profile.item(j).getChildNodes();
						for (int k = 0; k < partic.getLength(); k++) {
							if (partic.item(k).getNodeName().equals("person")) {
								getPerson(dialogueName, partic.item(k));
							}
						}
					} else if (profile.item(j).getNodeName().equals("textClass")) {
						NodeList textCls = profile.item(j).getChildNodes();
						for (int k = 0; k < textCls.getLength(); k++) {
							if (textCls.item(k).getNodeName().equals("catRef")) {
								String catRef = textCls.item(k).getAttributes().getNamedItem("targets").getNodeValue();
								for (String cat : catRef.split("\\s+")) {
									// must be dialogue!
									if (cat.equalsIgnoreCase(DIALOGUE_TYPE)
											|| (includeMonologue && cat.equalsIgnoreCase(MONOLOGUE_TYPE))) {
										success = true;
									}
									Matcher m = Pattern.compile("scgdom\\d+", Pattern.CASE_INSENSITIVE).matcher(cat);
									if (m.matches()) {
										getGenreMap().put(dialogueName, m.group(0).toUpperCase());
									}
								}
								// if no scgdom genre: demographic
								if (getGenreMap().get(dialogueName) == null) {
									getGenreMap().put(dialogueName, "DEMOG");
								}
								// set genre count
								String genre = getGenreMap().get(dialogueName);
								Integer n = getGenreCounts().get(genre);
								getGenreCounts().put(genre, (n == null ? 0 : n) + 1);
							}
						}
					}
				}
			}
		}
		return success;
	}

	/**
	 * Set speaker info for one &lt;person&gt;
	 * 
	 * @param dialogueName
	 * @param node
	 * @return success
	 */
	private boolean getPerson(String dialogueName, Node node) {
		boolean success = true;
		String id = node.getAttributes().getNamedItem("xml:id").getNodeValue();
		String origId = id;
		// ensure dialogue ID within speaker ID, as per other corpora + AlignmentTester
		if (id.startsWith(dialogueName)) {
			id = id.replaceFirst(dialogueName, dialogueName + ":");
		} else {
			id = dialogueName + ":" + id;
		}
		String gender = node.getAttributes().getNamedItem("sex").getNodeValue();
		String name = null;
		String age = null;
		String occupation = null;
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals("age")) {
				age = list.item(i).getFirstChild().getNodeValue();
			} else if (list.item(i).getNodeName().equals("occupation")) {
				occupation = list.item(i).getFirstChild().getNodeValue();
			} else if (list.item(i).getNodeName().equals("persName")) {
				name = list.item(i).getFirstChild().getNodeValue();
			}
		}
		String firstName = name;
		String lastName = null;
		if (name != null) {
			Matcher m = Pattern.compile("^(.+)\\s+(.+)$").matcher(name);
			if (m.matches()
					&& !(id.equalsIgnoreCase(UNKNOWN_SINGLE_SPEAKER) || id.equalsIgnoreCase(UNKNOWN_GROUP_SPEAKER))) {
				firstName = m.group(1);
				lastName = m.group(2);
			}
		}
		DialogueSpeaker spk = new DialogueSpeaker(id, firstName, lastName, gender, age, occupation);
		getSpeakerMap().put(id, spk);
		getSpeakerMap().put(origId, spk);
		// System.out.println("set spk " + id + " " + spk);
		// System.out.println("set spk " + origId + " " + spk);
		return success;
	}

	private class TextGridFileFilter implements FilenameFilter {

		private String pattern;

		private TextGridFileFilter(String a, int b) {
			this.pattern = ".+" + b + ".+" + a + ".+TextGrid";
			System.out.println("new file pattern " + pattern);
		}

		@Override
		public boolean accept(File dir, String name) {
			return name.matches(pattern);
		}

	}

	protected ArrayList<DialogueWord<Word>> wordTimings = null;

	private void getWordTimings(Dialogue dialogue) {
		String dialogueName = dialogue.getId();
		int divNo = 1;
		if (dialogue.getId().contains(":")) {
			String[] bits = dialogue.getId().split(":");
			dialogueName = bits[0];
			divNo = Integer.parseInt(bits[1]);
		}
		System.out.println("Looking for Praat TextGrid file for " + dialogueName + " " + divNo + " ...");
		wordTimings = new ArrayList<DialogueWord<Word>>();
		File dir = new File(TIMING_DIR);
		File[] files = dir.listFiles(new TextGridFileFilter(dialogueName, divNo));
		if (files == null || files.length != 1) {
			System.out.println("Problem finding files for " + dialogueName + " " + divNo + ": " + files);
			wordTimings = null;
		} else {
			for (File file : files) {
				System.out.println("Reading Praat TextGrid file " + file + " ...");
				ArrayList<String> lines = new ArrayList<String>();
				try {
					FileUtils.getFileLines(file, lines);
				} catch (IOException e) {
					e.printStackTrace();
				}
				boolean found1 = false;
				boolean found2 = false;
				boolean started = false;
				float startTime = -1.0f;
				float endTime = -1.0f;
				int num = 1;
				for (String line : lines) {
					if (started && line.equals("\"IntervalTier\"")) {
						found1 = found2 = started = false; // stop at next IntervalTier
					}
					if (started) {
						try {
							float f = Float.parseFloat(line);
							if (startTime < 0) {
								startTime = f;
							} else {
								endTime = f;
							}
						} catch (NumberFormatException e) {
							if (line.startsWith("\"")) {
								line = line.substring(1);
							}
							if (line.endsWith("\"")) {
								line = line.substring(0, line.length() - 1);
							}
							DialogueWord<Word> w = new DialogueWord<Word>(dialogue.getId() + " " + num, num++,
									dialogue, null, new Word(line));
							w.setStartTime(startTime);
							w.setEndTime(endTime);
							// System.out.println("found word " + w);
							wordTimings.add(w);
							startTime = -1.0f;
							endTime = -1.0f;
						}
					}
					found2 = (found1 && line.equals("\"word\""));
					if (found2) {
						started = true; // start at IntervalTier followed by "word"
					}
					found1 = line.equals("\"IntervalTier\"");
				}
				System.out.println("Read " + wordTimings.size() + " wordTimings " + wordTimings.get(0) + " "
						+ wordTimings.get(wordTimings.size() - 1));
			}
		}
	}

	private boolean getDivs(String dialogueName, NodeList list) {
		int autoDivNo = 0; // some e.g. KS7 have no <div> numbering
		if (firstNonEmptyNodeName(list).equals("div")) {
			for (int i = 0; i < list.getLength(); i++) {
				if (list.item(i).getNodeName().equals("div")) {
					Node divN = list.item(i).getAttributes().getNamedItem("n");
					int divNo = (divN == null ? ++autoDivNo : Integer.parseInt(divN.getNodeValue()));
					// System.out.println("g " + getGenreMap().get(dialogueName));
					if (!getSubdialogue(dialogueName + ":" + divNo, getGenreMap().get(dialogueName), list.item(i)
							.getChildNodes())) {
						return false;
					}
				} else {
					System.err.println("not all divs " + list.item(i));
					System.exit(0);
				}
			}
		} else {
			if (!getSubdialogue(dialogueName, getGenreMap().get(dialogueName), list)) {
				return false;
			}
		}
		return true;
	}

	private boolean getSubdialogue(String dialogueName, String genre, NodeList list) {
		Dialogue dialogue = addDialogue(dialogueName, genre);
		if (getTimings) {
			getWordTimings(dialogue);
		}
		if (!getTurns(dialogue, list)) {
			return false;
		}
		if (!checkDialogue(dialogue)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#addDialogue(java.lang.String, java.lang.String)
	 */
	@Override
	public Dialogue addDialogue(String id, String genre) {
		// ensure it's a BNCDialogue that gets added
		Dialogue dialogue = new BNCDialogue(this, id, genre);
		this.getDialogues().add(dialogue);
		this.getGenreMap().put(id, genre);
		return dialogue;
	}

	private boolean getTurns(Dialogue dialogue, NodeList list) {
		boolean success = true;
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals("u")) {
				String spkId = list.item(i).getAttributes().getNamedItem("who").getNodeValue();
				DialogueSpeaker spk = getSpeaker(dialogue.getId(), spkId);
				DialogueTurn turn = dialogue.addTurn(-1, spk);
				if (turn != null) {
					// System.out.println("adding turn for spkid " + spkId);
					if (!getSentences(dialogue, turn, list.item(i).getChildNodes())) {
						success = false;
					}
					// System.out.println("turn " + turn);
					// } else {
					// System.out.println("null turn for " + spk + " " + removeUnknowns);
				}
			}
		}
		return success;
	}

	/**
	 * @param dialogueId
	 * @param speakerId
	 * @return the speaker with this ID in this dialogue, optionally making a new unknown speaker if the BNC people have
	 *         messed up & left the old SGML PS000/PS001 "unknown" IDs in the XML files
	 */
	private DialogueSpeaker getSpeaker(String dialogueId, String speakerId) {
		DialogueSpeaker speaker = getSpeakerMap().get(speakerId);
		if (speaker == null) {
			String test;
			// for "unknown" speakers only, try to find using the XML "unknown" ID
			if (speakerId.equals(SGML_UNKNOWN_SINGLE_SPEAKER) || speakerId.endsWith(UNKNOWN_SINGLE_SPEAKER)) {
				test = UNKNOWN_SINGLE_SPEAKER;
			} else if (speakerId.equals(SGML_UNKNOWN_GROUP_SPEAKER) || speakerId.endsWith(UNKNOWN_GROUP_SPEAKER)) {
				test = UNKNOWN_GROUP_SPEAKER;
			} else {
				throw new RuntimeException("Can't find speaker for known ID " + speakerId);
			}
			speaker = getSpeakerMap().get(test);
			if (speaker == null) {
				test = (dialogueId.contains(":") ? dialogueId.substring(0, dialogueId.indexOf(":")) : dialogueId) + ":"
						+ test;
				speaker = getSpeakerMap().get(test);
			}
			// if not there because header missing info, create the "unknown" speaker for this dialogue
			if (speaker == null) {
				String name = (test.endsWith(UNKNOWN_SINGLE_SPEAKER) ? "Unknown speaker" : "Group of unknown speakers");
				speaker = new DialogueSpeaker(test, name, null, null, null, null);
				getSpeakerMap().put(test, speaker);
				System.err.println("WARNING: creating missing unknown speaker " + speaker + " for " + speakerId);
			}
		}
		return speaker;
	}

	private boolean getSentences(Dialogue dialogue, DialogueTurn turn, NodeList list) {
		DialogueSentence s = null;
		if (firstNonEmptyNodeName(list).equals("s")) {
			for (int i = 0; i < list.getLength(); i++) {
				if (list.item(i).getNodeName().equals("s")) {
					String sentNoStr = list.item(i).getAttributes().getNamedItem("n").getNodeValue();
					int sentNo = (sentNoStr.contains("_") ? Integer.parseInt(sentNoStr.replaceFirst("_", ""))
							: (Integer.parseInt(sentNoStr) * 10));
					ArrayList<TaggedWord> taggedWords = new ArrayList<TaggedWord>();
					ArrayList<TaggedWord> taggedLemmas = new ArrayList<TaggedWord>();
					Pair<String, String> trans = getTranscription(list.item(i).getChildNodes(), taggedWords,
							taggedLemmas);
					String transcription = trans.first();
					List<Word> tokens = tok.getWordsFromString(trans.second());
					System.out.println(sentNo + " trans " + transcription);
					System.out.println(sentNo + " toks " + tokens);
					s = dialogue.addSent(sentNo, turn, transcription, null);
					s.setTokens(tokens);
					s.setTaggedWords(taggedWords);
					s.setTaggedLemmas(taggedLemmas);
					if (getTimings && (wordTimings != null)) {
						addTimings(s, turn, dialogue);
						System.out.println("matched sent " + s.getId() + " " + s.getStartTime() + "-" + s.getEndTime());
						System.out.println(" so turn " + turn.getId() + " " + turn.getStartTime() + "-"
								+ turn.getEndTime());
					}
				} else {
					if (list.item(i).getNodeName().equals("#text") && list.item(i).getNodeValue().trim().isEmpty()) {
						// just some intervening white space
					} else {
						System.err.println("unexpected u child " + list.item(i));
						System.exit(0);
					}
				}
			}
		} else {
			// System.out.println("no s because " + firstNonEmptyNodeName(list));
			// whole turn (unclear/laugh etc) without a <s> in
			ArrayList<TaggedWord> taggedWords = new ArrayList<TaggedWord>();
			ArrayList<TaggedWord> taggedLemmas = new ArrayList<TaggedWord>();
			Pair<String, String> trans = getTranscription(list, taggedWords, taggedLemmas);
			String transcription = trans.first();
			List<Word> tokens = tok.getWordsFromString(trans.second());
			System.out.println(-1 + " trans " + transcription);
			System.out.println(-1 + " toks " + tokens);
			s = dialogue.addSent(-1, turn, transcription, null);
			s.setTokens(tokens);
			s.setTaggedWords(taggedWords);
			s.setTaggedLemmas(taggedLemmas);
			// // Looks like nonverbal turns don't have matching TextGrid timings
			// if (getTimings && (wordTimings != null)) {
			// addTimings(s, turn, dialogue);
			// }
			// System.out.println("Added as " + s.getNum() + " = " + s.getTranscription());
		}
		return (s != null);
	}

	/**
	 * get the corresponding subsequences from head of wordTimings and current position in a token list (timings
	 * contains e.g. "it's" when tokens have separate "it", "'s")
	 * 
	 * @param tokens
	 * @param iT
	 * @return the number of members to match, or null if no match
	 */
	private Pair<Integer, Integer> matchTimings(List<HasWord> tokens, int iT) {
		return matchTimings(tokens, iT, 0, 0, "", ""); // yes I know, I used to program in Prolog
	}

	protected static String lastTim = "";

	/**
	 * @param tim
	 * @param tok
	 * @return whether a textgrid token tim matches a BNC token tok
	 */
	private boolean matchTokens(String tim, String tok) {
		if (tim.equals(tok) || tim.replaceAll("['.]", "").equals(tok.replaceAll("['.]", ""))
				|| (tim.startsWith("{gap_") && tok.startsWith("gap_"))) {
			return true;
		} else if (tim.equals("{oov}")
				|| tim.equals("{ns}")
				// case for compounds e.g. "d{oov}" matching "d'ya", "bric-{oov}-brac" matching "bric-à-brac"
				|| (tim.contains("{oov}") && tok.matches(tim.replace("{oov}", ".*").replaceAll("([\\{\\}\\[\\]])",
						"\\$1")))) {
			return true;
			// } else if (tok.equals("unclear")) { // actually expect this to match {oov}
			// return true;
		}
		return false;
	}

	/**
	 * get the corresponding subsequences from head of wordTimings and current position in a token list (timings
	 * contains e.g. "it's" when tokens have separate "it", "'s"; and tokens have hyphenated e.g. "off-licence" when
	 * timings have separate "off", "licence")
	 * 
	 * @param tokens
	 * @param iT
	 * @param iTim
	 * @param iTok
	 * @param timSoFar
	 * @param tokSoFar
	 * @return the number of members to match, or null if no match
	 */
	private Pair<Integer, Integer> matchTimings(List<HasWord> tokens, int iT, int iTim, int iTok, String timSoFar,
			String tokSoFar) {
		if ((iTim >= wordTimings.size()) || ((iT + iTok) >= tokens.size())) {
			return null;
		}
		while (wordTimings.get(iTim).getWord().word().equals("sp") // pauses don't match tokens
				|| wordTimings.get(iTim).getWord().word().matches("^\\{(LG|CG|BR|XX)\\}$")) { // laughter, cough etc
			iTim++;
		}
		String tim = timSoFar + wordTimings.get(iTim).getWord().word().toLowerCase();
		String tok = tokSoFar + tokens.get(iT + iTok).word().replaceAll("/", "").toLowerCase(); // "ab" matches "a/b"
		String lastTok = ((iT + iTok) > 0 ? tokens.get(iT + iTok - 1).word().toLowerCase() : ""); // TODO not if
																									// tokSoFar
		String nextTok = ((iT + iTok) < (tokens.size() - 1) ? tokens.get(iT + iTok + 1).word().toLowerCase() : "");
		String nextTim = (iTim < (wordTimings.size() - 1) ? wordTimings.get(iTim + 1).getWord().word().toLowerCase()
				: "");
		String nextNonSpTim = nextTim;
		int iTmp = iTim + 1;
		while ((nextNonSpTim.equals("sp") || nextNonSpTim.matches("^\\{(lg|cg|br|xx)\\}$"))
				&& (iTmp < (wordTimings.size() - 1))) {
			nextNonSpTim = wordTimings.get(iTmp + 1).getWord().word().toLowerCase();
			iTmp++;
		}
		System.out.println("Matching " + tim + " " + tok + " (" + lastTim + " " + lastTok + ") (" + nextTim + " "
				+ nextNonSpTim + " " + nextTok + ")");
		if (tim.equals("{oov}")
				&& !(tok.equals("unclear") || tok.equals("truncated_word"))
				&& (!nextNonSpTim.isEmpty() && !nextNonSpTim.equals("{oov}") && !nextTok.isEmpty()
						&& matchTokens(nextNonSpTim, tok) && !matchTokens(nextNonSpTim, nextTok))) {
			// match 1 against 0 if a (usually sentence-initial) {oov} matches nothing
			// or a matching {oov} can match more e.g. "bric-{oov}" matching "bric-à-brac" when "brac" is coming next
			System.out.println("clause 1");
			return matchTimings(tokens, iT, iTim + 1, iTok, timSoFar, tokSoFar);
		} else if (tim.contains("{oov}")
				&& !(tok.equals("unclear") || tok.equals("truncated_word"))
				&& (!nextNonSpTim.isEmpty() && !nextNonSpTim.equals("{oov}") && !nextTok.isEmpty()
						&& matchTokens(tim + nextNonSpTim, tok) && !matchTokens(nextNonSpTim, nextTok))) {
			// match 2 against 1 if {oov} can match more e.g. "bric-{oov}" matching "bric-à-brac" when "brac" is coming
			System.out.println("clause 1a");
			return matchTimings(tokens, iT, iTim + 1, iTok, tim, tokSoFar);
			// TRY WITHOUT: gets to K6J with
			// } else if (tim.equals("{oov}")
			// && !(tok.equals("unclear") || tok.equals("truncated_word") || tok.startsWith("gap_"))
			// && (!nextNonSpTim.isEmpty() && !nextTok.isEmpty() && !matchTokens(nextNonSpTim, nextTok))) {
			// // match 1 against many if a {oov} has to match more than one token
			// System.out.println("clause 2a");
			// return matchTimings(tokens, iT, iTim, iTok + 1, timSoFar, tok);
		} else if (tim.equals("oov") && lastTok.equals("truncated_word") && nextTok.endsWith(tok)) {
			// match {oov} 1 against 2 if they are repeated self-repairs
			System.out.println("clause 2");
			return new Pair<Integer, Integer>(iTim + 1, iTok + 2);
		} else if (matchTokens(tim, tok) || (tok.startsWith("'") && matchTokens(tim, lastTok + tok))
				|| (tim.equals("t") && tok.equals("n't"))) {
			// standard - match 1 against 1
			System.out.println("clause 3");
			return new Pair<Integer, Integer>(iTim + 1, iTok + 1);
		} else if (tok.equals("unclear")) {
			// match 0 against 1 for [unclear] token if it didn't match {oov} in clause above
			System.out.println("clause 4");
			return new Pair<Integer, Integer>(iTim, iTok + 1);
		} else if (tok.endsWith("truncated_word")) {
			// match 2 against 1 for cases where truncated part is split "o'clo[ck]-", "we'v-", "re-lau-", "he's-",
			System.out.println("clause 5");
			// if ((tim.equals("o") && nextTim.startsWith("clo")) || (tim.equals("we") && nextTim.equals("v"))
			// || (tim.equals("re") && nextTim.equals("lau")) || (tim.equals("organo") && nextTim.equals("{oov}"))
			// || (tim.equals("he") && nextTim.equals("s")) || (tim.equals("non") && nextTim.equals("gav"))
			// || (tim.equals("re") && nextTim.equals("writ")) || (tim.equals("krook") && nextTim.equals("lo"))
			// || (tim.equals("multi") && nextNonSpTim.equals("c"))
			// || (tim.equals("pre") && nextNonSpTim.equals("element"))
			// || (tim.equals("re") && nextNonSpTim.equals("in")) || (tim.equals("d") && nextNonSpTim.equals("y"))) {
			if (tok.startsWith(tim + nextNonSpTim)) {
				return matchTimings(tokens, iT, iTim + 1, iTok, tim + "'", tokSoFar);
			}
			// else match 1 against 1
			return new Pair<Integer, Integer>(iTim + 1, iTok + 1);
		} else if ((tim.contains("'") || nextTok.equals("n't") || nextTok.equals("s"))
				&& (!tok.contains("'") || tok.startsWith("o'") || tok.equals("'n'") || tok.equals("'em"))
				&& tim.startsWith(tok.replaceFirst("^'", ""))) {
			System.out.println("clause 6");
			// contractions: match 1 in textgrids against 2 in tokenised transcript ("i've" vs "i 've")
			if (nextTok.matches("^(ve|re|ll|m|s|un|uns|all|dear|er)$")) {
				tok += "'"; // sometimes BNC transcription misses off '
			}
			return matchTimings(tokens, iT, iTim, iTok + 1, timSoFar, tok);
			// } else if (!tim.contains("-") && tok.contains("-") && tok.startsWith(tim)) {
		} else if (tok.contains("-") && tok.equals(tim + "-")) {
			System.out.println("clause 7");
			// hyphenations: occasionally match 2 in textgrids against 2 in tokenised transcript ("anti X" vs "anti- X")
			return matchTimings(tokens, iT, iTim + 1, iTok + 1, tim + "-", tok);
		} else if (tok.contains("-") && tok.startsWith(tim + "-")) {
			// hyphenations: usually match 2 in textgrids against 1 in tokenised transcript ("semi X" vs "semi-X")
			System.out.println("clause 8");
			return matchTimings(tokens, iT, iTim + 1, iTok, tim + "-", tokSoFar);
		} else if (tok.replace(".", "").startsWith(tim)) {
			// compounds: match 2 in textgrids against 1 in tokenised transcript ("wa n na" vs "wanna")
			// (including "man/woman" which will then match when / removed)
			System.out.println("clause 9");
			if (tok.startsWith(nextNonSpTim)) {
				// but in some cases, ditch the initial e.g. "s self"
				return matchTimings(tokens, iT, iTim + 1, iTok, timSoFar, tokSoFar);
			} else {
				return matchTimings(tokens, iT, iTim + 1, iTok, tim, tokSoFar);
			}
		} else if (tim.equals("sa") && tokens.get(iT + iTok - 1).word().toLowerCase().equals("truncated_word")) {
			// special (because unsafe) case to match truncated & already hyphenated words
			System.out.println("clause 10");
			return new Pair<Integer, Integer>(iTim + 1, iTok);
		} else if (matchTokens(tim, nextTok) || tok.startsWith("gap_") || tok.equals("hunslet")) {
			// match 0 against 1 for if there's a missing word in the TextGrids (see e.g. JNG, JNH) or "hunslet" in KGP
			System.out.println("clause 11");
			return new Pair<Integer, Integer>(iTim, iTok + 1);
		}
		return null;
	}

	/**
	 * Add start/end times at sentence, turn and dialogue level, from textgrid wordTimings
	 * 
	 * @param s
	 * @param turn
	 * @param dialogue
	 */
	private void addTimings(DialogueSentence s, DialogueTurn turn, Dialogue dialogue) {
		int iT = 0;
		ArrayList<HasWord> myTokens = new ArrayList<HasWord>(s.getTokens());
		if (!removePunctuationTokens) {
			// if haven't previously removed punctuation-only tokens, remove them now (replacing ampersands)
			myTokens.clear();
			for (HasWord token : s.getTokens()) {
				if (token.word().matches("^&$")) {
					myTokens.add(new Word("and"));
				}
				if (token.word().matches(".*\\w.*")) {
					myTokens.add(token);
				}
			}
		}
		while (iT < myTokens.size()) {
			// System.out.println("iT = " + iT + " len " + wordTimings.size());
			while ((wordTimings.size() > 0) && (wordTimings.get(0).getWord().word().equals("sp") // pauses don't match
					|| wordTimings.get(0).getWord().word().matches("^\\{(LG|CG|BR|XX)\\}$"))) { // laughter, cough etc
				wordTimings.remove(0);
			}
			Pair<Integer, Integer> m = matchTimings(myTokens, iT);
			if (m != null) {
				float start = wordTimings.get(0).getStartTime();
				// System.out.println("got start " + start + " vs " + turn.getStartTime() + " " + s.getStartTime());
				if (Float.isNaN(turn.getStartTime())) {
					turn.setStartTime(start);
				}
				if (Float.isNaN(s.getStartTime())) {
					s.setStartTime(start);
				}
				if (Float.isNaN(dialogue.getStartTime()) || (start < dialogue.getStartTime())) {
					dialogue.setStartTime(start);
				}
				float end = wordTimings.get(m.first() > 0 ? m.first() - 1 : 0).getEndTime();
				turn.setEndTime(end);
				s.setEndTime(end);
				if (Float.isNaN(dialogue.getEndTime()) || (end > dialogue.getEndTime())) {
					dialogue.setEndTime(end);
				}
				for (int i = 0; i < m.first(); i++) {
					lastTim = wordTimings.get(0).getWord().word().toLowerCase();
					wordTimings.remove(0);
				}
				iT += m.second();
			} else {
				System.out.println("mismatch " + (wordTimings.isEmpty() ? "[]" : wordTimings.get(0).getWord().word())
						+ " vs " + (myTokens.isEmpty() ? "[]" : myTokens.get(iT)) + " (" + wordTimings.size() + " "
						+ myTokens.size() + ") " + myTokens.get(0).word().toLowerCase());
				if (wordTimings.isEmpty() && ((myTokens.size() - iT) < 2)) {
					// could just have come across a "mute" or similar
					System.out.println(" " + turn.getStartTime() + " " + turn.getEndTime());
					return;
				} else {
					// something seriously wrong
					System.exit(0);
				}
			}
		}
	}

	/**
	 * @param list
	 * @param taggedWords
	 *            to hold the PoS-tagged & tokenised transcription
	 * @param taggedLemmas
	 *            to hold the PoS-tagged & stemmed transcription
	 * @return a pair of transcriptions: first one is for reading (includes transcriber notes, non-vocal sounds etc),
	 *         second one is for parsing
	 */
	private Pair<String, String> getTranscription(NodeList list, ArrayList<TaggedWord> taggedWords,
			ArrayList<TaggedWord> taggedLemmas) {
		String trans1 = "";
		String trans2 = "";
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals("w")) {
				// word
				String w = list.item(i).getFirstChild().getNodeValue();
				// fix bug in KC1 s440 he'd've
				w = w.replaceFirst("^he'd'\\s*$", "he'd");
				// fix bug in KCS s1976 m=Mm, s22051 y=Yes, KCU s7900 j=Just, s9510 p=bonk, HUW s114 an=and etc etc
				w = w.replaceFirst("^\\w+=(\\w)", "$1");
				// fix bug in KDE s1750 then.Come
				w = w.replaceFirst("([a-z]\\.)([A-Z])", "$1 $2");
				boolean remove = (removeFilledPauses && w.matches("(?i)^er(m)?$"))
						|| (removeBackchannels && w.matches("(?i)^(h|m)m+$"));
				trans1 += (remove ? " " : w);
				trans2 += (remove ? " " : w);
				getTagAndHeadWord(list.item(i), w, taggedWords, taggedLemmas);
			} else if (list.item(i).getNodeName().equals("c")) {
				// punctuation
				String w = list.item(i).getFirstChild().getNodeValue();
				trans1 += w;
				trans2 += (removePunctuationTokens ? " " : w);
				if (!removePunctuationTokens) {
					getTagAndHeadWord(list.item(i), w, taggedWords, taggedLemmas);
				}
			} else if (list.item(i).getNodeName().equals("pause")) {
				trans1 += " [pause] ";
				trans2 += " ";
			} else if (list.item(i).getNodeName().equals("unclear")) {
				trans1 += " [unclear] ";
				trans2 += " UNCLEAR ";
			} else if (list.item(i).getNodeName().equals("vocal")) {
				trans1 += " [" + list.item(i).getAttributes().getNamedItem("desc").getNodeValue() + "] ";
				trans2 += " ";
			} else if (list.item(i).getNodeName().equals("gap")) {
				Node desc = list.item(i).getAttributes().getNamedItem("desc");
				if (desc == null) {
					desc = list.item(i).getAttributes().getNamedItem("reason");
				}
				trans1 += " [" + desc.getNodeValue() + "] ";
				// trans2 += " " + desc.getNodeValue().replaceAll("\\s+", "_").toUpperCase() + " ";
				// prevent tokeniser splitting entities e.g. TRAVEL_NEWS+WEATHER
				trans2 += " GAP_" + desc.getNodeValue().replaceAll("(\\s+|\\+)", "_").toUpperCase() + " ";
			} else if (list.item(i).getNodeName().equals("event")) {
				trans1 += " [" + list.item(i).getAttributes().getNamedItem("desc").getNodeValue() + "] ";
				trans2 += " ";
			} else if (list.item(i).getNodeName().equals("trunc")) {
				Pair<String, String> trans = getTranscription(list.item(i).getChildNodes(), taggedWords, taggedLemmas);
				trans1 += " " + trans.first() + "-";
				trans2 += " "
						+ (replaceTruncatedTokens ? trans.second().replaceFirst("\\S+\\s*$", "truncated_word") : trans
								.second()) + " ";
			} else if (list.item(i).getNodeName().equals("mw")) {
				Pair<String, String> trans = getTranscription(list.item(i).getChildNodes(), taggedWords, taggedLemmas);
				trans1 += " " + trans.first() + " ";
				trans2 += " " + trans.second() + " ";
			} else if (list.item(i).getNodeName().equals("corr")) {
				Pair<String, String> trans = getTranscription(list.item(i).getChildNodes(), taggedWords, taggedLemmas);
				trans1 += " " + trans.first() + " ";
				trans2 += " " + trans.second() + " ";
			} else if (list.item(i).getNodeName().equals("align")) {
				// do nothing for now
			} else if (list.item(i).getNodeName().equals("shift")) {
				if (list.item(i).hasAttributes()) {
					trans1 += " [" + list.item(i).getAttributes().getNamedItem("new").getNodeValue() + "] ";
					trans2 += " ";
				} else {
					// do nothing for now
				}
			} else {
				if (list.item(i).getNodeName().equals("#text") && list.item(i).getNodeValue().trim().isEmpty()) {
					// just some intervening white space
				} else if (list.item(i).getNodeName().equals("#comment")) {
					// just an annotator comment
					System.err.println("Annotator comment: " + list.item(i).getNodeValue());
				} else {
					System.err.println("unrecognised s child " + list.item(i));
					System.exit(0);
				}
			}
		}
		// fix bug in KB7 s8235 (and a few other places) extraneous ) character
		// System.out.println("raw " + trans1);
		// System.out.println("raw " + trans2);
		if (trans1.matches("^[^\\(]*\\).*")) {
			trans1 = trans1.replaceFirst("\\)", " ");
			trans2 = trans2.replaceFirst("\\)", " ");
		}
		// System.out.println("raww " + trans1);
		// System.out.println("raww " + trans2);
		return new Pair<String, String>(trans1.replaceAll("\\s+", " ").trim(), trans2.replaceAll("\\s+", " ").trim());
	}

	private String firstNonEmptyNodeName(NodeList list) {
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals("#text") && list.item(i).getNodeValue().trim().isEmpty()) {
				// ignore
			} else {
				return list.item(i).getNodeName();
			}
		}
		return null;
	}

	private void getTagAndHeadWord(Node node, String w, ArrayList<TaggedWord> taggedWords,
			ArrayList<TaggedWord> taggedLemmas) {
		if (node.getAttributes().getNamedItem("c5") != null) {
			String tag = node.getAttributes().getNamedItem("c5").getNodeValue();
			taggedWords.add(new TaggedWord(w.trim(), tag));
			if (node.getAttributes().getNamedItem("hw") != null) {
				String hw = node.getAttributes().getNamedItem("hw").getNodeValue();
				taggedLemmas.add(new TaggedWord(hw.trim(), tag));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#badNumSpeakers(qmul.corpus.Dialogue)
	 */
	@Override
	protected boolean badNumSpeakers(Dialogue dialogue) {
		BNCDialogue d = (BNCDialogue) dialogue;
		// in the BNC, we (usually) don't want to count the official "unknown" speakers as extra people, as they
		// (usually) just mean the transcribers weren't sure which one of the known speakers was talking
		if ((d.numSpeakers() < getMinSpeakers())
				|| ((d.numKnownSpeakers() > getMaxSpeakers()) && (getMaxSpeakers() > 0))) {
			System.out.println("Dialogue " + d.getId() + " - bad number of speakers " + d.numSpeakers() + " ("
					+ d.numKnownSpeakers() + " " + d.numUnknownSpeakers() + ")");
			return true;
		}
		// but we may also want to check there aren't too many "unknown" speaker turns
		if (d.numUnknownTurns() > BNCCorpus.maxNumUnknownTurns) {
			System.out.println("Dialogue " + d.getId() + " - bad number of unknown turns " + d.numUnknownTurns());
			return true;
		}
		int total = (removeUnknowns ? (d.numTurns() + d.numUnknownTurns()) : d.numTurns());
		if (((double) d.numUnknownTurns() / (double) total) > BNCCorpus.maxPropUnknownTurns) {
			System.out.println("Dialogue " + d.getId() + " - bad proportion of unknown turns " + d.numUnknownTurns()
					+ " of " + total);
			return true;
		}
		return false;
	}

	/**
	 * @param speaker
	 * @return true iff speaker is the "unknown speaker" PSUNK or the "unknown group" PSUGP
	 */
	public static boolean isUnknown(DialogueSpeaker speaker) {
		return (speaker.getId().endsWith(UNKNOWN_SINGLE_SPEAKER) || speaker.getId().endsWith(UNKNOWN_GROUP_SPEAKER));
	}

	/**
	 * @param dialogue
	 * @param speaker
	 * @return true iff speaker is the "unknown speaker" PSUNK or the "unknown group" PSUGP from the given dialogue
	 */
	public static boolean isUnknown(Dialogue dialogue, DialogueSpeaker speaker) {
		return (speaker.getId().equalsIgnoreCase(dialogue.getId() + ":" + UNKNOWN_SINGLE_SPEAKER) || speaker.getId()
				.equalsIgnoreCase(dialogue.getId() + ":" + UNKNOWN_GROUP_SPEAKER));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#topTenSynProductions()
	 */
	@Override
	public HashSet<String> topTenSynProductions() {
		return new HashSet<String>(Arrays.asList("S[dcl]:NP:S[dcl]\\NP", "NP:N", "NP[nb]:NP[nb]/N:N", "N:N/N:N",
				"NP:NP:NP\\NP", "S[dcl]:S[dcl]:.", "S[dcl]\\NP:(S[dcl]\\NP)/NP:NP", "S[dcl]:S[X]/S[X]:S[dcl]",
				"NP:NP[nb]:NP\\NP", "S[dcl]\\NP:(S[dcl]\\NP)/(S[b]\\NP):S[b]\\NP"));
	}

	/**
	 * just for testing
	 * 
	 * @args put in your local corpus base dir if you want
	 */
	public static void main(String[] args) {

		if (args.length > 0) {
			System.out.println("CL args: " + Arrays.asList(args));
		}

		// to create a parsed corpus:
		String raw = "bnc";
		// String raw = "bnc_mono";
		// String raw = "bnc_nointj";
		// String raw = "bnc_mono_nointj";
		if (getTimings) {
			raw = raw.replaceFirst("bnc", "bnc_timed");
		}
		File rawBnc = new File(raw + ".corpus.gz");
		BNCCorpus bnc = (BNCCorpus) BNCCorpus.readFromFile(rawBnc);
		if (bnc == null) {
			if (!raw.contains("mono")) {
				// BNCCorpus.setRemoveUnknowns(true); // must be left in to align with Praat timings
				BNCCorpus.setAssignUnknowns(false);
				BNCCorpus.setMaxPropUnknownTurns(0.2);
			}
			if (raw.contains("nointj")) {
				System.out
						.println("nointj specified - removing filled pauses, backchannels; plus punctuation for parsers ...");
				BNCCorpus.setRemoveFilledPauses(true);
				// BNCCorpus.setRemoveBackchannels(true);
				BNCCorpus.setRemovePunctuationTokens(true);
			}
			int minSpeakers = 2;
			if (raw.contains("mono")) {
				BNCCorpus.setIncludeMonologue(true);
				minSpeakers = 1;
			}
			if ((args.length > 0) && !args[0].equals("dummy")) {
				System.out.println("Found arg, using non-default base dir " + args[0]);
				bnc = new BNCCorpus(args[0], minSpeakers, 2, 0, 0);
			} else {
				bnc = new BNCCorpus(minSpeakers, 2, 0, 0);
			}
			bnc.writeToFile(rawBnc);
		}
		String parserName = "ccg";
		if (args.length > 1) {
			parserName = args[1].trim().toLowerCase();
		}
		boolean leaveExisting = true;
		if (args.length > 2) {
			leaveExisting = Boolean.parseBoolean(args[2]);
		}
		File parsedBnc = new File(raw + "_" + parserName + ".corpus.gz");
		if (parsedBnc.exists()) {
			bnc = (BNCCorpus) BNCCorpus.readFromFile(parsedBnc);
		}
		Parser parser = null;
		if (parserName.equals("ccg")) {
			CreateTreeFromClarkCurranCCGProlog.setOption(CreateTreeFromClarkCurranCCGProlog.REMOVE_PUNCTUATION, true);
			parser = ((args.length > 4) ? new ClarkCurranParser(args[3], args[4]) : new ClarkCurranParser());
		} else if (parserName.equals("rasp")) {
			parser = ((args.length > 4) ? new RASPParser(args[3], args[4]) : new RASPParser());
		} else if (parserName.equals("stanford")) {
			parser = ((args.length > 3) ? new StanfordParser(args[3]) : new StanfordParser());
		}
		if (parser != null) {
			CorpusParser.setParser(parser);
			CorpusParser.setLeaveExisting(leaveExisting);
			if (CorpusParser.parse(bnc) > 0) {
				bnc.writeToFile(parsedBnc);
			}
		}
	}
}
