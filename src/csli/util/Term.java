// Copyright c 2004 The Board of Trustees of the Leland Stanford Junior University. All rights reserved.

package csli.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple tree-based representation of Terms. Note that this does NO error-checking!! If incoming string is an
 * ill-formed term then result is unpredictable ...
 * 
 * Author: Lawrence Cavedon, 3/03
 * 
 * 7/21/03 : added the ability to parse quoted terms as units. Doesn't support nesting of quote (because there is only
 * one quote character) and has no error checking.
 */
public class Term implements Serializable {

	protected String functor;

	protected TermList children = null;

	// Type of the bracket surrounding the list
	protected int bracketType = ROUND;

	public static final int ROUND = 0; // ( )

	public static final int CURLY = 1; // { }

	public static final int SQUARE = 2; // [ ]

	private static final double DEFAULT_PROBABILITY = 1.0;

	private double probability = Term.DEFAULT_PROBABILITY;

	private boolean hasProbability = false;

	// public static final int MORELESS = 3; // < >

	// NOTE: we use special handing of square brackets for compatibility,
	// but parsing for this is done in the term class. Essentially, the square
	// bracketed
	// terms are considered to have a functor '[]' for compatibility

	public Term() {
		// TODO: should we allow empty ftor?
	}

	public Term(String string) {
		parse(string);
	}

	public Term(String functor, TermList children) {
		this.functor = functor;
		this.children = children;
	}

	// for milestone 3 of NIST project, all the subterms probability
	// scores will be 1.0...
	public void setProbability(double p) {
		hasProbability = true;
		probability = p;
	}

	public double getProbability() {
		if (hasProbability)
			return probability;
		else
			return DEFAULT_PROBABILITY;
	}

	public boolean termHasProbability() {
		return hasProbability;
	}

	// this might not be needed, as parser possibly returns probabilities for
	// the entire sub-tree...
	public double computeProbability() {
		if (children != null && children.size() > 0) {
			double prob = 1.0;
			for (int i = 0; i < children.size(); i++) {
				prob *= children.getTerm(i).computeProbability();
			}
			return prob;
		} else
			return Term.DEFAULT_PROBABILITY;

	}

	private void parse(String string) {
		// System.out.println("TERM PARSE: " + string);
		if ((string == null) || (string.equals("")))
			functor = "";

		// Quote case
		// TODO: support double quote (") as well?
		else if (string.charAt(0) == '\'') {
			// QUOTED STRING CASE
			// Tobias Scheideck 04/17/06: Incude quotes with functor to ensure
			// stringWithQuotes.equals((new Term(stringWithQuotes)).toString())
			functor = string;
		} else {
			// NORMAL CASE

			int idx = string.indexOf("(");
			bracketType = ROUND;

			int ci = string.indexOf("{");
			if (ci != -1 && (idx == -1 || ci < idx)) {
				idx = ci;
				bracketType = CURLY;
			}
			ci = string.indexOf("[");
			if (ci != -1 && (idx == -1 || ci < idx)) {
				idx = ci;
				bracketType = SQUARE;
			}

			if (idx < 0) {
				functor = string;
				// no children

			} else {
				// Special case: if we have a bracketed list with no ftor,
				// ftor is set to [] for compatibility reasons
				if (string.charAt(0) == '[')
					functor = "[]";
				// Normal case
				else
					functor = string.substring(0, idx);

				String rest = string.substring(idx + 1, string.length() - 1);
				children = new TermList(rest);
			}
		}
	}

	/**
	 * @return type of the bracket used (ROUND,SQUARE,CURLY)
	 */
	public int getBracketType() {
		return bracketType;
	}

	/**
	 * @return functor for this term
	 */
	public String getFunctor() {
		return functor;
	}

	/**
	 * DANGEROUS - violates the immutability of term TODO: refactor and have it return a new term with a changed
	 * functor.
	 * 
	 * @param functor
	 */
	public void setFunctor(String functor) {
		this.functor = functor;
	}

	/**
	 * DANGEROUS - violates the immutability of term
	 */
	public void setChild(int pos, Term newChild) {
		children.setTerm(pos, newChild);
	}

