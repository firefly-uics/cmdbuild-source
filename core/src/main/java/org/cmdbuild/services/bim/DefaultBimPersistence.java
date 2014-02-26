package org.cmdbuild.services.bim;

import java.util.List;

import org.cmdbuild.data.converter.StorableProjectConverter;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.StorableProject;
import org.cmdbuild.services.bim.RelationPersistence.ProjectRelations;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class DefaultBimPersistence implements BimPersistence {

	public static final String DEFAULT_DOMAIN_SUFFIX = StorableProjectConverter.TABLE_NAME;
	private final RelationPersistence relationPersistenceManager;
	private final BimStoreManager storeManager;

	public DefaultBimPersistence(BimStoreManager storeManager, RelationPersistence relationPersistence) {
		this.relationPersistenceManager = relationPersistence;
		this.storeManager = storeManager;
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
		CmProject cmProject = from(storableProject, relations);
		return cmProject;
	}

	@Override
	public void saveProject(CmProject project) {
		final StorableProject projectToStore = PROJECT_TO_STORABLE.apply(project);
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

	@Override
	public String getProjectIdFromCardId(final Long cardId) {
		Iterable<CmProject> projectList = readAll();
		for (CmProject project : projectList) {
			if (project.getCmId().equals(cardId)) {
				return project.getProjectId();
			}
		}
		return null;
	}

	@Override
	public Long getCardIdFromProjectId(String projectId) {
		Long cardId = new Long("-1");
		CmProject project = read(projectId);
		if (project != null) {
			cardId = project.getCmId();
		}
		return cardId;
	}

	private static CmProject from(final StorableProject storableProject, ProjectRelations relations) {
		return new CmProjectAsWrapper(storableProject, relations);
	}

	private static Function<CmProject, StorableProject> PROJECT_TO_STORABLE = new Function<CmProject, StorableProject>() {

		@Override
		public StorableProject apply(CmProject input) {
			StorableProject storableProject = new StorableProject();
			storableProject.setActive(input.isActive());
			storableProject.setDescription(input.getDescription());
			storableProject.setName(input.getName());
			storableProject.setLastCheckin(input.getLastCheckin());
			storableProject.setImportMapping(input.getImportMapping());
			storableProject.setExportMapping(input.getExportMapping());
			storableProject.setProjectId(input.getProjectId());
			return storableProject;
		}
	};

	private static class CmProjectAsWrapper implements CmProject {

		private final StorableProject delegate;
		private Iterable<String> cardBinding = Lists.newArrayList();

		public CmProjectAsWrapper(final StorableProject delegate, final ProjectRelations relations) {
			this.delegate = delegate;
			if (relations != null) {
				this.cardBinding = relations.getBindedCards();
			}
		}

		@Override
		public String getProjectId() {
			return delegate.getProjectId();
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public String getDescription() {
			return delegate.getDescription();
		}

		@Override
		public Long getCmId() {
			return delegate.getCardId();
		}

		@Override
		public boolean isActive() {
			return delegate.isActive();
		}

		@Override
		public boolean isSynch() {
			return delegate.isSynch();
		}

		@Override
		public String getImportMapping() {
			return delegate.getImportMapping();
		}

		@Override
		public String getExportMapping() {
			return delegate.getExportMapping();
		}

		@Override
		public DateTime getLastCheckin() {
			return delegate.getLastCheckin();
		}

		@Override
		public Iterable<String> getCardBinding() {
			return cardBinding;
		}

		@Override
		public void setSynch(boolean synch) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setProjectId(String projectId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setLastCheckin(DateTime lastCheckin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setName(String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setDescription(String description) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setCardBinding(Iterable<String> cardBinding) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setActive(boolean active) {
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public Iterable<BimLayer> listLayers() {
		return storeManager.readAllLayers();
	}

	@Override
	public void saveActiveStatus(String className, String value) {
		storeManager.saveActiveStatus(className, value);
	}

	@Override
	public void saveExportStatus(String className, String value) {
		storeManager.saveExportStatus(className, value);
	}

	@Override
	public void saveContainerStatus(String className, String value) {
		storeManager.saveContainerStatus(className, value);
	}

	@Override
	public void saveRoot(String className, boolean value) {
		storeManager.saveRoot(className, value);
	}

	@Override
	public BimLayer findRoot() {
		return storeManager.findRoot();
	}

	@Override
	public BimLayer findContainer() {
		return storeManager.findContainer();
	}

	@Override
	public boolean isActiveLayer(final String classname) {
		return storeManager.isActive(classname);
	}

	@Override
	public String getContainerClassName() {
		return storeManager.getContainerClassName();
	}

}
