package org.cmdbuild.dao.view;

import org.cmdbuild.dao.entrytype.CMClass;

/**
 * Class definition used for creating or updating classes.
 */
public interface CMClassDefinition {

	/**
	 * Returns the class's id.
	 * 
	 * @return the class's id or {@code null} if missing.
	 */
	Long getId();

	String getName();

	String getDescription();

	/**
	 * Returns the class's parent.
	 * 
	 * @return the class's parent, {@code null} if the class have no parent.
	 */
	CMClass getParent();

	/**
	 * Tells if the class must be a superclass.
	 * 
	 * @return {@code true} if the class must be a superclass, {@code false}
	 *         otherwise.
	 */
	boolean isSuperClass();

	/**
	 * Tells if the class must hold history.
	 * 
	 * @return {@code true} if the class holds history, {@code false} otherwise.
	 */
	boolean isHoldingHistory();

	/**
	 * Tells if the class must be active or not.
	 * 
	 * @return {@code true} if the class must be a active, {@code false}
	 *         otherwise.
	 */
	boolean isActive();

}
