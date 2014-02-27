/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;
import net.didion.jwnl.dictionary.Dictionary;

/**
 * @author mpurver
 */
public class Synonyms {

	private static final String propsFile = "../util/lib/jwnl14-rc2/config/file_properties.xml";

	public static void main(String[] args) {
		try {
			new Synonyms().test();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	public Synonyms() {
		System.out.println("Using WordNet as specified in " + propsFile);
		try {
			// initialize JWNL (this must be done before JWNL can be used)
			JWNL.initialize(new FileInputStream(this.propsFile));
			System.out.println("Initialized JWNL.");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IllegalArgumentException("Cannot initialize JWNL");
			// WAS: System.exit(-1);
		}
	}

	public double similarityMetric(String s1, String s2) throws JWNLException {
		IndexWordSet iws1 = Dictionary.getInstance().lookupAllIndexWords(s1);
		IndexWordSet iws2 = Dictionary.getInstance().lookupAllIndexWords(s2);
		List<?> pointerTypes = PointerType.getAllPointerTypes();
		double val = 0.0;
		int denom = 0;
		for (Iterator it1 = iws1.getIndexWordCollection().iterator(); it1.hasNext();) {
			IndexWord word1 = (IndexWord) it1.next();
			Synset[] synset1 = word1.getSenses();
			for (int i = 0; i < synset1.length; i++) {
				for (Iterator it2 = iws2.getIndexWordCollection().iterator(); it2.hasNext();) {
					IndexWord word2 = (IndexWord) it2.next();
					Synset[] synset2 = word2.getSenses();
					for (int j = 0; j < synset2.length; j++) {
						denom++;
						for (Iterator itP = pointerTypes.iterator(); itP.hasNext();) {
							PointerType ptype = (PointerType) itP.next();
							// System.out.println(i + " " + j + " " + ptype.getLabel());
							RelationshipList list = RelationshipFinder.getInstance().findRelationships(
									word1.getSense(i + 1), word2.getSense(j + 1), ptype);
							// System.out.println(list.size());
							for (Iterator itR = list.iterator(); itR.hasNext();) {
								Relationship r = (Relationship) itR.next();
								val += 1.0 / ((double) r.getDepth() + 1.0);
							}
						}
					}
				}
			}
		}
		val /= denom;
		return val;
	}

	public boolean areSynonyms(String s1, String s2) throws JWNLException {
		IndexWordSet iws1 = Dictionary.getInstance().lookupAllIndexWords(s1);
		IndexWordSet iws2 = Dictionary.getInstance().lookupAllIndexWords(s2);
		for (Iterator it1 = iws1.getIndexWordCollection().iterator(); it1.hasNext();) {
			IndexWord word1 = (IndexWord) it1.next();
			Synset[] synset1 = word1.getSenses();
			for (int i = 0; i < synset1.length; i++) {
				for (Iterator it2 = iws2.getIndexWordCollection().iterator(); it2.hasNext();) {
					IndexWord word2 = (IndexWord) it2.next();
					Synset[] synset2 = word2.getSenses();
					for (int j = 0; j < synset2.length; j++) {
						RelationshipList list = RelationshipFinder.getInstance().findRelationships(
								word1.getSense(i + 1), word2.getSense(j + 1), PointerType.SIMILAR_TO);
						if (list.size() > 0) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public void test() throws JWNLException {
		for (String w1 : new String[] { "apple", "funny", "penguin" }) {
			for (String w2 : new String[] { "orange", "fruit", "core", "droll", "peculiar", "eagle", "bird", "biscuit" }) {
				System.out.println(w1 + " " + w2 + " = " + similarityMetric(w1, w2));
			}
		}
		System.exit(0);
		IndexWord word1 = Dictionary.getInstance().lookupIndexWord(POS.NOUN, "apple");
		// POS.ADJECTIVE, "funny");
		IndexWord word2 = Dictionary.getInstance().lookupIndexWord(POS.NOUN, "core");
		// POS.ADJECTIVE, "droll");
		Synset[] synset1 = word1.getSenses();
		Synset[] synset2 = word2.getSenses();
		for (int i = 0; i < synset1.length; i++) {
			System.out.println(synset1[i]);
		}
		for (int j = 0; j < synset2.length; j++) {
			System.out.println(synset2[j]);
		}
		List pt = PointerType.getAllPointerTypes();
		for (int i = 0; i < synset1.length; i++) {
			for (int j = 0; j < synset2.length; j++) {
				for (Iterator it = pt.iterator(); it.hasNext();) {
					PointerType ptype = (PointerType) it.next();
					RelationshipList list = RelationshipFinder.getInstance().findRelationships(word1.getSense(i + 1),
							word2.getSense(j + 1), ptype);
					if (list.size() > 0) {
						System.out.println(ptype.getLabel() + " relationship between \"" + word1.getLemma()
								+ "\" and \"" + word2.getLemma() + "\":");
						for (Iterator itr = list.iterator(); itr.hasNext();) {
							Relationship r = (Relationship) itr.next();
							r.getNodeList().print();
							System.out.println("Depth: " + r.getDepth());
						}
					}
				}
			}
		}
	}
}