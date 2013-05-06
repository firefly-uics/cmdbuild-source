package org.cmdbuild.dao.driver.postgres.quote;

import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.AliasVisitor;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;

public class AliasQuoter implements Quoter {

	public static String quote(final Alias alias) {
		return new AliasQuoter(alias).quote();
	}

	private final Alias alias;

	public AliasQuoter(final Alias alias) {
		this.alias = alias;
	}

	@Override
	public String quote() {
		final StringBuilder quoted = new StringBuilder();
		alias.accept(new AliasVisitor() {

			@Override
			public void visit(final EntryTypeAlias alias) {
				quoted.append(EntryTypeQuoter.quote(alias.getEntryType()));
			}

			@Override
			public void visit(final NameAlias alias) {
				quoted.append(IdentQuoter.quote(alias.getName()));
			}
		});
		return quoted.toString();
	}

}
