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

/**
 * A user-defined (from command line) {@link TranscriptCorpus}
 * 
 * @author mpurver
 */
public class UserTranscriptCorpus extends TranscriptCorpus {

	public static String DIR = "/path/to/data";

	public static String ID = "MY_ID";

	public static String GENRE = "my_genre";

	public UserTranscriptCorpus() {
		super(ID, new File(DIR), false);
	}

	public UserTranscriptCorpus(String id, String dir, String genre) {
		super(id, new File(dir), false);
		ID = id;
		DIR = dir;
		GENRE = genre;
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
		if (args.length != 3) {
			System.err.println("Need exactly 3 arguments: data_dir corpus_id corpus_genre");
			System.exit(-1);
		}
		UserTranscriptCorpus c = new UserTranscriptCorpus(args[0], args[1], args[2]);
		c.writeToFile(new File(ID + ".corpus.gz"));
	}
}
