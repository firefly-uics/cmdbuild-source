package org.cmdbuild.services.meta;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.services.auth.UserContext;

public class MetadataService {

	public static final String RUNTIME_PREFIX = "runtime";
	public static final String RUNTIME_PRIVILEGES_KEY = RUNTIME_PREFIX + ".privileges";
	public static final String RUNTIME_USERNAME_KEY = RUNTIME_PREFIX + ".username";
	public static final String RUNTIME_DEFAULTGROUPNAME_KEY = RUNTIME_PREFIX + ".groupname";
	public static final String RUNTIME_PROCESS_ISSTOPPABLE = RUNTIME_PREFIX + ".processstoppable";
	public static final String SYSTEM_PREFIX = "system";
	public static final String SYSTEM_TEMPLATE_PREFIX = SYSTEM_PREFIX + ".template";

	public static final String METADATA_CLASS_NAME = "Metadata";
	public static final String METADATA_FULL_NAME = METADATA_CLASS_NAME;

	public static final String METADATA_SCHEMA_ATTRIBUTE = ICard.CardAttributes.Code.toString();
	public static final String METADATA_KEY_ATTRIBUTE = ICard.CardAttributes.Description.toString();
	public static final String METADATA_VALUE_ATTRIBUTE = ICard.CardAttributes.Notes.toString();

	private static final ITable metadataClass = UserContext.systemContext().tables().get(METADATA_CLASS_NAME);

	private static final Map<String, MetadataMap> metaMapCache = new HashMap<String, MetadataMap>();

	public static MetadataMap getMetadata() {
		return getMetadata(EMPTY, false);
	}

	public static MetadataMap getMetadata(BaseSchema schema) {
		String schemaFullName = getSchemaFullName(schema);
		return getMetadata(schemaFullName, true);
	}

	private static MetadataMap getMetadata(String schemaFullName, boolean cacheResults) {
		MetadataMap metaMap = metaMapCache.get(schemaFullName);
		if (metaMap == null) {
			metaMap = loadMetaMap(schemaFullName);
			if (cacheResults) {
				metaMapCache.put(schemaFullName, metaMap);
			}
		}
		return metaMap;
	}

	private static MetadataMap loadMetaMap(String schemaFullName) {
		MetadataMap metaMap = new MetadataMap();
		if (!METADATA_FULL_NAME.equals(schemaFullName)) { // skip metadata class
			CardQuery cardQuery = metadataClass.cards().list().attributes(METADATA_KEY_ATTRIBUTE,
					METADATA_VALUE_ATTRIBUTE);
			if (isNotBlank(schemaFullName)) {
				cardQuery = cardQuery.filter(METADATA_SCHEMA_ATTRIBUTE, AttributeFilterType.EQUALS, schemaFullName);
			}
			for (ICard metadataCard : cardQuery) {
				String key = (String) metadataCard.getValue(METADATA_KEY_ATTRIBUTE);
				String value = (String) metadataCard.getValue(METADATA_VALUE_ATTRIBUTE);
				metaMap.put(key, value);
			}
		}
		return metaMap;
	}

	public static String getSchemaFullName(BaseSchema schema) {
		if (schema instanceof IAttribute) {
			IAttribute attr = (IAttribute) schema;
			return String.format("%s.%s", attr.getSchema().getName(), attr.getName());
		} else {
			return schema.getName();
		}
	}

	public static synchronized void updateMetadata(BaseSchema schema, String name, String newValue) {
		String schemaFullName = getSchemaFullName(schema);
		MetadataMap metaMap = getMetadata(schemaFullName, true);
		String oldValue = (String) metaMap.get(name);

		if (newValue != null) {
			if (!newValue.equals(oldValue)) {
				ICard metaCard;
				if (oldValue == null) {
					metaCard = createMetaCard(schemaFullName, name);
				} else {
					metaCard = getMetaCard(schemaFullName, name);
				}
				metaCard.setValue(METADATA_VALUE_ATTRIBUTE, newValue);
				metaCard.save();
				metaMap.put(name, newValue);
			}
		} else {
			if (oldValue != null) {
				getMetaCard(schemaFullName, name).delete();
				metaMap.remove(name);
			}
		}
	}

	private static ICard createMetaCard(String schemaFullName, String name) {
		ICard metaCard = metadataClass.cards().create();
		metaCard.setValue(METADATA_SCHEMA_ATTRIBUTE, schemaFullName);
		metaCard.setValue(METADATA_KEY_ATTRIBUTE, name);
		return metaCard;
	}

	private static ICard getMetaCard(String schemaFullName, String name) {
		ICard metaCard = metadataClass.cards().list().filter(METADATA_SCHEMA_ATTRIBUTE, AttributeFilterType.EQUALS,
				schemaFullName).filter(METADATA_KEY_ATTRIBUTE, AttributeFilterType.EQUALS, name).get(false);
		return metaCard;
	}

	public static void deleteMetadata(BaseSchema schema, String name) {
		String schemaFullName = getSchemaFullName(schema);
		MetadataMap metaMap = getMetadata(schemaFullName, true);
		getMetaCard(schemaFullName, name).delete();
		metaMap.remove(name);
	}

	/*
	 * Delete all metadata for schema
	 */
	public static void deleteMetadata(BaseSchema schema) {
		String schemaFullName = getSchemaFullName(schema);
		getDeleteMetaTemplate();
		metadataClass.cards().list().filter(METADATA_SCHEMA_ATTRIBUTE, AttributeFilterType.EQUALS, schemaFullName)
				.update(getDeleteMetaTemplate());
		for (IAttribute attribute : schema.getAttributes().values()) {
			deleteMetadata(attribute);
		}
		metaMapCache.remove(schemaFullName);
	}

	private static ICard getDeleteMetaTemplate() {
		ICard deleteMetaTemplate = metadataClass.cards().create();
		deleteMetaTemplate.setStatus(ElementStatus.INACTIVE);
		return deleteMetaTemplate;
	}
}
