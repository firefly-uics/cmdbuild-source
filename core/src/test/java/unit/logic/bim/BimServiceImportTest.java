package unit.logic.bim;

import java.util.ArrayList;

import org.cmdbuild.bim.mapper.Reader;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.bimserver.BimserverService;
import org.cmdbuild.bim.service.bimserver.BimserverService.Configuration;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.cmdbuild.services.bim.DefaultBimServiceFacade;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BimServiceImportTest {
	
	private BimServiceFacade facade;

	private BimService service;

	private static final String xmlString = "<?xml version='1.0' encoding='ISO-8859-1'?><bim-conf xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='import-conf.xsd'><entity name='IfcBuilding' label='Edificio'><attributes><attribute type='simple' name='Name' label='Code' /><attribute type='simple' name='Description' label='Description' /></attributes>	</entity></bim-conf>";

	private static final String PROJECTID = null;

	@Before
	public void setUp() throws Exception {
//		service = new BimserverService(new Configuration() {
//
//			@Override
//			public String getUsername() {
//				return "admin@tecnoteca.com";
//			}
//
//			@Override
//			public String getPassword() {
//				return "admin";
//			}
//
//			@Override
//			public String getUrl() {
//				return "http://localhost:10080/bimserver-1.2/";
//			}
//		});
		service = mock(BimService.class);
		facade = new DefaultBimServiceFacade(service);
	}
	
	@Test
	public void buildTheReader() throws Exception {
		// given
		Reader reader = mock(Reader.class);
		when(service.buildReader(xmlString)).thenReturn(reader);
		when(reader.getIfcType(0)).thenReturn("IfcBuilding");
		when(reader.getCmdbClass(0)).thenReturn("Edificio");
		
		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setProjectId(PROJECTID);
		projectInfo.setImportMapping(xmlString);
		
		BimProject project = mock(BimProject.class);
		when(project.getIdentifier()).thenReturn(PROJECTID);
		when(project.getLastRevisionId()).thenReturn(PROJECTID);
		when(service.getProjectByPoid(PROJECTID)).thenReturn(project);
		
		ArrayList<Entity> source = null;
		Entity entity = mock(Entity.class);
		source.add(entity);
		when(reader.readEntities(PROJECTID, 0)).thenReturn(source);
		
		// when
		source = (ArrayList<Entity>) facade.readFrom(projectInfo);

		// then

	}
	


}
