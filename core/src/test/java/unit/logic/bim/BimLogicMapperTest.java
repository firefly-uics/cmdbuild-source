package unit.logic.bim;

import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.SimpleAttribute;
import org.cmdbuild.logic.bim.BimLogic;
import org.cmdbuild.logic.bim.BimLogic.Project;
import org.cmdbuild.logic.bim.DefaultBimLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.CmProject;
import org.cmdbuild.services.bim.connector.DefaultBimDataView.BimCard;
import org.cmdbuild.services.bim.connector.Mapper;
import org.cmdbuild.services.bim.connector.export.Export;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.collect.Lists;

public class BimLogicMapperTest {

	private BimFacade serviceFacade;
	private BimPersistence dataPersistence;
	private BimDataModelManager dataModelManager;
	private DataAccessLogic dataAccessLogic;
	private BimDataView bimDataView;
	private Mapper mapper;
	private Export exporter;
	private BimLogic bimLogic;
	private static final String PROJECTID = "123";
	private static final String GLOBALID_VALUE = "234";
	private static final String OID = null;
	private static final String REVISIONID = null;

	private static String XML_MAPPING = "";


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
		inOrder.verify(dataPersistence).saveProject(any(CmProject.class));
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
		BimCard response = new BimCard();
		when(bimDataView.getBimDataFromGlobalid(GLOBALID_VALUE)).thenReturn(response);

		// when
		response = bimLogic.fetchCardDataFromObjectId(objectId, revisionId);

		// then
		assertTrue(response.getId() == null);
		assertTrue(response.getId() == null);
	}

}
