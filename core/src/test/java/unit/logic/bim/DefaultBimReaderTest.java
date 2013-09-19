package unit.logic.bim;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.cmdbuild.bim.mapper.Reader;
import org.cmdbuild.bim.mapper.xml.BimReader;
import org.cmdbuild.bim.model.AttributeDefinition;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.model.implementation.SimpleAttributeDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.SimpleAttribute;
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
		reader = new BimReader(service);
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
		when(
				service.getEntitiesByType(revisionId,
						entityDefinition.getTypeName())).thenReturn(
				bimEntityList);

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
	public void sourceDataContainsOneEntityWithNoAttributesToRead()
			throws Exception {
		// given
		String revisionId = "123";
		EntityDefinition entityDefinition = mock(EntityDefinition.class);
		when(entityDefinition.getTypeName()).thenReturn(IFC_TYPE);
		when(entityDefinition.getLabel()).thenReturn(CM_CLASS);
		when(entityDefinition.isValid()).thenReturn(true);

		List<Entity> bimEntityList = Lists.newArrayList();
		Entity entity = mock(Entity.class);
		SimpleAttribute guid = mock(SimpleAttribute.class);
		when(guid.isValid()).thenReturn(true);
		when(guid.getStringValue()).thenReturn("GuidValue");
		when(entity.getAttributeByName("GlobalId")).thenReturn(guid);
		bimEntityList.add(entity);
		when(
				service.getEntitiesByType(revisionId,
						entityDefinition.getTypeName())).thenReturn(
				bimEntityList);

		// when
		reader.readEntities(revisionId, entityDefinition);

		// then
		InOrder inOrder = inOrder(service, entityDefinition);
		inOrder.verify(service).getEntitiesByType(revisionId, IFC_TYPE);

		verifyNoMoreInteractions(service);
	}

	@Test
	public void sourceDataContainsOneEntityWithOneSimpleAttribute()
			throws Exception {
		// given
		String revisionId = "123";
		EntityDefinition entityDefinition = mock(EntityDefinition.class);
		when(entityDefinition.getTypeName()).thenReturn(IFC_TYPE);
		when(entityDefinition.getLabel()).thenReturn(CM_CLASS);
		when(entityDefinition.isValid()).thenReturn(true);
		List<AttributeDefinition> attributeList = Lists.newArrayList();
		SimpleAttributeDefinition attributeDef = mock(SimpleAttributeDefinition.class);
		String attributeName = "Name";
		when(attributeDef.getName()).thenReturn(attributeName);
		String attributeName2 = "Code";
		when(attributeDef.getLabel()).thenReturn(attributeName2);
		when(attributeDef.getValue()).thenReturn("");

		attributeList.add(attributeDef);
		when(entityDefinition.getAttributes()).thenReturn(attributeList);

		List<Entity> bimEntityList = Lists.newArrayList();
		Entity bimserverEntity = mock(Entity.class);
		bimEntityList.add(bimserverEntity);
		SimpleAttribute attribute = mock(SimpleAttribute.class);
		when(bimserverEntity.getTypeName()).thenReturn(IFC_TYPE);
		when(bimserverEntity.getAttributeByName(attributeName)).thenReturn(
				attribute);
		SimpleAttribute guid = mock(SimpleAttribute.class);
		when(guid.isValid()).thenReturn(true);
		when(guid.getStringValue()).thenReturn("GuidValue");
		when(bimserverEntity.getAttributeByName("GlobalId")).thenReturn(guid);
		String value = "ED1";
		when(attribute.getStringValue()).thenReturn(value);
		when(attribute.isValid()).thenReturn(true);
		when(
				service.getEntitiesByType(revisionId,
						entityDefinition.getTypeName())).thenReturn(
				bimEntityList);

		// when
		List<Entity> source = reader.readEntities(revisionId, entityDefinition);

		// then
		InOrder inOrder = inOrder(service, entityDefinition);
		inOrder.verify(service).getEntitiesByType(revisionId, IFC_TYPE);
		verifyNoMoreInteractions(service);

		assertTrue(source.size() == 1);
		assertThat(source.get(0).getTypeName(), equalTo(CM_CLASS));
		assertThat(source.get(0).getAttributeByName(attributeName2).getValue(),
				equalTo(value));
	}

	// @Test
	// public void fetchCoordinates() throws Exception {
	// // given
	// String revisionId = "123";
	//
	// EntityDefinition entityDefinition = mock(EntityDefinition.class);
	// when(entityDefinition.getTypeName()).thenReturn(IFC_TYPE);
	// when(entityDefinition.getLabel()).thenReturn(CM_CLASS);
	// when(entityDefinition.isValid()).thenReturn(true);
	//
	// List<AttributeDefinition> attributeList = Lists.newArrayList();
	// SimpleAttributeDefinition attributeDef1 =
	// mock(SimpleAttributeDefinition.class);
	// String attributeName = "Name";
	// when(attributeDef1.getName()).thenReturn(attributeName);
	// when(attributeDef1.getValue()).thenReturn("");
	//
	// SimpleAttributeDefinition attributeDef2 =
	// mock(SimpleAttributeDefinition.class);
	// String attributeName2 = "_Coordinates";
	// when(attributeDef2.getName()).thenReturn(attributeName2);
	// when(attributeDef2.getValue()).thenReturn("");
	//
	// attributeList.add(attributeDef1);
	// attributeList.add(attributeDef2);
	// when(entityDefinition.getAttributes()).thenReturn(attributeList);
	//
	// List<Entity> bimserverEntityList = Lists.newArrayList();
	// Entity bimserverEntity = mock(Entity.class);
	// bimserverEntityList.add(bimserverEntity);
	//
	// SimpleAttribute attribute = mock(SimpleAttribute.class);
	// when(bimserverEntity.getTypeName()).thenReturn(IFC_TYPE);
	// when(bimserverEntity.getAttributeByName(attributeName)).thenReturn(
	// attribute);
	// SimpleAttribute guid = mock(SimpleAttribute.class);
	// when(guid.isValid()).thenReturn(true);
	// when(guid.getStringValue()).thenReturn("GuidValue");
	// when(bimserverEntity.getAttributeByName("GlobalId")).thenReturn(guid);
	// String value = "ED1";
	// when(attribute.getStringValue()).thenReturn(value);
	// when(attribute.isValid()).thenReturn(true);
	//
	// when(
	// service.getEntitiesByType(revisionId,
	// entityDefinition.getTypeName())).thenReturn(
	// bimserverEntityList);
	//
	// // when
	// List<Entity> source = reader.readEntities(revisionId, entityDefinition);
	//
	// }

}
