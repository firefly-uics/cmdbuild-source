package unit.logic.bim;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.ClassBuilder;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.DefaultBimDataModelManager;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class DefaultBimDataModelManagerTest {

	private static final String THE_CLASS = "Edificio";

	private BimDataModelManager dataModelManager;

	private CMDataView dataView;
	private DataDefinitionLogic dataDefinitionLogic;

	@Before
	public void setUp() throws Exception {
		dataDefinitionLogic = mock(DataDefinitionLogic.class);
		dataView = mock(CMDataView.class);
		
		dataModelManager = new DefaultBimDataModelManager(dataView,dataDefinitionLogic);	
	}

	@Test
	public void newBimTableCreated() throws Exception {
		//given
		CMIdentifier identifier = mock(CMIdentifier.class);
		when(identifier.getLocalName()).thenReturn(THE_CLASS);
		when(identifier.getNameSpace()).thenReturn(DefaultBimDataModelManager.BIM_SCHEMA);
		
		when(dataView.findClass(identifier)).thenReturn(null);
		
		//when
		dataModelManager.createBimTableIfNeeded(THE_CLASS);
		
		//then FIXME :( 
		
		InOrder inOrder = inOrder(dataDefinitionLogic, dataView);
		//inOrder.verify(dataView).findClass(identifier);
		//inOrder.verify(dataDefinitionLogic).createOrUpdate(entryType);
		
		
	}


}
