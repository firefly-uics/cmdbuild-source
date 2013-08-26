package unit.logic.bim;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logic.bim.BIMLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.model.bim.BimMapperInfo;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class BimLogicTest {

	private Store<BimProjectInfo> store;
	private Store<BimMapperInfo> mapperInfoStore;
	private BimService bimService;
	private CMDataView dataView;

	private BimServiceFacade bimServiceFacade;
	private BimDataPersistence bimDataPersistence;

	private BIMLogic bimLogic;
	private static final String PROJECTID = "123";
	private static final String PROJECT_NAME = "projectName";

	@Before
	public void setUp() throws Exception {
		store = mock(Store.class, BimProjectInfo.class.getName());
		mapperInfoStore = mock(Store.class, BimMapperInfo.class.getName());
		bimService = mock(BimService.class);
		dataView = mock(CMDataView.class);
		bimServiceFacade = mock(BimServiceFacade.class);
		bimDataPersistence = mock(BimDataPersistence.class);
		bimLogic = new BIMLogic( //
				store, //
				mapperInfoStore, //
				bimService, //
				new DataDefinitionLogic(dataView), //
				dataView, //
				bimServiceFacade, //
				bimDataPersistence);
	}

	@Test
	public void projectCreated() throws Exception {
		// given
		final File ifcFile = File.createTempFile("ifc", null, FileUtils.getTempDirectory());
		ifcFile.deleteOnExit();
		final BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setName(PROJECT_NAME);

		// when
		bimLogic.create2(projectInfo, ifcFile);

		// then
		InOrder inOrder = inOrder(bimServiceFacade, bimDataPersistence);
		inOrder.verify(bimServiceFacade).create(PROJECT_NAME);
		inOrder.verify(bimDataPersistence).store(projectInfo);
		inOrder.verify(bimServiceFacade).upload(projectInfo, ifcFile);
		inOrder.verify(bimDataPersistence).store(projectInfo);
		
		verifyNoMoreInteractions(bimServiceFacade,bimDataPersistence);
		
	}
	
	@Test
	public void projectDisabled() throws Exception {
		//given
		final String projectId = PROJECTID ;
		
		//when
		bimLogic.disableProject2(projectId);
		
		//then
		InOrder inOrder = inOrder(bimServiceFacade, bimDataPersistence);
		inOrder.verify(bimServiceFacade).disableProject(projectId);
		inOrder.verify(bimDataPersistence).disableProject(projectId);
		
		verifyNoMoreInteractions(bimServiceFacade,bimDataPersistence);
	}
	
	@Test
	public void projectEnabled() throws Exception {
		//given
		final String projectId = PROJECTID ;
		
		//when
		bimLogic.enableProject2(projectId);
		
		//then
		InOrder inOrder = inOrder(bimServiceFacade, bimDataPersistence);
		inOrder.verify(bimServiceFacade).enableProject(projectId);
		inOrder.verify(bimDataPersistence).enableProject(projectId);
		
		verifyNoMoreInteractions(bimServiceFacade,bimDataPersistence);
	}
	
	@Test
	public void readProjectInfoList() throws Exception {
		//given
		
		//when
		bimLogic.readBimProjectInfo2();
		
		//then
		InOrder inOrder = inOrder(bimServiceFacade, bimDataPersistence);
		inOrder.verify(bimDataPersistence).readBimProjectInfo();
		
		verifyNoMoreInteractions(bimServiceFacade,bimDataPersistence);
	}
	
	@Test
	public void updateProjectInfoWithoutUpload() throws Exception {
		//given 
		final BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setName(PROJECT_NAME);
		
		//when
		bimLogic.update2(projectInfo, null);
		
		//then
		InOrder inOrder = inOrder(bimServiceFacade, bimDataPersistence);
		inOrder.verify(bimServiceFacade).update(projectInfo);
		inOrder.verify(bimDataPersistence).store(projectInfo);
		
		verifyNoMoreInteractions(bimServiceFacade,bimDataPersistence);
	}
	
	@Test
	public void updateProjectInfoWithUpload() throws Exception {
		//given 
		final File ifcFile = File.createTempFile("ifc", null, FileUtils.getTempDirectory());
		ifcFile.deleteOnExit();
		final BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setName(PROJECT_NAME);
		
		//when
		bimLogic.update2(projectInfo, ifcFile);
		
		//then
		InOrder inOrder = inOrder(bimServiceFacade, bimDataPersistence);
		inOrder.verify(bimServiceFacade).update(projectInfo);
		inOrder.verify(bimServiceFacade).upload(projectInfo, ifcFile);
		inOrder.verify(bimDataPersistence).store(projectInfo);
		
		verifyNoMoreInteractions(bimServiceFacade,bimDataPersistence);
	}
	
	@Test
	public void readMapperInfoList() throws Exception {
		//given
		
		//when
		bimLogic.readBimMapperInfo2();
		
		//then
		InOrder inOrder = inOrder(bimServiceFacade, bimDataPersistence);
		inOrder.verify(bimDataPersistence).readBimMapperInfo();
		
		verifyNoMoreInteractions(bimServiceFacade,bimDataPersistence);
	}
	
}
