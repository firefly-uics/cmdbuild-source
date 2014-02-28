//package unit.services.bim;
//
//import static org.mockito.Mockito.inOrder;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verifyNoMoreInteractions;
//import static org.mockito.Mockito.verifyZeroInteractions;
//import static org.mockito.Mockito.when;
//
//import java.util.List;
//
//import org.cmdbuild.bim.model.Catalog;
//import org.cmdbuild.bim.model.Entity;
//import org.cmdbuild.bim.model.EntityDefinition;
//import org.cmdbuild.model.bim.BimLayer;
//import org.cmdbuild.services.bim.BimDataPersistence;
//import org.cmdbuild.services.bim.BimDataView;
//import org.cmdbuild.services.bim.DefaultBimServiceFacade;
//import org.cmdbuild.services.bim.connector.export.DefaultExport;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.InOrder;
//
//import com.google.common.collect.Lists;
//
//public class BimExporterBaseTest {
//
//	private static final String PROJECT_ID = "projectId";
//	private static final String ROOM_CLASSNAME = "Room";
//	//private static final String GUID = "objectGuid";
//	private static final String ROOM_GUID = "roomGuid";
//	private static final String roomId = "55";
//
//	private final BimDataView bimDataView = mock(BimDataView.class);
//	private final DefaultBimServiceFacade serviceFacade = mock(DefaultBimServiceFacade.class);
//	private BimDataPersistence persistence = mock(BimDataPersistence.class);
//
//	private DefaultExport exporter;
//
//	@Before
//	public void setUp() {
//		exporter = new DefaultExport(bimDataView, serviceFacade, persistence);
//	}
//
//	@Test
//	public void ifThereIsNotAContainerLayerDoNothing() throws Exception {
//		// given
//		Catalog catalog = mock(Catalog.class);
//		Iterable<EntityDefinition> entities = Lists.newArrayList();
//		when(catalog.getEntitiesDefinitions()).thenReturn(entities);
//
//		Entity spaceEntity = mock(Entity.class);
//		List<Entity> containersList = Lists.newArrayList();
//		containersList.add(spaceEntity);
//		when(serviceFacade.fetchContainers(PROJECT_ID)).thenReturn(containersList);
//
//		// when
//		exporter.export(catalog, PROJECT_ID);
//
//		// then
//		InOrder inOrder = inOrder(bimDataView, serviceFacade, persistence);
//		inOrder.verify(serviceFacade).fetchContainers(PROJECT_ID);
//		inOrder.verify(persistence).findContainer();
//		verifyNoMoreInteractions(serviceFacade, persistence);
//		verifyZeroInteractions(bimDataView);
//	}
//
//	@Test
//	public void ifThereAreNoIfcSpacesInProjectDoNothing() throws Exception {
//		// given
//		Catalog catalog = mock(Catalog.class);
//		Iterable<EntityDefinition> entities = Lists.newArrayList();
//		when(catalog.getEntitiesDefinitions()).thenReturn(entities);
//
//		BimLayer containerLayer = mock(BimLayer.class);
//		when(containerLayer.getClassName()).thenReturn(ROOM_CLASSNAME);
//		when(persistence.findContainer()).thenReturn(containerLayer);
//
//		Entity spaceEntity = mock(Entity.class);
//		when(spaceEntity.getKey()).thenReturn(ROOM_GUID);
//		List<Entity> containersList = Lists.newArrayList();
//		containersList.add(spaceEntity);
//		when(serviceFacade.fetchContainers(PROJECT_ID)).thenReturn(containersList);
//
//		// when
//		exporter.export(catalog, PROJECT_ID);
//
//		// then
//		InOrder inOrder = inOrder(bimDataView, serviceFacade, persistence);
//		inOrder.verify(serviceFacade).fetchContainers(PROJECT_ID);
//		inOrder.verify(persistence).findContainer();
//		inOrder.verify(bimDataView).getIdFromGlobalId(ROOM_GUID, ROOM_CLASSNAME);
//		inOrder.verify(serviceFacade).commitTransaction();
//		verifyNoMoreInteractions(serviceFacade, persistence);
//		verifyZeroInteractions(bimDataView);
//	}
//
//	@Test
//	public void loopOnOneSpaceNotInCMDB() throws Exception {
//		// given
//		Catalog catalog = mock(Catalog.class);
//		Iterable<EntityDefinition> entities = Lists.newArrayList();
//		when(catalog.getEntitiesDefinitions()).thenReturn(entities);
//
//		Entity spaceEntity = mock(Entity.class);
//		List<Entity> containersList = Lists.newArrayList();
//		containersList.add(spaceEntity);
//		when(serviceFacade.fetchContainers(PROJECT_ID)).thenReturn(containersList);
//		when(spaceEntity.getKey()).thenReturn(ROOM_GUID);
//
//		BimLayer containerLayer = mock(BimLayer.class);
//		when(containerLayer.getClassName()).thenReturn(ROOM_CLASSNAME);
//		when(persistence.findContainer()).thenReturn(containerLayer);
//		when(bimDataView.getIdFromGlobalId(ROOM_GUID, ROOM_CLASSNAME)).thenReturn(Long.valueOf(-1));
//
//		// when
//		exporter.export(catalog, PROJECT_ID);
//
//		// then
//		InOrder inOrder = inOrder(bimDataView, serviceFacade, persistence);
//		inOrder.verify(serviceFacade).fetchContainers(PROJECT_ID);
//		inOrder.verify(persistence).findContainer();
//		inOrder.verify(bimDataView).getIdFromGlobalId(ROOM_GUID, ROOM_CLASSNAME);
//		inOrder.verify(serviceFacade).commitTransaction();
//		
//		verifyNoMoreInteractions(serviceFacade, persistence);
//		verifyZeroInteractions(bimDataView);
//	}
//
//	@Test
//	public void ifThereIsOneSpaceButTheCatalogIsEmptyDoNothing() throws Exception {
//		// given
//		Catalog catalog = mock(Catalog.class);
//		Iterable<EntityDefinition> entities = Lists.newArrayList();
//		when(catalog.getEntitiesDefinitions()).thenReturn(entities);
//
//		Entity spaceEntity = mock(Entity.class);
//		List<Entity> containersList = Lists.newArrayList();
//		containersList.add(spaceEntity);
//		when(serviceFacade.fetchContainers(PROJECT_ID)).thenReturn(containersList);
//		when(spaceEntity.getKey()).thenReturn(ROOM_GUID);
//
//		BimLayer containerLayer = mock(BimLayer.class);
//		when(containerLayer.getClassName()).thenReturn(ROOM_CLASSNAME);
//		when(persistence.findContainer()).thenReturn(containerLayer);
//		when(bimDataView.getIdFromGlobalId(ROOM_GUID, ROOM_CLASSNAME)).thenReturn(Long.valueOf(roomId));
//
//		// when
//		exporter.export(catalog, PROJECT_ID);
//
//		// then
//		InOrder inOrder = inOrder(bimDataView, serviceFacade, persistence);
//		inOrder.verify(serviceFacade).fetchContainers(PROJECT_ID);
//		inOrder.verify(persistence).findContainer();
//		inOrder.verify(bimDataView).getIdFromGlobalId(ROOM_GUID, ROOM_CLASSNAME);
//		inOrder.verify(serviceFacade).commitTransaction();
//		verifyNoMoreInteractions(serviceFacade, persistence);
//		verifyZeroInteractions(bimDataView);
//	}
//
//}
