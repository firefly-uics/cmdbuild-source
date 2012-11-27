package org.cmdbuild.services.meta;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.HashMap;
import java.util.Map;

import net.jcip.annotations.NotThreadSafe;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

// FIXME: it's not enough to synchronize the update
@NotThreadSafe
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

	private static final ITable metadataClass = UserOperations.from(UserContext.systemContext()).tables()
			.get(METADATA_CLASS_NAME);

	private static final Map<String, MetadataMap> metaMapCache = new HashMap<String, MetadataMap>();

	public static MetadataMap getMetadata() {
		return getMetadata(EMPTY, false);
	}

	public static Object getMetadata(final BaseSchema schema, final String name) {
		return getMetadata(schema).get(name);
	}

	public static MetadataMap getMetadata(final BaseSchema schema) {
		final String schemaFullName = getSchemaFullName(schema);
		return getMetadata(schemaFullName, true);
	}

	private static MetadataMap getMetadata(final String schemaFullName, final boolean cacheResults) {
		MetadataMap metaMap = metaMapCache.get(schemaFullName);
		if (metaMap == null) {
			metaMap = loadMetaMap(schemaFullName);
			if (cacheResults) {
				metaMapCache.put(schemaFullName, metaMap);
			}
		}
		return metaMap;
	}

	private static MetadataMap loadMetaMap(final String schemaFullName) {
		final MetadataMap metaMap = new MetadataMap();
		if (!METADATA_FULL_NAME.equals(schemaFullName)) { // skip metadata class
			CardQuery cardQuery = metadataClass.cards().list()
					.attributes(METADATA_KEY_ATTRIBUTE, METADATA_VALUE_ATTRIBUTE);
			if (isNotBlank(schemaFullName)) {
				cardQuery = cardQuery.filter(METADATA_SCHEMA_ATTRIBUTE, AttributeFilterType.EQUALS, schemaFullName);
			}
			for (final ICard metadataCard : cardQuery) {
				final String key = (String) metadataCard.getValue(METADATA_KEY_ATTRIBUTE);
				final String value = (String) metadataCard.getValue(METADATA_VALUE_ATTRIBUTE);
				metaMap.put(key, value);
			}
		}
		return metaMap;
	}

	public static String getSchemaFullName(final BaseSchema schema) {
		if (schema instanceof IAttribute) {
			final IAttribute attr = (IAttribute) schema;
			return String.format("%s.%s", attr.getSchema().getName(), attr.getName());
		} else {
			return schema.getName();
		}
	}

	public static synchronized void updateMetadata(final BaseSchema schema, final String name, final String newValue) {
		final String schemaFullName = getSchemaFullName(schema);
		final MetadataMap metaMap = getMetadata(schemaFullName, true);
		final String oldValue = (String) metaMap.get(name);

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

	private static ICard createMetaCard(final String schemaFullName, final String name) {
		final ICard metaCard = metadataClass.cards().create();
		metaCard.setValue(METADATA_SCHEMA_ATTRIBUTE, schemaFullName);
		metaCard.setValue(METADATA_KEY_ATTRIBUTE, name);
		return metaCard;
	}

	private static ICard getMetaCard(final String schemaFullName, final String name) {
		final ICard metaCard = metadataClass.cards().list()
				.filter(METADATA_SCHEMA_ATTRIBUTE, AttributeFilterType.EQUALS, schemaFullName)
				.filter(METADATA_KEY_ATTRIBUTE, AttributeFilterType.EQUALS, name).get(false);
		return metaCard;
	}

	public static void deleteMetadata(final BaseSchema schema, final String name) {
		final String schemaFullName = getSchemaFullName(schema);
		final MetadataMap metaMap = getMetadata(schemaFullName, true);
		getMetaCard(schemaFullName, name).delete();
		metaMap.remove(name);
	}

	/*
	 * Delete all metadata for schema
	 */
	public static void deleteMetadata(final BaseSchema schema) {
		final String schemaFullName = getSchemaFullName(schema);
		getDeleteMetaTemplate();
		metadataClass.cards().list().filter(METADATA_SCHEMA_ATTRIBUTE, AttributeFilterType.EQUALS, schemaFullName)
				.update(getDeleteMetaTemplate());
		for (final IAttribute attribute : schema.getAttributes().values()) {
			deleteMetadata(attribute);
		}
		metaMapCache.remove(schemaFullName);
	}

	private static ICard getDeleteMetaTemplate() {
		final ICard deleteMetaTemplate = metadataClass.cards().create();
		deleteMetaTemplate.setStatus(ElementStatus.INACTIVE);
		return deleteMetaTemplate;
	}
}
