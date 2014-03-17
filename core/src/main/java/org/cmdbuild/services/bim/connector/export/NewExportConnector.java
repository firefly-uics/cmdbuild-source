package org.cmdbuild.services.bim.connector.export;

import static org.cmdbuild.bim.utils.BimConstants.DEFAULT_TAG_EXPORT;
import static org.cmdbuild.bim.utils.BimConstants.FK_COLUMN_NAME;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_BUILDING_ELEMENT_PROXY;
import static org.cmdbuild.bim.utils.BimConstants.IFC_DESCRIPTION;
import static org.cmdbuild.bim.utils.BimConstants.IFC_FURNISHING;
import static org.cmdbuild.bim.utils.BimConstants.IFC_NAME;
import static org.cmdbuild.bim.utils.BimConstants.IFC_SPACE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_TAG;
import static org.cmdbuild.bim.utils.BimConstants.IFC_TYPE;
import static org.cmdbuild.bim.utils.BimConstants.INVALID_ID;
import static org.cmdbuild.bim.utils.BimConstants.OBJECT_OID;
import static org.cmdbuild.bim.utils.BimConstants.isValidId;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.services.bim.connector.DefaultBimDataView.CONTAINER_GUID;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.bim.mapper.DefaultAttribute;
import org.cmdbuild.bim.mapper.DefaultEntity;
import org.cmdbuild.bim.mapper.xml.XmlExportCatalogFactory;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.bimserver.BimserverEntity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.DBIdentifier;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.CmProject;
import org.cmdbuild.services.bim.connector.Output;
import org.cmdbuild.services.bim.connector.export.DataChangedListener.DataChangedException;
import org.cmdbuild.services.bim.connector.export.DataChangedListener.DataNotChangedException;
import org.cmdbuild.services.bim.connector.export.DataChangedListener.InvalidOutputException;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

public class NewExportConnector implements Export {

	private final BimFacade serviceFacade;
	private final BimPersistence persistence;
	private final BimDataView bimDataView;
	private final Map<String, Map<String, String>> shapeNameToOidMap;
	private final Iterable<String> candidateTypes = Lists.newArrayList(IFC_BUILDING_ELEMENT_PROXY, IFC_FURNISHING);
	private final ExportProjectPolicy exportProjectPolicy;

	public NewExportConnector(final BimDataView dataView, final BimFacade bimServiceFacade,
			final BimPersistence bimPersistence, final ExportProjectPolicy exportProjectPolicy) {
		this.serviceFacade = bimServiceFacade;
		this.persistence = bimPersistence;
		this.bimDataView = dataView;
		this.exportProjectPolicy = exportProjectPolicy;
		shapeNameToOidMap = Maps.newHashMap();
	}

