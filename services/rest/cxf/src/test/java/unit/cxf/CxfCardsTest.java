package unit.cxf;

import static org.cmdbuild.service.rest.model.Builders.newCard;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.service.rest.cxf.CxfCards;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CxfCardsTest {

	@Captor
	private ArgumentCaptor<MultivaluedMap<String, String>> multivaluedMapCaptor;

	private ErrorHandler errorHandler;
	private DataAccessLogic userDataAccessLogic;
	private CMDataView systemDataView;
	private CMDataView userDataView;

	private CxfCards cxfCards;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		userDataAccessLogic = mock(DataAccessLogic.class);
		systemDataView = mock(CMDataView.class);
		userDataView = mock(CMDataView.class);
		cxfCards = new CxfCards(errorHandler, userDataAccessLogic, systemDataView, userDataView);
	}

	@Test(expected = WebApplicationException.class)
	public void createRaisesErrorWhenTypeIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(userDataAccessLogic).findClass(anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(anyLong());

		// when
		cxfCards.create(123L, newCard() //
				.withType(456L) //
				.build());

		// then
		final InOrder inOrder = inOrder(errorHandler, userDataAccessLogic, systemDataView, userDataView);
		inOrder.verify(userDataAccessLogic).findClass(eq(123L));
		inOrder.verify(errorHandler).classNotFound(123L);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicCalledOnCreation() throws Exception {
		// given
		final CMClass type = mock(CMClass.class);
		doReturn("baz") //
				.when(type).getName();
		doReturn(type) //
				.when(userDataAccessLogic).findClass(anyLong());
		doReturn(123L) //
				.when(userDataAccessLogic).createCard(any(org.cmdbuild.model.data.Card.class));

		// when
		final ResponseSingle<Long> response = cxfCards.create(123L, newCard() //
				.withType(456L) //
				.withValue("some name", "some value") //
				.build());

		// then
		final ArgumentCaptor<org.cmdbuild.model.data.Card> cardCaptor = ArgumentCaptor
				.forClass(org.cmdbuild.model.data.Card.class);
		final InOrder inOrder = inOrder(errorHandler, userDataAccessLogic, systemDataView, userDataView);
		inOrder.verify(userDataAccessLogic).findClass(eq(123L));
		inOrder.verify(userDataAccessLogic).createCard(cardCaptor.capture());
		inOrder.verifyNoMoreInteractions();
		final org.cmdbuild.model.data.Card captured = cardCaptor.getValue();
		assertThat(captured.getClassName(), equalTo("baz"));
		assertThat(captured.getAttributes(), hasEntry("some name", (Object) "some value"));
		assertThat(response.getElement(), equalTo(123L));
	}

	@Test(expected = WebApplicationException.class)
	public void updateRaisesErrorWhenTypeIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(userDataAccessLogic).findClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(anyLong());

		// when
		cxfCards.update(12L, 34L, newCard() //
				.withType(56L) //
				.withId(78L) //
				.build());

		// then
		final InOrder inOrder = inOrder(errorHandler, userDataAccessLogic, systemDataView, userDataView);
		inOrder.verify(userDataAccessLogic).findClass(eq(12L));
		inOrder.verify(errorHandler).classNotFound(12L);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicCalledOnUpdate() throws Exception {
		// given
		final CMClass type = mock(CMClass.class);
		doReturn("baz") //
				.when(type).getName();
		doReturn(type) //
				.when(userDataAccessLogic).findClass(anyLong());

		// when
		cxfCards.update(12L, 34L, newCard() //
				.withType(56L) //
				.withId(78L) //
				.withValue("some name", "some value") //
				.build());

		// then
		final ArgumentCaptor<org.cmdbuild.model.data.Card> cardCaptor = ArgumentCaptor
				.forClass(org.cmdbuild.model.data.Card.class);
		final InOrder inOrder = inOrder(errorHandler, userDataAccessLogic, systemDataView, userDataView);
		inOrder.verify(userDataAccessLogic).findClass(eq(12L));
		inOrder.verify(userDataAccessLogic).updateCard(cardCaptor.capture());
		inOrder.verifyNoMoreInteractions();
		final org.cmdbuild.model.data.Card captured = cardCaptor.getValue();
		assertThat(captured.getClassName(), equalTo("baz"));
		assertThat(captured.getId(), equalTo(34L));
		assertThat(captured.getAttributes(), hasEntry("some name", (Object) "some value"));
	}

	@Test(expected = WebApplicationException.class)
	public void deleteRaisesErrorWhenTypeIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(userDataAccessLogic).findClass(anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(anyLong());

		// when
		cxfCards.delete(123L, 456L);

		// then
		final InOrder inOrder = inOrder(errorHandler, userDataAccessLogic, systemDataView, userDataView);
		inOrder.verify(userDataAccessLogic).findClass(eq(123L));
		inOrder.verify(errorHandler).classNotFound(123L);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicCalledOnDeletion() throws Exception {
		// given
		final CMClass type = mock(CMClass.class);
		doReturn("baz") //
				.when(type).getName();
		doReturn(type) //
				.when(userDataAccessLogic).findClass(anyLong());

		// when
		cxfCards.delete(123L, 456L);

		// then
		final InOrder inOrder = inOrder(errorHandler, userDataAccessLogic, systemDataView, userDataView);
		inOrder.verify(userDataAccessLogic).findClass(eq(123L));
		inOrder.verify(userDataAccessLogic).deleteCard(eq("baz"), eq(456L));
		inOrder.verifyNoMoreInteractions();
	}

}
