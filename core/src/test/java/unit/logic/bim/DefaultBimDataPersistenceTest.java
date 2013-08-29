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

import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.model.bim.BimMapperInfo;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.DefaultBimDataPersistence;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.common.collect.Lists;

public class DefaultBimDataPersistenceTest {

	private static final String PROJECTID = "projectId";
	private static final String THE_CLASS = "className";
	private Store<BimProjectInfo> projectInfoStore;
	private Store<BimMapperInfo> mapperInfoStore;

	private BimDataPersistence dataPersistence;

	@Before
	public void setUp() throws Exception {
		projectInfoStore = mock(Store.class);
		mapperInfoStore = mock(Store.class);

		dataPersistence = new DefaultBimDataPersistence(projectInfoStore, mapperInfoStore);
	}

	@Test
	public void newProjectInfoSaved() throws Exception {
		// given
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setProjectId(PROJECTID);
		
		when(projectInfoStore.read(storableCaptor.capture())).thenReturn(null);

		// when
		dataPersistence.saveProject(projectInfo);

		// then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);
		inOrder.verify(projectInfoStore).read(any(Storable.class));
		inOrder.verify(projectInfoStore).create(projectInfo);

		verifyNoMoreInteractions(projectInfoStore);
		verifyZeroInteractions(mapperInfoStore);
	}
	
	@Test
	public void alreadyExistingProjectInfoSaved() throws Exception {
		// given
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setProjectId(PROJECTID);
		
		when(projectInfoStore.read(storableCaptor.capture())).thenReturn(projectInfo);

		// when
		dataPersistence.saveProject(projectInfo);

		// then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);
		inOrder.verify(projectInfoStore).read(any(Storable.class));
		inOrder.verify(projectInfoStore).update(projectInfo);

		verifyNoMoreInteractions(projectInfoStore);
		verifyZeroInteractions(mapperInfoStore);
	}

	@Test
	public void projectInfoDisabled() throws Exception {
		// given
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);

		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setProjectId(PROJECTID);
		projectInfo.setActive(true);

		when(projectInfoStore.read(storableCaptor.capture())).thenReturn(projectInfo);

		// when
		dataPersistence.disableProject(PROJECTID);

