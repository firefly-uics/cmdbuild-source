package unit.cxf;

import static java.util.Collections.emptyList;
import static org.cmdbuild.service.rest.model.Builders.newAttachment;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.HashMap;
import java.util.NoSuchElementException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.io.input.NullInputStream;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.service.rest.cxf.CxfCardAttachments;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.model.Attachment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class CxfCardAttachmentsTest {

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

	private CxfCardAttachments cxfCardAttachments;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		dmsLogic = mock(DmsLogic.class);
		dataAccessLogic = mock(DataAccessLogic.class);
		cxfCardAttachments = new CxfCardAttachments(errorHandler, dmsLogic, dataAccessLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnCreate() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));
		final Attachment attachment = newAttachment() //
				.build();
		final DataSource dataSource = mock(DataSource.class);
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfCardAttachments.create("foo", 123L, attachment, dataHandler);
	}

	@Test(expected = WebApplicationException.class)
	public void missingCardOnCreate() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doThrow(new NoSuchElementException()) //
				.when(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(123L));
		final Attachment attachment = newAttachment() //
				.build();
		final DataSource dataSource = mock(DataSource.class);
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfCardAttachments.create("foo", 123L, attachment, dataHandler);
	}

	@Test(expected = WebApplicationException.class)
	public void missingAttachmentOnCreate() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachment();
		final DataSource dataSource = mock(DataSource.class);
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfCardAttachments.create("foo", 123L, null, dataHandler);
	}

	@Test(expected = WebApplicationException.class)
	public void missingAttachmentNameOnCreate() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentName();
		final Attachment attachment = newAttachment().withCategory("the category").build();
		final DataSource dataSource = mock(DataSource.class);
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfCardAttachments.create("foo", 123L, attachment, dataHandler);
	}

	@Test(expected = WebApplicationException.class)
	public void missingFileOnCreate() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingFile();
		final Attachment attachment = newAttachment().build();

		// when
		cxfCardAttachments.create("foo", 123L, attachment, null);
	}

	@Test
	public void logicCalledOnCreate() throws Exception {
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
		final Attachment attachment = newAttachment() //
				.withName("the name") //
				.withCategory("the category") //
				.withDescription("the description") //
				.withAuthor("the author") //
				.withMetadata(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("foo", "bar") //
						.chainablePut("bar", "baz") //
						.chainablePut("baz", "foo")) //
				.build();
		final InputStream inputStream = new NullInputStream(1024);
		final DataSource dataSource = mock(DataSource.class);
		doReturn(inputStream) //
				.when(dataSource).getInputStream();
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfCardAttachments.create("foo", 123L, attachment, dataHandler);

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).getCategoryDefinition(eq("the category"));
		inOrder.verify(dmsLogic).upload(eq("the author"), eq("foo"), eq(123L), same(inputStream), eq("the name"),
				eq("the category"), eq("the description"), any(Iterable.class));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnReadAll() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));

		// when
		cxfCardAttachments.read("foo", 123L);
	}

	@Test(expected = WebApplicationException.class)
	public void missingCardOnReadAll() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doThrow(new NoSuchElementException()) //
				.when(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(123L));

		// when
		cxfCardAttachments.read("foo", 123L);
	}

	@Test
	public void logicCalledOnReadAll() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());

		// when
		cxfCardAttachments.read("foo", 123L);

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).search(eq("foo"), eq(123L));
		inOrder.verifyNoMoreInteractions();
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

	@Test
	public void logicCalledOnRead() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());

		// when
		cxfCardAttachments.read("foo", 123L, "bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).download(eq("foo"), eq(123L), eq("bar"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnDelete() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));

		// when
		cxfCardAttachments.delete("foo", 123L, "bar");
	}

	@Test(expected = WebApplicationException.class)
	public void missingCardOnDelete() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doThrow(new NoSuchElementException()) //
				.when(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(123L));

		// when
		cxfCardAttachments.delete("foo", 123L, "bar");
	}

	@Test
	public void logicCalledOnDelete() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("baz") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());

		// when
		cxfCardAttachments.delete("foo", 123L, "bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).delete(eq("foo"), eq(123L), eq("bar"));
		inOrder.verifyNoMoreInteractions();
	}

}
