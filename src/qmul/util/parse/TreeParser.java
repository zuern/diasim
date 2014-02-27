/*******************************************************************************
 * Copyright (c) 2009, 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package qmul.util.parse;

import edu.stanford.nlp.parser.Parser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

/**
 * Just an extension to allow a {@link Parser} to return you the best {@link Tree} (as the {@link LexicalizedParser}
 * implementation already does)
 * 
 * @author mpurver
 */
public interface TreeParser extends Parser {

	public Tree getBestParse();

}
