package integration.logic.data;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Attribute.AttributeBuilder;
import org.cmdbuild.model.data.Class;
import org.cmdbuild.model.data.Class.ClassBuilder;
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

	protected static Class a(final ClassBuilder classBuilder) {
		return classBuilder.build();
	}

	protected static ClassBuilder newClass(final String name) {
		return Class.newClass() //
				.withName(name);
	}

	protected static Attribute a(final AttributeBuilder attributeBuilder) {
		return attributeBuilder.build();
	}

	protected static AttributeBuilder newAttribute(final String name) {
		return Attribute.newAttribute() //
				.withName(name);
	}

}