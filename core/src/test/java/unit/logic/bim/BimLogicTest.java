package unit.logic.bim;

import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.SimpleAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.bim.BimLogic;
import org.cmdbuild.logic.bim.BimLogic.Project;
import org.cmdbuild.logic.bim.DefaultBimLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.CmProject;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimFacade.BimFacadeProject;
import org.cmdbuild.services.bim.connector.DefaultBimDataView.BimObjectCard;
import org.cmdbuild.services.bim.connector.Mapper;
import org.cmdbuild.services.bim.connector.export.Export;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;

public class BimLogicTest {

	private BimFacade serviceFacade;
	private BimPersistence dataPersistence;
	private BimDataModelManager dataModelManager;
	private DataAccessLogic dataAccessLogic;
	private BimDataView bimDataView;
	private Mapper mapper;
	private Export exporter;
	private BimLogic bimLogic;
	private static final String CLASSNAME = "className";
	private static final String PROJECTID = "123";
	private static final String PROJECT_NAME = "projectName";
	private static final String GLOBALID_VALUE = "234";
	private static final String OID = null;
	private static final String REVISIONID = null;

	private static String XML_MAPPING = "";

	private String ATTRIBUTE_NAME;
	private String ATTRIBUTE_VALUE;

	@Before
	public void setUp() throws Exception {
		serviceFacade = mock(BimFacade.class);
		dataPersistence = mock(BimPersistence.class);
		dataModelManager = mock(BimDataModelManager.class);
		bimDataView = mock(BimDataView.class);
		mapper = mock(Mapper.class);
		exporter = mock(Export.class);
		dataAccessLogic = mock(DataAccessLogic.class);
		bimLogic = new DefaultBimLogic(serviceFacade, dataPersistence, dataModelManager, mapper, exporter, bimDataView,
				dataAccessLogic);
	}

