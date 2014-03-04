package org.cmdbuild.services.bim.connector.export;

import static org.cmdbuild.bim.utils.BimConstants.FK_COLUMN_NAME;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.OBJECT_OID;
import static org.cmdbuild.services.bim.connector.DefaultBimDataView.CONTAINER_GUID;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.bim.mapper.DefaultAttribute;
import org.cmdbuild.bim.mapper.DefaultEntity;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.bimserver.BimserverEntity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.DBIdentifier;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.connector.ExportDifferListener;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

public class NewExport implements Export {

	private final BimFacade serviceFacade;
	private final BimPersistence persistence;
	private BimDataView bimDataView;
	private ExportDifferListener listener;
	private Map<String, String> shapeNameToOidMap;

	public NewExport(BimDataView dataView, BimFacade bimServiceFacade, BimPersistence bimPersistence) {
		this.serviceFacade = bimServiceFacade;
		this.persistence = bimPersistence;
		this.bimDataView = dataView;
		shapeNameToOidMap = Maps.newHashMap();

		this.listener = new ExportDifferListener() {

			Map<String, Map<String, List<String>>> relationsMap = Maps.newHashMap();

			@Override
			public void createTarget(Entity cardData, String targetProjectId, String className, String containerKey,
					String shapeOid, String sourceRevisionId) {
				serviceFacade.createCard(cardData, targetProjectId, className, containerKey, shapeOid);
			}

			@Override
			public void deleteTarget(Entity cardData, String targetProjectId, String containerKey) {
				serviceFacade.removeCard(cardData, targetProjectId, containerKey);
			}

			@Override
			public void createTarget(Entity entityToCreate, String targetProjectId) {
				final String objectOid = serviceFacade.createCard(entityToCreate, targetProjectId);
				final String spaceGuid = entityToCreate.getAttributeByName(CONTAINER_GUID).getValue();
				toAdd(objectOid, spaceGuid);
			}

			@Override
			public void deleteTarget(Entity entityToRemove, String targetProjectId) {
				final String removedObjectOid = serviceFacade.removeCard(entityToRemove, targetProjectId);
				final String oldContainerOid = entityToRemove.getAttributeByName(CONTAINER_GUID).getValue();
				if (!oldContainerOid.isEmpty()) {
					toRemove(removedObjectOid, oldContainerOid);
				}
			}

			private void toAdd(String objectOid, String spaceGuid) {
				if (relationsMap.containsKey(spaceGuid)) {
					Map<String, List<String>> spaceMap = relationsMap.get(spaceGuid);
					if (spaceMap.containsKey("A")) {
						spaceMap.get("A").add(objectOid);
					} else {
						List<String> listToAdd = Lists.newArrayList(objectOid);
						spaceMap.put("A", listToAdd);
					}
				} else {
					Map<String, List<String>> spaceMap = Maps.newHashMap();
					List<String> listToAdd = Lists.newArrayList(objectOid);
					spaceMap.put("A", listToAdd);
					relationsMap.put(spaceGuid, spaceMap);
				}
			}

			private void toRemove(String objectOid, String spaceGuid) {
				if (relationsMap.containsKey(spaceGuid)) {
					Map<String, List<String>> spaceMap = relationsMap.get(spaceGuid);
					if (spaceMap.containsKey("D")) {
						spaceMap.get("D").add(objectOid);
					} else {
						List<String> listToAdd = Lists.newArrayList(objectOid);
						spaceMap.put("D", listToAdd);
					}
				} else {
					Map<String, List<String>> spaceMap = Maps.newHashMap();
					List<String> listToAdd = Lists.newArrayList(objectOid);
					spaceMap.put("D", listToAdd);
					relationsMap.put(spaceGuid, spaceMap);
				}
			}

			@Override
			public void updateRelations(String targetProjectId) {
				serviceFacade.updateRelations(relationsMap, targetProjectId);
				relationsMap = Maps.newHashMap();
			}
		};
	}

