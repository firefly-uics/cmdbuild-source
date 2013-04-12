package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.DEFAULT_VALUE;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.EDITOR_TYPE;
import static org.cmdbuild.servlets.json.ComunicationConstants.FIELD_MODE;
import static org.cmdbuild.servlets.json.ComunicationConstants.GROUP;
import static org.cmdbuild.servlets.json.ComunicationConstants.INHERITED;
import static org.cmdbuild.servlets.json.ComunicationConstants.LENGTH;
import static org.cmdbuild.servlets.json.ComunicationConstants.LOOKUP;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.NOT_NULL;
import static org.cmdbuild.servlets.json.ComunicationConstants.PRECISION;
import static org.cmdbuild.servlets.json.ComunicationConstants.SCALE;
import static org.cmdbuild.servlets.json.ComunicationConstants.SHOW_IN_GRID;
import static org.cmdbuild.servlets.json.ComunicationConstants.TYPE;
import static org.cmdbuild.servlets.json.ComunicationConstants.UNIQUE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.model.data.Metadata;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public class AttributeSerializer extends Serializer {

	public static enum JsonModeMapper {

		WRITE("write", Mode.WRITE), //
		READ("read", Mode.READ), //
		HIDDEN("hidden", Mode.HIDDEN), //
		;

		private final String text;
		private final Mode mode;

		private JsonModeMapper(final String text, final Mode mode) {
			this.text = text;
			this.mode = mode;
		}

		public static Mode modeFrom(final String text) {
			for (final JsonModeMapper mapper : values()) {
				if (mapper.text.equals(text)) {
					return mapper.mode;
				}
			}
			return Mode.WRITE;
		}

		public static String textFrom(final Mode mode) {
			for (final JsonModeMapper mapper : values()) {
				if (mapper.mode.equals(mode)) {
					return mapper.text;
				}
			}
			return WRITE.text;
		}

		public String getText() {
			return text;
		}
	}

	public JSONArray toClient(final Iterable<? extends CMAttribute> attributes, final boolean active)
			throws JSONException {
		final JSONArray attributeList = new JSONArray();
		for (final CMAttribute attribute : sortAttributes(attributes)) {
			if (active && !attribute.isActive()) {
				continue;
			}
			attributeList.put(toClient(attribute));
		}
		return attributeList;
	}

	/**
	 * we sort attributes on the class order and index number because Ext.JS
	 * DOES NOT ALLOW IT. Thanks Jack!
	 */
	private static Iterable<? extends CMAttribute> sortAttributes(final Iterable<? extends CMAttribute> attributes) {
		return new Ordering<CMAttribute>() {

			@Override
			public int compare(final CMAttribute left, final CMAttribute right) {
				if (left.getClassOrder() == right.getClassOrder()) {
					return (left.getIndex() - right.getIndex());
				} else {
					return (left.getClassOrder() - right.getClassOrder());
				}
			}

		}.immutableSortedCopy(attributes);
	}

	public JSONObject toClient(final CMAttribute attribute) throws JSONException {
		return toClient(attribute, Collections.<Metadata> emptyList());
	}

	public JSONObject toClient(final CMAttribute attribute, final Iterable<Metadata> metadata) throws JSONException {
		final Map<String, Object> attributes = new CMAttributeTypeVisitor() {

			private final Map<String, Object> attributes = Maps.newHashMap();

			@Override
			public void visit(final BooleanAttributeType attributeType) {
			}

			@Override
			public void visit(final CharAttributeType attributeType) {
			}

			@Override
			public void visit(final EntryTypeAttributeType attributeType) {
			}

			@Override
			public void visit(final DateTimeAttributeType attributeType) {
			}

			@Override
			public void visit(final DateAttributeType attributeType) {
			}

			@Override
			public void visit(final DecimalAttributeType attributeType) {
				attributes.put(PRECISION, attributeType.precision);
				attributes.put(SCALE, attributeType.scale);
			}

			@Override
			public void visit(final DoubleAttributeType attributeType) {
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				attributes.put("fkDestination", attributeType.getForeignKeyDestinationClassName());
			}

			@Override
			public void visit(final IntegerAttributeType attributeType) {
			}

			@Override
			public void visit(final IpAddressAttributeType attributeType) {
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				// Temporary solution to have single level lookup
				final String lookupTypeName = attributeType.getLookupTypeName();
				final JSONArray lookupChain = new JSONArray();
				lookupChain.put(lookupTypeName);
				attributes.put("lookupchain", lookupChain);
				attributes.put(LOOKUP, lookupTypeName);
				// // NdPaolo: PLEASE, LET ME REFACTOR THE LOOKUPS
				// LookupType lt = attribute.getLookupType();
				// JSONArray lookupChain = new JSONArray();
				// while (lt != null) {
				// if (lookupChain.length() == 0) {
				// jattr.put("lookup", lt.getType());
				// }
				// lookupChain.put(lt.getType());
				// lt = lt.getParentType();
				// }
				// jattr.put("lookupchain", lookupChain);
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				// ITable reftable = attribute.getReferenceTarget();
				// jattr.put("referencedClassName", reftable.getName());
				// jattr.put("referencedIdClass", reftable.getId());
				// jattr.put("fieldFilter", attribute.getFilter());
				// jattr.put("domainDirection", attribute.isReferenceDirect());
				// jattr.put("idDomain",
				// attribute.getReferenceDomain().getId());

				final String domainName = attributeType.getDomainName();
				final CMDomain domain = view.findDomain(domainName);
				if (domain == null) {
					throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(domainName);
				}
				final CMEntryType owner = attribute.getOwner();
				final CMClass target = domain.getClass1().getIdentifier().getLocalName()
						.equals(owner.getIdentifier().getLocalName()) ? domain.getClass2() : domain.getClass1();

				attributes.put("idClass", target.getId());
				attributes.put("referencedClassName", target.getIdentifier().getLocalName());
			}

			@Override
			public void visit(final StringAttributeType attributeType) {
				attributes.put(LENGTH, attributeType.length);
			}

			@Override
			public void visit(final TextAttributeType attributeType) {
				attributes.put(EDITOR_TYPE, attribute.getEditorType());
			}

			@Override
			public void visit(final TimeAttributeType attributeType) {
			}

			public Map<String, Object> fill(final CMAttribute attribute) {
				/*
				 * type specific
				 */
				attribute.getType().accept(this);

				/*
				 * common
				 */
				attributes.put(NAME, attribute.getName());
				attributes.put(DESCRIPTION, attribute.getDescription());
				attributes.put(TYPE,
						new JsonDashboardDTO.JsonDataSourceParameter.TypeConverter(attribute.getType()).getTypeName());
				attributes.put(SHOW_IN_GRID, attribute.isDisplayableInList());
				attributes.put(UNIQUE, attribute.isUnique());
				attributes.put(NOT_NULL, attribute.isMandatory());
				attributes.put(INHERITED, attribute.isInherited());
				attributes.put(ACTIVE, attribute.isActive());
				attributes.put(FIELD_MODE, JsonModeMapper.textFrom(attribute.getMode()));
				attributes.put("index", attribute.getIndex()); // TODO: constant
				attributes.put(DEFAULT_VALUE, attribute.getDefaultValue());
				attributes.put(GROUP, attribute.getGroup() == null ? "" : attribute.getGroup());

				final Map<String, String> metadataMap = Maps.newHashMap();
				for (final Metadata element : metadata) {
					metadataMap.put(element.name, element.value);
				}
				attributes.put("meta", metadata);

				int absoluteClassOrder = attribute.getClassOrder();
				int classOrderSign;
				if (absoluteClassOrder == 0) {
					classOrderSign = 0;
					// to manage the sorting in the AttributeGridForSorting
					absoluteClassOrder = 10000;
				} else if (absoluteClassOrder > 0) {
					classOrderSign = 1;
				} else {
					classOrderSign = -1;
					absoluteClassOrder *= -1;
				}
				attributes.put("classOrderSign", classOrderSign); // TODO
																	// constant
				attributes.put("absoluteClassOrder", absoluteClassOrder); // TODO
																			// constant
				return attributes;
			}

			@Override
			public void visit(final StringArrayAttributeType attributeType) {
			}

		}.fill(attribute);
		return attributesToJsonObject(attributes);
	}

	private static JSONObject attributesToJsonObject(final Map<String, Object> attributes) throws JSONException {
		final JSONObject jsonObject = new JSONObject();
		for (final Entry<String, Object> entry : attributes.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map<?, ?>) {
				@SuppressWarnings("unchecked")
				final Map<String, Object> nestedAttributes = (Map<String, Object>) value;
				value = attributesToJsonObject(nestedAttributes);
			}
			jsonObject.put(entry.getKey(), value);
		}
		return jsonObject;
	}

	public static AttributeSerializer of(final CMDataView view) {
		return new AttributeSerializer(view);
	}

	private final CMDataView view;

	private AttributeSerializer(final CMDataView view) {
		this.view = view;
	}

	// FIXME: replace List<CMAttributeType<?>> with List<String> with attribute
	// types names
	public static JSONArray toClient(final List<CMAttributeType<?>> types) throws JSONException {
		final JSONArray out = new JSONArray();
		for (final CMAttributeType<?> type : types) {
			final JSONObject jsonType = new CMAttributeTypeVisitor() {

				@Override
				public void visit(final TimeAttributeType attributeType) {
					put("name", "TIME");
					put("value", "TIME");
				}

				@Override
				public void visit(final TextAttributeType attributeType) {
					put("name", "TEXT");
					put("value", "TEXT");
				}

				@Override
				public void visit(final StringAttributeType attributeType) {
					put("name", "STRING");
					put("value", "STRING");
				}

				@Override
				public void visit(final StringArrayAttributeType attributeType) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void visit(final ReferenceAttributeType attributeType) {
					put("name", "REFERENCE");
					put("value", "REFERENCE");
				}

				@Override
				public void visit(final LookupAttributeType attributeType) {
					put("name", "LOOKUP");
					put("value", "LOOKUP");
				}

				@Override
				public void visit(final IpAddressAttributeType attributeType) {
					put("name", "INET");
					put("value", "INET");
				}

				@Override
				public void visit(final IntegerAttributeType attributeType) {
					put("name", "INTEGER");
					put("value", "INTEGER");
				}

				@Override
				public void visit(final ForeignKeyAttributeType attributeType) {
					put("name", "FOREIGNKEY");
					put("value", "FOREIGNKEY");
				}

				@Override
				public void visit(final EntryTypeAttributeType attributeType) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void visit(final DoubleAttributeType attributeType) {
					put("name", "DOUBLE");
					put("value", "DOUBLE");
				}

				@Override
				public void visit(final DecimalAttributeType attributeType) {
					put("name", "DECIMAL");
					put("value", "DECIMAL");
				}

				@Override
				public void visit(final DateTimeAttributeType attributeType) {
					put("name", "TIMESTAMP");
					put("value", "TIMESTAMP");
				}

				@Override
				public void visit(final DateAttributeType attributeType) {
					put("name", "DATE");
					put("value", "DATE");
				}

				@Override
				public void visit(final CharAttributeType attributeType) {
					put("name", "CHAR");
					put("value", "CHAR");
				}

				@Override
				public void visit(final BooleanAttributeType attributeType) {
					put("name", "BOOLEAN");
					put("value", "BOOLEAN");
				}

				private void put(final String key, final String value) {
					try {
						jsonType.put(key, value);
					} catch (final Exception e) {
						throw new Error(e);
					}
				}

				private JSONObject jsonType;

				public JSONObject jsonOf(final CMAttributeType<?> type) {
					jsonType = new JSONObject();
					type.accept(this);
					return jsonType;
				}

			}.jsonOf(type);
			out.put(jsonType);
		}

		return out;
	}

}
