package org.cmdbuild.services.bim.connector.export;

import static org.cmdbuild.bim.utils.BimConstants.FK_COLUMN_NAME;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.DBIdentifier;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.cmdbuild.services.bim.connector.ExportDifferListener;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public class DefaultExport implements Export {

	private final BimServiceFacade serviceFacade;
	private final BimDataPersistence persistence;
	private BimDataView bimDataView;
	private final ExportDifferListener listener;

	public DefaultExport(BimDataView dataView, BimServiceFacade bimServiceFacade, BimDataPersistence bimPersistence) {
		this.serviceFacade = bimServiceFacade;
		this.persistence = bimPersistence;
		this.bimDataView = dataView;
		this.listener = new ExportDifferListener() {

			@Override
			public void createTarget(Entity cardData, String targetProjectId, String className, String containerKey,
					String shapeOid, String sourceRevisionId) {
				serviceFacade.createCard(cardData, targetProjectId, className, containerKey, shapeOid, sourceRevisionId);
			}

			@Override
			public void deleteTarget(Entity cardData, String targetProjectId, String containerKey) {
				serviceFacade.removeCard(cardData, targetProjectId, containerKey);
			}
		};
	}

	// TODO what about concurrent calls of this method?
	@Override
	public String export(Catalog catalog, String sourceProjectId) {

		// I am assuming that the project for export has been already created
		System.out.println("--- Start export at " + new DateTime());
		Map<String, String> shapeNameToOidMap = Maps.newHashMap();
		String sourceRevisionId = serviceFacade.getProjectById(sourceProjectId).getLastRevisionId();
		
		BimProject targetProject = serviceFacade.fetchCorrespondingProjectForExport(sourceProjectId);
		if (!targetProject.isValid()) {
			throw new BimError("No project for export found");
		}
		final String targetProjectId = targetProject.getIdentifier();

		System.out.println("Revision for export is " + targetProject.getLastRevisionId());
		Iterable<String> globalIdList = serviceFacade.fetchAllGlobalIdForIfcType("IfcSpace", targetProjectId);
		Map<String, Long> globalIdToCmdbIdMap = Maps.newHashMap();
		String containerClassName = persistence.getContainerClassName();
		System.out.println("Match IfcSpaces with cards of class " + containerClassName);
		for (String globalId : globalIdList) {
			final Long matchingId = getIdFromGlobalId(globalId, containerClassName);
			globalIdToCmdbIdMap.put(globalId, matchingId);
		}

		System.out.println("Start to iterate on the IfcSpaces");
		for (String containerKey : globalIdToCmdbIdMap.keySet()) {
			Long containerId = globalIdToCmdbIdMap.get(containerKey);
			if (containerId == null) {
				System.out.println("IfcSpace with key '" + containerKey + "' not found in CMDB. Skip.");
				continue;
			}
			System.out.println("IfcSpace key: " + containerKey + " id " + containerId);
			for (EntityDefinition catalogEntry : catalog.getEntitiesDefinitions()) {
				final String className = catalogEntry.getLabel();
				final String containerAttributeName = catalogEntry.getContainerAttribute();
				if (className.isEmpty() || containerAttributeName.isEmpty()) {
					continue;
				}
				List<CMCard> cardsInTheIfcSpace = bimDataView.getCardsWithAttributeAndValue(
						DBIdentifier.fromName(className), new Long(containerId), containerAttributeName);
				if (cardsInTheIfcSpace.isEmpty()) {
					continue;
				}
				final String shapeName = catalogEntry.getShape();
				System.out.println("Export class with shape " + shapeName);
				String shapeOid = StringUtils.EMPTY;
				if (shapeNameToOidMap.containsKey(shapeName)) {
					shapeOid = shapeNameToOidMap.get(shapeName);
				} else {
					shapeOid = serviceFacade.findShapeWithName(shapeName, targetProjectId);
					shapeNameToOidMap.put(shapeName, shapeOid);
				}
				if (shapeOid.isEmpty()) {
					System.out.println("Shape " + shapeName + " not found. Skip.");
					continue;
				}
				for (CMCard cmcard : cardsInTheIfcSpace) {
					System.out.println("Perform export for card " + cmcard.getId() + " of class " + className);
					final Entity cardData = bimDataView.getCardDataForExport(cmcard, className,
							String.valueOf(containerId), containerClassName);
					final Entity entity = serviceFacade.fetchEntityFromGlobalId(targetProject.getLastRevisionId(),
							cardData.getAttributeByName(GLOBALID_ATTRIBUTE).getValue());
					if (entity.isValid()) {
						System.out.println("Entity with globalId "
								+ cardData.getAttributeByName(GLOBALID_ATTRIBUTE).getValue()
								+ " already present in project for export. \n Remove");

						listener.deleteTarget(cardData, targetProjectId, containerKey);

					}
					listener.createTarget(cardData, targetProjectId, catalogEntry.getTypeName(), containerKey, shapeOid, sourceRevisionId);
				}
			}
		}
		final String revisionId = serviceFacade.commitTransaction();
		if (revisionId.isEmpty()) {
			System.out.println("Nothing to export.");
		} else {
			System.out.println("Revision " + revisionId + " created");
		}
		return targetProjectId;
	}

	private Long getIdFromGlobalId(String key, String className) {
		CMCard theCard = getCardFromGlobalId(key, className);
		Long matchingId = null;
		if (theCard != null) {
			if (theCard.get(FK_COLUMN_NAME) != null) {
				IdAndDescription reference = (IdAndDescription) theCard.get(FK_COLUMN_NAME);
				matchingId = reference.getId();
			}
		}
		return matchingId;
	}

	private CMCard getCardFromGlobalId(String key, String className) {
		CMCard theCard = null;
		List<CMCard> cardList = bimDataView.getCardsWithAttributeAndValue(
				BimIdentifier.newIdentifier().withName(className), key, GLOBALID_ATTRIBUTE);
		if (!cardList.isEmpty() && cardList.size() == 1) {
			theCard = cardList.get(0);
		}
		return theCard;
	}

}
