package unit.logic.mappers.json;

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
import org.cmdbuild.logic.mappers.FilterMapper;
import org.cmdbuild.logic.mappers.json.JSONFilterMapper;
import org.json.JSONObject;
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
	public void nullFilterShouldThrowException() throws Exception {
		// given
		FilterMapper filterMapper = new JSONFilterMapper(entryType, null);

		// when
		WhereClause whereClause = filterMapper.deserialize();
	}

	@Test(expected = IllegalArgumentException.class)
	public void malformedFilterShouldThrowException() throws Exception {
		// given
		String filter = "{not_expected_key: value}";
		JSONObject filterObject = new JSONObject(filter);
		FilterMapper filterMapper = new JSONFilterMapper(entryType, filterObject);

		// when
		WhereClause whereClause = filterMapper.deserialize();
	}

	@Test
	public void shouldSuccessfullyDeserializeGlobalFilter() throws Exception {
		//given
		String globalFilter = "{filter: {attribute: {simple: {attribute: age, operator: greater, value: [5]}}, " +
				"query: test}}";
		JSONObject globalFilterObject = new JSONObject(globalFilter);
		
		//when
		FilterMapper filterMapper = new JSONFilterMapper(entryType, globalFilterObject);
		WhereClause whereClause = filterMapper.deserialize();

		//then
		assertTrue(whereClause instanceof AndWhereClause);
	}
	
	@Test
	public void globalFilterContainingOnlyFullTextQueryMustReturnOrWhereClauseIfMoreThanOneAttribute() throws Exception {
		//given
		String globalFilter = "{filter: {query: test}}";
		JSONObject globalFilterObject = new JSONObject(globalFilter);
		
		//when
		FilterMapper filterMapper = new JSONFilterMapper(entryType, globalFilterObject);
		WhereClause whereClause = filterMapper.deserialize();
		
		//then
		assertTrue(whereClause instanceof OrWhereClause);
	}
	
}