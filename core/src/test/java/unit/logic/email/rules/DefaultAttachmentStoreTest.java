package unit.logic.email.rules;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreator;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.logic.email.rules.DefaultAttachmentStore;
import org.cmdbuild.model.email.Attachment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class DefaultAttachmentStoreTest {

	private static final String CLASSNAME = "TheClazz";
	private static final Long ID = 42L;
	private static final String USER = "me";
	private static final String FOO = "foo";
	private static final String BAR = "bar";
	private static final String LOOKUP_NAME = "Attachments";

	private DefaultAttachmentStore attachmentStore;

	private CMDataView dataView;
	private DocumentCreatorFactory documentCreatorFactory;
	private DmsConfiguration dmsConfiguration;
	private DmsService dmsService;

	@Before
	public void setUp() throws Exception {
		dataView = mock(CMDataView.class);

		documentCreatorFactory = mock(DocumentCreatorFactory.class);

		dmsConfiguration = mock(DmsConfiguration.class);

		dmsService = mock(DmsService.class);

		attachmentStore = new DefaultAttachmentStore( //
				dataView, //
				CLASSNAME, //
				ID, //
				USER, //
				documentCreatorFactory, //
				dmsConfiguration, //
				dmsService);
	}

	@Test
	public void dmsNotEnabled() throws Exception {
		// given
		when(dmsConfiguration.isEnabled()) //
				.thenReturn(false);

		// when
		attachmentStore.store(Arrays.asList(attachment(FOO)));

		// then
		verify(dmsConfiguration).isEnabled();
		verifyNoMoreInteractions(dataView, documentCreatorFactory, dmsConfiguration, dmsService);
	}

	@Test
	public void attachmentsStored() throws Exception {
		// given
		when(dmsConfiguration.isEnabled()) //
				.thenReturn(true);
		when(dmsConfiguration.getLookupNameForAttachments()) //
				.thenReturn(LOOKUP_NAME);

		final CMClass targetClass = mock(CMClass.class, CLASSNAME);
		when(dataView.findClass(CLASSNAME)) //
				.thenReturn(targetClass);

		final StorableDocument storableDocument1 = mock(StorableDocument.class, FOO);
		final StorableDocument storableDocument2 = mock(StorableDocument.class, BAR);

		final DocumentCreator documentCreator = mock(DocumentCreator.class);
		when(documentCreator.createStorableDocument( //
				eq(USER), //
				eq(CLASSNAME), //
				eq(ID.toString()), //
				any(InputStream.class), //
				eq(FOO), //
				eq(LOOKUP_NAME), //
				eq(FOO))) //
				.thenReturn(storableDocument1);
		when(documentCreator.createStorableDocument( //
				eq(USER), //
				eq(CLASSNAME), //
				eq(ID.toString()), //
				any(InputStream.class), //
				eq(BAR), //
				eq(LOOKUP_NAME), //
				eq(BAR))) //
				.thenReturn(storableDocument2);

		when(documentCreatorFactory.create(targetClass)) //
				.thenReturn(documentCreator);

		// when
		attachmentStore.store(Arrays.asList(attachment(FOO), attachment(BAR)));

		// then
		final InOrder inOrder = inOrder(dataView, documentCreatorFactory, documentCreator, dmsConfiguration, dmsService);
		inOrder.verify(dataView).findClass(CLASSNAME);
		inOrder.verify(documentCreatorFactory).create(targetClass);
		inOrder.verify(documentCreator).createStorableDocument( //
				eq(USER), //
				eq(CLASSNAME), //
				eq(ID.toString()), //
				any(InputStream.class), //
				eq(FOO), //
				eq(LOOKUP_NAME), //
				eq(FOO));
		inOrder.verify(dmsService).upload(storableDocument1);
		inOrder.verify(documentCreator).createStorableDocument( //
				eq(USER), //
				eq(CLASSNAME), //
				eq(ID.toString()), //
				any(InputStream.class), //
				eq(BAR), //
				eq(LOOKUP_NAME), //
				eq(BAR));
		inOrder.verify(dmsService).upload(storableDocument2);
		inOrder.verifyNoMoreInteractions();
	}

	private Attachment attachment(final String name) throws IOException {
		final File directory = FileUtils.getTempDirectory();
		final File file = File.createTempFile("attachment", name, directory);
		file.deleteOnExit();
		final DataSource dataSource = new FileDataSource(file);
		final DataHandler dataHandler = new DataHandler(dataSource);
		return Attachment.newInstance() //
				.withName(name) //
				.withDataHandler(dataHandler) //
				.build();
	}

}
