package unit.cxf;

import static com.google.common.collect.Iterables.size;
import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.cmdbuild.service.rest.v2.cxf.CxfDataStores.DefaultDataStore;
import org.cmdbuild.services.FilesStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

public class DefaultDataStoreTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private FilesStore filesStore;
	private DefaultDataStore underTest;

	@Before
	public void setUp() throws Exception {
		filesStore = mock(FilesStore.class);
		underTest = new DefaultDataStore(filesStore);
	}

	@Test
	public void foldersAreReturnedRecursively() throws Exception {
		// given
		doReturn(tmp.getRoot()) //
				.when(filesStore).getRoot();
		doReturn(filesStore) //
				.when(filesStore).sub(anyString());
		doReturn(asList(tmp.newFolder("foo"), tmp.newFolder("bar"), tmp.newFile())) //
				.doReturn(asList(tmp.newFolder("baz"), tmp.newFile(), tmp.newFile())) //
				.doReturn(asList(tmp.newFolder("lol"), tmp.newFolder("rotfl"), tmp.newFile())) //
				// last one must be with files only
				.doReturn(asList(tmp.newFile())) //
				.when(filesStore).files(anyString());

		// when
		final Iterable<File> files = underTest.folders();

		// then
		verify(filesStore).getRoot();
		verify(filesStore, times(6)).files(eq(null));
		verify(filesStore, times(5)).sub(anyString());
		verifyNoMoreInteractions(filesStore);

		assertThat(files,
				containsInAnyOrder(tmp.getRoot(), new File(tmp.getRoot(), "foo"), new File(tmp.getRoot(), "bar"),
						new File(tmp.getRoot(), "baz"), new File(tmp.getRoot(), "lol"),
						new File(tmp.getRoot(), "rotfl")));
	}

	@Test
	public void fileIsNotCreatedIfDirectoryIsExternalToRoot() throws Exception {
		// given
		doReturn(tmp.newFolder()) //
				.when(filesStore).getRoot();
		final File targetFolder = tmp.newFolder("foo", "bar", "baz");
		final File toBeCreated = tmp.newFile();
		write(get(toBeCreated.toURI()), "this is a test".getBytes());
		final DataHandler dataHandler = new DataHandler(new FileDataSource(toBeCreated));

		// when
		final File created = underTest.create(targetFolder, dataHandler);

		// then
		verify(filesStore).getRoot();
		verifyNoMoreInteractions(filesStore);

		assertThat(created, equalTo(null));
	}

	@Test
	public void fileIsCreated() throws Exception {
		// given
		doReturn(tmp.getRoot()) //
				.when(filesStore).getRoot();
		final File returnedFromSave = tmp.newFile();
		doReturn(returnedFromSave) //
				.when(filesStore).save(any(InputStream.class), anyString());
		final File targetFolder = tmp.newFolder("foo", "bar", "baz");
		final File toBeCreated = tmp.newFile();
		write(get(toBeCreated.toURI()), "this is a test".getBytes());
		final DataHandler dataHandler = new DataHandler(new FileDataSource(toBeCreated));

		// when
		final File created = underTest.create(targetFolder, dataHandler);

		// then
		final ArgumentCaptor<InputStream> captor = ArgumentCaptor.forClass(InputStream.class);
		verify(filesStore, times(2)).getRoot();
		verify(filesStore).save(captor.capture(), eq("/foo/bar/baz/" + dataHandler.getName()));
		verifyNoMoreInteractions(filesStore);

		assertThat(created.exists(), equalTo(true));
		assertThat(toString(captor.getValue()), equalTo("this is a test"));
	}

	@Test
	public void filesAreNotReturnedIfFileIsNotExisting() throws Exception {
		// given
		doReturn(tmp.getRoot()) //
				.when(filesStore).getRoot();
		final File sholdExist = new File(tmp.getRoot(), "foo");

		// when
		final Iterable<File> files = underTest.files(sholdExist);

		// then
		verify(filesStore).getRoot();
		verifyNoMoreInteractions(filesStore);

		assertThat(size(files), equalTo(0));
	}

	@Test
	public void filesAreNotReturnedIfFileIsNotADirectory() throws Exception {
		// given
		doReturn(tmp.getRoot()) //
				.when(filesStore).getRoot();
		final File sholdBeADirectory = tmp.newFile();

		// when
		final Iterable<File> files = underTest.files(sholdBeADirectory);

		// then
		verify(filesStore).getRoot();
		verifyNoMoreInteractions(filesStore);

		assertThat(size(files), equalTo(0));
	}

	@Test
	public void filesAreNotReturnedIfDirectoryIsExternalToRoot() throws Exception {
		// given
		doReturn(tmp.newFolder()) //
				.when(filesStore).getRoot();
		final File ext = tmp.newFolder();
		final File foo = new File(ext, "foo");
		foo.createNewFile();

		// when
		final Iterable<File> files = underTest.files(ext);

		// then
		verify(filesStore).getRoot();
		verifyNoMoreInteractions(filesStore);

		assertThat(size(files), equalTo(0));
	}

	@Test
	public void filesAreReturned() throws Exception {
		// given
		final File root = tmp.getRoot();
		doReturn(root) //
				.when(filesStore).getRoot();
		final File foo = tmp.newFile();
		final File bar = tmp.newFile();
		final File baz = tmp.newFile();

		// when
		final Iterable<File> files = underTest.files(root);

		// then
		verify(filesStore).getRoot();
		verifyNoMoreInteractions(filesStore);

		assertThat(files, containsInAnyOrder(foo, bar, baz));
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
