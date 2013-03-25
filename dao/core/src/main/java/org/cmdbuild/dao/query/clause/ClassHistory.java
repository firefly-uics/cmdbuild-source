package org.cmdbuild.dao.query.clause;

import static com.google.common.collect.Iterables.transform;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.PlaceholderClass;

import com.google.common.base.Function;

public class ClassHistory extends PlaceholderClass { // Why place holder?

	private static final Function<CMClass, CMClass> TO_HISTORIC = new Function<CMClass, CMClass>() {

		@Override
		public CMClass apply(final CMClass input) {
			return history(input);
		}

	};

	private final CMClass current;

	private ClassHistory(final CMClass current) {
		this.current = current;
	}

	public static CMClass history(final CMClass current) {
		return new ClassHistory(current);
	}

	@Override
	public CMIdentifier getIdentifier() {
		return current.getIdentifier();
	}

	@Override
	public Long getId() {
		return current.getId();
	}

	@Override
	public String getName() {
		return current.getName() + " HISTORY";
	}

	public CMClass getCurrent() {
		return current;
	}

	@Override
	public Iterable<? extends CMClass> getLeaves() {
		return transform(current.getLeaves(), TO_HISTORIC);
	}

	@Override
	public boolean isSuperclass() {
		return false;
	}

	@Override
	public Iterable<? extends CMAttribute> getActiveAttributes() {
		return current.getActiveAttributes();
	}

	@Override
	public CMAttribute getAttribute(final String name) {
		return current.getAttribute(name);
	}

	@Override
	public boolean holdsHistory() {
		return current.holdsHistory();
	}

	@Override
	public int hashCode() {
		return current.getId().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof CMEntryType == false) {
			return false;
		}
		final CMEntryType other = CMEntryType.class.cast(obj);
		return current.getId().equals(other.getId());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("name", current.getIdentifier().getLocalName()) //
				.append("namespace", current.getIdentifier().getNamespace()) //
				.toString();
	}

}
