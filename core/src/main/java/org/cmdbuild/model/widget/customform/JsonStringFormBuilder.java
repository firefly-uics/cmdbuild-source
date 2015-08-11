package org.cmdbuild.model.widget.customform;

import java.util.Collection;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

class JsonStringFormBuilder extends AttributesBasedFormBuilder {

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final TypeReference<Collection<? extends Attribute>> TYPE_REFERENCE = new TypeReference<Collection<? extends Attribute>>() {
	};

	private final String expression;

	public JsonStringFormBuilder(final String expression) {
		this.expression = expression;
	}

	@Override
	public Iterable<Attribute> attributes() {
		try {
			logger.debug(CustomFormWidgetFactory.MARKER, "parsing expression '{}'", expression);
			return mapper.readValue(expression, TYPE_REFERENCE);
		} catch (final Exception e) {
			logger.error(CustomFormWidgetFactory.MARKER, "error parsing expression", e);
			throw new RuntimeException(e);
		}
	}

}