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
package qmul.distr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import qmul.corpus.Dialogue;
import qmul.corpus.DialogueCorpus;
import qmul.corpus.DialogueTurn;

public class CorpusDistribution {

	private DialogueCorpus corpus;

	private ArrayList<String> turns = new ArrayList<String>();
	private HashMap<String, TurnDistribution> turnDistr = new HashMap<String, TurnDistribution>();
	private ArrayList<String> tags = new ArrayList<String>();
	private HashMap<String, TurnDistribution> tagDistr = new HashMap<String, TurnDistribution>();
	private HashSet<String> words = new HashSet<String>();

	public CorpusDistribution(DialogueCorpus corpus) {
		this.corpus = corpus;
		for (Dialogue d : corpus.getDialogues()) {
			getDistr(d);
		}
	}

	private void getDistr(Dialogue d) {
		for (DialogueTurn t : d.getTurns()) {
			TurnDistribution td = new TurnDistribution(t);
			words.addAll(td.getLex().keySet());
			turnDistr.put(t.getId(), td);
			turns.add(t.getId());
			for (String tag : td.getTag().keySet()) {
				if (tagDistr.containsKey(tag)) {
					tagDistr.get(tag).add(td);
				} else {
					System.out.println("new tag " + tag);
					tagDistr.put(tag, td.clone());
					tags.add(tag);
				}
			}
		}
		double win = 5.0;
		for (int i = 0; i < d.numTurns(); i++) {
			DialogueTurn t = d.getTurns().get(i);
			int start = Math.max(0, (int) (i - win));
			int end = Math.min(d.numTurns(), (int) (i + win));
			for (int j = start; j < i; j++) {
				DialogueTurn tj = d.getTurns().get(j);
				double wgt = ((double) (i - j)) / win;
				turnDistr.get(t.getId()).getPreLex().add(turnDistr.get(tj.getId()).getLex(), wgt);
				turnDistr.get(t.getId()).getPreTag().add(turnDistr.get(tj.getId()).getTag(), wgt);
				for (String tag : t.getDaTags()) {
					tagDistr.get(tag).getPreLex().add(turnDistr.get(tj.getId()).getLex(), wgt);
					tagDistr.get(tag).getPreTag().add(turnDistr.get(tj.getId()).getTag(), wgt);
				}
			}
			for (int j = i + 1; j < end; j++) {
				DialogueTurn tj = d.getTurns().get(j);
				double wgt = ((double) (j - i)) / win;
				turnDistr.get(t.getId()).getPostLex().add(turnDistr.get(tj.getId()).getLex(), wgt);
				turnDistr.get(t.getId()).getPostTag().add(turnDistr.get(tj.getId()).getTag(), wgt);
				for (String tag : t.getDaTags()) {
					tagDistr.get(tag).getPostLex().add(turnDistr.get(tj.getId()).getLex(), wgt);
					tagDistr.get(tag).getPostTag().add(turnDistr.get(tj.getId()).getTag(), wgt);
				}
			}
			// d.get
		}
	}

	public void toM(File file) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write("id = '" + corpus.getId() + "';\n");
			bw.write("words = cell(" + words.size() + ",1);\n");
			ArrayList<String> sortWords = new ArrayList<String>(words);
			Collections.sort(sortWords);
			for (int i = 0; i < sortWords.size(); i++) {
				bw.write("words{" + (i + 1) + "} = '" + sortWords.get(i).replaceAll("'", "''") + "';\n");
			}
			bw.write("turns = cell(" + turns.size() + ",1);\n");
			// features x turns matrix
			bw.write("turnLex = zeros(" + words.size() + "," + turns.size() + ");\n");
			for (int i = 0; i < turns.size(); i++) {
				// for (int i = 0; i < 3; i++) {
				String t = turns.get(i);
				bw.write("turns{" + (i + 1) + "} = '" + t + "';\n");
				bw.write("turnLex(:," + (i + 1) + ") = " + turnDistr.get(t).getLex().toM(sortWords) + ".';\n");
				// bw.write("turnLex(:," + (i + 1) + ") = " + turnDistr.get(t).getLex() + ".';\n");
			}
			ArrayList<String> sortTags = new ArrayList<String>(tags);
			Collections.sort(sortTags);
			bw.write("tags = cell(" + tags.size() + ",1);\n");
			// features x tags matrix
			bw.write("tagLex = zeros(" + words.size() + "," + tags.size() + ");\n");
			bw.write("tagPretag = zeros(" + tags.size() + "," + tags.size() + ");\n");
			bw.write("tagPostag = zeros(" + tags.size() + "," + tags.size() + ");\n");
			for (int i = 0; i < sortTags.size(); i++) {
				// for (int i = 0; i < 3; i++) {
				String t = sortTags.get(i);
				bw.write("tags{" + (i + 1) + "} = '" + t + "';\n");
				bw.write("tagLex(:," + (i + 1) + ") = " + tagDistr.get(t).getLex().toM(sortWords) + ".';\n");
				bw.write("tagPretag(:," + (i + 1) + ") = " + tagDistr.get(t).getPreTag().toM(sortTags) + ".';\n");
				bw.write("tagPostag(:," + (i + 1) + ") = " + tagDistr.get(t).getPostTag().toM(sortTags) + ".';\n");
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		DialogueCorpus c = DialogueCorpus.readFromFile(new File("/import/imc-corpora/data/distr/ami.corpus"));
		for (Dialogue d : new ArrayList<Dialogue>(c.getDialogues().subList(3, c.numDialogues()))) {
			c.removeDialogue(d);
		}
		CorpusDistribution d = new CorpusDistribution(c);
		d.toM(new File("ami.m"));

	}

}
