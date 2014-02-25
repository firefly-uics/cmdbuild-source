package unit.logic.bim;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.model.bim.StorableProject;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.cmdbuild.services.bim.DefaultBimServiceFacade;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class DefaultBimServiceFacadeTest {

	private BimService service;
	private BimServiceFacade serviceFacade;

	private static final String PROJECTID = "123";
	private static final String PROJECT_NAME = "projectName";
	private static final String REVISIONID = "456";

	@Before
	public void setUp() throws Exception {
		service = mock(BimService.class);
		serviceFacade = new DefaultBimServiceFacade(service);
	}

	@Test
	public void projectWithNameCreated() throws Exception {
		// given
		BimProject project = mock(BimProject.class);
		when(project.getIdentifier()).thenReturn(PROJECTID);
		when(service.createProject(PROJECT_NAME)).thenReturn(project);

		// when
	//	serviceFacade.createProject(PROJECT_NAME);

		// then
		InOrder inOrder = inOrder(service, project);
		inOrder.verify(service).createProject(PROJECT_NAME);
		inOrder.verify(project).getIdentifier();

		verifyNoMoreInteractions(service, project);
	}

	@Test
	public void projectDisabled() throws Exception {
		// given
		StorableProject projectInfo = new StorableProject();
		projectInfo.setActive(false);
		projectInfo.setProjectId(PROJECTID);

		BimProject project = mock(BimProject.class);
		when(project.getIdentifier()).thenReturn(PROJECTID);
		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);
		when(project.isActive()).thenReturn(true);

		// when
		serviceFacade.updateProject(projectInfo);

		// then
		InOrder inOrder = inOrder(service, project);
		inOrder.verify(service).getProjectByPoid(PROJECTID);
		inOrder.verify(project).isActive();
		inOrder.verify(service).disableProject(PROJECTID);

		verifyNoMoreInteractions(service, project);
	}

	@Test
	public void projectEnabled() throws Exception {
		// given
		StorableProject projectInfo = new StorableProject();
		projectInfo.setActive(true);
		projectInfo.setProjectId(PROJECTID);

		BimProject project = mock(BimProject.class);
		when(project.getIdentifier()).thenReturn(PROJECTID);
		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);
		when(project.isActive()).thenReturn(false);

		// when
		serviceFacade.updateProject(projectInfo);

		// then
		InOrder inOrder = inOrder(service, project);
		inOrder.verify(service).getProjectByPoid(PROJECTID);
		inOrder.verify(project).isActive();
		inOrder.verify(service).enableProject(PROJECTID);

		verifyNoMoreInteractions(service, project);
	}

	@Test
	public void projectStatusTrueNotModified() throws Exception {
		// given
		StorableProject projectInfo = new StorableProject();
		projectInfo.setActive(true);
		projectInfo.setProjectId(PROJECTID);

		BimProject project = mock(BimProject.class);
		when(project.getIdentifier()).thenReturn(PROJECTID);
		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);
		when(project.isActive()).thenReturn(true);

		// when
		serviceFacade.updateProject(projectInfo);

		// then
		InOrder inOrder = inOrder(service, project);
		inOrder.verify(service).getProjectByPoid(PROJECTID);
		inOrder.verify(project).isActive();

		verifyNoMoreInteractions(service, project);
	}

	@Test
	public void projectStatusFalseNotModified() throws Exception {
		// given
		StorableProject projectInfo = new StorableProject();
		projectInfo.setActive(false);
		projectInfo.setProjectId(PROJECTID);

		BimProject project = mock(BimProject.class);
		when(project.getIdentifier()).thenReturn(PROJECTID);
		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);
		when(project.isActive()).thenReturn(false);

		// when
		serviceFacade.updateProject(projectInfo);

		// then
		InOrder inOrder = inOrder(service, project);
		inOrder.verify(service).getProjectByPoid(PROJECTID);
		inOrder.verify(project).isActive();

		verifyNoMoreInteractions(service, project);
	}

	@Test
	public void newRevisionLoadedAndStatusNotChanged() throws Exception {
		// given
		StorableProject projectInfo = new StorableProject();
		projectInfo.setActive(true);
		projectInfo.setProjectId(PROJECTID);

		final File ifcFile = File.createTempFile("ifc", null, FileUtils.getTempDirectory());
		ifcFile.deleteOnExit();

		BimProject project = mock(BimProject.class);
		when(project.getIdentifier()).thenReturn(PROJECTID);
		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);
		when(project.isActive()).thenReturn(true);
		when(project.getLastRevisionId()).thenReturn(REVISIONID);

		BimRevision revision = mock(BimRevision.class);
		Date date = new Date();
		when(revision.getDate()).thenReturn(date);

		when(service.getRevision(REVISIONID)).thenReturn(revision);

		// when
		serviceFacade.updateProject(projectInfo, ifcFile);

		// then
		InOrder inOrder = inOrder(service, project, revision);
		inOrder.verify(service).getProjectByPoid(PROJECTID);
		inOrder.verify(project).isActive();
		inOrder.verify(service).checkin(PROJECTID, ifcFile);
		inOrder.verify(service).getProjectByPoid(PROJECTID);
		inOrder.verify(project).getLastRevisionId();
		inOrder.verify(service).getRevision(REVISIONID);
		inOrder.verify(revision).getDate();

		verifyNoMoreInteractions(service, project, revision);
	}

	@Test
	public void newRevisionLoadedAndProjectStatusUpdated() throws Exception {
		// given
		StorableProject projectInfo = new StorableProject();
		projectInfo.setActive(true);
		projectInfo.setProjectId(PROJECTID);

		final File ifcFile = File.createTempFile("ifc", null, FileUtils.getTempDirectory());
		ifcFile.deleteOnExit();

		BimProject project = mock(BimProject.class);
		when(project.getIdentifier()).thenReturn(PROJECTID);
		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);
		when(project.isActive()).thenReturn(false);
		when(project.getLastRevisionId()).thenReturn(REVISIONID);

		BimRevision revision = mock(BimRevision.class);
		Date date = new Date();
		when(revision.getDate()).thenReturn(date);

		when(service.getRevision(REVISIONID)).thenReturn(revision);

		// when
		serviceFacade.updateProject(projectInfo, ifcFile);

		// then
		InOrder inOrder = inOrder(service, project, revision);
		inOrder.verify(service).getProjectByPoid(PROJECTID);
		inOrder.verify(project).isActive();
		inOrder.verify(service).enableProject(PROJECTID);
		inOrder.verify(service).checkin(PROJECTID, ifcFile);
		inOrder.verify(service).getProjectByPoid(PROJECTID);
		inOrder.verify(project).getLastRevisionId();
		inOrder.verify(service).getRevision(REVISIONID);
		inOrder.verify(revision).getDate();

		verifyNoMoreInteractions(service, project, revision);
	}

}
