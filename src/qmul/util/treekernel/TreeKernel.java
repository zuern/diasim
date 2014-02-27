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

package qmul.util.treekernel;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import qmul.util.MapUtil;
import qmul.util.parse.StanfordParser;
import edu.stanford.nlp.trees.Tree;

public class TreeKernel {

	/**
	 * A pair of nodes
	 * 
	 * @author mpurver
	 */
	private static class NodePair {

		private Production n1;

		private Production n2;

		private NodePair(Production n1, Production n2) {
			this.n1 = n1;
			this.n2 = n2;
		}

		private NodePair(Tree n1, Tree n2) {
			this.n1 = new Production(n1, true);
			this.n2 = new Production(n2, true);
		}

		public Production n1() {
			return n1;
		}

		public Production n2() {
			return n2;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "(" + n1.getBnf() + "," + n2.getBnf() + ")";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o) {
			if ((o == null) || !(o instanceof NodePair)) {
				return false;
			}
			NodePair other = (NodePair) o;
			return this.n1.equals(other.n1) && this.n2.equals(other.n2);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return (n1.hashCode() + " " + n2.hashCode()).hashCode();
		}

	}

	public static final int SUB_TREES = 0;
	public static final int SUBSET_TREES = 1;
	public static final int SYN_TREES = 2;

	private static Tree t1;
	private static Tree t2;

	// private LexicalizedParser parser=new
	// LexicalizedParser("D:\\UniWork\\StanfordParser\\stanford-parser-2007-08-19\\englishPCFG.ser.GZ");;
	private static List<Production> l1 = new LinkedList<Production>();
	private static List<Production> l2 = new LinkedList<Production>();
	private static HashMap<String, Integer> l1counts = new HashMap<String, Integer>();
	private static HashMap<String, Integer> l2counts = new HashMap<String, Integer>();
	private static HashMap<String, Integer> commonCounts = new HashMap<String, Integer>();
	private static List<NodePair> nodePairSet = new LinkedList<NodePair>();
	private static Map<NodePair, Integer> cache = new HashMap<NodePair, Integer>();
	private static int treeType;
	private static int sigma;

	/**
	 * Production will be from the second tree
	 */
	private static HashMap<Production, Long> maxCommonFragMatched = new HashMap<Production, Long>();

	/**
	 * Whether to include terminal nodes (lexical items)
	 */
	private static boolean includeWords = false;

	/**
	 * Whether to use standard kernel normalisation or Arash's number-of-frags version
	 */
	private static boolean kernelNormalisation = true;

	/**
	 * Whether to restrict calculation to certain {@link Production}s
	 */
	private static HashSet<String> allowedProductions = new HashSet<String>();

	/*
	 * public static float computeMaxTreeSimilarity(LinkedList<Tree> trees1, LinkedList<Tree> trees2) { Iterator<Tree>
	 * treeIter2=trees2.iterator(); float curMax=0; while(treeIter.hasNext()) { Tree curTree=treeIter.next(); float
	 * curValue=computeMaxSim(curTree, trees2); if (curValue>curMax) curMax=curValue; } return curMax; }
	 * 
	 * private static float computeMaxSim(Tree t, LinkedList<Tree> trees) {
	 */

