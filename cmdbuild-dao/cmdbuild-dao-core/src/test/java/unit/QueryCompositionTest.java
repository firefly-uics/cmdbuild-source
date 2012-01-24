package unit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static utils.IsCQL2.cql2;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class QueryCompositionTest {

	private final CMDataView dataView;
	private final DBDriver driver;

	private final QuerySpecsBuilder query;

	public QueryCompositionTest() {
		dataView = mock(CMDataView.class);
		driver = mock(DBDriver.class);
		query = new QuerySpecsBuilder(new DBDataView(driver));
	}

	@Test
	public void emptyQueriesReturnTheWholeDatabase() {
		assertThat(query, is(cql2("SELECT * FROM *")));
	}

	@Test
	public void fromClauseLimitsTheClassesQueried() {
		final CMClass fromClass = mock(CMClass.class);
		when(fromClass.getName()).thenReturn("C");

		assertThat(query.from(fromClass), is(cql2("SELECT * FROM C")));
	}

	@Test
	public void fromClauseCanBeSpecifiedWithAString() {
		final CMClass C = dataView.findClassByName("C"); 
		assertThat(query.from(C), is(cql2("SELECT * FROM C")));
	}

	@Test
	public void selectClauseCanBeSpecifiedByAttributeName() {
		assertThat(query.select("A"), is(cql2("SELECT A FROM *")));
	}

	@Test
	public void selectClauseAllowsMoreThanOneAttribute() {
		assertThat(query.select("A1", "A2"), is(cql2("SELECT A1, A2 FROM *")));
	}
}
