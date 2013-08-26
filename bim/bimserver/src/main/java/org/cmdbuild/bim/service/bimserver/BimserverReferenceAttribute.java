package org.cmdbuild.bim.service.bimserver;

import org.bimserver.interfaces.objects.SReferenceDataValue;
import org.cmdbuild.bim.service.ReferenceAttribute;

public class BimserverReferenceAttribute extends BimserverAttribute implements ReferenceAttribute {

	public BimserverReferenceAttribute(final SReferenceDataValue value) {
		super(value);
	}

	@Override
	public String getGuid() {
		final SReferenceDataValue referencedatavalue = (SReferenceDataValue) getDatavalue();
		return referencedatavalue.getGuid();
	}

	@Override
	public long getOid() {
		final SReferenceDataValue referencedatavalue = (SReferenceDataValue) getDatavalue();
		return referencedatavalue.getOid();
	}

	@Override
	public String getTypeName() {
		final SReferenceDataValue referencedatavalue = (SReferenceDataValue) getDatavalue();
		return referencedatavalue.getTypeName();
	}

}
