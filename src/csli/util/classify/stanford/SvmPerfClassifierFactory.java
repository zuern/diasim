/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.classify.stanford;

import java.io.InvalidObjectException;

/**
 * A factory for producing (training) {@link SvmLightClassifier}s which use SVMperf rather than SVMlight (allowing
 * direct f-score/precision etc optimization in training)
 */
public class SvmPerfClassifierFactory extends SvmLightClassifierFactory {

	private int lossFunction = -1;

	/**
	 * A factory for producing (training) {@link SvmLightClassifier}s which use SVMperf rather than SVMlight (allowing
	 * direct f-score/precision etc optimization in training)
	 * 
	 * @param fileStem
	 *            the stem for the input/output data & model files
	 */
	public SvmPerfClassifierFactory(String fileStem) {
		super("util.learner.svmperf", fileStem);
	}

	/**
	 * @return the loss function: 0=zero/one, 1=F1, 2=error, 3=prec/rec breakeven, 4=precision, 5=recall, 10=ROCarea
	 */
	public int getLossFunction() {
		return lossFunction;
	}

	/**
	 * @param lossFunction
	 *            the loss function: 0=zero/one, 1=F1, 2=error, 3=prec/rec breakeven, 4=precision, 5=recall, 10=ROCarea
	 */
	public void setLossFunction(int lossFunction) {
		this.lossFunction = lossFunction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.classify.stanford.SvmLightClassifierFactory#getCLOptions(int, int)
	 */
	@Override
	protected String getCLOptions(int nNeg, int nPos) throws InvalidObjectException {
		if (isRegressionModel()) {
			throw new InvalidObjectException("SVMperf cannot act as a regression model");
		}
		String cl = super.getCLOptions(nNeg, nPos);
		if (lossFunction > 0) {
			cl += " -l " + getLossFunction();
		}
		return cl;
	}

}
