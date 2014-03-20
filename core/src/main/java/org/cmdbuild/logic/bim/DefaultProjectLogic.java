package org.cmdbuild.logic.bim;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimFacade.BimFacadeProject;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.CmProject;
import org.cmdbuild.services.bim.connector.export.ExportPolicy;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;

public class DefaultProjectLogic implements ProjectLogic {

	private final BimFacade bimServiceFacade;
	private final BimPersistence bimPersistence;
	private final ExportPolicy exportPolicy;

	public DefaultProjectLogic( //
			final BimFacade bimServiceFacade, //
			final BimPersistence bimPersistence, //
			final ExportPolicy exportStrategy) {

		this.bimPersistence = bimPersistence;
		this.bimServiceFacade = bimServiceFacade;
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
	public DataHandler download(final String projectId) {
		return bimServiceFacade.download(projectId);
	}

	// FIXME
	public static Project from(final CmProject createdPersistenceProject) {
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

	// FIXME
	public static CmProject cmProjectFrom(final Project project) {
		final CmProject cmProject = new DefaultCmProject();
		cmProject.setName(project.getName());
		cmProject.setDescription(project.getDescription());
		cmProject.setCardBinding(project.getCardBinding());
		cmProject.setActive(project.isActive());
		cmProject.setProjectId(project.getProjectId());
		return cmProject;
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

}
