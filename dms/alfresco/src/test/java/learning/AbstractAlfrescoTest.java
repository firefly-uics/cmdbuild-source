package learning;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.alfresco.webservice.test.BaseWebServiceSystemTest;
import org.apache.commons.io.FileUtils;
import org.cmdbuild.dms.DefaultDocumentFactory;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentFactory;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.alfresco.AlfrescoDmsService;
import org.cmdbuild.dms.exception.DmsError;
import org.junit.Before;

import utils.TestConfiguration;

/**
 * Base class for all tests that connects to a local (running) instance of
 * Alfresco.
 * 
 * The file {@literal xmdbuildModel.xml} (within resources) must be copied in
 * the extensions directory of Alfresco before starting it.
 * 
 * @see {@link TestConfiguration} for an overview of the settings of the
 *      Alfresco instance.
 * 
 * @see {@link BaseWebServiceSystemTest} for tests that use an embedded instance
 *      of Alfresco (we cannot use it because we need to set a custom model).
 */
public class AbstractAlfrescoTest {

	private static final List<String> PATH = asList("path", "of", "test", "documents");
	private static final String CLASS = "class";
	private static final int CARD_ID = 42;
	protected static final String DESCRIPTION = "a brief description for this document";
	protected static final String CATEGORY = "Document";
	protected static final String AUTHOR = "The Author";
	private static final String SAMPLE_CONTENT = "sample content for uploaded file";
	private static final String TEMPORARY_FILE_PREFIX = "tmp";

	private static DocumentFactory documentFactory = new DefaultDocumentFactory(PATH);
	private DmsService dmsService;

	@Before
	public void setUp() throws Exception {
		createDmsService();
		emptyDocumentLocation();
	}

	private void createDmsService() {
		dmsService = new AlfrescoDmsService();
		dmsService.setConfiguration(configuration());
	}

	protected DmsConfiguration configuration() {
		return new TestConfiguration();
	}

	private void emptyDocumentLocation() throws Exception {
		final List<StoredDocument> storedDocuments = storedDocuments();
		for (final StoredDocument document : storedDocuments) {
			delete(document.getName());
		}
		assertThat(storedDocuments(), hasSize(0));
	}

	/*
	 * Utilities
	 */

	protected static File tempFile() throws IOException {
		final File file = File.createTempFile(TEMPORARY_FILE_PREFIX, null);
		file.deleteOnExit();
		FileUtils.writeStringToFile(file, SAMPLE_CONTENT);
		return file;
	}

	protected List<StoredDocument> storedDocuments() throws Exception {
		return dmsService.search(testDocuments());
	}

	protected void upload(final File file) throws DmsError, FileNotFoundException {
		dmsService.upload(storableDocumentFrom(file));
	}

	protected void upload(final File file, final String category) throws DmsError, FileNotFoundException {
		dmsService.upload(storableDocumentFrom(file, category));
	}

	protected void upload(final File file, final List<MetadataGroup> metadataGroups) throws DmsError,
			FileNotFoundException {
		dmsService.upload(storableDocumentFrom(file, metadataGroups));
	}

	protected void delete(final String name) throws DmsError {
		dmsService.delete(documentDeleteFrom(name));
	}

	private DocumentDelete documentDeleteFrom(final String name) {
		return documentFactory.createDocumentDelete(CLASS, CARD_ID, name);
	}

	private DocumentSearch testDocuments() {
		return documentFactory.createDocumentSearch(CLASS, CARD_ID);
	}

	private StorableDocument storableDocumentFrom(final File file) throws FileNotFoundException {
		return storableDocumentFrom(file, Collections.<MetadataGroup> emptyList());
	}

	private StorableDocument storableDocumentFrom(final File file, final String category) throws FileNotFoundException {
		return storableDocumentFrom(file, category, Collections.<MetadataGroup> emptyList());
	}

	private StorableDocument storableDocumentFrom(final File file, final Iterable<MetadataGroup> metadataGroups)
			throws FileNotFoundException {
		return storableDocumentFrom(file, CATEGORY, metadataGroups);
	}

	private StorableDocument storableDocumentFrom(final File file, final String category,
			final Iterable<MetadataGroup> metadataGroups) throws FileNotFoundException {
		return documentFactory.createStorableDocument( //
				AUTHOR, //
				CLASS, //
				CARD_ID, //
				new FileInputStream(file), //
				file.getName(), //
				category, //
				DESCRIPTION, //
				metadataGroups);
	}

}