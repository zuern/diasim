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
package qmul.align;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import csli.util.dsp.Smoother;
import csli.util.dsp.SmoothingFactory;
import qmul.corpus.BNCCorpus;
import qmul.corpus.CombinedCorpus;
import qmul.corpus.DCPSECorpus;
import qmul.corpus.Dialogue;
import qmul.corpus.DialogueCorpus;
import qmul.corpus.DialogueSentence;
import qmul.corpus.DialogueSpeaker;
import qmul.corpus.DialogueTurn;
import qmul.corpus.DialogueUnit;
import qmul.corpus.RandomCorpus;
import qmul.corpus.SwitchboardCorpus;
import qmul.util.ApacheStatistics;
import qmul.util.MapUtil;
import qmul.util.MapUtil.DescendingComparator;
import qmul.util.MathUtil;
import qmul.util.parse.CreateTreeFromDCPSE;
import qmul.util.parse.CreateTreeFromSWBD;
import qmul.util.similarity.SimilarityMeasure;
import qmul.util.treekernel.TreeKernel;
import qmul.window.DialogueWindower;
import qmul.window.OtherSpeakerAllOtherSentenceWindower;
import qmul.window.OtherSpeakerAllOtherTurnWindower;
import qmul.window.OtherSpeakerSentenceWindower;
import qmul.window.OtherSpeakerTurnWindower;
import qmul.window.SameSpeakerAllOtherSentenceWindower;
import qmul.window.SameSpeakerAllOtherTurnWindower;
import qmul.window.SameSpeakerSentenceWindower;
import qmul.window.SameSpeakerTurnWindower;
import qmul.window.SentenceWindower;
import qmul.window.TurnWindower;

/**
 * A general class for similarity testing over a {@link DialogueCorpus}
 * 
 * @author mpurver
 */
public class AlignmentTester<X extends DialogueUnit> {

	private DialogueCorpus corpus;
	private SimilarityMeasure<X> sim;
	private DialogueWindower<X> win;
	private OutputStream xls = null;
	private boolean counts = false;

	private static final int NORM_NONE = 0;
	private static final int NORM_MEAN = 1;
	private static final int NORM_MAX = 2;

	private int normalisation = NORM_NONE;
	private Smoother smoother = SmoothingFactory.getSmoother("null");

	/**
	 * Default constructor - doesn't write XLS output to file
	 */
	public AlignmentTester() {
		super();
	}

