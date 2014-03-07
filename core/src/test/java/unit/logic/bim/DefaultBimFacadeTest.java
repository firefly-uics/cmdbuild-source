package unit.logic.bim;

import static org.junit.Assert.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Date;

import javax.activation.DataHandler;

import static org.cmdbuild.bim.service.BimProject.INVALID_BIM_ID;

import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimFacade.BimFacadeProject;
import org.cmdbuild.services.bim.DefaultBimFacade;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class DefaultBimFacadeTest {

	private BimService service;
	private BimFacade serviceFacade;

	private static final String ID = "123";
	private static final String NAME = "projectName";
	private static final String REVISIONID = "456";
	private static final boolean STATUS = true;
	private static final String FILE = "pippo";

	@Before
	public void setUp() throws Exception {
		service = mock(BimService.class);
		serviceFacade = new DefaultBimFacade(service, null);
	}
	
	@Test
	public void getProjectByPoidForwardsToService() throws Exception {
		
		//when
		serviceFacade.getProjectById(ID);
		
		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).getProjectByPoid(ID);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void downloadProjectDoesNothingIfNoRevisions() throws Exception {
		//given
		BimProject project = mock(BimProject.class);
		when(project.getLastRevisionId()).thenReturn(INVALID_BIM_ID);
		when(service.getProjectByPoid(ID)).thenReturn(project);
		
		//when
		DataHandler download = serviceFacade.download(ID);
		
		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).getProjectByPoid(ID);
		verifyNoMoreInteractions(service);
		
		assertTrue(download == null);
	}
	
	@Test
	public void downloadProjectWhenThereIsARevision() throws Exception {
		//given
		BimProject project = mock(BimProject.class);
		when(project.getLastRevisionId()).thenReturn(REVISIONID);
		when(service.getProjectByPoid(ID)).thenReturn(project);
		DataHandler data = mock(DataHandler.class);
		when(service.downloadIfc(REVISIONID)).thenReturn(data);
	
		//when
		DataHandler download = serviceFacade.download(ID);
		
		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).getProjectByPoid(ID);
		inOrder.verify(service).downloadIfc(REVISIONID);
		verifyNoMoreInteractions(service);
		
		assertTrue(download.equals(data));
	}

	@Test
	public void createProjectWithNameAndStatus() throws Exception {
		// given
		BimFacadeProject projectToCreate = mock(BimFacadeProject.class);
		when(projectToCreate.getName()).thenReturn(NAME);
		when(projectToCreate.isActive()).thenReturn(STATUS);

		BimProject projectCreated = mock(BimProject.class);
		when(projectCreated.getIdentifier()).thenReturn(ID);
		when(projectCreated.getName()).thenReturn(NAME);
		when(projectCreated.isActive()).thenReturn(STATUS);
		when(service.createProject(NAME)).thenReturn(projectCreated);

		// when
		BimFacadeProject result = serviceFacade.createProject(projectToCreate);

		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).createProject(NAME);
		verifyNoMoreInteractions(service);

		assertTrue(result.getProjectId().equals(ID));
		assertTrue(result.getName().equals(NAME));
		assertTrue(result.isActive() == STATUS);
	}

	@Test
	public void createProjectAndLoadFile() throws Exception {
		// given
		BimFacadeProject projectToCreate = mock(BimFacadeProject.class);
		when(projectToCreate.getName()).thenReturn(NAME);
		when(projectToCreate.isActive()).thenReturn(STATUS);
		File file = new File(FILE);
		when(projectToCreate.getFile()).thenReturn(file);

		BimProject projectCreated = mock(BimProject.class);
		when(projectCreated.getIdentifier()).thenReturn(ID);
		when(projectCreated.getName()).thenReturn(NAME);
		when(projectCreated.isActive()).thenReturn(STATUS);
		when(projectCreated.getLastRevisionId()).thenReturn(REVISIONID);
		DateTime now = new DateTime();
		when(projectCreated.getLastCheckin()).thenReturn(now);

		BimRevision revision = mock(BimRevision.class);
		when(service.createProject(NAME)).thenReturn(projectCreated);
		when(service.checkin(ID, file)).thenReturn(now);
		when(service.getRevision(REVISIONID)).thenReturn(revision);
		when(service.getProjectByPoid(ID)).thenReturn(projectCreated);

		// when
		BimFacadeProject result = serviceFacade.createProject(projectToCreate);

		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).createProject(NAME);
		inOrder.verify(service).checkin(ID, file);
		inOrder.verify(service).getProjectByPoid(ID);
		inOrder.verify(service).getRevision(REVISIONID);
		verifyNoMoreInteractions(service);

		assertTrue(result.getProjectId().equals(ID));
		assertTrue(result.getName().equals(NAME));
		assertTrue(result.isActive() == STATUS);
		assertTrue(result.getLastCheckin().equals(now));
	}

	@Test
	public void projectDisabled() throws Exception {
		// given
		BimFacadeProject projectToDisable = mock(BimFacadeProject.class);
		when(projectToDisable.getProjectId()).thenReturn(ID);
		when(projectToDisable.getName()).thenReturn(NAME);
		when(projectToDisable.isActive()).thenReturn(STATUS);

		BimProject projectDisabled = mock(BimProject.class);
		when(projectDisabled.getIdentifier()).thenReturn(ID);
		when(projectDisabled.getName()).thenReturn(NAME);
		when(projectDisabled.isActive()).thenReturn(!STATUS);
		when(service.getProjectByPoid(ID)).thenReturn(projectDisabled);

		// when
		serviceFacade.disableProject(projectToDisable);

		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).disableProject(ID);
		verifyNoMoreInteractions(service);
	}

	@Test
	public void projectEnabled() throws Exception {
		// given
		BimFacadeProject projectToEnable = mock(BimFacadeProject.class);
		when(projectToEnable.getProjectId()).thenReturn(ID);
		when(projectToEnable.getName()).thenReturn(NAME);
		when(projectToEnable.isActive()).thenReturn(STATUS);

		BimProject projectDisabled = mock(BimProject.class);
		when(projectDisabled.getIdentifier()).thenReturn(ID);
		when(projectDisabled.getName()).thenReturn(NAME);
		when(projectDisabled.isActive()).thenReturn(!STATUS);
		when(service.getProjectByPoid(ID)).thenReturn(projectDisabled);

		// when
		serviceFacade.enableProject(projectToEnable);

		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).enableProject(ID);
		verifyNoMoreInteractions(service);
	}

	@Test
	public void updateProjectWithSameStatusAndNoFileDoesNothing() throws Exception {
		// given
		BimFacadeProject projectToUpdate = mock(BimFacadeProject.class);
		when(projectToUpdate.getProjectId()).thenReturn(ID);
		when(projectToUpdate.getName()).thenReturn(NAME);
		when(projectToUpdate.isActive()).thenReturn(STATUS);

		BimProject oldProject = mock(BimProject.class);
		when(oldProject.getIdentifier()).thenReturn(ID);
		when(oldProject.getName()).thenReturn(NAME);
		when(oldProject.isActive()).thenReturn(STATUS);
		when(service.getProjectByPoid(ID)).thenReturn(oldProject);

		// when
		BimFacadeProject updatedProject = serviceFacade.updateProject(projectToUpdate);

		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).getProjectByPoid(ID);
		verifyNoMoreInteractions(service);

		assertTrue(updatedProject.getProjectId().equals(ID));
		assertTrue(updatedProject.getName().equals(NAME));
		assertTrue(updatedProject.isActive() == STATUS);
	}
	
	@Test
	public void enableProjectIfDisabled() throws Exception {
		// given
		BimFacadeProject projectToUpdate = mock(BimFacadeProject.class);
		when(projectToUpdate.getProjectId()).thenReturn(ID);
		when(projectToUpdate.getName()).thenReturn(NAME);
		when(projectToUpdate.isActive()).thenReturn(STATUS);

		BimProject oldProject = mock(BimProject.class);
		when(oldProject.getIdentifier()).thenReturn(ID);
		when(oldProject.getName()).thenReturn(NAME);
		when(oldProject.isActive()).thenReturn(!STATUS);
		when(service.getProjectByPoid(ID)).thenReturn(oldProject);

		// when
		BimFacadeProject updatedProject = serviceFacade.updateProject(projectToUpdate);

		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).getProjectByPoid(ID);
		inOrder.verify(service).enableProject(ID);
		verifyNoMoreInteractions(service);

		assertTrue(updatedProject.getProjectId().equals(oldProject.getIdentifier()));
		assertTrue(updatedProject.getName().equals(oldProject.getName()));
		assertTrue(updatedProject.isActive() == oldProject.isActive());
	}

	@Test
	public void newRevisionLoadedAndProjectEnabled() throws Exception {
		// given
		BimFacadeProject projectToUpdate = mock(BimFacadeProject.class);
		when(projectToUpdate.getProjectId()).thenReturn(ID);
		when(projectToUpdate.getName()).thenReturn(NAME);
		when(projectToUpdate.isActive()).thenReturn(STATUS);
		final File file = new File(FILE);
		when(projectToUpdate.getFile()).thenReturn(file);

		BimProject project = mock(BimProject.class);
		when(project.getIdentifier()).thenReturn(ID);
		when(project.isActive()).thenReturn(!STATUS);
		when(project.getLastRevisionId()).thenReturn(REVISIONID);

		when(service.getProjectByPoid(ID)).thenReturn(project);

		BimRevision revision = mock(BimRevision.class);
		Date date = new Date();
		when(revision.getDate()).thenReturn(date);
		when(service.getRevision(REVISIONID)).thenReturn(revision);

		// when
		serviceFacade.updateProject(projectToUpdate);

		// then
		InOrder inOrder = inOrder(service);
		inOrder.verify(service).getProjectByPoid(ID);
		inOrder.verify(service).checkin(ID, file);
		inOrder.verify(service).getProjectByPoid(ID);
		inOrder.verify(service).enableProject(ID);

		verifyNoMoreInteractions(service);
	}

}
