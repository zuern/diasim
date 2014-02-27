/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.util.List;

import edu.stanford.nlp.ling.Datum;

/** @author Dan Klein */

public interface ClassifierFactory {
	<D extends Datum> Classifier trainClassifier(List<D> examples);
}