	// treekernel.resetandcompute(tree,tree,false) (subtrees)
	// THIS IS THE MAIN METHOD FOR COMPUTING COMPARISON
	public static double resetAndCompute(Tree t1, Tree t2, int treeType) {
System.out.println("Calculating syn sim ");
		TreeKernel.treeType = treeType;
		TreeKernel.sigma = (treeType == SUB_TREES ? 0 : 1);

		// System.out.println("this is printed");
		// parser.setOptionFlags(new String[]{"-maxLength", "80", "-retainTmpSubcategories"});

		// TreeKernel.t1 = null;
		// TreeKernel.t2 = null;
		// Runtime.getRuntime().gc(); // hope now saving on garbage via clear()
		TreeKernel.t1 = t1;
		TreeKernel.t2 = t2;

		cache.clear();

		extractSortProductionLists();
		// System.out.println("L1 complete: " + l1);
		// System.out.println("L2 complete: " + l2);
		evaluatePairSet(l1, l2, true);
		// System.out.println("NP complete: " + nodePairSet);
		maxCommonFragMatched.clear();
		long commonFrag = TreeKernel.sumCommonFragments(l1, l2);
		// System.out.println("there were " + commonFrag + " common frags");

		if (kernelNormalisation) {
			TreeKernel.t1 = t1;
			TreeKernel.t2 = t1;
			evaluatePairSet(l1, l1, false);
			long k1 = sumCommonFragments(l1, l1);
			TreeKernel.t1 = t2;
			TreeKernel.t2 = t2;
			evaluatePairSet(l2, l2, false);
			long k2 = sumCommonFragments(l2, l2);
			// System.out.println("T1 has " + k1 + ", T2 has " + k2);
			double denom = Math.sqrt(k1 * k2);
			if (denom == 0.0) {
				if (commonFrag == 0) {
					return 0.0;
				} else {
					throw new RuntimeException("zero norm denominator with non-zero numerator: " + commonFrag);
				}
			}
			System.out.println(" done.");
			return ((double) commonFrag / denom);
		} else {
			long subtrees;
			if (treeType == SYN_TREES)
				subtrees = TreeKernel.countSynTrees(t2);
			else if (treeType == SUBSET_TREES)
				subtrees = TreeKernel.countSubSetTrees(t2);
			else if (treeType == SUB_TREES)
				subtrees = TreeKernel.countSubTrees(t2);
			else
				throw new RuntimeException("Unknown tree type: " + treeType);
			// System.out.println("Common Frag: " + commonFrag
			// +" \n And subtrees in second tree: " +subtrees);
			if (commonFrag < 0 || subtrees < 0)
				throw new RuntimeException("Common Frag:" + commonFrag + "  subtrees:" + subtrees);
			System.out.println("there were " + subtrees + " in total");
			if (subtrees == 0.0) {
				if (commonFrag == 0) {
					return 0.0;
				} else {
					throw new RuntimeException("zero subtree denominator with non-zero numerator: " + commonFrag);
				}
			}
			return ((double) commonFrag / (double) subtrees);
		}
	}

	/**
	 * Set up sorted lists of all productions in each tree, optionally including lexical rules
	 */
	private static void extractSortProductionLists() {
		l1.clear();
		l2.clear();
		l1counts.clear();
		l2counts.clear();
		for (Tree cur1 : t1) {
			if (cur1.depth() < (includeWords ? 1 : 2))
				continue;
			Production p = new Production(cur1, true);
			if (!allowedProductions.isEmpty() && !allowedProductions.contains(p.getBnf()))
				continue;
			l1.add(p);
			MapUtil.increment(l1counts, p.getBnf());
		}
		for (Tree cur2 : t2) {
			if (cur2.depth() < (includeWords ? 1 : 2))
				continue;
			Production p = new Production(cur2, true);
			if (!allowedProductions.isEmpty() && !allowedProductions.contains(p.getBnf()))
				continue;
			l2.add(p);
			MapUtil.increment(l2counts, p.getBnf());
		}
		Comparator<Production> pc = new ProductionComparator();
		Collections.sort(l1, pc);
		Collections.sort(l2, pc);
	}

	public static void bignumber() {
		long a = 101;
		for (int i = 0; i < 50; i++) {
			a *= a;
			System.out.println(a);
		}
	}

	public static long countSubSetTrees(Tree t) {
		if (t.depth() == (includeWords ? 0 : 1))
			return 0;
		else {
			int sum = 0;
			int product = 1;
			for (Tree cur : t.getChildrenAsList()) {
				sum += countSubSetTrees(cur);
				product *= (1 + countRootSST(cur));
			}
			return sum + product;
		}
	}

	private static long countRootSST(Tree t) {
		if (t.depth() == (includeWords ? 0 : 1))
			return 0;
		else {
			long product = 1;
			for (Tree cur : t.getChildrenAsList()) {
				product *= (1 + countRootSST(cur));
			}
			return product;
		}
	}

	public static int countSubTrees(Tree t) {
		return (t.size() - ((includeWords ? 1 : 2) * t.getLeaves().size()));
	}

