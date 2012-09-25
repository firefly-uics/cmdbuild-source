package org.cmdbuild.dms.alfresco.webservice;

import static org.alfresco.webservice.util.Constants.ASPECT_VERSIONABLE;
import static org.alfresco.webservice.util.Constants.PROP_DESCRIPTION;
import static org.alfresco.webservice.util.Constants.PROP_TITLE;
import static org.alfresco.webservice.util.Constants.createQNameString;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.replace;
import static org.apache.commons.lang.StringUtils.startsWith;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSetRow;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.DocumentUpdate;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.SingleDocumentSearch;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.alfresco.AlfrescoInnerService;
import org.cmdbuild.dms.alfresco.StoredDocumentComparator;
import org.cmdbuild.dms.exception.DmsException;
import org.cmdbuild.dms.exception.WebserviceException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AlfrescoWsService extends AlfrescoInnerService {

	private static final Map<String, Map<String, String>> NO_ASPECTS_PROPERTIES = Collections.emptyMap();
	private static final Map<String, String> NO_ASPECT_PROPERTIES = Collections.emptyMap();

	private final String qnamePrefixForCustomAspects = createQNameString(configuration.getAlfrescoCustomUri(), EMPTY);

	private Iterable<DocumentTypeDefinition> cachedDocumentTypeDefinitions;

	public AlfrescoWsService(final DmsConfiguration configuration) {
		super(configuration);
	}

	@Override
	public List<StoredDocument> search(final DocumentSearch document) {
		final AlfrescoWebserviceClient wsClient = wsClient();
		final ResultSetRow[] resultSetRows = wsClient.search(document);
		final List<StoredDocument> storedDocuments = Lists.newArrayList();
		for (final ResultSetRow resultSetRow : resultSetRows) {
			storedDocuments.add(storedDocumentFrom(resultSetRow));
		}
		Collections.sort(storedDocuments, StoredDocumentComparator.INSTANCE);
		return storedDocuments;
	}

	private StoredDocument storedDocumentFrom(final ResultSetRow resultSetRow) {
		final AlfrescoWebserviceClient wsClient = wsClient();
		final StoredDocument storedDocument = new StoredDocument();
		final NamedValue[] namedValues = resultSetRow.getColumns();
		/*
		 * We cannot parse aspect properties before having "category" named
		 * value parsed. Since we are not sure of the order of the named values,
		 * we must iterate the first time on well-defined names and a second
		 * time looking for aspect properties.
		 */
		for (final NamedValue namedValue : namedValues) {
			final AlfrescoConstant alfrescoConstant = AlfrescoConstant.from(namedValue);
			alfrescoConstant.setInBean(storedDocument, namedValue, wsClient);
		}
		final String category = storedDocument.getCategory();
		if (isNotBlank(category)) {
			final Map<String, List<Metadata>> metadataListByGroup = guessMetadataGroups(category, namedValues);
			storedDocument.setMetadataGroups(metadataGroupsFrom(metadataListByGroup));
		}

		return storedDocument;
	}

	/**
	 * Needed because actually Alfresco API did not return which aspect a
	 * property ({@link NamedValue}) owns to. So we must guess using property
	 * name. That's why {@link DocumentTypeDefinition}s needs to be cached.
	 */
	private Map<String, List<Metadata>> guessMetadataGroups(final String category, final NamedValue[] namedValues) {
		final DocumentTypeDefinition documentTypeDefinition = documentTypeDefinitionFor(category);
		final Map<String, List<Metadata>> metadataListByGroup = Maps.newHashMap();
		for (final NamedValue namedValue : namedValues) {
			final AlfrescoConstant alfrescoConstant = AlfrescoConstant.from(namedValue);
			if ((alfrescoConstant == AlfrescoConstant.NULL) && canBeAspectProperty(namedValue)) {
				final String propertyName = propertyNameFrom(namedValue);
				for (final MetadataGroupDefinition metadataGroupDefinition : documentTypeDefinition
						.getMetadataGroupDefinitions()) {
					for (final MetadataDefinition metadataDefinition : metadataGroupDefinition.getMetadataDefinitions()) {
						if (metadataDefinition.getName().equals(propertyName)) {
							final String group = metadataGroupDefinition.getName();
							List<Metadata> metadataList = metadataListByGroup.get(group);
							if (metadataList == null) {
								metadataList = Lists.newArrayList();
								metadataListByGroup.put(group, metadataList);
							}
							metadataList.add(metadata(propertyName, namedValue.getValue()));
						}
					}
				}
			}
		}
		return metadataListByGroup;
	}

	private DocumentTypeDefinition documentTypeDefinitionFor(final String type) {
		for (final DocumentTypeDefinition documentTypeDefinition : getDocumentTypeDefinitions()) {
			if (documentTypeDefinition.getName().equals(type)) {
				return documentTypeDefinition;
			}
		}
		return documentTypeDefinitionWithNoMetadata(type);
	}

	// TODO put in an abstract factory
	private DocumentTypeDefinition documentTypeDefinitionWithNoMetadata(final String type) {
		return new DocumentTypeDefinition() {

			@Override
			public String getName() {
				return type;
			}

			@Override
			public Iterable<MetadataGroupDefinition> getMetadataGroupDefinitions() {
				return Collections.emptyList();
			}

		};
	}

	private boolean canBeAspectProperty(final NamedValue namedValue) {
		return startsWith(namedValue.getName(), qnamePrefixForCustomAspects);
	}

	private String propertyNameFrom(final NamedValue namedValue) {
		return replace(namedValue.getName(), qnamePrefixForCustomAspects, EMPTY);
	}

	private Metadata metadata(final String name, final String value) {
		return new Metadata() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getValue() {
				return value;
			}

		};
	}

	private Iterable<MetadataGroup> metadataGroupsFrom(final Map<String, List<Metadata>> metadataListByGroup) {
		final List<MetadataGroup> metadataGroups = Lists.newArrayList();
		for (final String group : metadataListByGroup.keySet()) {
			final List<Metadata> metadataList = metadataListByGroup.get(group);
			metadataGroups.add(new MetadataGroup() {

				@Override
				public String getName() {
					return group;
				}

				@Override
				public Iterable<Metadata> getMetadata() {
					return (metadataList == null) ? Collections.<Metadata> emptyList() : metadataList;
				}

			});
		}
		return metadataGroups;
	}

	@Override
	public void updateDescription(final DocumentUpdate document) throws DmsException {
		final AlfrescoWebserviceClient wsClient = wsClient();
		final List<StoredDocument> storedDocuments = search(new DocumentSearch() {

			@Override
			public String getClassName() {
				return document.getClassName();
			}

			@Override
			public int getCardId() {
				return document.getCardId();
			}

			@Override
			public List<String> getPath() {
				return document.getPath();
			}

		});

		for (final StoredDocument storedDocument : storedDocuments) {
			if (storedDocument.getName().equals(document.getFileName())) {
				final String uuid = storedDocument.getUuid();
				final Properties updatableProperties = new Properties();
				updatableProperties.setProperty(PROP_DESCRIPTION, document.getDescription());
				final boolean updated = wsClient.update(uuid, updatableProperties, NO_ASPECTS_PROPERTIES);
				if (!updated) {
					throw new WebserviceException();
				}
			}
		}
	}

	@Override
	public void updateProperties(final StorableDocument storableDocument) throws DmsException {
		final AlfrescoWebserviceClient wsClient = wsClient();
		final String uuid = getUuid(storableDocument);
		final boolean updated = wsClient.update( //
				uuid, //
				updatableProperties(storableDocument), //
				aspectsProperties(storableDocument));
		if (!updated) {
			final String message = String.format("error updating file '%s' for card '%s' with class '%s'",
					storableDocument.getFileName(), storableDocument.getCardId(), storableDocument.getClassName());
			logger.error(message);
		}
	}

	private static Properties updatableProperties(final StorableDocument storableDocument) {
		final Properties properties = new Properties();
		properties.setProperty(PROP_TITLE, storableDocument.getFileName());
		properties.setProperty(PROP_DESCRIPTION, storableDocument.getDescription());
		properties.setProperty(AlfrescoConstant.AUTHOR.getName(), storableDocument.getAuthor());
		return properties;
	}

	private Map<String, Map<String, String>> aspectsProperties(final StorableDocument storableDocument) {
		final Map<String, Map<String, String>> aspectsProperties = Maps.newHashMap();
		aspectsProperties.put(ASPECT_VERSIONABLE, NO_ASPECT_PROPERTIES);
		for (final MetadataGroup metadataGroup : storableDocument.getMetadataGroups()) {
			aspectsProperties.put(aspectNameFrom(metadataGroup), aspectPropertiesFrom(metadataGroup));
		}
		return aspectsProperties;
	}

	private String aspectNameFrom(final MetadataGroup metadataGroup) {
		return createQNameString(configuration.getAlfrescoCustomUri(), metadataGroup.getName());
	}

	private Map<String, String> aspectPropertiesFrom(final MetadataGroup metadataGroup) {
		final Map<String, String> properties = Maps.newHashMap();
		for (final Metadata metadata : metadataGroup.getMetadata()) {
			properties.put(propertyNameFrom(metadata), metadata.getValue());
		}
		return properties;
	}

	private String propertyNameFrom(final Metadata metadata) {
		return createQNameString(configuration.getAlfrescoCustomUri(), metadata.getName());
	}

	@Override
	public void updateCategory(final StorableDocument storableDocument) throws DmsException {
		final AlfrescoWebserviceClient wsClient = wsClient();
		Reference categoryReference = wsClient.getCategoryReference(storableDocument.getCategory());
		if (categoryReference == null) {
			final boolean categoryCreated = wsClient.createCategory(storableDocument.getCategory());
			if (!categoryCreated) {
				final String message = String.format("error creating category '%s'", storableDocument.getCategory());
				throw new WebserviceException(message);
			}
			categoryReference = wsClient.getCategoryReference(storableDocument.getCategory());
			if (categoryReference == null) {
				final String message = String.format("error getting new category '%s'", storableDocument.getCategory());
				throw new WebserviceException(message);
			}
		}
		final String uuid = getUuid(storableDocument);
		wsClient.applyCategory(categoryReference, uuid);
	}

	private String getUuid(final StorableDocument storableDocument) throws DmsException {
		final AlfrescoWebserviceClient wsClient = wsClient();
		final ResultSetRow resultSetRow = wsClient.search(singleDocumentSearchFrom(storableDocument));
		final String uuid = resultSetRow.getNode().getId();
		return uuid;
	}

	private static SingleDocumentSearch singleDocumentSearchFrom(final StorableDocument storableDocument) {
		return new SingleDocumentSearch() {

			@Override
			public String getClassName() {
				return storableDocument.getClassName();
			}

			@Override
			public int getCardId() {
				return storableDocument.getCardId();
			}

			@Override
			public List<String> getPath() {
				return storableDocument.getPath();
			}

			@Override
			public String getFileName() {
				return storableDocument.getFileName();
			}

		};
	}

	@Override
	public Iterable<DocumentTypeDefinition> getDocumentTypeDefinitions() {
		synchronized (this) {
			if (cachedDocumentTypeDefinitions == null) {
				final AlfrescoWebserviceClient wsClient = wsClient();
				cachedDocumentTypeDefinitions = wsClient.getDocumentTypeDefinitions();
			}
		}
		return cachedDocumentTypeDefinitions;
	}

	@Override
	public void clearCache() {
		synchronized (this) {
			cachedDocumentTypeDefinitions = null;
		}
	}

	private AlfrescoWebserviceClient wsClient() {
		return AlfrescoWebserviceClient.getInstance(configuration);
	}

}
