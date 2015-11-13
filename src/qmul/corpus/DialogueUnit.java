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

import java.io.Serializable;
import java.util.ArrayList;

public class DialogueUnit implements Serializable {

	private static final long serialVersionUID = 7890149890732764005L;

	private String id;
	private String originalId;
	private int num;
	private DialogueSpeaker speaker;
	private DialogueSpeaker originalSpeaker;
	private Dialogue dialogue;
	private Dialogue originalDialogue;
	private ArrayList<String> daTags = new ArrayList<String>();
	private float startTime = Float.NaN;
	private float endTime = Float.NaN;

	/**
	 * @deprecated just for serialization, use full constructor instead
	 */
	@Deprecated
	protected DialogueUnit() {
		super();
	}

	public DialogueUnit(String id, int num, Dialogue dialogue, DialogueSpeaker speaker) {
		this.id = id;
		this.num = num;
		this.dialogue = dialogue;
		this.speaker = speaker;
	}

	public String getId() {
		return id;
	}

	public int getNum() {
		return num;
	}

	public DialogueSpeaker getSpeaker() {
		return speaker;
	}

	public Dialogue getDialogue() {
		return dialogue;
	}

	/**
	 * @return the daTags
	 */
	public ArrayList<String> getDaTags() {
		return daTags;
	}

	/**
	 * @return the originalSpeaker
	 */
	public DialogueSpeaker getOriginalSpeaker() {
		return originalSpeaker;
	}

	/**
	 * @param originalSpeaker
	 *            the originalSpeaker to set
	 */
	public void setOriginalSpeaker(DialogueSpeaker originalSpeaker) {
		this.originalSpeaker = originalSpeaker;
	}

	/**
	 * @return the originalDialogue
	 */
	public Dialogue getOriginalDialogue() {
		return originalDialogue;
	}

	/**
	 * @param originalDialogue
	 *            the originalDialogue to set
	 */
	public void setOriginalDialogue(Dialogue originalDialogue) {
		this.originalDialogue = originalDialogue;
	}

	/**
	 * @return the originalId
	 */
	public String getOriginalId() {
		return originalId;
	}

	/**
	 * @param originalId
	 *            the originalId to set
	 */
	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}

	/**
	 * @param id
	 *            the id to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param num
	 *            the num to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setNum(int num) {
		this.num = num;
	}

	/**
	 * @param speaker
	 *            the speaker to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setSpeaker(DialogueSpeaker speaker) {
		this.speaker = speaker;
	}

	/**
	 * @param dialogue
	 *            the dialogue to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setDialogue(Dialogue dialogue) {
		this.dialogue = dialogue;
	}

	/**
	 * @param daTags
	 *            the daTags to set
	 * @deprecated just for serialization, use getDaTags().add,remove etc instead
	 */
	@Deprecated
	public void setDaTags(ArrayList<String> daTags) {
		this.daTags = new ArrayList<String>(daTags);
	}

	/**
	 * @return the startTime
	 */
	public float getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(float startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public float getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 */
	public void setEndTime(float endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return the number of words based on the standard transcription (defaults to 0 if this doesn't make sense)
	 */
	public int numWords() {
		return 0;
	}

	/**
	 * @return the number of words based on the tokenised transcription (defaults to 0 if this doesn't make sense)
	 */
	public int numTokens() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getId() + (Float.isNaN(getStartTime()) ? "" : (" " + getStartTime()))
				+ (Float.isNaN(getEndTime()) ? "" : ("-" + getEndTime()));
	}
}
