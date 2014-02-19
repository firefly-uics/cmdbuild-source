package unit.logic.email;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.Collections;
import java.util.List;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.logic.email.DefaultEmailAccountLogic;
import org.cmdbuild.logic.email.EmailAccountLogic.Account;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEmailAccountLogicTest {

	private static final List<EmailAccount> NO_ELEMENTS = Collections.emptyList();

	@Mock
	private Store<EmailAccount> store;

	private DefaultEmailAccountLogic logic;

	private final ArgumentCaptor<EmailAccount> captor = ArgumentCaptor.forClass(EmailAccount.class);

	@Before
	public void setUp() throws Exception {
		logic = new DefaultEmailAccountLogic(store);
	}

	@Test
	public void elementCreatedWhenThereAreNoOtherElements() throws Exception {
		// given
		when(store.list()) //
				.thenReturn(NO_ELEMENTS);
		final Account newOne = mock(Account.class);
		when(newOne.getName()) //
				.thenReturn("foo");

		// when
		logic.create(newOne);

		// then
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).list();
		inOrder.verify(store).create(captor.capture());
		final EmailAccount captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
	}

	@Test
	public void elementCreatedWhenThereIsNoOtherOneWithSameName() throws Exception {
		// given
		final EmailAccount stored = EmailAccount.newInstance() //
				.withName("bar") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored));
		final Account newOne = mock(Account.class);
		when(newOne.getName()) //
				.thenReturn("foo");

		// when
		logic.create(newOne);

		// then
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).list();
		inOrder.verify(store).create(captor.capture());
		final EmailAccount captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateNewElementWhenAnotherWithSameNameExists() throws Exception {
		// given
		final EmailAccount stored = EmailAccount.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored));
		final Account newOne = mock(Account.class);
		when(newOne.getName()) //
				.thenReturn("foo");

		// when
		try {
			logic.create(newOne);
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotUpdateNonExistingElement() throws Exception {
		// given
		when(store.list()) //
				.thenReturn(NO_ELEMENTS);
		final Account existing = mock(Account.class);
		when(existing.getName()) //
				.thenReturn("foo");

		// when
		try {
			logic.update(existing);
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotUpdateElementIfItIsStoredMoreThanOnce_thisShouldNeverHappenButWhoKnows() throws Exception {
		// given
		final EmailAccount stored = EmailAccount.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored, stored, stored));
		final Account existing = mock(Account.class);
		when(existing.getName()) //
				.thenReturn("foo");

		// when
		try {
			logic.update(existing);
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test
	public void elementUpdatedWhenOneIsFound() throws Exception {
		// given
		final EmailAccount stored = EmailAccount.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored));
		final Account existing = mock(Account.class);
		when(existing.getName()) //
				.thenReturn("foo");

		// when
		logic.update(existing);

		// then
		verify(store).list();
		verify(store).update(captor.capture());
		verifyNoMoreInteractions(store);
		final EmailAccount captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotDeleteNonExistingAccount() throws Exception {
		// given
		when(store.list()) //
				.thenReturn(NO_ELEMENTS);

		// when
		try {
			logic.delete("foo");
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotDeleteElementIfItIsStoredMoreThanOnce_thisShouldNeverHappenButWhoKnows() throws Exception {
		// given
		final EmailAccount stored = EmailAccount.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored, stored, stored));

		// when
		try {
			logic.delete("foo");
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test
	public void elementDeletedWhenAnotherOneWithSameNameIsFound() throws Exception {
		// given
		final EmailAccount stored = EmailAccount.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored));

		// when
		logic.delete("foo");

		// then
		verify(store).list();
		verify(store).delete(captor.capture());
		verifyNoMoreInteractions(store);
		final EmailAccount captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotGetNotExistingElement() throws Exception {
		// given
		when(store.list()) //
				.thenReturn(NO_ELEMENTS);

		// when
		try {
			logic.getAccount("foo");
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotGetElementIfItIsStoredMoreThanOnce_thisShouldNeverHappenButWhoKnows() throws Exception {
		// given
		final EmailAccount stored = EmailAccount.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored, stored, stored));

		// when
		try {
			logic.getAccount("foo");
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test
	public void elementGetWhenOneIsFound() throws Exception {
		// given
		final EmailAccount stored = EmailAccount.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored));

		// when
		logic.getAccount("foo");

		// then
		final ArgumentCaptor<Storable> captor = ArgumentCaptor.forClass(Storable.class);
		verify(store).list();
		verify(store).read(captor.capture());
		verifyNoMoreInteractions(store);
		assertThat(captor.getValue().getIdentifier(), equalTo("foo"));
	}

	@Test
	public void allElementsGet() throws Exception {
		// when
		logic.getAll();

		// then
		verify(store).list();
		verifyNoMoreInteractions(store);
	}

	@Test
	public void firstCreatedElementIsDefaultEvenIfNotSetted() throws Exception {
		// given
		when(store.list()) //
				.thenReturn(NO_ELEMENTS);
		final Account newOne = mock(Account.class);
		when(newOne.getName()) //
				.thenReturn("foo");
		when(newOne.isDefault()) //
				.thenReturn(false);

		// when
		logic.create(newOne);

		// then
		verify(store).list();
		verify(store).create(captor.capture());
		verifyNoMoreInteractions(store);
		final EmailAccount captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
		assertThat(captured.isDefault(), is(true));
	}

	@Test
	public void defaultAttributeCannotBeSettedInCreateAndUpdateAccountOperations() throws Exception {
		// given
		final EmailAccount stored = EmailAccount.newInstance() //
				.withName("bar") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored));
		final Account newOne = mock(Account.class);
		when(newOne.getName()) //
				.thenReturn("foo");
		when(newOne.isDefault()) //
				.thenReturn(true);
		final Account existingOne = mock(Account.class);
		when(existingOne.getName()) //
				.thenReturn("bar");
		when(existingOne.isDefault()) //
				.thenReturn(true);

		// when
		logic.create(newOne);
		logic.update(existingOne);

		// then
		verify(store).create(captor.capture());
		verify(store).update(captor.capture());

		final EmailAccount capturedNewOne = captor.getAllValues().get(0);
		assertThat(capturedNewOne.getName(), equalTo("foo"));
		assertThat(capturedNewOne.isDefault(), is(false));

		final EmailAccount capturedExistingOne = captor.getAllValues().get(1);
		assertThat(capturedExistingOne.getName(), equalTo("bar"));
		assertThat(capturedExistingOne.isDefault(), is(false));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotSetToDefaultNonExistingElement() throws Exception {
		// given
		when(store.list()) //
				.thenReturn(NO_ELEMENTS);

		// when
		try {
			logic.setDefault("foo");
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotSetToDefaultAnElementIfItIsStoredMoreThanOnce_thisShouldNeverHappenButWhoKnows() throws Exception {
		// given
		final EmailAccount stored = EmailAccount.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored, stored, stored));

		// when
		try {
			logic.setDefault("foo");
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test
	public void settingDefaultAnAlreadyDefaultElementDoesNothing() throws Exception {
		// given
		final EmailAccount stored = EmailAccount.newInstance() //
				.withName("foo") //
				.withDefaultStatus(true) //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored));

		// when
		logic.setDefault("foo");

		// then
		verify(store).list();
		verifyNoMoreInteractions(store);
	}

	@Test
	public void defaultConditionIsSettedToSpecifiedOneAndRemovedFromActualOne() throws Exception {
		// given
		final EmailAccount storedDefault = EmailAccount.newInstance() //
				.withName("foo") //
				.withDefaultStatus(true) //
				.build();
		final EmailAccount storedNotDefault = EmailAccount.newInstance() //
				.withName("bar") //
				.build();
		final EmailAccount anotherStoredNotDefault = EmailAccount.newInstance() //
				.withName("baz") //
				.build();
		when(store.list()) //
				.thenReturn(asList(storedDefault, storedNotDefault, anotherStoredNotDefault));

		// when
		logic.setDefault("bar");

		// then
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).list();
		inOrder.verify(store, times(2)).update(captor.capture());
		verifyNoMoreInteractions(store);

		final EmailAccount resetted = captor.getAllValues().get(0);
		assertThat(resetted.getName(), equalTo("foo"));
		assertThat(resetted.isDefault(), is(false));

		final EmailAccount setted = captor.getAllValues().get(1);
		assertThat(setted.getName(), equalTo("bar"));
		assertThat(setted.isDefault(), is(true));
	}

}
