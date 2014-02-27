/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package qmul.util.treekernel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordTokenFactory;

public class LexicalSimilarityCalc {

	private static Morphology m = new Morphology();

	private static float lexSimilarity(String s1, String s2) {
		String[] split1 = s1.split("[.?!]");
		String[] split2 = s2.split("[.?!]");
		Set<String> stemsInFirst = new HashSet<String>();
		Set<String> stemsInSecond = new HashSet<String>();
		for (int i = 0; i < split1.length; i++) {
			PTBTokenizer<Word> tokenizer1 = new PTBTokenizer<Word>(new StringReader(split1[i]), new WordTokenFactory(),
					"tokenizeNLs=false");
			while (tokenizer1.hasNext()) {
				Word w = tokenizer1.next();
				String stem = m.stem(w).word();

				stemsInFirst.add(stem);
			}
		}

		for (int j = 0; j < split2.length; j++) {
			PTBTokenizer<Word> tokenizer2 = new PTBTokenizer<Word>(new StringReader(split2[j]), new WordTokenFactory(),
					"tokenizeNLs=false");
			while (tokenizer2.hasNext()) {
				Word w = tokenizer2.next();
				String stem = m.stem(w).word();

				stemsInSecond.add(stem);
			}
		}

		Iterator<String> i = stemsInSecond.iterator();
		float commonStems = 0;
		while (i.hasNext()) {
			String curStem = i.next();
			// System.out.println(curStem);
			if (stemsInFirst.contains(curStem))
				commonStems++;
		}
		int secondSize = stemsInSecond.size();
		if (secondSize > 0)
			return commonStems / (float) (secondSize);
		else
			return 0;
	}

	public static void computeWriteSimilaritiesToFile(String inputFileName, String outputFileName) throws IOException {

		FileInputStream fstream = new FileInputStream(inputFileName);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		File outputFile = new File(outputFileName);
		outputFile.createNewFile();
		File averageFile = new File(outputFileName + "ave");
		averageFile.createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		BufferedWriter aveWriter = new BufferedWriter(new FileWriter(averageFile));
		String conversationID = "";
		float totalSimilarity1 = 0;
		float totalSimilarity2 = 0;
		int totalTurns1 = 0;
		int totalTurns2 = 0;
		String line = reader.readLine();
		String[] values1 = line.split("\t");
		boolean flag = true;// totalsimmilarity1 is being incremented

		while ((line = reader.readLine()) != null) {
			String[] values2 = line.split("\t");

			if (!values1[0].equals(values2[0]))// new conversation
			{

				float aveSimilarity1 = (float) (totalSimilarity1 / (totalTurns1));
				float aveSimilarity2 = (float) (totalSimilarity2 / (totalTurns2));
				String aveLineWritten = values1[0] + "\t" + aveSimilarity1 + "\t" + totalTurns1 + "\t" + aveSimilarity2
						+ "\t" + totalTurns2;

				aveWriter.write(aveLineWritten, 0, aveLineWritten.length());
				aveWriter.newLine();
				aveWriter.flush();
				totalSimilarity1 = 0;
				totalSimilarity2 = 0;
				totalTurns1 = 0;
				totalTurns2 = 0;
				values1 = values2;

				flag = true;
				continue;
			}

			float similarity = lexSimilarity(values1[2], values2[2]);
			String lineWritten = values1[0] + "\t" + values1[1] + "\t" + values2[1] + "\t" + values1[2] + "\t"
					+ values2[2] + "\t" + similarity;
			if (flag) {
				totalSimilarity2 += similarity;
				totalTurns2++;
			} else {
				totalSimilarity1 += similarity;
				totalTurns1++;
			}
			writer.write(lineWritten, 0, lineWritten.length());
			writer.newLine();
			writer.flush();
			// //
			values1 = values2;
			flag = !flag;

		}
		float aveSimilarity1 = (float) (totalSimilarity1 / (totalTurns1));
		float aveSimilarity2 = (float) (totalSimilarity2 / (totalTurns2));
		String aveLineWritten = values1[0] + "\t" + aveSimilarity1 + "\t" + totalTurns1 + "\t" + aveSimilarity2 + "\t"
				+ totalTurns2;

		aveWriter.write(aveLineWritten, 0, aveLineWritten.length());
		aveWriter.newLine();
		aveWriter.flush();
		writer.flush();
		aveWriter.flush();
		writer.close();
		aveWriter.close();
	}

	public static void main(String args[]) {

		String s1 = "Shall I buy you a foot bath?";
		String s2 = "I bought one yesterday.";
		System.out.println(lexSimilarity(s1, s2));

		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
		// try
		// {
		// computeWriteSimilaritiesToFile(args[0], args[1]);
		// }
		// catch(IOException e)
		// {
		// e.printStackTrace();
		// }

	}
}
