package org.cmdbuild.services.bim.connector.export;

import static org.cmdbuild.bim.utils.BimConstants.FK_COLUMN_NAME;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;

import java.util.List;
import java.util.Map;

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
import org.cmdbuild.utils.bim.BimIdentifier;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public class DefaultExport implements Export {

	private final BimServiceFacade serviceFacade;
	private final BimDataPersistence persistence;
	private BimDataView bimDataView;

	public DefaultExport(BimDataView dataView, BimServiceFacade bimServiceFacade, BimDataPersistence bimPersistence) {
		this.serviceFacade = bimServiceFacade;
		this.persistence = bimPersistence;
		this.bimDataView = dataView;
	}

	// TODO what about concurrent calls of this method?
	@Override
	public String export(Catalog catalog, String sourceProjectId) {

		// TODO move this to another thread launched after check-in
		System.out.println("--- Start export at " + new DateTime());
		Map<String, String> shape_name_oid_map = Maps.newHashMap();
		BimProject targetProject = serviceFacade.fetchProjectForExport(sourceProjectId);
		if (!targetProject.isValid()) {
			throw new BimError("No project for export found");
		}
		System.out.println("--- Project for export is ready at " + new DateTime());
		System.out.println("--- Revision for export is " + targetProject.getLastRevisionId());
		
		Map<String, Long> globalid_cmdbId_map = serviceFacade.fetchAllGlobalIdForIfcType("IfcSpace",
				targetProject.getIdentifier());
		String containerClassName = persistence.getContainerClassName();

		fillGlobalidIdMap(globalid_cmdbId_map, containerClassName);

		for (String containerKey : globalid_cmdbId_map.keySet()) {
			System.out.println("--- Export for room " + containerKey + " at " + new DateTime());
			Long containerId = globalid_cmdbId_map.get(containerKey);

			if (containerId == null) {
				System.out.println("Container card with key '" + containerKey
						+ "' not found in CMDB. Skip this container.");
				continue;
			}
			System.out.println("IfcSpace has key '" + containerKey + "' and id '" + containerId + "'");
			for (EntityDefinition catalogEntry : catalog.getEntitiesDefinitions()) {
				String className = catalogEntry.getLabel();
				String containerAttributeName = catalogEntry.getContainerAttribute();
				if (className.isEmpty() || containerAttributeName.isEmpty()) {
					continue;
				}
				List<CMCard> allCardsOfClassInTheRoom = fetchCardsOfClassInContainer(className, containerId,
						containerAttributeName);
				if (allCardsOfClassInTheRoom.isEmpty()) {
					continue;
				}
				String shapeName = catalogEntry.getShape();
				System.out.println("Export class with shape '" + shapeName + "'");
				String shapeOid = "-1";
				if (shape_name_oid_map.containsKey(shapeName)) {
					shapeOid = shape_name_oid_map.get(shapeName);
				} else {
					shapeOid = serviceFacade.findShapeWithName(shapeName, targetProject.getIdentifier());
					shape_name_oid_map.put(shapeName, shapeOid);
				}
				if (shapeOid.equals("-1")) {
					System.out.println("shape with name '" + shapeName + "' not found");
					return "-1";
				}
				for (CMCard cmcard : allCardsOfClassInTheRoom) {
					System.out.println("Export card " + cmcard.getId());
					Map<String, String> bimData = bimDataView.getBimDataFromCard(cmcard, className,
							String.valueOf(containerId), containerClassName);
					
					Entity entity = serviceFacade.fetchEntityFromGlobalId(targetProject.getLastRevisionId(), bimData.get(GLOBALID_ATTRIBUTE));
					if(entity.isValid()){
						System.out.println("entity is already present");
						System.out.println("skip....");
						continue;
					}
					serviceFacade.insertCard(bimData, targetProject.getIdentifier(), catalogEntry.getTypeName(),
							containerKey, shapeOid);
				}
			}
		}
		System.out.println("--- Start commit transaction");
		String revisionId = serviceFacade.commitTransaction();
		System.out.println("--- End commit transaction");
		System.out.println("[INFO] revision '" + revisionId + "' created");
		return targetProject.getIdentifier();
	}

	private void fillGlobalidIdMap(Map<String, Long> globalid_cmdbId_map, String className) {
		for (String globalId : globalid_cmdbId_map.keySet()) {
			Long matchingId = getIdFromGlobalId(globalId, className);
			globalid_cmdbId_map.put(globalId, matchingId);
		}
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

	private List<CMCard> fetchCardsOfClassInContainer(String className, long containerId, String containerAttribute) {
		List<CMCard> cardList = bimDataView.getCardsWithAttributeAndValue(DBIdentifier.fromName(className), new Long(
				containerId), containerAttribute);
		return cardList;
	}

}
