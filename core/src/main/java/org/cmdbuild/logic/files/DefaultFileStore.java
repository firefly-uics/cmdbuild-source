package org.cmdbuild.logic.files;

import static java.io.File.separator;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.StringUtils.difference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.services.FilesStore;

public class DefaultFileStore implements FileStore {

	private static final String NULL = null;
	private static final File[] MISSING_FILES = new File[] {};

	private final FilesStore delegate;
	private final Hashing hashing;

	public DefaultFileStore(final FilesStore delegate, final Hashing hashing) {
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
					public Optional<File> apply(final File directory) {
						try {
							final String path = new StringBuilder() //
									.append(relativePath(directory)) //
									.append(separator) //
									.append(dataHandler.getName()) //
									.toString();
							return of(delegate.save(dataHandler.getInputStream(), path));
						} catch (final IOException e) {
							// TODO log
							return empty();
						}
					}

				}) //
				.map(input -> toElement(input));
	}

	@Override
	public Optional<DataHandler> download(final Element file) {
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

			private final File root = delegate.getRoot();
			private final String id = id(value);
			private final String parent = id(value.getParentFile());
			private final String name = value.getName();

			@Override
			public String getId() {
				return id;
			}

			@Override
			public String getParent() {
				return value.equals(root) ? null : parent;
			};

			@Override
			public String getName() {
				return value.equals(root) ? null : name;
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
			public boolean equals(final Object obj) {
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