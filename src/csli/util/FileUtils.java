/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author niekrasz, jefe
 * 
 * Utilities for file IO.
 * 
 */
public class FileUtils {

    public static interface LineReader {
        void readLine(String line);
    }

    public static interface ObjectReader<E> {
        E readLine(String line);
    }

    public static interface Writer<E> {
        String write(E o);
    }

    public static void copy(File src, File dst) throws IOException {
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
    }

    public static long countFileLines(String filename) throws IOException {
        long result = 0;
        FileInputStream instream = new FileInputStream(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                instream));
        while ((reader.readLine()) != null) {
            result++;
        }
        return result;
    }

    /**
     * Fill the Collection with the lines in filename. If no collection passed
     * in create a new arraylist and return it
     * 
     * @param filename
     * @param lines
     * @return lines
     * @throws IOException
     */
    public static Collection<String> getFileLines(String filename, Collection<String> lines)
            throws IOException {
        if (lines == null) {
            lines = new ArrayList<String>();
        }

        File file = new File(filename);

        return getFileLines(file, lines);
    }

    /**
     * Fill the Collection with the lines in file. If no collection passed
     * in create a new arraylist and return it
     * 
     * @param file
     * @param lines
     * @return lines
     * @throws IOException
     */
    public static Collection<String> getFileLines(File file, Collection<String> lines)
            throws IOException {
        FileInputStream instream = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                instream));
        String inLine;
        while ((inLine = reader.readLine()) != null) {
            lines.add(inLine);
        }
        reader.close();

        return lines;
    }

    /**
     * Read a file's contents into a string.
     * 
     * (copied from csli.agent.manager.DisplayFile)
     * 
     * @param filename
     *            The filename to read from.
     * @return The contents of <i>filename</i>.
     * 
     * @throws IOException
     *             Thrown when an I/O error occurs.
     */
    public static String readFile(String filename) throws IOException {
        File file = new File(filename);
        StringBuffer buf = new StringBuffer((int) file.length() + 2);
        FileReader in = new FileReader(file);

        int c;
        while ((c = in.read()) != -1) {
            buf.append((char) c);
        }
        in.close();

        return (buf.toString());
    }

    /**
     * Open a file and read one line at a time, processing it using something
     * derived from a LineReader.
     * 
     * @param filename
     * @param reader
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void readLinesFromFile(String filename, LineReader reader)
            throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = in.readLine()) != null)
            reader.readLine(line);
        in.close();
    }

    public static <E> List<E> readObjectsFromFile(String filename,
            ObjectReader<E> reader) throws IOException {
        List<E> ret = new ArrayList<E>();
        BufferedReader in = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = in.readLine()) != null)
            ret.add(reader.readLine(line));
        in.close();
        return ret;
    }

    /**
     * Writes a string to a file.
     * 
     * @param filename
     * @param string
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void writeStringToFile(String filename, String string)
            throws FileNotFoundException, IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        out.write(string);
        out.close();
    }

    /**
     * Iterates over objects, writing each object successively to a file.
     * 
     * @param <E>
     * @param filename
     * @param objects
     * @param writer:
     *            a Writer<E> object. if not supplied, uses the object's
     *            natural toString() method.
     * @param lineBreak:
     *            true if a newline should be inserted after each object.
     *            defaults to true.
     * @throws IOException
     */
    public static <E> void writeObjectsToFile(String filename,
            Iterable<E> objects, Writer<E> writer, boolean lineBreak)
            throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        for (E o : objects)
            out.write(writer.write(o) + (lineBreak ? "\r\n" : ""));
        out.close();
    }

    public static <E> void writeObjectsToFile(String filename,
            Iterable<E> objects, boolean lineBreak) throws IOException {
        writeObjectsToFile(filename, objects, new Writer<E>() {
            public String write(E o) {
                return o.toString();
            }
        }, lineBreak);
    }

    public static <E> void writeObjectsToFile(String filename,
            Iterable<E> objects) throws IOException {
        writeObjectsToFile(filename, objects, true);
    }

    public static <E> void writeObjectsToFile(String filename,
            Iterable<E> objects, Writer<E> writer) throws IOException {
        writeObjectsToFile(filename, objects, writer, true);
    }
}