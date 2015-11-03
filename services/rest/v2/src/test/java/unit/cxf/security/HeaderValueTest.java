package unit.cxf.security;

import static java.util.Arrays.asList;
import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.cxf.message.Message;
import org.cmdbuild.service.rest.v2.cxf.util.Messages.HeaderValue;
import org.junit.Test;

import com.google.common.base.Optional;

public class HeaderValueTest {

	@Test
	public void absentWhenNullHeaders() throws Exception {
		// given
		final Message message = mock(Message.class);
		doReturn(null) //
				.when(message).get(any(String.class));

		// when
		final Optional<String> optional = HeaderValue.of("dummy").apply(message);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(message).get(eq(PROTOCOL_HEADERS));
	}

	@Test
	public void absentWhenEmptyHeaders() throws Exception {
		// given
		final Message message = mock(Message.class);
		doReturn(new HashMap<String, List<String>>()) //
				.when(message).get(any(String.class));

		// when
		final Optional<String> optional = HeaderValue.of("dummy").apply(message);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(message).get(eq(PROTOCOL_HEADERS));
	}

	@Test
	public void absentWhenGettingTokenKeyReturnsEmptyList() throws Exception {
		// given
		final HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
		headers.put("dummy", new ArrayList<String>());
		final Message message = mock(Message.class);
		doReturn(headers) //
				.when(message).get(any(String.class));

		// when
		final Optional<String> optional = HeaderValue.of("dummy").apply(message);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(message).get(eq(PROTOCOL_HEADERS));
	}

	@Test
	public void valuedWithTheFirstElementOfTheList() throws Exception {
		// given
		final HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
		headers.put("dummy", new ArrayList<String>(asList("foo", "bar")));
		final Message message = mock(Message.class);
		doReturn(headers) //
				.when(message).get(any(String.class));

		// when
		final Optional<String> optional = HeaderValue.of("dummy").apply(message);

		// then
		assertThat(optional, equalTo(Optional.of("foo")));
		verify(message).get(eq(PROTOCOL_HEADERS));
	}

}
