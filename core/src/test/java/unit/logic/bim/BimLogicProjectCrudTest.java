package unit.logic.bim;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.logic.bim.BimLogic;
import org.cmdbuild.logic.bim.BimLogic.Project;
import org.cmdbuild.logic.bim.DefaultBimLogic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimFacade.BimFacadeProject;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.CmProject;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class BimLogicProjectCrudTest {

	private static final String EXPORT_ID = "exportId";
	private static final String FILENAME = "ifc";
	private static final String c2 = "22";
	private static final String c1 = "11";
	private BimFacade serviceFacade;
	private BimPersistence dataPersistence;
	private BimLogic bimLogic;
	private static final String ID = "id of pippo";
	private static final String NAME = "pippo";
	private static final String DESCRIPTION = "description of pippo";
	private static final String IMPORT = "import";
	private static final String EXPORT = "export";
	boolean STATUS = true;

	@Before
	public void setUp() throws Exception {
		serviceFacade = mock(BimFacade.class);
		dataPersistence = mock(BimPersistence.class);
		bimLogic = new DefaultBimLogic(serviceFacade, dataPersistence, null, null, null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void createProjectWithoutFile() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> convertedProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<CmProject> cmProjectCaptor = ArgumentCaptor.forClass(CmProject.class);

		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(NAME);
		when(project.getFile()).thenReturn(null);
		when(project.getDescription()).thenReturn(DESCRIPTION);

		when(project.isActive()).thenReturn(STATUS);

		final BimFacadeProject createdProject = mock(BimFacadeProject.class);
		when(createdProject.getProjectId()).thenReturn(ID);
		when(createdProject.getName()).thenReturn(NAME);
		when(createdProject.getLastCheckin()).thenReturn(null);
		when(createdProject.getExportProjectId()).thenReturn(EXPORT_ID);
		when(serviceFacade.createBaseAndExportProject(convertedProjectCaptor.capture())).thenReturn(createdProject);

		// when
		final Project result = bimLogic.createProject(project);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		final BimFacadeProject convertedProject = convertedProjectCaptor.getValue();
		inOrder.verify(serviceFacade).createBaseAndExportProject(convertedProject);
		inOrder.verify(dataPersistence).saveProject(cmProjectCaptor.capture());
		verifyNoMoreInteractions(dataPersistence);

		assertTrue(convertedProject.getName().equals(NAME));
		assertTrue(convertedProject.getFile() == null);
		assertTrue(convertedProject.isActive() == STATUS);
		assertTrue(convertedProject.getProjectId() == null);

		final CmProject projectToStore = cmProjectCaptor.getValue();
		assertTrue(projectToStore.getProjectId().equals(ID));
		assertTrue(projectToStore.getName().equals(NAME));
		assertTrue(projectToStore.getDescription().equals(DESCRIPTION));
		assertTrue(projectToStore.isActive() == STATUS);
		assertTrue(projectToStore.getCardBinding() == null);
		assertTrue(projectToStore.getExportProjectId().equals(EXPORT_ID));

		assertTrue(result.getName().equals(NAME));
		assertTrue(result.getDescription() == DESCRIPTION);
		assertTrue(result.getLastCheckin() == null);
		assertTrue(result.isActive() == STATUS);
		assertTrue(result.getCardBinding() == null);
		assertTrue(result.getProjectId() == ID);

		result.getFile(); // unsupported
	}

	@Test
	public void projectDisabled() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<CmProject> cmProjectCaptor = ArgumentCaptor.forClass(CmProject.class);

		STATUS = true;

		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(NAME);
		when(project.getProjectId()).thenReturn(ID);
		when(project.getFile()).thenReturn(null);
		when(project.isActive()).thenReturn(STATUS);
		when(project.getDescription()).thenReturn(DESCRIPTION);

		// when
		bimLogic.disableProject(project);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		inOrder.verify(serviceFacade).disableProject(facadeProjectCaptor.capture());
		inOrder.verify(dataPersistence).disableProject(cmProjectCaptor.capture());
		verifyNoMoreInteractions(serviceFacade, dataPersistence);

		final BimFacadeProject facadeProject = facadeProjectCaptor.getValue();
		assertTrue(facadeProject.getProjectId().equals(ID));
		assertTrue(facadeProject.isActive() == STATUS);
		assertTrue(facadeProject.getFile() == null);
		assertTrue(facadeProject.getName().equals(NAME));

		final CmProject projectToStore = cmProjectCaptor.getValue();
		assertTrue(projectToStore.getProjectId().equals(ID));
		assertTrue(projectToStore.getName().equals(NAME));
		assertTrue(projectToStore.getDescription().equals(DESCRIPTION));
		assertTrue(projectToStore.isActive() == STATUS);
		assertTrue(projectToStore.getCardBinding() == null);
		assertTrue(projectToStore.getLastCheckin() == null);
	}

	@Test
	public void projectEnabled() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<CmProject> cmProjectCaptor = ArgumentCaptor.forClass(CmProject.class);

		STATUS = false;

		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(NAME);
		when(project.getProjectId()).thenReturn(ID);
		when(project.getFile()).thenReturn(null);
		when(project.isActive()).thenReturn(STATUS);
		when(project.getDescription()).thenReturn(DESCRIPTION);

		// when
		bimLogic.enableProject(project);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		inOrder.verify(serviceFacade).enableProject(facadeProjectCaptor.capture());
		inOrder.verify(dataPersistence).enableProject(cmProjectCaptor.capture());
		verifyNoMoreInteractions(serviceFacade, dataPersistence);

		final BimFacadeProject facadeProject = facadeProjectCaptor.getValue();
		assertTrue(facadeProject.getProjectId().equals(ID));
		assertTrue(facadeProject.isActive() == STATUS);
		assertTrue(facadeProject.getFile() == null);
		assertTrue(facadeProject.getName().equals(NAME));

		final CmProject projectToStore = cmProjectCaptor.getValue();
		assertTrue(projectToStore.getProjectId().equals(ID));
		assertTrue(projectToStore.getName().equals(NAME));
		assertTrue(projectToStore.getDescription().equals(DESCRIPTION));
		assertTrue(projectToStore.isActive() == STATUS);
		assertTrue(projectToStore.getCardBinding() == null);
		assertTrue(projectToStore.getLastCheckin() == null);
	}

	@Test
	public void readEmptyProjectList() throws Exception {
		// given
		final Iterable<CmProject> projectList = Lists.newArrayList();
		when(dataPersistence.readAll()).thenReturn(projectList);

		// when
		final Iterable<Project> projects = bimLogic.readAllProjects();

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		inOrder.verify(dataPersistence).readAll();
		verifyNoMoreInteractions(dataPersistence);
		verifyZeroInteractions(serviceFacade);

		assertTrue(Iterables.size(projects) == 0);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void readProjectListWithOneProject() throws Exception {
		// given
		final List<CmProject> projectList = Lists.newArrayList();
		final CmProject project = mock(CmProject.class);
		when(project.getProjectId()).thenReturn(ID);
		when(project.getName()).thenReturn(NAME);
		when(project.getDescription()).thenReturn(DESCRIPTION);
		when(project.isActive()).thenReturn(STATUS);
		when(project.getImportMapping()).thenReturn(IMPORT);
		when(project.getExportMapping()).thenReturn(EXPORT);

		final Iterable<String> cardsBinded = Lists.newArrayList(c1, c2);
		when(project.getCardBinding()).thenReturn(cardsBinded);
		projectList.add(project);
		when(dataPersistence.readAll()).thenReturn(projectList);

		// when
		final Iterable<Project> projects = bimLogic.readAllProjects();

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		inOrder.verify(dataPersistence).readAll();
		verifyNoMoreInteractions(serviceFacade);
		verifyZeroInteractions(serviceFacade);

		final Project theOnlyProject = projects.iterator().next();
		final Iterable<String> cardBinding = theOnlyProject.getCardBinding();
		final Iterator<String> iterator = cardBinding.iterator();

		assertTrue(Iterables.size(projects) == 1);
		assertTrue(theOnlyProject.getProjectId().equals(ID));
		assertTrue(theOnlyProject.getName().equals(NAME));
		assertTrue(theOnlyProject.getDescription().equals(DESCRIPTION));
		assertTrue(theOnlyProject.isActive() == STATUS);
		assertTrue(theOnlyProject.getImportMapping().equals(IMPORT));
		assertTrue(theOnlyProject.getExportMapping().equals(EXPORT));
		assertTrue(Iterables.size(cardBinding) == 2);
		assertTrue(iterator.next().equals(c1));
		assertTrue(iterator.next().equals(c2));

		theOnlyProject.getFile(); // unsupported
	}

	@Test
	public void updateProjectWithoutFile() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<CmProject> cmProjectCaptor = ArgumentCaptor.forClass(CmProject.class);

		final Project project = mock(Project.class);
		when(project.getProjectId()).thenReturn(ID);
		when(project.getName()).thenReturn(NAME);
		when(project.getDescription()).thenReturn(DESCRIPTION);
		when(project.isActive()).thenReturn(STATUS);
		when(project.getImportMapping()).thenReturn(IMPORT);
		when(project.getExportMapping()).thenReturn(EXPORT);
		final Iterable<String> cardsBinded = Lists.newArrayList(c1, c2);
		when(project.getCardBinding()).thenReturn(cardsBinded);
		when(project.getFile()).thenReturn(null);

		final BimFacadeProject updatedProject = mock(BimFacadeProject.class);
		when(updatedProject.getProjectId()).thenReturn(ID);
		when(updatedProject.getLastCheckin()).thenReturn(null);
		when(serviceFacade.updateProject(facadeProjectCaptor.capture())).thenReturn(updatedProject);

		// when
		bimLogic.updateProject(project);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		final BimFacadeProject facadeProject = facadeProjectCaptor.getValue();
		inOrder.verify(serviceFacade).updateProject(facadeProject);
		inOrder.verify(dataPersistence).saveProject(cmProjectCaptor.capture());
		verifyNoMoreInteractions(serviceFacade, dataPersistence);

		final CmProject projectToStore = cmProjectCaptor.getValue();

		assertTrue(facadeProject.getProjectId().equals(ID));
		assertTrue(facadeProject.getName().equals(NAME));
		assertTrue(facadeProject.getFile() == null);
		assertTrue(facadeProject.isActive() == STATUS);

		assertTrue(projectToStore.getProjectId().equals(ID));
		assertTrue(projectToStore.getName().equals(NAME));
		assertTrue(projectToStore.getDescription().equals(DESCRIPTION));
		assertTrue(projectToStore.isActive() == STATUS);

		final Iterable<String> cardBinding = projectToStore.getCardBinding();
		final Iterator<String> iterator = cardBinding.iterator();
		assertTrue(Iterables.size(cardBinding) == 2);
		assertTrue(iterator.next().equals(c1));
		assertTrue(iterator.next().equals(c2));
	}

	@Test
	public void updateProjectWithFile() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<CmProject> cmProjectCaptor = ArgumentCaptor.forClass(CmProject.class);

		final File ifcFile = new File(FILENAME);
		final Project project = mock(Project.class);
		when(project.getProjectId()).thenReturn(ID);
		when(project.getName()).thenReturn(NAME);
		when(project.getFile()).thenReturn(ifcFile);

		final BimFacadeProject updatedProject = mock(BimFacadeProject.class);
		when(updatedProject.getProjectId()).thenReturn(ID);
		final DateTime now = new DateTime();
		when(updatedProject.getLastCheckin()).thenReturn(now);
		when(serviceFacade.updateProject(facadeProjectCaptor.capture())).thenReturn(updatedProject);
		final CmProject storedProject = mock(CmProject.class);
		when(storedProject.getExportProjectId()).thenReturn(StringUtils.EMPTY);
		when(storedProject.getShapeProjectId()).thenReturn(StringUtils.EMPTY);
		when(dataPersistence.read(ID)).thenReturn(storedProject);

		// when
		bimLogic.updateProject(project);

		// then
		final BimFacadeProject projectToUpdate = facadeProjectCaptor.getValue();
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		inOrder.verify(serviceFacade).updateProject(projectToUpdate);
		inOrder.verify(dataPersistence).saveProject(cmProjectCaptor.capture());
		inOrder.verify(dataPersistence).read(ID);
		verifyNoMoreInteractions(serviceFacade, dataPersistence);

		assertTrue(projectToUpdate.getProjectId().equals(ID));
		assertTrue(projectToUpdate.getName().equals(NAME));
		assertTrue(projectToUpdate.getFile().getName().equals(FILENAME));

		final CmProject projectToSave = cmProjectCaptor.getValue();
		assertTrue(projectToSave.getProjectId().equals(ID));
		assertTrue(projectToSave.getName().equals(NAME));
		assertTrue(projectToSave.getLastCheckin().equals(now));
	}

	@Test
	public void projectCardIsBindedToOneCards() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<CmProject> cmProjectCaptor = ArgumentCaptor.forClass(CmProject.class);

		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(NAME);
		when(project.getProjectId()).thenReturn(ID);
		final List<String> stringList = Lists.newArrayList();
		stringList.add(c1);
		when(project.getCardBinding()).thenReturn(stringList);

		final BimFacadeProject createdProject = mock(BimFacadeProject.class);
		when(createdProject.getProjectId()).thenReturn(ID);
		when(createdProject.getLastCheckin()).thenReturn(null);
		when(createdProject.getExportProjectId()).thenReturn(EXPORT_ID);
		when(serviceFacade.createBaseAndExportProject(facadeProjectCaptor.capture())).thenReturn(createdProject);
		when(dataPersistence.findRoot()).thenReturn(new BimLayer("root"));

		// when
		bimLogic.createProject(project);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence);
		final BimFacadeProject bimProject = facadeProjectCaptor.getValue();
		inOrder.verify(serviceFacade).createBaseAndExportProject(bimProject);
		inOrder.verify(dataPersistence).saveProject(cmProjectCaptor.capture());
		verifyNoMoreInteractions(dataPersistence, serviceFacade);

		final CmProject projectToSave = cmProjectCaptor.getValue();
		assertTrue(projectToSave.getProjectId().equals(ID));
		assertTrue(projectToSave.getName().equals(NAME));
		assertTrue(projectToSave.getExportProjectId().equals(EXPORT_ID));

		final Iterable<String> cardBinding = projectToSave.getCardBinding();
		final Iterator<String> iterator = cardBinding.iterator();
		assertTrue(Iterables.size(cardBinding) == 1);
		assertTrue(iterator.next().equals(c1));
	}

}
