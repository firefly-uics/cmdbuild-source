package unit.logic.bim;

import static org.mockito.Mockito.mock;

import org.cmdbuild.logic.bim.BimLogic;
import org.cmdbuild.logic.bim.DefaultBimLogic;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.connector.export.Export;
import org.cmdbuild.services.bim.connector.export.ExportPolicy;
import org.cmdbuild.services.bim.connector.export.NewExportConnector;
import org.cmdbuild.services.bim.connector.export.Output;
import org.junit.Before;
import org.junit.Test;

public class ExportConnectorTest {

	private static final Long ID = (long) 999;
	private static final String POID = "poid";
	boolean STATUS = true;
	private BimFacade serviceFacade;
	private BimPersistence dataPersistence;
	private ExportPolicy exportStrategy;
	private Export exportConnector;

	@Before
	public void setUp() throws Exception {
		serviceFacade = mock(BimFacade.class);
		dataPersistence = mock(BimPersistence.class);
		exportStrategy = mock(ExportPolicy.class);
		BimDataView dataView = mock(BimDataView.class);
		exportConnector = new NewExportConnector(dataView , serviceFacade, dataPersistence, exportStrategy, null);
	}

	@Test
	public void testOrderOfCalls() throws Exception {
		
		//given
		final Output output = mock(Output.class);
		
		//when
		exportConnector.export(POID, output);
		
		//then
		
	}

}
