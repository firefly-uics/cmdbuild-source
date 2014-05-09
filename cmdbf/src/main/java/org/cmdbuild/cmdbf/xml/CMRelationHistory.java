package org.cmdbuild.cmdbf.xml;

import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.ForwardingEntry;
import org.cmdbuild.dao.entrytype.CMDomain;

public class CMRelationHistory extends ForwardingEntry implements CMRelation {

	private final CMRelation delegate;
	private final CMDomainHistory type;

	public CMRelationHistory(CMRelation delegate) {
		super(delegate);
		this.delegate = delegate;
		this.type = new CMDomainHistory(delegate.getType());
	}
	
	@Override
	public CMDomain getType(){
		return type;
	}

	@Override
	public Long getCard1Id(){
		return delegate.getCard1Id();
	}

	@Override
	public Long getCard2Id(){
		return delegate.getCard2Id();
	}
}
