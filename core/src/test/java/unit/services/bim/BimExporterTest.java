package unit.services.bim;

import static org.mockito.Mockito.mock;

import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.DefaultBimServiceFacade;
import org.cmdbuild.services.bim.connector.export.DefaultExport;
import org.junit.Before;

public class BimExporterTest {

	private static final String PROJECT_ID = "projectId";
	private static final String ROOM_CLASSNAME = "Room";
	//private static final String GUID = "objectGuid";
	private static final String ROOM_GUID = "roomGuid";
	private static final String roomId = "55";

	private final BimDataView bimDataView = mock(BimDataView.class);
	private final DefaultBimServiceFacade serviceFacade = mock(DefaultBimServiceFacade.class);
	private BimDataPersistence persistence = mock(BimDataPersistence.class);

	private DefaultExport exporter;

	@Before
	public void setUp() {
		exporter = new DefaultExport(bimDataView, serviceFacade, persistence);
	}

	

}
