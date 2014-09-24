package unit.rest.reflect;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.reflect.MultivaluedMapFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.reflect.Reflection;

@RunWith(MockitoJUnitRunner.class)
public class MultivaluedMapFilterTest {

	public static interface Dummy {

		void dummy();

		void dummy( //
				@FormParam("foo") String foo, //
				MultivaluedMap<String, String> values //
		);

		void dummy( //
				@FormParam("foo") String foo, //
				@FormParam("bar") String bar, //
				MultivaluedMap<String, String> values, //
				MultivaluedMap<String, String> moreValues //
		);

	}

	private Dummy delegate;
	private MultivaluedMapFilter<Dummy> underTest;
	private Dummy proxy;

	@Captor
	private ArgumentCaptor<MultivaluedMap<String, String>> multivaluedMapCaptor;

	@Before
	public void setUp() throws Exception {
		delegate = mock(Dummy.class);
		underTest = MultivaluedMapFilter.of(delegate);
		proxy = Reflection.newProxy(Dummy.class, underTest);
	}

	@Test
	public void noArgumentsShouldNotThrowException() throws Exception {
		// when
		proxy.dummy();
	}

	@Test
	public void singleMultivaluedMapAndSingleFormParam() throws Exception {
		// given
		final MultivaluedMap<String, String> values = new MetadataMap<String, String>();
		ChainablePutMap.of(values) //
				.chainablePut("foo", asList("oof")) //
				.chainablePut("bar", asList("rab")) //
				.chainablePut("baz", asList("zab"));

		// when
		proxy.dummy("oof", values);

		// then
		verify(delegate).dummy(eq("oof"), multivaluedMapCaptor.capture());
		final MultivaluedMap<String, String> captured = multivaluedMapCaptor.getValue();
		assertThat(captured, not(hasKey("foo")));
		assertThat(captured, hasEntry("bar", asList("rab")));
		assertThat(captured, hasEntry("baz", asList("zab")));
	}

	@Test
	public void multipleMultivaluedMapAndMultipleFormParam() throws Exception {
		// given
		final MultivaluedMap<String, String> values = new MetadataMap<String, String>();
		ChainablePutMap.of(values) //
				.chainablePut("foo", asList("oof")) //
				.chainablePut("bar", asList("rab")) //
				.chainablePut("baz", asList("zab"));
		final MultivaluedMap<String, String> moreValues = new MetadataMap<String, String>(values);

		// when
		proxy.dummy("oof", "rab", values, moreValues);

		// then
		verify(delegate).dummy(eq("oof"), eq("rab"), multivaluedMapCaptor.capture(), multivaluedMapCaptor.capture());
		final MultivaluedMap<String, String> capturedValues = multivaluedMapCaptor.getAllValues().get(0);
		assertThat(capturedValues, not(hasKey("foo")));
		assertThat(capturedValues, not(hasKey("bar")));
		assertThat(capturedValues, hasEntry("baz", asList("zab")));
		final MultivaluedMap<String, String> capturedMoreValues = multivaluedMapCaptor.getAllValues().get(1);
		assertThat(capturedMoreValues, not(hasKey("foo")));
		assertThat(capturedMoreValues, not(hasKey("bar")));
		assertThat(capturedMoreValues, hasEntry("baz", asList("zab")));
	}

}
