package unit.logic.bim;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.model.bim.StorableProject;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.cmdbuild.services.bim.DefaultBimServiceFacade;
import org.junit.Before;
import org.junit.Test;

public class DefaultBimServiceFacadeImportTest {

	private static final String PROJECTID = "111";

	private BimServiceFacade serviceFacade;
	private BimService service;

	@Before
	public void setUp() throws Exception {
		service = mock(BimService.class);
		serviceFacade = new DefaultBimServiceFacade(service);
	}

	@Test
	public void readDataFromProjectInfo() throws Exception {
		// given
		StorableProject projectInfo = new StorableProject();
		projectInfo.setProjectId(PROJECTID);

		BimProject project = mock(BimProject.class);
		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);

		EntityDefinition entityDefinition = mock(EntityDefinition.class);

		// when
	//	serviceFacade.readEntityFromProject(entityDefinition, projectInfo);

		// then
		verify(service).getProjectByPoid(PROJECTID);

		verifyNoMoreInteractions(service);
	}

	@Test(expected = BimError.class)
	public void readDataFromProjectInfoWithInvalidProjectIdThrowsBimError() throws Exception {
		// given
		StorableProject projectInfo = new StorableProject();
		projectInfo.setProjectId(PROJECTID);

		when(service.getProjectByPoid(PROJECTID)).thenThrow(new BimError("Invalid projectId"));

		EntityDefinition entityDefinition = mock(EntityDefinition.class);

		// when
//		serviceFacade.readEntityFromProject(entityDefinition, projectInfo);

		// then
	}

}
