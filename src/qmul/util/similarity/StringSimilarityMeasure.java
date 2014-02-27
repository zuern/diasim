/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package qmul.util.similarity;

import java.util.HashMap;
import java.util.Vector;

import qmul.util.MapUtil;

/**
 * 
 * @author user
 */

public class StringSimilarityMeasure implements SimilarityMeasure<String> {

	private static HashMap<String, Integer> countsA = new HashMap<String, Integer>();
	private static HashMap<String, Integer> countsB = new HashMap<String, Integer>();
	private static HashMap<String, Integer> countsAB = new HashMap<String, Integer>();

	private boolean kernelMeasure = true;

	public boolean isKernelMeasure() {
		return kernelMeasure;
	}

	public void setKernelMeasure(boolean kernelMeasure) {
		this.kernelMeasure = kernelMeasure;
	}

	static public Vector getAllSharedWords(Vector a, Vector b) {
		Vector shared = new Vector();
		for (int i = 0; i < a.size(); i++) {
			String s = (String) a.elementAt(i);
			for (int j = 0; j < b.size(); j++) {
				String s2 = (String) b.elementAt(j);
				if (s.equalsIgnoreCase(s2)) {
					shared.addElement(s);
					break;
				}
			}
		}
		return removeDuplicates(shared);
	}

	static public Vector getAllWordsDeletedFromAThatAreNotInBorC(Vector a, Vector b, Vector c) {
		Vector deleted = new Vector();
		Vector bUniq = removeDuplicates(b);
		Vector cUniq = removeDuplicates(c);
		for (int i = 0; i < bUniq.size(); i++) {
			cUniq.addElement(bUniq.elementAt(i));
		}
		return getAllWordsDeletedFromA(a, cUniq);
	}

	static public Vector getAllWordsDeletedFromA(Vector a, Vector b) {
		Vector deleted = new Vector();
		Vector aUniq = removeDuplicates(a);
		Vector bUniq = removeDuplicates(b);
		for (int i = 0; i < aUniq.size(); i++) {
			String s = (String) aUniq.elementAt(i);
			boolean found = false;
			for (int j = 0; j < bUniq.size(); j++) {
				String s2 = (String) bUniq.elementAt(j);
				if (s.equalsIgnoreCase(s2)) {
					found = true;
					break;
				}
			}
			if (!found) {
				deleted.addElement(s);
			}
		}
		return removeDuplicates(deleted);
	}

	static public float[] getProportionOfWordsOfAInBandBinA(String a, String b) {
		// First number is divided by total number of unique words in A
		// Second number is divided by total number of unique words in B
		if (a.length() == 0 || b.length() == 0) {
			float[] result = { 0, 0 };
			return result;
		}
		Vector<String> aVect = removeDuplicates(splitIntoWords(a));
		Vector<String> bVect = removeDuplicates(splitIntoWords(b));
		float matchingStrings = 0;
		for (int i = 0; i < aVect.size(); i++) {
			String asunique = (String) aVect.elementAt(i);
			for (int j = 0; j < bVect.size(); j++) {
				String bsunique = (String) bVect.elementAt(j);
				if (bsunique.equalsIgnoreCase(asunique))
					matchingStrings++;
			}
		}
		float[] result = new float[2];
		result[0] = 1;
		result[0] = matchingStrings / (float) aVect.size();
		result[1] = matchingStrings / (float) bVect.size();
		if (result[0] > 1)
			System.exit(-2);
		if (result[1] > 1)
			System.exit(-2);
		if (Float.isNaN(result[0]))
			result[0] = 0;
		if (Float.isNaN(result[1]))
			result[1] = 0;

		return result;

	}

	static public Vector<String> splitIntoWords(String s) {
		String sCleaned = "";
		for (int i = 0; i < s.length(); i++) {
			char schar = s.charAt(i);
			if (Character.isLetterOrDigit(schar)) {
				sCleaned = sCleaned + schar;
			} else {
				sCleaned = sCleaned + ":";
			}
		}
		// System.out.println("sCleaned: "+sCleaned);
		String[] sCleanedArray = sCleaned.split("[\\:]");
		for (int i = 0; i < sCleanedArray.length; i++) {
			sCleanedArray[i].trim();
			// System.out.println(i+" "+sCleanedArray[i]);
		}

		Vector<String> removeBlankSpace = new Vector<String>();
		for (int i = 0; i < sCleanedArray.length; i++) {
			String sNew = sCleanedArray[i].trim();
			if (sNew.length() != 0)
				removeBlankSpace.addElement(sNew);
		}

		return removeBlankSpace;
	}

	// static public Vector removeDuplicatesLexE(Vector v) {
	// Vector unique = new Vector();
	// for (int i = 0; i < v.size(); i++) {
	// LexiconEntry le = (LexiconEntry) v.elementAt(i);
	// boolean uniqueString = true;
	// for (int j = 0; j < unique.size(); j++) {
	// LexiconEntry uniq = (LexiconEntry) unique.elementAt(j);
	// String uniqW = uniq.getWord();
	// if (uniqW.equalsIgnoreCase(le.getWord())) {
	// uniqueString = false;
	// break;
	// }
	// }
	// if (uniqueString)
	// unique.addElement(le);
	//
	// }
	// // printVector(unique);
	// return unique;
	// }

