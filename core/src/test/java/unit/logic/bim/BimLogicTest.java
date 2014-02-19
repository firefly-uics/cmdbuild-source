package unit.logic.bim;

import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.junit.Assert.assertTrue;
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
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.cmdbuild.services.bim.connector.DefaultBimDataView.BimObjectCard;
import org.cmdbuild.services.bim.connector.Mapper;
import org.cmdbuild.services.bim.connector.export.Export;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;

public class BimLogicTest {

	private BimServiceFacade serviceFacade;
	private BimDataPersistence dataPersistence;
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
		serviceFacade = mock(BimServiceFacade.class);
		dataPersistence = mock(BimDataPersistence.class);
		dataModelManager = mock(BimDataModelManager.class);
		bimDataView = mock(BimDataView.class);
		mapper = mock(Mapper.class);
		exporter = mock(Export.class);
		dataAccessLogic = mock(DataAccessLogic.class);
		bimLogic = new BimLogic(serviceFacade, dataPersistence, dataModelManager, mapper, exporter, bimDataView,
				dataAccessLogic);
	}

	@Test
	public void projectCreated() throws Exception {
		// given
		final File ifcFile = File.createTempFile("ifc", null, FileUtils.getTempDirectory());
		ifcFile.deleteOnExit();
		final BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setName(PROJECT_NAME);
		projectInfo.setProjectId(PROJECTID);

		// when
		bimLogic.createBimProjectInfo(projectInfo, ifcFile);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(serviceFacade).createProject(PROJECT_NAME);
		inOrder.verify(dataPersistence).saveProject(projectInfo);
		inOrder.verify(serviceFacade).updateProject(projectInfo, ifcFile);
		inOrder.verify(dataPersistence).saveProject(projectInfo);

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void projectCreatedWithoutFileUpload() throws Exception {
		// given
		final File ifcFile = null;
		final BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setName(PROJECT_NAME);
		projectInfo.setProjectId(PROJECTID);

		// when
		bimLogic.createBimProjectInfo(projectInfo, ifcFile);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(serviceFacade).createProject(PROJECT_NAME);
		inOrder.verify(dataPersistence).saveProject(projectInfo);

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void projectDisabled() throws Exception {
		// given
		final String projectId = PROJECTID;

		// when
		bimLogic.disableProject(projectId);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(serviceFacade).disableProject(projectId);
		inOrder.verify(dataPersistence).disableProject(projectId);

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void projectEnabled() throws Exception {
		// given
		final String projectId = PROJECTID;

		// when
		bimLogic.enableProject(projectId);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(serviceFacade).enableProject(projectId);
		inOrder.verify(dataPersistence).enableProject(projectId);

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void readProjectInfoList() throws Exception {
		// given

		// when
		bimLogic.readBimProjectInfo();

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).listProjectInfo();

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void updateProjectInfoWithoutUpload() throws Exception {
		// given
		final BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setName(PROJECT_NAME);

		// when
		bimLogic.updateBimProjectInfo(projectInfo, null);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(serviceFacade).updateProject(projectInfo);
		inOrder.verify(dataPersistence).saveProject(projectInfo);

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void updateProjectInfoWithUpload() throws Exception {
		// given
		final File ifcFile = File.createTempFile("ifc", null, FileUtils.getTempDirectory());
		ifcFile.deleteOnExit();
		final BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setName(PROJECT_NAME);

		// when
		bimLogic.updateBimProjectInfo(projectInfo, ifcFile);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(serviceFacade).updateProject(projectInfo, ifcFile);
		inOrder.verify(dataPersistence).saveProject(projectInfo);

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
	}

	@Test
	public void readLayerList() throws Exception {
		// given

		// when
		final Iterable<? extends CMClass> classes = new ArrayList<CMClass>();
		when(dataAccessLogic.findAllClasses()) //
				.thenAnswer(new Answer<Iterable<? extends CMClass>>() {
					@Override
					public Iterable<? extends CMClass> answer(InvocationOnMock invocation) throws Throwable {

						return classes;
					}
				});
		// .thenReturn(classes);
		bimLogic.readBimLayer();

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper, dataAccessLogic);

		inOrder.verify(dataPersistence).listLayers();
		inOrder.verify(dataAccessLogic).findAllClasses();

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager, mapper);
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
	public void projectCardIsBindedToTwoCards() throws Exception {
		// given
		ArrayList<String> cards = Lists.newArrayList();
		cards.add("1");
		cards.add("2");
		when(dataPersistence.findRoot()).thenReturn(new BimLayer(CLASSNAME));

		// when
		bimLogic.bindProjectToCards(PROJECTID, cards);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).findRoot();
		inOrder.verify(dataModelManager).bindProjectToCards(PROJECTID, CLASSNAME, cards);

		verifyNoMoreInteractions(dataModelManager);
		verifyZeroInteractions(serviceFacade, dataPersistence, mapper);
	}

	@Test
	public void projectCardIsBindedToNoneCards() throws Exception {
		// given
		ArrayList<String> cards = Lists.newArrayList();
		when(dataPersistence.findRoot()).thenReturn(new BimLayer(CLASSNAME));

		// when
		bimLogic.bindProjectToCards(PROJECTID, cards);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).findRoot();
		inOrder.verify(dataModelManager).bindProjectToCards(PROJECTID, CLASSNAME, cards);

		verifyNoMoreInteractions(dataPersistence);
		verifyZeroInteractions(serviceFacade, dataModelManager, mapper);
	}

	@Test
	public void ifXmlMappingIsEmptyDoNothing() throws Exception {
		// given
		XML_MAPPING = "<bim-conf></bim-conf>";

		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setProjectId(PROJECTID);
		projectInfo.setImportMapping(XML_MAPPING);
		when(dataPersistence.fetchProjectInfo(PROJECTID)).thenReturn(projectInfo);

		// when
		bimLogic.importIfc(PROJECTID);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).fetchProjectInfo(PROJECTID);
		inOrder.verify(dataPersistence).setSynchronized(projectInfo, true);
		verifyNoMoreInteractions(dataPersistence);
		verifyZeroInteractions(serviceFacade, dataModelManager, mapper);
	}

	@Test
	public void readOneEntityAndCallTheUpdateOnCMDBOnce() throws Exception {
		// given
		XML_MAPPING = "<bim-conf><entity></entity></bim-conf>";

		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setProjectId(PROJECTID);
		projectInfo.setImportMapping(XML_MAPPING);
		when(dataPersistence.fetchProjectInfo(projectInfo.getProjectId())).thenReturn(projectInfo);

		ArgumentCaptor<EntityDefinition> entityDefCaptor = ArgumentCaptor.forClass(EntityDefinition.class);
		ArgumentCaptor<BimProjectInfo> projectCaptor = ArgumentCaptor.forClass(BimProjectInfo.class);

		List<Entity> bimEntityList = Lists.newArrayList();
		Entity entity = mock(Entity.class);
		SimpleAttribute globalIdAttribute = mock(SimpleAttribute.class);
		when(globalIdAttribute.isValid()).thenReturn(true);
		when(globalIdAttribute.getStringValue()).thenReturn("guid");
		when(entity.getAttributeByName(GLOBALID_ATTRIBUTE)).thenReturn(globalIdAttribute);
		bimEntityList.add(entity);
		when(serviceFacade.readEntityFromProject(entityDefCaptor.capture(), projectCaptor.capture())).thenReturn(
				bimEntityList);

		// when
		bimLogic.importIfc(PROJECTID);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).fetchProjectInfo(projectInfo.getProjectId());
		inOrder.verify(serviceFacade).readEntityFromProject(entityDefCaptor.getValue(), projectCaptor.getValue());
		inOrder.verify(mapper).update(bimEntityList);
		inOrder.verify(dataPersistence).setSynchronized(projectInfo, true);
		verifyNoMoreInteractions(dataPersistence, serviceFacade, dataModelManager, mapper);
	}

	@Test
	public void fetchNoEntitiesFromBimAndDoNothing() throws Exception {
		// given
		XML_MAPPING = "<bim-conf><entity></entity></bim-conf>";

		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setProjectId(PROJECTID);
		projectInfo.setImportMapping(XML_MAPPING);
		when(dataPersistence.fetchProjectInfo(projectInfo.getProjectId())).thenReturn(projectInfo);

		ArgumentCaptor<EntityDefinition> entityDefCaptor = ArgumentCaptor.forClass(EntityDefinition.class);
		ArgumentCaptor<BimProjectInfo> projectCaptor = ArgumentCaptor.forClass(BimProjectInfo.class);

		List<Entity> bimEntityList = Lists.newArrayList();
		when(serviceFacade.readEntityFromProject(entityDefCaptor.capture(), projectCaptor.capture())).thenReturn(
				bimEntityList);

		// when
		bimLogic.importIfc(PROJECTID);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).fetchProjectInfo(projectInfo.getProjectId());
		inOrder.verify(serviceFacade).readEntityFromProject(entityDefCaptor.getValue(), projectCaptor.getValue());
		inOrder.verify(dataPersistence).setSynchronized(projectInfo, true);
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