	// TODO what about concurrent calls of this method?
	@Override
	public String export(final Catalog catalog, final String sourceProjectId) {

		System.out.println("--- Start export at " + new DateTime());

		final String containerClassName = persistence.getContainerClassName();
		final String targetProjectId = persistence.read(sourceProjectId).getExportProjectId();
		final BimProject targetProject = serviceFacade.getProjectById(targetProjectId);
		if (!targetProject.isValid()) {
			throw new BimError("No project for export found");
		}
		final String sourceRevisionId = serviceFacade.getProjectById(targetProjectId).getLastRevisionId();
		System.out.println("Revision for export is " + sourceRevisionId);

		final Iterable<String> ifcSpacesGlobalIdList = serviceFacade.fetchAllGlobalIdForIfcType("IfcSpace",
				targetProjectId);
		Map<String, Long> globalIdToCmdbIdMap = Maps.newHashMap();
		for (String globalId : ifcSpacesGlobalIdList) {
			final Long matchingId = getIdFromGlobalId(globalId, containerClassName);
			globalIdToCmdbIdMap.put(globalId, matchingId);
		}

		final Map<String, Entity> dataSource = getSourceData(globalIdToCmdbIdMap, catalog, sourceRevisionId,
				containerClassName);
		final Map<String, Entity> dataTarget = getTargetData(sourceRevisionId, dataSource.keySet());
		final MapDifference<String, Entity> difference = Maps.difference(dataSource, dataTarget);

		final Map<String, Entity> entriesToCreate = difference.entriesOnlyOnLeft();
		final Map<String, ValueDifference<Entity>> entriesToUpdate = difference.entriesDiffering();
		final Map<String, Entity> entriesToRemove = difference.entriesOnlyOnRight();

		for (final String guidToCreate : entriesToCreate.keySet()) {
			final Entity entityToCreate = entriesToCreate.get(guidToCreate);
			listener.createTarget(entityToCreate, targetProjectId);
		}
		for (final String guidToUpdate : entriesToUpdate.keySet()) {
			final ValueDifference<Entity> entityToUpdate = entriesToUpdate.get(guidToUpdate);
			final Entity entityToRemove = entityToUpdate.rightValue();
			final Entity entityToCreate = entityToUpdate.leftValue();
			listener.createTarget(entityToCreate, targetProjectId);
			listener.deleteTarget(entityToRemove, targetProjectId);
		}
		for (final String guidToRemove : entriesToRemove.keySet()) {
			final Entity entityToRemove = entriesToRemove.get(guidToRemove);
			listener.deleteTarget(entityToRemove, targetProjectId);
		}
		listener.updateRelations(targetProjectId);

		final String revisionId = serviceFacade.commitTransaction();
		if (revisionId.isEmpty()) {
			System.out.println("Nothing to export.");
		} else {
			System.out.println("Revision " + revisionId + " created at " + new DateTime());
		}
		return targetProjectId;
	}

	private Map<String, Entity> getTargetData(String sourceRevisionId, Set<String> keySet) {
		Map<String, Entity> targetData = Maps.newHashMap();
		for (final String globalId : keySet) {
			final Entity entity = serviceFacade.fetchEntityFromGlobalId(sourceRevisionId, globalId);
			if (!entity.isValid()) {
				continue;
			}
			Long oid = BimserverEntity.class.cast(entity).getOid();
			final DefaultEntity targetEntity = DefaultEntity.withTypeAndKey(entity.getTypeName(), globalId);
			targetEntity.addAttribute(DefaultAttribute.withNameAndValue(OBJECT_OID, oid.toString()));
			final String containerGlobalId = serviceFacade.getContainerOfEntity(globalId, sourceRevisionId);
			targetEntity.addAttribute(DefaultAttribute.withNameAndValue(CONTAINER_GUID, containerGlobalId));
			if (entity.isValid()) {
				targetData.put(globalId, targetEntity);
			}
		}
		return targetData;
	}

	private Map<String, Entity> getSourceData(final Map<String, Long> globalIdToCmdbIdMap, final Catalog catalog,
			final String revisionId, String containerClassName) {

		Map<String, Entity> dataSource = Maps.newHashMap();
		for (final String ifcSpaceGuid : globalIdToCmdbIdMap.keySet()) {
			for (EntityDefinition catalogEntry : catalog.getEntitiesDefinitions()) {
				final Long ifcSpaceCmId = globalIdToCmdbIdMap.get(ifcSpaceGuid);
				final String className = catalogEntry.getLabel();
				final String containerAttributeName = catalogEntry.getContainerAttribute();
				final String shapeOid = getShapeOid(revisionId, catalogEntry.getShape());
				final String ifcType = catalogEntry.getTypeName();

				List<CMCard> cardsInTheIfcSpace = bimDataView.getCardsWithAttributeAndValue(
						DBIdentifier.fromName(className), ifcSpaceCmId, containerAttributeName);
				for (CMCard cmcard : cardsInTheIfcSpace) {
					Entity sourceData = bimDataView.getCardDataForExport(cmcard, className,
							String.valueOf(ifcSpaceCmId), ifcSpaceGuid, containerClassName, shapeOid, ifcType);
					dataSource.put(sourceData.getKey(), sourceData);
				}
			}
		}
		return dataSource;
	}

	private String getShapeOid(final String revisionId, final String shapeName) {
		String shapeOid = StringUtils.EMPTY;
		if (shapeNameToOidMap.containsKey(shapeName)) {
			shapeOid = shapeNameToOidMap.get(shapeName);
		} else {
			shapeOid = serviceFacade.findShapeWithName(shapeName, revisionId);
			shapeNameToOidMap.put(shapeName, shapeOid);
		}
		return shapeOid;
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
