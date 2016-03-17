package unit.cxf;

import static java.io.File.separator;
import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v2.model.Models.newFileSystemObject;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import org.cmdbuild.service.rest.v2.cxf.CxfDataStores.Hashing;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v2.model.FileSystemObject;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CxfDataStoresTest {

	private static class DummyException extends Exception {

		private static final long serialVersionUID = 1L;

	}

	private static class DelegatingAnswer implements Answer<Object> {

		private final Hashing delegate;

		public DelegatingAnswer(final Hashing delegate) {
			this.delegate = delegate;
		}

		@Override
		public Object answer(final InvocationOnMock invocation) throws Throwable {
			return invocation.getMethod().invoke(delegate, invocation.getArguments());
		}

	}

	private static final Hashing LAST_PATH_PART = new Hashing() {

		@Override
		public String hash(final String value) {
			final String[] values = value.split(separator);
			return values[values.length - 1];
		}

	};

	private static final String A_DATASTORE = "a datastore";
	private static final String A_FOLDER = "a folder";
	private static final String A_FILE = "a file";

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private ErrorHandler errorHandler;
	private DataStore dataStore;
	private Hashing hashing;
	private CxfDataStores underTest;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		dataStore = mock(DataStore.class);
		hashing = mock(Hashing.class);
		doAnswer(new DelegatingAnswer(LAST_PATH_PART)) //
				.when(hashing).hash(anyString());
		underTest = new CxfDataStores(errorHandler, ChainablePutMap.of(new HashMap<String, DataStore>()) //
				.chainablePut(A_DATASTORE, dataStore), hashing);
	}

	@Test(expected = DummyException.class)
	public void readFoldersCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		underTest = new CxfDataStores(errorHandler,
				ChainablePutMap.of(new HashMap<String, DataStore>()) //
						.chainablePut("foo", mock(DataStore.class)) //
						.chainablePut("bar", mock(DataStore.class)) //
						.chainablePut("baz", mock(DataStore.class)), //
				hashing);
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.readFolders("none");
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test
	public void readFolders() throws Exception {
		// given
		final File firstFolder = temporaryFolder.newFolder();
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();

		// when
		final ResponseMultiple<FileSystemObject> response = underTest.readFolders(A_DATASTORE);

		// then
		verify(dataStore).folders();
		verify(hashing, times(6)).hash(anyString());
		verifyNoMoreInteractions(errorHandler, dataStore, hashing);

		assertThat(response.getElements(),
				containsInAnyOrder( //
						newFileSystemObject() //
								.withId(firstFolder.getName()) //
								.withName(firstFolder.getName()) //
								.withParent(firstFolder.getParentFile().getName()) //
								.build(), //
						newFileSystemObject() //
								.withId(secondFolder.getName()) //
								.withName(secondFolder.getName()) //
								.withParent(secondFolder.getParentFile().getName()) //
								.build(), //
						newFileSystemObject() //
								.withId(thirdFolder.getName()) //
								.withName(thirdFolder.getName()) //
								.withParent(thirdFolder.getParentFile().getName()) //
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
						.chainablePut("baz", mock(DataStore.class)), //
				hashing);
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.readFolder("none", A_FOLDER);
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test(expected = DummyException.class)
	public void readFolderCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		final File firstFolder = temporaryFolder.newFolder();
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.readFolder(A_DATASTORE, A_FOLDER);
		} finally {
			// then
			verify(dataStore).folders();
			verify(hashing, times(3)).hash(anyString());
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test
	public void readFolder() throws Exception {
		// given
		final File firstFolder = temporaryFolder.newFolder(A_FOLDER);
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();

		// when
		final ResponseSingle<FileSystemObject> response = underTest.readFolder(A_DATASTORE, A_FOLDER);

		// then
		verify(dataStore).folders();
		verify(hashing, times(3)).hash(anyString());
		verifyNoMoreInteractions(errorHandler, dataStore, hashing);

		assertThat(response.getElement(),
				equalTo( //
						newFileSystemObject() //
								.withId(firstFolder.getName()) //
								.withName(firstFolder.getName()) //
								.withParent(firstFolder.getParentFile().getName()) //
								.build()));
	}

	@Test(expected = DummyException.class)
	public void uploadFileCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		underTest = new CxfDataStores(errorHandler,
				ChainablePutMap.of(new HashMap<String, DataStore>()) //
						.chainablePut("foo", mock(DataStore.class)) //
						.chainablePut("bar", mock(DataStore.class)) //
						.chainablePut("baz", mock(DataStore.class)), //
				hashing);
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());
		final File file = temporaryFolder.newFile();

		try {
			// when
			underTest.uploadFile("none", A_FOLDER, new DataHandler(new FileDataSource(file)));
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test(expected = DummyException.class)
	public void uploadFileCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		final File firstFolder = temporaryFolder.newFolder();
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());
		final File file = temporaryFolder.newFile();

		try {
			// when
			underTest.uploadFile(A_DATASTORE, A_FOLDER, new DataHandler(new FileDataSource(file)));
		} finally {
			// then
			verify(dataStore).folders();
			verify(hashing, times(3)).hash(anyString());
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test(expected = DummyException.class)
	public void uploadFileCallsErrorHandlerWhenFileNameIsDuplicated() throws Exception {
		// given
		final File firstFolder = temporaryFolder.newFolder(A_FOLDER);
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		final File alreadyCreatedFile = temporaryFolder.newFolder();
		doReturn(asList(alreadyCreatedFile)) //
				.when(dataStore).files(any(File.class));
		doThrow(DummyException.class) //
				.when(errorHandler).duplicateFileName(anyString());

		try {
			// when
			underTest.uploadFile(A_DATASTORE, A_FOLDER, new DataHandler(new FileDataSource(alreadyCreatedFile)));
		} finally {
			// then
			verify(dataStore).folders();
			verify(dataStore).files(eq(firstFolder));
			verify(hashing).hash(anyString());
			verify(errorHandler).duplicateFileName(eq(alreadyCreatedFile.getName()));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test
	public void uploadFile() throws Exception {
		// given
		final File firstFolder = temporaryFolder.newFolder(A_FOLDER);
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		doReturn(asList()) //
				.when(dataStore).files(any(File.class));
		final File created = temporaryFolder.newFile();
		doReturn(created) //
				.when(dataStore).create(any(File.class), any(DataHandler.class));
		final File file = temporaryFolder.newFile();
		final DataHandler dataHandler = new DataHandler(new FileDataSource(file));

		// when
		underTest.uploadFile(A_DATASTORE, A_FOLDER, dataHandler);

		// then
		verify(dataStore).folders();
		verify(dataStore).files(eq(firstFolder));
		verify(dataStore).create(eq(firstFolder), eq(dataHandler));
		verify(hashing, times(3)).hash(anyString());
		verifyNoMoreInteractions(errorHandler, dataStore, hashing);
	}

	@Test(expected = DummyException.class)
	public void readFilesCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		underTest = new CxfDataStores(errorHandler,
				ChainablePutMap.of(new HashMap<String, DataStore>()) //
						.chainablePut("foo", mock(DataStore.class)) //
						.chainablePut("bar", mock(DataStore.class)) //
						.chainablePut("baz", mock(DataStore.class)), //
				hashing);
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.readFiles("none", A_FOLDER);
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test(expected = DummyException.class)
	public void readFilesCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		final File firstFolder = temporaryFolder.newFolder();
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.readFiles(A_DATASTORE, A_FOLDER);
		} finally {
			// then
			verify(dataStore).folders();
			verify(hashing, times(3)).hash(anyString());
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test
	public void readFiles() throws Exception {
		final File firstFolder = temporaryFolder.newFolder(A_FOLDER);
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		final File firstFile = temporaryFolder.newFile();
		final File secondFile = temporaryFolder.newFile();
		final File thirdFile = temporaryFolder.newFile();
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(File.class));

		// when
		final ResponseMultiple<FileSystemObject> response = underTest.readFiles(A_DATASTORE, A_FOLDER);

		// then
		verify(dataStore).folders();
		verify(hashing, times(7)).hash(anyString());
		verify(dataStore).files(eq(firstFolder));
		verifyNoMoreInteractions(errorHandler, dataStore, hashing);

		assertThat(response.getElements(),
				containsInAnyOrder( //
						newFileSystemObject() //
								.withId(firstFile.getName()) //
								.withName(firstFile.getName()) //
								.withParent(firstFile.getParentFile().getName()) //
								.build(), //
						newFileSystemObject() //
								.withId(secondFile.getName()) //
								.withName(secondFile.getName()) //
								.withParent(secondFile.getParentFile().getName()) //
								.build(), //
						newFileSystemObject() //
								.withId(thirdFile.getName()) //
								.withName(thirdFile.getName()) //
								.withParent(thirdFile.getParentFile().getName()) //
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
						.chainablePut("baz", mock(DataStore.class)), //
				hashing);
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.readFile("none", A_FOLDER, A_FILE);
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test(expected = DummyException.class)
	public void readFileCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		final File firstFolder = temporaryFolder.newFolder();
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.readFile(A_DATASTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(dataStore).folders();
			verify(hashing, times(3)).hash(anyString());
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test(expected = DummyException.class)
	public void readFileCallsErrorHandlerWhenFileIsNotFound() throws Exception {
		// given
		final File firstFolder = temporaryFolder.newFolder(A_FOLDER);
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		final File firstFile = temporaryFolder.newFile();
		final File secondFile = temporaryFolder.newFile();
		final File thirdFile = temporaryFolder.newFile();
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(File.class));
		doThrow(DummyException.class) //
				.when(errorHandler).fileNotFound(anyString());

		try {
			// when
			underTest.readFile(A_DATASTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(dataStore).folders();
			verify(hashing, times(4)).hash(anyString());
			verify(dataStore).files(eq(firstFolder));
			verify(errorHandler).fileNotFound(eq(A_FILE));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test
	public void readFile() throws Exception {
		final File firstFolder = temporaryFolder.newFolder(A_FOLDER);
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		final File firstFile = temporaryFolder.newFile(A_FILE);
		final File secondFile = temporaryFolder.newFile();
		final File thirdFile = temporaryFolder.newFile();
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(File.class));

		// when
		final ResponseSingle<FileSystemObject> response = underTest.readFile(A_DATASTORE, A_FOLDER, A_FILE);

		// then
		verify(dataStore).folders();
		verify(hashing, times(4)).hash(anyString());
		verify(dataStore).files(eq(firstFolder));
		verifyNoMoreInteractions(errorHandler, dataStore, hashing);

		assertThat(response.getElement(),
				equalTo( //
						newFileSystemObject() //
								.withId(firstFile.getName()) //
								.withName(firstFile.getName()) //
								.withParent(firstFile.getParentFile().getName()) //
								.build()));
	}

	@Test(expected = DummyException.class)
	public void downloadFileCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		underTest = new CxfDataStores(errorHandler,
				ChainablePutMap.of(new HashMap<String, DataStore>()) //
						.chainablePut("foo", mock(DataStore.class)) //
						.chainablePut("bar", mock(DataStore.class)) //
						.chainablePut("baz", mock(DataStore.class)), //
				hashing);
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.downloadFile("none", A_FOLDER, A_FILE);
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test(expected = DummyException.class)
	public void downalodFileCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		final File firstFolder = temporaryFolder.newFolder();
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.downloadFile(A_DATASTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(dataStore).folders();
			verify(hashing, times(3)).hash(anyString());
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test(expected = DummyException.class)
	public void downloadFileCallsErrorHandlerWhenFileIsNotFound() throws Exception {
		// given
		final File firstFolder = temporaryFolder.newFolder(A_FOLDER);
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		final File firstFile = temporaryFolder.newFile();
		final File secondFile = temporaryFolder.newFile();
		final File thirdFile = temporaryFolder.newFile();
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(File.class));
		doThrow(DummyException.class) //
				.when(errorHandler).fileNotFound(anyString());

		try {
			// when
			underTest.downloadFile(A_DATASTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(dataStore).folders();
			verify(hashing, times(4)).hash(anyString());
			verify(dataStore).files(eq(firstFolder));
			verify(errorHandler).fileNotFound(eq(A_FILE));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test
	public void downloadFile() throws Exception {
		final File firstFolder = temporaryFolder.newFolder(A_FOLDER);
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		final File firstFile = temporaryFolder.newFile(A_FILE);
		write(get(firstFile.toURI()), "foo bar baz".getBytes());
		final File secondFile = temporaryFolder.newFile();
		final File thirdFile = temporaryFolder.newFile();
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(File.class));

		// when
		final DataHandler response = underTest.downloadFile(A_DATASTORE, A_FOLDER, A_FILE);

		// then
		verify(dataStore).folders();
		verify(hashing, times(2)).hash(anyString());
		verify(dataStore).files(eq(firstFolder));
		verifyNoMoreInteractions(errorHandler, dataStore, hashing);

		assertThat(response.getName(), equalTo(A_FILE));
		assertThat(toString(response.getInputStream()), equalTo("foo bar baz"));
	}

	@Test(expected = DummyException.class)
	public void deleteFileCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		underTest = new CxfDataStores(errorHandler,
				ChainablePutMap.of(new HashMap<String, DataStore>()) //
						.chainablePut("foo", mock(DataStore.class)) //
						.chainablePut("bar", mock(DataStore.class)) //
						.chainablePut("baz", mock(DataStore.class)), //
				hashing);
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.deleteFile("none", A_FOLDER, A_FILE);
		} finally {
			// then
			verify(errorHandler).dataStoreNotFound(eq("none"));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test(expected = DummyException.class)
	public void deleteFileCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		final File firstFolder = temporaryFolder.newFolder();
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.deleteFile(A_DATASTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(dataStore).folders();
			verify(hashing, times(3)).hash(anyString());
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test(expected = DummyException.class)
	public void deleteFileCallsErrorHandlerWhenFileIsNotFound() throws Exception {
		// given
		final File firstFolder = temporaryFolder.newFolder(A_FOLDER);
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		final File firstFile = temporaryFolder.newFile();
		final File secondFile = temporaryFolder.newFile();
		final File thirdFile = temporaryFolder.newFile();
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(File.class));
		doThrow(DummyException.class) //
				.when(errorHandler).fileNotFound(anyString());

		try {
			// when
			underTest.deleteFile(A_DATASTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(dataStore).folders();
			verify(hashing, times(4)).hash(anyString());
			verify(dataStore).files(eq(firstFolder));
			verify(errorHandler).fileNotFound(eq(A_FILE));
			verifyNoMoreInteractions(errorHandler, dataStore, hashing);
		}
	}

	@Test
	public void deleteFile() throws Exception {
		final File firstFolder = temporaryFolder.newFolder(A_FOLDER);
		final File secondFolder = temporaryFolder.newFolder();
		final File thirdFolder = temporaryFolder.newFolder();
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(dataStore).folders();
		final File firstFile = temporaryFolder.newFile(A_FILE);
		write(get(firstFile.toURI()), "foo bar baz".getBytes());
		final File secondFile = temporaryFolder.newFile();
		final File thirdFile = temporaryFolder.newFile();
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(dataStore).files(any(File.class));

		// when
		underTest.deleteFile(A_DATASTORE, A_FOLDER, A_FILE);

		// then
		verify(dataStore).folders();
		verify(dataStore).files(eq(firstFolder));
		verify(dataStore).delete(eq(firstFile));
		verify(hashing, times(2)).hash(anyString());
		verifyNoMoreInteractions(errorHandler, dataStore, hashing);
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
