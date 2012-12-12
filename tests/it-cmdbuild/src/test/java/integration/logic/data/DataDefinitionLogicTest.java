package integration.logic.data;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.DefaultCachingDriver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Attribute.AttributeBuilder;
import org.cmdbuild.model.data.Class;
import org.cmdbuild.model.data.Class.ClassBuilder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.Domain.DomainBuilder;
import org.junit.Before;

import utils.IntegrationTestBase;

public abstract class DataDefinitionLogicTest extends IntegrationTestBase {

	private DataDefinitionLogic dataDefinitionLogic;

	@Override
	protected DBDriver createDriver() {
		return new DefaultCachingDriver(super.createDriver());
	}

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

	protected Class a(final ClassBuilder classBuilder) {
		return classBuilder.build();
	}

	protected Domain a(final DomainBuilder domainBuilder) {
		return domainBuilder.build();
	}

	protected ClassBuilder newClass(final String name) {
		return Class.newClass() //
				.withName(name);
	}

	protected DomainBuilder newDomain() {
		return Domain.newDomain();
	}

	protected Attribute a(final AttributeBuilder attributeBuilder) {
		return attributeBuilder.build();
	}

	protected AttributeBuilder newAttribute(final String name) {
		return Attribute.newAttribute() //
				.withName(name);
	}

}