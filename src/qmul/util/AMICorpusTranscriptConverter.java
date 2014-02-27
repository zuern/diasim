/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package qmul.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AMICorpusTranscriptConverter {
	
	
	File root;
	public AMICorpusTranscriptConverter(String inputDir)
	{
		root=new File(inputDir);
	}
	
	public void convertAll() throws IOException
	{
		File[] files=root.listFiles();
		for(File dir:files)
		{
			if (dir.isDirectory())
			{
				File f=new File(dir.getAbsolutePath()+File.separator+"Elan_"+dir.getName()+".txt");
				System.out.println("converting "+f.getName());
				convert(f);
			}
		}
	}
	
	public void convert(File elantrans) throws IOException
	{
		BufferedReader reader=new BufferedReader(new FileReader(elantrans));
		
		String line=reader.readLine();
		List<Token> allTokens=new ArrayList<Token>();
		
		while(line!=null)
		{
			
			String[] values=line.trim().split("\\s", -1);
		//	System.out.println(Arrays.asList(values));
			if (values[2].endsWith("/"))
			{
				line=reader.readLine();
				continue;
			}
			String speaker=values[0];
			Float curStart=Float.parseFloat(values[1]);
			Float curEnd=Float.parseFloat(values[2]);
			if (curStart.equals(curEnd) && !allTokens.isEmpty())
			{
				allTokens.get(allTokens.size()-1).content+=values[3];
				line=reader.readLine();
				continue;
			}
			Token t=new Token(curStart, curEnd, speaker, values[3]);
			allTokens.add(t);
			line=reader.readLine();
			
		}
		reader.close();
		System.out.println("loaded tokens");
		
		Collections.sort(allTokens);
		String prevS=allTokens.get(0).speaker;
		List<Token> turnTokens=new ArrayList<Token>();
		BufferedWriter writer=new BufferedWriter(new FileWriter(elantrans.getParent()+File.separator+elantrans.getName().substring(4, elantrans.getName().length()-4)+"-human-readable.txt"));
		for(Token t: allTokens)
		{
			String curSpeaker=t.speaker;
			if (!curSpeaker.equals(prevS))
			{
				Turn turn=new Turn(turnTokens);
				//System.out.println("writing turn:"+turn);
				writer.write(turn.toString());
				writer.newLine();
				turnTokens.clear();
			}
			turnTokens.add(t);
			prevS=curSpeaker;
			
		}
		writer.close();
	}
	
	
	public static void main(String[] a)
	{
		AMICorpusTranscriptConverter converter=new AMICorpusTranscriptConverter("/home/arash/Documents/Corpora_analyses/AMI/annotations/ami_public_manual_1.6/words/sample_cogsci_paper");
		try{
			converter.convertAll();
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	

}

class Turn
{
	String content="";
	float start;
	float end;
	String speaker;
	
	Turn(List<Token> tokens)
	{
		this.start=tokens.get(0).start;
		this.end=tokens.get(tokens.size()-1).end;
		this.speaker=tokens.get(0).speaker;
		for(Token t:tokens)
		{
			content+=t.content+" ";
		}
		
	}
	
	public String toString()
	{
		Timestamp start=new Timestamp((int)this.start*1000);
		Timestamp end=new Timestamp((int)this.end*1000);
		String result=speaker+"		"+start.getMinutes()+":"+start.getSeconds();
		result+="-"+end.getMinutes()+":"+end.getSeconds();
		result+="		"+content;
		return result;
	}
	
}
class Token implements Comparable<Token>
{
	String content;
	float start;
	float end;
	String speaker;
	Token(float start, float end, String speaker, String content)
	{
		this.start=start;
		this.end=end;
		this.speaker=speaker;
		this.content=content;
	}
	@Override
	public int compareTo(Token arg) {
		Token other=(Token)arg;
		if (other.start>this.start)
			return -1;
		if (other.start<this.start)
			return +1;
		
		return 0;
		
	}
}
