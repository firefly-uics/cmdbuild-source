package org.cmdbuild.dao.entrytype;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;

import com.google.common.collect.Lists;

public class EntryTypeAnalyzer {

	private final CMEntryType entryType;

	private EntryTypeAnalyzer(final CMEntryType entryType) {
		this.entryType = entryType;
	}

	public static EntryTypeAnalyzer inspect(final CMEntryType entryType) {
		Validate.notNull(entryType);
		return new EntryTypeAnalyzer(entryType);
	}

	/**
	 * 
	 * @return true if the entry type has at least one ACTIVE and NOT SYSTEM
	 *         attribute of one of the following types: Lookup, Reference,
	 *         ForeignKey; false otherwise
	 */
	public boolean hasExternalReferences() {
		for (final CMAttribute attribute : entryType.getActiveAttributes()) {
			final CMAttributeType<?> attributeType = attribute.getType();
			if (attributeType instanceof LookupAttributeType || //
					attributeType instanceof ReferenceAttributeType || //
					attributeType instanceof ForeignKeyAttributeType) {
				return true;
			}
		}
		return false;
	}

	public Iterable<CMAttribute> getLookupAttributes() {
		final List<CMAttribute> lookupAttributes = Lists.newArrayList();
		for (final CMAttribute attribute : entryType.getActiveAttributes()) {
			if (attribute.getType() instanceof LookupAttributeType) {
				lookupAttributes.add(attribute);
			}
		}
		return lookupAttributes;
	}

	public Iterable<CMAttribute> getReferenceAttributes() {
		final List<CMAttribute> referenceAttributes = Lists.newArrayList();
		for (final CMAttribute attribute : entryType.getActiveAttributes()) {
			if (attribute.getType() instanceof ReferenceAttributeType) {
				referenceAttributes.add(attribute);
			}
		}
		return referenceAttributes;
	}

	public Iterable<CMAttribute> getForeignKeyAttributes() {
		final List<CMAttribute> foreignKeyAttributes = Lists.newArrayList();
		if (!entryType.holdsHistory()) { // entry type is a simple class
			for (final CMAttribute attribute : entryType.getActiveAttributes()) {
				if (attribute.getType() instanceof ForeignKeyAttributeType) {
					foreignKeyAttributes.add(attribute);
				}
			}
		}
		return foreignKeyAttributes;
	}

}
