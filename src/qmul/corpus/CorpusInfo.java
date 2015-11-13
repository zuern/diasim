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

public class CorpusInfo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		DialogueCorpus c = DialogueCorpus.readFromFile(new File(args[0]));
		c.sanityCheck();
		for (int i = 0; i < 10; i++) {
			Dialogue d = c.getDialogues().get(i);
			if (c.numDialogues() > i && d.numTurns() > i) {
				DialogueTurn t = d.getTurns().get(i);
				System.out.println("Dialogue " + i + " " + d.getId() + " turn " + i + " " + t.getId() + " "
						+ t.getStartTime() + "-" + t.getEndTime() + " speaker " + t.getSpeaker());
				if (t.getSents() != null && t.getSents().size() > 0) {
					DialogueSentence s = t.getSents().get(0);
					System.out.println("Dialogue " + i + " " + d.getId() + " turn " + i + " " + t.getId() + " "
							+ " sentence 0 " + s.getId() + " has " + s.numWords() + " words " + s.numTokens());
					System.out.println(" " + s.getTranscription());
					System.out.println(" " + s.getTokens());
					System.out.println(" " + s.getStartTime() + "-" + s.getEndTime());
				}
			}
		}

	}
}
