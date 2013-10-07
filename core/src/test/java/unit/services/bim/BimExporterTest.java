package unit.services.bim;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.RandomStringUtils;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.DefaultBimServiceFacade;
import org.cmdbuild.services.bim.connector.export.BimExporter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.collect.Lists;

public class BimExporterTest {

	private static final String PROJECT_ID = "xxxxxx";
	private static final String ROOM_CLASSNAME = "Room";
	private static final String GUID = RandomStringUtils.randomAscii(22);
	private final CMDataView dataView = mock(CMDataView.class);
	private final DefaultBimServiceFacade serviceFacade = mock(DefaultBimServiceFacade.class);
	private BimDataPersistence persistence = mock(BimDataPersistence.class);
	private DataSource dataSource = mock(DataSource.class);
	private BimExporter exporter;

	@Before
	public void setUp() {
		exporter = new BimExporter(dataView, serviceFacade, dataSource, persistence);
	}

	@Test
	public void ifThereIsNotAContainerLayerDoNothing() throws Exception {
		// given
		Catalog catalog = mock(Catalog.class);
		Iterable<EntityDefinition> entities = Lists.newArrayList();
		when(catalog.getEntitiesDefinitions()).thenReturn(entities);

		Entity spaceEntity = mock(Entity.class);
		List<Entity> containersList = Lists.newArrayList();
		containersList.add(spaceEntity);
		when(serviceFacade.fetchContainers(PROJECT_ID)).thenReturn(containersList);

		// when
		exporter.export(catalog, PROJECT_ID);

		// then
		InOrder inOrder = inOrder(dataView, serviceFacade, persistence);
		inOrder.verify(serviceFacade).service();
		inOrder.verify(serviceFacade).fetchContainers(PROJECT_ID);
		inOrder.verify(persistence).findContainer();
		verifyNoMoreInteractions(serviceFacade, persistence);
		verifyZeroInteractions(dataView);
	}

	@Test
	public void ifThereAreNoIfcSpacesInProjectDoNothing() throws Exception {
		// given
		Catalog catalog = mock(Catalog.class);
		Iterable<EntityDefinition> entities = Lists.newArrayList();
		when(catalog.getEntitiesDefinitions()).thenReturn(entities);

		BimLayer containerLayer = mock(BimLayer.class);
		when(containerLayer.getClassName()).thenReturn(ROOM_CLASSNAME);
		when(persistence.findContainer()).thenReturn(containerLayer);
		
		Entity spaceEntity = mock(Entity.class);
		List<Entity> containersList = Lists.newArrayList();
		containersList.add(spaceEntity);
		when(serviceFacade.fetchContainers(PROJECT_ID)).thenReturn(containersList);

		// when
		exporter.export(catalog, PROJECT_ID);

		// then
		InOrder inOrder = inOrder(dataView, serviceFacade, persistence);
		inOrder.verify(serviceFacade).service();
		inOrder.verify(serviceFacade).fetchContainers(PROJECT_ID);
		inOrder.verify(persistence).findContainer();
		verifyNoMoreInteractions(serviceFacade, persistence);
		verifyZeroInteractions(dataView);
	}

	@Test
	public void loopOnOneSpace() throws Exception {
		// given
		Catalog catalog = mock(Catalog.class);
		Iterable<EntityDefinition> entities = Lists.newArrayList();
		when(catalog.getEntitiesDefinitions()).thenReturn(entities);
		
		Entity spaceEntity = mock(Entity.class);
		List<Entity> containersList = Lists.newArrayList();
		containersList.add(spaceEntity);
		when(serviceFacade.fetchContainers(PROJECT_ID)).thenReturn(containersList);
		when(spaceEntity.getKey()).thenReturn(GUID);
		
		BimLayer containerLayer = mock(BimLayer.class);
		when(containerLayer.getClassName()).thenReturn(ROOM_CLASSNAME);
		when(persistence.findContainer()).thenReturn(containerLayer);
		
		//when
		exporter.export(catalog, PROJECT_ID);
		
		//then
		InOrder inOrder = inOrder(dataView, serviceFacade, persistence);
		inOrder.verify(serviceFacade).service();
		inOrder.verify(serviceFacade).fetchContainers(PROJECT_ID);
		inOrder.verify(persistence).findContainer();
		verifyNoMoreInteractions(serviceFacade, persistence);
		verifyZeroInteractions(dataView);
		
	}

}
