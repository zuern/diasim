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
 * Parse a corpus
 * 
 * @author mpurver
 */
public class CorpusPrinter {

	/**
	 * Print a corpus
	 * 
	 * @param corpus
	 */
	public static void print(DialogueCorpus corpus) {
		for (Dialogue d : corpus.getDialogues()) {
			for (DialogueSentence s : d.getSents()) {
				System.out.println("Dialogue " + d.getId() + " sentence " + s.getId() + " " + s.getNum());
				System.out.println(s.getTranscription());
				if (s.getSyntax() == null) {
					System.out.println(s.getSyntax());
				} else {
					System.out.println(s.getSyntax().pennString());
				}
				System.out.println();
			}
		}
	}

	public static void main(String[] args) {
		String corpusName = args[0];
		print(DialogueCorpus.readFromFile(new File(corpusName)));
	}

}