	/**
	 * @param xlsFile
	 *            a file to write XLS output to
	 */
	public AlignmentTester(File xlsFile) {
		this();
		try {
			xls = new FileOutputStream(xlsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process a single dialogue
	 * 
	 * @param d
	 *            the dialogue to process
	 * @param wb
	 *            the XLS workbook to write to, or null not to bother
	 * @return a list of {@link Double} scores, one per {@link DialogueWindower} step (e.g. dialogue turn)
	 */
	public List<Double> processDialogue(Dialogue d, Workbook wb, HashMap<String, ArrayList<Double>> speakerScores,
			HashMap<String, String> originalSpks, HashMap<String, ArrayList<Double>> speakerN, MetricsMap spkMetrics,
			MetricsMap totMetrics, Workbook wbcounts, HashMap<String, HashMap<Object, Integer>> allCounts,
			HashMap<String, HashMap<Object, Integer>> commonCounts, HashMap<Object, Integer> diaAllCounts,
			HashMap<Object, Integer> diaCommonCounts) {

		CreationHelper creationHelper = wb.getCreationHelper();
		
		win.setDialogue(d);
		sim.reset();
		ArrayList<DialogueSpeaker> spks = new ArrayList<DialogueSpeaker>(d.getSpeakers());
		Collections.sort(spks);
		Sheet sheet = (wb == null ? null : wb.createSheet(d.getId().replaceAll(":", "-")));
		Sheet sheetcounts = (wbcounts == null ? null : wbcounts.createSheet(d.getId().replaceAll(":", "-")));
		int iRow = 0;
		if (sheet != null) {
			iRow = writeSheetHeader(creationHelper, sheet, iRow, d, spks);
		}
		int iCRow = 0;
		if (sheetcounts != null) {
			iCRow = writeSheetHeader(creationHelper, sheet, iCRow, d, spks);
		}
		ArrayList<Double> scores = new ArrayList<Double>();
		HashSet<X> counted = new HashSet<X>();
		do {
			List<X> left = win.getLeftWindow();
			Collections.reverse(left); // windowers return things in dialogue order: we'll look progressively backwards
			List<X> right = win.getRightWindow();
			// System.out.println("lengthS " + left.size() + " " + right.size());
			double score = 0.0;
			double n = 0.0;
			for (X r : right) {
				String spkKey = makeSpkKey(r.getSpeaker(), d);
				String originalSpkKey = "";
				if (r.getOriginalSpeaker() != null) {
					originalSpkKey = r.getOriginalSpeaker().getId();
					// fix for the fact that BNC speakers are not currently given SUBdialogue ID in their ID - TODO
					// change that?
					Dialogue od = r.getOriginalDialogue();
					if ((od == null) && (r instanceof DialogueSentence)) {
						od = ((DialogueSentence) r).getTurn().getOriginalDialogue();
					}
					String originalDia;
					if (od != null) {
						originalDia = od.getId();
					} else {
						originalDia = d.getId().replaceFirst("-\\d+$", "");
					}
					if (!originalSpkKey.contains(originalDia)) {
						if (!originalDia.contains(":")) {
							throw new RuntimeException("can't find super-dialogue, no : in " + originalDia);
						}
						String originalSuperDia = originalDia.substring(0, originalDia.lastIndexOf(":"));
						if (originalSpkKey.contains(originalSuperDia)) {
							originalSpkKey = originalSpkKey.replace(originalSuperDia, originalDia);
						} else {
							throw new RuntimeException("spk key without super-dialogue " + spkKey + ", "
									+ originalSpkKey + ", " + originalDia);
						}
					}
				}
				Row row = (wb == null ? null : sheet.createRow(iRow++));
				int iCol = 0;
				Cell cell = (wb == null ? null : row.createCell(iCol++, Cell.CELL_TYPE_STRING));
				if (cell != null) {
					cell.setCellValue(creationHelper.createRichTextString(r.getSpeaker().getId()));
					cell = row.createCell(iCol++, Cell.CELL_TYPE_STRING);
					cell.setCellValue(creationHelper.createRichTextString(originalSpkKey));
					// cell = row.createCell(iCol++, Cell.CELL_TYPE_STRING);
					// cell.setCellValue(creationHelper.createRichTextString(r.getId()));
					cell = row.createCell(iCol++, Cell.CELL_TYPE_STRING);
					cell.setCellValue(creationHelper.createRichTextString(r.toString()));
					row.setHeightInPoints(12);
					sheet.setColumnWidth(iCol - 1, 2560);
				}
				if (!speakerScores.containsKey(spkKey)) {
					speakerScores.put(spkKey, new ArrayList<Double>());
					speakerN.put(spkKey, new ArrayList<Double>());
					originalSpks.put(spkKey, originalSpkKey);
					for (int i = 0; i < win.getLeftWindowSize(); i++) {
						speakerScores.get(spkKey).add(0.0);
						speakerN.get(spkKey).add(0.0);
					}
					Boolean isTurns = null;
					if (left.size() > 0) {
						isTurns = (left.get(0) instanceof DialogueTurn);
					} else if (right.size() > 0) {
						isTurns = (right.get(0) instanceof DialogueTurn);
					}
					spkMetrics.setNumUnits(spkKey, 0);
					totMetrics.setNumUnits(d.getId(), (isTurns ? d.numTurns() : d.numSents()));
					spkMetrics.setNumWords(spkKey, 0);
					totMetrics.setNumWords(d.getId(), d.numWords());
					spkMetrics.setNumTokens(spkKey, 0);
					totMetrics.setNumTokens(d.getId(), d.numTokens());
				}
				int iLeft = 0;
				double offset = Double.NaN;
				boolean gotOffset = false;
				for (X l : left) {
					double s = sim.similarity(l, r);
					// System.out.println("Siml = " + s + " for l:" + l.getId() + " r:" + r.getId());
					if ((l.getOriginalId() != null) && (r.getOriginalId() != null)
							&& l.getOriginalId().equals(r.getOriginalId())) {
						System.out.println("Equal IDs sim = " + s + " for l:" + l.getId() + " " + l.getOriginalId()
								+ " r:" + r.getId() + " " + r.getOriginalId() + " d " + d.getId() + " nturns "
								+ d.numTurns());
					}
					if (wbcounts != null) {
						if (!counted.contains(l)) {
							MapUtil.addAll(diaAllCounts, sim.rawCountsA());
							MapUtil.addAll(allCounts.get(""), sim.rawCountsA());
							MapUtil.addAll(allCounts.get(d.getGenre()), sim.rawCountsA());
							counted.add(l);
						}
						if (!counted.contains(r)) {
							MapUtil.addAll(diaAllCounts, sim.rawCountsB());
							MapUtil.addAll(allCounts.get(""), sim.rawCountsB());
							MapUtil.addAll(allCounts.get(d.getGenre()), sim.rawCountsB());
							counted.add(r);
						}
						MapUtil.addAll(diaCommonCounts, sim.rawCountsAB());
						MapUtil.addAll(commonCounts.get(""), sim.rawCountsAB());
						MapUtil.addAll(commonCounts.get(d.getGenre()), sim.rawCountsAB());
					}
					cell = (wb == null ? null : row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC));
					if (cell != null) {
						cell.setCellValue(s);
					}
					score += s;
					n++;
					speakerScores.get(spkKey).set(iLeft, speakerScores.get(spkKey).get(iLeft) + s);
					speakerN.get(spkKey).set(iLeft, speakerN.get(spkKey).get(iLeft) + 1);
					if (!win.getClass().toString().contains("AllOther")) { // for "all other" windowers, actually
						// average over "window"
						iLeft++;
					}
					if (!gotOffset) {
						offset = r.getStartTime() - l.getEndTime();
						gotOffset = true;
						// if (!Double.isNaN(offset)) {
						// System.out.println("Offset = " + offset + " for l:" + l.getId() + " r:" + r.getId());
						// }
					}
				}
				// print number sents/words/tokens
				iCol += (win.getLeftWindowSize() - left.size() + 1);
				if (wb != null) { // if we are writing to a workbook
					cell = (wb == null ? null : row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC));
					cell.setCellValue(r instanceof DialogueTurn ? ((DialogueTurn) r).getSents().size() : 1);
					cell = (wb == null ? null : row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC));
					cell.setCellValue(r.numWords());
					cell = (wb == null ? null : row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC));
					cell.setCellValue(r.numTokens());
				}
				iCol += 1;
				if (!Double.isNaN(offset)) {
					cell = (wb == null ? null : row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC));
					cell.setCellValue(offset);
				} else {
					iCol++;
				}
				double wordRate = (double) (r.getEndTime() - r.getStartTime()) / (double) r.numWords();
				if (r.numWords() == 0) {
					wordRate = Double.NaN; // on some OSs this doesn't happen in the calc above
				}
				if (!Double.isNaN(wordRate)) {
					cell = (wb == null ? null : row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC));
					cell.setCellValue(wordRate);
				} else {
					iCol++;
				}
				// make sure we counted this one - the first one can get missed if leftWindow empty
				if ((wbcounts != null) && !counted.contains(r)) {
					sim.similarity(r, r);
					MapUtil.addAll(diaAllCounts, sim.rawCountsA());
					MapUtil.addAll(allCounts.get(""), sim.rawCountsA());
					MapUtil.addAll(allCounts.get(d.getGenre()), sim.rawCountsA());
					counted.add(r);
				}
				spkMetrics.setNumUnits(spkKey, spkMetrics.getNumUnits(spkKey) + 1);
				spkMetrics.setNumWords(spkKey, spkMetrics.getNumWords(spkKey) + r.numWords());
				spkMetrics.setNumTokens(spkKey, spkMetrics.getNumTokens(spkKey) + r.numTokens());
				if (!Double.isNaN(offset)) {
					spkMetrics.setTurnOffset(spkKey, spkMetrics.getTurnOffset(spkKey) + offset);
					spkMetrics.setNumTurnOffsets(spkKey, spkMetrics.getNumTurnOffsets(spkKey) + 1);
					totMetrics.setTurnOffset(d.getId(), totMetrics.getTurnOffset(d.getId()) + offset);
					totMetrics.setNumTurnOffsets(d.getId(), totMetrics.getNumTurnOffsets(d.getId()) + 1);
				}
				if (!Double.isNaN(wordRate)) {
					spkMetrics.setWordRate(spkKey, spkMetrics.getWordRate(spkKey) + wordRate);
					spkMetrics.setNumWordRates(spkKey, spkMetrics.getNumWordRates(spkKey) + 1);
					totMetrics.setWordRate(d.getId(), totMetrics.getWordRate(d.getId()) + wordRate);
					totMetrics.setNumWordRates(d.getId(), totMetrics.getNumWordRates(d.getId()) + 1);
				}
			}
			scores.add((n == 0.0) ? 0.0 : (score / n));
		} while (win.advance());
		if (wb != null) {
			iRow++;
			for (DialogueSpeaker spk : spks) {
				String spkKey = makeSpkKey(spk, d);
				Row row = sheet.createRow(iRow++);
				int iCol = 0;
				row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(spk.getId()));
				row.createCell(iCol++, Cell.CELL_TYPE_STRING)
						.setCellValue(creationHelper.createRichTextString(originalSpks.get(spkKey)));
				row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Mean"));
				for (int i = 0; i < win.getLeftWindowSize(); i++) {
					if (speakerN.get(spkKey).get(i) > 0) {
						row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC)
								.setCellValue(speakerScores.get(spkKey).get(i) / speakerN.get(spkKey).get(i));
					} else {
						iCol++;
					}
					// System.out
					// .println("score " + i + " for speaker " + spkKey + "=" + speakerScores.get(spkKey).get(i));
					// System.out.println("N " + i + " for speaker " + spkKey + "=" + speakerN.get(spkKey).get(i));
					// System.out.println("mean " + i + " for speaker " + spkKey + "="
					// + (speakerScores.get(spkKey).get(i) / speakerN.get(spkKey).get(i)));
				}
				iCol++;
				row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(
						(double) spkMetrics.getNumUnits(spkKey) / (double) spkMetrics.getNumUnits(spkKey));
				row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(
						(double) spkMetrics.getNumWords(spkKey) / (double) spkMetrics.getNumUnits(spkKey));
				row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(
						(double) spkMetrics.getNumTokens(spkKey) / (double) spkMetrics.getNumUnits(spkKey));
				iCol++;
				row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(
						(double) spkMetrics.getTurnOffset(spkKey) / (double) spkMetrics.getNumTurnOffsets(spkKey));
				row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(
						(double) spkMetrics.getWordRate(spkKey) / (double) spkMetrics.getNumWordRates(spkKey));
			}
		}
		if (wbcounts != null) {
			iCRow++;
			ArrayList<Object> keys = new ArrayList<Object>(diaAllCounts.keySet());
			Collections.sort(keys, new DescendingComparator<Object>(diaAllCounts));
			for (Object key : keys) {
				Row row = sheetcounts.createRow(iCRow++);
				int iCol = 0;
				row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(key.toString()));
				Cell cell = row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC);
				if (diaAllCounts.get(key) != null) {
					cell.setCellValue(diaAllCounts.get(key));
				}
				cell = row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC);
				if (diaCommonCounts.get(key) != null) {
					cell.setCellValue(diaCommonCounts.get(key));
				}
			}
		}
		return scores;
	}

	private int writeSheetHeader(CreationHelper creationHelper, Sheet sheet, int iRow, Dialogue d, List<DialogueSpeaker> spks) {
		int iCol = 0;
		Row row = sheet.createRow(iRow++);
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("ID"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(d.getId()));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Num speakers"));
		row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(d.numSpeakers());
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Num turns"));
		row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(d.numTurns());
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Num sents"));
		row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(d.numSents());
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Num words"));
		row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(d.numWords());
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Num tok words"));
		row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(d.numTokens());
		iRow++;

		row = sheet.createRow(iRow++);
		iCol = 0;
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Speaker"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Orig Speaker"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("First Name"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Last Name"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Gender"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Age"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Occupation"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Dialogue genre"));
		for (DialogueSpeaker s : spks) {
			// HACK to find first original speaker for speaker - will only work with consistent speaker pairing
			DialogueSpeaker os = null;
			for (DialogueTurn t : d.getTurns()) {
				if (t.getSpeaker().equals(s)) {
					os = t.getOriginalSpeaker();
					break;
				}
			}
			row = sheet.createRow(iRow++);
			iCol = 0;
			row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(s.getId()));
			row.createCell(iCol++, Cell.CELL_TYPE_STRING)
					.setCellValue(creationHelper.createRichTextString(os == null ? "" : os.getId()));
			s = (os == null ? s : os);
			row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(s.getFirstName()));
			row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(s.getLastName()));
			row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(s.getGender()));
			row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(s.getAge()));
			row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(s.getOccupation()));
			row.createCell(iCol++, Cell.CELL_TYPE_STRING)
					.setCellValue(creationHelper.createRichTextString(corpus.getGenreMap().get(s.getId().split(":")[0])));
		}
		iRow++;

		row = sheet.createRow(iRow++);
		iCol = 0;
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Speaker"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Orig Speaker"));
		// row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Turn ID"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Transcription"));
		for (int i = 0; i < getWin().getLeftWindowSize(); i++) {
			row.createCell(iCol + i, Cell.CELL_TYPE_STRING)
					.setCellValue(creationHelper.createRichTextString("Val i-" + (i + 1)));
		}
		iCol += getWin().getLeftWindowSize() + 1;
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Num sents"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Num words"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Num tok words"));
		iCol += 1;
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Offset time"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Time per word"));
		return iRow;
	}

	/**
	 * we actually want spkKey to represent the (sub-)dialogue/speaker pairing. In some corpora (e.g. DCPSE) speaker ID
	 * is unique per (sub-)dialogue anyway, as we know so little about real-world speaker ID. In some (e.g. BNC),
	 * speaker ID may not be unique per (sub-)dialogue, so make it that way
	 * 
	 * @param spk
	 * @param d
	 * @return
	 */
	private String makeSpkKey(DialogueSpeaker spk, Dialogue d) {
		String spkKey = spk.getId();
		String dId = d.getId();
		if (!spkKey.contains(dId)) {
			String mainId = "";
			if (dId.matches("^.*-\\d+$")) {
				mainId = dId.substring(0, dId.lastIndexOf("-"));
				if (!spkKey.startsWith(mainId)) {
					mainId = "";
				}
			}
			if (mainId.isEmpty() && dId.contains(":")) { // avoid redundant info
				mainId = dId.substring(0, dId.indexOf(":"));
			}
			if (!mainId.isEmpty() && spkKey.contains(mainId)) {
				spkKey = spkKey.replaceFirst(mainId, dId);
			} else {
				spkKey = dId + ":" + spkKey;
			}
		}
		return spkKey;
	}

	/**
	 * Process all dialogues in the corpus
	 * 
	 * @return a list of lists of scores (one list per dialogue)
	 */
	public List<List<Double>> processCorpus(String runId) {
		ApacheStatistics stats = new ApacheStatistics();
		Workbook wb = (xls == null ? null : new XSSFWorkbook());
		Workbook wbcounts = (xls == null ? null : (counts ? new XSSFWorkbook() : null));
		ArrayList<List<Double>> scores = new ArrayList<List<Double>>();
		System.out.println("Similarity measure " + sim.getClass().getName() + ", windower " + win);
		System.out.println("Smoothing " + smoother + ", normalisation=" + normalisation);
		System.out.println("Processing corpus " + corpus.getId() + " with " + corpus.numDialogues() + " dialogues ...");
		HashMap<String, String> originalSpks = new HashMap<String, String>();
		MetricsMap spkMetrics = new MetricsMap();
		MetricsMap totMetrics = new MetricsMap();
		HashMap<String, ArrayList<Double>> speakerScores = new HashMap<String, ArrayList<Double>>();
		HashMap<String, ArrayList<Double>> speakerN = new HashMap<String, ArrayList<Double>>();
		// maps from genres to maps from objects to integers
		HashMap<String, HashMap<Object, Integer>> allCounts = new HashMap<String, HashMap<Object, Integer>>();
		HashMap<String, HashMap<Object, Integer>> commonCounts = new HashMap<String, HashMap<Object, Integer>>();
		allCounts.put("", new HashMap<Object, Integer>());
		commonCounts.put("", new HashMap<Object, Integer>());
		for (String genre : corpus.getGenreCounts().keySet()) {
			if (corpus.getGenreCounts().get(genre) > 0) {
				allCounts.put(genre, new HashMap<Object, Integer>());
				commonCounts.put(genre, new HashMap<Object, Integer>());
			}
		}
		ArrayList<Double> means = new ArrayList<Double>();
		for (Dialogue d : corpus.getDialogues()) {
			// if (!d.getId().startsWith("KB2")) {
			// continue;
			// }
			HashMap<Object, Integer> diaAllCounts = new HashMap<Object, Integer>();
			HashMap<Object, Integer> diaCommonCounts = new HashMap<Object, Integer>();
			List<Double> subScores = processDialogue(d, wb, speakerScores, originalSpks, speakerN, spkMetrics,
					totMetrics, wbcounts, allCounts, commonCounts, diaAllCounts, diaCommonCounts);
			System.out.println("Got " + subScores.size() + " scores for dialogue " + d.getId() + ": " + subScores);
			scores.add(subScores);
			// get stats
			Double mean = MathUtil.mean(subScores);
			means.add(mean);
			System.out.println("Mean for dialogue " + d.getId() + ": " + mean);
			ApacheStatistics subStats = new ApacheStatistics(subScores);
			System.out.println("Mean, SD for dialogue " + d.getId() + " = " + subStats.getMean() + " "
					+ subStats.getStandardDeviation());
			stats.addValues(subScores);
		}
		if (wb != null) {
			printSummarySheet(wb, null, speakerScores, originalSpks, speakerN, spkMetrics, totMetrics,
					corpus instanceof CombinedCorpus);
			if (counts) {
				printSummaryCountSheet(wbcounts, null, allCounts, commonCounts);
			}
			try {
				wb.write(xls);
				if (counts) {
					wbcounts.write(new FileOutputStream(new File("counts-" + runId + ".xlsx")));
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
			File summaryXls = new File("summary.xlsx");
			File countsXls = new File("counts.xlsx");
			Workbook summaryWb = null;
			Workbook countsWb = null;
			try {
				FileInputStream summaryXlsIn = new FileInputStream(summaryXls);
				summaryWb = new XSSFWorkbook(summaryXlsIn);
				if (counts) {
					FileInputStream countsXlsIn = new FileInputStream(countsXls);
					countsWb = new XSSFWorkbook(countsXlsIn);
				}
			} catch (FileNotFoundException e) {
				summaryWb = new XSSFWorkbook();
				if (counts) {
					countsWb = new XSSFWorkbook();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
			printSummarySheet(summaryWb, runId, speakerScores, originalSpks, speakerN, spkMetrics, totMetrics,
					corpus instanceof CombinedCorpus);
			if (counts) {
				printSummaryCountSheet(countsWb, runId, allCounts, commonCounts);
			}
			try {
				OutputStream summaryXlsOut = new FileOutputStream(summaryXls);
				summaryWb.write(summaryXlsOut);
				if (counts) {
					OutputStream countsXlsOut = new FileOutputStream(countsXls);
					countsWb.write(countsXlsOut);
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		System.out.println("Mean over all dialogues: " + MathUtil.mean(means));
		System.out.println("Mean, SD over all dialogues: " + stats.getMean() + " " + stats.getStandardDeviation());
		ApacheStatistics meanStats = new ApacheStatistics(means);
		System.out.println(
				"Mean, SD over all dialogue means: " + meanStats.getMean() + " " + meanStats.getStandardDeviation());
		return scores;
	}

	/**
	 * @param orig
	 * @return a version which is less than 32 chars long, to keep the {@link Workbook} class restrictions happy
	 */
	String shorten(String orig) {
		String shorter = new String(orig);
		shorter = shorter.replace("stanford", "stn");
		shorter = shorter.replace("random", "rd");
		shorter = shorter.replace("nointj", "noi");
		while (shorter.length() > 31) {
			// shorter = shorter.replaceFirst("-(\\w{2})\\w+", "-$1");
			shorter = shorter.replaceFirst("-(\\w)\\w(\\w)\\w*", "-$1$2");
		}
		if (!orig.equals(shorter)) {
			System.out.println("Shortened XLS sheet name " + orig + " -> " + shorter);
		}
		return shorter;
	}

	/**
	 * Print a summary sheet on the (gulp) excel spreadsheet
	 * 
	 * @param wb
	 * @param sheetName
	 * @param speakerScores
	 * @param originalSpks
	 * @param speakerN
	 */
	private void printSummarySheet(Workbook wb, String sheetName, HashMap<String, ArrayList<Double>> speakerScores,
			HashMap<String, String> originalSpks, HashMap<String, ArrayList<Double>> speakerN, MetricsMap spkMetrics,
			MetricsMap totMetrics, boolean pairedCorpus) {

		CreationHelper creationHelper = wb.getCreationHelper();
		sheetName = (sheetName == null ? "Summary" : shorten(sheetName));
		System.out.println("Checking workbook " + wb + " for sheet " + sheetName);
		Sheet sheet = wb.getSheet(sheetName);
		if (sheet != null) {
			System.out.println("Exists, removing sheet " + sheetName);
			wb.removeSheetAt(wb.getSheetIndex(sheet));
		}
		sheet = wb.createSheet(sheetName);
		wb.setSheetOrder(sheetName, 0);
		int iRow = 0;
		// first general identifying stuff
		Row row = sheet.createRow(iRow++);
		row.createCell(0, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Corpus"));
		row.createCell(1, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(getCorpus().getId()));
		row = sheet.createRow(iRow++);
		row.createCell(0, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Windower"));
		row.createCell(1, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(getWin().toString()));
		row = sheet.createRow(iRow++);
		row.createCell(0, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Similarity Measure"));
		row.createCell(1, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(getSim().toString()));
		// now header
		row = sheet.createRow(iRow++);
		row = sheet.createRow(iRow++);
		int iCol = 0;
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Speaker"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Genre"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Orig Speaker"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Orig Genre"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Speaker #units"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Dialogue #units"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Speaker #words"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Dialogue #words"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Speaker #tokens"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Dialogue #tokens"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Speaker avg offset"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Dialogue avg offset"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Speaker avg wordrate"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Dialogue avg wordrate"));
		iCol++;
		for (int i = 0; i < getWin().getLeftWindowSize(); i++) {
			row.createCell(i + iCol, Cell.CELL_TYPE_STRING)
					.setCellValue(creationHelper.createRichTextString("Mean i-" + (i + 1)));
		}
		// now means per speaker
		List<String> spks = new ArrayList<String>(speakerScores.keySet());
		Collections.sort(spks);
		List<Double> means = new ArrayList<Double>();
		List<Double> nums = new ArrayList<Double>();
		for (int i = 0; i < getWin().getLeftWindowSize(); i++) {
			means.add(0.0);
			nums.add(0.0);
		}
		int nAll = 0;
		int nMatch = 0;
		for (String spk : spks) {
			// System.out.println("org chk [" + originalSpks.get(spk) + "][" + spk + "]");
			boolean matching = false;
			if ((originalSpks.get(spk) != null) && originalSpks.get(spk).contains(":")) {
				int li = originalSpks.get(spk).lastIndexOf(":");
				String pre = originalSpks.get(spk).substring(0, li);
				String suf = originalSpks.get(spk).substring(li);
				matching = spk.startsWith(pre) && spk.endsWith(suf);
			}
			nAll++;
			if (!pairedCorpus || matching) {
				nMatch++;
				// System.out.println("match " + pre + " " + suf);
				row = sheet.createRow(iRow++);
				iCol = 0;
				String dId = spk.replaceFirst("(.*)_.*", "$1");
				row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(spk));
				row.createCell(iCol++, Cell.CELL_TYPE_STRING)
						.setCellValue(creationHelper.createRichTextString(corpus.getGenreMap().get(spk.split(":")[0])));
				row.createCell(iCol++, Cell.CELL_TYPE_STRING)
						.setCellValue(creationHelper.createRichTextString(originalSpks.get(spk)));
				row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(
						creationHelper.createRichTextString(corpus.getGenreMap().get(originalSpks.get(spk).split(":")[0])));
				row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(spkMetrics.getNumUnits(spk));
				row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(totMetrics.getNumUnits(dId));
				row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(spkMetrics.getNumWords(spk));
				row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(totMetrics.getNumWords(dId));
				row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(spkMetrics.getNumTokens(spk));
				row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC).setCellValue(totMetrics.getNumTokens(dId));
				if (Double.isNaN(spkMetrics.getTurnOffset(spk)) || spkMetrics.getNumTurnOffsets(spk) == 0) {
					iCol++;
				} else {
					row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC)
							.setCellValue(spkMetrics.getTurnOffset(spk) / (double) spkMetrics.getNumTurnOffsets(spk));
				}
				if (Double.isNaN(totMetrics.getTurnOffset(dId)) || totMetrics.getNumTurnOffsets(dId) == 0) {
					iCol++;
				} else {
					row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC)
							.setCellValue(totMetrics.getTurnOffset(dId) / (double) totMetrics.getNumTurnOffsets(dId));
				}
				if (Double.isNaN(spkMetrics.getWordRate(spk)) || spkMetrics.getNumWordRates(spk) == 0) {
					iCol++;
				} else {
					row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC)
							.setCellValue(spkMetrics.getWordRate(spk) / (double) spkMetrics.getNumWordRates(spk));
				}
				if (Double.isNaN(totMetrics.getWordRate(dId)) || totMetrics.getNumWordRates(dId) == 0) {
					iCol++;
				} else {
					row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC)
							.setCellValue(totMetrics.getWordRate(dId) / (double) totMetrics.getNumWordRates(dId));
				}
				iCol++;
				for (int i = 0; i < speakerScores.get(spk).size(); i++) {
					if (speakerN.get(spk).get(i) > 0.0) {
						double mean = speakerScores.get(spk).get(i) / speakerN.get(spk).get(i);
						row.createCell(i + iCol, Cell.CELL_TYPE_NUMERIC).setCellValue(mean);
						means.set(i, means.get(i) + mean);
						nums.set(i, nums.get(i) + 1);
					}
				}
			}
		}
		System.out.println("Matched " + nMatch + " of " + nAll);
		// and a final row for overall means
		row = sheet.createRow(iRow++);
		iCol = 14;
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Overall"));
		for (int i = 0; i < getWin().getLeftWindowSize(); i++) {
			means.set(i, means.get(i) / nums.get(i));
			row.createCell(i + iCol, Cell.CELL_TYPE_NUMERIC).setCellValue(means.get(i));
		}
	}

	/**
	 * Print a summary sheet on the (gulp) excel spreadsheet
	 */
	private void printSummaryCountSheet(Workbook wb, String sheetName,
			HashMap<String, HashMap<Object, Integer>> allCounts,
			HashMap<String, HashMap<Object, Integer>> commonCounts) {

		CreationHelper creationHelper = wb.getCreationHelper();
		sheetName = (sheetName == null ? "Summary" : shorten(sheetName));
		System.out.println("Checking workbook " + wb + " for sheet " + sheetName);
		Sheet sheet = wb.getSheet(sheetName);
		if (sheet != null) {
			System.out.println("Exists, removing sheet " + sheetName);
			wb.removeSheetAt(wb.getSheetIndex(sheet));
		}
		sheet = wb.createSheet(sheetName);
		wb.setSheetOrder(sheetName, 0);
		int iRow = 0;
		// first general identifying stuff
		Row row = sheet.createRow(iRow++);
		row.createCell(0, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Corpus"));
		row.createCell(1, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(getCorpus().getId()));
		row = sheet.createRow(iRow++);
		row.createCell(0, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Windower"));
		row.createCell(1, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(getWin().toString()));
		row = sheet.createRow(iRow++);
		row.createCell(0, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Similarity Measure"));
		row.createCell(1, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(getSim().toString()));
		// now header
		row = sheet.createRow(iRow++);
		row = sheet.createRow(iRow++);
		int iCol = 0;
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Type"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Overall count"));
		row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString("Common count"));
		for (String genre : allCounts.keySet()) {
			if (genre.isEmpty())
				continue;
			row.createCell(iCol++, Cell.CELL_TYPE_STRING)
					.setCellValue(creationHelper.createRichTextString(genre + " overall count"));
			row.createCell(iCol++, Cell.CELL_TYPE_STRING)
					.setCellValue(creationHelper.createRichTextString(genre + " common count"));
		}
		ArrayList<Object> keys = new ArrayList<Object>(allCounts.get("").keySet());
		Collections.sort(keys, new DescendingComparator<Object>(allCounts.get("")));
		for (Object key : keys) {
			row = sheet.createRow(iRow++);
			iCol = 0;
			row.createCell(iCol++, Cell.CELL_TYPE_STRING).setCellValue(creationHelper.createRichTextString(key.toString()));
			Cell cell = row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC);
			if (allCounts.get("").get(key) != null) {
				cell.setCellValue(allCounts.get("").get(key));
			}
			cell = row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC);
			if (commonCounts.get("").get(key) != null) {
				cell.setCellValue(commonCounts.get("").get(key));
			}
			for (String genre : allCounts.keySet()) {
				if (genre.isEmpty())
					continue;
				cell = row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC);
				if (allCounts.get(genre).get(key) != null) {
					cell.setCellValue(allCounts.get(genre).get(key));
				}
				cell = row.createCell(iCol++, Cell.CELL_TYPE_NUMERIC);
				if (commonCounts.get(genre).get(key) != null) {
					cell.setCellValue(commonCounts.get(genre).get(key));
				}
			}
		}
	}

	public DialogueCorpus getCorpus() {
		return corpus;
	}

	public void setCorpus(DialogueCorpus corpus) {
		this.corpus = corpus;
	}

	public SimilarityMeasure<X> getSim() {
		return sim;
	}

	public void setSim(SimilarityMeasure<X> sim) {
		this.sim = sim;
	}

	public DialogueWindower<X> getWin() {
		return win;
	}

	public void setWin(DialogueWindower<X> win) {
		this.win = win;
	}

	/**
	 * Normalise a data array depending on the normalisation setting
	 * 
	 * @param data
	 */
	private void normalise(List<Double> data) {
		double norm = 1.0;
		if (normalisation == NORM_MAX) {
			norm = Collections.max(data);
		} else if (normalisation == NORM_MEAN) {
			double sum = 0.0;
			for (Double datum : data) {
				sum += datum;
			}
			norm = (sum / data.size());
		}
		for (int i = 0; i < data.size(); i++) {
			data.set(i, data.get(i) / norm);
		}
	}

	/**
	 * @param raw
	 *            the raw data
	 * @param n
	 *            the number of desired bins (> raw.size())
	 * @return a list of length n via linear interpolation of data
	 */
	private List<Double> interpolate(List<Double> raw, int n) {
		if (n < raw.size()) {
			throw new RuntimeException("Can only interpolate to longer array");
		}
		List<Double> cooked = new ArrayList<Double>();
		for (double j = 0.0; j < n; j++) {
			double ind = j * (double) (raw.size() - 1) / (double) (n - 1);
			double last = Math.floor(ind);
			double next = Math.ceil(ind);
			double val = (last == next) ? raw.get((int) last)
					: raw.get((int) last) * (next - ind) + raw.get((int) next) * (ind - last);
			// System.out.println("I " + j + " " + ind + " " + last + " " + raw.get((int) last) + " " + next + " "
			// + raw.get((int) next) + " " + val);
			cooked.add((int) j, val);
		}
		return cooked;
	}

	/**
	 * Average and plot the scores
	 * 
	 * @param scores
	 *            the scores from processCorpus
	 * @param num
	 *            number of data points (e.g. turns) per dialogue to interpolate to
	 */
	public void processScores(List<List<Double>> scores, int num) {
		List<Double> means = new ArrayList<Double>();
		for (int m = 0; m < num; m++) {
			means.add(0.0);
		}
		int i = 0;
		for (List<Double> subScores : scores) {
			System.out.println("Dialogue " + i + " raw  scores: " + subScores);
			normalise(subScores);
			System.out.println("Dialogue " + i + " norm scores: " + subScores);
			List<Double> smoothScores = smoother.smooth(subScores);
			System.out.println("Dialogue " + i + " smooth scores: " + smoothScores);
			List<Double> intScores = interpolate(smoothScores, num);
			System.out.println("Dialogue " + i + " intp scores: " + intScores);
			plotScores("Dialogue " + i, subScores, smoothScores, intScores);
			for (int m = 0; m < num; m++) {
				means.set(m, means.get(m) + intScores.get(m));
			}
			i++;
		}
		for (int m = 0; m < num; m++) {
			means.set(m, means.get(m) / (double) i);
		}
		plotScores(means, "Mean over " + i + " dialogues");
	}

	/**
	 * Plot a turn vs score chart
	 * 
	 * @param scores
	 * @param title
	 */
	private void plotScores(List<Double> scores, String title) {
		ApplicationFrame af = new ApplicationFrame(title);
		final XYSeries series = new XYSeries("Coherence scores");
		for (int x = 0; x < scores.size(); x++) {
			series.add(x, scores.get(x));
		}
		final XYSeriesCollection data = new XYSeriesCollection(series);
		final JFreeChart chart = ChartFactory.createXYLineChart(title, "Turn", "Score", data, PlotOrientation.VERTICAL,
				true, true, false);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(1000, 500));
		af.setContentPane(chartPanel);
		af.pack();
		RefineryUtilities.centerFrameOnScreen(af);
		af.setVisible(true);
	}

	/**
	 * Plot a turn vs score chart with arbitrary subplots
	 * 
	 * @param scores1
	 * @param title
	 */
	private void plotScores(String title, List<Double>... scores) {
		ApplicationFrame af = new ApplicationFrame(title);
		final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis());
		plot.setGap(10.0);

		for (int i = 0; i < scores.length; i++) {

			final XYSeries series = new XYSeries("Coherence scores " + i);
			for (int x = 0; x < scores[i].size(); x++) {
				series.add(x, scores[i].get(x));
			}
			XYSeriesCollection data = new XYSeriesCollection(series);
			XYPlot subplot = new XYPlot(data, null, new NumberAxis(), new StandardXYItemRenderer());
			subplot.setRangeAxisLocation(i == 0 ? AxisLocation.TOP_OR_LEFT : AxisLocation.BOTTOM_OR_LEFT);
			plot.add(subplot, 1);
		}

		plot.setOrientation(PlotOrientation.VERTICAL);
		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(1000, 500));
		af.setContentPane(chartPanel);
		af.pack();
		RefineryUtilities.centerFrameOnScreen(af);
		af.setVisible(true);
	}

	/**
	 * Run a test, optionally producing a XLS spreadsheet and some pretty graphs
	 * 
	 * @param baseDir
	 *            the base corpus dir, or null if using default
	 * @param corpusRoot
	 *            "dcpse" or "swbd"
	 * @param randType
	 *            "" for the raw corpus, "random1", "random2" etc for a defined randomisation type
	 * @param simType
	 *            "lex" or "syn", or "syntop", "synbot" for top 10/other rules only
	 * @param unitType
	 *            "turn" or "sent"
	 * @param winType
	 *            "oth" or "same"
	 * @param monteCarlo
	 *            iteration for MC
	 * @param xlsOutput
	 *            whether to write XLS spreadsheet
	 * @param plotGraphs
	 *            whether to plot graphs
	 */
	public static void runTest(String baseDir, String corpusRoot, String randType, String simType, String unitType,
			String winType, int monteCarlo, boolean xlsOutput, boolean plotGraphs) {
		String randSuffix = (randType.isEmpty() ? "" : "_" + randType + (monteCarlo < 0 ? "" : "_" + monteCarlo));
		String runId = corpusRoot + randSuffix + "-" + simType + "-" + winType + "-" + unitType;
		File xlsFile = new File(runId + ".xlsx");

		AlignmentTester<? extends DialogueUnit> at;
		int leftWindow = ((simType.equals("gries") || winType.startsWith("all")) ? 1 : 5);
		int rightWindow = 1;
		int stepWindow = 1;
		if (unitType.equals("turn") || unitType.equals("tuco")) {
			AlignmentTester<DialogueTurn> att = (xlsOutput ? new AlignmentTester<DialogueTurn>(xlsFile)
					: new AlignmentTester<DialogueTurn>());
			if (winType.equals("oth")) {
				att.setWin(new OtherSpeakerTurnWindower(null, leftWindow, rightWindow, stepWindow));
			} else if (winType.equals("sam")) {
				att.setWin(new SameSpeakerTurnWindower(null, leftWindow, rightWindow, stepWindow));
			} else if (winType.equals("alloth")) {
				att.setWin(new OtherSpeakerAllOtherTurnWindower(null, leftWindow, rightWindow, stepWindow));
			} else if (winType.equals("allsam")) {
				att.setWin(new SameSpeakerAllOtherTurnWindower(null, leftWindow, rightWindow, stepWindow));
			} else if (winType.equals("any")) {
				att.setWin(new TurnWindower(null, leftWindow, rightWindow, stepWindow));
			} else {
				throw new RuntimeException("unknown win type " + winType);
			}
			if (simType.equals("lex")) {
				if (unitType.equals("tuco")) {
					att.setSim(new TurnConcatSimilarityMeasure(new SentenceLexicalSimilarityMeasure()));
				} else {
					att.setSim(new TurnAverageSimilarityMeasure(new SentenceLexicalSimilarityMeasure()));
				}
			} else if (simType.equals("tok")) {
				if (unitType.equals("tuco")) {
					att.setSim(new TurnConcatSimilarityMeasure(new SentenceLexicalTokenSimilarityMeasure()));
				} else {
					att.setSim(new TurnAverageSimilarityMeasure(new SentenceLexicalTokenSimilarityMeasure()));
				}
			} else if (simType.startsWith("syn")) {
				if (unitType.equals("tuco")) {
					att.setSim(new TurnConcatSimilarityMeasure(
							new SentenceSyntacticSimilarityMeasure(TreeKernel.SYN_TREES)));
				} else {
					att.setSim(new TurnAverageSimilarityMeasure(
							new SentenceSyntacticSimilarityMeasure(TreeKernel.SYN_TREES)));
				}
			} else if (simType.equals("gries")) {
				// HACK windower doesn't control person for Gries-style similarity
				if (winType.equals("oth")) {
					if (unitType.equals("tuco")) {
						att.setSim(new TurnConcatSimilarityMeasure(new SentenceLastConstructionOtherSimilarityMeasure(
								".*:VP\\(ditr.*", ".*:VP\\(montr.*:PP\\((to|for)\\b.*")));
					} else {
						att.setSim(new TurnAverageSimilarityMeasure(new SentenceLastConstructionOtherSimilarityMeasure(
								".*:VP\\(ditr.*", ".*:VP\\(montr.*:PP\\((to|for)\\b.*")));
					}
				} else if (winType.equals("sam")) {
					if (unitType.equals("tuco")) {
						att.setSim(new TurnConcatSimilarityMeasure(new SentenceLastConstructionSameSimilarityMeasure(
								".*:VP\\(ditr.*", ".*:VP\\(montr.*:PP\\((to|for)\\b.*")));
					} else {
						att.setSim(new TurnAverageSimilarityMeasure(new SentenceLastConstructionSameSimilarityMeasure(
								".*:VP\\(ditr.*", ".*:VP\\(montr.*:PP\\((to|for)\\b.*")));
					}
				} else if (winType.equals("any")) {
					if (unitType.equals("tuco")) {
						att.setSim(new TurnConcatSimilarityMeasure(new SentenceLastConstructionSimilarityMeasure(
								".*:VP\\(ditr.*", ".*:VP\\(montr.*:PP\\((to|for)\\b.*")));
					} else {
						att.setSim(new TurnAverageSimilarityMeasure(new SentenceLastConstructionSimilarityMeasure(
								".*:VP\\(ditr.*", ".*:VP\\(montr.*:PP\\((to|for)\\b.*")));
					}
				}
			} else {
				throw new RuntimeException("unknown sim type " + simType);
			}
			at = att;
		} else if (unitType.equals("sent")) {
			AlignmentTester<DialogueSentence> ats = (xlsOutput ? new AlignmentTester<DialogueSentence>(xlsFile)
					: new AlignmentTester<DialogueSentence>());
			if (winType.equals("oth")) {
				ats.setWin(new OtherSpeakerSentenceWindower(null, leftWindow, rightWindow, stepWindow));
			} else if (winType.equals("sam")) {
				ats.setWin(new SameSpeakerSentenceWindower(null, leftWindow, rightWindow, stepWindow));
			} else if (winType.equals("alloth")) {
				ats.setWin(new OtherSpeakerAllOtherSentenceWindower(null, leftWindow, rightWindow, stepWindow));
			} else if (winType.equals("allsam")) {
				ats.setWin(new SameSpeakerAllOtherSentenceWindower(null, leftWindow, rightWindow, stepWindow));
			} else if (winType.equals("any")) {
				ats.setWin(new SentenceWindower(null, leftWindow, rightWindow, stepWindow));
			} else {
				throw new RuntimeException("unknown win type " + winType);
			}
			if (simType.equals("lex")) {
				ats.setSim(new SentenceLexicalSimilarityMeasure());
			} else if (simType.startsWith("tok")) {
				ats.setSim(new SentenceLexicalTokenSimilarityMeasure());
			} else if (simType.startsWith("syn")) {
				ats.setSim(new SentenceSyntacticSimilarityMeasure(TreeKernel.SYN_TREES));
			} else if (simType.equals("gries")) {
				// HACK windower doesn't control person for Gries-style similarity
				if (winType.equals("oth")) {
					ats.setSim(new SentenceLastConstructionOtherSimilarityMeasure(".*:VP\\(ditr.*",
							".*:VP\\(montr.*:PP\\((to|for)\\b.*"));
				} else if (winType.equals("sam")) {
					ats.setSim(new SentenceLastConstructionSameSimilarityMeasure(".*:VP\\(ditr.*",
							".*:VP\\(montr.*:PP\\((to|for)\\b.*"));
				} else if (winType.equals("any")) {
					ats.setSim(new SentenceLastConstructionSimilarityMeasure(".*:VP\\(ditr.*",
							".*:VP\\(montr.*:PP\\((to|for)\\b.*"));
				}
			} else {
				throw new RuntimeException("unknown sim type " + simType);
			}
			at = ats;
		} else {
			throw new RuntimeException("unknown unit type " + unitType);
		}

		// // use something like this to test on the original corpus (limiting to 2-speaker cases)
		// at.setCorpus(new DCPSECorpus(2, 2, 0, 0));
		// // use something like this if you're not running on the server
		// at.setCorpus(new DCPSECorpus("C:/Documents and Settings/mpurver/My Documents/corpora", 2, 2, 0, 0));
		// // use something like this to test on a randomised version
		// at.setCorpus(new RandomCorpus(new DCPSECorpus(2, 2, 0, 0), RandomCorpus.RAND_ALL_TURNS, RandomCorpus.PAD_CUT,
		// RandomCorpus.LENGTH_IN_TURNS));
		// // use something like this to test on a randomised version and save it to file for later replication
		// DialogueCorpus corpus = new RandomCorpus(new DCPSECorpus(2, 2, 0, 0), RandomCorpus.RAND_OTHER_SPEAKERS,
		// RandomCorpus.PAD_CUT);
		// corpus.writeToFile(new File(corpusName + ".corpus.gz"));
		// at.setCorpus(corpus);
		// // use something like this to use a previously generated random corpus
		// at.setCorpus(DialogueCorpus.readFromFile(new File(corpusName + ".corpus.gz")));
		// // use something like this to (re-)parse a corpus
		// CorpusParser.parse(corpus);

		DialogueCorpus corpus = getCorpus(baseDir, corpusRoot, randSuffix, randType);
		if (randType.equals("random1") || randType.equals("random3") || randType.equals("random_same")) {
			// for random1, must actually set up two corpora, one for each speaker A and B
			ArrayList<DialogueCorpus> corpusPair = new ArrayList<DialogueCorpus>();
			corpusPair.add(corpus);
			corpusPair
					.add(getCorpus(baseDir, corpusRoot, randSuffix.replace(randType, randType + "B"), randType + "B"));
			corpus = new CombinedCorpus(corpusPair);
		}
		at.setCorpus(corpus);

		TreeKernel.clearAllowedProductions();
		TreeKernel.clearBannedProductions();
		if (simType.equals("syntop")) {
			for (String bnf : corpus.topTenSynProductions()) {
				TreeKernel.addAllowedProduction(bnf);
			}
		} else if (simType.equals("synbot")) {
			for (String bnf : corpus.topTenSynProductions()) {
				TreeKernel.addBannedProduction(bnf);
			}
		}

		at.normalisation = NORM_NONE;
		// at.smoother = SmoothingFactory.getSmoother("gaussian(5)");
		int num = 300;

		List<List<Double>> scores = at.processCorpus(runId);
		if (plotGraphs) {
			at.processScores(scores, num);
		}

	}

	private static DialogueCorpus getCorpus(String baseDir, String corpusRoot, String randSuffix, String randType) {
		String corpusName = corpusRoot + randSuffix + ".corpus";
		DialogueCorpus corpus = DialogueCorpus.readFromFile(new File(baseDir + corpusName));
		if (corpus == null) {
			if (!randType.isEmpty()) {
				corpus = DialogueCorpus.readFromFile(new File(corpusRoot + ".corpus"));
			}
			if (corpus == null) {
				if (corpusRoot.startsWith("dcpse")) {
					CreateTreeFromDCPSE.setOption(CreateTreeFromDCPSE.INCLUDE_NO_PAUSE, true);
					CreateTreeFromDCPSE.setOption(CreateTreeFromDCPSE.INCLUDE_NO_IGNORE, true);
					CreateTreeFromDCPSE.setOption(CreateTreeFromDCPSE.INCLUDE_NO_UNCLEAR, true);
					if (corpusRoot.endsWith("nointj")) {
						CreateTreeFromDCPSE.setOption(CreateTreeFromDCPSE.INCLUDE_NO_UMM, true);
						CreateTreeFromDCPSE.setOption(CreateTreeFromDCPSE.INCLUDE_NO_REACT, true);
					} else {
						CreateTreeFromDCPSE.setOption(CreateTreeFromDCPSE.INCLUDE_NO_UMM, false);
						CreateTreeFromDCPSE.setOption(CreateTreeFromDCPSE.INCLUDE_NO_REACT, false);
					}
					if (corpusRoot.startsWith("dcpsef")) {
						CreateTreeFromDCPSE.setOption(CreateTreeFromDCPSE.INCLUDE_NO_BRACKETS, false);
					}
					if (corpusRoot.startsWith("dcpsefp")) {
						CreateTreeFromDCPSE.setOption(CreateTreeFromDCPSE.PP_LEXICAL_FEATURES, true);
					}
					corpus = (baseDir == null ? new DCPSECorpus(2, 2, 10, 0) : new DCPSECorpus(baseDir, 2, 2, 10, 0));
				} else if (corpusRoot.startsWith("swbd")) {
					CreateTreeFromSWBD.setOption(CreateTreeFromSWBD.REPAIR_SELFREPAIRS, true);
					CreateTreeFromSWBD.setOption(CreateTreeFromSWBD.SIMPLIFY_CATEGORIES, true);
					if (corpusRoot.endsWith("nointj")) {
						CreateTreeFromSWBD.setOption(CreateTreeFromSWBD.INCLUDE_NO_INTJ, true);
					} else {
						CreateTreeFromSWBD.setOption(CreateTreeFromSWBD.INCLUDE_NO_INTJ, false);
					}
					corpus = (baseDir == null ? new SwitchboardCorpus(2, 2, 10, 0)
							: new SwitchboardCorpus(baseDir, 2, 2, 10, 0));
				} else if (corpusRoot.startsWith("bnc")) {
					if (corpusRoot.endsWith("nointj")) {
						throw new RuntimeException("not implemented yet");
					}
					// can't impose genre count as no separate metadata file - but BNC has loads in each genre anyway
					corpus = (baseDir == null ? new BNCCorpus(2, 2, 0, 0) : new BNCCorpus(baseDir, 2, 2, 0, 0));
					if (corpusRoot.endsWith("ccg") || corpusRoot.endsWith("stanford") || corpusRoot.endsWith("rasp")) {
						throw new RuntimeException(
								"parsed corpus doesn't exist - better to use BNCCorpus and/or CorpusParser to create, you get more options that way!");
					}
				}
			}
			if (!randType.isEmpty()) {
				// for "nointj" versions, keep the same randomisation assignment as the non-"nointj" if possible
				// this isn't possible with trimmed corpora, as parts of the original assignment may have been trimmed
				if (corpusRoot.contains("nointj") && !corpusRoot.contains("trim")) {
					RandomCorpus.setCorpusToCopy(DialogueCorpus
							.readFromFile(new File(corpusRoot.replaceAll("_nointj", "") + randSuffix + ".corpus.gz")));
					corpus = new RandomCorpus(corpus, RandomCorpus.RAND_COPY_CORPUS, RandomCorpus.PAD_CUT,
							RandomCorpus.LENGTH_IN_TURNS, true, true);
				} else {
					int spkOffset = 0;
					if (randType.endsWith("B")) {
						spkOffset = 1;
						randType = randType.substring(0, randType.length() - 1);
					}
					if (randType.equals("random1")) {
						corpus = new RandomCorpus(corpus, RandomCorpus.RAND_OTHER_TURNS, RandomCorpus.PAD_CUT,
								RandomCorpus.LENGTH_IN_TURNS, true, true, spkOffset);
					} else if (randType.equals("random2")) {
						corpus = new RandomCorpus(corpus, RandomCorpus.RAND_BEST_LENGTH_MATCH, RandomCorpus.PAD_CUT,
								RandomCorpus.LENGTH_IN_TURNS, true, true, spkOffset);
					} else if (randType.equals("random3")) {
						corpus = new RandomCorpus(corpus, RandomCorpus.RAND_BEST_LENGTH_RAND, RandomCorpus.PAD_CUT,
								RandomCorpus.LENGTH_IN_TURNS, true, true, spkOffset);
					} else if (randType.equals("random4")) {
						corpus = new RandomCorpus(corpus, RandomCorpus.RAND_ALL_TURNS, RandomCorpus.PAD_CUT,
								RandomCorpus.LENGTH_IN_TURNS, true, true, spkOffset);
					} else if (randType.equals("random5")) {
						corpus = new RandomCorpus(corpus, RandomCorpus.RAND_ALL_SENTS, RandomCorpus.PAD_CUT,
								RandomCorpus.LENGTH_IN_SENTS, true, true, spkOffset);
					} else if (randType.equals("random_same")) {
						corpus = new RandomCorpus(corpus, RandomCorpus.RAND_SAME_SPEAKER, RandomCorpus.PAD_CUT,
								RandomCorpus.LENGTH_IN_TURNS, true, false, spkOffset);
					} else if (randType.equals("random_s2me")) {
						corpus = new RandomCorpus(corpus, RandomCorpus.RAND_S2ME_SPEAKER, RandomCorpus.PAD_CUT,
								RandomCorpus.LENGTH_IN_TURNS, true, false, spkOffset);
					}
				}
			}
			corpus.writeToFile(new File(corpusName));
		}
		return corpus;
	}

	/**
	 * Run a test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		String[] base = { // "/import/imc-corpora/corpora/dcpse",
				"/Users/mpurver/SkyDrive/QMUL/imc-corpora/data/align",
				// "/import/imc-corpora/kcl/ldc/treebank_3",
				"/Users/mpurver/SkyDrive/QMUL/imc-corpora/data/align",
				// "/import/imc-corpora/corpora/bnc/bnc-xml",
				"/Users/mpurver/SkyDrive/QMUL/imc-corpora/data/align",
				// "/import/imc-corpora/corpora/bnc/bnc-xml"
				// "C:/Documents and Settings/mpurver/My Documents/corpora/bnc"
		};
		// String[] corpus = { "dcpse", "swbd", "bnc_trim", "dcpse_nointj", "swbd_nointj", "bnc_nointj_trim" };
		// String[] corpus = { /* "dcpse", */"swbd", /*
		// * "bnc_trim_stanford", "bnc_trim_ccg", /* "dcpse_nointj",
		// * "swbd_nointj", "bnc_nointj_trim_stanford", "bnc_nointj_trim_ccg"
		// */};
		// String[] corpus = { "dcpsefp" };
		String[] corpus = { "dcpse", "swbd", "bnc_trim_ccg" };
		String[] rand = {
				/* "", */"random1" /*
									 * , "random2", "random3", "random4", "random5", "random_same", "random_s2me"
									 */ };
		String[] sim = { "lex", /* "tok", */"syn", "syntop", "synbot" /* , "gries" */ };
		String[] unit = { "turn", /* "tuco", "sent" */ };
		String[] win = { "oth", "sam" /* , "any" */ };
		int monteCarlo = 0; // number of repetitions for MC

		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-C")) {
				corpus = args[i].replaceFirst("-C", "").split(",");
				System.out.println("Got corpus array from clargs: " + Arrays.toString(corpus));
			} else if (args[i].startsWith("-R")) {
				rand = args[i].replaceFirst("-R", "").split(",");
				System.out.println("Got rand array from clargs: " + Arrays.toString(rand));
			} else if (args[i].startsWith("-S")) {
				sim = args[i].replaceFirst("-S", "").split(",");
				System.out.println("Got sim array from clargs: " + Arrays.toString(sim));
			} else if (args[i].startsWith("-U")) {
				unit = args[i].replaceFirst("-U", "").split(",");
				System.out.println("Got unit array from clargs: " + Arrays.toString(unit));
			} else if (args[i].startsWith("-W")) {
				win = args[i].replaceFirst("-W", "").split(",");
				System.out.println("Got win array from clargs: " + Arrays.toString(win));
			} else if (args[i].startsWith("-M")) {
				monteCarlo = Integer.parseInt(args[i].replaceFirst("-M", ""));
				System.out.println("Got monte-carlo rounds: " + monteCarlo);
			}
		}

		for (int i = 0; i < corpus.length; i++) {
			for (int j = 0; j < rand.length; j++) {
				for (int k = 0; k < sim.length; k++) {
					for (int l = 0; l < unit.length; l++) {
						for (int m = 0; m < win.length; m++) {
							// no point doing random on same-person case
							if (rand[j].isEmpty() || (!rand[j].contains("s2me") && !win[m].equals("sam"))
									|| (rand[j].contains("s2me") && win[m].equals("sam"))) {
								if ((monteCarlo < 1) || rand[j].isEmpty()) {
									runTest(base[i % base.length], corpus[i], rand[j], sim[k], unit[l], win[m], -1,
											true, false);
								} else {
									for (int mc = 0; mc < monteCarlo; mc++) {
										runTest(base[i % base.length], corpus[i], rand[j], sim[k], unit[l], win[m], mc,
												true, false);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
