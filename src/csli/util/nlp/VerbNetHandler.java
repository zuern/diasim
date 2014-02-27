/*******************************************************************************
 * Copyright (c) 2004, 2005 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp;

import java.io.File;

import org.apache.xerces.parsers.DOMParser;

import verbnet.API.VNclass;
import verbnet.API.VNframes;
import verbnet.API.VNmembers;
import verbnet.API.VerbNet;

public class VerbNetHandler {

    public static void main(String[] argv) {

        // Initialize the static VerbNet object with the path to the verbnet
        // files and with the parser it should use.

        VerbNet.setPath(new File("c:\\verbnet\\v1.5"));
        VerbNet.setParser(new DOMParser());

        VNclass[] vnclasses = VerbNet.getAllVNclasses();

        for (int i = 0; i < vnclasses.length; i++) {
            VNclass vnclass = vnclasses[i];
            VNmembers members = vnclass.getMembers();
            System.out.println(vnclass.getID() + " has " + members.size()
                    + " members: ");
            for (int j = 0; j < members.size(); j++) {
                System.out.println("  " + members.get(j).toString());
            }
            VNframes frames = vnclass.getFrames();
            System.out.println(" and " + frames.size() + " frames: ");
            for (int j = 0; j < frames.size(); j++) {
                System.out.println("  " + frames.get(j).toString());
            }
        }
    }
}
