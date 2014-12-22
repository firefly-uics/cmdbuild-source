package unit.cxf;

import static java.util.Collections.emptyList;
import static org.cmdbuild.service.rest.model.Models.newAttachment;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.NoSuchElementException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.io.input.NullInputStream;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
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
	private UserStore userStore;

	private CxfCardAttachments cxfCardAttachments;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		dmsLogic = mock(DmsLogic.class);
		dataAccessLogic = mock(DataAccessLogic.class);
		userStore = mock(UserStore.class);
		cxfCardAttachments = new CxfCardAttachments(errorHandler, dmsLogic, dataAccessLogic, userStore);

		final AuthenticatedUser authUser = mock(AuthenticatedUser.class);
		doReturn("dummy user") //
				.when(authUser).getUsername();
		final OperationUser operationUser = new OperationUser(authUser, new NullPrivilegeContext(), new NullGroup());
		doReturn(operationUser) //
				.when(userStore).getUser();
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
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		cxfCardAttachments.create("foo", 123L, attachment, dataHandler);

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic, userStore);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(userStore).getUser();
		inOrder.verify(dmsLogic).upload(eq("dummy user"), eq("foo"), eq(123L), same(inputStream), eq("file name"),
				eq("the category"), eq("the description"), any(Iterable.class));
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
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		cxfCardAttachments.create("foo", 123L, null, dataHandler);

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic, userStore);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(userStore).getUser();
		inOrder.verify(dmsLogic).upload(eq("dummy user"), eq("foo"), eq(123L), same(inputStream), eq("file name"),
				isNull(String.class), isNull(String.class), any(Iterable.class));
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
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic, userStore);
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

		// when
		cxfCardAttachments.download("foo", 123L, "bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic, userStore);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).download(eq("foo"), eq(123L), eq("bar"));
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
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		cxfCardAttachments.update("foo", 123L, "bar", attachment, dataHandler);

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic, userStore);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(userStore).getUser();
		inOrder.verify(dmsLogic).upload(eq("dummy user"), eq("foo"), eq(123L), same(inputStream), eq("bar"),
				eq("the new category"), eq("the new description"), any(Iterable.class));
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
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		cxfCardAttachments.update("foo", 123L, "bar", null, dataHandler);

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic, userStore);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(userStore).getUser();
		inOrder.verify(dmsLogic).upload(eq("dummy user"), eq("foo"), eq(123L), same(inputStream), eq("bar"),
				isNull(String.class), isNull(String.class), any(Iterable.class));
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
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		cxfCardAttachments.update("foo", 123L, "bar", attachment, null);

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic, userStore);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).updateDescriptionAndMetadata(eq("foo"), eq(123L), eq("bar"), eq("the new category"),
				eq("the new description"), any(Iterable.class));
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
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		cxfCardAttachments.update("foo", 123L, "bar", null, null);

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic, userStore);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
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
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, dataAccessLogic, userStore);
		inOrder.verify(dataAccessLogic).findClass(eq("foo"));
		inOrder.verify(dataAccessLogic).fetchCard(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).delete(eq("foo"), eq(123L), eq("bar"));
		inOrder.verifyNoMoreInteractions();
	}

}
