package org.cmdbuild.dao.entry;

import java.util.Map.Entry;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.joda.time.DateTime;

public class ForwardingEntry extends ForwardingValueSet implements CMEntry {

	private final CMEntry inner;

	public ForwardingEntry(final CMEntry inner) {
		super(inner);
		this.inner = inner;
	}

	@Override
	public CMEntryType getType() {
		return inner.getType();
	}

	@Override
	public Long getId() {
		return inner.getId();
	}

	@Override
	public String getUser() {
		return inner.getUser();
	}

	@Override
	public DateTime getBeginDate() {
		return inner.getBeginDate();
	}

	@Override
	public DateTime getEndDate() {
		return inner.getEndDate();
	}

	@Override
	public Iterable<Entry<String, Object>> getAllValues() {
		return inner.getAllValues();
	}

}
