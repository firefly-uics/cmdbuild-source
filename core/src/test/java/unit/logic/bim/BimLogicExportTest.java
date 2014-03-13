package unit.logic.bim;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.logic.bim.BimLogic;
import org.cmdbuild.logic.bim.DefaultBimLogic;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.CmProject;
import org.cmdbuild.services.bim.connector.export.ExportProjectStrategy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class BimLogicExportTest {

	private static final String EXPORT_ID = "exportId";
	private static final String FILENAME = "ifc";
	private static final String c2 = "22";
	private static final String c1 = "11";
	private static final String IMPORT = "import";
	private static final String EXPORT = "export";
	private static final String ID = "id of pippo";
	private static final String NAME = "pippo";
	private static final String DESCRIPTION = "description of pippo";
	boolean STATUS = true;
	private BimFacade serviceFacade;
	private BimPersistence dataPersistence;
	private ExportProjectStrategy exportStrategy;
	private BimLogic bimLogic;

	@Before
	public void setUp() throws Exception {
		serviceFacade = mock(BimFacade.class);
		dataPersistence = mock(BimPersistence.class);
		exportStrategy = mock(ExportProjectStrategy.class);
		bimLogic = new DefaultBimLogic(serviceFacade, dataPersistence, null, null, null, null, exportStrategy);
	}

	@Test
	public void getRevisionForExport() throws Exception {
		// given
		CmProject value = mock(CmProject.class);
		when(value.getExportProjectId()).thenReturn(EXPORT_ID);
		when(dataPersistence.read(ID)).thenReturn(value);
		
		// when
		bimLogic.getExportProjectId(ID);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence, exportStrategy);
		inOrder.verify(dataPersistence).read(ID);
		
		verifyNoMoreInteractions(dataPersistence);
		verifyZeroInteractions(serviceFacade,exportStrategy);

	}

}
