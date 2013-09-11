package unit.logic.bim;

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
import org.cmdbuild.logic.bim.BimLogic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.cmdbuild.services.bim.DefaultBimDataModelManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.common.collect.Lists;

public class BimLogicTest {

	private BimServiceFacade serviceFacade;
	private BimDataPersistence dataPersistence;
	private BimDataModelManager dataModelManager;

	private BimLogic bimLogic;
	private static final String CLASSNAME = "className";
	private static final String PROJECTID = "123";
	private static final String PROJECT_NAME = "projectName";

	private static String XML_MAPPING = "";

	private String ATTRIBUTE_NAME;
	private String ATTRIBUTE_VALUE;

	@Before
	public void setUp() throws Exception {
		serviceFacade = mock(BimServiceFacade.class);
		dataPersistence = mock(BimDataPersistence.class);
		dataModelManager = mock(BimDataModelManager.class);

		bimLogic = new BimLogic(serviceFacade, dataPersistence,
				dataModelManager);

	}

	@Test
	public void projectCreated() throws Exception {
		// given
		final File ifcFile = File.createTempFile("ifc", null,
				FileUtils.getTempDirectory());
		ifcFile.deleteOnExit();
		final BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setName(PROJECT_NAME);
		projectInfo.setProjectId(PROJECTID);

		// when
		bimLogic.createBimProjectInfo(projectInfo, ifcFile);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(serviceFacade).create(PROJECT_NAME);
		inOrder.verify(dataPersistence).saveProject(projectInfo);
		inOrder.verify(serviceFacade).update(projectInfo, ifcFile);
		inOrder.verify(dataPersistence).saveProject(projectInfo);

		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
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
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(serviceFacade).create(PROJECT_NAME);
		inOrder.verify(dataPersistence).saveProject(projectInfo);

		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
	}

	@Test
	public void projectDisabled() throws Exception {
		// given
		final String projectId = PROJECTID;

		// when
		bimLogic.disableProject(projectId);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(serviceFacade).disableProject(projectId);
		inOrder.verify(dataPersistence).disableProject(projectId);

		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
	}

	@Test
	public void projectEnabled() throws Exception {
		// given
		final String projectId = PROJECTID;

		// when
		bimLogic.enableProject(projectId);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(serviceFacade).enableProject(projectId);
		inOrder.verify(dataPersistence).enableProject(projectId);

		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
	}

	@Test
	public void readProjectInfoList() throws Exception {
		// given

		// when
		bimLogic.readBimProjectInfo();

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(dataPersistence).listProjectInfo();

		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
	}

	@Test
	public void updateProjectInfoWithoutUpload() throws Exception {
		// given
		final BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setName(PROJECT_NAME);

		// when
		bimLogic.updateBimProjectInfo(projectInfo, null);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(serviceFacade).update(projectInfo);
		inOrder.verify(dataPersistence).saveProject(projectInfo);

		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
	}

	@Test
	public void updateProjectInfoWithUpload() throws Exception {
		// given
		final File ifcFile = File.createTempFile("ifc", null,
				FileUtils.getTempDirectory());
		ifcFile.deleteOnExit();
		final BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setName(PROJECT_NAME);

		// when
		bimLogic.updateBimProjectInfo(projectInfo, ifcFile);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(serviceFacade).update(projectInfo, ifcFile);
		inOrder.verify(dataPersistence).saveProject(projectInfo);

		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
	}

	@Test
	public void readLayerList() throws Exception {
		// given

		// when
		bimLogic.readBimLayer();

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(dataPersistence).listLayers();

		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
	}

