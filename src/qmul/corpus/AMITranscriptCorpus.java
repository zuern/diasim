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
 * The AMI corpus from http://amiproject.org/ - simple transcript-only version
 * 
 * @author mpurver
 */
public class AMITranscriptCorpus extends TranscriptCorpus {

	public static final String BASE_DIR = "/import/imc-corpora/data/distr/ami";

	public static final String ID = "AMI";

	public static final String AMI_GENRE = "ami";

	public AMITranscriptCorpus() {
		super(ID, new File(BASE_DIR), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.TranscriptCorpus#getGenre()
	 */
	@Override
	protected String getGenre() {
		return AMI_GENRE;
	}

	/**
	 * just for testing
	 */
	public static void main(String[] args) {

		AMITranscriptCorpus c = new AMITranscriptCorpus();
		c.writeToFile(new File("ami.corpus"));
	}
}
