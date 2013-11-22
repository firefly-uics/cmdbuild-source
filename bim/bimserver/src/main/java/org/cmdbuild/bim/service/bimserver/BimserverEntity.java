package org.cmdbuild.bim.service.bimserver;

import java.util.ArrayList;
import java.util.List;

import org.bimserver.interfaces.objects.SDataObject;
import org.bimserver.interfaces.objects.SDataValue;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;

public class BimserverEntity implements Entity {

	private final SDataObject bimserverDataObject;

	protected BimserverEntity(final SDataObject object) {
		this.bimserverDataObject = object;
	}

	@Override
	public boolean isValid() {
		return (bimserverDataObject != null);
	}

	@Override
	public List<Attribute> getAttributes() {
		final List<SDataValue> values = bimserverDataObject.getValues();
		final List<Attribute> attributes = new ArrayList<Attribute>();
		for (final SDataValue datavalue : values) {
			final BimserverAttributeFactory attributeFactory = new BimserverAttributeFactory(datavalue);
			final Attribute attribute = attributeFactory.create();
			attributes.add(attribute);
		}
		return attributes;
	}

	@Override
	public Attribute getAttributeByName(final String attributeName) {
		Attribute attribute = Attribute.NULL_ATTRIBUTE;
		for (final Attribute attr : this.getAttributes()) {
			if (attr.getName().equals(attributeName)) {
				attribute = attr;
				break;
			}
		}
		return attribute;
	}

	@Override
	public String getKey() {
		return (bimserverDataObject.getGuid() != null && !bimserverDataObject.getGuid().isEmpty()) ? bimserverDataObject.getGuid() : String.valueOf(bimserverDataObject.getOid());
	}

	public Long getOid() {
		return bimserverDataObject.getOid();
	}

	@Override
	public String getTypeName() {
		return bimserverDataObject.getType();
	}

	@Override
	public String getContainerKey() {
		throw new BimError("Can not call getContainerKey on BimserverEntity class");
	}

	@Override
	public String toString() {
		return bimserverDataObject.getType() + " " + getKey();
	}

	@Override
	public String getGlobalId() {
		return bimserverDataObject.getGuid();
	}
}
