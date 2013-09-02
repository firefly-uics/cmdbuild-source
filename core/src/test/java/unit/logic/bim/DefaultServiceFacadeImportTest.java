package unit.logic.bim;

import static org.mockito.Mockito.*;

import org.cmdbuild.bim.mapper.Reader;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.cmdbuild.services.bim.DefaultBimServiceFacade;
import org.junit.Before;
import org.junit.Test;

public class DefaultServiceFacadeImportTest {

	private BimService service;
	private BimServiceFacade serviceFacade;

	private static final String xmlString = "<bim-conf><entity name='IfcBuilding' label='Edificio'></entity></bim-conf>";
	private static final String PROJECTID = "111";

	@Before
	public void setUp() throws Exception {
		service = mock(BimService.class);
		serviceFacade = new DefaultBimServiceFacade(service);
	}

	@Test
	public void readDataFromProjectInfo() throws Exception {
		// given
		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setProjectId(PROJECTID);
		projectInfo.setImportMapping(xmlString);
		Reader reader = mock(Reader.class);
		when(service.buildReader(xmlString)).thenReturn(reader);
		when(reader.getNumberOfEntititesDefinitions()).thenReturn(1);
		BimProject project = mock(BimProject.class);
		when(project.getLastRevisionId()).thenReturn("222");
		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);
		
		// when
		serviceFacade.readFrom(projectInfo);

		// then
		verify(service).connect();
		verify(service).login();
		verify(service).buildReader(xmlString);
		verify(service).logout();
		verify(service).getProjectByPoid(PROJECTID);
		
		verifyNoMoreInteractions(service);
	}

}
