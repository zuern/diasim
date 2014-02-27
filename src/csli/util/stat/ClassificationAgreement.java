/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.stat;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;

import csli.util.file.VectorDataFileReader;

/**
 * Calculates agreement and evaluation statistics for classification annotations. Currently, methods support only
 * two-way comparison, though they support any number of possible classes.
 * 
 * In cases where a comparison is being made between a reference and hypothesized classification, the first dimension of
 * the matrix should refer to the reference classification, and the second dimension the hypothesized classification.
 */

public class ClassificationAgreement {

    /**
     * Checks to see if the contingency table is properly formed, i.e. is square and at least 2x2.
     */
    public static boolean checkTable(int[][] cTable) {
        if (cTable == null || cTable.length < 2) {
            return false;
        }
        for (int i = 0; i < cTable.length; i++) {
            if (cTable[i].length != cTable.length) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the total number of agreeing classifications.
     * 
     * (n x n) matrix allowed.
     */
    public static int correct(int[][] cTable) {
        int correct = 0;
        for (int i = 0; i < cTable.length; i++) {
            correct += cTable[i][i];
        }
        return correct;
    }

    /**
     * Evaluates the error between two n-class classifications.
     * 
     * (n x n) matrix allowed.
     */
    public static double error(int[][] cTable) {
        int sum = sum(cTable);
        int correct = correct(cTable);
        return (sum - correct) / (double) sum;
    }

    /**
     * Evaluates the positive fscore of a binary classification ref/hyp contingency table.
     * 
     * (2 x 2) matrix only
     */
    public static double fscore(int[][] cTable) {
        double precision = precision(cTable, 1);
        double recall = recall(cTable, 1);
        return ((2 * precision * recall) / (double) (precision + recall));
    }

    /**
     * Evaluates the fscore for a particular class in a ref/hyp contingency table.
     * 
     * (n x n) matrix accepted
     */
    public static double fscore(int[][] cTable, int cls) {
        double precision = precision(cTable, cls);
        double recall = recall(cTable, cls);
        return ((2 * precision * recall) / (double) (precision + recall));
    }

    /**
     * Sum the number of classifications the refernce annotation made for a given class.
     * 
     * (n x n) matrix allowed
     */
    public static int hypClassSum(int[][] cTable, int c) {
        int sum = 0;
        for (int j = 0; j < cTable[c].length; j++) {
            sum += cTable[j][c];
        }
        return sum;
    }

    /**
     * Evaluates n-class kappa between two annotations, using the cohen chance calculation. See
     * http://www-class.unl.edu/psycrs/handcomp/hckappa.PDF
     * 
     * (n x n) matrix allowed.
     */
    public static double kappaCohen(int[][] cTable) {
        double sum = sum(cTable);
        double nchance = 0;
        for (int i = 0; i < cTable.length; i++) {
            nchance += (hypClassSum(cTable, i) * refClassSum(cTable, i)) / (double) sum;
        }
        return (correct(cTable) - nchance) / (double) (sum - nchance);
    }

    /**
     * Evaluates n-class kappa between two annotations, using the siegel chance calculation.
     * 
     * (n x n) marix allowed.
     */
    public static double kappaSiegel(int[][] cTable) {
        int numAgreements = correct(cTable);
        int totalChoices = sum(cTable) * 2;
        int length = cTable.length;
        // double[] classProbs = new double[length];
        double chanceProb = 0.0;
        for (int i = 0; i < length; i++) {
            chanceProb += Math.pow((refClassSum(cTable, i) + hypClassSum(cTable, i)) / (double) totalChoices, 2);
        }
        return ((numAgreements / (double) (totalChoices / 2.0)) - chanceProb) / (double) (1 - chanceProb);
    }

    /**
     * For testing purposes.
     */
    public static void main(String[] args) {
        int[] ref = { 3, 1, 4, 3, 1, 2, 3, 4, 1, 2, 4, 2, 3, 1, 4, 2, 3, 1, 2, 4, 3, 2, 1, 3 };
        int[] hyp = { 4, 1, 2, 2, 2, 3, 3, 3, 2, 4, 1, 1, 4, 2, 3, 4, 1, 2, 2, 3, 4, 2, 1, 4 };
        int[][] table = makeTable(ref, hyp, 5);
        System.out.println("correct:" + correct(table));
        System.out.println("error:" + error(table));
        System.out.println("hypClassSum:" + hypClassSum(table, 2));
        System.out.println("refClassSum:" + refClassSum(table, 2));
        System.out.println("kappaCohen:" + kappaCohen(table));
        System.out.println("kappaSiegel:" + kappaSiegel(table));
        System.out.println("fscore:" + fscore(table, 2));
        System.out.println("precision:" + precision(table, 2));
        System.out.println("recall:" + recall(table, 2));
        System.out.println("fscore:" + fscore(table, 3));
        System.out.println("precision:" + precision(table, 3));
        System.out.println("recall:" + recall(table, 3));
        int[] ref2 = { 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1 };
        int[] hyp2 = { 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1 };
        table = makeTable(ref2, hyp2, 2);
        System.out.println("---------");
        System.out.println("fscore:" + fscore(table));
        System.out.println("precision:" + precision(table));
        System.out.println("recall:" + recall(table));
        System.out.println("kappaCohen:" + kappaCohen(table));
        System.out.println("kappaSiegel:" + kappaSiegel(table));

        // file testing
        try {
            FileDialog fd = new FileDialog(new Frame());
            fd.show();
            File f1 = new File(fd.getDirectory(), fd.getFile());
            fd = new FileDialog(new Frame());
            fd.show();
            File f2 = new File(fd.getDirectory(), fd.getFile());
            compareFiles(f1, f2);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.exit(0);
    }

    /**
     * Make a contingency table from two classification arrays, reference and hypothesized, with the specified number of
     * possible target classes.
     * 
     * Returns a (numClasses x numClasses) matrix.
     */
    public static int[][] makeTable(int[] ref, int[] hyp, int numClasses) {
        if (ref.length != hyp.length) {
            throw new RuntimeException("classification arrays are of different lengths");
        }
        int[][] result = new int[numClasses][numClasses];
        for (int i = 0; i < ref.length; i++) {
            if (ref[i] >= numClasses || hyp[i] >= numClasses) {
                throw new RuntimeException("classifications are not between 0 and numClasses-1");
            }
            result[ref[i]][hyp[i]]++;
        }
        return result;
    }

    /**
     * Evaluates positive precision in a binary-classification ref/hyp contingency table.
     * 
     * (2 x 2) matrix allowed
     */
    public static double precision(int[][] cTable) {
        return precision(cTable, 1);
    }

    /**
     * Evaluates the precision of a specified class in a multi-class classification.
     * 
     * (n x n) matrix allowed
     */
    public static double precision(int[][] cTable, int val) {
        return (cTable[val][val] / (double) hypClassSum(cTable, val));
    }

    /**
     * Evaluates positive recall in a binary classification ref/hyp contingency table.
     * 
     * (2 x 2) matrix allowed
     */
    public static double recall(int[][] cTable) {
        return recall(cTable, 1);
    }

    /**
     * Evaluates the recall of particular class in a multi-class classification.
     * 
     * (n x n) matrix allowed
     */
    public static double recall(int[][] cTable, int val) {
        return (cTable[val][val] / (double) refClassSum(cTable, val));
    }

    /**
     * Sum the number of classifications the reference annotation made for the given class.
     * 
     * (n x n) matrix allowed
     */
    public static int refClassSum(int[][] cTable, int c) {
        int sum = 0;
        for (int j = 0; j < cTable[c].length; j++) {
            sum += cTable[c][j];
        }
        return sum;
    }

    /**
     * Calculate the total number of data-points in the table.
     * 
     * (n x n) matrix allowed
     */
    public static int sum(int[][] cTable) {
        int sum = 0;
        for (int i = 0; i < cTable.length; i++) {
            for (int j = 0; j < cTable[i].length; j++) {
                sum += cTable[i][j];
            }
        }
        return sum;
    }

    /**
     * print a string representation of the table on stdout
     * 
     * @param cTable
     *            a contingency table as generated by makeTable
     */
    public static void printTable(int[][] cTable) {
        System.out.print(toString(cTable));
    }

    /**
     * @param cTable
     *            a contingency table as generated by makeTable
     * @return a String representation of the table, suitable for printing
     */
    public static String toString(int[][] cTable) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cTable.length; i++) {
            for (int j = 0; j < cTable[i].length; j++) {
                sb.append(cTable[i][j] + (j < cTable[i].length - 1 ? " " : ""));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Read in two files to compare, with possibly multiple columns of data which will be compared separately
     */
    public static void compareFiles(File f1, File f2) {

        VectorDataFileReader data = new VectorDataFileReader(f1, ' ');
        int[][] d1 = new int[data.getNumCols(0)][data.getNumRows()];
        for (int i = 0; i < data.getNumRows(); i++) {
            for (int j = 0; j < data.getNumCols(i); j++) {
                d1[j][i] = Integer.parseInt(data.get(i, j));
            }
        }
        data = new VectorDataFileReader(f2, ' ');
        int[][] d2 = new int[data.getNumCols(0)][data.getNumRows()];
        for (int i = 0; i < data.getNumRows(); i++) {
            for (int j = 0; j < data.getNumCols(i); j++) {
                d2[j][i] = Integer.parseInt(data.get(i, j));
            }
        }

        for (int i = 0; i < d1.length; i++) {
            int[][] t = makeTable(d1[i], d2[i], 2);
            System.out.println("testing column " + i + ", " + t.length + "x" + t[0].length);
            printTable(t);
            System.out.println("ref = " + refClassSum(t, 0) + ", " + refClassSum(t, 1));
            System.out.println("hyp = " + hypClassSum(t, 0) + ", " + hypClassSum(t, 1));
            System.out.println("correct = " + correct(t));
            System.out.println("error = " + error(t));
            System.out.println("kappaCohen = " + kappaCohen(t));
            System.out.println("kappaSiegel = " + kappaSiegel(t));
            System.out.println("precision = " + precision(t));
            System.out.println("recall = " + recall(t));
            System.out.println("fscore = " + fscore(t));
        }
        System.out.println();

    }

}