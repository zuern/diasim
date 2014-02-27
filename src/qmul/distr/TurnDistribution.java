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

import java.util.ArrayList;

import qmul.corpus.DialogueSentence;
import qmul.corpus.DialogueTurn;

public class TurnDistribution extends LabelDistribution {

	private static final long serialVersionUID = 7975225170240780022L;

	private LabelDistribution lex = new LabelDistribution();
	private LabelDistribution preLex = new LabelDistribution();
	private LabelDistribution postLex = new LabelDistribution();
	private LabelDistribution tag = new LabelDistribution();
	private LabelDistribution preTag = new LabelDistribution();
	private LabelDistribution postTag = new LabelDistribution();

	public TurnDistribution(DialogueTurn t) {
		super();
		for (DialogueSentence s : t.getSents()) {
			// lexical
			String[] words;
			if (s.getTokens() == null || s.getTokens().isEmpty()) {
				words = s.getTranscription().replaceAll("(\\w)(\\W)", "$1 $2").replaceAll("(\\W)(\\w)", "$1 $2").split(
						"\\s+");
			} else {
				words = new String[s.getTokens().size()];
				for (int i = 0; i < words.length; i++) {
					words[i] = s.getTokens().get(i).word();
				}
			}
			for (String word : words) {
				add("LEX:" + word);
				lex.add(word);
			}
			// tags
			if (s.getDaTags() != null) {
				for (String tag : s.getDaTags()) {
					add("DATAG:" + tag);
					this.tag.add(tag);
				}
			}
		}
	}

	public TurnDistribution(LabelDistribution lex, LabelDistribution tag) {
		super();
		this.lex = lex.clone();
		this.tag = tag.clone();
	}

	/**
	 * @return the lex
	 */
	public LabelDistribution getLex() {
		return lex;
	}

	/**
	 * @return the tag
	 */
	public LabelDistribution getTag() {
		return tag;
	}

	/**
	 * @return the preLex
	 */
	public LabelDistribution getPreLex() {
		return preLex;
	}

	/**
	 * @return the postLex
	 */
	public LabelDistribution getPostLex() {
		return postLex;
	}

	/**
	 * @return the preTag
	 */
	public LabelDistribution getPreTag() {
		return preTag;
	}

	/**
	 * @return the postTag
	 */
	public LabelDistribution getPostTag() {
		return postTag;
	}

	public TurnDistribution clone() {
		return new TurnDistribution(lex, tag);
	}

	public void add(TurnDistribution td) {
		super.add(td);
		this.lex.add(td.lex);
		this.tag.add(td.tag);
		this.preLex.add(td.preLex);
		this.postLex.add(td.postLex);
		this.preTag.add(td.preTag);
		this.postTag.add(td.postTag);
	}

}
