package org.cmdbuild.dao.entrytype;

public interface CMClass extends CMEntryType {

	interface CMClassDefinition {
	}

	CMClass getParent();

	Iterable<? extends CMClass> getChildren();

	Iterable<? extends CMClass> getLeaves();

	boolean isAncestorOf(CMClass cmClass);

	boolean isSuperclass();

	/**
	 * The code attribute is supposed to be unique for the class and it should
	 * be human-readable. It is used to identify cards in the CSV import and
	 * wherever a numeric key is not practical.
	 * 
	 * @return the name of the attribute containing the card code
	 */
	String getCodeAttributeName();

	/**
	 * A human-readable description of the card.
	 * 
	 * @return the name of the attribute containing the card description
	 */
	String getDescriptionAttributeName();
}
