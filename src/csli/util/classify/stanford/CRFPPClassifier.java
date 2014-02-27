package csli.util.classify.stanford;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import csli.util.ShellUtils;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counters;

/**
 * A class providing an implementation of the IITB CRF package with a naive model (1 state per label, all transitions
 * allowed, no nesting)
 * 
 * @author mpurver
 */
public class CRFPPClassifier extends ExternalClassifier {

	private int nFeatures;

	public CRFPPClassifier(String classifyCommand, String fileStem, HashMap<String, Integer> featureMap) {
		super(classifyCommand, fileStem, featureMap, null);
		// classification is fast, and it makes little sense to cache results for individual out-of-sequence data
		setResultCacheing(false);
		nFeatures = featureMap.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#classOf(edu.stanford.nlp.dbm.Datum[])
	 */
	@Override
	public Object[] classOf(Datum[] examples) {
		Object[] classes = new Object[examples.length];
		ClassicCounter<?>[] cs = scoresOf(examples);
		for (int i = 0; i < cs.length; i++) {
			classes[i] = Counters.argmax(cs[i]);
		}
		return classes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.Classifier#scoresOf(edu.stanford.nlp.dbm.Datum[])
	 */
	@Override
	public ClassicCounter<?>[] scoresOf(Datum[] examples) {

		File dataFile = new File(getFileStem() + CRFPPClassifierFactory.TEST_EXT);
		File modelFile = new File(getFileStem() + CRFPPClassifierFactory.MODEL_EXT);
		File resultFile = new File(getFileStem() + CRFPPClassifierFactory.RESULT_EXT);
		try {
			FileWriter out = new FileWriter(dataFile);
			for (int i = 0; i < examples.length; i++) {
				out.write(CRFPPClassifierFactory.toString(examples[i], getFeatureMap(), true));
			}
			out.close();
			// -v2 switch gives probabilities for all possible tags
			int retval = ShellUtils.execCommand(getClassifyCommand() + " -v 2 -m " + modelFile.getAbsolutePath()
					+ " -o " + resultFile.getAbsolutePath() + " " + dataFile.getAbsolutePath());
			if (retval != 0) {
				return null;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}

		char[] cbuf = new char[(int) resultFile.length()];
		try {
			FileReader in = new FileReader(resultFile);
			in.read(cbuf);
			in.close();
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}

		String sbuf = String.valueOf(cbuf);
		String[] resultLines = sbuf.split("\n");
		// CRF++ puts an extra CR/LF on the end
		if (!(resultLines.length == (examples.length + 1))) {
			throw new RuntimeException("CRF++ results length " + resultLines.length + " does not match input length "
					+ examples.length);
		}
		ClassicCounter<?>[] counters = new ClassicCounter<?>[resultLines.length - 1];
		for (int i = 0; i < (resultLines.length - 1); i++) {
			String[] resultCols = resultLines[i].split("\\s+");
			ClassicCounter<String> counter = new ClassicCounter<String>();
			for (int j = (nFeatures + 1); j < resultCols.length; j++) {
				String[] res = resultCols[j].split("/");
				if (!(res.length == 2)) {
					throw new RuntimeException("Result field not X/score: " + resultCols[j]);
				}
				String tag = res[0];
				double prob = Double.parseDouble(res[1]);
				counter.setCount(tag, prob);
			}
			counters[i] = counter;
		}
		return counters;
	}

}
