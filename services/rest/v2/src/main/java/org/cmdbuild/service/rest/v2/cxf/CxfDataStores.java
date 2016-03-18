package org.cmdbuild.service.rest.v2.cxf;

import static java.io.File.separator;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.StringUtils.difference;
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
import java.util.function.Function;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.v2.DataStores;
import org.cmdbuild.service.rest.v2.model.FileSystemObject;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.service.rest.v2.model.ClassWithFullDetails.AttributeOrder;
import org.cmdbuild.services.FilesStore;

import com.google.common.hash.HashFunction;

public class CxfDataStores implements DataStores {

	public static interface Element {

		String getId();

		String getParent();

		String getName();

	}

	public static interface DataStore {

		Iterable<Element> folders();

		Optional<Element> folder(String folder);

		// TODO change String to Element
		Iterable<Element> files(String folder);

		// TODO change String to Element
		Optional<Element> create(String folder, DataHandler dataHandler);

		Optional<DataHandler> download(Element file);

		void delete(Element file);

	}

	public static class DefaultDataStore implements DataStore {

		private static final String NULL = null;
		private static final File[] MISSING_FILES = new File[] {};

		private final FilesStore delegate;
		private final Hashing hashing;

		public DefaultDataStore(final FilesStore delegate, final Hashing hashing) {
			this.delegate = delegate;
			this.hashing = hashing;
		}

		private Collection<File> directories() {
			final Collection<File> output = new ArrayList<>();
			directories(delegate, output);
			return output;
		}

		private static void directories(final FilesStore filesStore, final Collection<File> output) {
			output.add(filesStore.getRoot());
			stream(filesStore.files(NULL).spliterator(), false) //
					.filter(File::isDirectory) //
					.forEach(input -> directories(filesStore.sub(input.getName()), output));
		}

		@Override
		public Iterable<Element> folders() {
			return directories().stream() //
					.map(input -> toElement(input)) //
					.collect(toSet());
		}

		@Override
		public Optional<Element> folder(final String folder) {
			return directories().stream() //
					.filter(input -> id(input).equals(requireNonNull(folder, "missing folder"))) //
					.findFirst() //
					.map(input -> toElement(input));
		}

		@Override
		public Iterable<Element> files(final String folder) {
			return stream(directories().stream() //
					.filter(input -> id(input).equals(requireNonNull(folder, "missing folder"))) //
					.findFirst() //
					.map(File::listFiles) //
					.orElse(MISSING_FILES)) //
							.filter(File::isFile) //
							.map(input -> toElement(input)) //
							.collect(toList());
		}

		@Override
		public Optional<Element> create(final String folder, final DataHandler dataHandler) {
			return directories().stream() //
					.filter(input -> id(input).equals(requireNonNull(folder, "missing folder"))) //
					.findFirst() //
					.flatMap(new Function<File, Optional<File>>() {

						@Override
						public Optional<File> apply(File directory) {
							try {
								final String path = new StringBuilder() //
										.append(relativePath(directory)) //
										.append(separator) //
										.append(dataHandler.getName()) //
										.toString();
								return of(delegate.save(dataHandler.getInputStream(), path));
							} catch (IOException e) {
								// TODO log
								return empty();
							}
						}

					}) //
					.map(input -> toElement(input));
		}

		@Override
		public Optional<DataHandler> download(Element file) {
			requireNonNull(file, "missing file");
			return stream(directories().stream() //
					.filter(input -> id(input).equals(file.getParent())) //
					.findFirst() //
					.map(File::listFiles) //
					.orElse(MISSING_FILES)) //
							.filter(input -> id(input).equals(file.getId())) //
							.findFirst() //
							.map(input -> new DataHandler(new FileDataSource(input)));
		}

		@Override
		public void delete(final Element file) {
			requireNonNull(file, "missing file");
			stream(directories().stream() //
					.filter(input -> id(input).equals(file.getParent())) //
					.findFirst() //
					.map(File::listFiles) //
					.orElse(MISSING_FILES)) //
							.filter(input -> id(input).equals(file.getId())) //
							.findFirst() //
							.ifPresent(File::delete);
		}