	/**
	 * same as numChildren()
	 * 
	 * @return the arity of the term
	 */
	public int getArity() {
		return numChildren();
	}

	public boolean isVar() {
		return (getArity() == 0 && functor.matches("^[A-Z_]"));
	}

	/**
	 * Get nth Term
	 */
	public Term getTerm(int n) {
		if (numChildren() == 0)
			return null;
		else
			return children.getTerm(n);
	}

	/**
	 * Get the first child term with specified functor, starting at s
	 * 
	 * @return the appropriate child, null if none
	 */
	public Term getTerm(String fn, int start) {
		if (start > numChildren())
			return null;
		for (int i = start; i < numChildren(); i++) {
			if (getTerm(i).getFunctor().equals(fn))
				return getTerm(i);
		}
		return null;
	}

	/**
	 * Get the first child term with specified functor.
	 */
	public Term getTerm(String fn) {
		return getTerm(fn, 0);
	}

	/**
	 * Get all child terms, starting at s
	 * 
	 * @return list of children, empty if none, null if s is inappropriate
	 */
	public List<Term> getTerms(int start) {
		if ((start < 0) || (start > numChildren()))
			return null;
		ArrayList<Term> kids = new ArrayList<Term>();
		for (int i = start; i < numChildren(); i++) {
			kids.add(getTerm(i));
		}
		return kids;
	}

	/**
	 * Get all child terms
	 * 
	 * @return list of children, empty if none
	 */
	public List<Term> getTerms() {
		return getTerms(0);
	}

	/**
	 * Get all child terms with specified functor, starting at s
	 * 
	 * @return list of appropriate children, empty if none, null if s is inappropriate
	 */
	public List<Term> getTerms(String fn, int start) {
		if ((start < 0) || (start > numChildren()))
			return null;
		ArrayList<Term> kids = new ArrayList<Term>();
		for (int i = start; i < numChildren(); i++) {
			if (getTerm(i).getFunctor().equals(fn))
				kids.add(getTerm(i));
		}
		return kids;
	}

	/**
	 * Get all child terms with specified functor.
	 */
	public List<Term> getTerms(String fn) {
		return getTerms(fn, 0);
	}

	/**
	 * @return Number of children
	 */
	public int numChildren() {
		if (children == null)
			return 0;
		else
			return children.size();
	}

	public void addChild(Term child) {
		if (children == null)
			children = new TermList(child);
		else
			children.addTerm(child);
	}

	public void addChild(int index, Term child) {
		if (children == null)
			children = new TermList(child);
		else
			children.addTerm(index, child);
	}

	/**
	 * Return String representation of Term.
	 */
	public String toString() {
		String chd = "";
		if (children != null && children.size() > 0) {
			chd = children.toString();

			// insert the appropriate brackets
			switch (bracketType) {
			case ROUND:
				chd = "(" + chd + ")";
				break;
			case CURLY:
				chd = "{" + chd + "}";
				break;
			case SQUARE:
				chd = "[" + chd + "]";
				break;
			default:
				throw new RuntimeException("Unsupported bracket type: " + bracketType);
			}

		}

		if (functor != null && functor.equals("[]")) {
			return chd;
		}

		// Normal case
		return functor + chd;
	}

	public String toPrettyString() {
		StringBuffer sb = new StringBuffer(200);
		toPrettyString(sb, 0);
		return sb.toString();
	}

	public void toPrettyString(StringBuffer sb, int level) {
		// Base case
		// if (! functor.equals("[]"))
		sb.append(getFunctor());
		if (numChildren() == 0)
			return;

		// recursion
		switch (bracketType) {
		case ROUND:
			sb.append("(");
			break;
		case CURLY:
			sb.append("{");
			break;
		case SQUARE:
			sb.append("[");
			break;
		default:
			throw new RuntimeException("Unsupported bracket type: " + bracketType);
		}

		// level++;

		if (numChildren() == 1)
			getTerm(0).toPrettyString(sb, level);
		else {
			boolean simpleChildren = !hasNonLeafChildren();
			level += 3;
			for (int i = 0; i < numChildren(); i++) {
				if (i != 0)
					sb.append(",");

				// Print the newline only
				// - if there are multiple children
				// - before the first child if the functor isn't empty
				if (!simpleChildren) {
					if (i == 0 && emptyFunctor()) {
						sb.append("  ");
					} else {
						sb.append("\n");
						indent(sb, level);
					}
				}
				getTerm(i).toPrettyString(sb, level);
			}
			level -= 3;
		}
		switch (bracketType) {
		case ROUND:
			sb.append(")");
			break;
		case CURLY:
			sb.append("}");
			break;
		case SQUARE:
			sb.append("]");
			break;
		default:
			throw new RuntimeException("Unsupported bracket type: " + bracketType);
		}
	}

