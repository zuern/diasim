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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.trees.Tree;

/**
 * 
 * @author Arash
 */
public class FragmentIdentifier {

	// public Tree parse;
	public Vector<PositiveFragmentFilter> positiveFilters;
	public Vector<NegativeFragmentFilter> negativeFilters;
	private String filterFileName;

	public FragmentIdentifier() {
		this.filterFileName = System.getProperty("user.dir") + File.separator + "fragmentFilters" + File.separator
				+ "filters.txt";

		setupFilters();

	}

	public void setupFilters() {

		// can edit this bit to impose more constraints on the fragments identified
		positiveFilters = new Vector<PositiveFragmentFilter>();
		negativeFilters = new Vector<NegativeFragmentFilter>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filterFileName));
			String line;
			while ((line = reader.readLine()) != null) {
				// System.out.println("loading Filter:"+line);
				String values[] = line.split("\\|");
				if (values[0].equalsIgnoreCase("positive")) {
					System.out.println("Adding positive filter:" + values[1] + " " + values[2]);
					positiveFilters.add(new PositiveFragmentFilter(values[1], Integer.parseInt(values[2])));

				} else if (values[0].equalsIgnoreCase("negative")) {
					if (values.length == 3) {
						System.out.println("Adding negative filter:" + values[1] + " " + values[2]);
						negativeFilters.add(new NegativeFragmentFilter(values[1], values[2]));
					} else {
						System.out.println("Adding negative filter:" + values[1]);
						negativeFilters.add(new NegativeFragmentFilter(values[1]));

					}

				}
			}

		} catch (Exception e) {
			System.out.print("loading filters failed");
			e.printStackTrace();

		}

	}

	public static int phraseLength(String phrase) {
		String[] words = phrase.split(" ");
		return words.length;
	}

	private ArrayList<Fragment> applyPositiveFilters(ArrayList<Fragment> frags) {
		ArrayList<Fragment> result = new ArrayList<Fragment>();
		for (Fragment frag : frags) {
			for (PositiveFragmentFilter filter : positiveFilters) {
				if (frag.phraseType.equalsIgnoreCase(filter.phraseTypeFilter)
						&& phraseLength(frag.content) == filter.lengthFilter) {
					result.add(frag);
					break;
				}
			}
		}
		return result;

	}

	private ArrayList<Fragment> applyNegativeFilters(ArrayList<Fragment> frags) {
		ArrayList<Fragment> result = new ArrayList<Fragment>();

		for (Fragment frag : frags) {
			boolean found = false;

			for (NegativeFragmentFilter filter : this.negativeFilters) {
				Pattern p = Pattern.compile(filter.regex);
				Matcher m = p.matcher(frag.content);
				// m.reset();
				// System.out.println("applying negative filter:"+ filter.match+" to "+frag.content);
				if (filter.phraseType != null) {
					if (filter.phraseType.equalsIgnoreCase(frag.phraseType) && m.find()) {
						found = true;
						break;

					}
				} else {
					if (m.find()) {
						found = true;
						break;
					}
				}

			}
			if (!found)
				result.add(frag);
		}
		return result;
	}

	public ArrayList<Fragment> filter(ArrayList<Fragment> v) {
		return applyNegativeFilters(applyPositiveFilters(v));
	}

	public ArrayList<Fragment> getFilteredFragments(Tree t) {
		ArrayList<Fragment> allFrags = getFragments(t);
		return filter(allFrags);
	}

	public ArrayList<Fragment> getFragments(Tree t) {

		List<Tree> children = t.getChildrenAsList();
		ArrayList<Fragment> fragments = new ArrayList<Fragment>();
		String dominatedPhrase = getDominatedPhrase(t);
		Fragment frag = new Fragment(t.label().value(), dominatedPhrase);
		// System.out.println("Adding "+t.label()+"-->"+dominatedPhrase);
		if (dominatedPhrase.length() > 0) {

			if (t.children().length > 1) {

				fragments.add(frag);
			} else if (t.depth() == 1)
				fragments.add(frag);
		}
		for (Tree child : children) {
			// System.out.println("Merging "+)
			fragments.addAll(getFragments(child));

		}
		return fragments;

	}

	// private Vector<String> fragments;
	// not using this one at the moment
	public HashMap<String, ArrayList<String>> getPhraseTypeToFragmentsMap(Tree t) {

		List<Tree> children = t.getChildrenAsList();
		HashMap<String, ArrayList<String>> fragments = new HashMap<String, ArrayList<String>>();
		String dominatedPhrase = getDominatedPhrase(t);
		// System.out.println("Adding "+t.label()+"-->"+dominatedPhrase);
		if (dominatedPhrase.length() > 0) {
			ArrayList<String> v = new ArrayList<String>();
			v.add(dominatedPhrase);

			if (t.children().length > 1) {

				fragments.put(t.label().value(), v);
			} else if (t.depth() == 1)
				fragments.put(t.label().value(), v);
		}
		for (Tree child : children) {
			// System.out.println("Merging "+)
			fragments = merge(fragments, getPhraseTypeToFragmentsMap(child));

		}
		return fragments;

	}

	// not using this one either at the moment
	public HashMap<String, ArrayList<String>> merge(HashMap<String, ArrayList<String>> h1,
			HashMap<String, ArrayList<String>> h2) {
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		for (String cur : h1.keySet()) {
			if (result.containsKey(cur)) {
				result.get(cur).addAll(h1.get(cur));

			} else
				result.put(cur, h1.get(cur));
		}
		for (String cur1 : h2.keySet()) {
			if (result.containsKey(cur1)) {
				result.get(cur1).addAll(h2.get(cur1));

			} else
				result.put(cur1, h2.get(cur1));
		}
		return result;

	}

	private String getDominatedPhrase(Tree t) {
		String result = "";
		List<Tree> leaves = t.getLeaves();
		for (Tree cur : leaves) {
			result += cur.label().value() + " ";

		}
		return result;
	}

	//
	// public void readWriteFragsToFile(ParserWrapper pw, String inputDir, String outputFile) throws IOException {
	//
	// File input = new File(inputDir + File.separator + "turns.txt");
	// File output = new File(inputDir + File.separator + outputFile);
	// BufferedReader in = new BufferedReader(new FileReader(input));
	// BufferedWriter out = new BufferedWriter(new FileWriter(output));
	// String curLine;
	//
	// while ((curLine = in.readLine()) != null) {
	// String[] fields = curLine.split("\\|");
	// Vector v = pw.parseTextTimeOut(fields[6], 4000);
	//
	// Tree parse = (Tree) v.elementAt(1);
	// if (parse == null) {
	// String lineWritten = fields[6] + "|" + "not parsed";
	// out.write(lineWritten, 0, lineWritten.length());
	// out.newLine();
	// out.flush();
	//
	// System.out.println("parsing failed");
	// continue;
	// }
	//
	// ArrayList<Fragment> fragments = getFragments(parse);
	// ArrayList<Fragment> filtered = filter(fragments);
	// String lineOut = fields[6] + "|";
	// for (Fragment frag : filtered) {
	// lineOut += "[" + frag.phraseType + "-->" + frag.content + "], ";
	// System.out.println("Writing Frag:" + frag.content);
	//
	// }
	// out.write(lineOut, 0, lineOut.length());
	// out.newLine();
	// out.flush();
	//
	// }
	// in.close();
	// out.close();
	//
	// }

	public static void main(String args[]) {

		FragmentIdentifier fi = new FragmentIdentifier();

		/*
		 * readWriteFragsToFile; long time0=(new Date()).getTime(); Vector
		 * v=pw.parseTextTimeOut("It looks like a big fat lady",1500); long time1=(new Date()).getTime(); long
		 * diff=time1-time0; System.out.println("Parsing took "+diff+" milliseconds.");
		 * 
		 * Tree parse=(Tree)v.elementAt(1); if (parse==null) {System.out.println("parsing failed");System.exit(1);}
		 * ArrayList<Fragment> fragments=fi.getFragments(parse); parse.pennPrint(); for(Fragment frag:fragments) {
		 * System.out.println(frag.phraseType+"-->"+frag.content);
		 * 
		 * } ArrayList<Fragment> filtered=fi.filter(fragments); System.out.println("\nFiltered:"); for(Fragment
		 * frag:filtered) { System.out.println(frag.phraseType+"-->"+frag.content);
		 * 
		 * }
		 */

	}

}
