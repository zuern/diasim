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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.parser.Parser;
import qmul.util.parse.ClarkCurranParser;
import qmul.util.parse.CreateTreeFromClarkCurranCCGProlog;
import qmul.util.parse.StanfordParser;

/**
 * The HCRC Map Task corpus from http://groups.inf.ed.ac.uk/maptask/ - simple transcript-only version
 * 
 * @author mpurver
 */
public class MapTaskTranscriptCorpus extends TranscriptCorpus {

	public static String DIR = "/import/imc-corpora/corpora/maptask/maptask2.1/Transcripts";

	public static final String ID = "MAPTASK";

	public static final String GENRE = "maptask";

	public MapTaskTranscriptCorpus() {
		super(ID, new File(DIR), false);
	}

	// override - we're using Edinburgh format with no DA tag
	private static final Pattern LINE_PAT = Pattern.compile("^\\s*([gf])\\s+(.*)\\s*$");

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.TranscriptCorpus#matchLine(java.lang.String, java.lang.String)
	 */
	@Override
	protected List<String> matchLine(String line, String dialogueId) {
		Matcher m = LINE_PAT.matcher(line);
		if (m.matches()) {
			String daTags = "";
			String spkId = dialogueId + ":" + m.group(1);
			String trans = m.group(2).trim();
			String[] s = { daTags, spkId, trans };
			return Arrays.asList(s);
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.TranscriptCorpus#getGenre()
	 */
	@Override
	protected String getGenre() {
		return GENRE;
	}

	/**
	 * just for testing
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			DIR = args[0];
		}
		MapTaskTranscriptCorpus c = new MapTaskTranscriptCorpus();
		c.writeToFile(new File("maptask.corpus.gz"));

		if (args.length > 1) {
			boolean leaveExisting = true;
			if (args.length > 2) {
				leaveExisting = Boolean.parseBoolean(args[2]);
			}
			Parser parser = null;
			String name = null;
			if (args[1].trim().toLowerCase().equals("ccg")) {
				CreateTreeFromClarkCurranCCGProlog.setOption(CreateTreeFromClarkCurranCCGProlog.REMOVE_PUNCTUATION,
						true);
				parser = ((args.length > 4) ? new ClarkCurranParser(args[3], args[4]) : new ClarkCurranParser());
				name = "maptask_ccg.corpus.gz";
			} else {
				parser = ((args.length > 3) ? new StanfordParser(args[3]) : new StanfordParser());
				name = "maptask_stanford.corpus.gz";
			}
			CorpusParser.setParser(parser);
			CorpusParser.setLeaveExisting(leaveExisting);
			if (CorpusParser.parse(c) > 0) {
				c.writeToFile(new File(name));
			}
		}
	}
}
