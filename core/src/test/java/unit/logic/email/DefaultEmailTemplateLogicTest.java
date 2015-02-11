package unit.logic.email;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.DefaultEmailTemplate;
import org.cmdbuild.data.store.email.DefaultExtendedEmailTemplate;
import org.cmdbuild.data.store.email.EmailTemplate;
import org.cmdbuild.data.store.email.ExtendedEmailTemplate;
import org.cmdbuild.logic.email.DefaultEmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.services.email.EmailAccount;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEmailTemplateLogicTest {

	private static final List<ExtendedEmailTemplate> NO_ELEMENTS = Collections.emptyList();

	@Mock
	private Store<ExtendedEmailTemplate> store;

	@Mock
	private Store<EmailAccount> accountStore;

	private DefaultEmailTemplateLogic logic;

	private final ArgumentCaptor<ExtendedEmailTemplate> captor = ArgumentCaptor.forClass(ExtendedEmailTemplate.class);

	@Before
	public void setUp() throws Exception {
		logic = new DefaultEmailTemplateLogic(store, accountStore);
	}

	@Test
	public void elementCreatedWhenThereAreNoOtherElements() throws Exception {
		// given
		when(store.readAll()) //
				.thenReturn(NO_ELEMENTS);
		when(store.read(any(Storable.class))) //
				.thenReturn(extended(DefaultEmailTemplate.newInstance() //
						.withId(42L) //
						.withName("foo") //
						.build()));
		final Template newOne = mock(Template.class);
		when(newOne.getName()) //
				.thenReturn("foo");

		// when
		final Long id = logic.create(newOne);

		// then
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).readAll();
		inOrder.verify(store).create(captor.capture());
		inOrder.verify(store).read(any(Storable.class));
		final EmailTemplate captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
		assertThat(id, equalTo(42L));
	}

	@Test
	public void elementCreatedWhenThereIsNoOtherOneWithSameName() throws Exception {
		// given
		final ExtendedEmailTemplate stored = extended(DefaultEmailTemplate.newInstance() //
				.withName("bar") //
				.build());
		when(store.readAll()) //
				.thenReturn(asList(stored));
		when(store.read(any(Storable.class))) //
				.thenReturn(extended(DefaultEmailTemplate.newInstance() //
						.withId(42L) //
						.withName("foo") //
						.build()));
		final Template newOne = mock(Template.class);
		when(newOne.getName()) //
				.thenReturn("foo");

		// when
		final Long id = logic.create(newOne);

		// then
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).readAll();
		inOrder.verify(store).create(captor.capture());
		final EmailTemplate captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
		assertThat(id, equalTo(42L));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateNewElementWhenAnotherWithSameNameExists() throws Exception {
		// given
		final ExtendedEmailTemplate stored = extended(DefaultEmailTemplate.newInstance() //
				.withName("foo") //
				.build());
		when(store.readAll()) //
				.thenReturn(asList(stored));
		final Template newOne = mock(Template.class);
		when(newOne.getName()) //
				.thenReturn("foo");

		// when
		try {
			logic.create(newOne);
		} finally {
			verify(store).readAll();
			verifyNoMoreInteractions(store);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotUpdateNonExistingElement() throws Exception {
		// given
		when(store.readAll()) //
				.thenReturn(NO_ELEMENTS);
		final Template existing = mock(Template.class);
		when(existing.getName()) //
				.thenReturn("foo");

		// when
		try {
			logic.update(existing);
		} finally {
			verify(store).readAll();
			verifyNoMoreInteractions(store);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotUpdateElementIfItIsStoredMoreThanOnce_thisShouldNeverHappenButWhoKnows() throws Exception {
		// given
		final ExtendedEmailTemplate stored = extended(DefaultEmailTemplate.newInstance() //
				.withName("foo") //
				.build());
		when(store.readAll()) //
				.thenReturn(asList(stored, stored, stored));
		final Template existing = mock(Template.class);
		when(existing.getName()) //
				.thenReturn("foo");

		// when
		try {
			logic.update(existing);
		} finally {
			verify(store).readAll();
			verifyNoMoreInteractions(store);
		}
	}

	@Test
	public void elementUpdatedWhenOneIsFound() throws Exception {
		// given
		final ExtendedEmailTemplate stored = extended(DefaultEmailTemplate.newInstance() //
				.withName("foo") //
				.build());
		when(store.readAll()) //
				.thenReturn(asList(stored));
		final Template existing = mock(Template.class);
		when(existing.getName()) //
				.thenReturn("foo");

		// when
		logic.update(existing);

		// then
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).readAll();
		inOrder.verify(store).update(captor.capture());
		verifyNoMoreInteractions(store);
		final EmailTemplate captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotDeleteNonExistingAccount() throws Exception {
		// given
		final Template nonExisting = mock(Template.class);
		when(nonExisting.getName()) //
				.thenReturn("foo");
		when(store.readAll()) //
				.thenReturn(NO_ELEMENTS);

		// when
		try {
			logic.delete(nonExisting);
		} finally {
			verify(store).readAll();
			verifyNoMoreInteractions(store);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotDeleteElementIfItIsStoredMoreThanOnce_thisShouldNeverHappenButWhoKnows() throws Exception {
		// given
		final Template existing = mock(Template.class);
		when(existing.getName()) //
				.thenReturn("foo");
		final ExtendedEmailTemplate stored = extended(DefaultEmailTemplate.newInstance() //
				.withName("foo") //
				.build());
		when(store.readAll()) //
				.thenReturn(asList(stored, stored, stored));

		// when
		try {
			logic.delete(existing);
		} finally {
			verify(store).readAll();
			verifyNoMoreInteractions(store);
		}
	}

	@Test
	public void elementDeletedWhenAnotherOneWithSameNameIsFound() throws Exception {
		// given
		final Template existing = mock(Template.class);
		when(existing.getName()) //
				.thenReturn("foo");
		final ExtendedEmailTemplate stored = extended(DefaultEmailTemplate.newInstance() //
				.withName("foo") //
				.build());
		when(store.readAll()) //
				.thenReturn(asList(stored));

		// when
		logic.delete(existing);

		// then
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).readAll();
		inOrder.verify(store).delete(captor.capture());
		verifyNoMoreInteractions(store);
		final EmailTemplate captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
	}

	@Test(expected = NoSuchElementException.class)
	public void cannotGetNotExistingElement() throws Exception {
		// given
		doThrow(new NoSuchElementException()) //
				.when(store).read(any(ExtendedEmailTemplate.class));

		// when
		try {
			logic.read("foo");
		} finally {
			verify(store).read(captor.capture());
			verifyNoMoreInteractions(store);
			final EmailTemplate captured = captor.getValue();
			assertThat(captured.getName(), equalTo("foo"));
		}
	}

	@Test
	public void elementGetWhenOneIsFound() throws Exception {
		// given
		final ExtendedEmailTemplate stored = extended(DefaultEmailTemplate.newInstance() //
				.withName("foo") //
				.build());
		when(store.read(any(ExtendedEmailTemplate.class))) //
				.thenReturn(stored);

		// when
		logic.read("foo");

		// then
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).read(captor.capture());
		verifyNoMoreInteractions(store);
		assertThat(captor.getValue().getName(), equalTo("foo"));
	}

	@Test
	public void allElementsGet() throws Exception {
		// when
		logic.readAll();

		// then
		verify(store).readAll();
		verifyNoMoreInteractions(store);
	}

	private static ExtendedEmailTemplate extended(final EmailTemplate delegate) {
		return DefaultExtendedEmailTemplate.newInstance() //
				.withDelegate(delegate) //
				.build();
	}

}
