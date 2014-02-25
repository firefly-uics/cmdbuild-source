package org.cmdbuild.services.bim;

import java.util.List;
import java.util.NoSuchElementException;

import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.StorableProjectConverter;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.StorableProject;
import org.cmdbuild.services.bim.RelationPersistence.ProjectRelations;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class DefaultBimDataPersistence implements BimDataPersistence {

	public static final String DEFAULT_DOMAIN_SUFFIX = StorableProjectConverter.TABLE_NAME;
	private final Store<BimLayer> layerStore;
	private final RelationPersistence relationPersistenceManager;
	private final BimStoreManager storeManager;

	public DefaultBimDataPersistence(Store<StorableProject> projectInfoStore, Store<BimLayer> layerStore,
			CMDataView dataView) {
		this.layerStore = layerStore;
		this.relationPersistenceManager = new DefaultRelationPersistence(dataView);
		this.storeManager = new DefaultBimStoreManager(projectInfoStore, layerStore);
	}

	@Override
	public Iterable<CmProject> readAll() {
		final Iterable<StorableProject> storableList = storeManager.readAll();
		final List<CmProject> cmProjectList = Lists.newArrayList();
		for (StorableProject storable : storableList) {
			final CmProject cmProject = read(storable.getIdentifier());
			cmProjectList.add(cmProject);
		}
		return cmProjectList;
	}

	@Override
	public CmProject read(final String projectId) {
		StorableProject storableProject = storeManager.read(projectId);
		ProjectRelations relations = relationPersistenceManager.readRelations(storableProject.getCardId(), findRoot()
				.getClassName());
		CmProject cmProject = STORABLE_TO_CM.apply(storableProject);
		cmProject = setRelations(cmProject, relations);
		return cmProject;
	}
	
	@Override
	public void saveProject(CmProject project) {
		final StorableProject projectToStore = from(project);
		storeManager.write(projectToStore);

		final StorableProject storedProject = storeManager.read(project.getProjectId());
		if (storedProject != null) {
			final String className = findRoot().getClassName();
			relationPersistenceManager.writeRelations(storedProject.getCardId(), project.getCardBinding(), className);
		}
	}
	
	@Override
	public void disableProject(CmProject persistenceProject) {
		storeManager.disableProject(persistenceProject.getProjectId());
	}

	@Override
	public void enableProject(CmProject persistenceProject) {
		storeManager.enableProject(persistenceProject.getProjectId());
	}

	private static Function<StorableProject, DefaultCmProject> STORABLE_TO_CM = new Function<StorableProject, DefaultCmProject>() {

		@Override
		public DefaultCmProject apply(final StorableProject input) {
			return new DefaultCmProject() {
				{
					setProjectId(input.getProjectId());
					setName(input.getName());
					setDescription(input.getDescription());
					setActive(input.isActive());
					setLastCheckin(input.getLastCheckin());
					setSynch(input.isSynch());
				}
			};
		}

	};

	private static class DefaultCmProject implements CmProject {

		private String projectId, name, description, importMapping, exportMapping;
		private boolean active, synch;
		private DateTime lastCheckin;
		private Iterable<String> cardBinding = Lists.newArrayList();
		private Long cardId;

		@Override
		public String getProjectId() {
			return projectId;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public Long getCmId() {
			return cardId;
		}

		@Override
		public boolean isActive() {
			return active;
		}

		@Override
		public boolean isSynch() {
			return synch;
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
		public DateTime getLastCheckin() {
			return lastCheckin;
		}

		@Override
		public Iterable<String> getCardBinding() {
			return cardBinding;
		}

		@Override
		public void setSynch(boolean synch) {
			this.synch = synch;
		}

		@Override
		public void setProjectId(String projectId) {
			this.projectId = projectId;
		}

		@Override
		public void setLastCheckin(DateTime lastCheckin) {
			this.lastCheckin = lastCheckin;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public void setDescription(String description) {
			this.description = description;
		}

		@Override
		public void setCardBinding(Iterable<String> cardBinding) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setActive(boolean active) {
			this.active = active;
		}

	}



	private StorableProject from(CmProject project) {
		StorableProject storableProject = new StorableProject();
		storableProject.setActive(project.isActive());
		storableProject.setDescription(project.getDescription());
		storableProject.setName(project.getName());
		storableProject.setLastCheckin(project.getLastCheckin());
		storableProject.setImportMapping(project.getImportMapping());
		storableProject.setExportMapping(project.getExportMapping());
		storableProject.setProjectId(project.getProjectId());
		return storableProject;
	}
	
	private static CmProject setRelations(CmProject cmProject, ProjectRelations bindedCards) {
		((List<String>) cmProject.getCardBinding()).clear();
		for (String cardId : bindedCards.getBindedCards()) {
			((List<String>) cmProject.getCardBinding()).add(cardId);
		}
		return cmProject;
	}


	
	
	@Override
	public List<BimLayer> listLayers() {
		return layerStore.list();
	}

	@Override
	public void saveActiveStatus(String className, String value) {
		BimLayer layerForClass = fetchLayer(className);
		if (layerForClass == null) {
			layerForClass = new BimLayer(className);
			layerForClass.setActive(Boolean.parseBoolean(value));
			layerStore.create(layerForClass);
		} else {
			layerForClass.setActive(Boolean.parseBoolean(value));
			layerStore.update(layerForClass);
		}
	}

	@Override
	public void saveExportStatus(String className, String value) {
		BimLayer layerForClass = fetchLayer(className);
		boolean exportValue = Boolean.parseBoolean(value);
		if (layerForClass == null) {
			layerForClass = new BimLayer(className);
			layerForClass.setExport(exportValue);
			layerStore.create(layerForClass);
		} else {
			layerForClass.setExport(exportValue);
			layerStore.update(layerForClass);
		}
	}

	@Override
	public void saveContainerStatus(String className, String value) {
		BimLayer layerForClass = fetchLayer(className);
		boolean containerValue = Boolean.parseBoolean(value);
		if (layerForClass == null) {
			layerForClass = new BimLayer(className);
			layerForClass.setContainer(containerValue);
			layerStore.create(layerForClass);
		} else {
			layerForClass.setContainer(containerValue);
			layerStore.update(layerForClass);
		}

	}

	@Override
	public void saveRoot(String className, boolean value) {
		BimLayer layer = fetchLayer(className);
		if (layer == null) {
			layer = new BimLayer(className);
			layer.setRoot(value);
			layerStore.create(layer);
		} else {
			layer.setRoot(value);
			layerStore.update(layer);
		}
	}

	@Override
	public BimLayer findRoot() {
		List<BimLayer> layerList = layerStore.list();
		for (BimLayer layer : layerList) {
			if (layer.isRoot()) {
				return layer;
			}
		}
		return null;
	}

	@Override
	public BimLayer findContainer() {
		List<BimLayer> layerList = layerStore.list();
		for (BimLayer layer : layerList) {
			if (layer.isContainer()) {
				return layer;
			}
		}
		return null;
	}

	@Deprecated
	private BimLayer fetchLayer(final String className) {
		try {
			return layerStore.read(storableWithId(className));
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	private Storable storableWithId(final String identifier) {
		return new Storable() {

			@Override
			public String getIdentifier() {
				return identifier;
			}

		};
	}

	@Override
	public String getProjectIdFromCardId(Long cardId) {
		Iterable<CmProject> projectList = readAll();
		for (CmProject projectInfo : projectList) {
			if (projectInfo.getCmId().equals(cardId)) {
				return projectInfo.getProjectId();
			}
		}
		return null;
	}

	@Override
	public boolean getActiveForClassname(String classname) {
		boolean response = false;
		BimLayer layer = fetchLayer(classname);
		if (layer != null) {
			response = layer.isActive();
		}
		return response;
	}

	@Override
	public String getContainerClassName() {
		BimLayer containerLayer = findContainer();
		if (containerLayer == null) {
			throw new BimError("Container layer not configured");
		} else {
			return containerLayer.getClassName();
		}
	}

	@Override
	public Long getCardIdFromProjectId(String projectId) {
		Long cardId = new Long("-1");
		CmProject projectInfo = read(projectId);
		if (projectInfo != null) {
			cardId = projectInfo.getCmId();
		}
		return cardId;
	}

}
