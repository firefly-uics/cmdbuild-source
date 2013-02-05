package org.cmdbuild.dao.driver.postgres.quote;

import static org.cmdbuild.dao.driver.postgres.Const.DOMAIN_PREFIX;

import org.cmdbuild.dao.driver.postgres.Const;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMFunctionCall;

public class EntryTypeHistoryQuoter implements Quoter, CMEntryTypeVisitor {

	public static String quote(final CMEntryType type) {
		return new EntryTypeHistoryQuoter(type).quote();
	}

	private final CMEntryType entryType;
	private String quotedTypeName;

	public EntryTypeHistoryQuoter(final CMEntryType entryType) {
		this.entryType = entryType;
	}

	@Override
	public String quote() {
		entryType.accept(this);
		return quotedTypeName;
	}

	@Override
	public void visit(final CMClass entryType) {
		quotedTypeName = IdentQuoter.quote(entryType.getName() + Const.HISTORY_SUFFIX);
	}

	@Override
	public void visit(final CMDomain entryType) {
		quotedTypeName = IdentQuoter.quote(DOMAIN_PREFIX + entryType.getName() + Const.HISTORY_SUFFIX);
	}

	@Override
	public void visit(final CMFunctionCall entryType) {
		throw new UnsupportedOperationException("Cannot specify history for functions");
	}

}
