package unit.logic.bim;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.StorableProject;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.CmProject;
import org.cmdbuild.services.bim.BimStoreManager;
import org.cmdbuild.services.bim.DefaultBimPersistence;
import org.cmdbuild.services.bim.RelationPersistence;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.common.collect.Lists;

public class DefaultBimDataPersistenceTest {

	private static final String PROJECTID = "projectId";
	private static final String THE_CLASS = "classname";
	private BimPersistence dataPersistence;
	private BimStoreManager storeManager;
	private RelationPersistence relationPersistence;

	@Before
	public void setUp() throws Exception {
		storeManager = mock(BimStoreManager.class);
		relationPersistence = mock(RelationPersistence.class);
		dataPersistence = new DefaultBimPersistence(storeManager, relationPersistence);
	}

	@Test
	public void newProjectInfoSaved() throws Exception {
		// given
		ArgumentCaptor<StorableProject> storableCaptor = ArgumentCaptor.forClass(StorableProject.class);
		CmProject project = mock(CmProject.class);
		when(project.getProjectId()).thenReturn(PROJECTID);

		// when
		dataPersistence.saveProject(project);

		// then
		InOrder inOrder = inOrder(storeManager, relationPersistence);
		inOrder.verify(storeManager).write(storableCaptor.capture());
		inOrder.verify(storeManager).read(PROJECTID);
		assertTrue(storableCaptor.getValue().getProjectId().equals(PROJECTID));
		verifyNoMoreInteractions(storeManager);
		verifyZeroInteractions(relationPersistence);
	}

	@Test
	public void alreadyExistingProjectInfoSaved() throws Exception {
		// given
		ArgumentCaptor<StorableProject> storableCaptor = ArgumentCaptor.forClass(StorableProject.class);
		CmProject project = mock(CmProject.class);
		when(project.getProjectId()).thenReturn(PROJECTID);

		// when
		dataPersistence.saveProject(project);

		// then
		InOrder inOrder = inOrder(storeManager, relationPersistence);
		inOrder.verify(storeManager).write(storableCaptor.capture());
		inOrder.verify(storeManager).read(PROJECTID);
		assertTrue(storableCaptor.getValue().getProjectId().equals(PROJECTID));
		verifyNoMoreInteractions(storeManager);
		verifyZeroInteractions(relationPersistence);
	}

	@Test
	public void updateAttributesOfAnExistingProject() throws Exception {
		// given
		ArgumentCaptor<StorableProject> storableCaptor = ArgumentCaptor.forClass(StorableProject.class);

		CmProject oldProjectInfo = mock(CmProject.class);
		when(oldProjectInfo.getProjectId()).thenReturn(PROJECTID);
		when(oldProjectInfo.getName()).thenReturn("Name");
		when(oldProjectInfo.getDescription()).thenReturn("oldDescription");
		when(oldProjectInfo.isActive()).thenReturn(false);

		// when
		dataPersistence.saveProject(oldProjectInfo);

		// then
		InOrder inOrder = inOrder(storeManager, relationPersistence);
		inOrder.verify(storeManager).write(storableCaptor.capture());
		inOrder.verify(storeManager).read(storableCaptor.getValue().getProjectId());

		verifyNoMoreInteractions(storeManager);
		verifyZeroInteractions(relationPersistence);
	}

	@Test
	public void projectInfoDisabled() throws Exception {
		// given
		CmProject projectToDisable = mock(CmProject.class);
		when(projectToDisable.getProjectId()).thenReturn(PROJECTID);

		// when
		dataPersistence.disableProject(projectToDisable);

		// then
		InOrder inOrder = inOrder(storeManager, relationPersistence);
		inOrder.verify(storeManager).disableProject(PROJECTID);

		verifyNoMoreInteractions(storeManager);
		verifyZeroInteractions(relationPersistence);
	}

	@Test
	public void projectInfoEnabled() throws Exception {
		// given
		CmProject projectToDisable = mock(CmProject.class);
		when(projectToDisable.getProjectId()).thenReturn(PROJECTID);

		// when
		dataPersistence.enableProject(projectToDisable);

		// then
		InOrder inOrder = inOrder(storeManager, relationPersistence);
		inOrder.verify(storeManager).enableProject(PROJECTID);

		verifyNoMoreInteractions(storeManager);
		verifyZeroInteractions(relationPersistence);
	}