	@Test
	public void updateLayerActiveAttributeWithTrueValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "active";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME,
				ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
	}

	@Test
	public void updateLayerBimRootAttributeWithTrueValueWithNullOldBimRoot()
			throws Exception {
		// given
		ATTRIBUTE_NAME = "root";
		ATTRIBUTE_VALUE = "true";
		when(dataPersistence.findRoot()).thenReturn(null);

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(dataPersistence).findRoot();
		inOrder.verify(dataModelManager).createBimDomainOnClass(CLASSNAME);
		inOrder.verify(dataPersistence).saveRoot(CLASSNAME, true);

		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
	}

	@Test
	public void updateLayerBimRootAttributeWithTrueValueWithNotNullOldBimRoot()
			throws Exception {
		// given
		ATTRIBUTE_NAME = "root";
		ATTRIBUTE_VALUE = "true";
		String OTHER_CLASS = "anotherClass";
		when(dataPersistence.findRoot()).thenReturn(new BimLayer(OTHER_CLASS));

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(dataPersistence).findRoot();
		inOrder.verify(dataModelManager).deleteBimDomainOnClass(OTHER_CLASS);
		inOrder.verify(dataPersistence).saveRoot(OTHER_CLASS, false);
		inOrder.verify(dataModelManager).createBimDomainOnClass(CLASSNAME);
		inOrder.verify(dataPersistence).saveRoot(CLASSNAME, true);

		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
	}

	@Test
	public void updateLayerBimRootAttributeWithFalseValue()
			throws Exception {
		// given
		ATTRIBUTE_NAME = "root";
		ATTRIBUTE_VALUE = "false";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(dataModelManager).deleteBimDomainOnClass(CLASSNAME);
		inOrder.verify(dataPersistence).saveRoot(CLASSNAME, false);

		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
	}
	
	@Test
	public void updateLayerExportAttributeWithTrueValue()
			throws Exception {
		// given
		ATTRIBUTE_NAME = "export";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, ATTRIBUTE_VALUE);
		inOrder.verify(dataModelManager).addCoordinatesFieldsIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveExportStatus(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
	}
	
	@Test
	public void updateLayerExportAttributeWithFalseValue()
			throws Exception {
		// given
		ATTRIBUTE_NAME = "export";
		ATTRIBUTE_VALUE = "false";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(dataPersistence).saveExportStatus(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
	}
	
	@Test
	public void updateLayerUnknownAttribute() throws Exception {
		// given
		String ATTRIBUTE_NAME = "unknown";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		verifyNoMoreInteractions(serviceFacade, dataPersistence,
				dataModelManager);
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
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(dataPersistence).findRoot();
		inOrder.verify(dataModelManager).bindProjectToCards(PROJECTID,
				CLASSNAME, cards);

		verifyNoMoreInteractions(dataModelManager);
		verifyZeroInteractions(serviceFacade, dataPersistence);
	}

	@Test
	public void projectCardIsBindedToNoneCards() throws Exception {
		// given
		ArrayList<String> cards = Lists.newArrayList();
		when(dataPersistence.findRoot()).thenReturn(new BimLayer(CLASSNAME));

		// when
		bimLogic.bindProjectToCards(PROJECTID, cards);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(dataPersistence).findRoot();
		inOrder.verify(dataModelManager).bindProjectToCards(PROJECTID,
				CLASSNAME, cards);

		verifyNoMoreInteractions(dataPersistence);
		verifyZeroInteractions(serviceFacade, dataModelManager);
	}

	@Test
	public void ifXmlMappingIsEmptyDoNothing() throws Exception {
		// given
		XML_MAPPING = "";

		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setProjectId(PROJECTID);
		projectInfo.setImportMapping(XML_MAPPING);
		when(dataPersistence.fetchProjectInfo(PROJECTID)).thenReturn(
				projectInfo);

		// when
		bimLogic.importIfc(PROJECTID);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(dataPersistence).fetchProjectInfo(PROJECTID);
		inOrder.verify(dataPersistence).setSynchronized(projectInfo, true);
		verifyNoMoreInteractions(dataPersistence);
		verifyZeroInteractions(serviceFacade, dataModelManager);
	}

	@Test
	public void readOneEntityAndCallTheUpdateOnCMDBOnce() throws Exception {
		// given
		XML_MAPPING = "<bim-conf><entity></entity></bim-conf>";

		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setProjectId(PROJECTID);
		projectInfo.setImportMapping(XML_MAPPING);
		when(dataPersistence.fetchProjectInfo(projectInfo.getProjectId()))
				.thenReturn(projectInfo);

		ArgumentCaptor<EntityDefinition> entityDefCaptor = ArgumentCaptor
				.forClass(EntityDefinition.class);
		ArgumentCaptor<BimProjectInfo> projectCaptor = ArgumentCaptor
				.forClass(BimProjectInfo.class);

		List<Entity> bimEntityList = Lists.newArrayList();
		Entity entity = mock(Entity.class);
		SimpleAttribute globalIdAttribute = mock(SimpleAttribute.class);
		when(globalIdAttribute.isValid()).thenReturn(true);
		when(globalIdAttribute.getStringValue()).thenReturn("guid");
		when(entity.getAttributeByName(DefaultBimDataModelManager.GLOBALID))
				.thenReturn(globalIdAttribute);
		bimEntityList.add(entity);
		when(
				serviceFacade.read(projectCaptor.capture(),
						entityDefCaptor.capture())).thenReturn(bimEntityList);

		// when
		bimLogic.importIfc(PROJECTID);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(dataPersistence).fetchProjectInfo(
				projectInfo.getProjectId());
		inOrder.verify(serviceFacade).read(projectCaptor.getValue(),
				entityDefCaptor.getValue());
		inOrder.verify(dataModelManager).updateCardsFromSource(bimEntityList);
		inOrder.verify(dataPersistence).setSynchronized(projectInfo, true);
		verifyNoMoreInteractions(dataPersistence, serviceFacade,
				dataModelManager);
	}

	@Test
	public void fetchNoEntitiesFromBimAndDoNothing() throws Exception {
		// given
		XML_MAPPING = "<bim-conf><entity></entity></bim-conf>";

		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setProjectId(PROJECTID);
		projectInfo.setImportMapping(XML_MAPPING);
		when(dataPersistence.fetchProjectInfo(projectInfo.getProjectId()))
				.thenReturn(projectInfo);

		ArgumentCaptor<EntityDefinition> entityDefCaptor = ArgumentCaptor
				.forClass(EntityDefinition.class);
		ArgumentCaptor<BimProjectInfo> projectCaptor = ArgumentCaptor
				.forClass(BimProjectInfo.class);

		List<Entity> bimEntityList = Lists.newArrayList();
		when(
				serviceFacade.read(projectCaptor.capture(),
						entityDefCaptor.capture())).thenReturn(bimEntityList);

		// when
		bimLogic.importIfc(PROJECTID);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence,
				dataModelManager);
		inOrder.verify(dataPersistence).fetchProjectInfo(
				projectInfo.getProjectId());
		inOrder.verify(serviceFacade).read(projectCaptor.getValue(),
				entityDefCaptor.getValue());
		inOrder.verify(dataPersistence).setSynchronized(projectInfo, true);
		verifyNoMoreInteractions(dataPersistence, serviceFacade,
				dataModelManager);
	}

}
