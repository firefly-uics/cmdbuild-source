package org.cmdbuild.services.bim;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.services.bim.connector.DefaultBimDataView.BimObjectCard;

public interface BimDataView {

	Map<String, String> getBimDataFromCard(CMCard card, String className, String containerId,
			String containerClassName);
	
	List<CMCard> getCardsWithAttributeAndValue(CMIdentifier classIdentifier, Object attributeValue, String attributeName);

	BimObjectCard getBimDataFromGlobalid(String globalId);
	
	@Deprecated
	long fetchBuildingIdFromCardId(Long cardId);

}
