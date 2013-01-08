package unit.logic.mappers.json;

import static org.junit.Assert.*;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.logic.mappers.SorterMapper;
import org.cmdbuild.logic.mappers.json.JSONSorterMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class JSONSorterMapperTest {
	
	private static final String CLASS_NAME = "mockedClassName";
	private CMClass mockedClass;
	
	@Before
	public void setUp() {
		mockedClass = mock(CMClass.class);
		when(mockedClass.getName()).thenReturn(CLASS_NAME);
	}
	
	@Test
	public void shouldReturnEmptyListIfNullSortersArgument() throws Exception {
		//given
		SorterMapper mapper = new JSONSorterMapper(mockedClass, null);
		
		//when
		List<OrderByClause> orderByClauses = mapper.deserialize();
		
		//then
		assertTrue(orderByClauses.isEmpty());
	}
	
	@Test
	public void shouldReturnEmptyListIfEmptySortersArray() throws Exception {
		//given
		SorterMapper mapper = new JSONSorterMapper(mockedClass, new JSONArray());
		
		//when
		List<OrderByClause> orderByClauses = mapper.deserialize();
		
		//then
		assertTrue(orderByClauses.isEmpty());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfNotValidKeysInSortersArray() throws Exception {
		//given
		JSONArray sorters = new JSONArray();
		sorters.put(new JSONObject("{property: attr1, not_existent_key: ASC}"));
		sorters.put(new JSONObject("{property: attr2, direction: DESC}"));
		SorterMapper mapper = new JSONSorterMapper(mockedClass, sorters);
		
		//when
		List<OrderByClause> orderByClauses = mapper.deserialize();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfNotValidValuesInSortersArray() throws Exception {
		//given
		JSONArray sorters = new JSONArray();
		sorters.put(new JSONObject("{property: attr1, direction: NOT_VALID_VALUE}"));
		sorters.put(new JSONObject("{property: attr2, direction: DESC}"));
		SorterMapper mapper = new JSONSorterMapper(mockedClass, sorters);
		
		//when
		List<OrderByClause> orderByClauses = mapper.deserialize();
	}
	
	@Test
	public void shouldSuccessfullyDeserializeNotEmptySortersArray() throws Exception {
		//given
		JSONArray sorters = new JSONArray();
		sorters.put(new JSONObject("{property: attr1, direction: ASC}"));
		sorters.put(new JSONObject("{property: attr2, direction: DESC}"));
		SorterMapper mapper = new JSONSorterMapper(mockedClass, sorters);
		
		//when
		List<OrderByClause> orderByClauses = mapper.deserialize();
		
		//then
		assertEquals(orderByClauses.size(), 2);
	}
	
}