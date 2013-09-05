package unit.logic.bim;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.cmdbuild.bim.mapper.Reader;
import org.cmdbuild.bim.mapper.xml.DefaultBimReader;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.collect.Lists;

public class DefaultBimReaderTest {

	private static final String IFC_TYPE = "IfcBuilding";
	private static final String CM_CLASS = "Edificio";
	private BimService service;
	private Reader reader;

	@Before
	public void SetUp() throws Exception {
		service = mock(BimService.class);
		reader = new DefaultBimReader(service);
	}

	@Test
	public void sourceDataIsEmptyThrowsBimError() throws Exception {
		// given
		String revisionId = "123";
		EntityDefinition entityDefinition = mock(EntityDefinition.class);
		when(entityDefinition.getTypeName()).thenReturn(IFC_TYPE);
		when(entityDefinition.getLabel()).thenReturn(CM_CLASS);
		when(entityDefinition.isValid()).thenReturn(true);

		List<Entity> bimEntityList = Lists.newArrayList();
		when(service.getEntitiesByType(revisionId, entityDefinition.getTypeName())).thenReturn(bimEntityList);

		// when
		try {
			reader.readEntities(revisionId, entityDefinition);
		} catch (BimError be) {

		}

		// then
		InOrder inOrder = inOrder(service, entityDefinition);
		inOrder.verify(service).getEntitiesByType(revisionId, IFC_TYPE);

		verifyNoMoreInteractions(service);
	}

	@Test
	public void sourceDataContainsOneEntityWithNoAttributesToRead() throws Exception {
		// given
		String revisionId = "123";
		EntityDefinition entityDefinition = mock(EntityDefinition.class);
		when(entityDefinition.getTypeName()).thenReturn(IFC_TYPE);
		when(entityDefinition.getLabel()).thenReturn(CM_CLASS);
		when(entityDefinition.isValid()).thenReturn(true);

		List<Entity> bimEntityList = Lists.newArrayList();
		Entity entity = mock(Entity.class);
		bimEntityList.add(entity);
		when(service.getEntitiesByType(revisionId, entityDefinition.getTypeName())).thenReturn(bimEntityList);

		// when
		reader.readEntities(revisionId, entityDefinition);

		// then
		InOrder inOrder = inOrder(service, entityDefinition);
		inOrder.verify(service).getEntitiesByType(revisionId, IFC_TYPE);

		verifyNoMoreInteractions(service);
	}

}
