/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.regex.Pattern;

import csli.util.Config;
import csli.util.Pair;

public class DeStemmer implements Serializable {
	private static final long serialVersionUID = 103847679400526139L;

	private HashMap<String,Pair<String,String>> table=new HashMap<String,Pair<String,String>>();   
	
	private static HashMap<String,Integer> POSScoring=new HashMap<String,Integer>();
	{
		POSScoring.put("NN",1);
		POSScoring.put("VB",1);
		POSScoring.put("NP",1);
		POSScoring.put("JJ",1);
	}
	
	public String deStem(String stem)
	{
		Pair<String,String> res=table.get(stem);
		if (res==null) return stem;
		return res.first();
	}
	
	public DeStemmer(Stemmer stemmer) {
		File F=Config.main.getFileProperty("csli.util.nlp.lemma.DictionaryLemmatiser.ruleFile");
		try{
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(F)));
			String line;
			while ((line=br.readLine())!=null)
			{
				String[] mots=line.split("\\s+");
				for(int i=0;i<mots.length;i++)
				{
					String mot=mots[i];
					while (!Pattern.matches("[A-Z]+:\\\".*\\\"",mot))
					{
						i++;
						mot=mot+" "+mots[i];
					}
					String[] temp=mot.split(":");
					String POS=temp[0];
					String word=temp[1].substring(1,temp[1].length()-1);
					if (word.equals("-")) continue;
					if (POSScoring.get(POS)==null)
					{
						//System.err.println(POS);
						POSScoring.put(POS,POSScoring.size()+1);
					}
					String stem=stemmer.stem(word);
					Pair<String,String> pa=table.get(stem);
					if (pa==null || POSScoring.get(pa.second())>POSScoring.get(POS))
					{
						table.put(stem,new Pair<String,String>(word,POS));
					}
					else
					{
						if (POSScoring.get(pa.second())==POSScoring.get(POS) && word.length()<pa.first().length())
						{
							table.put(stem,new Pair<String,String>(word,POS));
						}
					}
				}
			}
			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
