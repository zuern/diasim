package csli.util.classify.stanford;

import java.util.Iterator;

import weka.core.Instance;
import weka.core.Instances;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;

public class WekaClassifier extends Classifier {

	private weka.classifiers.Classifier classifier;
	private Instances instances;

	public WekaClassifier(weka.classifiers.Classifier classifier, Instances instances) {
		this.classifier = classifier;
		this.instances = instances;
	}

	@Override
	public Object classOf(Datum example) {
		double[] atts = new double[example.asFeatures().size() + 1];
		int i = 0;
		for (Iterator iterator = example.asFeatures().iterator(); iterator.hasNext();) {
			atts[i] = Double.parseDouble(iterator.next().toString());
			i++;
		}
		double cls = Double.parseDouble(example.label().toString());
		atts[i] = (cls > 0.0 ? 1.0 : 0.0);
		double res = 0.0;
		try {
			Instance instance = new Instance(1.0, atts);
			instance.setDataset(instances);
			res = classifier.classifyInstance(instance);
			// System.out.println("Classify inst " + res);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ((res > 0.0) ? SvmLightClassifierFactory.POS_LABEL : SvmLightClassifierFactory.NEG_LABEL);
	}

	@Override
	public ClassicCounter<?> scoresOf(Datum example) {
		double[] atts = new double[example.asFeatures().size() + 1];
		int i = 0;
		for (Iterator iterator = example.asFeatures().iterator(); iterator.hasNext();) {
			atts[i] = Double.parseDouble(iterator.next().toString());
			i++;
		}
		double cls = Double.parseDouble(example.label().toString());
		atts[i] = (cls > 0.0 ? 1.0 : 0.0);
		double res = 0.0;
		try {
			Instance instance = new Instance(1.0, atts);
			instance.setDataset(instances);
			res = classifier.classifyInstance(instance);
			// System.out.println("Classify inst " + res);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ClassicCounter<String> counter = new ClassicCounter<String>();
		counter.setCount(SvmLightClassifierFactory.POS_LABEL, ((res > 0.0) ? 1.0 : 0.0));
		counter.setCount(SvmLightClassifierFactory.NEG_LABEL, ((res > 0.0) ? 0.0 : 1.0));
		return counter;
	}

}
