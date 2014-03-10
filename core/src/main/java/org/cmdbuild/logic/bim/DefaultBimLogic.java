package org.cmdbuild.logic.bim;

import static org.cmdbuild.bim.utils.BimConstants.CARDID_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.CARD_DESCRIPTION_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.CLASSID_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.CLASSNAME_FIELD_NAME;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.services.bim.DefaultBimDataModelManager.DEFAULT_DOMAIN_SUFFIX;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.bim.mapper.xml.XmlExportCatalogFactory;
import org.cmdbuild.bim.mapper.xml.XmlImportCatalogFactory;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.bim.BimDataModelCommand;
import org.cmdbuild.services.bim.BimDataModelCommandFactory;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimFacade.BimFacadeProject;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.CmProject;
import org.cmdbuild.services.bim.connector.DefaultBimDataView.BimCard;
import org.cmdbuild.services.bim.connector.Mapper;
import org.cmdbuild.services.bim.connector.export.DefaultExportListener;
import org.cmdbuild.services.bim.connector.export.Export;
import org.cmdbuild.services.bim.connector.export.NewExport;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultBimLogic implements BimLogic {

	private final BimFacade bimServiceFacade;
	private final BimPersistence bimDataPersistence;
	private final BimDataModelManager bimDataModelManager;
	private final Mapper mapper;
	private final Export exporter;
	private final BimDataView bimDataView;
	private final DataAccessLogic dataAccessLogic;

	public DefaultBimLogic( //
			final BimFacade bimServiceFacade, //
			final BimPersistence bimDataPersistence, //
			final BimDataModelManager bimDataModelManager, //
			final Mapper mapper, //
			final BimDataView bimDataView, //
			final DataAccessLogic dataAccessLogic) {

		this.bimDataPersistence = bimDataPersistence;
		this.bimServiceFacade = bimServiceFacade;
		this.bimDataModelManager = bimDataModelManager;
		this.mapper = mapper;
		this.exporter = new NewExport(bimDataView, bimServiceFacade, bimDataPersistence);
		this.bimDataView = bimDataView;
		this.dataAccessLogic = dataAccessLogic;
	}

	private static class DefaultBimFacadeProject implements BimFacadeProject {

		private String name;
		private File file;
		private boolean active;
		private String projectId;

		@Override
		public String getProjectId() {
			return projectId;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public boolean isActive() {
			return this.active;
		}

		@Override
		public DateTime getLastCheckin() {
			throw new UnsupportedOperationException();
		}

		@Override
		public File getFile() {
			return this.file;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public void setFile(final File file) {
			this.file = file;
		}

		public void setActive(final boolean active) {
			this.active = active;
		}

		@Override
		public boolean isSynch() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setLastCheckin(final DateTime lastCheckin) {
			throw new UnsupportedOperationException();
		}

		public void setProjectId(final String projectId) {
			this.projectId = projectId;
		}

		@Override
		public String getShapeProjectId() {
			throw new UnsupportedOperationException("to do");
		}

		@Override
		public String getExportProjectId() {
			throw new UnsupportedOperationException("to do");
		}

	}

	private static class DefaultCmProject implements CmProject {

		private Long cmId;
		private String name, description, importMapping, exportMapping, projectId;
		private boolean sync, active;
		private DateTime lastCheckin;
		private Iterable<String> cardBinding;
		private String exportProjectId;

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isActive() {
			return active;
		}

		@Override
		public String getImportMapping() {
			return importMapping;
		}

		@Override
		public String getExportMapping() {
			return exportMapping;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public boolean isSynch() {
			return sync;
		}

		@Override
		public DateTime getLastCheckin() {
			return lastCheckin;
		}

		@Override
		public Iterable<String> getCardBinding() {
			return this.cardBinding;
		}

		@Override
		public void setProjectId(final String projectId) {
			this.projectId = projectId;
		}

		@Override
		public void setLastCheckin(final DateTime lastCheckin) {
			this.lastCheckin = lastCheckin;
		}

		@Override
		public void setSynch(final boolean sync) {
			this.sync = sync;
		}

		@Override
		public String getProjectId() {
			return projectId;
		}

		@Override
		public Long getCmId() {
			return cmId;
		}

		@Override
		public void setName(final String name) {
			this.name = name;
		}

		@Override
		public void setDescription(final String description) {
			this.description = description;
		}

		@Override
		public void setCardBinding(final Iterable<String> cardBinding) {
			this.cardBinding = cardBinding;
		}

		@Override
		public void setActive(final boolean active) {
			this.active = active;
		}

		@Override
		public String getExportProjectId() {
			return exportProjectId;
		}

		@Override
		public String getShapeProjectId() {
			throw new UnsupportedOperationException("to do");
		}

		@Override
		public void setExportProjectId(final String projectId) {
			this.exportProjectId = projectId;
		}
	}

	private static Project from(final CmProject createdPersistenceProject) {
		return new Project() {

			@Override
			public boolean isSynch() {
				return createdPersistenceProject.isSynch();
			}

			@Override
			public boolean isActive() {
				return createdPersistenceProject.isActive();
			}

			@Override
			public String getProjectId() {
				return createdPersistenceProject.getProjectId();
			}

			@Override
			public String getName() {
				return createdPersistenceProject.getName();
			}

			@Override
			public DateTime getLastCheckin() {
				return createdPersistenceProject.getLastCheckin();
			}

			@Override
			public String getDescription() {
				return createdPersistenceProject.getDescription();
			}

			@Override
			public Iterable<String> getCardBinding() {
				return createdPersistenceProject.getCardBinding();
			}

			@Override
			public String getImportMapping() {
				return createdPersistenceProject.getImportMapping();
			}

			@Override
			public String getExportMapping() {
				return createdPersistenceProject.getExportMapping();
			}

			@Override
			public File getFile() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public Iterable<Project> readAllProjects() {
		final Iterable<CmProject> cmProjectList = bimDataPersistence.readAll();
		final Iterable<Project> projectList = listFrom(cmProjectList);
		return projectList;
	}

	@Override
	public Project createProject(final Project project) {

		final BimFacadeProject bimProject = bimProjectfrom(project);
		final BimFacadeProject baseProject = bimServiceFacade.createBaseAndExportProject(bimProject);

		final CmProject cmProject = cmProjectFrom(project);
		cmProject.setProjectId(baseProject.getProjectId());
		cmProject.setLastCheckin(baseProject.getLastCheckin());
		cmProject.setSynch(project.isSynch());
		cmProject.setExportProjectId(baseProject.getExportProjectId());
		bimDataPersistence.saveProject(cmProject);

		final Project result = from(cmProject);
		return result;
	}

	@Override
	public void disableProject(final Project project) {
		final BimFacadeProject bimProject = bimProjectfrom(project);
		bimServiceFacade.disableProject(bimProject);

		final CmProject persistenceProject = cmProjectFrom(project);
		bimDataPersistence.disableProject(persistenceProject);
	}

	@Override
	public void enableProject(final Project project) {
		final BimFacadeProject bimProject = bimProjectfrom(project);
		bimServiceFacade.enableProject(bimProject);

		final CmProject persistenceProject = cmProjectFrom(project);
		bimDataPersistence.enableProject(persistenceProject);
	}

	@Override
	public void updateProject(final Project project) {
		final String projectId = project.getProjectId();
		final BimFacadeProject bimProject = bimProjectfrom(project);
		final BimFacadeProject updatedProject = bimServiceFacade.updateProject(bimProject);

		final CmProject persistenceProject = cmProjectFrom(project);
		if (updatedProject.getLastCheckin() != null) {
			persistenceProject.setLastCheckin(updatedProject.getLastCheckin());
		}
		bimDataPersistence.saveProject(persistenceProject);

		if (project.getFile() != null) {
			final Runnable uploadFileOnExportProject = new Runnable() {

				@Override
				public void run() {
					final CmProject storedProject = bimDataPersistence.read(projectId);
					final String exportProjectId = storedProject.getExportProjectId();
					final String shapeProjectId = storedProject.getShapeProjectId();
					if (!exportProjectId.isEmpty() && !shapeProjectId.isEmpty()) {
						bimServiceFacade.updateExportProject(projectId, exportProjectId, shapeProjectId);
					}
				}
			};
			final Thread threadForUpload = new Thread(uploadFileOnExportProject);
			threadForUpload.start();
		}
	}

	private Iterable<Project> listFrom(final Iterable<CmProject> cmProjectList) {
		final List<Project> projectList = Lists.newArrayList();
		for (final Iterator<CmProject> it = cmProjectList.iterator(); it.hasNext();) {
			final CmProject cmProject = it.next();
			final Project project = from(cmProject);
			projectList.add(project);
		}
		return projectList;
	}

	private static BimFacadeProject bimProjectfrom(final Project project) {
		final DefaultBimFacadeProject bimProject = new DefaultBimFacadeProject();
		bimProject.setName(project.getName());
		bimProject.setFile(project.getFile());
		bimProject.setActive(project.isActive());
		bimProject.setProjectId(project.getProjectId());
		return bimProject;
	}

	private static CmProject cmProjectFrom(final Project project) {
		final CmProject cmProject = new DefaultCmProject();
		cmProject.setName(project.getName());
		cmProject.setDescription(project.getDescription());
		cmProject.setCardBinding(project.getCardBinding());
		cmProject.setActive(project.isActive());
		cmProject.setProjectId(project.getProjectId());
		return cmProject;
	}

	// CRUD operations on BimLayer

	/**
	 * 
	 * @return a List of BimLayer. The list contains an item for each CMDBuild
	 *         Class with the relative BIM info
	 * 
	 */
	@Override
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
		final List<BimLayer> storedLayers = (List<BimLayer>) bimDataPersistence.listLayers();
		for (final BimLayer layer : storedLayers) {
			out.put(layer.getClassName(), layer);
		}
		return out;
	}

	@Override
	public void updateBimLayer(final String className, final String attributeName, final String value) {

		final BimDataModelCommandFactory factory = new BimDataModelCommandFactory(bimDataPersistence, //
				bimDataModelManager);
		final BimDataModelCommand dataModelCommand = factory.create(attributeName);
		dataModelCommand.execute(className, value);
	}

	@Override
	public String getExportProjectId(final String baseProjectId) {
		final CmProject project = bimDataPersistence.read(baseProjectId);
		return project.getExportProjectId();
	}

	@Override
	public String getDescriptionOfRoot(final Long cardId, final String className) {
		final Long rootId = getRootId(cardId, className);
		final BimLayer rootLayer = bimDataPersistence.findRoot();
		if (rootLayer == null || rootLayer.getClassName() == null || rootLayer.getClassName().isEmpty()) {
			throw new BimError("Root layer not configured");
		}
		final Card rootCard = dataAccessLogic.fetchCard(rootLayer.getClassName(), rootId);
		final String description = String.class.cast(rootCard.getAttribute(DESCRIPTION_ATTRIBUTE));
		return description;
	}

	private Long getRootId(final Long cardId, final String className) {
		Long rootId = null;
		final BimLayer rootLayer = bimDataPersistence.findRoot();
		if (rootLayer == null || rootLayer.getClassName() == null || rootLayer.getClassName().isEmpty()) {
			throw new BimError("Root layer not configured");
		}
		if (className.equals(rootLayer.getClassName())) {
			rootId = cardId;
		} else {
			final BimLayer layer = bimDataPersistence.readLayer(className);
			if (layer == null || layer.getRootReference() == null || layer.getRootReference().isEmpty()) {
				throw new BimError("'" + className + "' layer not configured");
			}
			final String referenceRoot = layer.getRootReference();
			rootId = bimDataView.fetchRoot(cardId, className, referenceRoot);
			if (rootId == null) {
				throw new BimError(referenceRoot + " is null for card '" + cardId + "' of class '" + className + "'");
			}
		}
		return rootId;
	}

	@Override
	public String getBaseProjectIdForCardOfClass(final Long cardId, final String className) {
		final Long rootId = getRootId(cardId, className);
		final BimLayer rootLayer = bimDataPersistence.findRoot();
		final String baseProjectId = getProjectIdForRootClass(rootId, rootLayer.getClassName());
		if (baseProjectId.isEmpty()) {
			throw new BimError("Project not found for card '" + cardId + "' and class '" + className + "'");
		}
		return baseProjectId;
	}

	private String getProjectIdForRootClass(final Long rootId, final String rootClassName) {
		String projectId = StringUtils.EMPTY;
		final Card src = Card.newInstance() //
				.withClassName(rootClassName) //
				.withId(rootId) //
				.build();
		final CMDomain domain = dataAccessLogic.findDomain(rootClassName + DEFAULT_DOMAIN_SUFFIX);
		final DomainWithSource dom = DomainWithSource.create(domain.getId(), "_1");
		final GetRelationListResponse domains = dataAccessLogic.getRelationList(src, dom);
		Object first = firstElement(domains);
		if (first != null) {
			final DomainInfo firstDomain = (DomainInfo) first;
			first = firstElement(firstDomain);
			if (first != null) {
				final RelationInfo firstRelation = (RelationInfo) first;
				final Long projectCardId = firstRelation.getRelation().getCard2Id();
				projectId = bimDataPersistence.getProjectIdFromCardId(projectCardId);
			}
		}
		return projectId;
	}

	@Override
	public String getLastRevisionOfProject(final String projectId) {
		String revisionId = StringUtils.EMPTY;
		if (!projectId.isEmpty()) {
			revisionId = bimServiceFacade.getLastRevisionOfProject(projectId);
		}
		return revisionId;
	}

	private Object firstElement(final Iterable<?> iterable) {
		final Iterator<?> iterator = iterable.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}

	@Override
	public void importIfc(final String projectId) {

		final CmProject project = bimDataPersistence.read(projectId);
		final String xmlMapping = project.getImportMapping();
		System.out.println("[DEBUG] import mapping \n " + xmlMapping);
		final Catalog catalog = XmlImportCatalogFactory.withXmlStringMapper(xmlMapping).create();

		for (final EntityDefinition entityDefinition : catalog.getEntitiesDefinitions()) {
			final List<Entity> source = bimServiceFacade
					.readEntityFromProject(entityDefinition, project.getProjectId());
			if (source.size() > 0) {
				mapper.update(source);
			}
		}
		final CmProject projectSynchronized = cmProjectFrom(from(project));
		projectSynchronized.setProjectId(projectId);
		projectSynchronized.setSynch(true);
		bimDataPersistence.saveProject(projectSynchronized);
	}

	@Override
	public void exportIfc(final String projectId) {
		final CmProject project = bimDataPersistence.read(projectId);
		final String xmlMapping = project.getExportMapping();
		final Catalog catalog = XmlExportCatalogFactory.withXmlString(xmlMapping).create();
		exporter.export(catalog, projectId, new DefaultExportListener(bimServiceFacade));
	}

	@Override
	public boolean isSynchForExport(final String projectId) {
		final CmProject project = bimDataPersistence.read(projectId);
		final String xmlMapping = project.getExportMapping();
		final Catalog catalog = XmlExportCatalogFactory.withXmlString(xmlMapping).create();
		return exporter.isSynch(catalog, projectId);
	}

	@Override
	public DataHandler download(final String projectId) {
		return bimServiceFacade.download(projectId);
	}

	@Override
	public BimLayer getRootLayer() {
		return bimDataPersistence.findRoot();
	}

	@Override
	public BimCard fetchCardDataFromObjectId(final String objectId, final String revisionId) {
		final String globalId = bimServiceFacade.fetchGlobalIdFromObjectId(objectId, revisionId);
		final BimCard bimCard = bimDataView.getBimDataFromGlobalid(globalId);
		return bimCard;
	}

	@Override
	public String fetchJsonForBimViewer(final String revisionId) {
		final DataHandler jsonFile = bimServiceFacade.fetchProjectStructure(revisionId);
		try {
			final Reader reader = new InputStreamReader(jsonFile.getInputStream(), "UTF-8");
			final BufferedReader fileReader = new BufferedReader(reader);
			final ObjectMapper mapper = new ObjectMapper();
			final JsonNode rootNode = mapper.readTree(fileReader);

			final JsonNode data = rootNode.findValue("data");
			final JsonNode properties = data.findValue("properties");

			final Iterator<String> propertiesIterator = properties.getFieldNames();

			while (propertiesIterator.hasNext()) {
				final String oid = propertiesIterator.next();
				final ObjectNode property = (ObjectNode) properties.findValue(oid);
				final Long longOid = Long.parseLong(oid);

				final BimCard cardData = getBimCardFromOid(longOid, revisionId);

				if (cardData != null) {
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

	private final Map<String, Map<Long, BimCard>> oidBimcardMap = new HashMap<String, Map<Long, BimCard>>();
	private Map<String, BimCard> guidCmidMap = Maps.newHashMap();

	private BimCard getBimCardFromOid(final Long longOid, final String revisionId) {
		if (oidBimcardMap.containsKey(revisionId)) {
			if (!oidBimcardMap.get(revisionId).containsKey(longOid)) {
				final String globalId = bimServiceFacade.getGlobalidFromOid(revisionId, longOid);
				BimCard bimCard = new BimCard();
				if (guidCmidMap.containsKey(globalId)) {
					bimCard = guidCmidMap.get(globalId);
				} else {
					guidCmidMap = bimDataView.getAllGlobalIdMap();
					bimCard = guidCmidMap.get(globalId);
				}
				oidBimcardMap.get(revisionId).put(longOid, bimCard);
			}
		} else {
			final String globalId = bimServiceFacade.getGlobalidFromOid(revisionId, longOid);
			guidCmidMap = bimDataView.getAllGlobalIdMap();
			final BimCard bimCard = guidCmidMap.get(globalId);
			final Map<Long, BimCard> oidBimCardMap = Maps.newHashMap();
			oidBimCardMap.put(longOid, bimCard);
			oidBimcardMap.put(revisionId, oidBimCardMap);
		}
		return oidBimcardMap.get(revisionId).get(longOid);
	}

	@Override
	public boolean getActiveForClassname(final String classname) {
		return bimDataPersistence.isActiveLayer(classname);
	}

}