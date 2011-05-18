package unit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static utils.IsCQL2.cql2;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.view.CMDataView;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Ignore;
import org.junit.Test;

public class QueryCompositionTest {

	private final Mockery mockContext;
	private final CMDataView dataView;
	private final QuerySpecsBuilder query;

	public QueryCompositionTest() {
		mockContext = new JUnit4Mockery();
		dataView = mockContext.mock(CMDataView.class);
		query = new QuerySpecsBuilder(dataView);
	}

	@Test
	public void emptyQueriesReturnTheWholeDatabase() {
		assertThat(query, is(cql2("SELECT * FROM *")));
	}

	@Ignore
	@Test
	public void fromClauseLimitsTheClassesQueried() {
		final CMClass fromClass = mockContext.mock(CMClass.class);

		mockContext.checking(new Expectations() {{
			oneOf(fromClass).getName(); will(returnValue("C"));
		}});

		assertThat(query.from(fromClass), is(cql2("SELECT * FROM C")));
	}

	@Test
	public void fromClauseCanBeSpecifiedWithAString() {
		assertThat(query.from("C"), is(cql2("SELECT * FROM C")));
	}


	@Ignore
	@Test
	public void selectClauseChangeTheReturnedAttributes() {
		final CMAttribute attribute = mockContext.mock(CMAttribute.class);

		mockContext.checking(new Expectations() {{
			oneOf(attribute).getName(); will(returnValue("A"));
		}});

		assertThat(query.select(attribute), is(cql2("SELECT A FROM *")));
	}

	@Test
	public void selectClauseCanBeSpecifiedByAttributeName() {
		assertThat(query.select("A"), is(cql2("SELECT A FROM *")));
	}

	@Test
	public void selectClauseAllowsMoreThanOneAttribute() {
		assertThat(query.select("A1", "A2"), is(cql2("SELECT A1, A2 FROM *")));
	}


//	@Ignore
//	@Test
//	public void attributesCanBeRelativeToATable() {
//		// this is not the right way to test it...
//		assertThat(query.select("C.A1", "C.A2").from("C"), is(cql2("SELECT C.A1, C.A2 FROM C")));
//	}
}
