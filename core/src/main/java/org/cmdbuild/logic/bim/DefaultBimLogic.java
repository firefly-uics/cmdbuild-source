package org.cmdbuild.logic.bim;

import static org.cmdbuild.bim.utils.BimConstants.CARDID_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.CARD_DESCRIPTION_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.CLASSID_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.CLASSNAME_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.IFC_BUILDING_ELEMENT_PROXY;
import static org.cmdbuild.bim.utils.BimConstants.IFC_FURNISHING;
import static org.cmdbuild.bim.utils.BimConstants.isValidId;
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
import org.cmdbuild.bim.mapper.xml.XmlImportCatalogFactory;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.DBIdentifier;
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
import org.cmdbuild.services.bim.DefaultBimDataView.BimCard;
import org.cmdbuild.services.bim.connector.Mapper;
import org.cmdbuild.services.bim.connector.export.DefaultExportListener;
import org.cmdbuild.services.bim.connector.export.Export;
import org.cmdbuild.services.bim.connector.export.ExportPolicy;
import org.cmdbuild.services.bim.connector.export.NewExportConnector;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultBimLogic implements BimLogic {

	private final BimFacade bimServiceFacade;
	private final BimPersistence bimPersistence;
	private final BimDataModelManager bimDataModelManager;
	private final Mapper mapper;
	private final Export exportConnector;
	private final BimDataView bimDataView;
	private final DataAccessLogic dataAccessLogic;
	private final ExportPolicy exportPolicy;
	private final Map<String, Map<String, Long>> guidToOidMap = Maps.newHashMap();

	public DefaultBimLogic( //
			final BimFacade bimServiceFacade, //
			final BimPersistence bimPersistence, //
			final BimDataModelManager bimDataModelManager, //
			final Mapper mapper, //
			final BimDataView bimDataView, //
			final DataAccessLogic dataAccessLogic, //
			final ExportPolicy exportStrategy) {

		this.bimPersistence = bimPersistence;
		this.bimServiceFacade = bimServiceFacade;
		this.bimDataModelManager = bimDataModelManager;
		this.mapper = mapper;
		this.exportConnector = new NewExportConnector(bimDataView, bimServiceFacade, bimPersistence, exportStrategy, dataAccessLogic);
		this.bimDataView = bimDataView;
		this.dataAccessLogic = dataAccessLogic;
		this.exportPolicy = exportStrategy;
	}

	@Override
	public Iterable<Project> readAllProjects() {
		final Iterable<CmProject> cmProjectList = bimPersistence.readAll();
		final Iterable<Project> projectList = listFrom(cmProjectList);
		return projectList;
	}

	@Override
	public Project createProject(final Project project) {

		final BimFacadeProject bimProject = bimProjectfrom(project);
		final BimFacadeProject baseProject = bimServiceFacade.createProjectAndUploadFile(bimProject);
		final String projectId = baseProject.getProjectId();
		final String exportProjectId = exportPolicy.createProjectForExport(projectId);

		final CmProject cmProject = cmProjectFrom(project);
		cmProject.setProjectId(projectId);
		cmProject.setLastCheckin(baseProject.getLastCheckin());
		cmProject.setSynch(project.isSynch());
		cmProject.setExportProjectId(exportProjectId);
		bimPersistence.saveProject(cmProject);

		final Project result = from(cmProject);
		return result;
	}

	@Override
	public void disableProject(final Project project) {
		final BimFacadeProject bimProject = bimProjectfrom(project);
		bimServiceFacade.disableProject(bimProject);

		final CmProject persistenceProject = cmProjectFrom(project);
		bimPersistence.disableProject(persistenceProject);
	}

	@Override
	public void enableProject(final Project project) {
		final BimFacadeProject bimProject = bimProjectfrom(project);
		bimServiceFacade.enableProject(bimProject);

		final CmProject persistenceProject = cmProjectFrom(project);
		bimPersistence.enableProject(persistenceProject);
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
		if (project.getFile() != null) {
			final String exportProjectId = exportPolicy.updateProjectForExport(projectId);
			persistenceProject.setExportProjectId(exportProjectId);
		}
		bimPersistence.saveProject(persistenceProject);
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
	public List<BimLayer> readLayers() {
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
		final List<BimLayer> storedLayers = (List<BimLayer>) bimPersistence.listLayers();
		for (final BimLayer layer : storedLayers) {
			out.put(layer.getClassName(), layer);
		}
		return out;
	}

	@Override
	public void updateBimLayer(final String className, final String attributeName, final String value) {

		final BimDataModelCommandFactory factory = new BimDataModelCommandFactory(bimPersistence, //
				bimDataModelManager);
		final BimDataModelCommand dataModelCommand = factory.create(attributeName);
		dataModelCommand.execute(className, value);
	}

	@Override
	public String getDescriptionOfRoot(final Long cardId, final String className) {
		final Long rootId = getRootId(cardId, className);
		final BimLayer rootLayer = bimPersistence.findRoot();
		if (rootLayer == null || rootLayer.getClassName() == null || rootLayer.getClassName().isEmpty()) {
			throw new BimError("Root layer not configured");
		}
		final Card rootCard = dataAccessLogic.fetchCard(rootLayer.getClassName(), rootId);
		final String description = String.class.cast(rootCard.getAttribute(DESCRIPTION_ATTRIBUTE));
		return description;
	}

	private Long getRootId(final Long cardId, final String className) {
		Long rootId = null;
		final BimLayer rootLayer = bimPersistence.findRoot();
		if (rootLayer == null || rootLayer.getClassName() == null || rootLayer.getClassName().isEmpty()) {
			throw new BimError("Root layer not configured");
		}
		if (className.equals(rootLayer.getClassName())) {
			rootId = cardId;
		} else {
			final BimLayer layer = bimPersistence.readLayer(className);
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
	public String getBaseRevisionIdForViewer(final Long cardId, final String className) {
		final String baseProjectId = getBaseProjectIdForCardOfClass(cardId, className);
		final String revisionId = getLastRevisionOfProject(baseProjectId);
		return revisionId;
	}

	@Override
	public String getExportedRevisionIdForViewer(final Long cardId, final String className) {
		final String baseProjectId = getBaseProjectIdForCardOfClass(cardId, className);
		String outputRevisionId = StringUtils.EMPTY;
		if (isValidId(baseProjectId)) {
			if (exportPolicy.forceUpdate()) {
				exportIfc(baseProjectId);
			}
			outputRevisionId = exportConnector.getLastGeneratedOutput(baseProjectId);
		}
		return outputRevisionId;
	}

	@Override
	public String getBaseProjectId(final Long cardId, final String className) {
		return getBaseProjectIdForCardOfClass(cardId, className);
	}

	private String getBaseProjectIdForCardOfClass(final Long cardId, final String className) {
		final Long rootId = getRootId(cardId, className);
		final BimLayer rootLayer = bimPersistence.findRoot();
		final String baseProjectId = getProjectIdForRootClass(rootId, rootLayer.getClassName());
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
				projectId = bimPersistence.getProjectIdFromCardId(projectCardId);
			}
		}
		return projectId;
	}

	private Long getRootCardIdForProjectId(final String projectId) {
		
		final String rootClassName = bimPersistence.findRoot().getClassName();
		
		Long cardId = (long) -1;

		final List<CMCard> cards = bimDataView.getCardsWithAttributeAndValue(DBIdentifier.fromName("_BimProject"),
				projectId, "ProjectId");
		if (cards.isEmpty() || cards.size() != 1) {
			throw new BimError("Something is wrong for projectId " + projectId);
		}
		final CMCard projectCard = cards.get(0);
		final CMDomain domain = dataAccessLogic.findDomain(rootClassName + DEFAULT_DOMAIN_SUFFIX);

		final Card src = dataAccessLogic.fetchCard("_BimProject", projectCard.getId());

		final DomainWithSource dom = DomainWithSource.create(domain.getId(), "_2");
		final GetRelationListResponse domains = dataAccessLogic.getRelationList(src, dom);
		Object first = firstElement(domains);
		if (first != null) {
			final DomainInfo firstDomain = (DomainInfo) first;
			first = firstElement(firstDomain);
			if (first != null) {
				final RelationInfo firstRelation = (RelationInfo) first;
				cardId = firstRelation.getRelation().getCard2Id();
			}
		}
		return cardId;
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

		final CmProject project = bimPersistence.read(projectId);
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
		bimPersistence.saveProject(projectSynchronized);
	}

	@Override
	public void exportIfc(final String projectId) {
		exportConnector.export(projectId, new DefaultExportListener(bimServiceFacade, exportPolicy));
	}

	@Override
	public DataHandler download(final String projectId) {
		return bimServiceFacade.download(projectId);
	}

	@Override
	public BimLayer getRootLayer() {
		return bimPersistence.findRoot();
	}

	@Override
	public BimCard fetchCardDataFromObjectId(final String objectId, final String revisionId) {
		final String globalId = bimServiceFacade.fetchGlobalIdFromObjectId(objectId, revisionId);
		final BimCard bimCard = bimDataView.getBimDataFromGlobalid(globalId);
		return bimCard;
	}

	@Override
	public String getJsonForBimViewer(final String revisionId, final String baseProjectId) {
		System.out.println("Open revision " + revisionId);
		final DataHandler jsonFile = bimServiceFacade.fetchProjectStructure(revisionId);
		try {
			final Long rootCardId = getRootCardIdForProjectId(baseProjectId);
			final Reader reader = new InputStreamReader(jsonFile.getInputStream(), "UTF-8");
			final BufferedReader fileReader = new BufferedReader(reader);
			final ObjectMapper mapper = new ObjectMapper();
			final JsonNode rootNode = mapper.readTree(fileReader);
			final JsonNode data = rootNode.findValue("data");
			final JsonNode properties = data.findValue("properties");
			final List<BimLayer> layers = readLayers();
			for (final BimLayer layer : layers) {
				final String className = layer.getClassName();
				System.out.println("Layer " + className);
				final String rootClassName = getRootLayer().getClassName();
				List<BimCard> bimCards = Lists.newArrayList();
				if (className.equals(rootClassName)) {
					bimCards = bimDataView.getBimCardsWithGivenValueOfRootReferenceAttribute(className, null, null);
				} else {
					final String rootReferenceName = layer.getRootReference();
					if (StringUtils.isNotBlank(rootReferenceName)) {
						bimCards = bimDataView.getBimCardsWithGivenValueOfRootReferenceAttribute(className, rootCardId,
								rootReferenceName);
					}
				}
				for (final BimCard bimCard : bimCards) {
					final String guid = bimCard.getGlobalId();
					System.out.println("guid " + guid);
					Long oid = (long) -1;
					if (guidToOidMap.containsKey(revisionId)) {
						final Map<String, Long> revisionMap = guidToOidMap.get(revisionId);
						if (revisionMap.containsKey(guid)) {
							oid = revisionMap.get(guid);
						}
					} else {
						oid = bimServiceFacade.getOidFromGlobalId(guid, revisionId, Lists.newArrayList("IfcBuilding",
								"IfcBuildingStorey", "IfcSpace", IFC_BUILDING_ELEMENT_PROXY, IFC_FURNISHING));
						if (guidToOidMap.containsKey(revisionId)) {
							final Map<String, Long> revisionMap = guidToOidMap.get(revisionId);
							revisionMap.put(guid, oid);
						} else {
							final Map<String, Long> revisionMap = Maps.newHashMap();
							revisionMap.put(guid, oid);
						}
					}
					System.out.println("oid " + oid);
					final String oidAsString = String.valueOf(oid);
					if (isValidId(oidAsString)) {
						final ObjectNode property = (ObjectNode) properties.findValue(oidAsString);
						final ObjectNode cmdbuildData = mapper.createObjectNode();
						cmdbuildData.put(CARDID_FIELD_NAME, bimCard.getId());
						cmdbuildData.put(CLASSID_FIELD_NAME, bimCard.getClassId());
						cmdbuildData.put(CLASSNAME_FIELD_NAME, bimCard.getClassName());
						cmdbuildData.put(CARD_DESCRIPTION_FIELD_NAME, bimCard.getCardDescription());
						property.put("cmdbuild_data", cmdbuildData);
					}
				}
			}
			return rootNode.toString();
		} catch (final Throwable t) {
			throw new BimError("Cannot read the Json", t);
		}
	}

	@Override
	public boolean getActiveForClassname(final String classname) {
		return bimPersistence.isActiveLayer(classname);
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

}