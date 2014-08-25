package unit.cxf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.cmdbuild.service.rest.LookupTypeValues;
import org.cmdbuild.service.rest.cxf.CxfLookupValues;
import org.junit.Before;
import org.junit.Test;

public class CxfLookupValuesTest {

	private LookupTypeValues lookupTypeValues;

	private CxfLookupValues cxfLookupValues;

	@Before
	public void setUp() throws Exception {
		lookupTypeValues = mock(LookupTypeValues.class);

		cxfLookupValues = new CxfLookupValues(lookupTypeValues);
	}

	@Test
	public void readAllDelegated() throws Exception {
		// when
		cxfLookupValues.readAll("foo", true, 123, 456);

		// then
		verify(lookupTypeValues).readAll("foo", true, 123, 456);
	}

	@Test
	public void readDelegated() throws Exception {
		// when
		cxfLookupValues.read("foo", 123L);

		// then
		verify(lookupTypeValues).read("foo", 123L);
	}

}
