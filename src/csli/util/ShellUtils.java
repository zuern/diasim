/*******************************************************************************
 * Copyright (c) 2004, 2005 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * @author mpurver
 */
public class ShellUtils {

    public static int execCommand(String cmd) {
        return execCommand(cmd, null, true, true);
    }

    public static int execCommand(String[] cmd) {
        return execCommand(cmd, null, true, true);
    }

    public static int execCommand(String cmd, File dir) {
        return execCommand(cmd, dir, true, true);
    }

    public static int execCommand(String[] cmd, File dir) {
        return execCommand(cmd, dir, true, true);
    }

    public static int execCommand(String cmd, boolean printStdout,
            boolean printStderr) {
        return execCommand(cmd, null, printStdout, printStderr);
    }

    public static int execCommand(String[] cmd, boolean printStdout,
            boolean printStderr) {
        return execCommand(cmd, null, printStdout, printStderr);
    }

    public static int execCommand(String cmd, File dir, boolean printStdout,
            boolean printStderr) {
        try {
            System.out.println("Calling " + cmd);
            Process proc = Runtime.getRuntime().exec(cmd, null, dir);

            BufferedReader in = new BufferedReader(new InputStreamReader(proc
                    .getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(proc
                    .getErrorStream()));

            String line;
            while ((line = in.readLine()) != null) {
                if (printStdout)
                    System.out.println(line);
            }
            while ((line = err.readLine()) != null) {
                if (printStderr)
                    System.err.println(line);
            }

            proc.waitFor();
            return proc.exitValue();
        } catch (Throwable t) {
            t.printStackTrace();
            return -1;
        }
    }

    public static int execCommand(String[] cmd, File dir, boolean printStdout,
            boolean printStderr) {
        try {
            System.out.print("Calling");
            for (int i = 0; i < cmd.length; i++) {
                System.out.println(" " + cmd[i]);
            }
            System.out.println();
            Process proc = Runtime.getRuntime().exec(cmd, null, dir);

            BufferedReader in = new BufferedReader(new InputStreamReader(proc
                    .getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(proc
                    .getErrorStream()));

            String line;
            while ((line = in.readLine()) != null) {
                if (printStdout)
                    System.out.println(line);
            }
            while ((line = err.readLine()) != null) {
                if (printStderr)
                    System.err.println(line);
            }

            proc.waitFor();
            return proc.exitValue();
        } catch (Throwable t) {
            t.printStackTrace();
            return -1;
        }
    }

}