	public static int countSynTrees(Tree t) {
		return (t.size() - ((includeWords ? 1 : 2) * t.getLeaves().size()));
	}

	@Deprecated
	public static float getNormalizedSimilarity() {
		return sumCommonFragments(l1, l2) / countSubSetTrees(t2);
	}

	/**
	 * Set up the list of matching production pairs
	 */
	private static void evaluatePairSet(List<Production> l1, List<Production> l2, boolean doCounts) {
		TreeKernel.nodePairSet.clear();
		if (doCounts) {
			commonCounts.clear();
		}

		int index1 = 0;
		int index2 = 0;
		if (l1.isEmpty() || l2.isEmpty())
			return;
		Production p1 = l1.get(index1);
		Production p2 = l2.get(index2);
		while (index1 < l1.size() && index2 < l2.size()) {
			if (p1.getBnf().compareTo(p2.getBnf()) > 0) {
				// System.out.println(p1+" is more than "+p2);
				index2++;
				if (index2 < l2.size())
					p2 = l2.get(index2);

			} else if (p2.getBnf().compareTo(p1.getBnf()) > 0) {
				// System.out.println(p2+" is more than "+p1);
				index1++;
				if (index1 < l1.size())
					p1 = l1.get(index1);

			} else {
				while (p1.getBnf().equals(p2.getBnf())) {
					int count = 0;
					while (p1.getBnf().equals(p2.getBnf())) {
						// System.out.println(p1+" equals "+p2);
						nodePairSet.add(new NodePair(p1, p2));
						index2++;
						count++;
						if (doCounts) {
							MapUtil.increment(commonCounts, p1.getBnf());
						}
						if (index2 < l2.size())
							p2 = l2.get(index2);
						else {
							break;
						}
					}
					index1++;
					index2 = index2 - count;
					p2 = l2.get(index2);
					if (index1 < l1.size())
						p1 = l1.get(index1);
					else {
						break;
					}

					// if (index2<l2.size()) p2=l2.get(index2);
					// else p2=null;

				}
				// System.out.println(p1+" "+p2);
			}
		}
		return;
	}

	public static long sumCommonFragments(List<Production> l1, List<Production> l2) {
		if (TreeKernel.nodePairSet == null)
			return 0;
		long count = 0;
		for (NodePair pair : nodePairSet) {

			// System.out.println("Checking pair " + pair);
			long c = countCommonFragmentsAt(pair, l1, l2);

			if (c > 0) {
				// System.out.println("Matched at " + pair + " = " + c);
				// TODO currently disabled for testing ...
				if (false && maxCommonFragMatched.containsKey(pair.n2())) {
					long curMaximum = maxCommonFragMatched.get(pair.n2()).longValue();
					if (curMaximum < c) {
						count -= curMaximum;
						count += c;
						maxCommonFragMatched.put(pair.n2(), new Long(c));
					}
				} else {
					count += c;
					maxCommonFragMatched.put(pair.n2(), new Long(c));
				}

			}
		}
		if (count < 0)
			throw new RuntimeException("negative result from sumCommonFragments(): " + count);
		return count;
	}

