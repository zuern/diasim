/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.dsp;

import java.util.ArrayList;
import java.util.List;

import csli.util.Term;

/**
 * A factory to produce {@link Smoother}s given a text spec (or list of specs)
 * 
 * @author mpurver
 */
public class SmoothingFactory {

	/**
	 * A {@link Smoother} which leaves data unchanged (in new arrays/collections).
	 * 
	 * @author mpurver
	 */
	public class NullSmoother implements Smoother {

		/*
		 * (non-Javadoc)
		 * 
		 * @see diet.utils.dsp.Smoother#smooth(double[])
		 */
		@Override
		public double[] smooth(double[] raw) {
			return raw.clone();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see diet.utils.dsp.Smoother#smooth(java.util.List)
		 */
		@Override
		public List<Double> smooth(List<Double> raw) {
			return new ArrayList<Double>(raw);
		}

	}

	/**
	 * An abstract {@link Smoother} which provides the {@link List} method.
	 * 
	 * @author mpurver
	 */
	public abstract class BasicSmoother implements Smoother {

		/*
		 * (non-Javadoc)
		 * 
		 * @see diet.utils.dsp.Smoother#smooth(java.util.List)
		 */
		@Override
		public List<Double> smooth(List<Double> raw) {
			double[] rawArray = new double[raw.size()];
			for (int i = 0; i < raw.size(); i++) {
				rawArray[i] = raw.get(i);
			}
			double[] cookedArray = smooth(rawArray);
			ArrayList<Double> cooked = new ArrayList<Double>();
			for (int i = 0; i < cookedArray.length; i++) {
				cooked.add(cookedArray[i]);
			}
			return cooked;
		}

	}

	/**
	 * A {@link Smoother} which smooths by convolution with a given kernel.
	 * 
	 * @author mpurver
	 */
	public class ConvolutionSmoother extends BasicSmoother {

		private double[] kernel;

		public ConvolutionSmoother(double[] kernel) {
			this.kernel = kernel.clone();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see csli.util.dsp.Smoother#smooth(double[])
		 */
		public double[] smooth(double[] raw) {
			return Smoothing.smooth(raw, kernel);
		}

	}

	/**
	 * A {@link Smoother} which smooths by convolution with a Gaussian kernel of a given length.
	 * 
	 * @author mpurver
	 */
	public class GaussianSmoother extends ConvolutionSmoother {

		public GaussianSmoother(int length) {
			super(Smoothing.gaussianKernel(length));
		}

	}

	/**
	 * A {@link Smoother} which smooths by conglomerating non-zero values within a window of a given length.
	 * 
	 * @author mpurver
	 */
	public class ConglomerationSmoother extends BasicSmoother {

		private int length;

		public ConglomerationSmoother(int length) {
			this.length = length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see csli.util.dsp.Smoother#smooth(double[])
		 */
		public double[] smooth(double[] raw) {
			return Smoothing.conglomerate(raw, length);
		}

	}

	/**
	 * A {@link Smoother} which smooths with a finite impulse response filter.
	 * 
	 * @author mpurver
	 */
	public class FIRSmoother extends BasicSmoother {

		private double[] filter;

		public FIRSmoother(double[] filter) {
			this.filter = filter.clone();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see csli.util.dsp.Smoother#smooth(double[])
		 */
		public double[] smooth(double[] raw) {
			return Smoothing.smoothFIR(raw, filter);
		}

	}

	/**
	 * A {@link Smoother} which smooths with a infinite impulse response filter.
	 * 
	 * @author mpurver
	 */
	public class IIRSmoother extends BasicSmoother {

		private double[] filter;

		public IIRSmoother(double[] filter) {
			this.filter = filter.clone();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see csli.util.dsp.Smoother#smooth(double[])
		 */
		public double[] smooth(double[] raw) {
			return Smoothing.smoothIIR(raw, filter);
		}

	}

	/**
	 * A {@link Smoother} which combines multiple smoothing operations.
	 * 
	 * @author mpurver
	 */
	public class ComplexSmoother extends BasicSmoother {

		private List<Smoother> smoothers = new ArrayList<Smoother>();

