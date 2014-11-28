package unit.cxf;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.model.Models.newCard;
import static org.cmdbuild.service.rest.model.Models.newRelation;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.commands.GetRelationList.DomainWithSource;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.service.rest.cxf.CxfRelations;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.model.ResponseSingle;
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
	public void typeNotFoundOnCreate() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findDomain(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).domainNotFound(anyString());

		// when
		cxfRelations.create("some domain", newRelation() //
				.withType("should be ignored") //
				.withValue("foo", "FOO") //
				.withValue("bar", "BAR") //
				.withValue("baz", "BAZ") //
				.build());

		// then
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findDomain(eq("some domain"));
		inOrder.verify(errorHandler).domainNotFound(eq("some domain"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicCalledOnCreation() throws Exception {
		// given
		final CMDomain type = mock(CMDomain.class);
		doReturn("found domain") //
				.when(type).getName();
		doReturn(type) //
				.when(dataAccessLogic).findDomain(anyString());
		doReturn(asList(789L)) //
				.when(dataAccessLogic).createRelations(any(RelationDTO.class));

		// when
		final ResponseSingle<Long> response = cxfRelations.create("some domain", newRelation() //
				.withSource(newCard() //
						.withId(123L) //
						.withType("source class") //
						.build()) //
				.withDestination(newCard() //
						.withId(456L) //
						.withType("destination class") //
						.build()) //
				.withType("should be ignored") //
				.withValue("foo", "FOO") //
				.withValue("bar", "BAR") //
				.withValue("baz", "BAZ") //
				.build());

		// then
		final ArgumentCaptor<RelationDTO> relationCaptor = ArgumentCaptor.forClass(RelationDTO.class);
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findDomain(eq("some domain"));
		inOrder.verify(dataAccessLogic).createRelations(relationCaptor.capture());
		inOrder.verifyNoMoreInteractions();
		final RelationDTO captured = relationCaptor.getValue();
		assertThat(captured.domainName, equalTo("found domain"));
		assertThat(captured.srcCardIdToClassName, hasEntry(123L, "source class"));
		assertThat(captured.srcCardIdToClassName.size(), equalTo(1));
		assertThat(captured.dstCardIdToClassName, hasEntry(456L, "destination class"));
		assertThat(captured.dstCardIdToClassName.size(), equalTo(1));
		assertThat(captured.relationAttributeToValue, hasEntry("foo", (Object) "FOO"));
		assertThat(captured.relationAttributeToValue, hasEntry("bar", (Object) "BAR"));
		assertThat(captured.relationAttributeToValue, hasEntry("baz", (Object) "BAZ"));
		assertThat(response.getElement(), equalTo(789L));
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingRelationsButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findDomain(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).domainNotFound(anyString());

		// when
		cxfRelations.read("123", null, null);

		// then
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findDomain(eq("123"));
		inOrder.verify(errorHandler).domainNotFound(eq("123"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingRelationsButBusinessLogicThrowsException() throws Exception {
		// given
		final CMDomain domain = mock(CMDomain.class);
		doReturn(123L) //
				.when(domain).getId();
		doReturn(domain) //
				.when(dataAccessLogic).findDomain(anyString());
		final CMClass clazz = mock(CMClass.class);
		doReturn("foo") //
				.when(clazz).getName();
		doReturn(clazz) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(domain) //
				.when(dataAccessLogic).findDomain(anyString());
		final RuntimeException exception = new RuntimeException();
		doThrow(exception) //
				.when(dataAccessLogic).getRelationListEmptyForWrongId(any(Card.class), any(DomainWithSource.class));
		doThrow(new WebApplicationException(exception)) //
				.when(errorHandler).propagate(any(Exception.class));

		// when
		cxfRelations.read("12", null, null);

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
