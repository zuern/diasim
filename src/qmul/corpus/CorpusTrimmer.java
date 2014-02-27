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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.HasWord;

/**
 * Trim a corpus, removing specified dialogues and/or those with given characteristics
 * 
 * @author mpurver
 */
public class CorpusTrimmer {

	/**
	 * Remove dialogues containing (one-sided) BNC telephone conversations, on the basis of markup, which will be in the
	 * {@link DialogueSentence} transcription but not in the word tokens. It would probably be cleaner to do this when
	 * building the corpus in the first place (could then look directly for XML &lt;event desc="..."&gt; markup) but I'm
	 * trying to avoid having to re-run parsers ...
	 * 
	 * @param corpus
	 *            the corpus to trim (which gets modified)
	 * @return the number of {@link Dialogue}s removed
	 */
	public static int removePhone(DialogueCorpus corpus) {
		int iD = 0;
		int iR = 0;
		// "[phonecall starts/ends]" or "[telephone conversation starts]"
		Pattern p1 = Pattern.compile("\\[[^\\]]*\\b(tele)?phone(call)?\\b[^\\]]*\\b(starts|ends)\\b",
				Pattern.CASE_INSENSITIVE);
		// "[speaking/talking on (the) (tele)phone]" or just "[on the phone]"
		Pattern p2 = Pattern.compile("\\[[^\\]]*\\bon\\b[^\\]]*\\b(tele)?phone\\b", Pattern.CASE_INSENSITIVE);
		for (Dialogue d : new ArrayList<Dialogue>(corpus.getDialogues())) {
			System.out.println("Checking dialogue " + d.getId() + ", " + ++iD + " of " + corpus.getDialogues().size());
			for (DialogueSentence s : d.getSents()) {
				String trans = s.getTranscription();
				List<? extends HasWord> words = s.getTokens();
				String toks = (words == null ? "" : words.toString());
				Matcher mTrans1 = p1.matcher(trans);
				Matcher mTrans2 = p2.matcher(trans);
				Matcher mToks1 = p1.matcher(toks);
				Matcher mToks2 = p2.matcher(toks);
				if ((mTrans1.find() && !mToks1.find()) || (mTrans2.find() && !mToks2.find())) {
					System.out.println("Found telephone conversation: " + trans);
					corpus.removeDialogue(d);
					iR++;
					break;
				}
			}
		}
		System.out.println("Finished (removed " + iR + " dialogues)");
		return iR;
	}

	/**
	 * Remove dialogues where one party's contributions contain no actual words except the UNCLEAR marker. Easier to do
	 * this now rather than when building the corpus in the first place (e.g. excluding UNCLEAR from tokens, although
	 * presumably keeping it in transcription), as the UNCLEAR tokens can be useful for parsing as they help maintain
	 * syntactic structure.
	 * 
	 * @param corpus
	 *            the corpus to trim (which gets modified)
	 * @return the number of {@link Dialogue}s removed
	 */
	public static int removeOneSided(DialogueCorpus corpus) {
		int iD = 0;
		int iR = 0;
		DIAL: for (Dialogue d : new ArrayList<Dialogue>(corpus.getDialogues())) {
			System.out.println("Checking dialogue " + d.getId() + ", " + ++iD + " of " + corpus.getDialogues().size());
			HashSet<DialogueSpeaker> speakersToFind = new HashSet<DialogueSpeaker>(d.getSpeakers());
			SENT: for (DialogueSentence s : d.getSents()) {
				// if we've found a proper word for all speakers, this dialogue is safe, so stop checking
				if (speakersToFind.isEmpty()) {
					continue DIAL;
				}
				// only bother checking for speakers we haven't found a proper word for yet
				if (speakersToFind.contains(s.getSpeaker())) {
					List<? extends HasWord> words = s.getTokens();
					for (HasWord w : words) {
						if (!w.word().equals("UNCLEAR")) {
							// we've found a proper word for this speaker
							speakersToFind.remove(s.getSpeaker());
							continue SENT;
						}
					}
				}
			}
			if (!speakersToFind.isEmpty()) {
				System.out.println("Found one-sided conversation: " + speakersToFind);
				corpus.removeDialogue(d);
				iR++;
			}
		}
		System.out.println("Finished (removed " + iR + " dialogues)");
		return iR;
	}

	/**
	 * @param dialogueArray
	 * @return true if we managed to find & remove all specified dialogues
	 */
	public static boolean removeDialogues(DialogueCorpus corpus, String[] dialogueArray) {
		ArrayList<String> dialogues = new ArrayList<String>(Arrays.asList(dialogueArray));
		for (String name : new ArrayList<String>(dialogues)) {
			Dialogue d = corpus.getDialogue(name);
			if (d != null) {
				corpus.removeDialogue(d);
				dialogues.remove(name);
			} else {
				System.err.println("WARNING: couldn't find dialogue " + name);
			}
		}
		return dialogues.isEmpty();
	}

	public static void main(String[] args) {
		// reading stories, talking to babies
		// Pat's initial list
		// String[] dialoguesToRemove = { "KCD:4911", "KE2:39302", "KD3:24708", "KD3:24709", "KD3:24804", "KDE:7201" };
		// Chris' more comprehensive list
		String[] dialoguesToRemove = { "KB9:62104", "KBK:2308", "KCD:4911", "KCH:76202", "KCN:27602", "KD3:24701",
				"KD3:24708", "KD3:24709", "KD3:24804", "KD4:48904", "KDE:7014", "KDE:7201", "KDW:57007", "KE2:39302",
				"KE2:39614" };
		String[] corpusNames = (args.length > 0 ? new String[] { args[0] } : new String[] { "bnc.corpus",
				"bnc_ccg.corpus", "bnc_stanford.corpus", "bnc_nointj.corpus", "bnc_nointj_ccg.corpus",
				"bnc_nointj_stanford.corpus" });
		for (String corpusName : corpusNames) {
			File trimmed = new File(corpusName.replaceFirst("(\\.|_(ccg|stanford))", "_trim$1"));
			System.out.println("Trimming corpus " + corpusName + " to create " + trimmed);
			DialogueCorpus corpus = DialogueCorpus.readFromFile(new File(corpusName));
			System.out.println("Removing flagged dialogues " + Arrays.asList(dialoguesToRemove));
			if (!removeDialogues(corpus, dialoguesToRemove)) {
				throw new RuntimeException("not all dialogues removed");
			}
			System.out.println("Removing telephone conversations ...");
			removePhone(corpus);
			System.out.println("Removing one-sided conversations ...");
			removeOneSided(corpus);
			System.out.println("Trimmed corpus to " + corpus.numDialogues() + " dialogues, " + corpus.numTurns()
					+ " turns, " + corpus.numSents() + " sentences, " + corpus.numWords() + " words.");
			System.out.println("Saving trimmed corpus as " + trimmed + " ...");
			corpus.writeToFile(trimmed);
		}
	}
}
