package org.cmdbuild.dao.query.clause;

import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.ForwardingDomain;

public class DomainHistory extends ForwardingDomain implements HistoricEntryType<CMDomain> {

	public static CMDomain history(final CMDomain current) {
		return of(current);
	}

	public static CMDomain of(final CMDomain current) {
		return new DomainHistory(current);
	}

	private final CMDomain current;

	private DomainHistory(final CMDomain current) {
		super(UnsupportedProxyFactory.of(CMDomain.class).create());
		this.current = current;
	}

	@Override
	public void accept(final CMEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public CMDomain getType() {
		return current;
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

	public CMDomain getCurrent() {
		return current;
	}

}
