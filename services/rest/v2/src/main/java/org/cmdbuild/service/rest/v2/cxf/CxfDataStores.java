package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.io.BaseEncoding.base64Url;
import static java.io.File.separator;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.v2.model.Models.newFileSystemObject;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.cmdbuild.service.rest.v2.DataStores;
import org.cmdbuild.service.rest.v2.model.FileSystemObject;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.services.ForwardingFilesStore;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.io.BaseEncoding;

public class CxfDataStores implements DataStores {

	public static interface DataStore {

		Iterable<File> folders();

		Iterable<File> files(File folder);

		File create(File folder, DataHandler dataHandler) throws IOException;

		void delete(File file);

	}

	public static class DefaultDataStore extends ForwardingFilesStore implements DataStore {

		private static final String NULL = null;
		private static final File[] MISSING_FILES = new File[] {};

		private final FilesStore delegate;

		public DefaultDataStore(final FilesStore delegate) {
			this.delegate = delegate;
		}

		@Override
		protected FilesStore delegate() {
			return delegate;
		}

		@Override
		public Iterable<File> folders() {
			final Collection<File> output = new ArrayList<>();
			output.add(getRoot());
			folders(delegate, output);
			return output;
		}

		private static void folders(final FilesStore filesStore, final Collection<File> output) {
			final Collection<File> collect = stream(filesStore.files(NULL).spliterator(), false) //
					.filter(input -> input.isDirectory()) //
					.collect(toSet());
			/*
			 * we cannot use forEach because we need to avoid the infinite loop
			 */
			for (final File input : collect) {
				output.add(input);
				folders(filesStore.sub(input.getName()), output);
			}
		}

		@Override
		public Iterable<File> files(final File folder) {
			requireNonNull(folder, "missing directory");
			return stream(listFiles(folder).spliterator(), false) //
					.filter(f -> f.isFile()) //
					.collect(toSet());
		}

		private Iterable<File> listFiles(final File folder) {
			final File[] output;
			if (folder.getAbsolutePath().startsWith(getRoot().getAbsolutePath())) {
				output = defaultIfNull(folder.listFiles(), MISSING_FILES);
			} else {
				output = null;
			}
			return asList(defaultIfNull(output, MISSING_FILES));
		}

		@Override
		public File create(final File folder, final DataHandler dataHandler) throws IOException {
			File output;
			if (folder.getAbsolutePath().startsWith(getRoot().getAbsolutePath())) {
				final String path = new StringBuilder() //
						.append(folder.getAbsolutePath().substring(getRoot().getAbsolutePath().length())) //
						.append(separator) //
						.append(dataHandler.getName()) //
						.toString();
				output = save(dataHandler.getInputStream(), path);
			} else {
				output = null;
			}
			return output;
		}

		@Override
		public void delete(final File file) {
			remove(file.getName());
		}

	}

	public static interface Hashing {

		String hash(String value);

	}

	public static class DefaultHashing implements Hashing {

		private final HashFunction hashFunction;

		public DefaultHashing() {
			hashFunction = com.google.common.hash.Hashing.md5();
		}

		@Override
		public String hash(final String value) {
			return hashFunction.hashBytes(value.getBytes()).toString();
		}

	}

	private final ErrorHandler errorHandler;
	private final Map<String, DataStore> stores;
	private final Hashing hashing;

	public CxfDataStores(final ErrorHandler errorHandler, final Map<String, DataStore> stores, final Hashing hashing) {
		this.errorHandler = requireNonNull(errorHandler, "missing " + ErrorHandler.class);
		this.stores = requireNonNull(stores, "missing " + Map.class);
		this.hashing = requireNonNull(hashing, "missing " + Hashing.class);
	}

