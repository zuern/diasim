/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
/*
 * FilenameToolkit.java
 *
 * Created on 17 November 2007, 20:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package qmul.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 
 * @author user
 */
public class FilenameToolkit {

	static public boolean copy(File src, File dst) throws IOException {
		try {

			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			return true;
		} catch (Exception e) {
			System.err.println("Error copying file " + src.getName() + " to " + dst.getName());
			return false;
		}

	}

	static public String getNextPrefixInteger(String[] filenames) {
		// Find highest integer

		System.out.println("---------" + filenames.length);
		int highestNumberSoFar = 0;

		if (filenames != null) {
			for (int i = 0; i < filenames.length; i++) {
				String s = filenames[i];
				int snum = getIntPrefix(s);
				System.out.println("FOUND HIGH NUMBER " + snum);
				if (snum > highestNumberSoFar)
					highestNumberSoFar = snum;
			}
		}

		String prefixNumber = "" + (highestNumberSoFar + 1);
		for (int i = prefixNumber.length(); i < 4; i++) {
			prefixNumber = "0" + prefixNumber;
		}
		return prefixNumber;

	}

	static public int getIntPrefix(String s) {
		String prefixNumber = "0";
		boolean reachedEndOfInitialPrefix = false;
		int indexFromStart = 0;

		while (!reachedEndOfInitialPrefix && indexFromStart < s.length()) {
			char c = s.charAt(indexFromStart);
			if (Character.isDigit(c)) {
				prefixNumber = prefixNumber + c;
				System.out.println("-----PRF: " + c);
			} else {
				reachedEndOfInitialPrefix = true;
			}
			indexFromStart++;
		}
		try {
			System.out.println("PREFIX IS:" + prefixNumber);
			int intPrefix = Integer.valueOf(prefixNumber);
			System.out.println("RETURNING " + intPrefix);
			return intPrefix;
		} catch (Exception e) {
			return 0;
		}
	}

	public static String returnSystemDependentFilename(String s) {
		String s2 = "" + s.replaceAll("[\\]", File.separator);
		s2.replaceAll("[//]", File.separator);
		return s2;
	}

	/**
	 * @param files
	 *            an array of {@link File}s which will get sorted by getName() value
	 */
	public static void sortByFileName(File[] files) {
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			};
		});
	}

	/**
	 * @param files
	 *            an array of {@link File}s which will get sorted by getName() value ignoring case
	 */
	public static void sortByFileNameIgnoreCase(File[] files) {
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File o1, File o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			};
		});
	}

	/**
	 * @param files
	 *            an array of {@link File}s which will get sorted by lastModified() value
	 */
	public static void sortByModificationTime(File[] files) {
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File o1, File o2) {
				return Long.valueOf(o1.lastModified()).compareTo(o2.lastModified());
			};
		});
	}

}
