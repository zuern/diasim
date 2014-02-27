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
package qmul.align;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.didion.jwnl.JWNLException;
import qmul.corpus.DialogueSentence;
import qmul.corpus.DialogueTurn;
import qmul.util.parse.PennTreebankTokenizer;
import qmul.util.similarity.SimilarityMeasure;
import csli.util.Pair;
import csli.util.nlp.Synonyms;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Word;

/**
 * WordNet distance
 * 
 * @author mpurver
 */
public class SentenceSemanticSimilarityMeasure implements SimilarityMeasure<DialogueSentence> {

	private Synonyms syn;
	private HashMap<String, HashMap<String, Double>> cache = new HashMap<String, HashMap<String, Double>>();

	private int mode = SIM_JWNL;
	public static final int SIM_JWNL = 0;
	public static final int SIM_RPI_NSS = 1;
	public static final int SIM_RPI_PMI = 2;
	public static final int SIM_RPI_LSA = 3;
	public static final int SIM_RPI_WORDNET = 4;

	public static final String MSR_SERVER = "http://cwl-projects.cogsci.rpi.edu";
	public static final String MSR_SCRIPT = "/cgi-bin/msr/msr.cgi";

	/**
	 * use the defaults
	 */
	public SentenceSemanticSimilarityMeasure(int mode) {
		super();
		setMode(mode);
	}

	/**
	 * @return the mode
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(int mode) {
		if (this.mode != mode) {
			for (String key : cache.keySet()) {
				cache.get(key).clear();
			}
			cache.clear();
			this.mode = mode;
			if ((mode == SIM_JWNL) && (syn == null)) {
				syn = new Synonyms();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#similarity(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double similarity(DialogueSentence a, DialogueSentence b) {
		List<HasWord> aTokens = a.getTokens();
		if (aTokens == null) {
			aTokens = new ArrayList<HasWord>(new PennTreebankTokenizer(false).getWordsFromString(a.getTranscription()));
		}
		List<HasWord> bTokens = b.getTokens();
		if (bTokens == null) {
			bTokens = new ArrayList<HasWord>(new PennTreebankTokenizer(false).getWordsFromString(b.getTranscription()));
		}
		double s = 1.0;
		for (HasWord aW : aTokens) {
			for (HasWord bW : bTokens) {
				Pair<String, String> ab = getWordPair(aW, bW);
				Double ps = getSimilarity(ab, null, null);
				// Double ps = getSimilarity(ab, aTokens, bTokens); // to do lookahead API calls
				if (!ps.isNaN()) {
					s *= ps;
				}
			}
		}
		return Math.pow(s, 1.0 / ((double) (aTokens.size() + bTokens.size())));
	}

	/**
	 * @param a
	 * @param b
	 * @return the pair in alphabetical order
	 */
	private Pair<String, String> getWordPair(HasWord a, HasWord b) {
		if (a.word().compareTo(b.word()) > 0) {
			return new Pair<String, String>(a.word(), b.word());
		} else {
			return new Pair<String, String>(b.word(), a.word());
		}
	}

