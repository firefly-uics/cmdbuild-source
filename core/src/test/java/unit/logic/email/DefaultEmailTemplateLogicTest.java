package unit.logic.email;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailTemplate;
import org.cmdbuild.logic.email.DefaultEmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEmailTemplateLogicTest {

	private static final List<EmailTemplate> NO_TEMPLATES = Collections.emptyList();

	@Mock
	private Store<EmailTemplate> store;

	private DefaultEmailTemplateLogic logic;

	@Before
	public void setUp() throws Exception {
		logic = new DefaultEmailTemplateLogic(store);
	}

	@Test
	public void createdTemplatesAreStored() throws Exception {
		// given
		final Template template = mock(Template.class);
		when(template.getName()) //
				.thenReturn("foo");

		// when
		logic.create(template);

		// then
		final ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).list();
		inOrder.verify(store).create(captor.capture());
		final EmailTemplate captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
	}

	@Test
	public void cannotCreateIfAnotherWithSameNameExists() throws Exception {
		// given
		final EmailTemplate emailTemplate = EmailTemplate.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(emailTemplate));
		final Template template = mock(Template.class);
		when(template.getName()) //
				.thenReturn("foo");

		// when
		try {
			logic.create(template);
		} catch (final IllegalArgumentException e) {
			// ok
		} catch (final Throwable e) {
			fail("unexpected");
		}

		// then
		verify(store).list();
		verifyNoMoreInteractions(store);
	}

	@Test
	public void cannotUpdateNonExistingTemplate() throws Exception {
		// given
		when(store.list()) //
				.thenReturn(NO_TEMPLATES);
		final Template template = mock(Template.class);
		when(template.getName()) //
				.thenReturn("foo");

		// when
		try {
			logic.update(template);
		} catch (final IllegalArgumentException e) {
			// ok
		} catch (final Throwable e) {
			fail("unexpected");
		}

		// then
		verify(store).list();
		verifyNoMoreInteractions(store);
	}

	@Test
	public void cannotUpdateIfTemplateExistsMultipleTimes() throws Exception {
		// given
		final EmailTemplate emailTemplate = EmailTemplate.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(emailTemplate, emailTemplate));
		final Template template = mock(Template.class);
		when(template.getName()) //
				.thenReturn("foo");

		// when
		try {
			logic.update(template);
		} catch (final IllegalArgumentException e) {
			// ok
		} catch (final Throwable e) {
			fail("unexpected");
		}

		// then
		verify(store).list();
		verifyNoMoreInteractions(store);
	}

	@Test
	public void updatesIfNameIsFound() throws Exception {
		// given
		final EmailTemplate emailTemplate = EmailTemplate.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(emailTemplate));
		final Template template = mock(Template.class);
		when(template.getName()) //
				.thenReturn("foo");

		// when
		logic.update(template);

		// then
		final ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);
		verify(store).list();
		verify(store).update(captor.capture());
		verifyNoMoreInteractions(store);
		final EmailTemplate captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
	}
	
	@Test
	public void cannotDeleteNonExistingTemplate() throws Exception {
		// given
		when(store.list()) //
				.thenReturn(NO_TEMPLATES);
		final Template template = mock(Template.class);
		when(template.getName()) //
				.thenReturn("foo");

		// when
		try {
			logic.delete("foo");
		} catch (final IllegalArgumentException e) {
			// ok
		} catch (final Throwable e) {
			fail("unexpected");
		}

		// then
		verify(store).list();
		verifyNoMoreInteractions(store);
	}
	
	@Test
	public void deletesIfNameIsFound() throws Exception {
		// given
		final EmailTemplate emailTemplate = EmailTemplate.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(emailTemplate));
		final Template template = mock(Template.class);
		when(template.getName()) //
				.thenReturn("foo");

		// when
		logic.delete("foo");

		// then
		final ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);
		verify(store).list();
		verify(store).delete(captor.capture());
		verifyNoMoreInteractions(store);
		final EmailTemplate captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
	}

}
