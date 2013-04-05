package org.cmdbuild.logic.data.access;

import java.util.Map;
import java.util.Map.Entry;

public class RelationDTO {

	public String domainName;
	public String master;
	public Map<Long, String> srcCardIdToClassName;
	public Map<Long, String> dstCardIdToClassName;
	public Map<String, Object> relationAttributeToValue;
	public Long relationId;

	public RelationDTO() {
	}

	public void addSourceCardToClass(final Long srcCardId, final String srcClassName) {
		srcCardIdToClassName.put(srcCardId, srcClassName);
	}

	public void addDestinationCardToClass(final Long dstCardId, final String dstClassName) {
		dstCardIdToClassName.put(dstCardId, dstClassName);
	}

	public Entry<Long, String> getUniqueEntryForSourceCard() {
		for (final Entry<Long, String> entry : srcCardIdToClassName.entrySet()) {
			return entry;
		}
		return null;
	}

	public Entry<Long, String> getUniqueEntryForDestinationCard() {
		for (final Entry<Long, String> entry : dstCardIdToClassName.entrySet()) {
			return entry;
		}
		return null;
	}

}