	private Double getSimilarity(Pair<String, String> pair, List<HasWord> aTokens, List<HasWord> bTokens) {
		System.out.print("Checking pair " + pair + " ... ");
		Double sim = getCache(pair);
		if (sim != null) {
			System.out.println("cached.");
			return sim;
		}
		System.out.println("calculating ...");
		sim = Double.NaN;
		if (mode == SIM_JWNL) {
			try {
				sim = syn.similarityMetric(pair.first(), pair.second());
			} catch (JWNLException e) {
				e.printStackTrace();
				System.exit(0);
			}
		} else {
			// String apiURL = "http://cwl-projects.cogsci.rpi.edu/cgi-bin/msr/msr.cgi?msrs=1 ";
			String method;
			switch (mode) {
			case SIM_RPI_LSA:
				method = "LSA-tasa";
				break;
			case SIM_RPI_NSS:
				method = "NSS-G";
				break;
			case SIM_RPI_PMI:
				method = "PMI-G";
				break;
			case SIM_RPI_WORDNET:
				method = "WordnetVector-UMN";
				break;
			default:
				method = "NSS-G";
				break;
			}
			// if we're going to do a API call, might as well send the whole lot to avoid http overhead
			String quotedTermsA = (aTokens == null ? ("[" + quoteEscape(pair.first()) + "]") : quoteEscape(aTokens)
					.toString());
			String quotedTermsB = (bTokens == null ? ("[" + quoteEscape(pair.second()) + "]") : quoteEscape(bTokens)
					.toString());
			String urlStr = MSR_SERVER + MSR_SCRIPT + "?msr=" + method + "&terms=" + quotedTermsA + "&terms2="
					+ quotedTermsB;
			Pattern p1 = Pattern.compile("^\\s*<a\\s+href=(.+?)>.*");
			Pattern p2 = Pattern.compile("Step (\\d+) of \\1 complete");
			Pattern p3 = Pattern.compile("Please reload");
			try {
				// urlStr =
				// "http://cwl-projects.cogsci.rpi.edu/output/2010-10-29-10-38-41-138.37.2.37-WordnetVector-UMN-output.xls.txt";
				URL u = new URL(urlStr);
				System.out.println("Writing query1 " + u);
				BufferedReader br = new BufferedReader(new InputStreamReader(u.openStream()));
				String res;
				boolean found = false;
				while (!found && ((res = br.readLine()) != null)) {
					System.out.println(res);
					Matcher m = p1.matcher(res);
					if (m.matches()) {
						urlStr = MSR_SERVER + m.group(1);
						u = new URL(urlStr);
						found = true;
					}
				}
				br.close();
				boolean ready = false;
				while (!ready) {
					System.out.println("Writing query2 " + u);
					br = new BufferedReader(new InputStreamReader(u.openStream()));
					while (!ready && ((res = br.readLine()) != null)) {
						System.out.println(res);
						Matcher m = p2.matcher(res);
						if (m.find()) {
							ready = true;
						}
						m = p3.matcher(res);
						if (m.find()) {
							Thread.sleep(10000);
						}
					}
					br.close();
				}
				u = new URL(urlStr.replaceAll(".txt", ""));
				System.out.println("Writing query3 " + u);
				br = new BufferedReader(new InputStreamReader(u.openStream()));
				while ((res = br.readLine()) != null) {
					System.out.println(res);
					// tab-separated text, despite the .xls suffix
					if (!res.trim().isEmpty()) {
						String[] fields = res.split("\\t");
						String term1 = fields[0];
						String term2 = fields[1];
						Double termSim = (fields[2].equalsIgnoreCase("none") ? Double.NaN : new Double(fields[2]));
						Pair<String, String> termPair = getWordPair(new Word(term1), new Word(term2));
						setCache(termPair, termSim);
						if (termPair.equals(pair)) {
							sim = termSim;
						}
					}
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		setCache(pair, sim);
		return sim;
	}

	private Double getCache(Pair<String, String> pair) {
		if (cache.containsKey(pair.first()) && cache.get(pair.first()).containsKey(pair.second())) {
			return cache.get(pair.first()).get(pair.second());
		}
		return null;
	}

	private void setCache(Pair<String, String> pair, Double val) {
		if (!cache.containsKey(pair.first())) {
			cache.put(pair.first(), new HashMap<String, Double>());
		}
		cache.get(pair.first()).put(pair.second(), val);
	}

	private List<String> quoteEscape(List<HasWord> in) {
		ArrayList<String> out = new ArrayList<String>();
		for (HasWord hw : in) {
			out.add(quoteEscape(hw.word()));
		}
		return out;
	}

	private String quoteEscape(String s) {
		if (s.contains("'")) {
			return ("\"" + s.replaceAll("\"", "") + "\"");
		} else {
			return ("'" + s + "'");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#reset()
	 */
	@Override
	public void reset() {
		// nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#rawCountsA()
	 */
	@Override
	public HashMap<? extends Object, Integer> rawCountsA() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#rawCountsAB()
	 */
	@Override
	public HashMap<? extends Object, Integer> rawCountsAB() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#rawCountsB()
	 */
	@Override
	public HashMap<? extends Object, Integer> rawCountsB() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		SentenceSemanticSimilarityMeasure sm = new SentenceSemanticSimilarityMeasure(
				SentenceSemanticSimilarityMeasure.SIM_RPI_NSS);
		String s1 = "You're full of catarrh.";
		String s2 = "Lot of wax in it, right enough.";
		// System.exit(0);

		DialogueTurn t = new DialogueTurn("t", 1, null, null);
		DialogueSentence a = new DialogueSentence("a", 1, t, "ok");
		DialogueSentence b = new DialogueSentence("b", 1, t, "ok");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		a.setTranscription("ok ok ok ok");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		a.setTranscription("that's really not ok");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		b.setTranscription("that's really not ok");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		a.setTranscription("john likes the small bear");
		b.setTranscription("jim likes the small bear");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		b.setTranscription("jim likes the small rabbit");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		b.setTranscription("jim likes the big rabbit");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		b.setTranscription("jim loves the big rabbit");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
		a.setTranscription("the man likes the small bear");
		System.out.println("" + a + "\n" + b + "\n" + "sim = " + sm.similarity(a, b));
	}

}
