/*
 * Created on Jun 6, 2007 by mpurver
 */
package csli.util.nlp.postag;

import java.util.ArrayList;
import java.util.List;

import csli.util.nlp.PoSTagger;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class StanfordTagger extends PoSTagger {

	private static final String DEFAULT_MODEL = "../../util/lib/stanford-postagger-2010-05-26/models/bidirectional-distsim-wsj-0-18.tagger";

	private MaxentTagger tagger;

	/**
	 * A Stanford tagger using the default WSJ-English bidirectional model
	 */
	public StanfordTagger() {
		this(DEFAULT_MODEL);
	}

	/**
	 * A Stanford tagger using a specified model
	 */
	public StanfordTagger(String model) {
		try {
			// need to initialize with the desired model
			tagger = new MaxentTagger(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.nlp.PoSTagger#getTagSeparator()
	 */
	@Override
	public String getTagSeparator() {
		return "/";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.nlp.PoSTagger#tag(java.lang.String)
	 */
	@Override
	public String tag(String sentence) {
		String tagged = null;
		try {
			tagged = tagger.tagString(sentence);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tagged.replaceAll(" $", "");
	}

	/**
	 * @param sentence
	 * @return tagged - see Stanford {@link MaxentTagger} docs
	 */
	public ArrayList<TaggedWord> tagSentence(List<? extends HasWord> sentence) {
		return tagger.tagSentence(sentence);
	}

	/**
	 * Test program. It reads text from a list of files, tags each word, and writes the result to standard output.
	 * Usage: tagger file-name file-name ...
	 */
	public static void main(String[] args) {
		test(new StanfordTagger(), args);
	}

}
