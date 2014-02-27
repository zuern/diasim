/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package qmul.util.parse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Tree;

/**
 * A wrapper for the Clark & Curran CCG parser http://svn.ask.it.usyd.edu.au/trac/candc/
 * 
 * @author mpurver
 */
public class ClarkCurranParser implements TreeParser {

	private static final String DEFAULT_EXECUTABLE = "/import/imc-corpora/tools/parsers/candc-1.00/bin/candc";
	private static final String DEFAULT_MODELS = "/import/imc-corpora/tools/parsers/candc-1.00/models";
	private static final int MAX_PARSES_BEFORE_REINIT = Integer.MAX_VALUE;
	private static final int MAX_WORDS = 250; // should be possible to set --candc-maxwords, but it breaks!

	private String executable;
	private String models;
	private Process p;
	private BufferedWriter out;
	private BufferedReader in;
	private BufferedReader err;
	private String buffer;
	private PennTreebankTokenizer tok;
	private int numParsed = 0;

	public ClarkCurranParser() {
		this(DEFAULT_EXECUTABLE, DEFAULT_MODELS);
	}

	public ClarkCurranParser(String executable, String models) {
		this.executable = executable;
		this.models = models;
		init();
	}

	public void init() {
		try {
			if (p != null) {
				System.err.println("destroy!");
				in.close();
				out.close();
				err.close();
				p.destroy();
			}
			System.out.print("Loading parser " + executable + " ... ");
			if (System.getProperty("os.name").matches("(?i).*windows.*")) {
				// C&C assume / directory separator, so we'll have to run via cygwin under Windows
				p = Runtime.getRuntime().exec(
						new String[] {
								"c:\\cygwin\\bin\\bash.exe",
								"-c",
								executable + " --models " + models
										+ " --candc-printer prolog --candc-maxwords_policy warn" });
			} else {
				p = Runtime.getRuntime().exec(
						new String[] { executable, "--models", models, "--candc-printer", "prolog",
								"--candc-maxwords_policy", "warn" });
			}
			out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			buffer = "";
			String errBuf;
			if (!(errBuf = getStderr()).isEmpty()) {
				System.err.println(errBuf);
			}
			// read prolog header comment
			readUntilEmptyLine();
			// read prolog header declarations
			readUntilEmptyLine();
			System.out.println("done.");
			System.out.println("readinit " + buffer);
			if (!(errBuf = getStderr()).isEmpty()) {
				System.err.println(errBuf);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		numParsed = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.TreeParser#getBestParse()
	 */
	@Override
	public Tree getBestParse() {
		List<Tree> trees = CreateTreeFromClarkCurranCCGProlog.makeTrees(buffer);
		if (trees.size() > 0) {
			return trees.get(0);
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.parser.Parser#parse(java.util.List)
	 */
	@Override
	public boolean parse(List<? extends HasWord> sentence) {
		if (numParsed++ > MAX_PARSES_BEFORE_REINIT) {
			init();
		}
		String input = "";
		for (HasWord w : sentence) {
			input += " " + w;
		}
		input = input.trim();
		// we get no output from C&C for empty lines, so return now to avoid hanging waiting for buffer
		if (input.isEmpty()) {
			return false;
		}
		try {
			// write the sentence to the parser process
			out.write(input);
			out.newLine();
			out.flush();
			System.out.println("wrote " + input);
			// read back its reaction
			buffer = "";
			if (waitForStdout()) {
				while (in.ready()) {
					readUntilEmptyLine();
				}
			}
			if (!buffer.trim().startsWith("ccg(")) {
				// there wasn't a ccg/2 tree, so nothing to produce a Tree from
				return false;
			}
			// ditch stderr so it doesn't clog up the works
			getStderr();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return (buffer != null);
	}

	/**
	 * Read parser's STDOUT until an empty line or end of stream, and add to buffer
	 * 
	 * @throws IOException
	 */
	private void readUntilEmptyLine() throws IOException {
		// read lines until empty line or null (end of stream)
		String line = in.readLine();
		while ((line != null) && !line.isEmpty()) {
			buffer += line + "\n";
			line = in.readLine();
		}
	}

	/**
	 * @return true when STDOUT is ready with output, or false if STDERR tells us the parser is ignoring an over-length
	 *         sentence so STDOUT will never be ready (which is why we need the maxwords_policy option set)
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private boolean waitForStdout() throws IOException, InterruptedException {
		// wait until reader is ready
		while (!in.ready()) {
			String errBuf;
			if ((errBuf = getStderr()).startsWith("ignor")) {
				System.err.println(errBuf);
				return false;
			}
			Thread.sleep(10);
		}
		return true;
	}

	/**
	 * @return the contents of STDERR if any, empty string otherwise
	 * @throws IOException
	 */
	private String getStderr() throws IOException {
		String errBuffer = "";
		while (err.ready()) {
			errBuffer += err.readLine();
		}
		return errBuffer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.parser.Parser#parse(java.util.List, java.lang.String)
	 */
	@Override
	public boolean parse(List<? extends HasWord> sentence, String goal) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param sentence
	 *            raw sentence string - will be tokenised according to Penn treebank standards
	 * @return true iff the sentence is recognized
	 */
	public boolean parse(String sentence) {
		if (tok == null) {
			tok = new PennTreebankTokenizer(true);
		}
		return parse(tok.getWordsFromString(sentence));
	}

	public static void main(String[] args) {
		ClarkCurranParser ccp = new ClarkCurranParser();
		ArrayList<HasWord> l = new ArrayList<HasWord>();
		l.add(new Word("john"));
		l.add(new Word("likes"));
		l.add(new Word("mary"));
		boolean r;
		System.out.println((r = ccp.parse(l)) ? (r + " " + ccp.getBestParse().pennString()) : "failed!");
		l.add(new Word("because"));
		l.add(new Word("she"));
		l.add(new Word("is"));
		l.add(new Word("nice"));
		System.out.println((r = ccp.parse(l)) ? (r + " " + ccp.getBestParse().pennString()) : "failed!");

		String s = "john likes mary, his neighbour, because (I think) she is nice!";
		System.out.println(s);
		System.out.println((r = ccp.parse(s)) ? (r + " " + ccp.getBestParse().pennString()) : "failed!");

		s = "Introduce Brenda who's going to speak to us on Make do and Mend and she's asked me to say that she'd be very pleased if people break in or erm sort of form some sort of dialogue with her as she goes along.";
		System.out.println(s);
		System.out.println((r = ccp.parse(s)) ? (r + " " + ccp.getBestParse().pennString()) : "failed!");

		s = "erm and as you'll be able to see from my introduction make do and mend wasn't something that suddenly happened in nineteen thirty nine there were sections of society in which make do and mend was a permanent and not not particularly erm welcome fact of life.";
		System.out.println(s);
		System.out.println((r = ccp.parse(s)) ? (r + " " + ccp.getBestParse().pennString()) : "failed!");

		s = "[pause] And an industrious and thrifty working working class supporting and supported by an extended family network.";
		System.out.println(s);
		System.out.println((r = ccp.parse(s)) ? (r + " " + ccp.getBestParse().pennString()) : "failed!");

		s = "when we erm, er when we were doing those projects we erm, we had a comments book and more, more people were in favour of them and saw them as an improvement to the town and so it needs something that's really interesting actually, it's er, erm the work's department have said, as a result of those graffiti projects they can shift two officer's from the graffiti team to the highway's team, so it's actually cut down on the work of actually clearing up unwanted graffiti, so it's had a positive effect, so we, we've got these two people work with the other people and we've got Dorothy [name] who's working with, with black people and ethnic minorities in the town and erm for people who were here when this presentation was last given, er Robin [name] who used to be in the local The Policy Team of the Local Government Unit is now actually a Community Development Officer, one of the decision's, Robin use to do video work for the Authority erm, and we decided we asset whether the the need for that kind of, k ind of work to continue, and we thought on balance not erm and he now is running the music rehearsal space over at Latton Bush, that again is a project for young people, to enable, it's a place where band's can practice and that's the problem in Harlow erm and er that's really exciting project because it's bringing in a lot of income for the Council as as well as providing the service that people want and, and it, I mean it is important at this time that we are doing [unclear] limited projects where we are bringing in income, cos at, you know we estimate that erm we can get that erm rehearsal space properly resource, that project could be self financing, so your providing a service but your also getting paid, your getting paid for as well, so erm that is who we are now and were er, where were located, we have an open door policy as people will know.";
		System.out.println(s);
		System.out.println((r = ccp.parse(s)) ? (r + " " + ccp.getBestParse().pennString()) : "failed!");

	}

}
