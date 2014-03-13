package unit.logic.bim;

import static org.mockito.Mockito.mock;

import org.cmdbuild.logic.bim.BimLogic;
import org.cmdbuild.logic.bim.DefaultBimLogic;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.connector.export.ExportProjectPolicy;
import org.junit.Before;
import org.junit.Test;

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
	private ExportProjectPolicy exportStrategy;
	private BimLogic bimLogic;

	@Before
	public void setUp() throws Exception {
		serviceFacade = mock(BimFacade.class);
		dataPersistence = mock(BimPersistence.class);
		exportStrategy = mock(ExportProjectPolicy.class);
		bimLogic = new DefaultBimLogic(serviceFacade, dataPersistence, null, null, null, null, exportStrategy);
	}

	@Test
	public void getRevisionForExport() throws Exception {
		//TODO
	}

}
