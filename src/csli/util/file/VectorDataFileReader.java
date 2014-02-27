/*******************************************************************************
 * Copyright (c) 2004, 2005 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import csli.util.FileUtils;

/**
 * Reads data from a character-delimited 2-dimensional vector data file. The
 * number of columns for each row is not required to be the same throughout.
 * Data are returned as Strings, and indexing starts at 0.
 */
public class VectorDataFileReader {

    /**
     * For testing purposes.
     */
    public static void main(String[] args) {
        VectorDataFileReader data = new VectorDataFileReader(new File(
                "c:\\test"), ' ');
        for (int i = 0; i < data.getNumRows(); i++) {
            for (int j = 0; j < data.getNumCols(i); j++) {
                System.out.print(data.get(i, j));
            }
            System.out.print("\n");
        }
    }

    private ArrayList data = new ArrayList();

    /**
     * Construct a reader for a specific file with the specified column
     * delimiter (row delimiters must be newlines).
     */
    public VectorDataFileReader(File file, char delimiter) {
        ArrayList lines = new ArrayList();
        try {
            lines = (ArrayList) FileUtils.getFileLines(file, lines);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (Iterator i = lines.iterator(); i.hasNext();) {
            ArrayList datai = new ArrayList();
            data.add(datai);
            String line = (String) i.next();
            int j2 = 0;
            String val = null;
            int j = line.indexOf(delimiter, 0);
            if (j == -1) {
                if (line.length() != 0) {
                    datai.add(line);
                }
            } else {
                datai.add(line.substring(0, j));
            }
            while (j != -1) {
                j2 = line.indexOf(delimiter, j + 1);
                if (j2 == -1) {
                    val = line.substring(j + 1);
                } else {
                    val = line.substring(j + 1, j2);
                }
                j = j2;
                datai.add(val);
            }
        }
    }

    /**
     * Get the String value in the specified slot. Users must parse into the
     * appropriate type of data.
     */
    public String get(int row, int col) {
        return (String) ((ArrayList) data.get(row)).get(col);
    }

    /**
     * Get the number of columns for the given row.
     */
    public int getNumCols(int row) {
        return ((ArrayList) data.get(row)).size();
    }

    /**
     * Get the number of rows.
     */
    public int getNumRows() {
        return data.size();
    }

}