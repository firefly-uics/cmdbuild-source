package unit.logic;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.query.clause.where.AndWhereClause;
import org.cmdbuild.dao.query.clause.where.OrWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.logic.FilterMapper;
import org.cmdbuild.logic.JSONFilterMapper;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class JSONFilterMapperTest {

	private CMClass entryType;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		DBAttribute attr1 = new DBAttribute("attr1", new IntegerAttributeType(), null);
		DBAttribute attr2 = new DBAttribute("attr2", new IntegerAttributeType(), null);
		Iterable<DBAttribute> attributes = new ArrayList<DBAttribute>();
		entryType = mock(CMClass.class);
		when((Iterable<DBAttribute>) entryType.getAttributes()).thenReturn(Lists.newArrayList(attr1, attr2));
		when(entryType.getName()).thenReturn("Clazz");
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyFilterShouldThrowException() throws Exception {
		// given
		String filter = "";
		FilterMapper filterMapper = new JSONFilterMapper(filter, entryType);

		// when
		WhereClause whereClause = filterMapper.deserialize();
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullFilterShouldThrowException() throws Exception {
		// given
		String filter = null;
		FilterMapper filterMapper = new JSONFilterMapper(filter, entryType);

		// when
		WhereClause whereClause = filterMapper.deserialize();
	}

	@Test(expected = IllegalArgumentException.class)
	public void malformedJSONStringShouldThrowException() throws Exception {
		// given
		String filter = "{malformedJsonObject}";
		FilterMapper filterMapper = new JSONFilterMapper(filter, entryType);

		// when
		WhereClause whereClause = filterMapper.deserialize();
	}

	@Test(expected = IllegalArgumentException.class)
	public void malformedFilterShouldThrowException() throws Exception {
		// given
		String filter = "{not_expected_key: value}";
		FilterMapper filterMapper = new JSONFilterMapper(filter, entryType);

		// when
		WhereClause whereClause = filterMapper.deserialize();
	}

	@Test
	public void shouldSuccessfullyDeserializeGlobalFilter() throws Exception {
		//given
		String globalFilter = "{filter: {simple: {attribute: age, operator: greater, value: [5]}}, " +
				"fullTextQuery: test}";
		
		//when
		FilterMapper filterMapper = new JSONFilterMapper(globalFilter, entryType);
		WhereClause whereClause = filterMapper.deserialize();

		//then
		assertTrue(whereClause instanceof AndWhereClause);
	}
	
	@Test
	public void globalFilterContainingOnlyFullTextQueryMustReturnOrWhereClauseIfMoreThanOneAttribute() throws Exception {
		//given
		String globalFilter = "{fullTextQuery: test}";
		
		//when
		FilterMapper filterMapper = new JSONFilterMapper(globalFilter, entryType);
		WhereClause whereClause = filterMapper.deserialize();
		
		//then
		assertTrue(whereClause instanceof OrWhereClause);
	}
	
}