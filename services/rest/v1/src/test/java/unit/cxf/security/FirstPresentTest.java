package unit.cxf.security;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v1.cxf.security.FirstPresent.firstPresent;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.cxf.message.Message;
import org.cmdbuild.service.rest.v1.cxf.security.TokenHandler.TokenExtractor;
import org.junit.Test;

import com.google.common.base.Optional;

public class FirstPresentTest {

	private static class DummyTokenExtractor implements TokenExtractor {

		private final Optional<String> optional;

		public DummyTokenExtractor(final Optional<String> optional) {
			this.optional = optional;
		}

		@Override
		public Optional<String> extract(final Message message) {
			return optional;
		}

	}

	@Test
	public void firstReturned() throws Exception {
		// given
		final DummyTokenExtractor first = new DummyTokenExtractor(Optional.of("foo"));
		final DummyTokenExtractor second = new DummyTokenExtractor(Optional.of("bar"));
		final Message message = mock(Message.class);

		// when
		final Optional<String> optional = firstPresent(asList(first, second)).extract(message);

		// then
		assertThat(optional, equalTo(Optional.of("foo")));
	}

	@Test
	public void secondReturned() throws Exception {
		// given
		final DummyTokenExtractor first = new DummyTokenExtractor(Optional.<String> absent());
		final DummyTokenExtractor second = new DummyTokenExtractor(Optional.of("bar"));
		final Message message = mock(Message.class);

		// when
		final Optional<String> optional = firstPresent(asList(first, second)).extract(message);

		// then
		assertThat(optional, equalTo(Optional.of("bar")));
	}

}
