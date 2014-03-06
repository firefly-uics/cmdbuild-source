package org.cmdbuild.services.bim;

import java.util.List;
import java.util.Map;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.services.bim.connector.DefaultBimDataView.BimCard;

public interface BimDataView {

	Entity getCardDataForExport(CMCard card, String className, String containerId, String containerGlobalId,
			String containerClassName, String shapeOid, String ifcType);

	List<CMCard> getCardsWithAttributeAndValue(CMIdentifier classIdentifier, Object attributeValue, String attributeName);

	BimCard getBimDataFromGlobalid(String globalId);

	@Deprecated
	long fetchBuildingIdFromCardId(Long cardId);

	CMCard getCmCardFromGlobalId(String globalId, String className);

	Long getIdFromGlobalId(String globalId, String className);

	Map<String, BimCard> getAllGlobalIdMap();

	Long fetchRoot(Long cardId, String className, String referenceRoot);

}
