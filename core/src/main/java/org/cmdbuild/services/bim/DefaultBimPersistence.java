package org.cmdbuild.services.bim;

import java.util.List;
import java.util.Map;

import org.cmdbuild.data.converter.StorableProjectConverter;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.StorableProject;
import org.cmdbuild.services.bim.RelationPersistence.ProjectRelations;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class DefaultBimPersistence implements BimPersistence {

	public static final String DEFAULT_DOMAIN_SUFFIX = StorableProjectConverter.TABLE_NAME;
	private final RelationPersistence relationPersistenceManager;
	private final BimStoreManager storeManager;

	public DefaultBimPersistence(final BimStoreManager storeManager, final RelationPersistence relationPersistence) {
		this.relationPersistenceManager = relationPersistence;
		this.storeManager = storeManager;
	}

	@Override
	public Iterable<CmProject> readAll() {
		final Iterable<StorableProject> storableList = storeManager.readAll();
		final List<CmProject> cmProjectList = Lists.newArrayList();
		for (final StorableProject storable : storableList) {
			final CmProject cmProject = read(storable.getIdentifier());
			cmProjectList.add(cmProject);
		}
		return cmProjectList;
	}

	@Override
	public CmProject read(final String projectId) {
		final StorableProject storableProject = storeManager.read(projectId);
		final ProjectRelations relations = relationPersistenceManager.readRelations(storableProject.getCardId(),
				findRoot().getClassName());
		final CmProject cmProject = from(storableProject, relations);
		return cmProject;
	}

	@Override
	public void saveProject(final CmProject project) {
		final StorableProject projectToStore = PROJECT_TO_STORABLE.apply(project);
		storeManager.write(projectToStore);

		final StorableProject storedProject = storeManager.read(project.getProjectId());
		if (storedProject != null) {
			final String rootClassName = findRoot().getClassName();
			final Long cardId = storedProject.getCardId();
			final Iterable<String> oldRelations = relationPersistenceManager.readRelations(cardId,
					rootClassName).getBindedCards();
			final Iterable<String> newRelations = project.getCardBinding();
			
			final Function<String, String> keyForMap = new Function<String, String>() {
				public String apply(String from) {
					return from; // or something else
				}
			};
			final Map<String, String> oldRelationsMap = Maps.uniqueIndex(oldRelations, keyForMap);
			final Map<String, String> newRelationsMap = Maps.uniqueIndex(newRelations, keyForMap);
			final MapDifference<String, String> difference = Maps.difference(oldRelationsMap, newRelationsMap);
			if(!difference.areEqual()){
				relationPersistenceManager.writeRelations(cardId, newRelations, rootClassName);
			}
			
		}
	}

	@Override
	public void disableProject(final CmProject persistenceProject) {
		storeManager.disableProject(persistenceProject.getProjectId());
	}

	@Override
	public void enableProject(final CmProject persistenceProject) {
		storeManager.enableProject(persistenceProject.getProjectId());
	}

	@Override
	public String getProjectIdFromCardId(final Long cardId) {
		final Iterable<CmProject> projectList = readAll();
		for (final CmProject project : projectList) {
			if (project.getCmId().equals(cardId)) {
				return project.getProjectId();
			}
		}
		return null;
	}

	@Override
	public Long getCardIdFromProjectId(final String projectId) {
		Long cardId = new Long("-1");
		final CmProject project = read(projectId);
		if (project != null) {
			cardId = project.getCmId();
		}
		return cardId;
	}

	private static CmProject from(final StorableProject storableProject, final ProjectRelations relations) {
		return new StorableAndRelations(storableProject, relations);
	}

	private static Function<CmProject, StorableProject> PROJECT_TO_STORABLE = new Function<CmProject, StorableProject>() {

		@Override
		public StorableProject apply(final CmProject input) {
			final StorableProject storableProject = new StorableProject();
			storableProject.setActive(input.isActive());
			storableProject.setDescription(input.getDescription());
			storableProject.setName(input.getName());
			storableProject.setLastCheckin(input.getLastCheckin());
			storableProject.setImportMapping(input.getImportMapping());
			storableProject.setExportMapping(input.getExportMapping());
			storableProject.setExportProjectId(input.getExportProjectId());
			storableProject.setProjectId(input.getProjectId());
			return storableProject;
		}
	};

	private static class StorableAndRelations implements CmProject {

		private final StorableProject delegate;
		private Iterable<String> cardBinding = Lists.newArrayList();

		public StorableAndRelations(final StorableProject delegate, final ProjectRelations relations) {
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
		public String getShapeProjectId() {
			return delegate.getShapeProjectId();
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
		public String getExportProjectId() {
			return delegate.getExportProjectId();
		}

		@Override
		public void setSynch(final boolean synch) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setProjectId(final String projectId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setLastCheckin(final DateTime lastCheckin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setName(final String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setDescription(final String description) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setCardBinding(final Iterable<String> cardBinding) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setActive(final boolean active) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setExportProjectId(final String projectId) {
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public Iterable<BimLayer> listLayers() {
		return storeManager.readAllLayers();
	}

	@Override
	public void saveActiveFlag(final String className, final String value) {
		storeManager.saveActiveStatus(className, value);
	}

	@Override
	public void saveExportFlag(final String className, final String value) {
		storeManager.saveExportStatus(className, value);
	}

	@Override
	public void saveContainerFlag(final String className, final String value) {
		storeManager.saveContainerStatus(className, value);
	}

	@Override
	public void saveRootFlag(final String className, final boolean value) {
		storeManager.saveRoot(className, value);
	}
	

	@Override
	public void saveRootReferenceName(String className, String value) {
		storeManager.saveRootReference(className, value);
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

	@Override
	public BimLayer readLayer(final String className) {
		return storeManager.readLayer(className);
	}


}
