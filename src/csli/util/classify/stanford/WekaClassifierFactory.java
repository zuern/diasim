package csli.util.classify.stanford;

import java.util.Iterator;
import java.util.List;

import weka.classifiers.bayes.BayesNet;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import edu.stanford.nlp.ling.Datum;

public class WekaClassifierFactory implements ClassifierFactory {

	private weka.classifiers.Classifier classifier;

	@Override
	public <D extends Datum> Classifier trainClassifier(List<D> examples) {
		classifier = new BayesNet();
		try {
			String[] options = weka.core.Utils
					.splitOptions("-D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5");
			classifier.setOptions(options);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		FastVector attInfo = new FastVector(examples.get(0).asFeatures().size() + 1);
		for (int i = 0; i < examples.get(0).asFeatures().size(); i++) {
			attInfo.addElement(new Attribute("f" + i));
		}
		;
		FastVector my_nominal_values = new FastVector(2);
		my_nominal_values.addElement("0");
		my_nominal_values.addElement("1");
		attInfo.addElement(new Attribute("cl", my_nominal_values));
		Instances instances = new Instances("ai", attInfo, examples.size());
		instances.setClassIndex(attInfo.size() - 1);
		for (int i = 0; i < examples.size(); i++) {
			double[] attValues = new double[examples.get(i).asFeatures().size() + 1];
			int j = 0;
			for (Iterator iterator = examples.get(i).asFeatures().iterator(); iterator.hasNext();) {
				attValues[j] = Double.parseDouble(iterator.next().toString());
				j++;
			}
			double cls = Double.parseDouble(examples.get(i).label().toString());
			attValues[j] = (cls > 0.0 ? 1.0 : 0.0);
			// System.out.println("set class " + attValues[j]);
			// System.out.println("set atts " + Arrays.asList(attValues[0],
			// attValues[1], attValues[2]));
			Instance instance = new Instance(1.0, attValues);
			instances.add(instance);
		}
		try {
			classifier.buildClassifier(instances);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		return new WekaClassifier(classifier, instances);
	}

}