	@Test
	public void simpleCreationOfAProjectWithoutFile() throws Exception {
		// given
		Project project = mock(Project.class);
		when(project.getName()).thenReturn(PROJECT_NAME);
		when(project.getFile()).thenReturn(null);
		BimFacadeProject createdProject = mock(BimFacadeProject.class);
		when(createdProject.getProjectId()).thenReturn(PROJECTID);
		when(createdProject.getLastCheckin()).thenReturn(null);
		when(serviceFacade.createProject(any(BimFacadeProject.class))).thenReturn(createdProject);

		// when
		bimLogic.createProject(project);
		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(serviceFacade).createProject(any(BimFacadeProject.class));
		inOrder.verify(dataPersistence).saveProject(any(CmProject.class));

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void projectDisabled() throws Exception {
		// given
		Project project = mock(Project.class);
		when(project.getName()).thenReturn(PROJECT_NAME);
		when(project.getFile()).thenReturn(null);

		// when
		bimLogic.disableProject(project);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(serviceFacade).disableProject(any(BimFacadeProject.class));
		inOrder.verify(dataPersistence).disableProject(any(CmProject.class));
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void projectEnabled() throws Exception {
		// given
		Project project = mock(Project.class);
		when(project.getName()).thenReturn(PROJECT_NAME);
		when(project.getFile()).thenReturn(null);

		// when
		bimLogic.enableProject(project);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(serviceFacade).enableProject(any(BimFacadeProject.class));
		inOrder.verify(dataPersistence).enableProject(any(CmProject.class));
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void readEmptyProjectList() throws Exception {
		// given
		Iterable<CmProject> projectList = Lists.newArrayList();
		when(dataPersistence.readAll()).thenReturn(projectList);

		// when
		bimLogic.readAllProjects();

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).readAll();
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void readProjectListWithOneProject() throws Exception {
		// given
		List<CmProject> projectList = Lists.newArrayList();
		CmProject project = mock(CmProject.class);
		projectList.add(project);
		when(dataPersistence.readAll()).thenReturn(projectList);

		// when
		bimLogic.readAllProjects();

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).readAll();
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void updateProjectWithoutFile() throws Exception {
		// given
		Project project = mock(Project.class);
		when(project.getName()).thenReturn(PROJECT_NAME);
		when(project.getFile()).thenReturn(null);
		BimFacadeProject updatedProject = mock(BimFacadeProject.class);
		when(updatedProject.getProjectId()).thenReturn(PROJECTID);
		when(updatedProject.getLastCheckin()).thenReturn(null);
		when(serviceFacade.updateProject(any(BimFacadeProject.class))).thenReturn(updatedProject);

		// when
		bimLogic.updateProject(project);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(serviceFacade).updateProject(any(BimFacadeProject.class));
		inOrder.verify(dataPersistence).saveProject(any(CmProject.class));
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void updateProjectWithFile() throws Exception {
		// given
		final File ifcFile = File.createTempFile("ifc", null, FileUtils.getTempDirectory());
		ifcFile.deleteOnExit();
		Project project = mock(Project.class);
		when(project.getName()).thenReturn(PROJECT_NAME);
		when(project.getFile()).thenReturn(ifcFile);
		BimFacadeProject updatedProject = mock(BimFacadeProject.class);
		when(updatedProject.getProjectId()).thenReturn(PROJECTID);
		when(updatedProject.getLastCheckin()).thenReturn(null);
		when(serviceFacade.updateProject(any(BimFacadeProject.class))).thenReturn(updatedProject);

		// when
		bimLogic.updateProject(project);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(serviceFacade).updateProject(any(BimFacadeProject.class));
		inOrder.verify(dataPersistence).saveProject(any(CmProject.class));
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void projectCardIsBindedToOneCards() throws Exception {
		// given
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
		when(serviceFacade.createProject(any(BimFacadeProject.class))).thenReturn(createdProject);
		when(dataPersistence.findRoot()).thenReturn(new BimLayer("root"));

		// when
		bimLogic.createProject(project);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(serviceFacade).createProject(any(BimFacadeProject.class));
		inOrder.verify(dataPersistence).saveProject(any(CmProject.class));
		verifyNoMoreInteractions(dataModelManager, serviceFacade, dataPersistence, mapper);
	}

	@Test
	public void readLayerList() throws Exception {
		// given
		final Iterable<? extends CMClass> classes = new ArrayList<CMClass>();
		when(dataAccessLogic.findAllClasses()) //
				.thenAnswer(new Answer<Iterable<? extends CMClass>>() {
					@Override
					public Iterable<? extends CMClass> answer(InvocationOnMock invocation) throws Throwable {

						return classes;
					}
				});
		// .thenReturn(classes);

		// when
		// bimLogic.readBimLayer();
		//
		// // then
		// InOrder inOrder = inOrder(serviceFacade, dataPersistence,
		// dataModelManager, mapper, dataAccessLogic);
		//
		// inOrder.verify(dataPersistence).listLayers();
		// inOrder.verify(dataAccessLogic).findAllClasses();
		//
		// verifyNoMoreInteractions(serviceFacade, dataPersistence,
		// dataModelManager, mapper);
	}

	@Test
	public void updateLayerActiveAttributeWithTrueValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "active";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void setContainersetExportLayer() throws Exception {
		// given
		ATTRIBUTE_NAME = "container";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		ATTRIBUTE_NAME = "export";
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, "true");
		inOrder.verify(dataPersistence).saveExportStatus(CLASSNAME, "false");
		inOrder.verify(dataModelManager).addPerimeterAndHeightFieldsIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveContainerStatus(CLASSNAME, "true");

		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, "true");
		inOrder.verify(dataPersistence).saveContainerStatus(CLASSNAME, "false");
		inOrder.verify(dataModelManager).addPositionFieldIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveExportStatus(CLASSNAME, "true");

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void updateLayerBimRootAttributeWithTrueValueWithNullOldBimRoot() throws Exception {
		// given
		ATTRIBUTE_NAME = "root";
		ATTRIBUTE_VALUE = "true";
		when(dataPersistence.findRoot()).thenReturn(null);

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, "true");
		inOrder.verify(dataPersistence).findRoot();
		inOrder.verify(dataModelManager).createBimDomainOnClass(CLASSNAME);
		inOrder.verify(dataPersistence).saveRoot(CLASSNAME, true);

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void updateLayerBimRootAttributeWithTrueValueWithNotNullOldBimRoot() throws Exception {
		// given
		ATTRIBUTE_NAME = "root";
		ATTRIBUTE_VALUE = "true";
		String OTHER_CLASS = "anotherClass";
		when(dataPersistence.findRoot()).thenReturn(new BimLayer(OTHER_CLASS));

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, "true");
		inOrder.verify(dataPersistence).findRoot();
		inOrder.verify(dataModelManager).deleteBimDomainOnClass(OTHER_CLASS);
		inOrder.verify(dataPersistence).saveRoot(OTHER_CLASS, false);
		inOrder.verify(dataModelManager).createBimDomainOnClass(CLASSNAME);
		inOrder.verify(dataPersistence).saveRoot(CLASSNAME, true);

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void updateLayerBimRootAttributeWithFalseValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "root";
		ATTRIBUTE_VALUE = "false";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataModelManager).deleteBimDomainOnClass(CLASSNAME);
		inOrder.verify(dataPersistence).saveRoot(CLASSNAME, false);

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void updateLayerExportAttributeWithTrueValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "export";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, ATTRIBUTE_VALUE);
		inOrder.verify(dataPersistence).saveContainerStatus(CLASSNAME, "false");
		inOrder.verify(dataModelManager).addPositionFieldIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveExportStatus(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void updateLayerExportAttributeWithFalseValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "export";
		ATTRIBUTE_VALUE = "false";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).saveExportStatus(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void updateLayerContainerAttributeWithTrueValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "container";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, ATTRIBUTE_VALUE);
		inOrder.verify(dataPersistence).saveExportStatus(CLASSNAME, "false");
		inOrder.verify(dataModelManager).addPerimeterAndHeightFieldsIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveContainerStatus(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void updateLayerContainerAttributeWithFalseValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "container";
		ATTRIBUTE_VALUE = "false";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).saveContainerStatus(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void updateLayerUnknownAttribute() throws Exception {
		// given
		String ATTRIBUTE_NAME = "unknown";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		verifyZeroInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void ifXmlMappingIsEmptyDoNothing() throws Exception {
		// given
		XML_MAPPING = "<bim-conf></bim-conf>";

		Project project = mock(Project.class);
		when(project.getProjectId()).thenReturn(PROJECTID);
		when(project.getImportMapping()).thenReturn(XML_MAPPING);
		CmProject cmProject = mock(CmProject.class);
		when(cmProject.getImportMapping()).thenReturn(XML_MAPPING);
		when(dataPersistence.read(PROJECTID)).thenReturn(cmProject);

		// when
		bimLogic.importIfc(PROJECTID);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).read(PROJECTID);
		inOrder.verify(dataPersistence).saveProject(cmProject);
		verifyNoMoreInteractions(dataPersistence);
		verifyZeroInteractions(serviceFacade, dataModelManager, mapper);
	}

	@Test
	public void readOneEntityAndCallTheUpdateOnCMDBOnce() throws Exception {
		// given
		XML_MAPPING = "<bim-conf><entity></entity></bim-conf>";
		Project project = mock(Project.class);
		when(project.getProjectId()).thenReturn(PROJECTID);
		when(project.getImportMapping()).thenReturn(XML_MAPPING);
		CmProject cmProject = mock(CmProject.class);
		when(cmProject.getImportMapping()).thenReturn(XML_MAPPING);
		when(dataPersistence.read(PROJECTID)).thenReturn(cmProject);

		List<Entity> bimEntityList = Lists.newArrayList();
		Entity entity = mock(Entity.class);
		SimpleAttribute globalIdAttribute = mock(SimpleAttribute.class);
		when(globalIdAttribute.isValid()).thenReturn(true);
		when(globalIdAttribute.getStringValue()).thenReturn("guid");
		when(entity.getAttributeByName(GLOBALID_ATTRIBUTE)).thenReturn(globalIdAttribute);
		bimEntityList.add(entity);
		when(serviceFacade.readEntityFromProject(any(EntityDefinition.class), any(String.class))).thenReturn(
				bimEntityList);

		// when
		bimLogic.importIfc(PROJECTID);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).read(PROJECTID);
		inOrder.verify(serviceFacade).readEntityFromProject(any(EntityDefinition.class), any(String.class));
		inOrder.verify(mapper).update(bimEntityList);
		inOrder.verify(dataPersistence).saveProject(any(CmProject.class));
		verifyNoMoreInteractions(dataPersistence, serviceFacade, dataModelManager, mapper);
	}

	@Test
	public void fetchNoEntitiesFromBimAndDoNothing() throws Exception {
		// given
		XML_MAPPING = "<bim-conf><entity></entity></bim-conf>";
		Project project = mock(Project.class);
		when(project.getProjectId()).thenReturn(PROJECTID);
		when(project.getImportMapping()).thenReturn(XML_MAPPING);
		CmProject cmProject = mock(CmProject.class);
		when(cmProject.getImportMapping()).thenReturn(XML_MAPPING);
		when(dataPersistence.read(PROJECTID)).thenReturn(cmProject);

		List<Entity> bimEntityList = Lists.newArrayList();
		when(serviceFacade.readEntityFromProject(any(EntityDefinition.class), any(String.class))).thenReturn(
				bimEntityList);

		// when
		bimLogic.importIfc(PROJECTID);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).read(PROJECTID);
		inOrder.verify(serviceFacade).readEntityFromProject(any(EntityDefinition.class), any(String.class));
		inOrder.verify(dataPersistence).saveProject(any(CmProject.class));
		verifyNoMoreInteractions(dataPersistence, serviceFacade, dataModelManager, mapper);
	}

	@Test
	public void whenThereIsNotAMatchingGloablIdReturnNothing() throws Exception {
		// given
		String objectId = OID;
		String revisionId = REVISIONID;

		when(serviceFacade.fetchGlobalIdFromObjectId(objectId, revisionId)).thenReturn(GLOBALID_VALUE);
		BimObjectCard response = new BimObjectCard();
		when(bimDataView.getBimDataFromGlobalid(GLOBALID_VALUE)).thenReturn(response);

		// when
		response = bimLogic.fetchCardDataFromObjectId(objectId, revisionId);

		// then
		assertTrue(response.getId() == null);
		assertTrue(response.getId() == null);
	}

}