	@Override
	public boolean isSynch(final String projectId) {
		boolean synch = true;
		final Output changeListener = new DataChangedListener();
		try {
			executeExport(projectId, changeListener);
		} catch (final DataChangedException de) {
			synch = false;
		} catch (final InvalidOutputException oe) {
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
		System.out.println(IFC_SPACE + " list fetched from the service");
		final Map<String, Long> globalIdToCmdbIdIfcSpaceMap = Maps.newHashMap();
		for (final String globalId : ifcSpacesGlobalIdList) {
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

	private Map<String, Entity> getTargetDataNew(final String sourceProjectId) {
		final String sourceRevisionId = getExportRevisionId(sourceProjectId);
		final Map<String, Entity> targetData = Maps.newHashMap();
		for (final String type : candidateTypes) {
			final Iterable<Entity> entityList = serviceFacade.fetchEntitiesOfType(type, sourceRevisionId);
			for (final Entity entity : entityList) {
				final Attribute tag = entity.getAttributeByName(IFC_TAG);
				if (!tag.isValid() || !DEFAULT_TAG_EXPORT.equals(tag.getValue())) {
					continue;
				} else {
					final String globalId = entity.getKey();
					final Long oid = BimserverEntity.class.cast(entity).getOid();
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
			}
		}
		return targetData;
	}

	@Override
	public String export(final String sourceProjectId, final Output output) {
		final boolean isSynchronized = isSynch(sourceProjectId);
		if (isSynchronized) {
			return getLastGeneratedOutput(sourceProjectId);
		} else {
			executeExport(sourceProjectId, output);
			return getLastGeneratedOutput(sourceProjectId);
		}
	}

	private void executeExport(final String sourceProjectId, final Output output) {

		final CmProject project = persistence.read(sourceProjectId);
		final String xmlMapping = project.getExportMapping();
		final Catalog catalog = XmlExportCatalogFactory.withXmlString(xmlMapping).create();

		final Map<String, Entity> sourceData = getSource(catalog, sourceProjectId);
		final Map<String, Entity> targetData = getTargetDataNew(sourceProjectId);
		final MapDifference<String, Entity> difference = Maps.difference(sourceData, targetData);
		final Map<String, Entity> entriesToCreate = difference.entriesOnlyOnLeft();
		final Map<String, ValueDifference<Entity>> entriesToUpdate = difference.entriesDiffering();
		final Map<String, Entity> entriesToRemove = difference.entriesOnlyOnRight();

		final String exportProjectId = getExportProjectId(sourceProjectId);
		final String exportRevisionId = serviceFacade.getLastRevisionOfProject(exportProjectId);
		final boolean shapesLoaded = areShapesOfCatalogAlreadyLoadedInRevision(catalog, exportRevisionId);
		if (!shapesLoaded) {
			output.outputInvalid();
			exportProjectPolicy.beforeExport(exportProjectId);
		}
		serviceFacade.openTransaction(exportProjectId);
		try {
			for (final String guidToCreate : entriesToCreate.keySet()) {
				final Entity entityToCreate = entriesToCreate.get(guidToCreate);
				output.createTarget(entityToCreate, exportProjectId);
			}
			for (final String guidToUpdate : entriesToUpdate.keySet()) {
				final ValueDifference<Entity> entityToUpdate = entriesToUpdate.get(guidToUpdate);
				final Entity entityToRemove = entityToUpdate.rightValue();
				final Entity entityToCreate = entityToUpdate.leftValue();
				final boolean toUpdate = areDifferent(entityToRemove, entityToCreate);
				if (toUpdate) {
					output.createTarget(entityToCreate, exportProjectId);
					output.deleteTarget(entityToRemove, exportProjectId);
				}
			}
			for (final String guidToRemove : entriesToRemove.keySet()) {
				final Entity entityToRemove = entriesToRemove.get(guidToRemove);
				output.deleteTarget(entityToRemove, exportProjectId);
			}
			output.updateRelations(exportProjectId);
			final String revisionId = serviceFacade.commitTransaction();
			System.out.println("Revision " + revisionId + " created at " + new DateTime());

			/*
			 * In order to see the generated objects I have to download and
			 * upload again the file. This is due to some problems with BimServer
			 * cache, I will investigate about a more efficient solution.
			 */
			final DataHandler exportedData = serviceFacade.download(exportProjectId);
			final File file = File.createTempFile("ifc", null);
			final FileOutputStream outputStream = new FileOutputStream(file);
			exportedData.writeTo(outputStream);
			serviceFacade.checkin(exportProjectId, file);
			System.out.println("export file is ready");
		} catch (final DataChangedException d) {
			serviceFacade.abortTransaction();
			throw new DataChangedException();
		} catch (final DataNotChangedException d) {
			serviceFacade.abortTransaction();
		} catch (final Throwable t) {
			serviceFacade.abortTransaction();
			throw new BimError("Error during export", t);
		}
	}

	private boolean areShapesOfCatalogAlreadyLoadedInRevision(final Catalog catalog, final String revisionId) {
		boolean allShapesAreLoaded = true;
		for (final EntityDefinition catalogEntry : catalog.getEntitiesDefinitions()) {
			final String shapeOid = getShapeOid(revisionId, catalogEntry.getShape());
			if (!isValidId(shapeOid)) {
				allShapesAreLoaded = false;
				break;
			}
		}
		return allShapesAreLoaded;
	}

	private boolean areDifferent(final Entity entityToRemove, final Entity entityToCreate) {
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

	private Map<String, Entity> getSourceData(final Map<String, Long> globalIdToCmdbIdMap, final Catalog catalog,
			final String revisionId, final String containerClassName) {

		final Map<String, Entity> dataSource = Maps.newHashMap();
		for (final Entry<String, Long> entry : globalIdToCmdbIdMap.entrySet()) {
			for (final EntityDefinition catalogEntry : catalog.getEntitiesDefinitions()) {
				final String ifcSpaceGuid = entry.getKey();
				final Long ifcSpaceCmId = entry.getValue();
				final String className = catalogEntry.getLabel();
				final String containerAttributeName = catalogEntry.getContainerAttribute();
				final String shapeOid = getShapeOid(revisionId, catalogEntry.getShape());
				final String ifcType = catalogEntry.getTypeName();

				final List<CMCard> cardsInTheIfcSpace = bimDataView.getCardsWithAttributeAndValue(
						DBIdentifier.fromName(className), ifcSpaceCmId, containerAttributeName);
				for (final CMCard cmcard : cardsInTheIfcSpace) {
					final Entity sourceData = bimDataView.getCardDataForExport(cmcard, className,
							String.valueOf(ifcSpaceCmId), ifcSpaceGuid, containerClassName, shapeOid, ifcType);
					dataSource.put(sourceData.getKey(), sourceData);
				}
			}
		}
		return dataSource;
	}

	private String getShapeOid(final String revisionId, final String shapeName) {
		String shapeOid = StringUtils.EMPTY;
		if (shapeNameToOidMap.containsKey(revisionId)) {
			Map<String, String> mapForCurrentRevision = shapeNameToOidMap.get(revisionId);
			if (mapForCurrentRevision.containsKey(shapeName)) {
				shapeOid = mapForCurrentRevision.get(shapeName);
			} else {
				shapeOid = serviceFacade.findShapeWithName(shapeName, revisionId);
				if (isValidId(shapeOid)) {
					mapForCurrentRevision.put(shapeName, shapeOid);
				}
			}
		} else {
			shapeOid = serviceFacade.findShapeWithName(shapeName, revisionId);
			if (isValidId(shapeOid)) {
				Map<String, String> mapForCurrentRevision = Maps.newHashMap();
				shapeNameToOidMap.put(revisionId, mapForCurrentRevision);
				mapForCurrentRevision.put(shapeName, shapeOid);
			}
		}
		return shapeOid;
	}

	private Long getIdFromGlobalId(final String key, final String className) {
		final CMCard theCard = getCardFromGlobalId(key, className);
		long matchingId = -1;
		if (theCard != null) {
			if (theCard.get(FK_COLUMN_NAME) != null) {
				final IdAndDescription reference = (IdAndDescription) theCard.get(FK_COLUMN_NAME);
				matchingId = reference.getId();
			}
		}
		return matchingId;
	}

	private CMCard getCardFromGlobalId(final String key, final String className) {
		CMCard theCard = null;
		final List<CMCard> cardList = bimDataView.getCardsWithAttributeAndValue(
				BimIdentifier.newIdentifier().withName(className), key, GLOBALID_ATTRIBUTE);
		if (!cardList.isEmpty() && cardList.size() == 1) {
			theCard = cardList.get(0);
		}
		return theCard;
	}

	private String getExportProjectId(final String masterProjectId) {
		final String targetProjectId = persistence.read(masterProjectId).getExportProjectId();
		if (targetProjectId == null || targetProjectId.isEmpty() || targetProjectId.equals(INVALID_ID)) {
			throw new BimError("Project for export not found");
		}
		return targetProjectId;
	}

	private String getExportRevisionId(final String masterProjectId) {
		final String exportProjectId = getExportProjectId(masterProjectId);
		final String exportRevisionId = serviceFacade.getLastRevisionOfProject(exportProjectId);
		if (exportRevisionId == null || exportRevisionId.isEmpty() || exportRevisionId.equals("-1")) {
			throw new BimError("Revision for export not found");
		}
		return exportRevisionId;
	}

	@Override
	public String getLastGeneratedOutput(final String baseProjectId) {
		final String exportProjectId = getExportProjectId(baseProjectId);
		final String outputRevisionId = serviceFacade.getLastRevisionOfProject(exportProjectId);
		return outputRevisionId;
	}

}
