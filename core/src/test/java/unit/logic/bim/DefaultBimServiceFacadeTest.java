package unit.logic.bim;

import static org.junit.Assert.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.model.bim.BimProjectInfo;
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
		serviceFacade.create(PROJECT_NAME);

		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).connect();
		inOrder.verify(service).login();
		inOrder.verify(service).createProject(PROJECT_NAME);
		inOrder.verify(service).logout();

		verifyNoMoreInteractions(service);
	}

	@Test
	public void projectDisabled() throws Exception {
		// given
		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setActive(false);
		projectInfo.setProjectId(PROJECTID);

		BimProject project = mock(BimProject.class);
		when(project.getIdentifier()).thenReturn(PROJECTID);
		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);
		when(project.isActive()).thenReturn(true);

		// when
		serviceFacade.update(projectInfo);

		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).connect();
		inOrder.verify(service).login();
		inOrder.verify(service).getProjectByPoid(PROJECTID);
		inOrder.verify(service).disableProject(PROJECTID);
		inOrder.verify(service).logout();

		verifyNoMoreInteractions(service);
	}

	@Test
	public void projectEnabled() throws Exception {
		// given
		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setActive(true);
		projectInfo.setProjectId(PROJECTID);

		BimProject project = mock(BimProject.class);
		when(project.getIdentifier()).thenReturn(PROJECTID);
		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);
		when(project.isActive()).thenReturn(false);

		// when
		serviceFacade.update(projectInfo);

		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).connect();
		inOrder.verify(service).login();
		inOrder.verify(service).getProjectByPoid(PROJECTID);
		inOrder.verify(service).enableProject(PROJECTID);
		inOrder.verify(service).logout();

		verifyNoMoreInteractions(service);
	}

	@Test
	public void projectStatusTrueNotModified() throws Exception {
		// given
		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setActive(true);
		projectInfo.setProjectId(PROJECTID);

		BimProject project = mock(BimProject.class);
		when(project.getIdentifier()).thenReturn(PROJECTID);
		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);
		when(project.isActive()).thenReturn(true);

		// when
		serviceFacade.update(projectInfo);

		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).connect();
		inOrder.verify(service).login();
		inOrder.verify(service).getProjectByPoid(PROJECTID);
		inOrder.verify(service).logout();

		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void projectStatusFalseNotModified() throws Exception {
		// given
		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setActive(false);
		projectInfo.setProjectId(PROJECTID);

		BimProject project = mock(BimProject.class);
		when(project.getIdentifier()).thenReturn(PROJECTID);
		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);
		when(project.isActive()).thenReturn(false);

		// when
		serviceFacade.update(projectInfo);

		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).connect();
		inOrder.verify(service).login();
		inOrder.verify(service).getProjectByPoid(PROJECTID);
		inOrder.verify(service).logout();

		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void newRevisionLoaded() throws Exception {
		
	}
	
	

}
