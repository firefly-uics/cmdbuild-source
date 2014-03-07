package unit.logic.bim;

import static org.mockito.Mockito.mock;

import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.DefaultBimFacade;
import org.cmdbuild.services.bim.DefaultTransactionManager;
import org.junit.Before;
import org.junit.Test;

public class DefaultBimFacadeImportTest {

	private static final String PROJECTID = "111";

	private BimFacade serviceFacade;
	private BimService service;

	@Before
	public void setUp() throws Exception {
		service = mock(BimService.class);
		serviceFacade = new DefaultBimFacade(service, new DefaultTransactionManager(service));
	}

	@Test
	public void readDataFromProjectInfo() throws Exception {
//		// given
//		StorableProject projectInfo = new StorableProject();
//		projectInfo.setProjectId(PROJECTID);
//
//		BimProject project = mock(BimProject.class);
//		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);
//
//		EntityDefinition entityDefinition = mock(EntityDefinition.class);
//
//		// when
//	//	serviceFacade.readEntityFromProject(entityDefinition, projectInfo);
//
//		// then
//		verify(service).getProjectByPoid(PROJECTID);
//
//		verifyNoMoreInteractions(service);
	}

	@Test//(expected = BimError.class)
	public void readDataFromProjectInfoWithInvalidProjectIdThrowsBimError() throws Exception {
//		// given
//		StorableProject projectInfo = new StorableProject();
//		projectInfo.setProjectId(PROJECTID);
//
//		when(service.getProjectByPoid(PROJECTID)).thenThrow(new BimError("Invalid projectId"));
//
//		EntityDefinition entityDefinition = mock(EntityDefinition.class);
//
//		// when
////		serviceFacade.readEntityFromProject(entityDefinition, projectInfo);
//
//		// then
	}

}
