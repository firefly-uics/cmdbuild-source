package integration.driver;

import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.junit.Assert.assertThat;

import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBase;

public class QueryCreatorTest extends IntegrationTestBase {

	private DBClass newClass;

	@Before
	public void createNewClass() {
		newClass = rollbackDriver.createClass(uniqueUUID(), null);
	}

	@Test
	public void shouldBuildQueryWithDefinedAttributes() {
		// given
		final String codeAttributeName = newClass.getCodeAttributeName();
		final String CLASS_ALIAS = "root";
		final QuerySpecs querySpecs = new QuerySpecsBuilder(view).select(codeAttributeName)
				.from(newClass, as(CLASS_ALIAS)).build();

		// when
		final QueryCreator qc = new QueryCreator(querySpecs);

		// then
		final String generatedQuery = qc.getQuery();
		final StringContains sc = new StringContains("SELECT " + CLASS_ALIAS + ".\"" + codeAttributeName + "\"");
		assertThat(generatedQuery, sc);
		assertThat(generatedQuery,
				new StringContains("FROM " + "\"" + newClass.getName() + "\" " + "AS " + CLASS_ALIAS));
	}

}