		// then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);
		inOrder.verify(projectInfoStore).read(any(Storable.class));
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(PROJECTID));

		assertTrue(!projectInfo.isActive());
		inOrder.verify(projectInfoStore).update(projectInfo);

		verifyNoMoreInteractions(projectInfoStore);
		verifyZeroInteractions(mapperInfoStore);
	}

	@Test
	public void projectInfoEnabled() throws Exception {
		// given
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);

		BimProjectInfo projectInfo = new BimProjectInfo();
		projectInfo.setProjectId(PROJECTID);
		projectInfo.setActive(false);

		when(projectInfoStore.read(storableCaptor.capture())).thenReturn(projectInfo);

		// when
		dataPersistence.enableProject(PROJECTID);

		// then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);
		inOrder.verify(projectInfoStore).read(any(Storable.class));
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(PROJECTID));

		assertTrue(projectInfo.isActive());
		inOrder.verify(projectInfoStore).update(projectInfo);

		verifyNoMoreInteractions(projectInfoStore);
		verifyZeroInteractions(mapperInfoStore);
	}

	@Test
	public void readAllProjectInfo() throws Exception {
		// given
		List<BimProjectInfo> projects = Lists.newArrayList();
		projects.add(new BimProjectInfo());
		when(projectInfoStore.list()).thenReturn(projects);

		// when
		List<BimProjectInfo> list = dataPersistence.listProjectInfo();

		// then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);
		inOrder.verify(projectInfoStore).list();
		assertTrue(list.size() == 1);
		verifyNoMoreInteractions(projectInfoStore);
		verifyZeroInteractions(mapperInfoStore);
	}

	@Test
	public void readAllMapperInfo() throws Exception {
		// given
		List<BimMapperInfo> mappers = Lists.newArrayList();
		mappers.add(new BimMapperInfo(THE_CLASS));
		when(mapperInfoStore.list()).thenReturn(mappers);

		// when
		List<BimMapperInfo> list = dataPersistence.listMapperInfo();

		// then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);
		inOrder.verify(mapperInfoStore).list();
		assertTrue(list.size() == 1);
		assertThat(list.get(0).getClassName(), equalTo(THE_CLASS));
		verifyNoMoreInteractions(mapperInfoStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void changeExistingMapperInfoToActive() throws Exception {
		// given
		BimMapperInfo mapperInfo = new BimMapperInfo(THE_CLASS);
		mapperInfo.setActive(false);
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		when(mapperInfoStore.read(storableCaptor.capture())).thenReturn(mapperInfo);

		// when
		dataPersistence.saveActiveStatus(THE_CLASS, "true");

		// then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);

		inOrder.verify(mapperInfoStore).read(any(Storable.class));
		assertTrue(mapperInfo.isActive());
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(THE_CLASS));

		inOrder.verify(mapperInfoStore).update(mapperInfo);

		verifyNoMoreInteractions(mapperInfoStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void createNewActiveMapperInfo() throws Exception {
		// given
		BimMapperInfo mapperInfo = new BimMapperInfo(THE_CLASS);

		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		ArgumentCaptor<BimMapperInfo> mapperCaptor = ArgumentCaptor.forClass(BimMapperInfo.class);

		when(mapperInfoStore.read(storableCaptor.capture())).thenReturn(null);
		when(mapperInfoStore.create(mapperCaptor.capture())).thenReturn(mapperInfo);

		// when
		dataPersistence.saveActiveStatus(THE_CLASS, "true");

		// then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);

		inOrder.verify(mapperInfoStore).read(any(Storable.class));
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(THE_CLASS));

		inOrder.verify(mapperInfoStore).create(any(BimMapperInfo.class));
		assertTrue(mapperCaptor.getValue().isActive());

		verifyNoMoreInteractions(mapperInfoStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void bimRootFound() throws Exception {
		// given
		List<BimMapperInfo> mappers = Lists.newArrayList();
		BimMapperInfo b1 = new BimMapperInfo(THE_CLASS);
		b1.setBimRoot(true);
		BimMapperInfo b2 = new BimMapperInfo("className2");
		b2.setBimRoot(false);
		BimMapperInfo b3 = new BimMapperInfo("className3");
		b3.setBimRoot(false);
		mappers.add(b1);
		mappers.add(b2);
		mappers.add(b3);
		when(mapperInfoStore.list()).thenReturn(mappers);

		// when
		BimMapperInfo theRoot = dataPersistence.findRoot();

		// then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);
		inOrder.verify(mapperInfoStore).list();

		assertThat(theRoot.getClassName(), equalTo(THE_CLASS));
		assertTrue(theRoot.isBimRoot());

		verifyNoMoreInteractions(mapperInfoStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void bimRootNotFound() throws Exception {
		// given
		List<BimMapperInfo> mappers = Lists.newArrayList();
		BimMapperInfo b1 = new BimMapperInfo(THE_CLASS);
		b1.setBimRoot(false);
		BimMapperInfo b2 = new BimMapperInfo("className2");
		b2.setBimRoot(false);
		BimMapperInfo b3 = new BimMapperInfo("className3");
		b3.setBimRoot(false);
		mappers.add(b1);
		mappers.add(b2);
		mappers.add(b3);
		when(mapperInfoStore.list()).thenReturn(mappers);

		// when
		BimMapperInfo theRoot = dataPersistence.findRoot();

		// then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);
		inOrder.verify(mapperInfoStore).list();

		assertTrue(theRoot == null);

		verifyNoMoreInteractions(mapperInfoStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void bimRootNotFoundInAnEmptyStore() throws Exception {
		// given
		List<BimMapperInfo> mappers = Lists.newArrayList();
		when(mapperInfoStore.list()).thenReturn(mappers);

		// when
		BimMapperInfo theRoot = dataPersistence.findRoot();

		// then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);
		inOrder.verify(mapperInfoStore).list();

		assertTrue(theRoot == null);

		verifyNoMoreInteractions(mapperInfoStore);
		verifyZeroInteractions(projectInfoStore);
	}

	@Test
	public void existingBimMapperSetToBimRoot() throws Exception {
		// given
		List<BimMapperInfo> mappers = Lists.newArrayList();
		BimMapperInfo b1 = new BimMapperInfo(THE_CLASS);
		b1.setBimRoot(false);
		BimMapperInfo b2 = new BimMapperInfo("className2");
		b2.setBimRoot(false);
		BimMapperInfo b3 = new BimMapperInfo("className3");
		b3.setBimRoot(false);
		mappers.add(b1);
		mappers.add(b2);
		mappers.add(b3);
		when(mapperInfoStore.list()).thenReturn(mappers);
		
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		when(mapperInfoStore.read(storableCaptor.capture())).thenReturn(b1);
		
		//when
		dataPersistence.saveRoot(THE_CLASS, true);
		
		//then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);
		inOrder.verify(mapperInfoStore).read(any(BimMapperInfo.class));		
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(THE_CLASS));
		
		assertTrue(b1.isBimRoot());
		inOrder.verify(mapperInfoStore).update(b1);
		
		verifyNoMoreInteractions(mapperInfoStore);
		verifyZeroInteractions(projectInfoStore);

	}
	
	@Test
	public void saveRootOnNotExistingBimMapperCallCreateStorable() throws Exception {
		// given
		List<BimMapperInfo> mappers = Lists.newArrayList();
		BimMapperInfo b1 = new BimMapperInfo(THE_CLASS);
		b1.setBimRoot(false);
		BimMapperInfo b2 = new BimMapperInfo("className2");
		b2.setBimRoot(false);
		BimMapperInfo b3 = new BimMapperInfo("className3");
		b3.setBimRoot(false);
		mappers.add(b1);
		mappers.add(b2);
		mappers.add(b3);
		when(mapperInfoStore.list()).thenReturn(mappers);
		
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		when(mapperInfoStore.read(storableCaptor.capture())).thenReturn(null);
		
		//when
		dataPersistence.saveRoot("classNotInTheStore", true);
		
		//then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);
		inOrder.verify(mapperInfoStore).read(any(BimMapperInfo.class));		
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo("classNotInTheStore"));
		
		inOrder.verify(mapperInfoStore).create(any(BimMapperInfo.class));
		
		verifyNoMoreInteractions(mapperInfoStore);
		verifyZeroInteractions(projectInfoStore);
	}
	
	@Test
	public void saveRootOnExistingBimMapperCallUpdateStorable() throws Exception {
		// given
		List<BimMapperInfo> mappers = Lists.newArrayList();
		BimMapperInfo b1 = new BimMapperInfo(THE_CLASS);
		b1.setBimRoot(false);
		mappers.add(b1);
		when(mapperInfoStore.list()).thenReturn(mappers);
		
		ArgumentCaptor<Storable> storableCaptor = ArgumentCaptor.forClass(Storable.class);
		when(mapperInfoStore.read(storableCaptor.capture())).thenReturn(b1);
		
		//when
		dataPersistence.saveRoot(THE_CLASS, true);
		
		//then
		InOrder inOrder = inOrder(projectInfoStore, mapperInfoStore);
		inOrder.verify(mapperInfoStore).read(any(BimMapperInfo.class));		
		assertThat(storableCaptor.getValue().getIdentifier(), equalTo(THE_CLASS));
		inOrder.verify(mapperInfoStore).update(b1);
		verifyNoMoreInteractions(mapperInfoStore);
		verifyZeroInteractions(projectInfoStore);
	}

}