		public ComplexSmoother(List<String> specs) {
			for (String spec : specs) {
				smoothers.add(getSmoother(spec));
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see csli.util.dsp.Smoother#smooth(double[])
		 */
		public double[] smooth(double[] raw) {
			for (Smoother smoother : smoothers) {
				raw = smoother.smooth(raw);
			}
			return raw;
		}

	}

	private static SmoothingFactory factory = new SmoothingFactory();

	/**
	 * @param spec
	 *            a {@link String} specification of the {@link Smoother} desired e.g. gaussian(10) or fir(0.5,1.0,0.5)
	 * @return a {@link Smoother} with the desired properties
	 */
	public static Smoother getSmoother(String spec) {
		if (spec == null || spec.trim().equals("")) {
			throw new RuntimeException("ERROR: null or empty Smoother spec " + spec);
		}
		Term specTerm = new Term(spec.trim().toLowerCase());
		if (specTerm.getFunctor().startsWith("null")) {
			return factory.new NullSmoother();
		} else if (specTerm.getFunctor().startsWith("gauss")) {
			int length = getIntFromTerm(specTerm);
			return factory.new GaussianSmoother(length);
		} else if (specTerm.getFunctor().startsWith("conv")) {
			double[] kernel = getDoubleArrayFromTerm(specTerm);
			return factory.new ConvolutionSmoother(kernel);
		} else if (specTerm.getFunctor().startsWith("cong")) {
			int length = getIntFromTerm(specTerm);
			return factory.new ConglomerationSmoother(length);
		} else if (specTerm.getFunctor().startsWith("fir")) {
			double[] filter = getDoubleArrayFromTerm(specTerm);
			return factory.new FIRSmoother(filter);
		} else if (specTerm.getFunctor().startsWith("iir")) {
			double[] filter = getDoubleArrayFromTerm(specTerm);
			return factory.new IIRSmoother(filter);
		}
		throw new RuntimeException("ERROR: unrecognized Smoother spec " + spec);
	}

	/**
	 * @param specs
	 *            a {@link List} of {@link String} specifications of the {@link Smoother} desired, in the order which
	 *            the smoothing operations are to be applied e.g. [gaussian(10), fir(0.5,1.0,0.5)]
	 * @return a {@link Smoother} with the desired combined properties
	 */
	public static Smoother getSmoother(List<String> specs) {
		if ((specs == null) || (specs.size() == 0)) {
			throw new RuntimeException("ERROR: null or empty Smoother spec list " + specs);
		}
		if (specs.size() == 1) {
			return getSmoother(specs.get(0));
		}
		return factory.new ComplexSmoother(specs);
	}

	/**
	 * @param term
	 * @return true if the term has no sub-terms, or if the functor of the first sub-term equals "true" (ignoring case);
	 *         false otherwise
	 */
	protected static boolean getBooleanFromTerm(Term term) {
		if (term.getArity() == 0) {
			return true;
		} else {
			return getBooleanFromTerm(term, 0);
		}
	}

	/**
	 * @param term
	 * @param index
	 * @return true if the functor of the specifed sub-term term.getTerm(index) equals "true" (ignoring case); false
	 *         otherwise
	 */
	protected static boolean getBooleanFromTerm(Term term, int index) {
		return term.getTerm(index).getFunctor().equalsIgnoreCase("true");
	}

	/**
	 * @param term
	 * @return the result of parsing the functor of the first sub-term
	 */
	protected static int getIntFromTerm(Term term) {
		return getIntFromTerm(term, 0);
	}

	/**
	 * @param term
	 * @param index
	 * @return the result of parsing the functor of the specified sub-term
	 */
	protected static int getIntFromTerm(Term term, int index) {
		return Integer.parseInt(term.getTerm(index).getFunctor());
	}

	/**
	 * @param term
	 * @return the result of parsing the functor of the first sub-term
	 */
	protected static double getDoubleFromTerm(Term term) {
		return getDoubleFromTerm(term, 0);
	}

	/**
	 * @param term
	 * @param index
	 * @return the result of parsing the functor of the specified sub-term
	 */
	protected static double getDoubleFromTerm(Term term, int index) {
		return Double.parseDouble(term.getTerm(index).getFunctor());
	}

	/**
	 * @param term
	 * @return the result of parsing the functor of each sub-term in order
	 */
	protected static double[] getDoubleArrayFromTerm(Term term) {
		double[] array = new double[term.getArity()];
		for (int i = 0; i < array.length; i++) {
			array[i] = Double.parseDouble(term.getTerm(i).getFunctor());
		}
		return array;
	}

}
