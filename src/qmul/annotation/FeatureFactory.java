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
package qmul.annotation;

import qmul.corpus.DialogueSentence;
import qmul.util.parse.PennTreebankTokenizer;
import qmul.util.parse.StanfordParser;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import csli.util.nlp.postag.StanfordTagger;
import edu.stanford.nlp.ling.TaggedWord;

public class FeatureFactory {

	public static final String FIRST_POS = "firstPos";
	public static final String LAST_POS = "lastPos";
	public static final String FIRST_POS_BIGRAM = "firstPosBigram";
	public static final String LAST_POS_BIGRAM = "lastPosBigram";
	public static final String FIRST_WORD = "firstWord";
	public static final String LAST_WORD = "lastWord";
	public static final String FIRST_WORD_BIGRAM = "firstWordBigram";
	public static final String LAST_WORD_BIGRAM = "lastWordBigram";
	public static final String FIRST_LEMMA = "firstLemma";
	public static final String LAST_LEMMA = "lastLemma";
	public static final String FIRST_LEMMA_BIGRAM = "firstLemmaBigram";
	public static final String LAST_LEMMA_BIGRAM = "lastLemmaBigram";
	public static final String MIRROR_WORD_COUNT = "mirrorWordCount";
	public static final String MIRROR_WORD_PROPORTION = "mirrorWordProp";
	public static final String WORD_COUNT = "wordCount";
	public static final String CR_KEYWORDS = "crKeyWords";
	public static final String PARSE_PROB = "parseProb";
	public static final String PATIENT = "patient";

	public static final String CONTINUES = "continues";
	public static final String END_COMPLETE = "endComplete";

	public static final String YES = "y";
	public static final String NO = "n";

	public static final String NGRAM_SEP = "#";

	public static final String[] crKeyWords1 = new String[] { "pardon", "what you mean", "(what )?do you mean" };
	public static final String[] crKeyWords2 = new String[] { "pardon", "wh(o|en|at|ere)" };

	private static StanfordParser parser = null;
	private static StanfordTagger tagger = null;
	private static PennTreebankTokenizer tokenizer = null;

	public static void setAttribute(Instance inst, Attribute attribute, DialogueSentence sent) {
		setAttribute(inst, attribute, sent, null, null);
	}

	public static void setAttribute(Instance inst, Attribute attribute, DialogueSentence sent,
			DialogueSentence prevSent, DialogueSentence nextSent) {

		if ((sent.getTokens() == null) && (sent.getTranscription() != null)) {
			if (tokenizer == null) {
				tokenizer = new PennTreebankTokenizer(false);
			}
			sent.setTokens(tokenizer.getWordsFromString(sent.getTranscription()));
		}
		if ((sent.getTaggedWords() == null) && (sent.getTokens() != null)) {
			if (tagger == null) {
				tagger = new StanfordTagger(
						"../util/lib/stanford-postagger-2010-05-26/models/bidirectional-distsim-wsj-0-18.tagger");
			}
			sent.setTaggedWords(tagger.tagSentence(sent.getTokens()));
		}

		if (attribute.name().equals(PATIENT)) {
			setYNAttribute(inst, attribute, sent.getSpeaker().getId().startsWith("P"));
		}

		if (sent.getTaggedWords() != null && sent.getTaggedWords().size() > 0) {
			int n = sent.getTaggedWords().size();
			if (attribute.name().equals(WORD_COUNT)) {
				inst.setValue(attribute, sent.getTokens().size());
			} else if (attribute.name().equals(FIRST_WORD)) {
				inst.setValue(attribute, sent.getTaggedWords().get(0).word());
			} else if (attribute.name().equals(LAST_WORD)) {
				inst.setValue(attribute, sent.getTaggedWords().get(n - 1).word());
			} else if (attribute.name().equals(FIRST_POS)) {
				inst.setValue(attribute, sent.getTaggedWords().get(0).tag());
			} else if (attribute.name().equals(LAST_POS)) {
				inst.setValue(attribute, sent.getTaggedWords().get(n - 1).tag());
			} else if (attribute.name().equals(CR_KEYWORDS)) {
				int count = 0;
				for (String key : crKeyWords1) {
					if (sent.getTranscription().matches("(?i).*\\b" + key + "\\b.*")) {
						count++;
					}
				}
				// more points if ONLY the pattern
				for (String key : crKeyWords2) {
					if (sent.getTranscription().matches("(?i)\\s*" + key + "\\s*")) {
						count++;
					}
				}
				inst.setValue(attribute, count);
			} else if (attribute.name().equals(PARSE_PROB)) {
				if (Double.isNaN(sent.getSyntaxProb())) {
					if (parser == null) {
						System.out.println("Initialising parser");
						parser = new StanfordParser();
					}
					if (parser.parse(sent.getTaggedWords())) {
						sent.setSyntax(parser.getBestParse());
						sent.setSyntaxProb(parser.getScore());
					} else {
						sent.setSyntaxProb(0.0);
					}
				}
				inst.setValue(attribute, sent.getSyntaxProb());
			}
			if (n > 1) {
				if (attribute.name().equals(FIRST_WORD_BIGRAM)) {
					inst.setValue(attribute, sent.getTaggedWords().get(0).word() + NGRAM_SEP
							+ sent.getTaggedWords().get(1).word());
				} else if (attribute.name().equals(LAST_WORD_BIGRAM)) {
					inst.setValue(attribute, sent.getTaggedWords().get(n - 2).word() + NGRAM_SEP
							+ sent.getTaggedWords().get(n - 1).word());
				} else if (attribute.name().equals(FIRST_POS_BIGRAM)) {
					inst.setValue(attribute, sent.getTaggedWords().get(0).tag() + NGRAM_SEP
							+ sent.getTaggedWords().get(1).tag());
				} else if (attribute.name().equals(LAST_POS_BIGRAM)) {
					inst.setValue(attribute, sent.getTaggedWords().get(n - 2).tag() + NGRAM_SEP
							+ sent.getTaggedWords().get(n - 1).tag());
				}
			}
			if (prevSent != null) {
				if (attribute.name().equals(MIRROR_WORD_COUNT) || attribute.name().equals(MIRROR_WORD_PROPORTION)) {
					int max = 0;
					for (int i = 0; i < sent.getTaggedWords().size(); i++) {
						int count = 0;
						for (int j = 0; j < prevSent.getTaggedWords().size(); j++) {
							if (((i + count) < sent.getTaggedWords().size())
									&& mirrors(sent.getTaggedWords().get(i + count), prevSent.getTaggedWords().get(j))) {
								count++;
								max = Math.max(count, max);
							} else {
								if (count > 0) {
									j -= (count - 1);
								}
								count = 0;
							}
						}
					}
					if (attribute.name().equals(MIRROR_WORD_COUNT)) {
						inst.setValue(attribute, max);
					} else {
						inst.setValue(attribute, ((double) max) / ((double) sent.getTaggedWords().size()));
					}
				}
			}

		} else {
			System.out.println("No tagged words: " + sent.getTranscription());
		}
		if (sent.getTaggedLemmas() != null && sent.getTaggedLemmas().size() > 0) {
			int n = sent.getTaggedWords().size();
			if (attribute.name().equals(FIRST_LEMMA)) {
				inst.setValue(attribute, sent.getTaggedLemmas().get(0).word());
			} else if (attribute.name().equals(LAST_LEMMA)) {
				inst.setValue(attribute, sent.getTaggedLemmas().get(sent.getTaggedLemmas().size() - 1).word());
			} else if (n > 1) {
				if (attribute.name().equals(FIRST_LEMMA_BIGRAM)) {
					inst.setValue(attribute, sent.getTaggedLemmas().get(0).word() + NGRAM_SEP
							+ sent.getTaggedWords().get(1).word());
				} else if (attribute.name().equals(LAST_LEMMA_BIGRAM)) {
					inst.setValue(attribute, sent.getTaggedWords().get(n - 2).word() + NGRAM_SEP
							+ sent.getTaggedWords().get(n - 1).word());
				}
			}
		}
	}

