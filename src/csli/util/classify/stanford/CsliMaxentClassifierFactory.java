/*
 * Created on May 7, 2007 by mpurver
 */
package csli.util.classify.stanford;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.classify.LinearClassifierFactory;
import edu.stanford.nlp.ling.Datum;

public class CsliLinearClassifierFactory extends LinearClassifierFactory {

	private boolean upSample = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.stanford.nlp.classify.AbstractLinearClassifierFactory#trainClassifier(java.util.List)
	 */
	public <D extends Datum> Classifier trainClassifier(List<D> examples) {
		int upFactor = 1;
		if (upSample) {
			int nPos = 0;
			int nNeg = 0;
			for (Datum example : examples) {
				if (example.label().equals(SvmLightClassifierFactory.POS_LABEL)) {
					nPos++;
				} else {
					nNeg++;
				}
			}
			upFactor = nNeg / nPos;
			System.out.println(nPos + " pos, " + nNeg + " neg: upsampling by " + upFactor + " ... ");
		}
		List<Datum> ex = new ArrayList<Datum>();
		for (Datum example : examples) {
			ex.add(example);
			if (upSample && example.label().equals(SvmLightClassifierFactory.POS_LABEL)) {
				for (int i = 1; i < upFactor; i++) {
					ex.add(example);
				}
			}
		}
		if (upSample) {
			System.out.println("Upsampled from " + examples.size() + " to " + ex.size());
		}
		return super.trainClassifier(ex);
	}

	public void setUpSample(boolean upSample) {
		this.upSample = upSample;
	}

	public boolean isUpSample() {
		return upSample;
	}

}
