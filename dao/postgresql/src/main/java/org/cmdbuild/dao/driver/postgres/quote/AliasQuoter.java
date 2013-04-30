package org.cmdbuild.dao.driver.postgres.quote;

import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.AliasVisitor;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;

public class AliasQuoter implements Quoter {

	private static final String NAMESPACE_LOCALNAME_SEPARATOR = "_";

	public static String quote(final Alias alias) {
		return new AliasQuoter(alias).quote();
	}

	private final Alias alias;

	public AliasQuoter(final Alias alias) {
		this.alias = alias;
	}

	@Override
	public String quote() {
		final StringBuilder toQuote = new StringBuilder();
		alias.accept(new AliasVisitor() {

			@Override
			public void visit(final EntryTypeAlias alias) {
				final CMIdentifier identifier = alias.getEntryType().getIdentifier();
				if (identifier.getNameSpace() != CMIdentifier.DEFAULT_NAMESPACE) {
					toQuote.append(identifier.getNameSpace());
					toQuote.append(NAMESPACE_LOCALNAME_SEPARATOR);
				}
				toQuote.append(identifier.getLocalName());
			}

			@Override
			public void visit(final NameAlias alias) {
				toQuote.append(alias.getName());
			}
		});

		return IdentQuoter.quote(toQuote.toString());
	}

}