	private void indent(StringBuffer sb, int level) {
		for (int i = 0; i < level; i++)
			sb.append(' ');
	}

	/**
	 * True if at least one child of this node is not a leaf (has children of its own) False if all children are leaves.
	 */
	public boolean hasNonLeafChildren() {
		for (int i = 0; i < numChildren(); i++)
			if (getTerm(i).numChildren() > 0)
				return true;
		return false;
	}

	/**
	 * @return true if this term is a leaf (has no children), false otherwise
	 */
	public boolean isLeaf() {
		return numChildren() == 0;
	}

	/**
	 * Returns a new functor identical to this one, but with all functors matching the equal to oldFn replaced with
	 * newFn.
	 */
	public Term replaceChildren(Term oldTerm, Term newTerm) {
		// Base case
		if (this.equals(oldTerm))
			return newTerm;

		// Recursion
		List kids = new ArrayList();
		for (int i = 0; i < this.numChildren(); i++) {
			kids.add(this.getTerm(i).replaceChildren(oldTerm, newTerm));
		}

		return Term.createFromList(this.getFunctor(), kids, getBracketType());
	}

	/**
	 * Returns a new term based on this one, with no empty leaves. For instance y(,(,),foo) -> y(foo)
	 */
	public Term removeEmptyLeaves() {
		// Base case: we're a leaf
		if (this.numChildren() == 0) {
			// if we're empty return null
			if (emptyFunctor())
				return null;
			// otherwise return us
			return this;
		}

		// Recursive case: we have children
		// Make a list of all our non-empty children
		List nonEmptyChildren = new ArrayList();
		for (int i = 0; i < this.numChildren(); i++) {
			Term c = this.getTerm(i).removeEmptyLeaves();
			if (c != null)
				nonEmptyChildren.add(c);
		}

		// Special case: no kids, no functor -- we're empty
		if (nonEmptyChildren.size() == 0) {
			if (emptyFunctor())
				return null;
		}

		// Regular case
		// Return a new term containing all our non-empty kids
		return createFromList(this.getFunctor(), nonEmptyChildren, getBracketType());
	}

	private boolean emptyFunctor() {
		String fn = this.getFunctor();
		return fn.equals("") || fn.equals("[]");
	}

	/**
	 * Flatten long linear terms to the top level. e.g. foo((((bar))),far) -> foo(bar,far)
	 */
	public Term flattenLinear() {
		// Base case -- no children
		if (numChildren() == 0)
			return this;

		// Recursive case -- single child
		if (numChildren() == 1 && emptyFunctor())
			return this.getTerm(0).flattenLinear();

		// Recursive case - flatten each child
		List kids = new ArrayList();
		for (int i = 0; i < this.numChildren(); i++) {
			Term c = this.getTerm(i).flattenLinear();
			kids.add(c);
		}

		return createFromList(this.getFunctor(), kids, getBracketType());
	}

	public static Term createFromList(String fn, List kids, int bracketType) {
		Term rv = new Term(fn);
		rv.bracketType = bracketType;
		for (int i = 0; i < kids.size(); i++)
			rv.addChild((Term) kids.get(i));

		return rv;
	}

	public boolean equals(Object o) {
		if (o instanceof Term) {
			Term t = (Term) o;

			// just use string equality...
			return t.toString().equals(toString());
		}

		return false;
	}

	public Object clone() {
		// BUG: has drawbacks with quoted strings - term not always reproduced exactly
		// may actually throw an error in some cases
		// Tobias Scheideck 04/17/06: Fixed by including the quotes in the functor
		return new Term(this.toString());
	}

}
