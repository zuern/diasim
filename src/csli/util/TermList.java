// Copyright c 2004 The Board of Trustees of the Leland Stanford Junior University. All rights reserved.

package csli.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * List of Terms. Note that this does NO error-checking!! If incoming string is an ill-formed term then result is
 * unpredictable ...
 * 
 * Author: Lawrence Cavedon, 3/03
 * 
 * 7/21/03 : added the ability to parse quoted terms as units. Doesn't support nesting of quote (because there is only
 * one quote character) and has no error checking.
 */
public class TermList implements Serializable {

	protected List terms = new ArrayList();

	/**
	 * Default ctor. Constructs an empty term list
	 */
	public TermList() { /* empty */
	}

	/**
	 * Constructs TermList from comma-separated list of terms
	 */
	public TermList(String string) {
		parse(string);
	}

	/**
	 * Construct TermList from list of Term objects
	 */
	public TermList(List termList) {
		if (termList != null) {
			// making a deep copy
			terms.clear();
			terms.addAll(termList);
		}
	}

	/**
	 * @deprecated use TermList( A.list(term) ) instead
	 */
	public TermList(Term term) {
		this(Arrays.asList(term));
	}

	/**
	 * @deprecated use TermList( A.list(term1, term2) ) instead
	 */
	public TermList(Term term1, Term term2) {
		this(Arrays.asList(term1, term2));
	}

	/**
	 * Get the terms as a list
	 */
	List getTerms() {
		return terms;
	}

	/**
	 * @return the number of children
	 */
	public int size() {
		if (terms == null)
			return 0;
		else
			return terms.size();
	}

	/**
	 * Return nth Term---indexing starts at 0.
	 */
	public Term getTerm(int n) {
		if ((terms == null) || (n > terms.size() - 1))
			return null;
		else {
			try {
				return (Term) terms.get(n);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Insert a new term at the end
	 */
	public void addTerm(Term term) {
		addTerm(size(), term);
	}

	/**
	 * Insert a new term at the specified position
	 */
	public void addTerm(int index, Term term) {
		terms.add(index, term);
	}

	/**
	 * Replaces the term at the given positon with the new one
	 * 
	 * @param index
	 * @param term
	 */
	public void setTerm(int index, Term term) {
		terms.set(index, term);
	}

	/**
	 * Constructs TermList from comma-separated list of terms
	 */
	private void parse(String string) {
		if (string == null)
			return;

		// remove leading spaces, which cause problems with
		// comma positions such in "x(a(b) ,c)"
		string = string.trim();

		// base case
		if (string.equals(""))
			return;

		// System.out.println("TERMLIST STRING: " + string);
		// there may have been a leading ',' left by previous recursion

		if (string.charAt(0) == ',')
			string = string.substring(1).trim();

		// HACK: danilo: if there's nothing after a comma, ignore
		if (string.length() == 0)
			return;

		// grab the first term ...
		String firstTerm = "";
		String rest = "";

		// It's a paren by default
		int parenidx = string.indexOf("(");
		int bracketType = Term.ROUND;

		// Override with { if it preceeds it
		int ci = string.indexOf("{");
		if (ci != -1 && (parenidx == -1 || ci < parenidx)) {
			parenidx = ci;
			bracketType = Term.CURLY;
		}

		// Override with [ if it preceeds it
		ci = string.indexOf("[");
		if (ci != -1 && (parenidx == -1 || ci < parenidx)) {
			parenidx = ci;
			bracketType = Term.SQUARE;
		}

		int commaidx = string.indexOf(",");
		int quoteidx = string.indexOf("'");
		// ignore it, if there's no second apostrophe
		if ((quoteidx > -1) && (string.indexOf("'", quoteidx + 1) == -1)) {
			quoteidx = -1;
		}
		// ignore it, if it's not the first char
		if (quoteidx > 0) {
			quoteidx = -1;
		}

		// first check we have no paren, no comma
		if ((parenidx < 0) && (commaidx < 0)) {
			firstTerm = string;
			rest = "";
		}
		// next, check if comma is first punctuation
		else if ((commaidx >= 0) && (parenidx < 0 || commaidx < parenidx) && (quoteidx < 0 || commaidx < quoteidx)) {
			// process up to comma ...
			firstTerm = string.substring(0, commaidx);
			rest = string.substring(commaidx + 1);
		} else {
			// next, check if paren is first punctuation ...
			if ((parenidx >= 0) && (quoteidx < 0 || parenidx < quoteidx)) {
				// process parens ...
				switch (bracketType) {
				case Term.ROUND:
					firstTerm = tillMatching(string, '(', ')');
					break;
				case Term.CURLY:
					firstTerm = tillMatching(string, '{', '}');
					break;
				case Term.SQUARE:
					firstTerm = tillMatching(string, '[', ']');
					break;
				default:
					throw new RuntimeException("Unsupported bracket type: " + bracketType);
				}
				// chop off the brackets
				firstTerm = firstTerm.substring(0, firstTerm.length());

			} else { // process quoted term ...
				firstTerm = quotedTerm(string);
			}
			rest = string.substring(firstTerm.length());
		}

		/*
		 * else if ( (parenidx < 0 && quoteidx < 0) || ((commaidx >= 0) && ((commaidx < parenidx && parenidx >= 0) &&
		 * (commaidx < quoteidx && quoteidx >= 0)))) { firstTerm = string.substring(0, commaidx); rest =
		 * string.substring(commaidx + 1); } else { if ((parenidx < quoteidx || quoteidx < 0) && parenidx >= 0)
		 * firstTerm = tillMatching(string, '(', ')'); else firstTerm = quotedTerm(string); rest =
		 * string.substring(firstTerm.length()); }
		 */

		// Add in the detected first term
		terms.add(new Term(firstTerm));

		// Recursively parse the rest of the list
		parse(rest);
	}

	/*
	 * Have a string with parens in it ... first term is complex ... Find the first term by matching parens
	 */
	protected String tillMatching(String string, char openChar, char closeChar) {
		// find the matching paren to the first one
		// System.out.println("IN STRING: " + string);
		int idx = string.indexOf(openChar) + 1;
		int count = 1;
		while (count > 0) {
			if (idx >= string.length()) {
				throw new RuntimeException("Unmatched bracket in: " + string);
			}

			if (string.charAt(idx) == closeChar)
				count--;
			else if (string.charAt(idx) == openChar)
				count++;
			idx++;
		}
		// idx = index of matching paren
		String termString = string.substring(0, idx);
		// System.out.println("FIRST STRING: " + termString + "STRING: " + string);
		return termString;
	}

	protected String quotedTerm(String s) {
		int open = s.indexOf('\'');
		int close = s.indexOf('\'', open + 1);

		// HACK -- this should be done using a proper parser
		if (open == -1 || close == -1) {
			// match double paren
			int xopen = s.indexOf('"');
			int xclose = s.indexOf('"', open + 1);
			if (xopen == -1 || xclose == -1)
				return s;
			return s.substring(xopen, xclose + 1);

		}

		return s.substring(open, close + 1);
	}

	/**
	 * Convert this list of Terms to comma-separated string of term-strings
	 */
	public String toString() {
		if (size() == 0)
			return "";

		// create the list
		String result = ((Term) terms.get(0)).toString();
		// note the iteration from 1
		for (int i = 1; i < size(); i++) {
			result = result + "," + ((Term) terms.get(i)).toString();
		}

		return result;
	}

	/**
	 * Simple test program ...
	 */
	public static void main(String[] params) throws Exception {
		TermList terms = new TermList("[qual(speed), value(fast)]");
		System.out.println(terms.toString());
	}

}
