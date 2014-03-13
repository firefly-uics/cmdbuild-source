package unit.services.bim;

import static org.mockito.Mockito.mock;

import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.DefaultBimFacade;
import org.cmdbuild.services.bim.connector.export.Export;
import org.cmdbuild.services.bim.connector.export.NewExportConnector;
import org.junit.Before;

public class BimExporterTest {

	private static final String PROJECT_ID = "projectId";
	private static final String ROOM_CLASSNAME = "Room";
	//private static final String GUID = "objectGuid";
	private static final String ROOM_GUID = "roomGuid";
	private static final String roomId = "55";

	private final BimDataView bimDataView = mock(BimDataView.class);
	private final DefaultBimFacade serviceFacade = mock(DefaultBimFacade.class);
	private BimPersistence persistence = mock(BimPersistence.class);

	private Export exporter;

	@Before
	public void setUp() {
		exporter = new NewExportConnector(bimDataView, serviceFacade, persistence, null);
	}

	

}
