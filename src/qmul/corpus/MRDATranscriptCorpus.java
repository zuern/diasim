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
 * The ICSI-MRDA corpus from http://www.icsi.berkeley.edu/Speech/mr/ - simple transcript-only version
 * 
 * @author mpurver
 */
public class MRDATranscriptCorpus extends TranscriptCorpus {

	public static final String DIR = "/import/imc-corpora/data/distr/mrda";

	public static final String ID = "MRDA";

	public static final String GENRE = "mrda";

	public MRDATranscriptCorpus() {
		super(ID, new File(DIR), false);
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

		MRDATranscriptCorpus c = new MRDATranscriptCorpus();
		c.writeToFile(new File("mrda.corpus"));
	}
}
