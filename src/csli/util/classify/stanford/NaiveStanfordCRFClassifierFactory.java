package csli.util.classify.stanford;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.objectbank.ObjectBank;

public class NaiveStanfordCRFClassifierFactory implements ClassifierFactory {

	private class DummyObjectBank<E> extends ObjectBank<List<E>> {

		private class DummyOBIterator implements Iterator<List<E>> {

			private int done = 0;

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Iterator#hasNext()
			 */
			// @Override
			public boolean hasNext() {
				return (done < 1);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Iterator#next()
			 */
			// @Override
			public List<E> next() {
				done++;
				return list;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Iterator#remove()
			 */
			// @Override
			public void remove() {
				list = null;
				done++;
			}

		}

		private List<E> list = null;

		private DummyObjectBank() {
			super(null, null);
		}

		private DummyObjectBank(List<E> list) {
			this();
			this.list = list;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see edu.stanford.nlp.objectbank.ObjectBank#add(java.lang.Object)
		 */
		@Override
		public boolean add(List<E> list) {
			this.list = list;
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see edu.stanford.nlp.objectbank.ObjectBank#clear()
		 */
		@Override
		public void clear() {
			list = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see edu.stanford.nlp.objectbank.ObjectBank#iterator()
		 */
		@Override
		public Iterator<List<E>> iterator() {
			return new DummyOBIterator();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see edu.stanford.nlp.objectbank.ObjectBank#size()
		 */
		@Override
		public int size() {
			return 1;
		}

	}

	private Properties properties;

	public NaiveStanfordCRFClassifierFactory() {
		super();
		properties = new Properties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.ClassifierFactory#trainClassifier(java.util.List)
	 */
	// @Override
	public <D extends Datum> Classifier trainClassifier(List<D> examples) {
		List<CoreLabel> fl = getCoreLabelList(examples);
		ObjectBank<List<CoreLabel>> objBank = new DummyObjectBank<CoreLabel>(fl);
		CRFClassifier crf = new CRFClassifier(properties);
		crf.train(objBank);
		return new NaiveStanfordCRFClassifier(crf);
	}

	/**
	 * @param examples
	 * @return a list of {@link CoreLabel}s corresponding to this array of {@link Datum}s
	 */
	protected static List<CoreLabel> getCoreLabelList(Datum[] examples) {
		ArrayList<CoreLabel> fls = new ArrayList<CoreLabel>(examples.length);
		for (Datum datum : examples) {
			fls.add(getCoreLabel(datum));
		}
		return fls;
	}

	/**
	 * @param <D>
	 *            extends {@link Datum}
	 * @param examples
	 * @return a list of {@link CoreLabel}s corresponding to this list of {@link Datum}s
	 */
	protected static <D extends Datum> List<CoreLabel> getCoreLabelList(List<D> examples) {
		ArrayList<CoreLabel> fls = new ArrayList<CoreLabel>(examples.size());
		for (Datum datum : examples) {
			fls.add(getCoreLabel(datum));
		}
		return fls;
	}

	/**
	 * @param datum
	 * @return a {@link CoreLabel} corresponding to this {@link Datum}
	 */
	protected static CoreLabel getCoreLabel(Datum datum) {
		Object[] features = datum.asFeatures().toArray(new Object[datum.asFeatures().size()]);
		String[] featureNames = new String[features.length];
		String[] featureValues = new String[features.length];
		for (int i = 0; i < featureNames.length; i++) {
			featureNames[i] = "" + i;
			featureValues[i] = features[i].toString();
		}
		CoreLabel fl = new CoreLabel(featureNames, featureValues);
		if (datum.label() != null) {
			fl.setCategory(datum.label().toString());
		}
		return fl;
	}

}
