package org.cmdbuild.dao.query.clause;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.dao.query.clause.alias.Aliases.name;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.dao.query.clause.alias.Alias;

public class NamedAttribute implements QueryAttribute {

	private final Alias alias;
	private final String name;

	NamedAttribute(final String fullname) {
		final String[] split = fullname.split("\\.");
		switch (split.length) {
		case 1:
			alias = null;
			name = split[0];
			break;
		case 2:
			alias = name(split[0]);
			name = split[1];
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void accept(final QueryAttributeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Alias getAlias() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof NamedAttribute)) {
			return false;
		}
		final NamedAttribute other = NamedAttribute.class.cast(obj);
		return new EqualsBuilder() //
				.append(alias, other.alias) //
				.append(name, other.name) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(alias) //
				.append(name) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}
