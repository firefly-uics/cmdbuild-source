package org.cmdbuild.elements.history;

import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.CMBackend;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.proxy.TableForwarder;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class TableHistory extends TableForwarder {

	private static final CMBackend backend = new CMBackend();

	public static final String EndDateAttribute = "EndDate"; 
	public static final String HistoryTableSuffix = "_history";

	Map<String, IAttribute> fakeAttributes;

	public TableHistory(ITable t) {
		super(t);
	}

	public String getDBName() {
		return t.getDBName()+HistoryTableSuffix;
	}

	// Temporary fix: the whole attributes mechanism should be redesigned
	public Map<String, IAttribute> getAttributes() {
		if (fakeAttributes == null) {
			fakeAttributes = backend.findAttributes(t);
			for (IAttribute a : fakeAttributes.values()) {
				a.setSchema(this);
			}
			IAttribute endDateAttr = AttributeImpl.create(this, EndDateAttribute, AttributeType.TIMESTAMP);
			fakeAttributes.put(EndDateAttribute, endDateAttr);
		}
		return fakeAttributes;
	}

	public IAttribute getAttribute(String name) throws NotFoundException {
		return getAttributes().get(name);
	}
	
	public void save() throws ORMException {
		throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
	}
}
