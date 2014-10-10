package unit.cxf;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.service.rest.cxf.CxfRelations;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class CxfRelationsTest {

	private ErrorHandler errorHandler;
	private DataAccessLogic dataAccessLogic;

	private CxfRelations cxfRelations;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		dataAccessLogic = mock(DataAccessLogic.class);
		cxfRelations = new CxfRelations(errorHandler, dataAccessLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingRelationsButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findDomain(anyLong());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).domainNotFound(anyLong());

		// when
		cxfRelations.read(123L, null, null, null, null, null);

		// then
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findDomain(eq(123L));
		inOrder.verify(errorHandler).domainNotFound(eq(123L));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingRelationsButBusinessLogicThrowsException() throws Exception {
		// given
		final CMDomain domain = mock(CMDomain.class);
		doReturn(123L) //
				.when(domain).getId();
		doReturn(domain) //
				.when(dataAccessLogic).findDomain(anyLong());
		final CMClass clazz = mock(CMClass.class);
		doReturn("foo") //
				.when(clazz).getName();
		doReturn(clazz) //
				.when(dataAccessLogic).findClass(anyLong());
		doReturn(domain) //
				.when(dataAccessLogic).findDomain(anyLong());
		final RuntimeException exception = new RuntimeException();
		doThrow(exception) //
				.when(dataAccessLogic).getRelationListEmptyForWrongId(any(Card.class), any(DomainWithSource.class));
		doThrow(new WebApplicationException(exception)) //
				.when(errorHandler).propagate(any(Exception.class));

		// when
		cxfRelations.read(12L, 34L, 56L, "baz", null, null);

		// then
		final ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
		final ArgumentCaptor<DomainWithSource> domainWithSourceCaptor = ArgumentCaptor.forClass(DomainWithSource.class);
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findDomain(eq(12L));
		inOrder.verify(dataAccessLogic).findClass(eq(34L));
		// TODO capture and verify
		inOrder.verify(dataAccessLogic).getRelationListEmptyForWrongId(cardCaptor.capture(),
				domainWithSourceCaptor.capture());
		inOrder.verify(errorHandler).propagate(eq(exception));
		inOrder.verifyNoMoreInteractions();

		final Card card = cardCaptor.getValue();
		assertThat(card.getClassName(), equalTo("bar"));
		assertThat(card.getId(), equalTo(42L));

		final DomainWithSource domainWithSource = domainWithSourceCaptor.getValue();
		assertThat(domainWithSource.domainId, equalTo(123L));
		assertThat(domainWithSource.querySource, equalTo("baz"));
	}

	@Ignore
	@Test
	public void businessLogicCalledSuccessfullyWhenReadingRelations() throws Exception {
		fail("cannot mock business logic return value");
	}

}
