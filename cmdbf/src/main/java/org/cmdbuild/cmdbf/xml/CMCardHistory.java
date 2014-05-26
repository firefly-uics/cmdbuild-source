package org.cmdbuild.cmdbf.xml;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.ForwardingCard;
import org.cmdbuild.dao.entrytype.CMClass;

public class CMCardHistory extends ForwardingCard {
	
	private final CMClassHistory type;

	public CMCardHistory(CMCard delegate) {
		super(delegate);
		this.type = new CMClassHistory(delegate.getType());
	}
	
	@Override
	public Long getId() {
		return getCurrentId();
	}
	
	@Override
	public CMClass getType() {
		return type;
	}
}
