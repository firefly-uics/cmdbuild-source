package org.cmdbuild.dao.query.clause.alias;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;

public class EntryTypeAlias implements Alias {

	public static EntryTypeAlias canonicalAlias(final CMEntryType entryType) {
		return new EntryTypeAlias(entryType);
	}

	private final CMEntryType entryType;

	public EntryTypeAlias(final CMEntryType entryType) {
		this.entryType = entryType;
	}

	@Override
	public void accept(final AliasVisitor visitor) {
		visitor.visit(this);
	}

	public CMEntryType getEntryType() {
		return entryType;
	}

	@Override
	public int hashCode() {
		return entryType.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof EntryTypeAlias)) {
			return false;
		}
		final EntryTypeAlias other = EntryTypeAlias.class.cast(obj);
		final CMIdentifier thisIdentifier = entryType.getIdentifier();
		final CMIdentifier otherIdentifier = other.getEntryType().getIdentifier();
		return new EqualsBuilder() //
				.append(thisIdentifier.getLocalName(), otherIdentifier.getLocalName()) //
				.append(thisIdentifier.getNamespace(), otherIdentifier.getNamespace()) //
				.isEquals();
	}

	@Override
	public String toString() {
		return entryType.toString();
	}

}
