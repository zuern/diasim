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

public class DialogueSpeaker implements Serializable, Comparable<DialogueSpeaker> {

	private static final long serialVersionUID = 3417987502842447240L;

	private String id;
	private String firstName;
	private String lastName;
	private String gender;
	private String age;
	private String occupation;

	public DialogueSpeaker(String id, String firstName, String lastName, String gender, String age, String occupation) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
		this.age = age;
		this.occupation = occupation;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * @return the age
	 */
	public String getAge() {
		return age;
	}

	/**
	 * @return the occupation
	 */
	public String getOccupation() {
		return occupation;
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
	 * @param firstName
	 *            the firstName to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @param lastName
	 *            the lastName to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @param gender
	 *            the gender to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * @param age
	 *            the age to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setAge(String age) {
		this.age = age;
	}

	/**
	 * @param occupation
	 *            the occupation to set
	 * @deprecated just for serialization
	 */
	@Deprecated
	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	/**
	 * @param s
	 * @return whether this speaker is probably the same actual person as s
	 */
	public boolean probablySameAs(DialogueSpeaker s) {
		// don't match BNC "unknown" speakers/groups
		if (BNCCorpus.isUnknown(s)) {
			return false;
		}
		// at least one name must be specified (just matching age/gender/occ is too loose)
		if ((firstName == null || firstName.equals("")) && ((lastName == null) || lastName.equals(""))) {
			return false;
		}
		// name must match exactly (including matching empty parts)
		if (!matchField(firstName, s.firstName) || !matchField(lastName, s.lastName)) {
			return false;
		}
		// age and gender must match exactly
		if (!matchField(age, s.age) || !matchField(gender, s.gender)) {
			return false;
		}
		// occupation need not match, e.g. Laura Tollfree is variously "Student", "Student (PhD)",
		// "PhD student (Linguistics), Cambridge"
		// System.out.println(this + " probably same as " + s);
		return true;
	}

	/**
	 * @param f1
	 * @param f2
	 * @return true if both are null, or if they are equal modulo case
	 */
	private boolean matchField(String f1, String f2) {
		if (f1 == null) {
			return (f2 == null);
		} else if (f2 == null) {
			return (f1 == null);
		} else {
			return f1.equalsIgnoreCase(f2);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return id + "=" + firstName + " " + lastName + ", age " + age + ", gender " + gender + ", occ " + occupation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DialogueSpeaker o) {
		return getId().compareTo(o.getId());
	}
}
