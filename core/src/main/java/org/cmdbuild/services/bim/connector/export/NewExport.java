package org.cmdbuild.services.bim.connector.export;

import static org.cmdbuild.bim.utils.BimConstants.FK_COLUMN_NAME;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_DESCRIPTION;
import static org.cmdbuild.bim.utils.BimConstants.IFC_NAME;
import static org.cmdbuild.bim.utils.BimConstants.IFC_SPACE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_TYPE;
import static org.cmdbuild.bim.utils.BimConstants.OBJECT_OID;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.services.bim.connector.DefaultBimDataView.CONTAINER_GUID;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.cmdbuild.services.bim.connector.Output;
import org.cmdbuild.services.bim.connector.export.DataChangedListener.DataChangedException;
import org.cmdbuild.services.bim.connector.export.DataChangedListener.DataNotChangedException;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.joda.time.DateTime;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

public class NewExport implements Export {

	private final BimFacade serviceFacade;
	private final BimPersistence persistence;
	private BimDataView bimDataView;
	private Map<String, String> shapeNameToOidMap;

	public NewExport(BimDataView dataView, BimFacade bimServiceFacade, BimPersistence bimPersistence) {
		this.serviceFacade = bimServiceFacade;
		this.persistence = bimPersistence;
		this.bimDataView = dataView;
		shapeNameToOidMap = Maps.newHashMap();
	}

	@Override
	public boolean isSynch(final Catalog catalog, final String projectId) {
		boolean synch = true;
		Output changeListener = new DataChangedListener();
		try {
			export(catalog, projectId, changeListener);
		} catch (DataChangedException e) {
			synch = false;
		}
		return synch;
	}

	private Map<String, Entity> getSource(final Catalog catalog, final String sourceProjectId) {
		final String containerClassName = persistence.getContainerClassName();
		if (containerClassName == null || containerClassName.isEmpty()) {
			throw new BimError("Container layer not found");
		}
		final String sourceRevisionId = getExportRevisionId(sourceProjectId);
		System.out.println("Revision for export is " + sourceRevisionId);

		final Iterable<String> ifcSpacesGlobalIdList = serviceFacade.fetchAllGlobalIdForIfcType(IFC_SPACE,
				sourceRevisionId);
		Map<String, Long> globalIdToCmdbIdIfcSpaceMap = Maps.newHashMap();
		for (String globalId : ifcSpacesGlobalIdList) {
			final Long matchingId = getIdFromGlobalId(globalId, containerClassName);
			if (matchingId.equals(-1)) {
				System.out.println(IFC_SPACE + " " + globalId + " not found in CMDBuild. Skip.");
				continue;
			}
			globalIdToCmdbIdIfcSpaceMap.put(globalId, matchingId);
		}
		final Map<String, Entity> sourceData = getSourceData(globalIdToCmdbIdIfcSpaceMap, catalog, sourceRevisionId,
				containerClassName);
		return sourceData;
	}



	@Override
	public String export(final Catalog catalog, final String sourceProjectId, Output output) {

		final Map<String, Entity> sourceData = getSource(catalog, sourceProjectId);
		final Map<String, Entity> targetData = getTargetData(sourceProjectId, sourceData.keySet());
		final MapDifference<String, Entity> difference = Maps.difference(sourceData, targetData);
		final Map<String, Entity> entriesToCreate = difference.entriesOnlyOnLeft();
		final Map<String, ValueDifference<Entity>> entriesToUpdate = difference.entriesDiffering();
		final Map<String, Entity> entriesToRemove = difference.entriesOnlyOnRight();

		final String targetId = getExportProjectId(sourceProjectId);
		serviceFacade.openTransaction(targetId);
		try {
			for (final String guidToCreate : entriesToCreate.keySet()) {
				final Entity entityToCreate = entriesToCreate.get(guidToCreate);
				output.createTarget(entityToCreate, targetId);
			}
			for (final String guidToUpdate : entriesToUpdate.keySet()) {
				final ValueDifference<Entity> entityToUpdate = entriesToUpdate.get(guidToUpdate);
				final Entity entityToRemove = entityToUpdate.rightValue();
				final Entity entityToCreate = entityToUpdate.leftValue();
				boolean toUpdate = areDifferent(entityToRemove, entityToCreate);
				if (toUpdate) {
					output.createTarget(entityToCreate, targetId);
					output.deleteTarget(entityToRemove, targetId);
				}
			}
			for (final String guidToRemove : entriesToRemove.keySet()) {
				final Entity entityToRemove = entriesToRemove.get(guidToRemove);
				output.deleteTarget(entityToRemove, targetId);
			}
			output.updateRelations(targetId);
			final String revisionId = serviceFacade.commitTransaction();
			System.out.println("Revision " + revisionId + " created at " + new DateTime());
		} catch (DataChangedException d) {
			serviceFacade.abortTransaction();
			throw new DataChangedException();
		} catch (DataNotChangedException d) {
			serviceFacade.abortTransaction();
		} catch (Throwable t) {
			serviceFacade.abortTransaction();
			throw new BimError("Error during export", t);
		}
		return targetId;
	}

