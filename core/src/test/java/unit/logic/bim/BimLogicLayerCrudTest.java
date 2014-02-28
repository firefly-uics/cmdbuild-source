package unit.logic.bim;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.bim.BimLogic;
import org.cmdbuild.logic.bim.DefaultBimLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimPersistence;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;

public class BimLogicLayerCrudTest {

	private BimPersistence dataPersistence;
	private BimDataModelManager dataModelManager;
	private DataAccessLogic dataAccessLogic;
	private BimLogic bimLogic;
	private static final String CLASSNAME = "className";

	private String ATTRIBUTE_NAME;
	private String ATTRIBUTE_VALUE;

	@Before
	public void setUp() throws Exception {
		dataPersistence = mock(BimPersistence.class);
		dataModelManager = mock(BimDataModelManager.class);
		dataAccessLogic = mock(DataAccessLogic.class);
		bimLogic = new DefaultBimLogic(null, dataPersistence, dataModelManager, null, null, null, dataAccessLogic);
	}


	@Test
	public void readLayerList() throws Exception {
		// given
		final Iterable<? extends CMClass> classes = new ArrayList<CMClass>();
		when(dataAccessLogic.findAllClasses()) //
				.thenAnswer(new Answer<Iterable<? extends CMClass>>() {
					@Override
					public Iterable<? extends CMClass> answer(InvocationOnMock invocation) throws Throwable {

						return classes;
					}
				});
		List<BimLayer> layerList = Lists.newArrayList();
		BimLayer layer = new BimLayer(CLASSNAME);
		layerList.add(layer);
		when(dataPersistence.listLayers()).thenReturn(layerList );

		// when
		bimLogic.readBimLayer();
		//
		// // then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager, dataAccessLogic);

		inOrder.verify(dataPersistence).listLayers();
		inOrder.verify(dataAccessLogic).findAllClasses();

		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerActiveAttributeWithTrueValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "active";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions( dataPersistence, dataModelManager);
	}

	@Test
	public void setContainersetExportLayer() throws Exception {
		// given
		ATTRIBUTE_NAME = "container";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		ATTRIBUTE_NAME = "export";
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, "true");
		inOrder.verify(dataPersistence).saveExportStatus(CLASSNAME, "false");
		inOrder.verify(dataModelManager).addPerimeterAndHeightFieldsIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveContainerStatus(CLASSNAME, "true");

		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, "true");
		inOrder.verify(dataPersistence).saveContainerStatus(CLASSNAME, "false");
		inOrder.verify(dataModelManager).addPositionFieldIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveExportStatus(CLASSNAME, "true");

		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerBimRootAttributeWithTrueValueWithNullOldBimRoot() throws Exception {
		// given
		ATTRIBUTE_NAME = "root";
		ATTRIBUTE_VALUE = "true";
		when(dataPersistence.findRoot()).thenReturn(null);

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, "true");
		inOrder.verify(dataPersistence).findRoot();
		inOrder.verify(dataModelManager).createBimDomainOnClass(CLASSNAME);
		inOrder.verify(dataPersistence).saveRoot(CLASSNAME, true);

		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerBimRootAttributeWithTrueValueWithNotNullOldBimRoot() throws Exception {
		// given
		ATTRIBUTE_NAME = "root";
		ATTRIBUTE_VALUE = "true";
		String OTHER_CLASS = "anotherClass";
		when(dataPersistence.findRoot()).thenReturn(new BimLayer(OTHER_CLASS));

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder( dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, "true");
		inOrder.verify(dataPersistence).findRoot();
		inOrder.verify(dataModelManager).deleteBimDomainOnClass(OTHER_CLASS);
		inOrder.verify(dataPersistence).saveRoot(OTHER_CLASS, false);
		inOrder.verify(dataModelManager).createBimDomainOnClass(CLASSNAME);
		inOrder.verify(dataPersistence).saveRoot(CLASSNAME, true);

		verifyNoMoreInteractions( dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerBimRootAttributeWithFalseValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "root";
		ATTRIBUTE_VALUE = "false";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder( dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).deleteBimDomainOnClass(CLASSNAME);
		inOrder.verify(dataPersistence).saveRoot(CLASSNAME, false);

		verifyNoMoreInteractions( dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerExportAttributeWithTrueValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "export";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder( dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, ATTRIBUTE_VALUE);
		inOrder.verify(dataPersistence).saveContainerStatus(CLASSNAME, "false");
		inOrder.verify(dataModelManager).addPositionFieldIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveExportStatus(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions( dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerExportAttributeWithFalseValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "export";
		ATTRIBUTE_VALUE = "false";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder( dataPersistence, dataModelManager);
		inOrder.verify(dataPersistence).saveExportStatus(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions( dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerContainerAttributeWithTrueValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "container";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder( dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveStatus(CLASSNAME, ATTRIBUTE_VALUE);
		inOrder.verify(dataPersistence).saveExportStatus(CLASSNAME, "false");
		inOrder.verify(dataModelManager).addPerimeterAndHeightFieldsIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveContainerStatus(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions( dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerContainerAttributeWithFalseValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "container";
		ATTRIBUTE_VALUE = "false";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder( dataPersistence, dataModelManager);
		inOrder.verify(dataPersistence).saveContainerStatus(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerUnknownAttribute() throws Exception {
		// given
		String ATTRIBUTE_NAME = "unknown";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		verifyZeroInteractions( dataPersistence, dataModelManager);
	}

}