	private static int countCommonFragmentsAt(NodePair nodePair, List<Production> l1, List<Production> l2) {
		int result = 1;

		// if its name's not down, it's not coming in
		if (!nodePairSet.contains(nodePair)) {
			// System.out.println("Non-matching pair " + nodePair);
			return 0;
		}

		// if we've got here, we know the productions (i.e. all mother & child cats) match
		// System.out.println("Checking matching pair " + nodePair);

		// have we already seen this pair (often happens in recursion)? then use cache
		if (cache.containsKey(nodePair)) {
			// System.out.println("Using cache for seen pair " + nodePair);
			return cache.get(nodePair);
		}

		// if we're just counting syn rules, we don't care about children
		if (treeType == SYN_TREES) {
			return 1;
		}

		// can't check directly for depth, as depth may be different for different children ...
		// so check each child pair, and make sure we'll get a 1 if they only have "leaf" children
		// (although "leaf" depends on includeWords, so easier to say we want a 1 if NOT in the L1/L2 "to-do" lists)
		Tree[] children1 = nodePair.n1().getNode().children();
		Tree[] children2 = nodePair.n2().getNode().children();
		if (children1.length != children2.length) {
			throw new RuntimeException("non-matching lengths for matching pair " + nodePair);
		}
		for (int i = 0; i < children1.length; i++) {
			Production child1 = new Production(children1[i], true);
			Production child2 = new Production(children2[i], true);
			NodePair childNodePair = new NodePair(child1, child2);
			int childResult;
			if (!l1.contains(child1) && !l2.contains(child2)) {
				childResult = (treeType == SUBSET_TREES ? 0 : 1);
				// System.out.println("Return " + childResult + " for leaf pair " + childNodePair);
			} else {
				childResult = countCommonFragmentsAt(childNodePair, l1, l2);
				// System.out.println("Return " + childResult + " for non-leaf pair " + childNodePair);
			}
			result = result * (sigma + childResult);
			// System.out.println("recursion gives " + childResult + "(" + result + ") for " + childNodePair);
		}
		cache.put(nodePair, result);
		// System.out.println("Cached " + cache.size() + " for " + nodePair);
		return result;

	}

	/*
	 * public void printLists() { System.out.println(l1); System.out.println(l2); }
	 */
	public static List<NodePair> getNodePairSet() {
		return nodePairSet;
	}

	/**
	 * @return Whether to include terminal nodes (lexical items)
	 */
	public static boolean includeWords() {
		return includeWords;
	}

	/**
	 * @param includeWords
	 *            whether to include terminal nodes (lexical items)
	 */
	public static void setIncludeWords(boolean includeWords) {
		TreeKernel.includeWords = includeWords;
	}

	/**
	 * @return whether to use standard kernel normalisation, or T2's number of fragments
	 */
	public static boolean kernelNormalisation() {
		return kernelNormalisation;
	}

	/**
	 * @param kernelNormalisation
	 *            whether to use standard kernel normalisation, or T2's number of fragments
	 */
	public static void setKernelNormalisation(boolean kernelNormalisation) {
		TreeKernel.kernelNormalisation = kernelNormalisation;
	}

	/**
	 * @param bnf
	 *            add this BNF to the list of allowed {@link Production}s
	 */
	public static void addAllowedProduction(String bnf) {
		TreeKernel.allowedProductions.add(bnf);
	}

	/**
	 * @param bnf
	 *            remove this BNF from the list of allowed {@link Production}s
	 */
	public static void removeAllowedProduction(String bnf) {
		TreeKernel.allowedProductions.remove(bnf);
	}

	/**
	 * clear the list of allowed {@link Production}s
	 */
	public static void clearAllowedProductions() {
		TreeKernel.allowedProductions.clear();
	}

	/**
	 * @return the l1counts
	 */
	public static HashMap<String, Integer> getL1counts() {
		return l1counts;
	}

	/**
	 * @return the l2counts
	 */
	public static HashMap<String, Integer> getL2counts() {
		return l2counts;
	}

	/**
	 * @return the commonCounts
	 */
	public static HashMap<String, Integer> getCommonCounts() {
		return commonCounts;
	}

	/**
	 * Main method for testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		StanfordParser pw = new StanfordParser();

		Tree t1 = pw.parse("I loves Mary.");
		Tree t2 = pw.parse("John hates you");
		// Tree t1 = pw.parse("John.");
		// Tree t2 =
		// pw.parse("John is the nicest person I have ever met and I dare you to say anything else, as time will show");

		t1.pennPrint();
		t2.pennPrint();
		setIncludeWords(false);
		setKernelNormalisation(true);
		System.out.println(TreeKernel.resetAndCompute(t1, t2, 0));
		System.out.println(TreeKernel.resetAndCompute(t1, t2, 1));
		System.out.println(TreeKernel.resetAndCompute(t1, t2, 2));
		System.out.println(TreeKernel.resetAndCompute(t2, t1, 0));
		System.out.println(TreeKernel.resetAndCompute(t2, t1, 1));
		System.out.println(TreeKernel.resetAndCompute(t2, t1, 2));

	}

}