	private boolean areDifferent(Entity entityToRemove, Entity entityToCreate) {
		final String oldName = entityToRemove.getAttributeByName(IFC_NAME).getValue();
		final String newName = entityToCreate.getAttributeByName(CODE_ATTRIBUTE).getValue();
		if (!StringUtils.equals(oldName, newName)) {
			return true;
		}
		final String oldDescription = entityToRemove.getAttributeByName(IFC_DESCRIPTION).getValue();
		final String newDescription = entityToCreate.getAttributeByName(DESCRIPTION_ATTRIBUTE).getValue();
		if (!StringUtils.equals(oldDescription, newDescription)) {
			return true;
		}
		final String oldType = entityToRemove.getTypeName();
		final String newType = entityToCreate.getAttributeByName(IFC_TYPE).getValue();
		if (!StringUtils.equals(oldType, newType)) {
			return true;
		}
		final String oldSpace = entityToRemove.getAttributeByName(CONTAINER_GUID).getValue();
		final String newSpace = entityToCreate.getAttributeByName(CONTAINER_GUID).getValue();
		if (!StringUtils.equals(oldSpace, newSpace)) {
			return true;
		}
		return false;
	}

	private Map<String, Entity> getTargetData(String sourceProjectId, Set<String> keySet) {
		final String sourceRevisionId = getExportRevisionId(sourceProjectId);
		Map<String, Entity> targetData = Maps.newHashMap();
		for (final String globalId : keySet) {
			final Entity entity = serviceFacade.fetchEntityFromGlobalId(sourceRevisionId, globalId);
			if (!entity.isValid()) {
				continue;
			}
			Long oid = BimserverEntity.class.cast(entity).getOid();
			final String name = entity.getAttributeByName(IFC_NAME).getValue();
			final String description = entity.getAttributeByName(IFC_DESCRIPTION).getValue();
			final DefaultEntity targetEntity = DefaultEntity.withTypeAndKey(entity.getTypeName(), globalId);
			final String containerGlobalId = serviceFacade.getContainerOfEntity(globalId, sourceRevisionId);

			targetEntity.addAttribute(DefaultAttribute.withNameAndValue(IFC_NAME, name));
			targetEntity.addAttribute(DefaultAttribute.withNameAndValue(IFC_DESCRIPTION, description));
			targetEntity.addAttribute(DefaultAttribute.withNameAndValue(OBJECT_OID, oid.toString()));
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
		for (final Entry<String, Long> entry : globalIdToCmdbIdMap.entrySet()) {
			for (EntityDefinition catalogEntry : catalog.getEntitiesDefinitions()) {
				final String ifcSpaceGuid = entry.getKey();
				final Long ifcSpaceCmId = entry.getValue();
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
		long matchingId = -1;
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
	
	private String getExportProjectId(final String masterProjectId) {
		final String targetProjectId = persistence.read(masterProjectId).getExportProjectId();
		final BimProject targetProject = serviceFacade.getProjectById(targetProjectId);
		if (!targetProject.isValid()) {
			throw new BimError("Project for export not found");
		}
		return targetProjectId;
	}

	private String getExportRevisionId(final String masterProjectId) {
		final String exportProjectId = getExportProjectId(masterProjectId);
		final String exportRevisionId = serviceFacade.getLastRevisionOfProject(exportProjectId);
		if (exportRevisionId == null || exportRevisionId.isEmpty()) {
			throw new BimError("Revision for export not found");
		}
		return exportRevisionId;
	}

}
