package unit.cxf;

import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.cmdbuild.service.rest.v2.model.Models.newFileSystemObject;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.v2.cxf.CxfDataStores;
import org.cmdbuild.service.rest.v2.cxf.CxfDataStores.DataStore;
import org.cmdbuild.service.rest.v2.cxf.CxfDataStores.Element;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v2.model.FileSystemObject;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CxfDataStoresTest {

	private static class DummyException extends Exception {

		private static final long serialVersionUID = 1L;

	}

	private static final String A_DATASTORE = "a datastore";
	private static final String A_FOLDER = "a folder";
	private static final String A_FILE = "a file";

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private ErrorHandler errorHandler;
	private DataStore dataStore;
	private CxfDataStores underTest;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		dataStore = mock(DataStore.class);
		underTest = new CxfDataStores(errorHandler, ChainablePutMap.of(new HashMap<String, DataStore>()) //
				.chainablePut(A_DATASTORE, dataStore));
	}

	@Test(expected = DummyException.class)
	public void readFoldersCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		underTest = new CxfDataStores(errorHandler,
				ChainablePutMap.of(new HashMap<String, DataStore>()) //
						.chainablePut("foo", mock(DataStore.class)) //
						.chainablePut("bar", mock(DataStore.class)) //
						.chainablePut("baz", mock(DataStore.class)));
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.readFolders("none");
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test
	public void readFolders() throws Exception {
		// given
		final Element firstFolder = element(temporaryFolder.newFolder());
		final Element secondFolder = element(temporaryFolder.newFolder());
		final Element thirdFolder = element(temporaryFolder.newFolder());
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();

		// when
		final ResponseMultiple<FileSystemObject> response = underTest.readFolders(A_DATASTORE);

		// then
		verify(dataStore).folders();
		verifyNoMoreInteractions(errorHandler, dataStore);

		assertThat(response.getElements(),
				containsInAnyOrder( //
						newFileSystemObject() //
								.withId(firstFolder.getName()) //
								.withName(firstFolder.getName()) //
								.withParent(firstFolder.getParent()) //
								.build(), //
						newFileSystemObject() //
								.withId(secondFolder.getName()) //
								.withName(secondFolder.getName()) //
								.withParent(secondFolder.getParent()) //
								.build(), //
						newFileSystemObject() //
								.withId(thirdFolder.getName()) //
								.withName(thirdFolder.getName()) //
								.withParent(thirdFolder.getParent()) //
								.build()));
		assertThat(response.getMetadata().getTotal(), equalTo(3L));
	}

	@Test(expected = DummyException.class)
	public void readFolderCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		underTest = new CxfDataStores(errorHandler,
				ChainablePutMap.of(new HashMap<String, DataStore>()) //
						.chainablePut("foo", mock(DataStore.class)) //
						.chainablePut("bar", mock(DataStore.class)) //
						.chainablePut("baz", mock(DataStore.class)));
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.readFolder("none", A_FOLDER);
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test(expected = DummyException.class)
	public void readFolderCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		doReturn(empty()) //
				.when(dataStore).folder(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.readFolder(A_DATASTORE, A_FOLDER);
		} finally {
			// then
			verify(dataStore).folder(eq(A_FOLDER));
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test
	public void readFolder() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(dataStore).folder(anyString());

		// when
		final ResponseSingle<FileSystemObject> response = underTest.readFolder(A_DATASTORE, A_FOLDER);

		// then
		verify(dataStore).folder(eq(A_FOLDER));
		verifyNoMoreInteractions(errorHandler, dataStore);

		assertThat(response.getElement(),
				equalTo( //
						newFileSystemObject() //
								.withId(folder.getName()) //
								.withName(folder.getName()) //
								.withParent(folder.getParent()) //
								.build()));
	}

	@Test(expected = DummyException.class)
	public void uploadFileCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		underTest = new CxfDataStores(errorHandler,
				ChainablePutMap.of(new HashMap<String, DataStore>()) //
						.chainablePut("foo", mock(DataStore.class)) //
						.chainablePut("bar", mock(DataStore.class)) //
						.chainablePut("baz", mock(DataStore.class)));
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());
		final File file = temporaryFolder.newFile();

		try {
			// when
			underTest.uploadFile("none", A_FOLDER, new DataHandler(new FileDataSource(file)));
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test(expected = DummyException.class)
	public void uploadFileCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		doReturn(empty()) //
				.when(dataStore).folder(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());
		final File file = temporaryFolder.newFile();

		try {
			// when
			underTest.uploadFile(A_DATASTORE, A_FOLDER, new DataHandler(new FileDataSource(file)));
		} finally {
			// then
			verify(dataStore).folder(eq(A_FOLDER));
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test(expected = DummyException.class)
	public void uploadFileCallsErrorHandlerWhenFileNameIsDuplicated() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(dataStore).folder(anyString());
		final File file = temporaryFolder.newFile();
		final Element alreadyCreatedFile = element(file);
		doReturn(asList(alreadyCreatedFile)) //
				.when(dataStore).files(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).duplicateFileName(anyString());

		try {
			// when
			underTest.uploadFile(A_DATASTORE, A_FOLDER, new DataHandler(new FileDataSource(file)));
		} finally {
			// then
			verify(dataStore).folder(eq(A_FOLDER));
			verify(dataStore).files(eq(A_FOLDER));
			verify(errorHandler).duplicateFileName(eq(alreadyCreatedFile.getName()));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	// TODO test error on creation

	@Test
	public void uploadFile() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(dataStore).folder(anyString());
		doReturn(asList()) //
				.when(dataStore).files(any(String.class));
		final Element created = element(temporaryFolder.newFile());
		doReturn(of(created)) //
				.when(dataStore).create(any(String.class), any(DataHandler.class));
		final File file = temporaryFolder.newFile();
		final DataHandler dataHandler = new DataHandler(new FileDataSource(file));

		// when
		underTest.uploadFile(A_DATASTORE, A_FOLDER, dataHandler);

		// then
		verify(dataStore).folder(eq(A_FOLDER));
		verify(dataStore).files(eq(A_FOLDER));
		verify(dataStore).create(eq(A_FOLDER), eq(dataHandler));
		verifyNoMoreInteractions(errorHandler, dataStore);
	}

	@Test(expected = DummyException.class)
	public void readFilesCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		underTest = new CxfDataStores(errorHandler,
				ChainablePutMap.of(new HashMap<String, DataStore>()) //
						.chainablePut("foo", mock(DataStore.class)) //
						.chainablePut("bar", mock(DataStore.class)) //
						.chainablePut("baz", mock(DataStore.class)));
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.readFiles("none", A_FOLDER);
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test(expected = DummyException.class)
	public void readFilesCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		doReturn(empty()) //
				.when(dataStore).folder(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.readFiles(A_DATASTORE, A_FOLDER);
		} finally {
			// then
			verify(dataStore).folder(eq(A_FOLDER));
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test
	public void readFiles() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(dataStore).folder(anyString());
		final Element firstFile = element(temporaryFolder.newFile());
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(String.class));

		// when
		final ResponseMultiple<FileSystemObject> response = underTest.readFiles(A_DATASTORE, A_FOLDER);

		// then
		verify(dataStore).folder(eq(A_FOLDER));
		verify(dataStore).files(eq(A_FOLDER));
		verifyNoMoreInteractions(errorHandler, dataStore);

		assertThat(response.getElements(),
				containsInAnyOrder( //
						newFileSystemObject() //
								.withId(firstFile.getName()) //
								.withName(firstFile.getName()) //
								.withParent(firstFile.getParent()) //
								.build(), //
						newFileSystemObject() //
								.withId(secondFile.getName()) //
								.withName(secondFile.getName()) //
								.withParent(secondFile.getParent()) //
								.build(), //
						newFileSystemObject() //
								.withId(thirdFile.getName()) //
								.withName(thirdFile.getName()) //
								.withParent(thirdFile.getParent()) //
								.build()));
		assertThat(response.getMetadata().getTotal(), equalTo(3L));
	}

	@Test(expected = DummyException.class)
	public void readFileCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		underTest = new CxfDataStores(errorHandler,
				ChainablePutMap.of(new HashMap<String, DataStore>()) //
						.chainablePut("foo", mock(DataStore.class)) //
						.chainablePut("bar", mock(DataStore.class)) //
						.chainablePut("baz", mock(DataStore.class)));
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.readFile("none", A_FOLDER, A_FILE);
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test(expected = DummyException.class)
	public void readFileCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		doReturn(empty()) //
				.when(dataStore).folder(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.readFile(A_DATASTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(dataStore).folder(eq(A_FOLDER));
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test(expected = DummyException.class)
	public void readFileCallsErrorHandlerWhenFileIsNotFound() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(dataStore).folder(anyString());
		final Element firstFile = element(temporaryFolder.newFile());
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(String.class));
		doThrow(DummyException.class) //
				.when(errorHandler).fileNotFound(anyString());

		try {
			// when
			underTest.readFile(A_DATASTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(dataStore).folder(eq(A_FOLDER));
			verify(dataStore).files(eq(A_FOLDER));
			verify(errorHandler).fileNotFound(eq(A_FILE));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test
	public void readFile() throws Exception {
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(dataStore).folder(anyString());
		final Element firstFile = element(temporaryFolder.newFile(A_FILE));
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(String.class));

		// when
		final ResponseSingle<FileSystemObject> response = underTest.readFile(A_DATASTORE, A_FOLDER, A_FILE);

		// then
		verify(dataStore).folder(eq(A_FOLDER));
		verify(dataStore).files(eq(A_FOLDER));
		verifyNoMoreInteractions(errorHandler, dataStore);

		assertThat(response.getElement(),
				equalTo( //
						newFileSystemObject() //
								.withId(firstFile.getName()) //
								.withName(firstFile.getName()) //
								.withParent(firstFile.getParent()) //
								.build()));
	}

	@Test(expected = DummyException.class)
	public void downloadFileCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		underTest = new CxfDataStores(errorHandler,
				ChainablePutMap.of(new HashMap<String, DataStore>()) //
						.chainablePut("foo", mock(DataStore.class)) //
						.chainablePut("bar", mock(DataStore.class)) //
						.chainablePut("baz", mock(DataStore.class)));
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.downloadFile("none", A_FOLDER, A_FILE);
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test(expected = DummyException.class)
	public void downalodFileCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		doReturn(empty()) //
				.when(dataStore).folder(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.downloadFile(A_DATASTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(dataStore).folder(eq(A_FOLDER));
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test(expected = DummyException.class)
	public void downloadFileCallsErrorHandlerWhenFileIsNotFound() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(dataStore).folder(anyString());
		final Element firstFile = element(temporaryFolder.newFile());
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(String.class));
		doThrow(DummyException.class) //
				.when(errorHandler).fileNotFound(anyString());

		try {
			// when
			underTest.downloadFile(A_DATASTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(dataStore).folder(eq(A_FOLDER));
			verify(dataStore).files(eq(A_FOLDER));
			verify(errorHandler).fileNotFound(eq(A_FILE));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test
	public void downloadFile() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(dataStore).folder(anyString());
		final Element firstFile = element(temporaryFolder.newFile(A_FILE));
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(String.class));
		final File file = temporaryFolder.newFile("the file name");
		write(get(file.toURI()), "foo bar baz".getBytes());
		doReturn(of(new DataHandler(new FileDataSource(file)))) //
				.when(dataStore).download(any(Element.class));

		// when
		final DataHandler response = underTest.downloadFile(A_DATASTORE, A_FOLDER, A_FILE);

		// then
		verify(dataStore).folder(eq(A_FOLDER));
		verify(dataStore).files(eq(A_FOLDER));
		verify(dataStore).download(eq(firstFile));
		verifyNoMoreInteractions(errorHandler, dataStore);

		assertThat(response.getName(), equalTo("the file name"));
		assertThat(toString(response.getInputStream()), equalTo("foo bar baz"));
	}

	@Test(expected = DummyException.class)
	public void deleteFileCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		underTest = new CxfDataStores(errorHandler,
				ChainablePutMap.of(new HashMap<String, DataStore>()) //
						.chainablePut("foo", mock(DataStore.class)) //
						.chainablePut("bar", mock(DataStore.class)) //
						.chainablePut("baz", mock(DataStore.class)));
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.deleteFile("none", A_FOLDER, A_FILE);
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test(expected = DummyException.class)
	public void deleteFileCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		doReturn(empty()) //
				.when(dataStore).folder(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.deleteFile(A_DATASTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(dataStore).folder(eq(A_FOLDER));
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test(expected = DummyException.class)
	public void deleteFileCallsErrorHandlerWhenFileIsNotFound() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(dataStore).folder(anyString());
		final Element firstFile = element(temporaryFolder.newFile());
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(String.class));
		doThrow(DummyException.class) //
				.when(errorHandler).fileNotFound(anyString());

		try {
			// when
			underTest.deleteFile(A_DATASTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(dataStore).folder(eq(A_FOLDER));
			verify(dataStore).files(eq(A_FOLDER));
			verify(errorHandler).fileNotFound(eq(A_FILE));
			verifyNoMoreInteractions(errorHandler, dataStore);
		}
	}

	@Test
	public void deleteFile() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(dataStore).folder(anyString());
		final File file = temporaryFolder.newFile(A_FILE);
		final Element firstFile = element(file);
		write(get(file.toURI()), "foo bar baz".getBytes());
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(String.class));

		// when
		underTest.deleteFile(A_DATASTORE, A_FOLDER, A_FILE);

		// then
		verify(dataStore).folder(eq(A_FOLDER));
		verify(dataStore).files(eq(A_FOLDER));
		verify(dataStore).delete(eq(firstFile));
		verifyNoMoreInteractions(errorHandler, dataStore);
	}

	private static Element element(final File file) {
		return new Element() {

			@Override
			public String getId() {
				return file.getName();
			}

			@Override
			public String getParent() {
				return file.getParent();
			}

			@Override
			public String getName() {
				return file.getName();
			}

		};
	}

	private static String toString(final InputStream is) {
		final Scanner scanner = new Scanner(is).useDelimiter("\\A");
		try {
			return scanner.hasNext() ? scanner.next() : null;
		} finally {
			scanner.close();
		}
	}

}
