/*******************************************************************************
 * Copyright (c) 2015 Matthew Purver, Queen Mary University of London.
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
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import qmul.util.parse.ClarkCurranParser;
import qmul.util.parse.CreateTreeFromClarkCurranCCGProlog;
import qmul.util.parse.RASPParser;
import qmul.util.parse.StanfordParser;
import csli.util.FileUtils;
import edu.stanford.nlp.parser.Parser;

/**
 * A {@link DialogueCorpus} implementation for the ISIP version of Switchboard: includes word timings, full 2,438
 * dialogues, but no parse trees
 * 
 * @author mpurver
 */
public class SwitchboardISIPCorpus extends DialogueCorpus {

	private static final long serialVersionUID = 8222028361162158807L;

	public static boolean REMOVE_NON_VERBAL = true;

	private static final String BASE_DIR = "/import/imc-corpora/corpora/switchboard/swb_ms98_transcriptions";

	/**
	 * @param id
	 *            an ID for this dataset
	 * @param dir
	 *            the dir to read all files from
	 */
	public SwitchboardISIPCorpus(String id, String dir) {
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

	/**
	 * @param f
	 * @return a list of {@link TmpUtt}s for later sorting
	 */
	public ArrayList<TmpUtt> getUtts(File f) {
		ArrayList<String> lines = new ArrayList<String>();
		ArrayList<TmpUtt> u = new ArrayList<TmpUtt>();
		Pattern pTurn = Pattern.compile("^\\s*(\\S+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+(.*?)\\s*$");
		try {
			lines = (ArrayList<String>) FileUtils.getFileLines(f, lines);
			String lastWord = "";
			String trans = "";
			float startTime = Float.MIN_VALUE;
			float endTime = Float.MIN_VALUE;
			for (String line : lines) {
				line = line.trim();
				Matcher m = pTurn.matcher(line);
				if (m.matches()) {
					if (!m.group(4).equals("[silence]")) {
						String uttId = m.group(1);
						// add last utt to list
						if (!uttId.equals(lastWord)) {
							if (!lastWord.isEmpty()) {
								u.add(new TmpUtt(trans.trim(), lastWord, startTime, endTime));
							}
							startTime = Float.parseFloat(m.group(2));
							trans = "";
							lastWord = uttId;
						}
						// add new word to current utt
						endTime = Float.parseFloat(m.group(3));
						trans += " " + m.group(4);
					}
				}
			}
			u.add(new TmpUtt(trans, lastWord, startTime, endTime));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Got " + u.size() + " utts from file " + f.getName());
		return u;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#setupCorpus()
	 */
	@Override
	public boolean setupCorpus() {
		FilenameFilter tmp = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith("A-ms98-a-word.text"); // initially just find speaker A's files
			}
		};
		File[] subdirs = getDir().listFiles();
		for (File subdir : subdirs) {
			if (subdir.isDirectory()) {
				File[] subsubdirs = subdir.listFiles();
				for (File subsubdir : subsubdirs) {
					if (subsubdir.isDirectory()) {
						for (File file : subsubdir.listFiles(tmp)) {
							System.out.println("File: " + file);
							ArrayList<TmpUtt> uttsA = getUtts(file);
							ArrayList<TmpUtt> uttsB = getUtts(new File(file.getAbsolutePath().replace("A-", "B-")));
							uttsA.addAll(uttsB);
							Collections.sort(uttsA, new Comparator<TmpUtt>() {
								public int compare(TmpUtt o1, TmpUtt o2) {
									return Float.compare(o1.getStartTime(), o2.getStartTime());
								}
							});
							String genre = "default";
							Dialogue d = addDialogue(file.getName().replaceFirst("[AB]-.*$", ""), genre);
							getGenreCounts().put(genre,
									(getGenreCounts().get(genre) == null ? 0 : getGenreCounts().get(genre)) + 1);
							DialogueTurn turn = null;
							String lastSpk = "";
							for (TmpUtt u : uttsA) {
								String trans = u.getTrans();
								// ensure dialogue ID within speaker ID, as per other corpora + AlignmentTester
								String spkName = d.getId() + ":" + u.getId().replaceFirst("-.*$", "");
								float startTime = u.getStartTime();
								float endTime = u.getEndTime();
								if (trans != null && !trans.isEmpty()) {
									// hypothesised self-repair continuations
									trans = trans.replaceAll("(\\w+)\\[\\S+?\\]-", "$1-");
									// marked words (as discourse markers?)
									trans = trans.replaceAll("(\\w)_\\d+", "$1");
									// laughing speech - mark as laughter and keep words
									trans = trans.replaceAll("\\[laughter-(\\S+?)\\]", "[laughter] $1");
									if (REMOVE_NON_VERBAL) {
										trans = trans.replaceAll("\\[\\S+\\]", "");
										trans = trans.replaceAll("�", "'");
										trans = trans.replaceAll("\\s+", " ");
									}
								}
								if (!spkName.equals(lastSpk)) {
									// new speaker turn
									lastSpk = spkName;
									DialogueSpeaker spk = getSpeakerMap().get(spkName);
									if (spk == null) {
										spk = new DialogueSpeaker(spkName, spkName, null, null, null, null);
										getSpeakerMap().put(spkName, spk);
									}
									d.getSpeakers().add(spk);
									if (turn != null) {
										System.out.println(turn.getSpeaker().getId() + " turn: " + turn);
									}
									turn = d.addTurn(-1, spk);
									turn.setStartTime(startTime);
									turn.setEndTime(endTime);
									DialogueSentence sent = d.addSent(-1, turn, trans, null);
									sent.setStartTime(startTime);
									sent.setEndTime(endTime);
								} else {
									// new sentence in existing speaker turn
									DialogueSentence sent = d.addSent(-1, turn, trans, null);
									sent.setStartTime(startTime);
									sent.setEndTime(endTime);
									if (endTime > turn.getEndTime()) {
										turn.setEndTime(endTime);
									}
								}
							}
							if (!checkDialogue(d)) {
								return false;
							}
						}
					}
				}
			}
		}
		return sanityCheck();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String dir = BASE_DIR;
		if ((args.length > 0) && !args[0].equals("dummy")) {
			System.out.println("Found arg, using non-default base dir " + args[0]);
			dir = args[0];
		}
		SwitchboardISIPCorpus c = new SwitchboardISIPCorpus("swbd_isip", dir);
		c.writeToFile(new File("swbd_isip.corpus.gz"));
		String parserName = "ccg";
		Parser parser = null;
		if (args.length > 1) {
			parserName = args[1].trim().toLowerCase();
		}
		boolean leaveExisting = true;
		if (args.length > 2) {
			leaveExisting = Boolean.parseBoolean(args[2]);
		}
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
			if (CorpusParser.parse(c) > 0) {
				c.writeToFile(new File("swbd_isip_" + parserName + ".corpus.gz"));
			}
		}
	}

	private class TmpUtt {
		private String trans = null;
		private String id = null;
		private float startTime = Float.NaN;
		private float endTime = Float.NaN;

		public TmpUtt(String trans, String id, float startTime, float endTime) {
			this.trans = trans;
			this.id = id;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		/**
		 * @return the trans
		 */
		public String getTrans() {
			return trans;
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @return the startTime
		 */
		public float getStartTime() {
			return startTime;
		}

		/**
		 * @return the endTime
		 */
		public float getEndTime() {
			return endTime;
		}
	}

}
