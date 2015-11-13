/*******************************************************************************
 * Copyright (c) 2013, 2014 Matthew Purver, Queen Mary University of London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Matthew Purver, Queen Mary University of London - initial API and implementation
 ******************************************************************************/
package qmul.corpus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * A {@link DialogueCorpus} created by random permutation of an existing {@link DialogueCorpus}
 * 
 * @author mpurver
 */
public class RandomCorpus extends DialogueCorpus {

	private static final long serialVersionUID = 5515129793029332080L;

	/**
	 * A {@link Comparator} for {@link Dialogue}s by length, either by turn or sentence
	 * 
	 * @author mpurver
	 */
	private class DialogueLengthComparator implements Comparator<Dialogue> {

		private Dialogue d;

		/**
		 * @param d
		 *            the {@link Dialogue} to compare to
		 */
		public DialogueLengthComparator(Dialogue d) {
			this.d = d;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Dialogue arg0, Dialogue arg1) {
			if (lengthUnits == LENGTH_IN_TURNS) {
				return Double.compare(Math.abs(arg0.numTurns() - d.numTurns()),
						Math.abs(arg1.numTurns() - d.numTurns()));
			} else if (lengthUnits == LENGTH_IN_SENTS) {
				return Double.compare(Math.abs(arg0.numSents() - d.numSents()),
						Math.abs(arg1.numSents() - d.numSents()));
			} else {
				throw new RuntimeException("unexpected length units value " + lengthUnits);
			}
		}

	}

	private static Random random = new Random();

	private DialogueCorpus rawCorpus;

	private int randType;

	/**
	 * RAND_S2ME_SPEAKER means randomly re-ordering each speaker's turns within their original dialogues
	 */
	public static final int RAND_S2ME_SPEAKER = -2;
	/**
	 * RAND_SAME_SPEAKER means keep all turns from one speaker A; substitute each other speaker with a random ordering
	 * of A's turns
	 */
	public static final int RAND_SAME_SPEAKER = -1;
	/**
	 * RAND_OTHER_SPEAKERS means keep all turns from one speaker; for each other speaker, choose a random speaker in a
	 * random other dialogue and take all their turns from that
	 */
	public static final int RAND_OTHER_SPEAKERS = 0;
	/**
	 * RAND_BEST_LENGTH_MATCH means keep all turns from one speaker; for each other speaker, choose a random speaker
	 * from the closest other dialogue in length and take all their turns from that
	 */
	public static final int RAND_BEST_LENGTH_MATCH = 1;
	/**
	 * RAND_COPY_CORPUS means use another {@link RandomCorpus} to give the choice of dialogues/speakers, and take all
	 * turns in order
	 */
	public static final int RAND_COPY_CORPUS = 2;
	/**
	 * RAND_BEST_LENGTH_RAND means keep all turns from one speaker; for each other speaker, choose a random speaker from
	 * the closest other dialogue in length and take all their turns from that, randomising their order. NB values of
	 * RAND_ types which involve keeping other-speaker turns in order should be less than this value
	 */
	public static final int RAND_BEST_LENGTH_RAND = 3;
	/**
	 * RAND_OTHER_TURNS means keep all turns from one speaker; choose random turns from random dialogues/speakers for
	 * all other turns. NB values of RAND_ types which involve keeping random other-speaker/dialogue correspondences
	 * should be less than this value
	 */
	public static final int RAND_OTHER_TURNS = 4;
	/**
	 * RAND_ALL_TURNS means choose all turns at random from random dialogues/speakers. NB values of RAND_ types which
	 * involve keeping turns from one speaker should be less than this value
	 */
	public static final int RAND_ALL_TURNS = 5;
	/**
	 * RAND_ALL_SENTS means choose all sentences at random from random dialogues/speakers (creating a turn for each
	 * sentence). NB values of RAND_ types which involve turns rather than sentences should be less than this value
	 */
	public static final int RAND_ALL_SENTS = 6;

	private int padType;

	/**
	 * PAD_WRAP means if we run out of turns when stitching dialogues together, wrap round to the beginning again
	 */
	public static final int PAD_WRAP = 0;
	/**
	 * PAD_END means if we run out of turns when stitching dialogues together, keep repeating the last turn
	 */
	public static final int PAD_END = 1;
	/**
	 * PAD_RAND_WRAP means if we run out of turns when stitching dialogues together, wrap round to a random point
	 */
	public static final int PAD_RAND_WRAP = 2;
	/**
	 * PAD_RAND_TURNS means if we run out of turns when stitching dialogues together, keep choosing random turns
	 */
	public static final int PAD_RAND_TURNS = 3;
	/**
	 * PAD_CUT means if we run out of turns when stitching dialogues together, stop
	 */
	public static final int PAD_CUT = 4;

	private int lengthUnits;

	/**
	 * LENGTH_IN_SENTS means any length comparison will be done by number of sentences
	 */
	public static final int LENGTH_IN_SENTS = 0;

	/**
	 * LENGTH_IN_TURNS means any length comparison will be done by number of turns
	 */
	public static final int LENGTH_IN_TURNS = 1;

	private boolean matchGenre;

