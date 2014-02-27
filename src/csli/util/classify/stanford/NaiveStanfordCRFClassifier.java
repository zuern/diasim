package csli.util.classify.stanford;

import java.util.List;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;

/**
 * A class providing an implementation of the Stanford CRF package with a naive model (1 state per label, all
 * transitions allowed, no nesting). This is a subclass of {@link ExternalClassifier} just so as to make use of results
 * cacheing.
 * 
 * @author mpurver
 */
public class NaiveStanfordCRFClassifier extends ExternalClassifier {

	public static final String POS_LABEL = SvmLightClassifierFactory.POS_LABEL;

	public static final String NEG_LABEL = SvmLightClassifierFactory.NEG_LABEL;

	private CRFClassifier crf;

	public NaiveStanfordCRFClassifier(CRFClassifier crf) {
		super(null, null);
		this.crf = crf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.classify.stanford.ExternalClassifier#classOf(edu.stanford.nlp.dbm.Datum)
	 */
	@Override
	public Object classOf(Datum example) {
		// single datum can't be classified directly (we need a sequence), so rely on cacheing
		if (!getClassResults().containsKey(example)) {
			System.err.println("ERROR: no cached result for single datum " + example);
			return null;
		}
		return getClassResults().get(example);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#classOf(edu.stanford.nlp.dbm.Datum[])
	 */
	public Object[] classOf(Datum[] examples) {
		// this array method does the actual sequence classification work
		List<CoreLabel> features = NaiveStanfordCRFClassifierFactory.getCoreLabelList(examples);
		crf.classify(features);
		Object[] classes = new Object[examples.length];
		for (int i = 0; i < classes.length; i++) {
			classes[i] = features.get(i).category();
			setClassResult(examples[i], classes[i]);
		}
		return classes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csli.util.classify.stanford.ExternalClassifier#scoresOf(edu.stanford.nlp.dbm.Datum)
	 */
	@Override
	public ClassicCounter<?> scoresOf(Datum example) {
		// single datum can't be classified directly (we need a sequence), so rely on cacheing
		if (!getScoreResults().containsKey(example)) {
			System.err.println("ERROR: no cached result for single datum " + example);
			return null;
		}
		return getScoreResults().get(example);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#scoresOf(edu.stanford.nlp.dbm.Datum[])
	 */
	@Override
	public ClassicCounter<?>[] scoresOf(Datum[] examples) {
		// this array method does the actual sequence classification work
		List<CoreLabel> features = NaiveStanfordCRFClassifierFactory.getCoreLabelList(examples);
		crf.classify(features);
		ClassicCounter<?>[] scores = new ClassicCounter<?>[examples.length];
		for (int i = 0; i < scores.length; i++) {
			ClassicCounter<Object> score = new ClassicCounter<Object>();
			score.setCount(POS_LABEL, (features.get(i).category().equals(POS_LABEL) ? 1.0 : 0.0));
			score.setCount(NEG_LABEL, (features.get(i).category().equals(NEG_LABEL) ? 1.0 : 0.0));
			scores[i] = score;
			setClassResult(examples[i], scores[i]);
		}
		return scores;
	}

}
