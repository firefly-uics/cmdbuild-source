package org.cmdbuild.servlets.json.serializers;

import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;

import org.cmdbuild.dao.entry.CMLookup;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.GeometryAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IPAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.joda.time.DateTime;

public class JsonAttributeValueVisitor implements CMAttributeTypeVisitor {

	private Object value;
	private CMAttributeType<?> type;
	private Object valueForJson;

	public JsonAttributeValueVisitor(CMAttributeType<?> type, Object value) {
		this.value = value;
		this.type = type;
		this.valueForJson = null;
	}

	@Override
	public void visit(BooleanAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(DateTimeAttributeType attributeType) {
		if (value != null) {
			valueForJson = AbstractJsonResponseSerializer.DATE_TIME_FORMATTER.print((DateTime)value);
		}
	}

	@Override
	public void visit(DateAttributeType attributeType) {
		if (value != null) {
			valueForJson = AbstractJsonResponseSerializer.DATE_FORMATTER.print((DateTime)value);
		}
	}

	@Override
	public void visit(TimeAttributeType attributeType) {
		if (value != null) {
			valueForJson = AbstractJsonResponseSerializer.TIME_FORMATTER.print((DateTime)value);
		}
	}

	@Override
	public void visit(DecimalAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(DoubleAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(ForeignKeyAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(GeometryAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(IntegerAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(IPAddressAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(LookupAttributeType attributeType) {
		if (value instanceof CMLookup) {
			final CMLookup lookup = (CMLookup) value;

			valueForJson = new HashMap<String, Object>() {{
				put("id", lookup.getId());
				put("description", lookup.getDescription());
			}};

		} else {
			valueForJson = value;
		}
	}

	@Override
	public void visit(ReferenceAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(StringAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(TextAttributeType attributeType) {
		valueForJson = value;
	}

	public Object valueForJson() {
		type.accept(this);
		return valueForJson;
	}
}