	private boolean avoidSelf;

	private int fixedSpeakerOffset = 0;

	/**
	 * Create a new corpus with speakers/turns randomised, with fixed speaker = first speaker (if applicable)
	 * 
	 * @param corpus
	 *            the raw corpus to randomise
	 * @param randType
	 *            the type of randomisation - see {@link RandomCorpus}.RAND_*
	 * @param padType
	 *            the type of turn padding - see {@link RandomCorpus}.PAD_*
	 * @param lengthUnits
	 *            the (sentence/turn) units to use for length matching - see {@link RandomCorpus}.LENGTH_*
	 * @param matchGenre
	 *            whether to ensure dialogues maintain consistent genre
	 * @param avoidSelf
	 *            whether to use cross-dialogue speaker identity to avoid matching speakers with themselves
	 */
	public RandomCorpus(DialogueCorpus corpus, int randType, int padType, int lengthUnits, boolean matchGenre,
			boolean avoidSelf) {
		this(corpus, randType, padType, lengthUnits, matchGenre, avoidSelf, 0);
	}

	/**
	 * Create a new corpus with speakers/turns randomised
	 * 
	 * @param corpus
	 *            the raw corpus to randomise
	 * @param randType
	 *            the type of randomisation - see {@link RandomCorpus}.RAND_*
	 * @param padType
	 *            the type of turn padding - see {@link RandomCorpus}.PAD_*
	 * @param lengthUnits
	 *            the (sentence/turn) units to use for length matching - see {@link RandomCorpus}.LENGTH_*
	 * @param matchGenre
	 *            whether to ensure dialogues maintain consistent genre
	 * @param avoidSelf
	 *            whether to use cross-dialogue speaker identity to avoid matching speakers with themselves
	 * @param fixedSpeakerOffset
	 *            the offset from 0 of the speaker to keep as the fixed-turn-order one (if applicable given randType).
	 *            Default 0 (i.e. first speaker fixed)
	 */
	public RandomCorpus(DialogueCorpus corpus, int randType, int padType, int lengthUnits, boolean matchGenre,
			boolean avoidSelf, int fixedSpeakerOffset) {
		// super(corpus.getId() + "-" + Math.abs(random.nextInt()), corpus.getDir());
		// no need for a real directory, and easier to use something guaranteed to exist
		super(corpus.getId() + "-" + Math.abs(random.nextInt()), new File(System.getProperty("user.dir")));
		this.rawCorpus = corpus;
		this.randType = randType;
		this.padType = padType;
		this.lengthUnits = lengthUnits;
		this.matchGenre = matchGenre;
		this.avoidSelf = avoidSelf;
		this.fixedSpeakerOffset = fixedSpeakerOffset;
		if (avoidSelf && (randType == RAND_SAME_SPEAKER)) {
			throw new RuntimeException("can't avoid self if randomising same-speaker ...");
		}
		getSpeakerMap().putAll(rawCorpus.getSpeakerMap());
		getGenreMap().putAll(rawCorpus.getGenreMap());
		getGenreCounts().putAll(rawCorpus.getGenreCounts());
		if (randType < RAND_ALL_SENTS) {
			randomizeCorpus();
		} else {
			randomizeCorpusBySentence();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#setupCorpus()
	 */
	@Override
	public boolean setupCorpus() {
		// nothing to actually do here - we have to wait for the raw corpus to be set
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#loadDialogue(java.lang.String)
	 */
	@Override
	public boolean loadDialogue(String name) {
		// dynamic loading not supported
		return false;
	}

	private static DialogueCorpus corpusToCopy = null;

	/**
	 * @param corpusToCopy
	 *            only for RAND_COPY_CORPUS: the {@link RandomCorpus} to copy dialogue/speaker assignments from
	 */
	public static void setCorpusToCopy(DialogueCorpus corpusToCopy) {
		RandomCorpus.corpusToCopy = corpusToCopy;
	}

	/**
	 * Set up the random corpus
	 * 
	 * @return success
	 */
	private boolean randomizeCorpus() {
		ArrayList<Dialogue> dialoguesByLength = (ArrayList<Dialogue>) rawCorpus.getDialogues().clone();
		HashSet<Dialogue> usedDialogues = new HashSet<Dialogue>();
		DIALOGUES: for (Dialogue dRaw : rawCorpus.getDialogues()) {
			// if (!dRaw.getId().startsWith("KB2")) {
			// continue DIALOGUES;
			// }
			Dialogue d = addDialogue(dRaw.getId() + "-" + Math.abs(random.nextInt()), dRaw.getGenre());
			System.out.println("Making random dialogue " + d.getId() + " from " + dRaw.getId() + " length "
					+ (lengthUnits == LENGTH_IN_TURNS ? dRaw.numTurns() : dRaw.numSents()));
			DialogueSpeaker fixedSpeaker = dRaw.getTurns().get(0).getSpeaker();
			int iS = 0, iT = 0;
			while (iS < fixedSpeakerOffset) {
				DialogueSpeaker tmpSpeaker = dRaw.getTurns().get(iT).getSpeaker();
				if (!tmpSpeaker.getId().equals(fixedSpeaker.getId())) {
					fixedSpeaker = tmpSpeaker;
					iS++;
				}
				iT++;
			}
			if (randType == RAND_S2ME_SPEAKER) {
				fixedSpeaker = null;
			}
			HashMap<DialogueSpeaker, Dialogue> myDialogues = new HashMap<DialogueSpeaker, Dialogue>();
			HashMap<DialogueSpeaker, DialogueSpeaker> mySpeakers = new HashMap<DialogueSpeaker, DialogueSpeaker>();
			HashMap<DialogueSpeaker, ArrayList<Integer>> myTurnOrders = new HashMap<DialogueSpeaker, ArrayList<Integer>>();
			HashMap<DialogueSpeaker, Integer> myTurnIndices = new HashMap<DialogueSpeaker, Integer>();
			HashSet<DialogueSpeaker> usedSpeakers = new HashSet<DialogueSpeaker>();
			if (fixedSpeakerOffset > 0) {
				usedSpeakers.add(fixedSpeaker);
			}
			HashSet<DialogueTurn> usedTurns = new HashSet<DialogueTurn>();
			HashSet<Dialogue> triedDialogues = new HashSet<Dialogue>();
			Collections.sort(dialoguesByLength, new DialogueLengthComparator(dRaw));
			// TODO get rid of magic number 5 (= the similarity window size)
			final int SIM_WINDOW_SIZE = 5;
			// find last turn by each speaker for check later
			HashMap<DialogueSpeaker, DialogueTurn> lastTurns = new HashMap<DialogueSpeaker, DialogueTurn>();
			HashMap<DialogueSpeaker, Integer> numTurns = new HashMap<DialogueSpeaker, Integer>();
			for (DialogueTurn t : dRaw.getTurns()) {
				lastTurns.put(t.getSpeaker(), t);
				Integer n = numTurns.get(t.getSpeaker());
				numTurns.put(t.getSpeaker(), ((n == null) ? 0 : n + 1));
			}
			TURNS: for (DialogueTurn tRaw : dRaw.getTurns()) {
				// System.out.println("Making turn " + t.getId() + " because turn " + tRaw.getId());
				if ((randType < RAND_ALL_TURNS) && tRaw.getSpeaker().equals(fixedSpeaker)) {
					if (randType == RAND_S2ME_SPEAKER) {
						throw new RuntimeException("was hoping not to get here");
					}
					// leave out last turn from second speaker in v short dialogues (will always match self)
					if ((randType != RAND_SAME_SPEAKER) || (fixedSpeakerOffset == 0)
							|| (numTurns.get(tRaw.getSpeaker()) > SIM_WINDOW_SIZE)
							|| (!tRaw.getId().equals(lastTurns.get(tRaw.getSpeaker()).getId()))) {
						// if this is a turn from the speaker we're keeping, copy it
						DialogueTurn t = d.addTurn(tRaw.getNum(), tRaw.getSpeaker());
						t.setOriginalSpeaker(tRaw.getSpeaker());
						t.setOriginalId(tRaw.getId());
						t.setOriginalDialogue(tRaw.getDialogue());
						usedSpeakers.add(tRaw.getSpeaker());
						if (!Double.isNaN(tRaw.getStartTime())) {
							t.setStartTime(tRaw.getStartTime());
						}
						if (!Double.isNaN(tRaw.getEndTime())) {
							t.setEndTime(tRaw.getEndTime());
						}
						for (DialogueSentence sRaw : tRaw.getSents()) {
							DialogueSentence s = d.addSent(sRaw.getNum(), t, sRaw.getTranscription(), sRaw.getSyntax());
							s.setOriginalSpeaker(sRaw.getSpeaker());
							s.setOriginalId(sRaw.getId());
							if (sRaw.getTokens() != null) {
								s.setTokens(sRaw.getTokens());
							}
							if (!Double.isNaN(sRaw.getStartTime())) {
								s.setStartTime(sRaw.getStartTime());
							}
							if (!Double.isNaN(sRaw.getEndTime())) {
								s.setEndTime(sRaw.getEndTime());
							}
						}
						// System.out.println("Keeping speaker " + tRaw.getSpeaker() + ", copied from turn " +
						// tRaw.getId());
					} else {
						System.out.println("Skip same spk turn " + fixedSpeakerOffset + " " + tRaw.getId() + " "
								+ lastTurns.get(tRaw.getSpeaker()).getId() + " " + tRaw.getSpeaker().getId() + " "
								+ dRaw.getId() + " " + dRaw.numTurns() + " " + numTurns.get(tRaw.getSpeaker()));
					}
				} else {
					// otherwise choose a random dialogue; saving the choice if < RAND_OTHER_TURNS
					Dialogue dRand = (randType < RAND_OTHER_TURNS ? myDialogues.get(tRaw.getSpeaker()) : dRaw);
					while (dRand == null || (dRand.equals(dRaw) && (randType > RAND_SAME_SPEAKER))) {
						int i = 0;
						RANDDIALOGUES: while (true) {
							if ((randType == RAND_BEST_LENGTH_MATCH) || (randType == RAND_BEST_LENGTH_RAND)) {
								// if matching length, find closest
								dRand = dialoguesByLength.get(i++);
							} else if (randType == RAND_COPY_CORPUS) {
								// if copying assignments, find them
								dRand = getDialogueToCopy(dRaw);
								System.out.println("Found dRand " + dRand.getId() + " for raw " + dRaw.getId());
								break RANDDIALOGUES;
							} else if (randType <= RAND_SAME_SPEAKER) {
								// if doing same-speaker comparison, keep same dialogue
								dRand = dRaw;
							} else {
								// otherwise choose at random
								dRand = rawCorpus.getDialogues().get(random.nextInt(rawCorpus.getDialogues().size()));
							}
							// don't allow same one, or one we've used already
							// TODO this will not work properly for >2 speakers, we'll run out of dialogues
							if ((dRand.equals(dRaw) && (randType > RAND_SAME_SPEAKER))
									|| (usedDialogues.contains(dRand) && (randType != RAND_S2ME_SPEAKER))) {
								continue RANDDIALOGUES;
							}
							// test for genre match if using
							if (matchGenre && (!dRaw.getGenre().equals(dRand.getGenre()))) {
								continue RANDDIALOGUES;
							}
							// test for speaker match if using (against SECOND speaker in chosen dialogue)
							if (avoidSelf && (randType < RAND_OTHER_TURNS)) {
								for (DialogueSpeaker usedSpeaker : usedSpeakers) {
									if (usedSpeaker.probablySameAs(dRand.getTurns().get(fixedSpeakerOffset + 1)
											.getSpeaker())) {
										continue RANDDIALOGUES;
									}
								}
							}
							break RANDDIALOGUES;
						}
						System.out.println("Got random dialogue " + dRand.getId() + " length "
								+ (lengthUnits == LENGTH_IN_TURNS ? dRand.numTurns() : dRand.numSents()));
						myDialogues.put(tRaw.getSpeaker(), dRand);
						triedDialogues.add(dRand);
						if (randType < RAND_OTHER_TURNS) {
							usedDialogues.add(dRand);
						}
						// initialise with first turn by SECOND speaker in the chosen dialogue
						myTurnIndices.put(tRaw.getSpeaker(), 0);
						ArrayList<Integer> myTurnOrder = new ArrayList<Integer>();
						ArrayList<Integer> origTurnOrder = new ArrayList<Integer>();
						for (int turnIndex = 0; turnIndex < dRand.numTurns(); turnIndex++) {
							if (dRand.getTurns().get(turnIndex).getSpeaker()
									.equals(randType == RAND_SAME_SPEAKER ? fixedSpeaker : tRaw.getSpeaker())) {
								myTurnOrder.add(turnIndex);
								origTurnOrder.add(turnIndex);
							}
						}
						System.out.println("original turnorder " + origTurnOrder);
						if ((randType == RAND_BEST_LENGTH_RAND) || (randType == RAND_S2ME_SPEAKER)) {
							Collections.shuffle(myTurnOrder, random);
						}
						if (randType == RAND_SAME_SPEAKER) {
							boolean redraw = true;
							while (redraw) {
								Collections.shuffle(myTurnOrder, random);
								redraw = false;
								// TODO get rid of magic number 5 (= the similarity window size)
								int window = SIM_WINDOW_SIZE;
								// window = Math.min(window, origTurnOrder.size() / 2); // for v short dialogues
								window = Math.min(window, origTurnOrder.size() - fixedSpeakerOffset); // short dialogues
								// if (dRaw.getId().startsWith("G54")) {
								System.out.println("DRAW win " + window + " turnorder " + myTurnOrder + " vs "
										+ origTurnOrder + " speaker " + fixedSpeakerOffset);
								// }
								DRAW: for (int i1 = 0; i1 < origTurnOrder.size(); i1++) {
									for (int i2 = (i1 - 1 + fixedSpeakerOffset); i2 >= Math.max(0, i1 - window
											+ fixedSpeakerOffset); i2--) {
										if (origTurnOrder.get(i1) == myTurnOrder.get(i2)) {
											redraw = true;
											// if (dRaw.getId().startsWith("G54")) {
											System.out.println("DRAW redrawing, too close " + origTurnOrder.get(i1)
													+ " " + myTurnOrder.get(i2));
											// }
											break DRAW;
										}
									}
								}
							}
						}
						System.out.println("DRAW random turnorder " + myTurnOrder + " speaker " + fixedSpeakerOffset);
						myTurnOrders.put(tRaw.getSpeaker(), myTurnOrder);
						if (randType == RAND_SAME_SPEAKER) {
							mySpeakers.put(tRaw.getSpeaker(), fixedSpeaker);
						} else if (randType == RAND_S2ME_SPEAKER) {
							mySpeakers.put(tRaw.getSpeaker(), dRand.getTurns().get(origTurnOrder.get(0)).getSpeaker());
						} else {
							mySpeakers.put(tRaw.getSpeaker(), dRand.getTurns().get(1).getSpeaker());
						}
					}
					// then choose a turn ...
					DialogueTurn tRand = null;
					if (randType < RAND_OTHER_TURNS) {
						// if keeping speakers fixed, choose the next turn from the dialogue/speaker combo we've chosen
						while (tRand == null || !tRand.getSpeaker().equals(mySpeakers.get(tRaw.getSpeaker()))) {
							Integer tIndex = myTurnIndices.get(tRaw.getSpeaker());
							System.out.println("For d " + dRand.getId() + " t " + tRaw.getId() + " trying " + tIndex);
							// check not run out of turns (e.g. with 2nd-speaker-offset RANDOM_SAME_SPEAKER where 1st
							// speaker has an extra turn)
							if (tIndex >= myTurnOrders.get(tRaw.getSpeaker()).size()) {
								tRand = null;
								break;
							}
							Integer index = tIndex;
							if (tIndex >= dRand.numTurns()) {
								if (padType == PAD_WRAP) {
									index = tIndex = 0;
								} else if (padType == PAD_END) {
									index = tIndex = dRand.numTurns() - 1;
								} else if (padType == PAD_RAND_WRAP) {
									index = tIndex = random.nextInt(dRand.numTurns());
								} else if (padType == PAD_RAND_TURNS) {
									index = random.nextInt(dRand.numTurns());
								} else if (padType == PAD_CUT) {
									break TURNS;
								} else {
									throw new RuntimeException("Unknown pad type " + padType);
								}
							}
							tRand = dRand.getTurns().get(myTurnOrders.get(tRaw.getSpeaker()).get(tIndex));
							System.out.println("Check turn for speaker " + tRand.getSpeaker() + " " + tRaw.getSpeaker()
									+ " " + tRand.getId());
							myTurnIndices.put(tRaw.getSpeaker(), index + 1);
							// check not already used
							for (DialogueTurn usedTurn : usedTurns) {
								if (usedTurn.equals(tRand)) {
									tRand = null;
								}
							}
						}
						if (tRand == null) {
							System.out.println("No matching random turn");
						} else {
							System.out.println("Got random turn " + tRand.getId());
						}
					} else {
						// otherwise just choose a turn at random
						HashSet<DialogueTurn> triedTurns = new HashSet<DialogueTurn>();
						RANDTURNS: while (true) {
							// if we've tried everything in this dRand dialogue and got stuck, draw another dRand
							if (triedTurns.size() == dRand.getTurns().size()) {
								NEWRANDDIALOGUE: while (true) {
									if (triedDialogues.size() == rawCorpus.getDialogues().size()) {
										// we've tried everything! give up on this dialogue. Could check for PAD_CUT,
										// but we don't have much option anyway
										break TURNS;
									}
									if (randType <= RAND_SAME_SPEAKER) {
										dRand = dRaw;
									} else {
										dRand = rawCorpus.getDialogues().get(
												random.nextInt(rawCorpus.getDialogues().size()));
									}
									triedDialogues.add(dRand);
									// don't allow same one, or one we've used already
									// TODO this will not work properly for >2 speakers, we'll run out of dialogues
									if ((dRand.equals(dRaw) && (randType > RAND_SAME_SPEAKER))
											|| (usedDialogues.contains(dRand) && (randType != RAND_S2ME_SPEAKER))) {
										continue NEWRANDDIALOGUE;
									}
									// test for genre match if using
									if (matchGenre && (!dRaw.getGenre().equals(dRand.getGenre()))) {
										continue NEWRANDDIALOGUE;
									}
									break NEWRANDDIALOGUE;
								}
								System.out.println("Run out of turns, got new random dialogue " + dRand.getId());
								triedTurns.clear();
							}
							// draw a turn
							tRand = dRand.getTurns().get(random.nextInt(dRand.getTurns().size()));
							triedTurns.add(tRand);
							// check not already used
							// System.out.print("trying new random turn " + tRand.getId() + " ... ");
							for (DialogueTurn usedTurn : usedTurns) {
								if (usedTurn.equals(tRand)) {
									// System.out.println("already used");
									continue RANDTURNS;
								}
							}
							// check speaker suitable
							if (avoidSelf) {
								for (DialogueSpeaker usedSpeaker : usedSpeakers) {
									if ((!(usedSpeaker.equals(tRand.getSpeaker())))
											&& usedSpeaker.probablySameAs(tRand.getSpeaker())) {
										// System.out.println("same as spk " + usedSpeaker.getId() + " "
										// + tRand.getSpeaker().getId());
										continue RANDTURNS;
									}
								}
							}
							break RANDTURNS;
						}
						System.out.println("Got random turn " + tRand.getId());
					}
					if (tRand != null) { // could be there was NO matching turn e.g. second-person RAND_SAME_SPEAKER
						// where 1st speaker has an extra turn
						usedTurns.add(tRand);
						DialogueTurn t = d.addTurn(tRaw.getNum(), tRaw.getSpeaker());
						t.setOriginalSpeaker(tRand.getSpeaker());
						t.setOriginalId(tRand.getId());
						t.setOriginalDialogue(tRand.getDialogue());
						usedSpeakers.add(tRand.getSpeaker());
						if (!Double.isNaN(tRand.getStartTime())) {
							t.setStartTime(tRand.getStartTime());
						}
						if (!Double.isNaN(tRand.getEndTime())) {
							t.setEndTime(tRand.getEndTime());
						}
						for (DialogueSentence sRand : tRand.getSents()) {
							DialogueSentence s = d.addSent(sRand.getNum(), t, sRand.getTranscription(),
									sRand.getSyntax());
							s.setOriginalSpeaker(sRand.getSpeaker());
							s.setOriginalId(sRand.getId());
							if (sRand.getTokens() != null) {
								s.setTokens(sRand.getTokens());
							}
							if (!Double.isNaN(sRand.getStartTime())) {
								s.setStartTime(sRand.getStartTime());
							}
							if (!Double.isNaN(sRand.getEndTime())) {
								s.setEndTime(sRand.getEndTime());
							}
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Set up the random corpus on a {@link DialogueSentence} basis
	 * 
	 * @return success
	 */
	private boolean randomizeCorpusBySentence() {
		ArrayList<Dialogue> dialoguesByLength = (ArrayList<Dialogue>) rawCorpus.getDialogues().clone();
		HashSet<Dialogue> usedDialogues = new HashSet<Dialogue>();
		DIALOGUES: for (Dialogue dRaw : rawCorpus.getDialogues()) {
			Dialogue d = addDialogue(dRaw.getId() + "-" + Math.abs(random.nextInt()), dRaw.getGenre());
			System.out.println("Making random dialogue " + d.getId() + " from " + dRaw.getId() + " length "
					+ (lengthUnits == LENGTH_IN_TURNS ? dRaw.numTurns() : dRaw.numSents()));
			DialogueSpeaker firstSpeaker = dRaw.getSents().get(0).getSpeaker();
			HashMap<DialogueSpeaker, Dialogue> myDialogues = new HashMap<DialogueSpeaker, Dialogue>();
			HashMap<DialogueSpeaker, DialogueSpeaker> mySpeakers = new HashMap<DialogueSpeaker, DialogueSpeaker>();
			HashMap<DialogueSpeaker, ArrayList<Integer>> mySentOrders = new HashMap<DialogueSpeaker, ArrayList<Integer>>();
			HashMap<DialogueSpeaker, Integer> myTurnIndices = new HashMap<DialogueSpeaker, Integer>();
			HashSet<DialogueSpeaker> usedSpeakers = new HashSet<DialogueSpeaker>();
			HashSet<DialogueSentence> usedSents = new HashSet<DialogueSentence>();
			HashSet<Dialogue> triedDialogues = new HashSet<Dialogue>();
			Collections.sort(dialoguesByLength, new DialogueLengthComparator(dRaw));
			SENTS: for (DialogueSentence sRaw : dRaw.getSents()) {
				// System.out.println("Making turn " + t.getId() + " because turn " + tRaw.getId());
				if ((randType < RAND_ALL_TURNS) && sRaw.getSpeaker().equals(firstSpeaker)) {
					// if this is a turn from the speaker we're keeping, copy it
					DialogueTurn t = d.addTurn(sRaw.getTurn().getNum(), sRaw.getSpeaker());
					t.setOriginalSpeaker(sRaw.getSpeaker());
					t.setOriginalId(sRaw.getTurn().getId());
					usedSpeakers.add(sRaw.getSpeaker());
					if (!Double.isNaN(sRaw.getTurn().getStartTime())) {
						t.setStartTime(sRaw.getTurn().getStartTime());
					}
					if (!Double.isNaN(sRaw.getTurn().getEndTime())) {
						t.setEndTime(sRaw.getTurn().getEndTime());
					}
					DialogueSentence s = d.addSent(sRaw.getNum(), t, sRaw.getTranscription(), sRaw.getSyntax());
					s.setOriginalSpeaker(sRaw.getSpeaker());
					s.setOriginalId(sRaw.getId());
					if (sRaw.getTokens() != null) {
						s.setTokens(sRaw.getTokens());
					}
					// System.out.println("Keeping speaker " + tRaw.getSpeaker() + ", copied from turn " +
					// tRaw.getId());
					if (!Double.isNaN(sRaw.getStartTime())) {
						s.setStartTime(sRaw.getStartTime());
					}
					if (!Double.isNaN(sRaw.getEndTime())) {
						s.setEndTime(sRaw.getEndTime());
					}
				} else {
					// otherwise choose a random dialogue; saving the choice if < RAND_OTHER_TURNS
					Dialogue dRand = (randType < RAND_OTHER_TURNS ? myDialogues.get(sRaw.getSpeaker()) : dRaw);
					while (dRand == null || dRand.equals(dRaw)) {
						int i = 0;
						RANDDIALOGUES: while (true) {
							if ((randType == RAND_BEST_LENGTH_MATCH) || (randType == RAND_BEST_LENGTH_RAND)) {
								// if matching length, find closest
								dRand = dialoguesByLength.get(i++);
							} else if (randType == RAND_COPY_CORPUS) {
								// if copying assignments, find them
								dRand = getDialogueToCopy(dRaw);
								System.out.println("Found dRand " + dRand.getId() + " for raw " + dRaw.getId());
								break RANDDIALOGUES;
							} else {
								// otherwise choose at random
								dRand = rawCorpus.getDialogues().get(random.nextInt(rawCorpus.getDialogues().size()));
							}
							// don't allow same one, or one we've used already
							// TODO this will not work properly for >2 speakers, we'll run out of dialogues
							if (dRand.equals(dRaw) || usedDialogues.contains(dRand)) {
								continue RANDDIALOGUES;
							}
							// test for genre match if using
							if (matchGenre && (!dRaw.getGenre().equals(dRand.getGenre()))) {
								continue RANDDIALOGUES;
							}
							// test for speaker match if using (against SECOND speaker in chosen dialogue)
							if (avoidSelf && (randType < RAND_OTHER_TURNS)) {
								for (DialogueSpeaker usedSpeaker : usedSpeakers) {
									if (usedSpeaker.probablySameAs(dRand.getTurns().get(1).getSpeaker())) {
										continue RANDDIALOGUES;
									}
								}
							}
							break RANDDIALOGUES;
						}
						System.out.println("Got random dialogue " + dRand.getId() + " length "
								+ (lengthUnits == LENGTH_IN_TURNS ? dRand.numTurns() : dRand.numSents()));
						myDialogues.put(sRaw.getSpeaker(), dRand);
						triedDialogues.add(dRand);
						if (randType < RAND_OTHER_TURNS) {
							usedDialogues.add(dRand);
						}
						// initialise with first turn by SECOND speaker in the chosen dialogue
						myTurnIndices.put(sRaw.getSpeaker(), 0);
						ArrayList<Integer> myTurnOrder = new ArrayList<Integer>();
						for (int turnIndex = 0; turnIndex < dRand.numTurns(); turnIndex++) {
							myTurnOrder.add(turnIndex);
						}
						if (randType == RAND_BEST_LENGTH_RAND) {
							Collections.shuffle(myTurnOrder, random);
						}
						mySentOrders.put(sRaw.getSpeaker(), myTurnOrder);
						mySpeakers.put(sRaw.getSpeaker(), dRand.getTurns().get(1).getSpeaker());
					}
					// then choose a turn ...
					DialogueSentence sRand = null;
					if (randType < RAND_OTHER_TURNS) {
						// if keeping speakers fixed, choose the next turn from the dialogue/speaker combo we've chosen
						while (sRand == null || !sRand.getSpeaker().equals(mySpeakers.get(sRaw.getSpeaker()))) {
							Integer tIndex = myTurnIndices.get(sRaw.getSpeaker());
							System.out.println("For d " + dRand.getId() + " t " + sRaw.getId() + " trying " + tIndex);
							Integer index = tIndex;
							if (tIndex >= dRand.numTurns()) {
								if (padType == PAD_WRAP) {
									index = tIndex = 0;
								} else if (padType == PAD_END) {
									index = tIndex = dRand.numTurns() - 1;
								} else if (padType == PAD_RAND_WRAP) {
									index = tIndex = random.nextInt(dRand.numTurns());
								} else if (padType == PAD_RAND_TURNS) {
									index = random.nextInt(dRand.numTurns());
								} else if (padType == PAD_CUT) {
									break SENTS;
								} else {
									throw new RuntimeException("Unknown pad type " + padType);
								}
							}
							sRand = dRand.getSents().get(mySentOrders.get(sRaw.getSpeaker()).get(tIndex));
							// System.out.println("Got turn for speaker " + t.getSpeaker() + " " + tRand.getId());
							myTurnIndices.put(sRaw.getSpeaker(), index + 1);
							// check not already used
							for (DialogueSentence usedSent : usedSents) {
								if (usedSent.equals(sRand)) {
									sRand = null;
								}
							}
						}
					} else {
						// otherwise just choose a turn at random
						HashSet<DialogueSentence> triedSents = new HashSet<DialogueSentence>();
						RANDSENTS: while (true) {
							// if we've tried everything in this dRand dialogue and got stuck, draw another dRand
							if (triedSents.size() == dRand.getSents().size()) {
								NEWRANDDIALOGUE: while (true) {
									if (triedDialogues.size() == rawCorpus.getDialogues().size()) {
										// we've tried everything! give up on this dialogue. Could check for PAD_CUT,
										// but we don't have much option anyway
										break SENTS;
									}
									dRand = rawCorpus.getDialogues().get(
											random.nextInt(rawCorpus.getDialogues().size()));
									triedDialogues.add(dRand);
									// don't allow same one, or one we've used already
									// TODO this will not work properly for >2 speakers, we'll run out of dialogues
									if (dRand.equals(dRaw) || usedDialogues.contains(dRand)) {
										continue NEWRANDDIALOGUE;
									}
									// test for genre match if using
									if (matchGenre && (!dRaw.getGenre().equals(dRand.getGenre()))) {
										continue NEWRANDDIALOGUE;
									}
									break NEWRANDDIALOGUE;
								}
								System.out.println("Run out of sents, got new random dialogue " + dRand.getId());
								triedSents.clear();
							}
							// draw a turn
							sRand = dRand.getSents().get(random.nextInt(dRand.getSents().size()));
							triedSents.add(sRand);
							// check not already used
							// System.out.print("trying new random turn " + tRand.getId() + " ... ");
							for (DialogueSentence usedSent : usedSents) {
								if (usedSent.equals(sRand)) {
									// System.out.println("already used");
									continue RANDSENTS;
								}
							}
							// check speaker suitable
							if (avoidSelf) {
								for (DialogueSpeaker usedSpeaker : usedSpeakers) {
									if ((!(usedSpeaker.equals(sRand.getSpeaker())))
											&& usedSpeaker.probablySameAs(sRand.getSpeaker())) {
										// System.out.println("same as spk " + usedSpeaker.getId() + " "
										// + tRand.getSpeaker().getId());
										continue RANDSENTS;
									}
								}
							}
							break RANDSENTS;
						}
						System.out.println("Got random sent " + sRand.getId());
					}
					usedSents.add(sRand);
					DialogueTurn t = d.addTurn(sRaw.getTurn().getNum(), sRaw.getSpeaker());
					t.setOriginalSpeaker(sRand.getSpeaker());
					t.setOriginalId(sRaw.getTurn().getId());
					t.setOriginalDialogue(sRand.getDialogue());
					usedSpeakers.add(sRand.getSpeaker());
					if (!Double.isNaN(sRaw.getTurn().getStartTime())) {
						t.setStartTime(sRaw.getTurn().getStartTime());
					}
					if (!Double.isNaN(sRaw.getTurn().getEndTime())) {
						t.setEndTime(sRaw.getTurn().getEndTime());
					}
					DialogueSentence s = d.addSent(sRand.getNum(), t, sRand.getTranscription(), sRand.getSyntax());
					s.setOriginalSpeaker(sRand.getSpeaker());
					s.setOriginalId(sRand.getId());
					if (sRand.getTokens() != null) {
						s.setTokens(sRand.getTokens());
					}
					if (!Double.isNaN(sRand.getStartTime())) {
						s.setStartTime(sRand.getStartTime());
					}
					if (!Double.isNaN(sRand.getEndTime())) {
						s.setEndTime(sRand.getEndTime());
					}
				}
			}
		}
		return true;
	}

	/**
	 * @param dRaw
	 * @return the (raw) dialogue to copy. HACKY relies on original speaker IDs being set and containing original
	 *         dialogue IDs
	 */
	private Dialogue getDialogueToCopy(Dialogue dRaw) {
		for (Dialogue dCopy : corpusToCopy.getDialogues()) {
			if (dCopy.getId().startsWith(dRaw.getId())) {
				// this is the one originally generated from dRaw in corpusToCopy
				for (DialogueTurn tCopy : dCopy.getTurns()) {
					// if originalDialogue has been set, use that
					if ((tCopy.getOriginalDialogue() != null)
							&& (!tCopy.getOriginalDialogue().getId().equals(dCopy.getId()))) {
						for (Dialogue d : rawCorpus.getDialogues()) {
							if (d.getId().equals(tCopy.getOriginalDialogue().getId())) {
								return d;
							}
						}
					}
					// otherwise check originalSpeaker
					// System.out.println("FOund speaker " + tCopy.getSpeaker() + " " + tCopy.getOriginalSpeaker());
					if (!tCopy.getSpeaker().equals(tCopy.getOriginalSpeaker())) {
						String sId = tCopy.getOriginalSpeaker().getId();
						String dId = sId.substring(0, sId.lastIndexOf(":"));
						// System.out.println("FOund id " + dId);
						for (Dialogue d : rawCorpus.getDialogues()) {
							if (d.getId().equals(dId)) {
								return d;
							}
						}
						System.out.println("Couldn't find matching id " + dId);
						return null;
					}
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see qmul.corpus.DialogueCorpus#topTenSynProductions()
	 */
	@Override
	public HashSet<String> topTenSynProductions() {
		return rawCorpus.topTenSynProductions();
	}

	/**
	 * just for testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		DCPSECorpus dc = new DCPSECorpus("C:\\Documents and Settings\\mpurver\\My Documents\\corpora", 2, 2, 0, 5);
		System.out.println(dc.getDialogues().get(0));
		RandomCorpus rc = new RandomCorpus(dc, RAND_ALL_TURNS, PAD_CUT, LENGTH_IN_TURNS, false, false);
		System.out.println("Completely random: ");
		System.out.println(rc.getDialogues().get(0));
		rc = new RandomCorpus(dc, RAND_BEST_LENGTH_MATCH, PAD_CUT, LENGTH_IN_TURNS, true, true);
		System.out.println("Get best length other speaker: ");
		System.out.println(rc.getDialogues().get(0));
		rc = new RandomCorpus(dc, RAND_OTHER_SPEAKERS, PAD_CUT, LENGTH_IN_TURNS, true, true);
		System.out.println("Randomize other speaker: ");
		System.out.println(rc.getDialogues().get(0));
		rc = new RandomCorpus(dc, RAND_OTHER_SPEAKERS, PAD_WRAP, LENGTH_IN_TURNS, true, true);
		System.out.println("Randomize other speaker, pad by wrapping: ");
		System.out.println(rc.getDialogues().get(0));
	}
}
