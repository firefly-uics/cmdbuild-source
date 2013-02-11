package org.cmdbuild.dao.driver.postgres.quote;

import static java.lang.String.format;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;

public abstract class AbstractEntryTypeQuoter implements Quoter {

	protected final CMEntryType entryType;

	public AbstractEntryTypeQuoter(final CMEntryType entryType) {
		this.entryType = entryType;
	}

	protected String quoteClassOrDomain(final CMIdentifier identifier) {
		final String quotedTypeName;
		if (identifier.getNamespace() != CMIdentifier.DEFAULT_NAMESPACE) {
			quotedTypeName = format("%s.%s", //
					IdentQuoter.quote(identifier.getNamespace()), IdentQuoter.quote(identifier.getLocalName()));
		} else {
			quotedTypeName = IdentQuoter.quote(identifier.getLocalName());
		}
		return quotedTypeName;
	}

}
