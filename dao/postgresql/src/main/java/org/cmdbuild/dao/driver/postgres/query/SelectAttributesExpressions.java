package org.cmdbuild.dao.driver.postgres.query;

import static com.google.common.collect.Iterables.transform;
import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAlias;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAttribute;

import java.util.Collection;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.cmdbuild.dao.driver.postgres.logging.LoggingSupport;
import org.cmdbuild.dao.query.clause.alias.Alias;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class SelectAttributesExpressions implements SelectAttributesHolder, LoggingSupport {

	private static class Element {

		private final Alias typeAlias;
		private final String name;
		private final String cast;
		private final Alias alias;

		public Element(final Alias typeAlias, final String name, final String cast, final Alias alias) {
			this.typeAlias = typeAlias;
			this.name = name;
			this.cast = cast;
			this.alias = alias;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private final Collection<Element> elements;

	public SelectAttributesExpressions() {
		elements = Lists.newArrayList();
	}

	@Override
	public void add(final Alias typeAlias, final String name, final String cast, final Alias alias) {
		final Element element = new Element(typeAlias, name, cast, alias);
		logger.debug("adding element '{}'", element);
		elements.add(element);
	}

	/**
	 * Returns the expressions that must be used within <code>SELECT</code>
	 * statement.
	 * 
	 * @return an iterable collection of expressions.
	 */
	public Iterable<String> getExpressions() {
		return transform(elements, new Function<Element, String>() {
			@Override
			public String apply(final Element input) {
				logger.debug("transforming element '{}'", input);
				final StringBuffer sb = new StringBuffer(quoteAttribute(input.typeAlias, input.name));
				if (input.cast != null) {
					logger.debug("appending cast '{}'", input.cast);
					sb.append("::").append(input.cast);
				}
				if (input.alias != null) {
					logger.debug("appending alias '{}'", input.alias);
					sb.append(" AS ").append(quoteAlias(input.alias));
				}
				final String expression = sb.toString();
				logger.debug("appending expression '{}'", expression);
				return expression;
			}
		});
	}

}
