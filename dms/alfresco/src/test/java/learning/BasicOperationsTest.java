package learning;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.exception.DmsException;
import org.junit.Test;

public class BasicOperationsTest extends AbstractAlfrescoTest {

	private static final String CATEGORY_WITH_SPACES = "Category with spaces";

	@Test
	public void fileUploadedAndDeleted() throws Exception {
		assertTrue(storedDocuments().isEmpty());
		upload(tempFile());
		assertThat(storedDocuments(), hasSize(1));
		delete(storedDocuments().get(0).getName());
		assertTrue(storedDocuments().isEmpty());
	}

	@Test
	public void uploadedFileSuccessfullyQueried() throws Exception {
		final File file = tempFile();
		assertTrue(storedDocuments().isEmpty());
		upload(file);
		assertThat(storedDocuments(), hasSize(1));

		final StoredDocument storedDocument = storedDocuments().get(0);
		assertThat(storedDocument.getAuthor(), equalTo(AUTHOR));
		assertThat(storedDocument.getCategory(), equalTo(CATEGORY));
		assertThat(storedDocument.getDescription(), equalTo(DESCRIPTION));
		assertThat(storedDocument.getName(), equalTo(file.getName()));

		delete(storedDocument.getName());
		assertTrue(storedDocuments().isEmpty());
	}

	@Test(expected = DmsException.class)
	public void deleteMissingFileThrowsExeption() throws Exception {
		delete(tempFile().getName());
	}

	@Test
	public void categoryWithSpacesAreAllowed() throws Exception {
		assertTrue(storedDocuments().isEmpty());
		upload(tempFile(), CATEGORY_WITH_SPACES);
		assertThat(storedDocuments(), hasSize(1));

		final StoredDocument storedDocument = storedDocuments().get(0);
		assertThat(storedDocument.getCategory(), equalTo(CATEGORY_WITH_SPACES));
	}

}