		private Element toElement(final File value) {
			return new Element() {

				private final String id = id(value);
				private final String parent = id(value);
				private final String name = value.getName();

				@Override
				public String getId() {
					return id;
				}

				@Override
				public String getParent() {
					return parent;
				};

				@Override
				public String getName() {
					return name;
				}

				@Override
				public int hashCode() {
					return new HashCodeBuilder() //
							.append(getId()) //
							.append(getParent()) //
							.append(getName()) //
							.toHashCode();
				}

				@Override
				public boolean equals(Object obj) {
					if (obj == this) {
						return true;
					}
					if (!(obj instanceof Element)) {
						return false;
					}
					final Element other = Element.class.cast(obj);
					return new EqualsBuilder() //
							.append(this.getId(), other.getId()) //
							.append(this.getParent(), other.getParent()) //
							.append(this.getName(), other.getName()) //
							.isEquals();
				}

				@Override
				public String toString() {
					return getId();
				}

			};
		}

		private String id(final File value) {
			return hashing.hash(relativePath(value));
		}

		private String relativePath(final File value) {
			return difference(delegate.getRoot().getAbsolutePath(), value.getAbsolutePath());
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

	public CxfDataStores(final ErrorHandler errorHandler, final Map<String, DataStore> stores) {
		this.errorHandler = requireNonNull(errorHandler, "missing " + ErrorHandler.class);
		this.stores = requireNonNull(stores, "missing " + Map.class);
	}

	@Override
	public ResponseMultiple<FileSystemObject> readFolders(final String datastoreId) {
		final DataStore store = assureDataStore(datastoreId);
		final Collection<FileSystemObject> folders = stream(store.folders().spliterator(), false) //
				.map(input -> newFileSystemObject() //
						.withId(input.getId()) //
						.withName(input.getName()) //
						.withParent(input.getParent()) //
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
		final Optional<FileSystemObject> folder = store.folder(folderId) //
				.map(input -> newFileSystemObject() //
						.withId(input.getId()) //
						.withName(input.getName()) //
						.withParent(input.getParent()) //
						.build());
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
		final Optional<Element> folder = store.folder(folderId);
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Optional<Element> file = stream(store.files(folderId).spliterator(), false) //
				.filter(input -> input.getName().equals(dataHandler.getName())) //
				.findFirst();
		if (file.isPresent()) {
			errorHandler.duplicateFileName(dataHandler.getName());
		}
		final Optional<Element> created = store.create(folderId, dataHandler);
		if (!created.isPresent()) {
			errorHandler.fileNotCreated();
		}
		final Element element = created.get();
		return newResponseSingle(FileSystemObject.class) //
				.withElement(newFileSystemObject() //
						.withId(element.getId()) //
						.withName(element.getName()) //
						.withParent(element.getParent()) //
						.build()) //
				.build();
	}

	@Override
	public ResponseMultiple<FileSystemObject> readFiles(final String datastoreId, final String folderId) {
		final DataStore store = assureDataStore(datastoreId);
		final Optional<Element> folder = store.folder(folderId);
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Collection<FileSystemObject> files = stream(store.files(folderId).spliterator(), false) //
				.map(input -> newFileSystemObject() //
						.withId(input.getId()) //
						.withName(input.getName()) //
						.withParent(input.getParent()) //
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
		final Optional<Element> folder = store.folder(folderId);
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Optional<FileSystemObject> file = stream(store.files(folderId).spliterator(), false) //
				.filter(input -> input.getId().equals(fileId)) //
				.map(input -> newFileSystemObject() //
						.withId(input.getId()) //
						.withName(input.getName()) //
						.withParent(input.getParent()) //
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
		final Optional<Element> folder = store.folder(folderId);
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Optional<Element> file = stream(store.files(folderId).spliterator(), false) //
				.filter(input -> input.getId().equals(fileId)) //
				.findFirst();
		if (!file.isPresent()) {
			errorHandler.fileNotFound(fileId);
		}
		return store.download(file.get()).get();
	}

	@Override
	public void deleteFile(final String datastoreId, final String folderId, final String fileId) {
		final DataStore store = assureDataStore(datastoreId);
		final Optional<Element> folder = store.folder(folderId);
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Optional<Element> file = stream(store.files(folderId).spliterator(), false) //
				.filter(input -> input.getId().equals(fileId)) //
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

}
