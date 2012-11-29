package org.cmdbuild.dao.entrytype;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public interface CMAttribute extends Deactivable {

	/**
	 * How the attribute is showed (or not) in the form.
	 */
	enum Mode {
		WRITE, //
		READ, //
		HIDDEN, //
	}

	CMEntryType getOwner();

	CMAttributeType<?> getType();

	String getName();

	String getDescription();

	/**
	 * Attributes can be showed in the list/grid.
	 * 
	 * @return {@code true} if attribute can be showed, {@code false} otherwise.
	 */
	boolean isDisplayableInList();

	/**
	 * Attribute's values must be specified or not.
	 * 
	 * @return {@code true} if attribute's value must be specified,
	 *         {@code false} otherwise.
	 */
	boolean isMandatory();

	/**
	 * Attribute's values must be unique or not
	 * 
	 * @return {@code true} if attribute's value must be unique, {@code false}
	 *         otherwise.
	 */
	boolean isUnique();
	
	/**
	 * Returns how the field is showed (or not) in the form.
	 * 
	 * @return the {@link Mode}.
	 */
	Mode getMode();

}
