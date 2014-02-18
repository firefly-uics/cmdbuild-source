package org.cmdbuild.logic.bim;

import static org.cmdbuild.bim.utils.BimConstants.CARDID_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.CARD_DESCRIPTION_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.CLASSID_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.CLASSNAME_FIELD_NAME;
import static org.cmdbuild.services.bim.DefaultBimDataModelManager.DEFAULT_DOMAIN_SUFFIX;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.bim.mapper.xml.XmlExportCatalogFactory;
import org.cmdbuild.bim.mapper.xml.XmlImportCatalogFactory;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.bim.BimDataModelCommand;
import org.cmdbuild.services.bim.BimDataModelCommandFactory;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.cmdbuild.services.bim.connector.DefaultBimDataView.BimObjectCard;
import org.cmdbuild.services.bim.connector.Mapper;
import org.cmdbuild.services.bim.connector.export.Export;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public class BimLogic implements Logic {

	private final BimServiceFacade bimServiceFacade;
	private final BimDataPersistence bimDataPersistence;
	private final BimDataModelManager bimDataModelManager;
	private final Mapper mapper;
	private final Export exporter;
	private final BimDataView bimDataView;
	private final DataAccessLogic dataAccessLogic;

	public BimLogic( //
			final BimServiceFacade bimServiceFacade, //
			final BimDataPersistence bimDataPersistence, //
			final BimDataModelManager bimDataModelManager, //
			final Mapper mapper, //
			final Export exporter, //
			final BimDataView bimDataView, //
			final DataAccessLogic dataAccessLogic) {

		this.bimDataPersistence = bimDataPersistence;
		this.bimServiceFacade = bimServiceFacade;
		this.bimDataModelManager = bimDataModelManager;
		this.mapper = mapper;
		this.exporter = exporter;
		this.bimDataView = bimDataView;
		this.dataAccessLogic = dataAccessLogic;
	}

	// CRUD operations on BimProjectInfo

	public BimProjectInfo createBimProjectInfo(final BimProjectInfo projectInfo, final File ifcFile) {

		final String identifier = bimServiceFacade.createProject(projectInfo.getName());
		projectInfo.setProjectId(identifier);

		bimDataPersistence.saveProject(projectInfo);

		if (ifcFile != null) {
			uploadIfcFile(projectInfo, ifcFile);
		}

		return projectInfo;
	}

	public List<BimProjectInfo> readBimProjectInfo() {
		return bimDataPersistence.listProjectInfo();
	}

	public void disableProject(final String projectId) {
		bimServiceFacade.disableProject(projectId);
		bimDataPersistence.disableProject(projectId);
	}

	public void enableProject(final String projectId) {
		bimServiceFacade.enableProject(projectId);
		bimDataPersistence.enableProject(projectId);
	}

	/**
	 * This method can update only description, active attributes. It updates
	 * lastCheckin attribute and synchronized attribute if ifcFile != null
	 * */
	public void updateBimProjectInfo(final BimProjectInfo projectInfo, final File ifcFile) {
		if (ifcFile != null) {
			uploadIfcFile(projectInfo, ifcFile);
		} else {
			bimServiceFacade.updateProject(projectInfo);
			bimDataPersistence.saveProject(projectInfo);
		}
	}

	private void uploadIfcFile(final BimProjectInfo projectInfo, final File ifcFile) {
		final DateTime timestamp = bimServiceFacade.updateProject(projectInfo, ifcFile);
		projectInfo.setLastCheckin(timestamp);
		projectInfo.setSynch(false);
		bimDataPersistence.saveProject(projectInfo);
	}

	// CRUD operations on BimLayer

	/**
	 * 
	 * @return a List of BimLayer. The list contains an item for each CMDBuild
	 *         Class with the relative BIM info
	 * 
	 */
	public List<BimLayer> readBimLayer() {
		final List<BimLayer> out = new LinkedList<BimLayer>();
		final Map<String, BimLayer> storedLayers = bimLayerMap();
		final Iterable<? extends CMClass> allClasses = dataAccessLogic.findAllClasses();
		for (final CMClass cmdbuildClass : allClasses) {
			if (cmdbuildClass.isSystem() || cmdbuildClass.isBaseClass()) {

				continue;
			}

			final String layerName = cmdbuildClass.getName();
			final String layerDescription = cmdbuildClass.getDescription();

			BimLayer layerToPut = null;
			if (storedLayers.containsKey(layerName)) {
				layerToPut = storedLayers.get(layerName);
			} else {
				layerToPut = new BimLayer(layerName);
			}

			layerToPut.setDescription(layerDescription);
			out.add(layerToPut);
		}

		return out;
	}

	private Map<String, BimLayer> bimLayerMap() {
		final Map<String, BimLayer> out = new HashMap<String, BimLayer>();
		final List<BimLayer> storedLayers = bimDataPersistence.listLayers();
		for (final BimLayer layer : storedLayers) {
			out.put(layer.getClassName(), layer);
		}

		return out;
	}

	public void updateBimLayer(final String className, final String attributeName, final String value) {

		final BimDataModelCommandFactory factory = new BimDataModelCommandFactory(bimDataPersistence, //
				bimDataModelManager);
		final BimDataModelCommand dataModelCommand = factory.create(attributeName);
		dataModelCommand.execute(className, value);
	}

	// write binding between BimProjects and cards of "BimRoot" class

	public void bindProjectToCards(final String projectCardId, final ArrayList<String> cardsId) {
		final String rootClass = bimDataPersistence.findRoot().getClassName();
		bimDataModelManager.bindProjectToCards(projectCardId, rootClass, cardsId);
	}

	public String getPoidForCardId(final Long cardId) {
		String poid = null;
		final String rootClass = bimDataPersistence.findRoot().getClassName();
		final Card src = Card.newInstance() //
				.withClassName(rootClass) //
				.withId(cardId) //
				.build();
		final CMDomain domain = dataAccessLogic.findDomain(rootClass + DEFAULT_DOMAIN_SUFFIX);

		final DomainWithSource dom = DomainWithSource.create(domain.getId(), "_1");
		final GetRelationListResponse domains = dataAccessLogic.getRelationList(src, dom);
		Object first = firstElement(domains);
		if (first != null) {
			final DomainInfo firstDomain = (DomainInfo) first;
			first = firstElement(firstDomain);
			if (first != null) {
				final RelationInfo firstRelation = (RelationInfo) first;
				final Long projectCardId = firstRelation.getRelation().getCard2Id();
				poid = bimDataPersistence.getProjectIdFromCardId(projectCardId);
			}
		}
		if (poid == null) {
			final long buildingId = bimDataView.fetchBuildingIdFromCardId(cardId);
			if (buildingId != -1) {
				poid = getPoidForCardId(buildingId);
			}
		}
		return poid;
	}

	public String getRoidForCardId(final Long cardId) {
		final String poid = getPoidForCardId(cardId);
		if (poid != null) {
			return bimServiceFacade.roidFromPoid(poid);
		}
		return null;
	}

	private Object firstElement(final Iterable<?> iterable) {
		final Iterator<?> iterator = iterable.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}

	// read binding between BimProjects and cards of "BimRoot" class

	public ArrayList<String> readBindingProjectToCards(final String projectId, final String className) {
		return bimDataModelManager.fetchCardsBindedToProject(projectId, className);
	}

	// Synchronization of data between IFC and CMDB

	public void importIfc(final String projectId) {
		final BimProjectInfo projectInfo = bimDataPersistence.fetchProjectInfo(projectId);

		final String xmlMapping = projectInfo.getImportMapping();
		System.out.println("[DEBUG] import mapping \n " + xmlMapping);
		final Catalog catalog = XmlImportCatalogFactory.withXmlStringMapper(xmlMapping).create();

		for (final EntityDefinition entityDefinition : catalog.getEntitiesDefinitions()) {
			final List<Entity> source = bimServiceFacade.readEntityFromProject(entityDefinition, projectInfo);
			if (source.size() > 0) {
				mapper.update(source);
			}
		}
		bimDataPersistence.setSynchronized(projectInfo, true);
	}

	// Export data from CMDB to a BimProject

	public void exportIfc(final String sourceProjectId) {

		final BimProjectInfo projectInfo = bimDataPersistence.fetchProjectInfo(sourceProjectId);
		final String xmlMapping = projectInfo.getExportMapping();
		final Catalog catalog = XmlExportCatalogFactory.withXmlString(xmlMapping).create();
		final String targetProjectId = exporter.export(catalog, sourceProjectId);

		// TODO remove, this is just for test
		bimServiceFacade.downloadLastRevisionOfProject(targetProjectId);

	}

	public void download(final String projectId) {
		bimServiceFacade.downloadLastRevisionOfProject(projectId);
	}

	public BimLayer getRootLayer() {
		return bimDataPersistence.findRoot();
	}

	public BimObjectCard fetchCardDataFromObjectId(final String objectId, final String revisionId) {
		final String globalId = bimServiceFacade.fetchGlobalIdFromObjectId(objectId, revisionId);
		final BimObjectCard bimCard = bimDataView.getBimDataFromGlobalid(globalId);
		return bimCard;
	}

	public String fetchJsonForBimViewer(final String revisionId) {
		final DataHandler jsonFile = bimServiceFacade.fetchProjectStructure(revisionId);
		try {
			final Reader reader = new InputStreamReader(jsonFile.getInputStream(), "UTF-8");
			final BufferedReader fileReader = new BufferedReader(reader);
			final ObjectMapper mapper = new ObjectMapper();
			final JsonNode rootNode = mapper.readTree(fileReader);

			final JsonNode data = rootNode.findValue("data");
			final JsonNode properties = data.findValue("properties");

			final Map<Long, BimObjectCard> mergedMap = buildIdMapForBimViewer(revisionId);
			final Iterator<String> propertieIds = properties.getFieldNames();

			while (propertieIds.hasNext()) {
				final String oid = propertieIds.next();
				final ObjectNode property = (ObjectNode) properties.findValue(oid);

				final Long longOid = Long.parseLong(oid);
				if (mergedMap.containsKey(longOid)) {
					final BimObjectCard cardData = mergedMap.get(longOid);
					final ObjectNode cmdbuildData = mapper.createObjectNode();
					cmdbuildData.put(CARDID_FIELD_NAME, cardData.getId());
					cmdbuildData.put(CLASSID_FIELD_NAME, cardData.getClassId());
					cmdbuildData.put(CLASSNAME_FIELD_NAME, cardData.getClassName());
					cmdbuildData.put(CARD_DESCRIPTION_FIELD_NAME, cardData.getCardDescription());
					property.put("cmdbuild_data", cmdbuildData);
				}
			}

			return rootNode.toString();
		} catch (final Throwable t) {
			throw new BimError("Cannot read the Json", t);
		}
	}

	private final Map<String, Map<Long, BimObjectCard>> cacheMapsIds = new HashMap<String, Map<Long, BimObjectCard>>();

	private Map<Long, BimObjectCard> buildIdMapForBimViewer(final String revisionId) {
		if (cacheMapsIds.containsKey(revisionId)) {
			return cacheMapsIds.get(revisionId);
		}
		final Map<Long, String> oidGuidMap = bimServiceFacade.fetchAllGlobalId(revisionId);
		final Map<Long, BimObjectCard> oidBimDataMap = Maps.newHashMap();
		for (final Long oid : oidGuidMap.keySet()) {
			final String objectId = oid.toString();
			final String globalId = bimServiceFacade.fetchGlobalIdFromObjectId(objectId, revisionId);
			final BimObjectCard card = bimDataView.getBimDataFromGlobalid(globalId);
			oidBimDataMap.put(oid, card);
		}
		cacheMapsIds.put(revisionId, oidBimDataMap);
		return oidBimDataMap;
	}

	public boolean getActiveForClassname(final String classname) {
		return bimDataPersistence.getActiveForClassname(classname);
	}

}