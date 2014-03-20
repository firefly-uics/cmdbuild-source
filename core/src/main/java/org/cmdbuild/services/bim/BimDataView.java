package org.cmdbuild.services.bim;

import java.util.List;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.services.bim.DefaultBimDataView.BimCard;

public interface BimDataView {
	
	Iterable<? extends CMClass> findClasses();
	
	CMCard fetchCard(String className, Long id);

	List<CMCard> getCardsWithAttributeAndValue(CMIdentifier classIdentifier, Object attributeValue, String attributeName);

	BimCard getBimDataFromGlobalid(String globalId);

	CMCard getCmCardFromGlobalId(String globalId, String className);

	Long getIdFromGlobalId(String globalId, String className);

	Long getRootId(Long cardId, String className, String referenceRootName);

	List<BimCard> getBimCardsWithGivenValueOfRootReferenceAttribute(String className, Long rootCardId,
			String rootReferenceName);

	BimCard getBimCardFromRootId(String className, Long rootCardId);

	Entity getCardDataForExport(Long id, String className, String containerAttributeName, String containerClassName,
			String shapeOid, String ifcType);

	Long getProjectCardIdFromRootCard(Long rootId, String rootClassName);

	Long getRootCardIdFromProjectId(String projectId, String rootClassName);


}
