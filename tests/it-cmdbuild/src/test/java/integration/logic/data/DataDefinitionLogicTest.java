package integration.logic.data;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.AttributeDTO;
import org.cmdbuild.logic.data.AttributeDTO.AttributeDTOBuilder;
import org.cmdbuild.logic.data.ClassDTO;
import org.cmdbuild.logic.data.ClassDTO.ClassDTOBuilder;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.junit.Before;

import utils.IntegrationTestBase;

public abstract class DataDefinitionLogicTest extends IntegrationTestBase {

	private DataDefinitionLogic dataDefinitionLogic;

	@Before
	public void createDataDefinitionLogic() throws Exception {
		// TODO add privileges management as database designer
		dataDefinitionLogic = new DataDefinitionLogic(dbDataView());
	}

	protected DataDefinitionLogic dataDefinitionLogic() {
		return dataDefinitionLogic;
	}

	protected CMDataView dataView() {
		return dbDataView();
	}

	/*
	 * Utilities
	 */

	protected static ClassDTO a(final ClassDTOBuilder classDTO) {
		return classDTO.build();
	}

	protected static ClassDTOBuilder newClass(final String name) {
		return ClassDTO.newClassDTO() //
				.withName(name);
	}

	protected static AttributeDTO a(final AttributeDTOBuilder attributeDTO) {
		return attributeDTO.build();
	}

	protected static AttributeDTOBuilder newAttribute(final String name) {
		return AttributeDTO.newAttributeDTO() //
				.withName(name);
	}

}