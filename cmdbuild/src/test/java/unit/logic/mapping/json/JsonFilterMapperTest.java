package unit.logic.mapping.json;

import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.query.clause.where.AndWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.cmdbuild.logic.validation.Validator.ValidationError;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class JsonFilterMapperTest {

	private CMClass mockEntryType;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		final DBAttribute attr1 = new DBAttribute("attr1", new IntegerAttributeType(), null);
		final DBAttribute attr2 = new DBAttribute("attr2", new IntegerAttributeType(), null);
		mockEntryType = mock(CMClass.class);
		when((Iterable<DBAttribute>) mockEntryType.getAttributes()).thenReturn(Lists.newArrayList(attr1, attr2));
		when(mockEntryType.getAttribute(attr1.getName())).thenReturn(attr1);
		when(mockEntryType.getAttribute(attr2.getName())).thenReturn(attr2);
		when(mockEntryType.getName()).thenReturn("Clazz");
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullFilterShouldThrowException() throws Exception {
		// given
		final FilterMapper filterMapper = new JsonFilterMapper(mockEntryType, null);

		// when
		filterMapper.whereClauses();
	}

	@Test(expected = ValidationError.class)
	public void malformedFilterShouldThrowException() throws Exception {
		// given
		final FilterMapper filterMapper = new JsonFilterMapper(mockEntryType, filter("{not_expected_key: value}"));

		// when
		filterMapper.whereClauses();
	}

	@Test
	public void shouldSuccessfullyDeserializeGlobalFilter() throws Exception {
		// given
		final FilterMapper filterMapper = new JsonFilterMapper(mockEntryType,
				filter("{attribute: {simple: {attribute: attr1, operator: greater, value: [5]}}, " + "query: test}"));

		// when
		final WhereClause whereClause = filterMapper.whereClauses();

		// then
		assertTrue(whereClause instanceof AndWhereClause);
	}

	@Test
	public void joinElementsSuccessfullyRead() throws Exception {
		// given
		final FilterMapper filterMapper = new JsonFilterMapper(mockEntryType,
				filter("{relation: [{domain: foo, src: _1, type: any}, {domain: bar, src: _2, type: any}]}"));

		// when
		final Iterable<FilterMapper.JoinElement> joinElements = filterMapper.joinElements();

		// then
		assertThat(size(joinElements), equalTo(2));
	}

	/*
	 * Utilities
	 */

	private JSONObject filter(final String filter) throws Exception {
		return new JSONObject(filter);
	}

}
