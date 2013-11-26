package org.cmdbuild.logic.bim;

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


import static org.cmdbuild.bim.utils.BimConstants.*;
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
import org.cmdbuild.services.bim.connector.Mapper;
import org.cmdbuild.services.bim.connector.export.Exporter;
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
	private final Exporter exporter;
	private final BimDataView bimDataView;
	private final DataAccessLogic dataAccessLogic;

	public BimLogic( //
			final BimServiceFacade bimServiceFacade, //
			final BimDataPersistence bimDataPersistence, //
			final BimDataModelManager bimDataModelManager, //
			final Mapper mapper, //
			final Exporter exporter, //
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

	public BimProjectInfo createBimProjectInfo(BimProjectInfo projectInfo, final File ifcFile) {

		String identifier = bimServiceFacade.createProject(projectInfo.getName());
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
	public void updateBimProjectInfo(BimProjectInfo projectInfo, final File ifcFile) {
		if (ifcFile != null) {
			uploadIfcFile(projectInfo, ifcFile);
		} else {
			bimServiceFacade.updateProject(projectInfo);
			bimDataPersistence.saveProject(projectInfo);
		}
	}

	private void uploadIfcFile(BimProjectInfo projectInfo, final File ifcFile) {
		DateTime timestamp = bimServiceFacade.updateProject(projectInfo, ifcFile);
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

	public void updateBimLayer(String className, String attributeName, String value) {

		BimDataModelCommandFactory factory = new BimDataModelCommandFactory(bimDataPersistence, //
				bimDataModelManager);
		BimDataModelCommand dataModelCommand = factory.create(attributeName);
		dataModelCommand.execute(className, value);
	}

	// write binding between BimProjects and cards of "BimRoot" class

	public void bindProjectToCards(String projectCardId, ArrayList<String> cardsId) {
		String rootClass = bimDataPersistence.findRoot().getClassName();
		bimDataModelManager.bindProjectToCards(projectCardId, rootClass, cardsId);
	}

	public String getPoidForCardId(Long cardId) {
		String rootClass = bimDataPersistence.findRoot().getClassName();
		final Card src = Card.newInstance() //
				.withClassName(rootClass) //
				.withId(cardId) //
				.build();
		final CMDomain domain = dataAccessLogic.findDomain(rootClass + DEFAULT_DOMAIN_SUFFIX);

		final DomainWithSource dom = DomainWithSource.create(domain.getId(), "_1");
		final GetRelationListResponse domains = dataAccessLogic.getRelationList(src, dom);
		Object first = firstElement(domains);
		if (first != null) {
			DomainInfo firstDomain = (DomainInfo) first;
			first = firstElement(firstDomain);
			if (first != null) {
				RelationInfo firstRelation = (RelationInfo) first;
				Long projectCardId = firstRelation.getRelation().getCard2Id();

				return bimDataPersistence.getProjectIdFromCardId(projectCardId);
			}
		}

		return null;
	}

	public String getRoidForCardId(Long cardId) {
		final String poid = getPoidForCardId(cardId);
		if (poid != null) {
			return bimServiceFacade.roidFromPoid(poid);
		}
		return null;
	}

	private Object firstElement(Iterable<?> iterable) {
		Iterator<?> iterator = iterable.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}

	// read binding between BimProjects and cards of "BimRoot" class

	public ArrayList<String> readBindingProjectToCards(String projectId, String className) {
		return bimDataModelManager.fetchCardsBindedToProject(projectId, className);
	}

	// Synchronization of data between IFC and CMDB

	public void importIfc(String projectId) {
		BimProjectInfo projectInfo = bimDataPersistence.fetchProjectInfo(projectId);

		String xmlMapping = projectInfo.getImportMapping();
		System.out.println("[DEBUG] import mapping \n " + xmlMapping);
		Catalog catalog = XmlImportCatalogFactory.withXmlStringMapper(xmlMapping).create();

		for (EntityDefinition entityDefinition : catalog.getEntitiesDefinitions()) {
			List<Entity> source = bimServiceFacade.readEntityFromProject(entityDefinition, projectInfo);
			if (source.size() > 0) {
				mapper.update(source);
			}
		}
		bimDataPersistence.setSynchronized(projectInfo, true);
	}

	// Export data from CMDB to a BimProject

	public void exportIfc(String projectId) {

		BimProjectInfo projectInfo = bimDataPersistence.fetchProjectInfo(projectId);
		String xmlMapping = projectInfo.getExportMapping();
		System.out.println("[DEBUG] export mapping \n " + xmlMapping);
		Catalog catalog = XmlExportCatalogFactory.withXmlString(xmlMapping).create();

		String revisionId = exporter.export(catalog, projectId);

		// TODO remove this, it is just for test.
		if (!revisionId.equals("-1")) {
			bimServiceFacade.download(projectId);
		}

	}

	public void download(String projectId) {
		bimServiceFacade.download(projectId);
	}

	public BimLayer getRootLayer() {
		return bimDataPersistence.findRoot();
	}

	public Map<String, Long> fetchIdAndIdClassFromBimViewerId(String objectId, String revisionId) {
		String globalId = bimServiceFacade.fetchGlobalIdFromObjectId(objectId, revisionId);
		return bimDataView.fetchIdAndIdClassFromGlobalId(globalId);

	}


	public String fetchJsonForBimViewer(String revisionId) {
		DataHandler jsonFile = bimServiceFacade.fetchProjectStructure(revisionId);
		try {
			Reader reader = new InputStreamReader(jsonFile.getInputStream(), "UTF-8");
			BufferedReader fileReader = new BufferedReader(reader);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(fileReader);
			Map<Long, int[]> mergedMap = buildIdMapForBimViewer(revisionId);
			readNode(rootNode, mergedMap);
			return rootNode.toString();
		} catch (Throwable t) {
			throw new BimError("Cannot read the Json", t);
		}
	}
	
	private Map<Long, int[]> buildIdMapForBimViewer(String revisionId) {
		Map<Long, String> oidGuidMap = bimServiceFacade.fetchAllGlobalId(revisionId);
		Map<String, int[]> guidIdIdclassMap = bimDataView.fetchIdAndIdClassForGlobalIdMap(oidGuidMap);
		Map<Long, int[]> mergedMap = Maps.newHashMap();
		for (Long oid : oidGuidMap.keySet()) {
			String guid = oidGuidMap.get(oid);
			if (guidIdIdclassMap.get(guid) != null) {
				mergedMap.put(oid, guidIdIdclassMap.get(guid));
			}
		}
		return mergedMap;
	}
	
	private void readNode(JsonNode node, Map<Long, int[]> mergedMap) {
		JsonNode nodeId = node.get(ID_FIELD_NAME);
		if (nodeId != null && !nodeId.isTextual()) {
			if (mergedMap.get(nodeId.getLongValue()) != null) {
				ObjectNode objectNode = (ObjectNode) node;
				objectNode.put(CARDID_FIELD_NAME, mergedMap.get(nodeId.getLongValue())[0]);
				objectNode.put(CLASSID_FIELD_NAME, mergedMap.get(nodeId.getLongValue())[1]);
			}
		}
		Iterator<Map.Entry<String, JsonNode>> fields = node.getFields();
		while (fields.hasNext()) {
			Map.Entry<String, JsonNode> field = fields.next();
			JsonNode value = field.getValue();
			if (value.isArray()) {
				readArray(value, mergedMap);
			} else if (value.isObject()) {
				readNode(value, mergedMap);
			} else {
			}
		}
	}

	public void readArray(JsonNode node, Map<Long, int[]> mergedMap) {
		Iterator<JsonNode> nodes = node.iterator();
		while (nodes.hasNext())
			readNode(nodes.next(), mergedMap);
	}

}
