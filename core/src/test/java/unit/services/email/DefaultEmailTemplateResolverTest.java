package unit.services.email;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.services.email.DefaultEmailTemplateResolver;
import org.cmdbuild.services.email.EmailTemplateResolver.Configuration;
import org.cmdbuild.services.email.EmailTemplateResolver.DataFacade;
import org.junit.Before;
import org.junit.Test;

public class DefaultEmailTemplateResolverTest {

	private DataFacade dataFacade;

	@Before
	public void setUp() throws Exception {
		dataFacade = mock(DataFacade.class);
	}

	private String resolve(final String text) {
		return resolve(text, null);
	}

	private String resolve(final String template, final String multiSeparator) {
		return new DefaultEmailTemplateResolver(new Configuration() {

			@Override
			public DataFacade dataFacade() {
				return dataFacade;
			}

			@Override
			public String multiSeparator() {
				return multiSeparator;
			}

		}).resolve(template, null);
	}

	@Test
	public void resolveUnknown() throws Exception {
		// when
		final String resolved = resolve("{foo:bar} is unknown");

		// then
		verifyNoMoreInteractions(dataFacade);
		assertThat(resolved, equalTo("{foo:bar} is unknown"));
	}

	@Test
	public void resolveUser() throws Exception {
		// given
		when(dataFacade.getEmailForUser("foo")) //
				.thenReturn("foo@example.com");

		// when
		final String resolved = resolve("{user:foo}");

		// then
		verify(dataFacade).getEmailForUser("foo");
		assertThat(resolved, equalTo("foo@example.com"));
	}

	@Test
	public void resolveGroup() throws Exception {
		// given
		when(dataFacade.getEmailForGroup("foo")) //
				.thenReturn("foo@example.com");

		// when
		final String resolved = resolve("{group:foo}");

		// then
		verify(dataFacade).getEmailForGroup("foo");
		assertThat(resolved, equalTo("foo@example.com"));
	}

	@Test
	public void resolveUserAndGroupTogether() throws Exception {
		// given
		when(dataFacade.getEmailForUser("foo")) //
				.thenReturn("foo@example.com");
		when(dataFacade.getEmailForGroup("bar")) //
				.thenReturn("bar@example.com");

		// when
		final String resolved = resolve("{user:foo} {group:bar}");

		// then
		verify(dataFacade).getEmailForUser("foo");
		verify(dataFacade).getEmailForGroup("bar");
		assertThat(resolved, equalTo("foo@example.com bar@example.com"));
	}

	@Test
	public void resolveGroupUsers() throws Exception {
		// given
		when(dataFacade.getEmailsForGroupUsers("foo")) //
				.thenReturn(asList("bar@example.com", "baz@example.com"));

		// when
		final String resolved = resolve("{groupUsers:foo}", ",");

		// then
		verify(dataFacade).getEmailsForGroupUsers("foo");
		assertThat(resolved, equalTo("bar@example.com,baz@example.com"));
	}

	@Test
	public void resolveAttributes() throws Exception {
		// given
		when(dataFacade.getAttributeValue("foo")) //
				.thenReturn("FOO");
		when(dataFacade.getAttributeValue("bar")) //
				.thenReturn("BAR");

		// when
		final String resolved = resolve("{attribute:foo} {attribute:bar}");

		// then
		verify(dataFacade).getAttributeValue("foo");
		verify(dataFacade).getAttributeValue("bar");
		assertThat(resolved, equalTo("FOO BAR"));
	}

	@Test
	public void resolveAttributeSubElement() throws Exception {
		// given
		when(dataFacade.getReferenceAttributeValue("foo", "bar")) //
				.thenReturn("FOO");

		// when
		final String resolved = resolve("{attribute:foo:bar}");

		// then
		verify(dataFacade).getReferenceAttributeValue("foo", "bar");
		assertThat(resolved, equalTo("FOO"));
	}

}
