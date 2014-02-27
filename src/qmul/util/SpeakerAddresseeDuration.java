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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SpeakerAddresseeDuration {

	HashMap<String, Long[]> durationData;
	String headers = "Segment|Speak1|Speak2|Speak3|Speak4|Address1|Address2|Address3|address4|Nod1|Nod2|Nod3|Nod4";
	File file;

	public SpeakerAddresseeDuration(String fileName) {
		file = new File(fileName);

	}

	public void calculate(String outFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
		out.write(headers);
		out.newLine();
		String line = reader.readLine();
		String lastSegment = null;
		HashMap<String, Integer> nods = new HashMap<String, Integer>();
		HashMap<String, Integer> address = new HashMap<String, Integer>();
		HashMap<String, Integer> speak = new HashMap<String, Integer>();
		nods.put("A", 0);
		nods.put("B", 0);
		nods.put("C", 0);
		nods.put("D", 0);
		address.put("A", 0);
		address.put("B", 0);
		address.put("C", 0);
		address.put("D", 0);
		speak.put("A", 0);
		speak.put("B", 0);
		speak.put("C", 0);
		speak.put("D", 0);

		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\\s", -1);
			String segment = values[1];
			if (lastSegment == null)
				lastSegment = segment;

			if (!segment.equals(lastSegment)) {
				Map<String, Integer> sortedSpeak=sortByValue(speak);
				
				String outLine = "";
				outLine += lastSegment;
				for(Integer a:sortedSpeak.values())
				{
					outLine+="|"+a*0.1;
				}
				for(String per: sortedSpeak.keySet())
				{
					outLine+="|"+address.get(per)*0.1;
				}
				for(String per: sortedSpeak.keySet())
				{
					outLine+="|"+nods.get(per)*0.1;
				}
				/*
				outLine += "|" + address.get("A") * 0.1;
				outLine += "|" + address.get("B") * 0.1;
				outLine += "|" + address.get("C") * 0.1;
				outLine += "|" + address.get("D") * 0.1;
				outLine += "|" + nods.get("A") * 0.1;
				outLine += "|" + nods.get("B") * 0.1;
				outLine += "|" + nods.get("C") * 0.1;
				outLine += "|" + nods.get("D") * 0.1;*/
				out.write(outLine);
				out.newLine();
				for (String per : speak.keySet())
					speak.put(per, 0);
				for (String per : nods.keySet())
					nods.put(per, 0);
				for (String per : nods.keySet())
					address.put(per, 0);

				lastSegment = segment;
			}
			HashMap<String, String> lookAt = new HashMap<String, String>();
			HashMap<String, Boolean> speaking = new HashMap<String, Boolean>();
			HashMap<String, Boolean> nodding = new HashMap<String, Boolean>();
			speaking.put("A", !values[5].isEmpty());
			speaking.put("B", !values[6].isEmpty());
			speaking.put("C", !values[7].isEmpty());
			speaking.put("D", !values[8].isEmpty());
			nodding.put("A", values[9].equalsIgnoreCase("y"));
			nodding.put("B", values[10].equalsIgnoreCase("y"));
			nodding.put("C", values[11].equalsIgnoreCase("y"));
			nodding.put("D", values[12].equalsIgnoreCase("y"));
			lookAt.put("A", values[13]);
			lookAt.put("B", values[14]);
			lookAt.put("C", values[15]);
			lookAt.put("D", values[16]);
			for (String person : lookAt.keySet())
				if (!lookAt.get(person).isEmpty() && speaking.get(person))
					address.put(lookAt.get(person), address.get(lookAt.get(person)) + 1);

			for (String person : nodding.keySet())
				if (nodding.get(person))
					nods.put(person, nods.get(person) + 1);
			for (String person : speaking.keySet())
				if (speaking.get(person))
					speak.put(person, speak.get(person) + 1);

		}
		
		Map<String, Integer> sortedSpeak=sortByValue(speak);
		
		String outLine = "";
		outLine += lastSegment;
		for(Integer a:sortedSpeak.values())
		{
			outLine+="|"+a*0.1;
		}
		for(String per: sortedSpeak.keySet())
		{
			outLine+="|"+address.get(per);
		}
		for(String per: sortedSpeak.keySet())
		{
			outLine+="|"+nods.get(per);
		}
		/*
		outLine += "|" + address.get("A") * 0.1;
		outLine += "|" + address.get("B") * 0.1;
		outLine += "|" + address.get("C") * 0.1;
		outLine += "|" + address.get("D") * 0.1;
		outLine += "|" + nods.get("A") * 0.1;
		outLine += "|" + nods.get("B") * 0.1;
		outLine += "|" + nods.get("C") * 0.1;
		outLine += "|" + nods.get("D") * 0.1;*/
		out.write(outLine);
		out.newLine();
		for (String per : speak.keySet())
			speak.put(per, 0);
		for (String per : nods.keySet())
			nods.put(per, 0);
		for (String per : nods.keySet())
			address.put(per, 0);

		out.close();
		reader.close();

	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static void main(String a[]) {
		SpeakerAddresseeDuration sa = new SpeakerAddresseeDuration(
				"/home/arash/Documents/Corpora_analyses/AMI_Data_analysis/Nonverbal_annotations/Elan_AMI.txt");
		try {
			sa.calculate("/home/arash/Documents/Corpora_analyses/AMI_Data_analysis/Nonverbal_annotations/segments_addressees_nods.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
