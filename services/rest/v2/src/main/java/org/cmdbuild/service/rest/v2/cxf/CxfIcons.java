package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.requireNonNull;
import static org.cmdbuild.logic.icon.Types.classType;
import static org.cmdbuild.service.rest.v2.model.Models.newIcon;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.activation.DataHandler;

import org.cmdbuild.logic.icon.Element;
import org.cmdbuild.logic.icon.IconsLogic;
import org.cmdbuild.logic.icon.Type;
import org.cmdbuild.logic.icon.TypeVisitor;
import org.cmdbuild.logic.icon.Types.ClassType;
import org.cmdbuild.logic.icon.Types.ProcessType;
import org.cmdbuild.service.rest.v2.Icons;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.cmdbuild.service.rest.v2.model.Icon;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

import com.google.common.base.Function;

public class CxfIcons implements Icons, LoggingSupport {

	private static final String CLASS = "class";
	private static final String PROCESS = "process";

	public static class IconToElement implements Function<Icon, Element> {

		private final ErrorHandler errorHandler;

		public IconToElement(final ErrorHandler errorHandler) {
			this.errorHandler = errorHandler;
		}

		@Override
		public Element apply(final Icon input) {
			final Type type;
			switch (input.getType()) {
			case CLASS: {
				final Map<String, Object> details = requireNonNull(input.getDetails(), "missing details");
				final String name = String.class.cast(requireNonNull(details.get(Icon.id), "missing " + Icon.id));
				type = classType() //
						.withName(name) //
						.build();
				break;
			}
			case PROCESS: {
				final Map<String, Object> details = requireNonNull(input.getDetails(), "missing details");
				final String name = String.class.cast(requireNonNull(details.get(Icon.id), "missing " + Icon.id));
				type = classType() //
						.withName(name) //
						.build();
				break;
			}
			default:
				errorHandler.invalidIconType(input.getType());
				throw new AssertionError("should never come here");
			}
			return new Element() {

				@Override
				public String getId() {
					return input.getId();
				}

				@Override
				public Type getType() {
					return type;
				}

			};
		}

	}

	public static class ElementToIcon implements Function<Element, Icon> {

		@Override
		public Icon apply(final Element input) {
			return new TypeVisitor() {

				private String type;
				private Map<String, Object> details;

				public Icon icon() {
					input.getType().accept(this);
					return newIcon() //
							.withId(input.getId()) //
							.withType(requireNonNull(type, "missing type")) //
							.withDetails(requireNonNull(details, "missing details")) //
							.build();
				}

				@Override
				public void visit(final ClassType type) {
					this.type = CLASS;
					this.details = newHashMap();
					this.details.put(Icon.id, type.getName());
				}

				@Override
				public void visit(final ProcessType type) {
					this.type = PROCESS;
					this.details = newHashMap();
					this.details.put(Icon.id, type.getName());
				}

			}.icon();
		}

	}

	private final ErrorHandler errorHandler;
	private final IconsLogic logic;
	private final Function<Icon, Element> iconToElement;
	private final Function<Element, Icon> elementToIcon;

	public CxfIcons(final ErrorHandler errorHandler, final IconsLogic logic,
			final Function<Icon, Element> iconToElement, final Function<Element, Icon> elementToIcon) {
		this.errorHandler = errorHandler;
		this.logic = logic;
		this.iconToElement = iconToElement;
		this.elementToIcon = elementToIcon;
	}

	@Override
	public ResponseSingle<Icon> create(final Icon icon, final DataHandler dataHandler) {
		try {
			final Element created = logic.create( //
					iconToElement.apply( //
							requireNonNull(icon, "missing icon")), //
					requireNonNull(dataHandler, "missing data handler"));
			return newResponseSingle(Icon.class) //
					.withElement(elementToIcon.apply(created)) //
					.build();
		} catch (final IOException e) {
			logger.error("error creating icon");
			errorHandler.propagate(e);
			throw new AssertionError("should never come here");
		}
	}

	@Override
	public ResponseMultiple<Icon> read() {
		final Iterable<Element> elements = logic.read();
		return newResponseMultiple(Icon.class) //
				.withElements(from(elements) //
						.transform(elementToIcon)) //
				.withMetadata(newMetadata() //
						.withTotal(size(elements)) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<Icon> read(final String id) {
		requireNonNull(id, "missing id");
		// FIXME but cannot use converter/function
		final Element element = new Element() {

			@Override
			public String getId() {
				return id;
			}

			@Override
			public Type getType() {
				return null;
			}

		};
		final Optional<Element> read = logic.read(element);
		final Icon output;
		if (read.isPresent()) {
			output = elementToIcon.apply(read.get());
		} else {
			errorHandler.missingIcon(id);
			output = null;
		}
		return newResponseSingle(Icon.class) //
				.withElement(output) //
				.build();
	}

	@Override
	public DataHandler download(final String id) {
		requireNonNull(id, "missing id");
		// FIXME but cannot use converter/function
		final Element element = new Element() {

			@Override
			public String getId() {
				return id;
			}

			@Override
			public Type getType() {
				return null;
			}

		};
		final Optional<DataHandler> download = logic.download(element);
		final DataHandler output;
		if (download.isPresent()) {
			output = download.get();
		} else {
			errorHandler.missingIcon(id);
			output = null;
		}
		return output;
	}

	@Override
	public void update(final String id, final DataHandler dataHandler) {
		try {
			requireNonNull(id, "missing id");
			requireNonNull(dataHandler, "missing data handler");
			// FIXME but cannot use converter/function
			final Element element = new Element() {

				@Override
				public String getId() {
					return id;
				}

				@Override
				public Type getType() {
					return null;
				}

			};
			final Optional<Element> read = logic.read(element);
			if (read.isPresent()) {
				logic.update(read.get(), dataHandler);
			} else {
				errorHandler.missingIcon(id);
			}
		} catch (final IOException e) {
			logger.error("error updating icon");
			errorHandler.propagate(e);
			throw new AssertionError("should never come here");
		}
	}

	@Override
	public void delete(final String id) {
		requireNonNull(id, "missing id");
		// FIXME but cannot use converter/function
		final Element element = new Element() {

			@Override
			public String getId() {
				return id;
			}

			@Override
			public Type getType() {
				return null;
			}

		};
		final Optional<Element> read = logic.read(element);
		if (read.isPresent()) {
			logic.delete(read.get());
		} else {
			errorHandler.missingIcon(id);
		}
	}

}
