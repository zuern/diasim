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

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import qmul.util.parse.ClarkCurranParser;
import qmul.util.parse.CreateTreeFromClarkCurranCCGProlog;
import qmul.util.parse.RASPParser;
import qmul.util.parse.StanfordParser;
import csli.util.FileUtils;
import edu.stanford.nlp.parser.Parser;

/**
 * A {@link DialogueCorpus} implementation for the Santa Barbara Corpus of Spoken American English
 * 
 * @author mpurver
 */
public class SBCSAECorpus extends DialogueCorpus {

	private static final long serialVersionUID = -8277027701130649226L;

	public static final boolean REMOVE_CA_NOTATION = true;
	public static final boolean REMOVE_MANY_SPEAKER_TURNS = true;
	public static final boolean REMOVE_ENV_TURNS = true;
	public static final boolean REMOVE_UNKNOWN_TURNS = true;

	private static final String BASE_DIR = "/import/imc-corpora/corpora/sbcsae";

	/**
	 * @param id
	 *            an ID for this dataset
	 * @param dir
	 *            the dir to read all files from
	 */
	public SBCSAECorpus(String id, String dir) {
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
		Pattern pTurn = Pattern.compile("^\\s*([\\d\\.]+)\\s([\\d\\.]+)\\s(\\S+)?\\s(.*?)\\s*$");
		FilenameFilter tmp = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".trn");
			}
		};
		for (File file : getDir().listFiles(tmp)) {
			System.out.println("File: " + file);
			ArrayList<String> lines = new ArrayList<String>();
			try {
				lines = (ArrayList<String>) FileUtils.getFileLines(file, lines);
				String genre = "default";
				Dialogue d = addDialogue(file.getName().replaceFirst("\\.trn$", ""), genre);
				getGenreCounts()
						.put(genre, (getGenreCounts().get(genre) == null ? 0 : getGenreCounts().get(genre)) + 1);
				DialogueTurn turn = null;
				for (String line : lines) {
					line = line.trim();
					Matcher m = pTurn.matcher(line);
					if (m.matches()) {
						float startTime = Float.parseFloat(m.group(1));
						float endTime = Float.parseFloat(m.group(2));
						String spkName = m.group(3);
						String trans = m.group(4);
						if (trans != null && !trans.isEmpty()) {
							// System.out.println(spkName + ": " + trans);
							if (REMOVE_CA_NOTATION) {
								trans = trans.replaceAll(
										"\\.\\.+|<<?(\\S+)|(\\S+)>>?|=|~|@|%|--|\\[\\d?|\\d?\\]|\\(Hx?\\)", "");
								trans = trans.replaceAll("�", "'");
								trans = trans.replaceAll("\\s+", " ");
							}
						}
						if ((spkName != null) && !spkName.isEmpty()) {
							spkName = spkName.toUpperCase().replace(":", ""); // some occasionally vary case
							if (spkName.startsWith("$") // comment lines
									|| (REMOVE_UNKNOWN_TURNS && (spkName.equals("X") || spkName.equals("*X")))
									|| (REMOVE_ENV_TURNS && spkName.startsWith(">")) // >ENV, >DOG etc
									|| (REMOVE_MANY_SPEAKER_TURNS && spkName.equals("MANY"))) {
								System.out.println("Ignoring turn by " + spkName);
							} else {
								// sometimes transcriber wasn't sure of speaker - go for first possibility
								spkName = spkName.replaceAll("/.*", "");
								// ensure dialogue ID within speaker ID, as per other corpora + AlignmentTester
								spkName = d.getId() + ":" + spkName;
								// new speaker turn
								DialogueSpeaker spk = getSpeakerMap().get(spkName);
								if (spk == null) {
									spk = new DialogueSpeaker(spkName, spkName, null, null, null, null);
									getSpeakerMap().put(spkName, spk);
								}
								d.getSpeakers().add(spk);
								System.out.println("Turn: " + turn);
								turn = d.addTurn(-1, spk);
								turn.setStartTime(startTime);
								turn.setEndTime(endTime);
								DialogueSentence sent = d.addSent(-1, turn, trans, null);
								sent.setStartTime(startTime);
								sent.setEndTime(endTime);
							}
						} else {
							// new sentence in existing speaker turn
							DialogueSentence sent = d.addSent(-1, turn, trans, null);
							sent.setStartTime(startTime);
							sent.setEndTime(endTime);
							if (endTime > turn.getEndTime()) {
								turn.setEndTime(endTime);
							}
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
		String dir = BASE_DIR;
		if ((args.length > 0) && !args[0].equals("dummy")) {
			System.out.println("Found arg, using non-default base dir " + args[0]);
			dir = args[0];
		}
		SBCSAECorpus c = new SBCSAECorpus("sbcsae", dir);
		c.writeToFile(new File("sbcsae.corpus.gz"));
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
				c.writeToFile(new File("sbcsae_" + parserName + ".corpus.gz"));
			}
		}
	}

	/**
	 * Overrides the default writeToFile method. Saves the SBCSAECorpus to the disk in pieces to avoid out of memory
	 * issues.
	 * @param file The file to write to
	 * @return true if successful, false otherwise
     */
	@Override
	public boolean writeToFile(File file) {
		ObjectOutputStream out;
		try {
			OutputStream outs = new FileOutputStream(file);
			if (file.getName().endsWith(".gz")) {
				outs = new GZIPOutputStream(outs);
			}
			out = new ObjectOutputStream(outs);
			out.writeObject(this.id);
			out.writeObject(this.dir);
			out.writeObject(this.dynamic);
			out.writeObject(this.dialogues);
			out.writeObject(this.speakerMap);
			out.writeObject(this.genreMap);
			out.writeObject(this.genreCounts);
			out.writeObject(this.minSpeakers);
			out.writeObject(this.maxSpeakers);
			out.writeObject(this.maxDialogues);
			out.writeObject(this.minGenreCount);
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


    public static DialogueCorpus readFromFile(File file) {
        try {
            InputStream ins = new FileInputStream(file);
            if (file.getName().endsWith(".gz")) {
                ins = new GZIPInputStream(ins);
            }
            ObjectInputStream in = new ObjectInputStream(ins);
            System.out.print("Reading corpus from file " + file + " ... ");

            String id = (String) in.readObject();
            File dir = (File) in.readObject();

            boolean dynamic = (Boolean) in.readObject();
            ArrayList<Dialogue> dialogues = (ArrayList<Dialogue>) in.readObject();
            HashMap<String, DialogueSpeaker> speakerMap = (HashMap<String, DialogueSpeaker>) in.readObject();
            HashMap<String, String> genreMap = (HashMap<String, String>) in.readObject();
            HashMap<String, Integer> genreCounts = (HashMap<String, Integer>) in.readObject();
            int minSpeakers = (Integer) in.readObject();
            int maxSpeakers = (Integer) in.readObject();
            int maxDialogues = (Integer) in.readObject();
            int minGenreCount = (Integer) in.readObject();

            SBCSAECorpus c = new SBCSAECorpus(id, file.getAbsolutePath());
            c.id = id;
            c.dir = dir;
            c.dynamic = dynamic;
            c.dialogues = dialogues;
            c.speakerMap = speakerMap;
            c.genreMap = genreMap;
            c.genreCounts = genreCounts;
            c.minSpeakers = minSpeakers;
            c.maxSpeakers = maxSpeakers;
            c.maxDialogues = maxDialogues;
            c.minGenreCount = minGenreCount;

            return c;
        }
        catch (IOException ex) {
            // todo
            return null;
        }
        catch (ClassNotFoundException ex) {
            // todo
            return null;
        }
	}

}
