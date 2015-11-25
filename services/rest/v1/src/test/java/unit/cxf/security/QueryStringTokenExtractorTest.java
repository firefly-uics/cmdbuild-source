package unit.cxf.security;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.cxf.message.Message.QUERY_STRING;
import static org.cmdbuild.service.rest.v1.cxf.security.Token.TOKEN_KEY;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.cxf.message.Message;
import org.cmdbuild.service.rest.v1.cxf.security.QueryStringTokenExtractor;
import org.junit.Test;

import com.google.common.base.Optional;

public class QueryStringTokenExtractorTest {

	@Test
	public void absentWhenQueryStringIsNull() throws Exception {
		// given
		final Message message = mock(Message.class);
		doReturn(null) //
				.when(message).get(any(String.class));

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(message);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(message).get(eq(QUERY_STRING));
	}

	@Test
	public void absentWhenQueryStringIsEmpty() throws Exception {
		// given
		final Message message = mock(Message.class);
		doReturn(EMPTY) //
				.when(message).get(any(String.class));

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(message);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(message).get(eq(QUERY_STRING));
	}

	@Test
	public void absentWhenQueryStringIsDummyNameOnly() throws Exception {
		// given
		final Message message = mock(Message.class);
		doReturn("foo") //
				.when(message).get(any(String.class));

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(message);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(message).get(eq(QUERY_STRING));
	}

	@Test
	public void absentWhenQueryStringIsDummyNamesOnly() throws Exception {
		// given
		final Message message = mock(Message.class);
		doReturn("foo&bar") //
				.when(message).get(any(String.class));

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(message);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(message).get(eq(QUERY_STRING));
	}

	@Test
	public void absentWhenQueryStringIsDummyNameAndValues() throws Exception {
		// given
		final Message message = mock(Message.class);
		doReturn("foo=FOO&bar=BAR") //
				.when(message).get(any(String.class));

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(message);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(message).get(eq(QUERY_STRING));
	}

	@Test
	public void absentWhenQueryStringIsTokenKeyOnly() throws Exception {
		// given
		final Message message = mock(Message.class);
		doReturn(TOKEN_KEY) //
				.when(message).get(any(String.class));

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(message);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(message).get(eq(QUERY_STRING));
	}

	@Test
	public void valuedWithTheFirstOccurrence() throws Exception {
		// given
		final Message message = mock(Message.class);
		doReturn("foo=FOO&" + TOKEN_KEY + "=12345678&bar=BAR&" + TOKEN_KEY + "=abcdefgh&baz=BAZ") //
				.when(message).get(any(String.class));

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(message);

		// then
		assertThat(optional, equalTo(Optional.of("12345678")));
		verify(message).get(eq(QUERY_STRING));
	}

}
