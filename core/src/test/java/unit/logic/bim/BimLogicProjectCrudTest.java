package unit.logic.bim;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.logic.bim.BimLogic;
import org.cmdbuild.logic.bim.BimLogic.Project;
import org.cmdbuild.logic.bim.DefaultBimLogic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimFacade.BimFacadeProject;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.CmProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class BimLogicProjectCrudTest {

	private BimFacade serviceFacade;
	private BimPersistence dataPersistence;
	private BimDataModelManager dataModelManager;
	private BimLogic bimLogic;
	private static final String PROJECTID = "123";
	private static final String PROJECT_NAME = "projectName";
	private static final String DESCRIPTION = "description";

	@Before
	public void setUp() throws Exception {
		serviceFacade = mock(BimFacade.class);
		dataPersistence = mock(BimPersistence.class);
		bimLogic = new DefaultBimLogic(serviceFacade, dataPersistence, dataModelManager, null, null, null);
	}

	@Test
	public void simpleCreationOfAProjectWithoutFile() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<CmProject> cmProjectCaptor = ArgumentCaptor.forClass(CmProject.class);
		
		Project project = mock(Project.class);
		when(project.getName()).thenReturn(PROJECT_NAME);
		when(project.getFile()).thenReturn(null);
		when(project.getDescription()).thenReturn(DESCRIPTION);
		
		BimFacadeProject createdProject = mock(BimFacadeProject.class);
		when(createdProject.getProjectId()).thenReturn(PROJECTID);
		when(createdProject.getLastCheckin()).thenReturn(null);
		when(serviceFacade.createProject(facadeProjectCaptor.capture())).thenReturn(createdProject);

		// when
		bimLogic.createProject(project);
		
		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		inOrder.verify(serviceFacade).createProject(facadeProjectCaptor.getValue());
		inOrder.verify(dataPersistence).saveProject(cmProjectCaptor.capture());
		
		assertTrue(cmProjectCaptor.getValue().getProjectId().equals(PROJECTID));
		assertTrue(cmProjectCaptor.getValue().getName().equals(PROJECT_NAME));
		assertTrue(cmProjectCaptor.getValue().getDescription().equals(DESCRIPTION));
		
		verifyNoMoreInteractions(serviceFacade, dataPersistence);
	}

	@Test
	public void projectDisabled() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<CmProject> cmProjectCaptor = ArgumentCaptor.forClass(CmProject.class);
		
		Project project = mock(Project.class);
		when(project.getName()).thenReturn(PROJECT_NAME);
		when(project.getProjectId()).thenReturn(PROJECTID);
		when(project.getFile()).thenReturn(null);
		when(project.isActive()).thenReturn(true);

		// when
		bimLogic.disableProject(project);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		inOrder.verify(serviceFacade).disableProject(facadeProjectCaptor.capture());
		inOrder.verify(dataPersistence).disableProject(cmProjectCaptor.capture());
		
		assertTrue(facadeProjectCaptor.getValue().getProjectId().equals(PROJECTID));
		assertTrue(cmProjectCaptor.getValue().getProjectId().equals(PROJECTID));
		verifyNoMoreInteractions(serviceFacade, dataPersistence);
	}

	@Test
	public void projectEnabled() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<CmProject> cmProjectCaptor = ArgumentCaptor.forClass(CmProject.class);
		
		Project project = mock(Project.class);
		when(project.getName()).thenReturn(PROJECT_NAME);
		when(project.getProjectId()).thenReturn(PROJECTID);
		when(project.getFile()).thenReturn(null);
		when(project.isActive()).thenReturn(false);

		// when
		bimLogic.disableProject(project);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		inOrder.verify(serviceFacade).disableProject(facadeProjectCaptor.capture());
		inOrder.verify(dataPersistence).disableProject(cmProjectCaptor.capture());
		
		assertTrue(facadeProjectCaptor.getValue().getProjectId().equals(PROJECTID));
		assertTrue(cmProjectCaptor.getValue().getProjectId().equals(PROJECTID));
		verifyNoMoreInteractions(serviceFacade, dataPersistence);
	}

	@Test
	public void readEmptyProjectList() throws Exception {
		// given
		Iterable<CmProject> projectList = Lists.newArrayList();
		when(dataPersistence.readAll()).thenReturn(projectList);

		// when
		bimLogic.readAllProjects();

		// then
		assertTrue(Iterables.size(dataPersistence.readAll()) == 0);
		verifyZeroInteractions(serviceFacade);
	}

	@Test
	public void readProjectListWithOneProject() throws Exception {
		// given
		List<CmProject> projectList = Lists.newArrayList();
		CmProject project = mock(CmProject.class);
		when(project.getProjectId()).thenReturn(PROJECTID);
		projectList.add(project);
		when(dataPersistence.readAll()).thenReturn(projectList);

		// when
		bimLogic.readAllProjects();
		Iterable<CmProject> projects = dataPersistence.readAll();

		// then
		assertTrue(Iterables.size(projects) == 1);
		assertTrue(projects.iterator().next().getProjectId().equals(PROJECTID));
		verifyNoMoreInteractions(serviceFacade);
	}

	@Test
	public void updateProjectWithoutFile() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<CmProject> cmProjectCaptor = ArgumentCaptor.forClass(CmProject.class);
		
		Project project = mock(Project.class);
		when(project.getName()).thenReturn(PROJECT_NAME);
		when(project.getFile()).thenReturn(null);
		
		BimFacadeProject updatedProject = mock(BimFacadeProject.class);
		when(updatedProject.getProjectId()).thenReturn(PROJECTID);
		when(updatedProject.getLastCheckin()).thenReturn(null);
		when(serviceFacade.updateProject(facadeProjectCaptor.capture())).thenReturn(updatedProject);

		// when
		bimLogic.updateProject(project);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		inOrder.verify(serviceFacade).updateProject(facadeProjectCaptor.getValue());
		inOrder.verify(dataPersistence).saveProject(cmProjectCaptor.capture());
		
		verifyNoMoreInteractions(serviceFacade, dataPersistence);
	}

	@Test
	public void updateProjectWithFile() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<CmProject> cmProjectCaptor = ArgumentCaptor.forClass(CmProject.class);
		
		final File ifcFile = File.createTempFile("ifc", null, FileUtils.getTempDirectory());
		ifcFile.deleteOnExit();
		Project project = mock(Project.class);
		when(project.getName()).thenReturn(PROJECT_NAME);
		when(project.getFile()).thenReturn(ifcFile);
		BimFacadeProject updatedProject = mock(BimFacadeProject.class);
		when(updatedProject.getProjectId()).thenReturn(PROJECTID);
		when(updatedProject.getLastCheckin()).thenReturn(null);
		when(serviceFacade.updateProject(facadeProjectCaptor.capture())).thenReturn(updatedProject);

		// when
		bimLogic.updateProject(project);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		inOrder.verify(serviceFacade).updateProject(facadeProjectCaptor.getValue());
		inOrder.verify(dataPersistence).saveProject(cmProjectCaptor.capture());
		verifyNoMoreInteractions(serviceFacade, dataPersistence);
	}

	@Test
	public void projectCardIsBindedToOneCards() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<CmProject> cmProjectCaptor = ArgumentCaptor.forClass(CmProject.class);
		
		Project project = mock(Project.class);
		when(project.getName()).thenReturn(PROJECT_NAME);
		when(project.getFile()).thenReturn(null);
		when(project.getProjectId()).thenReturn(PROJECTID);
		List<String> stringList = Lists.newArrayList();
		stringList.add("11");
		when(project.getCardBinding()).thenReturn(stringList);
		
		BimFacadeProject createdProject = mock(BimFacadeProject.class);
		when(createdProject.getProjectId()).thenReturn(PROJECTID);
		when(createdProject.getLastCheckin()).thenReturn(null);
		when(serviceFacade.createProject(facadeProjectCaptor.capture())).thenReturn(createdProject);
		when(dataPersistence.findRoot()).thenReturn(new BimLayer("root"));

		// when
		bimLogic.createProject(project);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		inOrder.verify(serviceFacade).createProject(facadeProjectCaptor.getValue());
		inOrder.verify(dataPersistence).saveProject(cmProjectCaptor.capture());
		verifyNoMoreInteractions(dataPersistence, serviceFacade);
	}

}
