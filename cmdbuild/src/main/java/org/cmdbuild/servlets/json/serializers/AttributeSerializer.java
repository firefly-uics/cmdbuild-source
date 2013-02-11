package org.cmdbuild.servlets.json.serializers;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
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
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.servlets.json.JSONBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public class AttributeSerializer extends Serializer{

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

	public static JSONArray toClient(final Iterable<? extends CMAttribute> attributes, final boolean active)
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

	public static JSONObject toClient(final CMAttribute attribute) throws JSONException {
		final JSONObject jsonObject = new JSONObject();

		final Map<String, Object> attributes = new CMAttributeTypeVisitor() {

			private final Map<String, Object> attributes = Maps.newHashMap();

			@Override
			public void visit(final BooleanAttributeType attributeType) {
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
				attributes.put(JSONBase.PARAMETER_PRECISION, attributeType.precision);
				attributes.put(JSONBase.PARAMETER_SCALE, attributeType.scale);
			}

			@Override
			public void visit(final DoubleAttributeType attributeType) {
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				// jattr.put("fkDestination",
				// attribute.getFKTargetClass().getId());
			}

			@Override
			public void visit(final GeometryAttributeType attributeType) {
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
				String lookupTypeName = attributeType.getLookupTypeName();
				JSONArray lookupChain = new JSONArray();
				lookupChain.put(lookupTypeName);
				attributes.put("lookupchain", lookupChain);
				attributes.put(JSONBase.PARAMETER_LOOKUP, lookupTypeName);
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
			}

			@Override
			public void visit(final StringAttributeType attributeType) {
				attributes.put(JSONBase.PARAMETER_LENGTH, attributeType.length);
			}

			@Override
			public void visit(final TextAttributeType attributeType) {
				attributes.put(JSONBase.PARAMETER_EDITOR_TYPE, attribute.getEditorType());
			}

			@Override
			public void visit(final TimeAttributeType attributeType) {
			}

			public Map<String, Object> fill(final CMAttribute attribute) {
				// type specific
				attribute.getType().accept(this);

				// commons
				attributes.put("idClass", attribute.getOwner().getId()); // TODO: constant
				attributes.put(JSONBase.PARAMETER_NAME, attribute.getName());
				attributes.put(JSONBase.PARAMETER_DESCRIPTION, attribute.getDescription());
				attributes.put(JSONBase.PARAMETER_TYPE,
						new JsonDashboardDTO.JsonDataSourceParameter.TypeConverter(attribute.getType()).getTypeName());
				attributes.put(JSONBase.PARAMETER_SHOW_IN_GRID, attribute.isDisplayableInList());
				attributes.put(JSONBase.PARAMETER_UNIQUE, attribute.isUnique());
				attributes.put(JSONBase.PARAMETER_NOT_NULL, attribute.isMandatory());
				attributes.put(JSONBase.PARAMETER_INHERITED, attribute.isInherited());
				attributes.put(JSONBase.PARAMETER_ACTIVE, attribute.isActive());
				attributes.put(JSONBase.PARAMETER_FIELD_MODE, JsonModeMapper.textFrom(attribute.getMode()));
				attributes.put("index", attribute.getIndex()); // TODO: constant
				attributes.put(JSONBase.PARAMETER_DEFAULT_VALUE, attribute.getDefaultValue());
				attributes.put(JSONBase.PARAMETER_GROUP, attribute.getGroup());
				// addMetadata(jattr, attribute);

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
				attributes.put("classOrderSign", classOrderSign); // TODO constant
				attributes.put("absoluteClassOrder", absoluteClassOrder); // TODO constant
				return attributes;
			}

		}.fill(attribute);
		for (final Entry<String, Object> entry : attributes.entrySet()) {
			jsonObject.put(entry.getKey(), entry.getValue());
		}
		return jsonObject;
	}


	/**
	 * @deprecated use serialize(CMAttribute) instead.
	 */
	@Deprecated
	public static JSONObject toClient(final IAttribute attribute) throws JSONException {
		final JSONObject jattr = new JSONObject();
		jattr.put("idClass", attribute.getSchema().getId());
		jattr.put(JSONBase.PARAMETER_NAME, attribute.getName());
		jattr.put(JSONBase.PARAMETER_DESCRIPTION, attribute.getDescription());
		jattr.put(JSONBase.PARAMETER_TYPE, attribute.getType());
		jattr.put(JSONBase.PARAMETER_SHOW_IN_GRID, attribute.isBaseDSP());
		jattr.put(JSONBase.PARAMETER_UNIQUE, attribute.isUnique());
		jattr.put(JSONBase.PARAMETER_NOT_NULL, attribute.isNotNull());
		jattr.put(JSONBase.PARAMETER_INHERITED, !attribute.isLocal());
		jattr.put("index", attribute.getIndex());
		jattr.put(JSONBase.PARAMETER_GROUP, attribute.getGroup());

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
		jattr.put("classOrderSign", classOrderSign);
		jattr.put("absoluteClassOrder", absoluteClassOrder);
		jattr.put(JSONBase.PARAMETER_LENGTH, attribute.getLength());
		jattr.put(JSONBase.PARAMETER_PRECISION, attribute.getPrecision());
		jattr.put(JSONBase.PARAMETER_SCALE, attribute.getScale());
		jattr.put(JSONBase.PARAMETER_DEFAULT_VALUE, attribute.getDefaultValue());
		jattr.put(JSONBase.PARAMETER_ACTIVE, attribute.getStatus().isActive());
		jattr.put(JSONBase.PARAMETER_FIELD_MODE, attribute.getFieldMode().getMode());
		jattr.put(JSONBase.PARAMETER_EDITOR_TYPE, attribute.getEditorType());
		switch (attribute.getType()) {
		case LOOKUP:
			// NdPaolo: PLEASE, LET ME REFACTOR THE LOOKUPS
			LookupType lt = attribute.getLookupType();
			final JSONArray lookupChain = new JSONArray();
			while (lt != null) {
				if (lookupChain.length() == 0) {
					jattr.put(JSONBase.PARAMETER_LOOKUP, lt.getType());
				}
				lookupChain.put(lt.getType());
				lt = lt.getParentType();
			}
			jattr.put("lookupchain", lookupChain);
			break;
		case REFERENCE: // FIXME: constant and porting in new Dao serialization
			final ITable reftable = attribute.getReferenceTarget();
			jattr.put("referencedClassName", reftable.getName());
			jattr.put("referencedIdClass", reftable.getId());
			jattr.put("fieldFilter", attribute.getFilter());
			jattr.put("domainDirection", attribute.isReferenceDirect());
			jattr.put("idDomain", attribute.getReferenceDomain().getId());
			break;

		case FOREIGNKEY:
			jattr.put(JSONBase.PARAMETER_FK_DESTINATION, attribute.getFKTargetClass().getId());
			break;
		}
		addMetadata(jattr, attribute);
		return jattr;
	}

	/**
	 * @deprecated use serialize(Iterable<CMAttribute>, boolean) instead.
	 */
	@Deprecated
	public static JSONArray serializeAttributeList(final BaseSchema table, final boolean active) throws JSONException {
		final List<IAttribute> sortedAttributes = sortAttributes(table.getAttributes().values());
		final JSONArray attributeList = new JSONArray();
		for (final IAttribute attribute : sortedAttributes) {
			if (attribute.getMode().equals(org.cmdbuild.elements.interfaces.BaseSchema.Mode.RESERVED))
				continue;
			if (active && !attribute.getStatus().isActive())
				continue;

			attributeList.put(AttributeSerializer.toClient(attribute));
		}
		return attributeList;
	}

	/*
	 * we sort attributes on the class order and index number because Ext.JS
	 * DOES NOT ALLOW IT. Thanks Jack!
	 */
	private static List<IAttribute> sortAttributes(final Collection<IAttribute> attributeCollection) {
		final List<IAttribute> sortedAttributes = new LinkedList<IAttribute>();
		sortedAttributes.addAll(attributeCollection);
		Collections.sort(sortedAttributes, new Comparator<IAttribute>() {
			@Override
			public int compare(final IAttribute a1, final IAttribute a2) {
				if (a1.getClassOrder() == a2.getClassOrder()) {
					return (a1.getIndex() - a2.getIndex());
				} else {
					return (a1.getClassOrder() - a2.getClassOrder());
				}
			}
		});
		return sortedAttributes;
	}

	public static JSONArray toClient(List<CMAttributeType<?>> types) throws JSONException {
		JSONArray out = new JSONArray();
		for (CMAttributeType<?> type: types) {
			final JSONObject jsonType = new JSONObject();
			jsonType.put("name", type.toString());
			jsonType.put("value", type.toString());

			out.put(jsonType);
		}

		return out;
	}
}