	public static boolean mirrors(TaggedWord a, TaggedWord b) {
		return mirrors(a.word(), b.word());
	}

	public static boolean mirrors(String a, String b) {
		if (a.equalsIgnoreCase(b)) {
			return true;
		} else if (a.matches("(?i)(I|me|my|myself|we|us|our|ours|ourselves)")
				&& b.matches("(?i)(you|your|yours|yourselves)")) {
			return true;
		} else if (b.matches("(?i)(I|me|my|myself|we|us|our|ours|ourselves)")
				&& a.matches("(?i)(you|your|yours|yourselves)")) {
			return true;
		}
		return false;
	}

	public static void setYNAttribute(Instance inst, Attribute attribute, boolean value) {
		setYNAttribute(inst, attribute, (value ? YES : NO));
	}

	public static void setYNAttribute(Instance inst, Attribute attribute, String value) {
		if (value.trim().isEmpty()) {
			value = NO;
		} else {
			value = YES;
		}
		inst.setValue(attribute, value);
	}

	public static void setYNClass(Instance inst, boolean value) {
		setYNClass(inst, (value ? YES : NO));
	}

	public static void setYNClass(Instance inst, String value) {
		if (value.trim().isEmpty()) {
			value = NO;
		} else {
			value = YES;
		}
		inst.setClassValue(value);
	}

	public static Attribute newYNAttribute(String name) {
		FastVector yn = new FastVector();
		yn.addElement(YES);
		yn.addElement(NO);
		return new Attribute(name, yn);
	}

	public static Attribute newAttribute(String name) {
		if (name.equals(WORD_COUNT) || name.equals(MIRROR_WORD_COUNT) || name.equals(MIRROR_WORD_PROPORTION)
				|| name.equals(PARSE_PROB) || name.equals(CR_KEYWORDS)) {
			// numeric attributes
			return new Attribute(name);
		} else if (name.equals(FIRST_WORD) || name.equals(FIRST_WORD_BIGRAM) || name.equals(LAST_WORD)
				|| name.equals(LAST_WORD_BIGRAM) || name.equals(FIRST_LEMMA) || name.equals(FIRST_LEMMA_BIGRAM)
				|| name.equals(LAST_LEMMA) || name.equals(LAST_LEMMA_BIGRAM) || name.equals(FIRST_POS)
				|| name.equals(FIRST_POS_BIGRAM) || name.equals(LAST_POS) || name.equals(LAST_POS_BIGRAM)) {
			// string attributes
			return new Attribute(name, (FastVector) null);
		} else if (name.equals(PATIENT)) {
			return newYNAttribute(name);
		}
		return null;
	}

}
