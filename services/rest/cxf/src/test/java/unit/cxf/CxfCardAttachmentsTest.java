package unit.cxf;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.model.Models.newAttachment;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.Collection;
import java.util.NoSuchElementException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.io.input.NullInputStream;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.service.rest.cxf.AttachmentsHelper;
import org.cmdbuild.service.rest.cxf.CxfCardAttachments;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InOrder;

public class CxfCardAttachmentsTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private ErrorHandler errorHandler;
	private DataAccessLogic dataAccessLogic;
	private AttachmentsHelper attachmentsHelper;

	private CxfCardAttachments cxfCardAttachments;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		dataAccessLogic = mock(DataAccessLogic.class);
		attachmentsHelper = mock(AttachmentsHelper.class);
		cxfCardAttachments = new CxfCardAttachments(errorHandler, dataAccessLogic, attachmentsHelper);
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnCreate() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));
		final Attachment attachment = newAttachment().build();
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
		final Attachment attachment = newAttachment().build();
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
	public void logicCalledOnCreateWithBothAttachmentAndFile() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		final Attachment attachment = newAttachment() //
				.withCategory("the category") //
				.withDescription("the description") //
				.build();
		final InputStream inputStream = new NullInputStream(1024);
		final DataSource dataSource = mock(DataSource.class);
		doReturn("file name") //
				.when(dataSource).getName();
		doReturn(inputStream) //
				.when(dataSource).getInputStream();
		final DataHandler dataHandler = new DataHandler(dataSource);
		doReturn("bar") //
				.when(attachmentsHelper).create(anyString(), anyLong(), anyString(), any(Attachment.class),
						any(DataHandler.class));

		// when
		final ResponseSingle<String> response = cxfCardAttachments.create("foo", 123L, attachment, dataHandler);

		// then
		assertThat(response.getElement(), equalTo("bar"));
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic, attachmentsHelper);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).create(eq("foo"), eq(123L), eq("file name"), eq(attachment), eq(dataHandler));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicCalledOnCreateWithFileOnly() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		final InputStream inputStream = new NullInputStream(1024);
		final DataSource dataSource = mock(DataSource.class);
		doReturn("file name") //
				.when(dataSource).getName();
		doReturn(inputStream) //
				.when(dataSource).getInputStream();
		final DataHandler dataHandler = new DataHandler(dataSource);
		doReturn("bar") //
				.when(attachmentsHelper).create(anyString(), anyLong(), anyString(), any(Attachment.class),
						any(DataHandler.class));

		// when
		final ResponseSingle<String> response = cxfCardAttachments.create("foo", 123L, null, dataHandler);

		// then
		assertThat(response.getElement(), equalTo("bar"));
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic, attachmentsHelper);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).create(eq("foo"), eq(123L), eq("file name"), isNull(Attachment.class),
				eq(dataHandler));
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
		final Collection<Attachment> attachments = asList( //
				newAttachment() //
						.withId("foo").build(), //
				newAttachment() //
						.withId("bar").build());
		doReturn(attachments) //
				.when(attachmentsHelper).search(anyString(), anyLong());

		// when
		final ResponseMultiple<Attachment> response = cxfCardAttachments.read("foo", 123L);

		// then
		assertThat(response.getElements(), equalTo(attachments));
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic, attachmentsHelper);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).search(eq("foo"), eq(123L));
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
		cxfCardAttachments.download("foo", 123L, "bar");
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
		cxfCardAttachments.download("foo", 123L, "bar");
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
		final DataHandler dataHandler = new DataHandler(new FileDataSource(temporaryFolder.newFile()));
		doReturn(dataHandler) //
				.when(attachmentsHelper).download(anyString(), anyLong(), anyString());

		// when
		final DataHandler response = cxfCardAttachments.download("foo", 123L, "bar");

		// then
		assertThat(response, equalTo(dataHandler));
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic, attachmentsHelper);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).download(eq("foo"), eq(123L), eq("bar"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnUpdate() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));
		final Attachment attachment = newAttachment().build();
		final DataSource dataSource = mock(DataSource.class);
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfCardAttachments.update("foo", 123L, "bar", attachment, dataHandler);
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
		final Attachment attachment = newAttachment().build();
		final DataSource dataSource = mock(DataSource.class);
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfCardAttachments.update("foo", 123L, "bar", attachment, dataHandler);
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
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentId();
		final Attachment attachment = newAttachment().build();
		final DataSource dataSource = mock(DataSource.class);
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfCardAttachments.update("foo", 123L, null, attachment, dataHandler);
	}

	@Test
	public void logicCalledOnUpdateWithBothAttachmentAndFile() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		final Attachment attachment = newAttachment() //
				.withCategory("the new category") //
				.withDescription("the new description") //
				.build();
		final InputStream inputStream = new NullInputStream(1024);
		final DataSource dataSource = mock(DataSource.class);
		doReturn(inputStream) //
				.when(dataSource).getInputStream();
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfCardAttachments.update("foo", 123L, "bar", attachment, dataHandler);

		// then
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic, attachmentsHelper);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).update(eq("foo"), eq(123L), eq("bar"), eq(attachment), eq(dataHandler));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicCalledOnUpdateWithFileOnly() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		final InputStream inputStream = new NullInputStream(1024);
		final DataSource dataSource = mock(DataSource.class);
		doReturn(inputStream) //
				.when(dataSource).getInputStream();
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfCardAttachments.update("foo", 123L, "bar", null, dataHandler);

		// then
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic, attachmentsHelper);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).update(eq("foo"), eq(123L), eq("bar"), isNull(Attachment.class),
				eq(dataHandler));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicCalledOnUpdateWithAttachmentOnly() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());
		final Attachment attachment = newAttachment() //
				.withCategory("the new category") //
				.withDescription("the new description") //
				.build();

		// when
		cxfCardAttachments.update("foo", 123L, "bar", attachment, null);

		// then
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic, attachmentsHelper);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).update(eq("foo"), eq(123L), eq("bar"), eq(attachment),
				isNull(DataHandler.class));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicNotCalledOnUpdateWhenBothAttachmentAndFileAreMissing() throws Exception {
		// given
		final CMClass targetClass = mock(CMClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(dataAccessLogic).findClass(anyString());
		doReturn(Card.newInstance(targetClass).build()) //
				.when(dataAccessLogic).fetchCard(anyString(), anyLong());

		// when
		cxfCardAttachments.update("foo", 123L, "bar", null, null);

		// then
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic, attachmentsHelper);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).update(eq("foo"), eq(123L), eq("bar"), isNull(Attachment.class),
				isNull(DataHandler.class));
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
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic, attachmentsHelper);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).delete(eq("foo"), eq(123L), eq("bar"));
		inOrder.verifyNoMoreInteractions();
	}

}