	static public Vector<String> removeDuplicates(Vector<String> v) {
		Vector<String> unique = new Vector<String>();
		for (String s : v) {
			boolean uniqueString = true;
			for (String uniq : unique) {
				if (uniq.equalsIgnoreCase(s)) {
					uniqueString = false;
					break;
				}
			}
			if (uniqueString)
				unique.addElement(s);
		}
		// printVector(unique);
		return unique;
	}

	static public void printVector(Vector<String> v) {
		for (int i = 0; i < v.size(); i++) {
			String s = v.elementAt(i);
			System.out.println(i + ": " + s);
		}

	}

	public static int longestSubstr(String str_, String toCompare_) {
		if (str_.length() == 0 || toCompare_.length() == 0)
			return 0;

		int[][] compareTable = new int[str_.length()][toCompare_.length()];
		int maxLen = 0;

		for (int m = 0; m < str_.length(); m++) {
			for (int n = 0; n < toCompare_.length(); n++) {
				compareTable[m][n] = (str_.charAt(m) != toCompare_.charAt(n)) ? 0 : (((m == 0) || (n == 0)) ? 1
						: compareTable[m - 1][n - 1] + 1);
				maxLen = (compareTable[m][n] > maxLen) ? compareTable[m][n] : maxLen;
			}
		}
		return maxLen;
	}

	public static String calculatesLongestCommonSubSequence(String x, String y) {
		int M = x.length();
		int N = y.length();
		String longestSequence = "";

		// opt[i][j] = length of LCS of x[i..M] and y[j..N]
		int[][] opt = new int[M + 1][N + 1];

		// compute length of LCS and all subproblems via dynamic programming
		for (int i = M - 1; i >= 0; i--) {
			for (int j = N - 1; j >= 0; j--) {
				if (x.charAt(i) == y.charAt(j))
					opt[i][j] = opt[i + 1][j + 1] + 1;
				else
					opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
			}
		}

		// recover LCS itself and print it to standard output
		int i = 0, j = 0;
		while (i < M && j < N) {
			if (x.charAt(i) == y.charAt(j)) {
				longestSequence = longestSequence + (x.charAt(i));
				i++;
				j++;
			} else if (opt[i + 1][j] >= opt[i][j + 1])
				i++;
			else
				j++;
		}
		System.out.println();
		return longestSequence;
	}

	/**
	 * @param a
	 * @param b
	 * @return the number of matching word pairs, including duplicates
	 */
	public static int getNumberOfCommonPairs(String a, String b) {
		Vector<String> aVect = splitIntoWords(a);
		Vector<String> bVect = splitIntoWords(b);
		return getNumberOfCommonPairs(aVect, bVect, false);
	}

	/**
	 * @param a
	 * @param b
	 * @param doCounts
	 *            whether to generate raw counts
	 * @return the number of matching word pairs, including duplicates
	 */
	private static int getNumberOfCommonPairs(Vector<String> a, Vector<String> b, boolean doCounts) {
		int n = 0;
		if (doCounts) {
			countsA.clear();
			countsB.clear();
			countsAB.clear();
			for (String sa : a) {
				MapUtil.increment(countsA, sa.toLowerCase());
			}
			for (String sb : b) {
				MapUtil.increment(countsB, sb.toLowerCase());
			}
		}
		for (String sa : a) {
			sa = sa.toLowerCase();
			for (String sb : b) {
				sb = sb.toLowerCase();
				if (sa.equals(sb)) {
					if (doCounts) {
						MapUtil.increment(countsAB, sa);
					}
					n++;
				}
			}
		}
		return n;
	}

	/**
	 * @param a
	 * @param b
	 * @return a kernel based on number of matching word pairs, (a,b)/sqrt((a,a)*(b,b))
	 */
	public static double wordKernel(String a, String b) {
		Vector<String> aVect = splitIntoWords(a);
		Vector<String> bVect = splitIntoWords(b);
		double ab = getNumberOfCommonPairs(aVect, bVect, true);
		double a2 = getNumberOfCommonPairs(aVect, aVect, false);
		double b2 = getNumberOfCommonPairs(bVect, bVect, false);
		double denom = Math.sqrt(a2 * b2);
		return ((denom == 0.0) ? 0.0 : (ab / denom));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#similarity(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double similarity(String a, String b) {
		if (kernelMeasure) {
			return wordKernel(a, b);
		} else {
			float[] pair = getProportionOfWordsOfAInBandBinA(a, b);
			return ((double) pair[0] + (double) pair[1]) / 2.0;
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
		return countsA;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#rawCountsB()
	 */
	@Override
	public HashMap<? extends Object, Integer> rawCountsB() {
		return countsB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.util.similarity.SimilarityMeasure#rawCountsAB()
	 */
	@Override
	public HashMap<? extends Object, Integer> rawCountsAB() {
		return countsAB;
	}

}
