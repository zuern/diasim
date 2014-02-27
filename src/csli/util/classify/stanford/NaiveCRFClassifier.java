package csli.util.classify.stanford;

import java.util.List;

import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;

/**
 * A class providing an implementation of the IITB CRF package with a naive model (1 state per label, all transitions
 * allowed, no nesting)
 * 
 * @author mpurver
 */
public class NaiveCRFClassifier extends ExternalClassifier {

	private CRF crf;

	private List<Object> labels;

	public NaiveCRFClassifier() {
		super(null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#classOf(edu.stanford.nlp.dbm.Datum[])
	 */
	@Override
	public Object[] classOf(Datum[] examples) {
		DataSequence dataSeq = makeDataSequence(examples);
		crf.apply(dataSeq);
		Object[] cls = new Object[dataSeq.length()];
		for (int i = 0; i < dataSeq.length(); i++) {
			cls[i] = labels.get(dataSeq.y(i));
		}
		return cls;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#scoresOf(edu.stanford.nlp.dbm.Datum[])
	 */
	@Override
	public ClassicCounter<?>[] scoresOf(Datum[] examples) {
		DataSequence dataSeq = makeDataSequence(examples);
		crf.apply(dataSeq);
		// crf.applyAndScore(dataSeq); // TODO ??
		ClassicCounter<?>[] counters = new ClassicCounter<?>[dataSeq.length()];
		for (int i = 0; i < dataSeq.length(); i++) {
			ClassicCounter<Object> counter = new ClassicCounter<Object>();
			counter.setCount(labels.get(dataSeq.y(i)), dataSeq.y(i));
			counters[i] = counter;
		}
		return counters;
	}

	private DataSequence makeDataSequence(Datum[] data) {
		return null;
	}

}
