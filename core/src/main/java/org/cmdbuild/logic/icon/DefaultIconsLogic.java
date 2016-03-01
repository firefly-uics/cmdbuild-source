package org.cmdbuild.logic.icon;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static org.cmdbuild.logic.icon.Types.classType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.activation.DataHandler;

import org.bimserver.utils.FileDataSource;
import org.cmdbuild.logic.icon.Types.ClassType;
import org.cmdbuild.services.FilesStore;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class DefaultIconsLogic implements IconsLogic {

	public static interface IdGenerator {

		String generate(Type type);

	}

	public static class UuidIdGenerator implements IdGenerator {

		@Override
		public String generate(final Type type) {
			return randomUUID().toString();
		}

	}

	private static final String SEPARATOR = "_";

	private static Function<File, Element> FILE_TO_ELEMENT = new Function<File, Element>() {

		@Override
		public Element apply(final File input) {
			final List<String> parts = Splitter.on(SEPARATOR) //
					.limit(2) //
					.splitToList(input.getName());
			return new Element() {

				@Override
				public String getId() {
					return parts.get(0);
				}

				@Override
				public Type getType() {
					return classType() //
							.withName(parts.get(1)) //
							.build();
				}

			};
		};

	};

	private final FilesStore filesStore;
	private final IdGenerator idGenerator;

	public DefaultIconsLogic(final FilesStore filesStore, final IdGenerator idGenerator) {
		this.filesStore = requireNonNull(filesStore, "missing " + FilesStore.class);
		this.idGenerator = requireNonNull(idGenerator, "missing " + IdGenerator.class);
	}

	@Override
	public Element create(final Element element, final DataHandler dataHandler) throws IOException {
		try {
			requireNonNull(element, "missing " + Element.class);
			requireNonNull(dataHandler, "missing " + DataHandler.class);
			final String generatedId = idGenerator.generate(element.getType());
			final String name = Joiner.on(SEPARATOR) //
					.join(generatedId, new TypeVisitor() {

						private String output;

						public String part() {
							element.getType().accept(this);
							return output;
						}

						@Override
						public void visit(final ClassType type) {
							output = type.getName();
						}

					}.part());
			filesStore.save(dataHandler.getInputStream(), name);
			return new Element() {

				@Override
				public String getId() {
					return generatedId;
				}

				@Override
				public Type getType() {
					return element.getType();
				}

			};
		} catch (final IOException e) {
			logger.error("error saving file", e);
			throw e;
		}
	}

	@Override
	public Iterable<Element> read() {
		return from(filesStore.files(null)) //
				.transform(FILE_TO_ELEMENT);
	}

	@Override
	public Optional<Element> read(final Element element) {
		final String pattern = Joiner.on(SEPARATOR) //
				.join(element.getId(), ".*");
		return ofNullable(from(filesStore.files(pattern)) //
				.limit(1) //
				.transform(FILE_TO_ELEMENT) //
				.first() //
				.orNull());
	}

	@Override
	public Optional<DataHandler> download(final Element element) {
		final Optional<Element> read = read(element);
		final Optional<DataHandler> output;
		if (read.isPresent()) {
			final String name = Joiner.on(SEPARATOR) //
					.join(read.get().getId(), new TypeVisitor() {

						private String output;

						public String part() {
							read.get().getType().accept(this);
							return output;
						}

						@Override
						public void visit(final ClassType type) {
							output = type.getName();
						}

					}.part());
			final Optional<File> file = ofNullable(from(filesStore.files(name)) //
					.limit(1) //
					.first() //
					.orNull());
			output = file.isPresent() ? of(new DataHandler(new FileDataSource(file.get()))) : empty();
		} else {
			output = empty();
		}
		return output;
	}

	@Override
	public void update(final Element element, final DataHandler dataHandler) throws IOException {
		final Optional<Element> read = read(element);
		if (read.isPresent()) {
			final String name = Joiner.on(SEPARATOR) //
					.join(read.get().getId(), new TypeVisitor() {

						private String output;

						public String part() {
							read.get().getType().accept(this);
							return output;
						}

						@Override
						public void visit(final ClassType type) {
							output = type.getName();
						}

					}.part());
			final Optional<File> file = ofNullable(from(filesStore.files(name)) //
					.limit(1) //
					.first() //
					.orNull());
			if (file.isPresent()) {
				filesStore.remove(name);
				filesStore.save(dataHandler.getInputStream(), name);
			}
		}
	}

	@Override
	public void delete(final Element element) {
		final Optional<Element> read = read(element);
		if (read.isPresent()) {
			final String name = Joiner.on(SEPARATOR) //
					.join(read.get().getId(), new TypeVisitor() {

						private String output;

						public String part() {
							read.get().getType().accept(this);
							return output;
						}

						@Override
						public void visit(final ClassType type) {
							output = type.getName();
						}

					}.part());
			final Optional<File> file = ofNullable(from(filesStore.files(name)) //
					.limit(1) //
					.first() //
					.orNull());
			if (file.isPresent()) {
				filesStore.remove(name);
			}
		}
	}

}
