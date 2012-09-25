package learning;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import org.cmdbuild.dms.exception.DmsException;
import org.junit.Before;
import org.junit.BeforeClass;

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

	private static DmsService dmsService;
	private static DocumentFactory documentFactory;

	@BeforeClass
	public static void setUp() throws Exception {
		dmsService = new AlfrescoDmsService();
		dmsService.setConfiguration(new TestConfiguration());

		documentFactory = new DefaultDocumentFactory(PATH);
	}

	@Before
	public void documentsLocationMustBeEmpty() throws Exception {
		final List<StoredDocument> storedDocuments = storedDocuments();
		for (final StoredDocument document : storedDocuments) {
			delete(document.getName());
		}
		assertThat(storedDocuments(), hasSize(0));
	}

	/*
	 * Utilities
	 */

	protected static DmsConfiguration configuration() {
		return dmsService.getConfiguration();
	}

	protected static File tempFile() throws IOException {
		final File file = File.createTempFile(TEMPORARY_FILE_PREFIX, null);
		file.deleteOnExit();
		FileUtils.writeStringToFile(file, SAMPLE_CONTENT);
		return file;
	}

	protected static List<StoredDocument> storedDocuments() {
		return dmsService.search(testDocuments());
	}

	protected static void upload(final File file) throws DmsException {
		dmsService.upload(storableDocumentFrom(file));
	}

	protected static void upload(final File file, final List<MetadataGroup> metadataGroups) throws DmsException {
		dmsService.upload(storableDocumentFrom(file, metadataGroups));
	}

	protected static void delete(final String name) throws DmsException {
		dmsService.delete(documentDeleteFrom(name));
	}

	private static DocumentDelete documentDeleteFrom(final String name) {
		return documentFactory.createDocumentDelete(CLASS, CARD_ID, name);
	}

	private static DocumentSearch testDocuments() {
		return documentFactory.createDocumentSearch(CLASS, CARD_ID);
	}

	private static StorableDocument storableDocumentFrom(final File file) {
		return storableDocumentFrom(file, Collections.<MetadataGroup> emptyList());
	}

	private static StorableDocument storableDocumentFrom(final File file, final Iterable<MetadataGroup> metadataGroups) {
		return documentFactory.createStorableDocument( //
				AUTHOR, //
				CLASS, //
				CARD_ID, //
				inputStream(file), //
				file.getName(), //
				CATEGORY, //
				DESCRIPTION, //
				metadataGroups);
	}

	private static InputStream inputStream(final File file) {
		try {
			return new FileInputStream(file);
		} catch (final FileNotFoundException e) {
			fail();
			return null;
		}
	}

}