package org.cmdbuild.dao.entry;

public class CardReference {

	private final Long id;
	private final String description;
	
	public CardReference(final Long referencedCardId, final String referencedCardDescription) {
		this.id = referencedCardId;
		this.description = referencedCardDescription;
	}

	/**
	 * 
	 * @return the id of the referenced card
	 */
	public Long getId() {
		return id;
	}

	/**
	 * 
	 * @return the description of the referenced card
	 */
	public String getDescription() {
		return description;
	}
	
}
