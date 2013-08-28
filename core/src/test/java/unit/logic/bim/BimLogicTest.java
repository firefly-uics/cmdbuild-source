package unit.logic.bim;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.logic.bim.BIMLogic;
import org.cmdbuild.model.bim.BimMapperInfo;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class BimLogicTest {

	private BimServiceFacade serviceFacade;
	private BimDataPersistence dataPersistence;
	private BimDataModelManager dataModelManager;

	private BIMLogic bimLogic;
	private static final String CLASSNAME = "className";
	private static final String PROJECTID = "123";
	private static final String PROJECT_NAME = "projectName";

	private String ATTRIBUTE_NAME;
	private String ATTRIBUTE_VALUE;

	@Before
	public void setUp() throws Exception {
		serviceFacade = mock(BimServiceFacade.class);
		dataPersistence = mock(BimDataPersistence.class);
		dataModelManager = mock(BimDataModelManager.class);
		
		bimLogic = new BIMLogic(serviceFacade, dataPersistence, dataModelManager);
		
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
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager);
		inOrder.verify(serviceFacade).create(PROJECT_NAME);
		inOrder.verify(dataPersistence).store(projectInfo);
		inOrder.verify(serviceFacade).update(projectInfo, ifcFile);
		inOrder.verify(dataPersistence).store(projectInfo);
	
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager);

	}

	@Test
	public void projectDisabled() throws Exception {
		// given
		final String projectId = PROJECTID;

		// when
		bimLogic.disableProject(projectId);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager);
		inOrder.verify(serviceFacade).disableProject(projectId);
		inOrder.verify(dataPersistence).disableProject(projectId);

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager);
	}

	@Test
	public void projectEnabled() throws Exception {
		// given
		final String projectId = PROJECTID;

		// when
		bimLogic.enableProject(projectId);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager);
		inOrder.verify(serviceFacade).enableProject(projectId);
		inOrder.verify(dataPersistence).enableProject(projectId);

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager);
	}

	@Test
	public void readProjectInfoList() throws Exception {
		// given

		// when
		bimLogic.readBimProjectInfo();

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager);
		inOrder.verify(dataPersistence).readBimProjectInfo();

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager);
	}

	@Test
	public void updateProjectInfoWithoutUpload() throws Exception {
		// given
		final BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setName(PROJECT_NAME);

		// when
		bimLogic.updateBimProjectInfo(projectInfo, null);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager);
		inOrder.verify(serviceFacade).update(projectInfo);
		inOrder.verify(dataPersistence).store(projectInfo);

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager);
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
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager);
		inOrder.verify(serviceFacade).update(projectInfo, ifcFile);
		inOrder.verify(dataPersistence).store(projectInfo);

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager);
	}

	@Test
	public void readMapperInfoList() throws Exception {
		// given

		// when
		bimLogic.readBimMapperInfo();

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager);
		inOrder.verify(dataPersistence).readBimMapperInfo();

		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager);
	}

	@Test
	public void updateMapperInfoActiveAttributeWithTrueValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "active";
		ATTRIBUTE_VALUE = "true";
		
		// when
		bimLogic.updateBimMapperInfo(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager);
		inOrder.verify(dataPersistence).setActive(CLASSNAME, ATTRIBUTE_VALUE);
		
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager);
	}

	@Test
	public void updateMapperInfoBimRootAttributeWithTrueValueWithNullOldBimRoot() throws Exception {
		// given
		ATTRIBUTE_NAME = "bimRoot";
		ATTRIBUTE_VALUE = "true";
		when(dataPersistence.findBimRoot()).thenReturn(null);
		
		// when
		bimLogic.updateBimMapperInfo(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);
		
		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager);
		inOrder.verify(dataPersistence).findBimRoot();
		inOrder.verify(dataPersistence).setBimRootOnClass(CLASSNAME, true);
		
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager);
	}
	
	@Test
	public void updateMapperInfoBimRootAttributeWithTrueValueWithNotNullOldBimRoot() throws Exception {
		// given
		ATTRIBUTE_NAME = "bimRoot";
		ATTRIBUTE_VALUE = "true";
		String OTHER_CLASS = "anotherClass";
		when(dataPersistence.findBimRoot()).thenReturn(new BimMapperInfo(OTHER_CLASS));
		
		// when
		bimLogic.updateBimMapperInfo(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);
		
		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager);
		inOrder.verify(dataPersistence).findBimRoot();
		inOrder.verify(dataPersistence).setBimRootOnClass(OTHER_CLASS, false);
		inOrder.verify(dataPersistence).setBimRootOnClass(CLASSNAME, true);
		
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager);
	}
	
	@Test
	public void updateMapperInfoBimRootAttributeWithFalseValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "bimRoot";
		ATTRIBUTE_VALUE = "false";
		
		// when
		bimLogic.updateBimMapperInfo(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);
		
		// then
		InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager);
		inOrder.verify(dataPersistence).setBimRootOnClass(CLASSNAME, false);
		
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager);
	}
	
	

	@Test
	public void updateMapperInfoUnknownAttribute() throws Exception {
		// given
		String ATTRIBUTE_NAME = "unknown";

		// when
		bimLogic.updateBimMapperInfo(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		verifyNoMoreInteractions(serviceFacade, dataPersistence, dataModelManager);
	}

}