	@Override
	public ResponseMultiple<FileSystemObject> readFolders(final String datastoreId) {
		final DataStore store = assureDataStore(datastoreId);
		final Collection<FileSystemObject> folders = stream(store.folders().spliterator(), false) //
				.map(input -> newFileSystemObject() //
						.withId(idOf(input)) //
						.withName(input.getName()) //
						.withParent(idOf(input.getParentFile())) //
						.build()) //
				.collect(toList());
		return newResponseMultiple(FileSystemObject.class) //
				.withElements(folders) //
				.withMetadata(newMetadata() //
						.withTotal(folders.size()) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<FileSystemObject> readFolder(final String datastoreId, final String folderId) {
		final DataStore store = assureDataStore(datastoreId);
		final Optional<FileSystemObject> folder = stream(store.folders().spliterator(), false) //
				.filter(input -> idOf(input).equals(folderId)) //
				.map(input -> newFileSystemObject() //
						.withId(idOf(input)) //
						.withName(input.getName()) //
						.withParent(idOf(input.getParentFile())) //
						.build()) //
				.findFirst();
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		return newResponseSingle(FileSystemObject.class) //
				.withElement(folder.get()) //
				.build();
	}

	@Override
	public ResponseSingle<FileSystemObject> uploadFile(final String datastoreId, final String folderId,
			final DataHandler dataHandler) {
		final DataStore store = assureDataStore(datastoreId);
		final Optional<File> folder = stream(store.folders().spliterator(), false) //
				.filter(input -> idOf(input).equals(folderId)) //
				.findFirst();
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Optional<File> file = stream(store.files(folder.get()).spliterator(), false) //
				.filter(input -> input.getName().equals(dataHandler.getName())) //
				.findFirst();
		if (file.isPresent()) {
			errorHandler.duplicateFileName(dataHandler.getName());
		}
		final File created = create(store, folder.get(), dataHandler);
		return newResponseSingle(FileSystemObject.class) //
				.withElement(newFileSystemObject() //
						.withId(idOf(created)) //
						.withName(created.getName()) //
						.withParent(idOf(created.getParentFile())) //
						.build()) //
				.build();
	}

	private File create(final DataStore store, final File folder, final DataHandler dataHandler) {
		try {
			return store.create(folder, dataHandler);
		} catch (final IOException e) {
			errorHandler.propagate(e);
			throw new AssertionError("should not come here", e);
		}
	}

	@Override
	public ResponseMultiple<FileSystemObject> readFiles(final String datastoreId, final String folderId) {
		final DataStore store = assureDataStore(datastoreId);
		final Optional<File> folder = stream(store.folders().spliterator(), false) //
				.filter(input -> idOf(input).equals(folderId)) //
				.findFirst();
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Collection<FileSystemObject> files = stream(store.files(folder.get()).spliterator(), false) //
				.map(input -> newFileSystemObject() //
						.withId(idOf(input)) //
						.withName(input.getName()) //
						.withParent(idOf(input.getParentFile())) //
						.build()) //
				.collect(toList());
		return newResponseMultiple(FileSystemObject.class) //
				.withElements(files) //
				.withMetadata(newMetadata() //
						.withTotal(files.size()) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<FileSystemObject> readFile(final String datastoreId, final String folderId,
			final String fileId) {
		final DataStore store = assureDataStore(datastoreId);
		final Optional<File> folder = stream(store.folders().spliterator(), false) //
				.filter(input -> idOf(input).equals(folderId)) //
				.findFirst();
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Optional<FileSystemObject> file = stream(store.files(folder.get()).spliterator(), false) //
				.filter(input -> idOf(input).equals(fileId)) //
				.map(input -> newFileSystemObject() //
						.withId(idOf(input)) //
						.withName(input.getName()) //
						.withParent(idOf(input.getParentFile())) //
						.build()) //
				.findFirst();
		if (!file.isPresent()) {
			errorHandler.fileNotFound(fileId);
		}
		return newResponseSingle(FileSystemObject.class) //
				.withElement(file.get()) //
				.build();
	}

	@Override
	public DataHandler downloadFile(final String datastoreId, final String folderId, final String fileId) {
		final DataStore store = assureDataStore(datastoreId);
		final Optional<File> folder = stream(store.folders().spliterator(), false) //
				.filter(input -> idOf(input).equals(folderId)) //
				.findFirst();
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Optional<File> file = stream(store.files(folder.get()).spliterator(), false) //
				.filter(input -> idOf(input).equals(fileId)) //
				.findFirst();
		if (!file.isPresent()) {
			errorHandler.fileNotFound(fileId);
		}
		return new DataHandler(new FileDataSource(file.get()));
	}

	@Override
	public void deleteFile(final String datastoreId, final String folderId, final String fileId) {
		final DataStore store = assureDataStore(datastoreId);
		final Optional<File> folder = stream(store.folders().spliterator(), false) //
				.filter(input -> idOf(input).equals(folderId)) //
				.findFirst();
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Optional<File> file = stream(store.files(folder.get()).spliterator(), false) //
				.filter(input -> idOf(input).equals(fileId)) //
				.findFirst();
		if (!file.isPresent()) {
			errorHandler.fileNotFound(fileId);
		}
		store.delete(file.get());
	}

	private DataStore assureDataStore(final String datastoreId) {
		final DataStore output = stores.get(datastoreId);
		if (output == null) {
			errorHandler.dataStoreNotFound(datastoreId);
		}
		return output;
	}

	private String idOf(final File input) {
		return hashing.hash(input.getAbsolutePath());
	}

}
