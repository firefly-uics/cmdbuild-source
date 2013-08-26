package org.cmdbuild.bim.service.bimserver;

import java.util.ArrayList;
import java.util.List;

import org.bimserver.interfaces.objects.SDataObject;
import org.bimserver.interfaces.objects.SDataValue;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;

public class BimserverEntity implements Entity {

	private final SDataObject object;

	public BimserverEntity(final SDataObject object) {
		this.object = object;
	}

	@Override
	public boolean isValid() {
		return (object != null);
	}

	@Override
	public List<Attribute> getAttributes() {
		final List<SDataValue> values = object.getValues();
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
		return (object.getGuid() != null) ? object.getGuid() : "";
	}

	public Long getOid() {
		return object.getOid();
	}

	@Override
	public String getTypeName() {
		return object.getType();
	}

	@Override
	public String getContainerKey() {
		throw new BimError("Can not call getContainerKey on BimserverEntity class");
	}
}
