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
import java.util.Arrays;
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

	private static final String BASE_DIR = "/import/imc-corpora/corpora/BNC-XML";
	private static final String DATA_DIR = "Texts";

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

	private static boolean includeMonologue = false;
	public static final String DIALOGUE_TYPE = "spolog2";
	public static final String MONOLOGUE_TYPE = "spolog1";

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
		boolean success = true;
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
					// System.out.println(sentNo + " trans " + transcription);
					DialogueSentence s = dialogue.addSent(sentNo, turn, transcription, null);
					s.setTokens(tokens);
					s.setTaggedWords(taggedWords);
					s.setTaggedLemmas(taggedLemmas);
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
			DialogueSentence s = dialogue.addSent(-1, turn, transcription, null);
			s.setTokens(tokens);
			s.setTaggedWords(taggedWords);
			s.setTaggedLemmas(taggedLemmas);
			// System.out.println("Added as " + s.getNum() + " = " + s.getTranscription());
		}
		return success;
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
				trans2 += " " + desc.getNodeValue().replaceAll("\\s+", "_").toUpperCase() + " ";
			} else if (list.item(i).getNodeName().equals("event")) {
				trans1 += " [" + list.item(i).getAttributes().getNamedItem("desc").getNodeValue() + "] ";
				trans2 += " ";
			} else if (list.item(i).getNodeName().equals("trunc")) {
				Pair<String, String> trans = getTranscription(list.item(i).getChildNodes(), taggedWords, taggedLemmas);
				trans1 += " " + trans.first() + "-";
				trans2 += " " + trans.second() + " ";
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
		// String raw = "bnc";
		String raw = "bnc_mono";
		// String raw = "bnc_nointj";
		// String raw = "bnc_mono_nointj";
		File rawBnc = new File(raw + ".corpus");
		BNCCorpus bnc = (BNCCorpus) BNCCorpus.readFromFile(rawBnc);
		if (bnc == null) {
			if (!raw.contains("mono")) {
				BNCCorpus.setRemoveUnknowns(true);
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
		File parsedBnc = new File(raw + "-" + parserName + ".corpus");
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
		CorpusParser.setParser(parser);
		CorpusParser.setLeaveExisting(leaveExisting);
		if (CorpusParser.parse(bnc) > 0) {
			bnc.writeToFile(parsedBnc);
		}
	}
}
