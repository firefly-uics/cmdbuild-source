package unit.api.fluent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.ExistingRelation;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.api.fluent.NewRelation;
import org.cmdbuild.api.fluent.QueryClass;
import org.junit.Before;
import org.junit.Test;

public class FluentApiTest {

	private static final String CLASS_NAME = "class";
	private static final String DOMAIN_NAME = "domain";
	private static final int CARD_ID = 42;

	private static final CardDescriptor CARD_DESCRIPTOR = new CardDescriptor(CLASS_NAME, CARD_ID);

	private FluentApiExecutor executor;
	private FluentApi api;

	@Before
	public void createApi() throws Exception {
		executor = mock(FluentApiExecutor.class);
		api = new FluentApi(executor);
	}

	@Test
	public void executorCalledWhenCreatingNewCard() {
		final NewCard newCard = api.newCard(CLASS_NAME);

		when(executor.create(newCard)).thenReturn(CARD_DESCRIPTOR);

		assertThat(newCard.create(), equalTo(CARD_DESCRIPTOR));

		verify(executor).create(newCard);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenUpdatingExistingCard() {
		final ExistingCard existingCard = api.existingCard(CLASS_NAME, CARD_ID);
		existingCard.update();

		verify(executor).update(existingCard);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenDeletingExistingCard() {
		final ExistingCard existingCard = api.existingCard(CLASS_NAME, CARD_ID);
		existingCard.delete();

		verify(executor).delete(existingCard);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenFetchingExistingCard() {
		final ExistingCard existingCard = api.existingCard(CLASS_NAME, CARD_ID);
		existingCard.fetch();

		verify(executor).fetch(existingCard);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenCreatingNewRelation() {
		final NewRelation newRelation = api.newRelation(DOMAIN_NAME);
		newRelation.create();

		verify(executor).create(newRelation);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenDeletingExistingRelation() {
		final ExistingRelation existingRelation = api.existingRelation(DOMAIN_NAME);
		existingRelation.delete();

		verify(executor).delete(existingRelation);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenFetchingClass() {
		final QueryClass queryClass = api.queryClass(CLASS_NAME);
		queryClass.fetch();

		verify(executor).fetch(queryClass);
		verifyNoMoreInteractions(executor);
	}

}
