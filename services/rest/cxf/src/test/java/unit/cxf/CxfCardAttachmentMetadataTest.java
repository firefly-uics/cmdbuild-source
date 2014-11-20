package unit.cxf;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static java.util.Collections.emptyList;
import static org.cmdbuild.service.rest.model.Models.newAttachmentMetadata;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.NoSuchElementException;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.service.rest.cxf.CxfCardAttachmentMetadata;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.model.AttachmentMetadata;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class CxfCardAttachmentMetadataTest {

	protected static final Iterable<MetadataGroupDefinition> NO_METADATA_GROUP_DEFINITION = emptyList();

	private static final DocumentTypeDefinition NO_METADATA = new DocumentTypeDefinition() {

		@Override
		public String getName() {
			return "dummy " + DocumentTypeDefinition.class;
		};

		@Override
		public Iterable<MetadataGroupDefinition> getMetadataGroupDefinitions() {
			return NO_METADATA_GROUP_DEFINITION;
		};

	};

	private ErrorHandler errorHandler;
	private DmsLogic dmsLogic;
	private DataAccessLogic dataAccessLogic;

	private CxfCardAttachmentMetadata cxfCardAttachments;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		dmsLogic = mock(DmsLogic.class);
		dataAccessLogic = mock(DataAccessLogic.class);
		cxfCardAttachments = new CxfCardAttachmentMetadata(errorHandler, dmsLogic, dataAccessLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnRead() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));

		// when
		cxfCardAttachments.read("foo", 123L, "bar");
	}

	@Test(expected = WebApplicationException.class)
	public void missingCardOnRead() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doThrow(new NoSuchElementException()) //
				.when(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(123L));

		// when
		cxfCardAttachments.read("foo", 123L, "bar");
	}

	@Test(expected = WebApplicationException.class)
	public void missingAttachmentIdOnRead() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("dummy class") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentId();

		// when
		cxfCardAttachments.read("foo", 123L, null);
	}

	@Test(expected = WebApplicationException.class)
	public void blankAttachmentIdOnRead() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("dummy class") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentId();

		// when
		cxfCardAttachments.read("foo", 123L, "");
	}

	@Test(expected = WebApplicationException.class)
	public void missingAttachmentOnRead() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("dummy class") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		doReturn(absent()) //
				.when(dmsLogic).search(eq("foo"), eq(123L), eq("bar"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).attachmentNotFound("bar");

		// when
		cxfCardAttachments.read("foo", 123L, "bar");
	}

	@Test
	public void logicCalledOnRead() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("dummy class") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		final StoredDocument document = new StoredDocument();
		doReturn(of(document)) //
				.when(dmsLogic).search(eq("foo"), eq(123L), eq("bar"));

		// when
		cxfCardAttachments.read("foo", 123L, "bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).search(eq("foo"), eq(123L), eq("bar"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnUpdate() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));
		final AttachmentMetadata attachment = newAttachmentMetadata() //
				// ...
				.build();

		// when
		cxfCardAttachments.update("foo", 123L, "bar", attachment);
	}

	@Test(expected = WebApplicationException.class)
	public void missingCardOnUpdate() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doThrow(new NoSuchElementException()) //
				.when(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(123L));
		final AttachmentMetadata attachment = newAttachmentMetadata() //
				// ...
				.build();

		// when
		cxfCardAttachments.update("foo", 123L, "bar", attachment);
	}

	@Test(expected = WebApplicationException.class)
	public void missingAttachmentIdOnUpdate() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		final AttachmentMetadata attachment = newAttachmentMetadata() //
				// ...
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentId();

		// when
		cxfCardAttachments.update("foo", 123L, null, attachment);
	}

	@Test(expected = WebApplicationException.class)
	public void blankAttachmentIdOnUpdate() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		final AttachmentMetadata attachment = newAttachmentMetadata() //
				// ...
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentId();

		// when
		cxfCardAttachments.update("foo", 123L, "", attachment);
	}

	@Test(expected = WebApplicationException.class)
	public void missingAttachmentOnUpdate() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("dummy class") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentMetadata();

		// when
		cxfCardAttachments.update("foo", 123L, "bar", null);
	}

	@Test
	public void logicCalledOnUpdate() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());
		final AttachmentMetadata attachment = newAttachmentMetadata() //
				.withId("the id") //
				.withName("the name") //
				.withCategory("the category") //
				.withDescription("the description") //
				.withAuthor("the author") //
				.withExtra(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("foo", "bar") //
						.chainablePut("bar", "baz") //
						.chainablePut("baz", "foo")) //
				.build();

		// when
		cxfCardAttachments.update("foo", 123L, "bar", attachment);

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).getCategoryDefinition(eq("the category"));
		inOrder.verify(dmsLogic).updateDescriptionAndMetadata(eq("foo"), eq(123L), eq("bar"), eq("the category"),
				eq("the description"), any(Iterable.class));
		inOrder.verifyNoMoreInteractions();
	}

}
