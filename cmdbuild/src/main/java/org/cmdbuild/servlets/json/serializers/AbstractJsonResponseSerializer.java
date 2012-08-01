package org.cmdbuild.servlets.json.serializers;

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
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.operation.management.LookupOperation;
import org.cmdbuild.services.auth.UserContext;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractJsonResponseSerializer {

	@Deprecated
	// Needed because the new DAO does not fully support the lookups yet
	private LookupOperation systemLookupOperation = new LookupOperation(UserContext.systemContext());

	// TODO should be defined in the user session
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("dd/MM/yy HH:mm:ss");
	public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("HH:mm:ss");
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("dd/MM/yy");

	protected final String formatDateTime(final DateTime dateTime) {
		if (dateTime == null) {
			return null;
		} else {
			return DATE_TIME_FORMATTER.print(dateTime);
		}
	}

	protected Object javaToJsonValue(final CMAttributeType<?> type, final Object value) throws JSONException {
		return new CMAttributeTypeVisitor() {
			Object valueForJson;

			@Override
			public void visit(BooleanAttributeType attributeType) {
				valueForJson = value;
			}

			@Override
			public void visit(DateTimeAttributeType attributeType) {
				if (value != null) {
					valueForJson = DATE_TIME_FORMATTER.print((DateTime) value);
				}
			}

			@Override
			public void visit(DateAttributeType attributeType) {
				if (value != null) {
					valueForJson = DATE_FORMATTER.print((DateTime) value);
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
					final Lookup oldLookup = systemLookupOperation.getLookupById(lookup.getId().intValue());
					try {
						valueForJson = idAndDescription(Long.valueOf(oldLookup.getId()), oldLookup.getDescription());
					} catch (JSONException e) {
						valueForJson = null;
					}
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

			@Override
			public void visit(TimeAttributeType attributeType) {
				valueForJson = TIME_FORMATTER.print((DateTime) value);
			}

			Object valueForJson() {
				type.accept(this);
				return valueForJson;
			}
		}.valueForJson();
	}

	private JSONObject idAndDescription(final Long id, final String description) throws JSONException {
		final JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", id);
		jsonObject.put("dsc", description);
		return jsonObject;
	}
}
