package org.cmdbuild.dao.entrytype;

public class ForwardingDomain extends ForwardingEntryType implements CMDomain {

	private final CMDomain inner;

	public ForwardingDomain(final CMDomain inner) {
		super(inner);
		this.inner = inner;
	}

	@Override
	public CMClass getClass1() {
		return inner.getClass1();
	}

	@Override
	public CMClass getClass2() {
		return inner.getClass2();
	}

	@Override
	public String getDescription1() {
		return inner.getDescription1();
	}

	@Override
	public String getDescription2() {
		return inner.getDescription2();
	}

	@Override
	public String getCardinality() {
		return inner.getCardinality();
	}

	@Override
	public boolean isMasterDetail() {
		return inner.isMasterDetail();
	}

	@Override
	public String getMasterDetailDescription() {
		return inner.getMasterDetailDescription();
	}

}
