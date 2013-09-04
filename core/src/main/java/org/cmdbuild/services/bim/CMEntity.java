package org.cmdbuild.services.bim;

import java.util.List;

import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;

public class CMEntity implements Entity {

	private final CMCard card;

	// private final String containerName = "Stanza";
	// private String containerGuid = "";

	public CMEntity(final CMCard card) {
		this.card = card;
	}

	public CMCard getCard() {
		return card;
	}

	public Long getId() {
		return card.getId();
	}

	@Override
	public boolean isValid() {
		return !getKey().equals("?");
	}

	@Override
	public List<Attribute> getAttributes() {
		return null;
	}

	@Override
	public Attribute getAttributeByName(String attributeName) {

		return null;
	}

	@Override
	public String getKey() {
		String guid = "?";
		return guid;
	}

	@Override
	public String getTypeName() {
		return null;
	}

	@Override
	public String getContainerKey() {
		// TODO Auto-generated method stub
		return null;
	}

}
