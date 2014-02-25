package unit.logic.bim;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.StorableProject;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimDataPersistence.CmProject;
import org.cmdbuild.services.bim.DefaultBimDataPersistence;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.common.collect.Lists;

public class DefaultBimDataPersistenceTest {

	private static final String PROJECTID = "projectId";
	private static final String THE_CLASS = "className";
	private Store<StorableProject> projectInfoStore;
	private Store<BimLayer> layerStore;
	private CMDataView dataView;
	private BimDataPersistence dataPersistence;

	@Before
	public void setUp() throws Exception {
		projectInfoStore = mock(Store.class);
		layerStore = mock(Store.class);
		dataView = mock(CMDataView.class);
		dataPersistence = new DefaultBimDataPersistence(projectInfoStore, layerStore, dataView);
	}

	@Test
	public void newProjectInfoSaved() throws Exception {
		// given
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		CmProject projectInfo = mock(CmProject.class);
		when(projectInfo.getProjectId()).thenReturn(PROJECTID);
		when(projectInfoStore.read(storableCaptor.capture())).thenReturn(null);

		// when
		dataPersistence.saveProject(projectInfo);

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);
		inOrder.verify(projectInfoStore).read(any(Storable.class));
		inOrder.verify(projectInfoStore).create(any(StorableProject.class));
		verifyNoMoreInteractions(projectInfoStore);
		verifyZeroInteractions(layerStore);
	}

	@Test
	public void alreadyExistingProjectInfoSaved() throws Exception {
		// given
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		CmProject project = mock(CmProject.class);
		when(project.getProjectId()).thenReturn(PROJECTID);

		StorableProject storableProject = mock(StorableProject.class);
		when(projectInfoStore.read(storableCaptor.capture())).thenReturn(storableProject);

		// when
		dataPersistence.saveProject(project);

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);
		inOrder.verify(projectInfoStore).read(any(StorableProject.class));
		inOrder.verify(projectInfoStore).update(any(StorableProject.class));

		verifyNoMoreInteractions(projectInfoStore);
		verifyZeroInteractions(layerStore);
	}

	@Test
	public void updateAttributesOfAnExistingProject() throws Exception {
		// given
		ArgumentCaptor<CmProject> storableCaptor = ArgumentCaptor.forClass(CmProject.class);

		CmProject oldProjectInfo = mock(CmProject.class);
		when(oldProjectInfo.getProjectId()).thenReturn(PROJECTID);
		when(oldProjectInfo.getName()).thenReturn("Name");
		when(oldProjectInfo.getDescription()).thenReturn("oldDescription");
		when(oldProjectInfo.isActive()).thenReturn(false);

		StorableProject newProjectInfo = new StorableProject();
		newProjectInfo.setProjectId(PROJECTID);
		newProjectInfo.setName("Name");
		newProjectInfo.setDescription("newDescription");
		newProjectInfo.setActive(true);
		newProjectInfo.setLastCheckin(new DateTime());

		//when(projectInfoStore.read(storableCaptor.capture())).thenReturn(oldProjectInfo);

		// when
		//dataPersistence.saveProject(newProjectInfo);

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);
		inOrder.verify(projectInfoStore).read(any(Storable.class));
		//inOrder.verify(projectInfoStore).update(oldProjectInfo);

		verifyNoMoreInteractions(projectInfoStore);
		verifyZeroInteractions(layerStore);
	}

	@Test
	public void projectInfoDisabled() throws Exception {
		// given
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);

		StorableProject projectInfo = new StorableProject();
		projectInfo.setProjectId(PROJECTID);
		projectInfo.setActive(true);

		when(projectInfoStore.read(storableCaptor.capture())).thenReturn(projectInfo);

		// when
		dataPersistence.disableProject(null);

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);
		inOrder.verify(projectInfoStore).read(any(Storable.class));
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(PROJECTID));

		assertTrue(!projectInfo.isActive());
		inOrder.verify(projectInfoStore).update(projectInfo);

		verifyNoMoreInteractions(projectInfoStore);
		verifyZeroInteractions(layerStore);
	}

	@Test
	public void projectInfoEnabled() throws Exception {
		// given
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);

		StorableProject projectInfo = new StorableProject();
		projectInfo.setProjectId(PROJECTID);
		projectInfo.setActive(false);

		when(projectInfoStore.read(storableCaptor.capture())).thenReturn(projectInfo);

		// when
		dataPersistence.enableProject(null);

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);
		inOrder.verify(projectInfoStore).read(any(Storable.class));
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(PROJECTID));

		assertTrue(projectInfo.isActive());
		inOrder.verify(projectInfoStore).update(projectInfo);

		verifyNoMoreInteractions(projectInfoStore);
		verifyZeroInteractions(layerStore);
	}

	@Test
	public void readAllProjectInfo() throws Exception {
		// given
		List<StorableProject> projects = Lists.newArrayList();
		projects.add(new StorableProject());
		when(projectInfoStore.list()).thenReturn(projects);

		// when
		List<CmProject> list = (List<CmProject>) dataPersistence.readAll();

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);
		inOrder.verify(projectInfoStore).list();
		assertTrue(list.size() == 1);
		verifyNoMoreInteractions(projectInfoStore);
		verifyZeroInteractions(layerStore);
	}

	@Test
	public void readAllLayer() throws Exception {
		// given
		List<BimLayer> mappers = Lists.newArrayList();
		mappers.add(new BimLayer(THE_CLASS));
		when(layerStore.list()).thenReturn(mappers);

		// when
		List<BimLayer> list = dataPersistence.listLayers();

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);
		inOrder.verify(layerStore).list();
		assertTrue(list.size() == 1);
		assertThat(list.get(0).getClassName(), equalTo(THE_CLASS));
		verifyNoMoreInteractions(layerStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void changeExistingLayerToActive() throws Exception {
		// given
		BimLayer Layer = new BimLayer(THE_CLASS);
		Layer.setActive(false);
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		when(layerStore.read(storableCaptor.capture())).thenReturn(Layer);

		// when
		dataPersistence.saveActiveStatus(THE_CLASS, "true");

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);

		inOrder.verify(layerStore).read(any(Storable.class));
		assertTrue(Layer.isActive());
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(THE_CLASS));

		inOrder.verify(layerStore).update(Layer);

		verifyNoMoreInteractions(layerStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void changeExistingLayerToExport() throws Exception {
		// given
		BimLayer Layer = new BimLayer(THE_CLASS);
		Layer.setActive(false);
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		when(layerStore.read(storableCaptor.capture())).thenReturn(Layer);

		// when
		dataPersistence.saveExportStatus(THE_CLASS, "true");

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);

		inOrder.verify(layerStore).read(any(Storable.class));
		assertTrue(Layer.isExport());
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(THE_CLASS));

		inOrder.verify(layerStore).update(Layer);

		verifyNoMoreInteractions(layerStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void createNewActiveLayer() throws Exception {
		// given
		BimLayer layer = new BimLayer(THE_CLASS);

		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		ArgumentCaptor<BimLayer> layerCaptor = ArgumentCaptor.forClass(BimLayer.class);

		when(layerStore.read(storableCaptor.capture())).thenReturn(null);
		when(layerStore.create(layerCaptor.capture())).thenReturn(layer);

		// when
		dataPersistence.saveActiveStatus(THE_CLASS, "true");

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);

		inOrder.verify(layerStore).read(any(Storable.class));
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(THE_CLASS));

		inOrder.verify(layerStore).create(any(BimLayer.class));
		assertTrue(layerCaptor.getValue().isActive());

		verifyNoMoreInteractions(layerStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void createNewExportLayer() throws Exception {
		// given
		BimLayer layer = new BimLayer(THE_CLASS);
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		ArgumentCaptor<BimLayer> layerCaptor = ArgumentCaptor.forClass(BimLayer.class);
		when(layerStore.read(storableCaptor.capture())).thenReturn(null);
		when(layerStore.create(layerCaptor.capture())).thenReturn(layer);

		// when
		dataPersistence.saveExportStatus(THE_CLASS, "true");

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);

		inOrder.verify(layerStore).read(any(Storable.class));
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(THE_CLASS));

		inOrder.verify(layerStore).create(any(BimLayer.class));
		assertTrue(layerCaptor.getValue().isExport());

		verifyNoMoreInteractions(layerStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void bimRootFound() throws Exception {
		// given
		List<BimLayer> layers = Lists.newArrayList();
		BimLayer b1 = new BimLayer(THE_CLASS);
		b1.setRoot(true);
		BimLayer b2 = new BimLayer("className2");
		b2.setRoot(false);
		BimLayer b3 = new BimLayer("className3");
		b3.setRoot(false);
		layers.add(b1);
		layers.add(b2);
		layers.add(b3);
		when(layerStore.list()).thenReturn(layers);

		// when
		BimLayer theRoot = dataPersistence.findRoot();

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);
		inOrder.verify(layerStore).list();

		assertThat(theRoot.getClassName(), equalTo(THE_CLASS));
		assertTrue(theRoot.isRoot());

		verifyNoMoreInteractions(layerStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void bimRootNotFound() throws Exception {
		// given
		List<BimLayer> mappers = Lists.newArrayList();
		BimLayer b1 = new BimLayer(THE_CLASS);
		b1.setRoot(false);
		BimLayer b2 = new BimLayer("className2");
		b2.setRoot(false);
		BimLayer b3 = new BimLayer("className3");
		b3.setRoot(false);
		mappers.add(b1);
		mappers.add(b2);
		mappers.add(b3);
		when(layerStore.list()).thenReturn(mappers);

		// when
		BimLayer theRoot = dataPersistence.findRoot();

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);
		inOrder.verify(layerStore).list();

		assertTrue(theRoot == null);

		verifyNoMoreInteractions(layerStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void bimRootNotFoundInAnEmptyStore() throws Exception {
		// given
		List<BimLayer> mappers = Lists.newArrayList();
		when(layerStore.list()).thenReturn(mappers);

		// when
		BimLayer theRoot = dataPersistence.findRoot();

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);
		inOrder.verify(layerStore).list();

		assertTrue(theRoot == null);

		verifyNoMoreInteractions(layerStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void existingBimMapperSetToBimRoot() throws Exception {
		// given
		List<BimLayer> mappers = Lists.newArrayList();
		BimLayer b1 = new BimLayer(THE_CLASS);
		b1.setRoot(false);
		BimLayer b2 = new BimLayer("className2");
		b2.setRoot(false);
		BimLayer b3 = new BimLayer("className3");
		b3.setRoot(false);
		mappers.add(b1);
		mappers.add(b2);
		mappers.add(b3);
		when(layerStore.list()).thenReturn(mappers);

		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		when(layerStore.read(storableCaptor.capture())).thenReturn(b1);

		// when
		dataPersistence.saveRoot(THE_CLASS, true);

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);
		inOrder.verify(layerStore).read(any(BimLayer.class));
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(THE_CLASS));

		assertTrue(b1.isRoot());
		inOrder.verify(layerStore).update(b1);

		verifyNoMoreInteractions(layerStore);
		verifyZeroInteractions(projectInfoStore);

	}

	@Test
	public void saveRootOnNotExistingBimMapperCallCreateStorable() throws Exception {
		// given
		List<BimLayer> mappers = Lists.newArrayList();
		BimLayer b1 = new BimLayer(THE_CLASS);
		b1.setRoot(false);
		BimLayer b2 = new BimLayer("className2");
		b2.setRoot(false);
		BimLayer b3 = new BimLayer("className3");
		b3.setRoot(false);
		mappers.add(b1);
		mappers.add(b2);
		mappers.add(b3);
		when(layerStore.list()).thenReturn(mappers);

		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		when(layerStore.read(storableCaptor.capture())).thenReturn(null);

		// when
		dataPersistence.saveRoot("classNotInTheStore", true);

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);
		inOrder.verify(layerStore).read(any(BimLayer.class));
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo("classNotInTheStore"));

		inOrder.verify(layerStore).create(any(BimLayer.class));

		verifyNoMoreInteractions(layerStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void saveRootOnExistingBimMapperCallUpdateStorable() throws Exception {
		// given
		List<BimLayer> mappers = Lists.newArrayList();
		BimLayer b1 = new BimLayer(THE_CLASS);
		b1.setRoot(false);
		mappers.add(b1);
		when(layerStore.list()).thenReturn(mappers);

		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		when(layerStore.read(storableCaptor.capture())).thenReturn(b1);

		// when
		dataPersistence.saveRoot(THE_CLASS, true);

		// then
		InOrder inOrder = inOrder(projectInfoStore, layerStore);
		inOrder.verify(layerStore).read(any(BimLayer.class));
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(THE_CLASS));
		inOrder.verify(layerStore).update(b1);
		verifyNoMoreInteractions(layerStore);
		verifyZeroInteractions(projectInfoStore);
	}

}
