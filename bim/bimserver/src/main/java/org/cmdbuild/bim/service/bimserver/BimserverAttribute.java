package org.cmdbuild.bim.service.bimserver;

import org.bimserver.interfaces.objects.SDataValue;
import org.bimserver.interfaces.objects.SSimpleDataValue;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.service.BimError;

public abstract class BimserverAttribute implements Attribute {

	private final SDataValue datavalue;

	// protected BimserverAttribute() {
	// datavalue = null;
	// };

	public BimserverAttribute(final SDataValue datavalue) {
		this.datavalue = datavalue;
	}

	protected SDataValue getDatavalue() {
		return datavalue;
	}

	@Override
	public String getName() {
		return datavalue.getFieldName();
	}

	@Override
	public boolean isValid() {
		return datavalue != null;
	}

	@Override
	public String getValue() {
		String value = "";
		if (datavalue instanceof SSimpleDataValue) {
			value = ((SSimpleDataValue) datavalue).getStringValue();
		}
		return value;
	}
	
	@Override
	public void setValue(String value){
		throw new BimError("Method not allowed for " + this.getClass().getCanonicalName());
	}
}
