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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class QueryCreatorTest extends DriverFixture {

	private DBClass newClass;
	
	public QueryCreatorTest(final String driverBeanName) {
		super(driverBeanName);
	}
	
	@Before
	public void createNewClass() {
		newClass = driver.createClass(uniqueUUID(), null);
	}
	
	@Test
	public void shouldBuildQueryWithDefinedAttributes() {
		//given
		final String codeAttributeName = newClass.getCodeAttributeName();
		final String CLASS_ALIAS = "root";
		QuerySpecs querySpecs = new QuerySpecsBuilder(view).select(codeAttributeName).from(newClass, as(CLASS_ALIAS)).build();
		
		//when
		QueryCreator qc = new QueryCreator(querySpecs);
		
		//then
		String generatedQuery = qc.getQuery();
		StringContains sc = new StringContains("SELECT " + CLASS_ALIAS + ".\"" +  codeAttributeName + "\"");
		assertThat(generatedQuery, sc);
		assertThat(generatedQuery, new StringContains("FROM " + "\"" + newClass.getName() + "\" " + "AS " + CLASS_ALIAS));
	}
	
}
