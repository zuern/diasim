package qmul.align;

import java.util.HashMap;

/**
 * Things to record about speakers/dialogues
 * 
 * @author mpurver
 */
public class MetricsMap extends HashMap<String, MetricsMap.Metrics> {

	public class Metrics {

		int numUnits;
		int numWords;
		int numTokens;
		double turnOffset;
		int numTurnOffsets;
		double wordRate;
		int numWordRates;

		/**
		 * @return the numUnits
		 */
		public int getNumUnits() {
			return numUnits;
		}

		/**
		 * @param numUnits
		 *            the numUnits to set
		 */
		public void setNumUnits(int numUnits) {
			this.numUnits = numUnits;
		}

		/**
		 * @return the numWords
		 */
		public int getNumWords() {
			return numWords;
		}

		/**
		 * @param numWords
		 *            the numWords to set
		 */
		public void setNumWords(int numWords) {
			this.numWords = numWords;
		}

		/**
		 * @return the numTokens
		 */
		public int getNumTokens() {
			return numTokens;
		}

		/**
		 * @param numTokens
		 *            the numTokens to set
		 */
		public void setNumTokens(int numTokens) {
			this.numTokens = numTokens;
		}

		/**
		 * @return the turnOffset
		 */
		public double getTurnOffset() {
			return turnOffset;
		}

		/**
		 * @param turnOffset
		 *            the turnOffset to set
		 */
		public void setTurnOffset(double turnOffset) {
			this.turnOffset = turnOffset;
		}

		/**
		 * @return the numTurnOffsets
		 */
		public int getNumTurnOffsets() {
			return numTurnOffsets;
		}

		/**
		 * @param numTurnOffsets
		 *            the numTurnOffsets to set
		 */
		public void setNumTurnOffsets(int numTurnOffsets) {
			this.numTurnOffsets = numTurnOffsets;
		}

		/**
		 * @return the wordRate
		 */
		public double getWordRate() {
			return wordRate;
		}

		/**
		 * @param wordRate
		 *            the wordRate to set
		 */
		public void setWordRate(double wordRate) {
			this.wordRate = wordRate;
		}

		/**
		 * @return the numWordRates
		 */
		public int getNumWordRates() {
			return numWordRates;
		}

		/**
		 * @param numWordRates
		 *            the numWordRates to set
		 */
		public void setNumWordRates(int numWordRates) {
			this.numWordRates = numWordRates;
		}

	}

	private static final long serialVersionUID = -2996980404035810911L;

	/**
	 * @return the numUnits
	 */
	public int getNumUnits(String key) {
		return get(key).getNumUnits();
	}

	/**
	 * @param numUnits
	 *            the numUnits to set
	 */
	public void setNumUnits(String key, int numUnits) {
		if (!containsKey(key)) {
			put(key, new Metrics());
		}
		get(key).setNumUnits(numUnits);
	}

	/**
	 * @return the numWords
	 */
	public int getNumWords(String key) {
		return get(key).getNumWords();
	}

	/**
	 * @param numWords
	 *            the numWords to set
	 */
	public void setNumWords(String key, int numWords) {
		if (!containsKey(key)) {
			put(key, new Metrics());
		}
		get(key).setNumWords(numWords);
	}

	/**
	 * @return the numTokens
	 */
	public int getNumTokens(String key) {
		return get(key).getNumTokens();
	}

	/**
	 * @param numTokens
	 *            the numTokens to set
	 */
	public void setNumTokens(String key, int numTokens) {
		if (!containsKey(key)) {
			put(key, new Metrics());
		}
		get(key).setNumTokens(numTokens);
	}

	/**
	 * @return the turnOffset
	 */
	public double getTurnOffset(String key) {
		return get(key).getTurnOffset();
	}

	/**
	 * @param turnOffset
	 *            the turnOffset to set
	 */
	public void setTurnOffset(String key, double turnOffset) {
		if (!containsKey(key)) {
			put(key, new Metrics());
		}
		get(key).setTurnOffset(turnOffset);
	}

	/**
	 * @return the numTurnOffsets
	 */
	public int getNumTurnOffsets(String key) {
		return get(key).getNumTurnOffsets();
	}

	/**
	 * @param numTurnOffsets
	 *            the numTurnOffsets to set
	 */
	public void setNumTurnOffsets(String key, int numTurnOffsets) {
		if (!containsKey(key)) {
			put(key, new Metrics());
		}
		get(key).setNumTurnOffsets(numTurnOffsets);
	}

	/**
	 * @return the wordRate
	 */
	public double getWordRate(String key) {
		return get(key).getWordRate();
	}

	/**
	 * @param wordRate
	 *            the wordRate to set
	 */
	public void setWordRate(String key, double wordRate) {
		if (!containsKey(key)) {
			put(key, new Metrics());
		}
		get(key).setWordRate(wordRate);
	}

	/**
	 * @return the numWordRates
	 */
	public int getNumWordRates(String key) {
		return get(key).getNumWordRates();
	}

	/**
	 * @param numWordRates
	 *            the numWordRates to set
	 */
	public void setNumWordRates(String key, int numWordRates) {
		if (!containsKey(key)) {
			put(key, new Metrics());
		}
		get(key).setNumWordRates(numWordRates);
	}

}