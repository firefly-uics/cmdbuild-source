package org.cmdbuild.servlets.json.serializers;

import static org.joda.time.format.DateTimeFormat.forPattern;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.GeometryAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.reference.CMReference;
import org.cmdbuild.dao.reference.EntryTypeReference;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.operation.management.LookupOperation;
import org.cmdbuild.services.auth.UserContext;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractJsonResponseSerializer {

	@Deprecated
	// Needed because the new DAO does not fully support the lookups yet
	private final LookupOperation systemLookupOperation = new LookupOperation(UserContext.systemContext());

	// TODO should be defined in the user session
	public static final DateTimeFormatter DATE_TIME_FORMATTER = forPattern(Constants.DATETIME_PRINTING_PATTERN);
	public static final DateTimeFormatter TIME_FORMATTER = forPattern(Constants.TIME_PRINTING_PATTERN);
	public static final DateTimeFormatter DATE_FORMATTER = forPattern(Constants.DATE_PRINTING_PATTERN);

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
			public void visit(final BooleanAttributeType attributeType) {
				valueForJson = value;
			}

			@Override
			public void visit(final EntryTypeAttributeType attributeType) {
				if (value instanceof EntryTypeReference) {
					valueForJson = ((EntryTypeReference) value).getId();
				} else {
					valueForJson = value;
				}
			}

			@Override
			public void visit(final DateTimeAttributeType attributeType) {
				if (value != null) {
					valueForJson = DATE_TIME_FORMATTER.print((DateTime) value);
				}
			}

			@Override
			public void visit(final DateAttributeType attributeType) {
				if (value != null) {
					valueForJson = DATE_FORMATTER.print((DateTime) value);
				}
			}

			@Override
			public void visit(final DecimalAttributeType attributeType) {
				valueForJson = value;
			}

			@Override
			public void visit(final DoubleAttributeType attributeType) {
				valueForJson = value;
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				valueForJson = value;
			}

			@Override
			public void visit(final GeometryAttributeType attributeType) {
				valueForJson = value;
			}

			@Override
			public void visit(final IntegerAttributeType attributeType) {
				valueForJson = value;
			}

			@Override
			public void visit(final IpAddressAttributeType attributeType) {
				valueForJson = value;
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				if (value instanceof CMReference) {
					// FIXME
					final Object id = ((CMReference) value).getId();
					final Lookup oldLookup = systemLookupOperation.getLookupById((Integer) id);
					try {
						valueForJson = idAndDescription(Long.valueOf(oldLookup.getId()), oldLookup.getDescription());
					} catch (final JSONException e) {
						valueForJson = null;
					}
				} else {
					valueForJson = value;
				}
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				if (value instanceof CMReference) {
					valueForJson = ((CMReference) value).getId();
				} else {
					valueForJson = value;
				}
			}

			@Override
			public void visit(final StringAttributeType attributeType) {
				valueForJson = value;
			}

			@Override
			public void visit(final TextAttributeType attributeType) {
				valueForJson = value;
			}

			@Override
			public void visit(final TimeAttributeType attributeType) {
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