	@Test
	public void readAllProjectInfo() throws Exception {
		// given
		ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<String> rootCaptor = ArgumentCaptor.forClass(String.class);

		List<StorableProject> projects = Lists.newArrayList();

		StorableProject project = new StorableProject();
		project.setProjectId(PROJECTID);
		project.setCardId(new Long("111"));

		projects.add(project);
		when(storeManager.readAll()).thenReturn(projects);
		when(storeManager.findRoot()).thenReturn(new BimLayer("Root"));
		when(relationPersistence.readRelations(idCaptor.capture(), rootCaptor.capture())).thenReturn(null);
		when(storeManager.read(PROJECTID)).thenReturn(project);

		// when
		dataPersistence.readAll();

		// then
		InOrder inOrder = inOrder(storeManager, relationPersistence);
		inOrder.verify(storeManager).readAll();
		inOrder.verify(storeManager).read(PROJECTID);
		inOrder.verify(storeManager).findRoot();
		inOrder.verify(relationPersistence).readRelations(idCaptor.getValue(), rootCaptor.getValue());

		verifyNoMoreInteractions(storeManager, relationPersistence);
	}

	@Test
	public void readAllLayer() throws Exception {
		// given
		List<BimLayer> mappers = Lists.newArrayList();
		mappers.add(new BimLayer(THE_CLASS));
		when(storeManager.readAllLayers()).thenReturn(mappers);

		// when
		List<BimLayer> list = (List<BimLayer>) dataPersistence.listLayers();

		// then
		InOrder inOrder = inOrder(relationPersistence, storeManager);
		inOrder.verify(storeManager).readAllLayers();
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).getClassName().equals(THE_CLASS));
		verifyNoMoreInteractions(storeManager);
		verifyZeroInteractions(relationPersistence);
	}

	// @Test
	// public void changeExistingLayerToActive() throws Exception {
	// // given
	// BimLayer Layer = new BimLayer(THE_CLASS);
	// Layer.setActive(false);
	// ArgumentCaptor<Storable> storableCaptor =
	// ArgumentCaptor.forClass(Storable.class);
	// when(layerStore.read(storableCaptor.capture())).thenReturn(Layer);
	//
	// // when
	// dataPersistence.saveActiveStatus(THE_CLASS, "true");
	//
	// // then
	// InOrder inOrder = inOrder(projectInfoStore, layerStore);
	//
	// inOrder.verify(layerStore).read(any(Storable.class));
	// assertTrue(Layer.isActive());
	// assertThat(storableCaptor.getValue().getIdentifier(),
	// equalTo(THE_CLASS));
	//
	// inOrder.verify(layerStore).update(Layer);
	//
	// verifyNoMoreInteractions(layerStore);
	// verifyZeroInteractions(projectInfoStore);
	// }
	//
	// @Test
	// public void changeExistingLayerToExport() throws Exception {
	// // given
	// BimLayer Layer = new BimLayer(THE_CLASS);
	// Layer.setActive(false);
	// ArgumentCaptor<Storable> storableCaptor =
	// ArgumentCaptor.forClass(Storable.class);
	// when(layerStore.read(storableCaptor.capture())).thenReturn(Layer);
	//
	// // when
	// dataPersistence.saveExportStatus(THE_CLASS, "true");
	//
	// // then
	// InOrder inOrder = inOrder(projectInfoStore, layerStore);
	//
	// inOrder.verify(layerStore).read(any(Storable.class));
	// assertTrue(Layer.isExport());
	// assertThat(storableCaptor.getValue().getIdentifier(),
	// equalTo(THE_CLASS));
	//
	// inOrder.verify(layerStore).update(Layer);
	//
	// verifyNoMoreInteractions(layerStore);
	// verifyZeroInteractions(projectInfoStore);
	// }
	//
	// @Test
	// public void createNewActiveLayer() throws Exception {
	// // given
	// BimLayer layer = new BimLayer(THE_CLASS);
	//
	// ArgumentCaptor<Storable> storableCaptor =
	// ArgumentCaptor.forClass(Storable.class);
	// ArgumentCaptor<BimLayer> layerCaptor =
	// ArgumentCaptor.forClass(BimLayer.class);
	//
	// when(layerStore.read(storableCaptor.capture())).thenReturn(null);
	// when(layerStore.create(layerCaptor.capture())).thenReturn(layer);
	//
	// // when
	// dataPersistence.saveActiveStatus(THE_CLASS, "true");
	//
	// // then
	// InOrder inOrder = inOrder(projectInfoStore, layerStore);
	//
	// inOrder.verify(layerStore).read(any(Storable.class));
	// assertThat(storableCaptor.getValue().getIdentifier(),
	// equalTo(THE_CLASS));
	//
	// inOrder.verify(layerStore).create(any(BimLayer.class));
	// assertTrue(layerCaptor.getValue().isActive());
	//
	// verifyNoMoreInteractions(layerStore);
	// verifyZeroInteractions(projectInfoStore);
	// }
	//
	// @Test
	// public void createNewExportLayer() throws Exception {
	// // given
	// BimLayer layer = new BimLayer(THE_CLASS);
	// ArgumentCaptor<Storable> storableCaptor =
	// ArgumentCaptor.forClass(Storable.class);
	// ArgumentCaptor<BimLayer> layerCaptor =
	// ArgumentCaptor.forClass(BimLayer.class);
	// when(layerStore.read(storableCaptor.capture())).thenReturn(null);
	// when(layerStore.create(layerCaptor.capture())).thenReturn(layer);
	//
	// // when
	// dataPersistence.saveExportStatus(THE_CLASS, "true");
	//
	// // then
	// InOrder inOrder = inOrder(projectInfoStore, layerStore);
	//
	// inOrder.verify(layerStore).read(any(Storable.class));
	// assertThat(storableCaptor.getValue().getIdentifier(),
	// equalTo(THE_CLASS));
	//
	// inOrder.verify(layerStore).create(any(BimLayer.class));
	// assertTrue(layerCaptor.getValue().isExport());
	//
	// verifyNoMoreInteractions(layerStore);
	// verifyZeroInteractions(projectInfoStore);
	// }
	//
	// @Test
	// public void bimRootFound() throws Exception {
	// // given
	// List<BimLayer> layers = Lists.newArrayList();
	// BimLayer b1 = new BimLayer(THE_CLASS);
	// b1.setRoot(true);
	// BimLayer b2 = new BimLayer("className2");
	// b2.setRoot(false);
	// BimLayer b3 = new BimLayer("className3");
	// b3.setRoot(false);
	// layers.add(b1);
	// layers.add(b2);
	// layers.add(b3);
	// when(layerStore.list()).thenReturn(layers);
	//
	// // when
	// BimLayer theRoot = dataPersistence.findRoot();
	//
	// // then
	// InOrder inOrder = inOrder(projectInfoStore, layerStore);
	// inOrder.verify(layerStore).list();
	//
	// assertThat(theRoot.getClassName(), equalTo(THE_CLASS));
	// assertTrue(theRoot.isRoot());
	//
	// verifyNoMoreInteractions(layerStore);
	// verifyZeroInteractions(projectInfoStore);
	// }
	//
	// @Test
	// public void bimRootNotFound() throws Exception {
	// // given
	// List<BimLayer> mappers = Lists.newArrayList();
	// BimLayer b1 = new BimLayer(THE_CLASS);
	// b1.setRoot(false);
	// BimLayer b2 = new BimLayer("className2");
	// b2.setRoot(false);
	// BimLayer b3 = new BimLayer("className3");
	// b3.setRoot(false);
	// mappers.add(b1);
	// mappers.add(b2);
	// mappers.add(b3);
	// when(layerStore.list()).thenReturn(mappers);
	//
	// // when
	// BimLayer theRoot = dataPersistence.findRoot();
	//
	// // then
	// InOrder inOrder = inOrder(projectInfoStore, layerStore);
	// inOrder.verify(layerStore).list();
	//
	// assertTrue(theRoot == null);
	//
	// verifyNoMoreInteractions(layerStore);
	// verifyZeroInteractions(projectInfoStore);
	// }
	//
	// @Test
	// public void bimRootNotFoundInAnEmptyStore() throws Exception {
	// // given
	// List<BimLayer> mappers = Lists.newArrayList();
	// when(layerStore.list()).thenReturn(mappers);
	//
	// // when
	// BimLayer theRoot = dataPersistence.findRoot();
	//
	// // then
	// InOrder inOrder = inOrder(projectInfoStore, layerStore);
	// inOrder.verify(layerStore).list();
	//
	// assertTrue(theRoot == null);
	//
	// verifyNoMoreInteractions(layerStore);
	// verifyZeroInteractions(projectInfoStore);
	// }
	//
	// @Test
	// public void existingBimMapperSetToBimRoot() throws Exception {
	// // given
	// List<BimLayer> mappers = Lists.newArrayList();
	// BimLayer b1 = new BimLayer(THE_CLASS);
	// b1.setRoot(false);
	// BimLayer b2 = new BimLayer("className2");
	// b2.setRoot(false);
	// BimLayer b3 = new BimLayer("className3");
	// b3.setRoot(false);
	// mappers.add(b1);
	// mappers.add(b2);
	// mappers.add(b3);
	// when(layerStore.list()).thenReturn(mappers);
	//
	// ArgumentCaptor<Storable> storableCaptor =
	// ArgumentCaptor.forClass(Storable.class);
	// when(layerStore.read(storableCaptor.capture())).thenReturn(b1);
	//
	// // when
	// dataPersistence.saveRoot(THE_CLASS, true);
	//
	// // then
	// InOrder inOrder = inOrder(projectInfoStore, layerStore);
	// inOrder.verify(layerStore).read(any(BimLayer.class));
	// assertThat(storableCaptor.getValue().getIdentifier(),
	// equalTo(THE_CLASS));
	//
	// assertTrue(b1.isRoot());
	// inOrder.verify(layerStore).update(b1);
	//
	// verifyNoMoreInteractions(layerStore);
	// verifyZeroInteractions(projectInfoStore);
	//
	// }
	//
	// @Test
	// public void saveRootOnNotExistingBimMapperCallCreateStorable() throws
	// Exception {
	// // given
	// List<BimLayer> mappers = Lists.newArrayList();
	// BimLayer b1 = new BimLayer(THE_CLASS);
	// b1.setRoot(false);
	// BimLayer b2 = new BimLayer("className2");
	// b2.setRoot(false);
	// BimLayer b3 = new BimLayer("className3");
	// b3.setRoot(false);
	// mappers.add(b1);
	// mappers.add(b2);
	// mappers.add(b3);
	// when(layerStore.list()).thenReturn(mappers);
	//
	// ArgumentCaptor<Storable> storableCaptor =
	// ArgumentCaptor.forClass(Storable.class);
	// when(layerStore.read(storableCaptor.capture())).thenReturn(null);
	//
	// // when
	// dataPersistence.saveRoot("classNotInTheStore", true);
	//
	// // then
	// InOrder inOrder = inOrder(projectInfoStore, layerStore);
	// inOrder.verify(layerStore).read(any(BimLayer.class));
	// assertThat(storableCaptor.getValue().getIdentifier(),
	// equalTo("classNotInTheStore"));
	//
	// inOrder.verify(layerStore).create(any(BimLayer.class));
	//
	// verifyNoMoreInteractions(layerStore);
	// verifyZeroInteractions(projectInfoStore);
	// }
	//
	// @Test
	// public void saveRootOnExistingBimMapperCallUpdateStorable() throws
	// Exception {
	// // given
	// List<BimLayer> mappers = Lists.newArrayList();
	// BimLayer b1 = new BimLayer(THE_CLASS);
	// b1.setRoot(false);
	// mappers.add(b1);
	// when(layerStore.list()).thenReturn(mappers);
	//
	// ArgumentCaptor<Storable> storableCaptor =
	// ArgumentCaptor.forClass(Storable.class);
	// when(layerStore.read(storableCaptor.capture())).thenReturn(b1);
	//
	// // when
	// dataPersistence.saveRoot(THE_CLASS, true);
	//
	// // then
	// InOrder inOrder = inOrder(projectInfoStore, layerStore);
	// inOrder.verify(layerStore).read(any(BimLayer.class));
	// assertThat(storableCaptor.getValue().getIdentifier(),
	// equalTo(THE_CLASS));
	// inOrder.verify(layerStore).update(b1);
	// verifyNoMoreInteractions(layerStore);
	// verifyZeroInteractions(projectInfoStore);
	// }
	//
}
