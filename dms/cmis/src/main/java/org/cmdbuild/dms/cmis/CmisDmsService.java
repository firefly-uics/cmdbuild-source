package org.cmdbuild.dms.cmis;

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.base.Suppliers.synchronizedSupplier;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.chemistry.opencmis.commons.PropertyIds.DESCRIPTION;
import static org.apache.chemistry.opencmis.commons.PropertyIds.NAME;
import static org.apache.chemistry.opencmis.commons.PropertyIds.OBJECT_TYPE_ID;
import static org.apache.chemistry.opencmis.commons.PropertyIds.SECONDARY_OBJECT_TYPE_IDS;
import static org.apache.chemistry.opencmis.commons.SessionParameter.ATOMPUB_URL;
import static org.apache.chemistry.opencmis.commons.SessionParameter.AUTH_HTTP_BASIC;
import static org.apache.chemistry.opencmis.commons.SessionParameter.BINDING_TYPE;
import static org.apache.chemistry.opencmis.commons.SessionParameter.CONNECT_TIMEOUT;
import static org.apache.chemistry.opencmis.commons.SessionParameter.PASSWORD;
import static org.apache.chemistry.opencmis.commons.SessionParameter.READ_TIMEOUT;
import static org.apache.chemistry.opencmis.commons.SessionParameter.USER;
import static org.apache.chemistry.opencmis.commons.enums.CmisVersion.CMIS_1_0;
import static org.apache.chemistry.opencmis.commons.enums.UnfileObject.DELETE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.dms.MetadataAutocompletion.NULL_AUTOCOMPLETION_RULES;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.MimetypesFileTypeMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.cmdbuild.dms.DefaultDefinitionsFactory;
import org.cmdbuild.dms.DefinitionsFactory;
import org.cmdbuild.dms.DmsConfiguration.ChangeListener;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DmsService.LoggingSupport;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.DocumentUpdate;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataAutocompletion;
import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.cmis.Converter.Context;
import org.cmdbuild.dms.cmis.model.XmlConverter;
import org.cmdbuild.dms.cmis.model.XmlDocumentType;
import org.cmdbuild.dms.cmis.model.XmlMetadata;
import org.cmdbuild.dms.cmis.model.XmlMetadataGroup;
import org.cmdbuild.dms.cmis.model.XmlModel;
import org.cmdbuild.dms.cmis.model.XmlParameter;
import org.cmdbuild.dms.cmis.model.XmlPresets;
import org.cmdbuild.dms.exception.DmsError;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Supplier;

public class CmisDmsService implements DmsService, LoggingSupport, ChangeListener {

	private static final Marker MARKER = MarkerFactory.getMarker(CmisDmsService.class.getName());

	private static final String CMIS_DOCUMENT = "cmis:document";
	private static final String CMIS_FOLDER = "cmis:folder";

	private final CmisDmsConfiguration configuration;
	private DefinitionsFactory definitionsFactory;
	private Supplier<XmlModel> customModel;
	private Converter defaultConverter;
	private Map<String, Converter> converters;
	private Supplier<Collection<ObjectType>> types;
	private Supplier<Map<String, PropertyDefinition<?>>> propertyDefinitions;
	private Supplier<Map<String, CmisDocumentType>> documentTypeDefinitions;
	private Supplier<Repository> repository;
	private final CategoryLookupConverter categoryLookupConverter;

	public CmisDmsService(final CmisDmsConfiguration configuration,
			final CategoryLookupConverter categoryLookupConverter) {
		this.configuration = configuration;
		this.configuration.addListener(this);
		this.categoryLookupConverter = categoryLookupConverter;
		initialize();
	}

	private XmlModel model() {
		return customModel.get();
	}

	private Collection<ObjectType> types() {
		return types.get();
	}

	private Map<String, PropertyDefinition<?>> propertyDefinitions() {
		return propertyDefinitions.get();
	}

	private void initialize() {
		repository = synchronizedSupplier(memoize(new Supplier<Repository>() {

			@Override
			public Repository get() {
				logger.info(MARKER, "initializing repository");
				final SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
				final Map<String, String> parameters = newHashMap();
				parameters.put(ATOMPUB_URL, configuration.getServerURL());
				parameters.put(BINDING_TYPE, BindingType.ATOMPUB.value());
				parameters.put(AUTH_HTTP_BASIC, "true");
				parameters.put(USER, configuration.getAlfrescoUser());
				parameters.put(PASSWORD, configuration.getAlfrescoPassword());
				parameters.put(CONNECT_TIMEOUT, Integer.toString(10000));
				parameters.put(READ_TIMEOUT, Integer.toString(30000));
				if (model().getSessionParameters() != null) {
					for (final XmlParameter param : model().getSessionParameters()) {
						parameters.put(param.getName(), param.getValue());
					}
				}
				logger.info(MARKER, "parameters for repository '{}'", parameters);
				final List<Repository> repositories = sessionFactory.getRepositories(parameters);
				logger.info(MARKER, "got a repository list with length '{}'", repositories);
				final Repository repository = repositories.get(0);
				logger.info(MARKER, "will use repository with name '{}'", repository);
				return repository;
			}

		}));
		customModel = synchronizedSupplier(memoize(new Supplier<XmlModel>() {

			@Override
			public XmlModel get() {
				try {
					logger.info(MARKER, "loading model");
					final String modelType = defaultString(configuration.getCmisModelType());
					final XmlModel model;
					switch (modelType) {
					case "custom":
						logger.info(MARKER, "loading custom model");
						final String content = configuration.getCustomModelFileContent();
						model = content.isEmpty() ? new XmlModel() : unmarshal(source(content), XmlModel.class);
						break;

					default:
						logger.info(MARKER, "loading preset '{}'", modelType);
						final InputStream is = getClass().getClassLoader()
								.getResourceAsStream("org/cmdbuild/dms/cmis/model/presets.xml");
						final XmlPresets value = unmarshal(source(is), XmlPresets.class);
						model = value.getModels().stream() //
								.filter(t -> t.getId().equals(modelType)) //
								.findFirst() //
								.orElse(new XmlModel());
						break;
					}
					return model;
				} catch (final Exception e) {
					logger.error(MARKER, "error loading  model", e);
					return new XmlModel();
				}
			}

			private <T> T unmarshal(final Source source, final Class<T> type) throws JAXBException {
				return JAXBContext.newInstance(type) //
						.createUnmarshaller() //
						.unmarshal(source, type) //
						.getValue();
			}

			private Source source(final String value) {
				return new StreamSource(new StringReader(value));
			}

			private Source source(final InputStream value) {
				return new StreamSource(value);
			}

		}));
		definitionsFactory = new DefaultDefinitionsFactory();
		defaultConverter = new DefaultConverter();
		types = synchronizedSupplier(memoize(new Supplier<Collection<ObjectType>>() {

			@Override
			public Collection<ObjectType> get() {
				final List<ObjectType> output = newArrayList();
				final Session session = createSession();
				if (model().getCmisType() != null) {
					final ObjectType type = session.getTypeDefinition(model().getCmisType());
					if (type != null && type.getPropertyDefinitions() != null) {
						logger.info(MARKER, "storing CMIS type definition '{}'", type.getDisplayName());
						output.add(type);
					}
					if (model().getSecondaryTypeList() != null) {
						for (final String element : model().getSecondaryTypeList()) {
							logger.info(MARKER, "storing secondary CMIS type definition '{}'", element);
							final ObjectType secondaryType = session.getTypeDefinition(element);
							if (secondaryType != null && secondaryType.getPropertyDefinitions() != null) {
								output.add(secondaryType);
							}
						}
					}
				}
				return output;
			}

		}));
		propertyDefinitions = synchronizedSupplier(memoize(new Supplier<Map<String, PropertyDefinition<?>>>() {

			@Override
			public Map<String, PropertyDefinition<?>> get() {
				final Map<String, PropertyDefinition<?>> output = newHashMap();
				for (final String name : from(
						asList(model().getCategory(), model().getAuthor(), model().getDescription()))
								// skips non-null elements
								.filter(String.class)) {
					PropertyDefinition<?> property = null;
					for (final ObjectType element : types.get()) {
						if (property == null) {
							property = element.getPropertyDefinitions().get(name);
						}
					}
					if (property != null) {
						output.put(name, property);
					}
				}
				return output;
			}

		}));
		documentTypeDefinitions = synchronizedSupplier(memoize(new Supplier<Map<String, CmisDocumentType>>() {

			private final Iterable<XmlDocumentType> EMPTY = emptyList();

			@Override
			public Map<String, CmisDocumentType> get() {
				final Map<String, CmisDocumentType> output = newHashMap();
				for (final XmlDocumentType documentType : defaultIfNull(model().getDocumentTypeList(), EMPTY)) {
					logger.info(MARKER, "processing document type '{}' defined in customModel", documentType.getName());
					final Collection<CmisMetadataGroupDefinition> cmisMetadataGroupDefinitions = newArrayList();
					for (final XmlMetadataGroup metadataGroup : documentType.getGroupList()) {
						final Collection<CmisMetadataDefinition> cmisMetadataDefinitions = newArrayList();
						ObjectType secondaryType = null;
						if (metadataGroup.getCmisSecondaryTypeId() != null) {
							logger.info(MARKER, "getting secondary type definition for '{}'",
									metadataGroup.getCmisSecondaryTypeId());
							secondaryType = createSession().getTypeDefinition(metadataGroup.getCmisSecondaryTypeId());
						}
						if (metadataGroup.getMetadataList() != null) {
							for (final XmlMetadata metadata : metadataGroup.getMetadataList()) {
								PropertyDefinition<?> property = null;
								if (secondaryType != null && secondaryType.getPropertyDefinitions() != null) {
									property = secondaryType.getPropertyDefinitions().get(metadata.getCmisPropertyId());
								}
								for (final ObjectType baseType : types()) {
									if (property == null) {
										property = baseType.getPropertyDefinitions().get(metadata.getCmisPropertyId());
									}
								}
								if (property != null) {
									final Converter converter = converterOf(property);
									final CmisMetadataDefinition cmisMetadata = new CmisMetadataDefinition(
											metadata.getName(), property, converter.getType(property));
									cmisMetadataDefinitions.add(cmisMetadata);
								}
							}
						} else if (secondaryType != null && secondaryType.getPropertyDefinitions() != null) {
							logger.info(MARKER,
									"Processing property definitions for " + secondaryType.getDisplayName());
							logger.info(MARKER,
									"alfrescocustomuri (configuration) is " + configuration.getAlfrescoCustomUri());
							for (final PropertyDefinition<?> property : secondaryType.getPropertyDefinitions()
									.values()) {
								final String localNamespace = property.getLocalNamespace();
								logger.info(MARKER,
										"Processing property : " + property.getDisplayName() + " ,namespace : "
												+ localNamespace + " ,localname " + property.getLocalName()
												+ " ,queryname " + property.getQueryName());
								if (property.getLocalNamespace().equals(configuration.getAlfrescoCustomUri())) {
									final Converter converter = converterOf(property);
									final CmisMetadataDefinition cmisMetadata = new CmisMetadataDefinition(
											property.getDisplayName(), property, converter.getType(property));
									cmisMetadataDefinitions.add(cmisMetadata);
								}
							}
						}
						if (cmisMetadataDefinitions != null) {
							final CmisMetadataGroupDefinition cmisGroup = new CmisMetadataGroupDefinition(
									metadataGroup.getName(), secondaryType, cmisMetadataDefinitions);
							cmisMetadataGroupDefinitions.add(cmisGroup);
						}
					}
					if (cmisMetadataGroupDefinitions != null) {
						final CmisDocumentType cmisDocumentType = new CmisDocumentType(documentType.getName(),
								cmisMetadataGroupDefinitions);
						output.put(cmisDocumentType.getName(), cmisDocumentType);
					}
				}
				return output;
			}

		}));
		converters = newHashMap();
		if (model().getConverterList() != null) {
			for (final XmlConverter converter : model().getConverterList()) {
				try {
					final Converter cmisConverter = (Converter) Class.forName(converter.getType()).newInstance();
					cmisConverter.setContext(new Context() {

						@Override
						public CmisDmsConfiguration getConfiguration() {
							return configuration;
						}

						@Override
						public CategoryLookupConverter getCategoryLookupConverter() {
							return categoryLookupConverter;
						}

					});
					for (final String propertyId : converter.getCmisPropertyId()) {
						logger.debug(MARKER,
								"Property converter for " + propertyId + cmisConverter.getClass().getName());
						converters.put(propertyId, cmisConverter);
					}
				} catch (final Exception e) {
					logger.error(MARKER, "Exception loading CMIS converter " + converter.getType(), e);
				}
			}
		}
	}

	@Override
	public void configurationChanged() {
		synchronized (this) {
			initialize();
		}
	}

	@Override
	public void clearCache() {
		synchronized (this) {
			initialize();
		}
	}

	@Override
	public AutocompletionRules getAutoCompletionRules() throws DmsError {
		try {
			final String content = configuration.getMetadataAutocompletionFileContent();
			final AutocompletionRules autocompletionRules;
			if (content != null && !content.isEmpty()) {
				final MetadataAutocompletion.Reader reader = new XmlAutocompletionReader(content);
				autocompletionRules = reader.read();
			} else {
				autocompletionRules = NULL_AUTOCOMPLETION_RULES;
			}
			return autocompletionRules;
		} catch (final Exception e) {
			throw DmsError.forward(e);
		}
	}

	@Override
	public Iterable<DocumentTypeDefinition> getTypeDefinitions() throws DmsError {
		return from(documentTypeDefinitions.get().values()) //
				.filter(DocumentTypeDefinition.class);
	}

	@Override
	public List<StoredDocument> search(final DocumentSearch position) throws DmsError {
		final Session session = createSession();
		logger.info(MARKER,
				"Searching from: " + position.getPath() + "" + position.getClassName() + position.getCardId() + "");
		final List<StoredDocument> results = newArrayList();
		final Folder folder = getFolder(session, position.getPath());
		logger.debug(MARKER, "In search : got a folder :" + folder);
		if (folder != null) {
			logger.debug(MARKER, "Got children of " + folder.getPath());
			for (final CmisObject child : folder.getChildren()) {
				logger.debug(MARKER, "got a child " + child.getName());

				if (child instanceof Document) {
					final Document cmisDocument = (Document) child;
					logger.debug(MARKER, "child is a cmisDocument " + cmisDocument.getDescription());

					logger.debug(MARKER, "getting paths for " + child.getName());
					String cmisPath = null;
					for (final String path : cmisDocument.getPaths()) {
						if (cmisPath == null) {
							cmisPath = path;
						} else if (!cmisPath.startsWith(folder.getPath()) && path.startsWith(folder.getPath())) {
							cmisPath = path;
						}
					}

					String category = null;
					if (model().getCategory() != null) {
						final Property<Object> property = cmisDocument.getProperty(model().getCategory());
						if (property != null) {
							category = converterOf(property.getDefinition()).convertFromCmisValue(session,
									property.getDefinition(), property.getValue());
						}
					}

					String author = null;
					if (model().getAuthor() != null) {
						final Property<Object> property = cmisDocument.getProperty(model().getAuthor());
						if (property != null) {
							author = converterOf(property.getDefinition()).convertFromCmisValue(session,
									property.getDefinition(), property.getValue());
						}
					}

					DocumentTypeDefinition documentTypeDefinition = null;
					logger.info(MARKER, "Category of searchd document is " + category);
					if (category != null) {
						documentTypeDefinition = documentTypeDefinitions.get().get(category);
					}
					if (documentTypeDefinition == null) {
						documentTypeDefinition = definitionsFactory.newDocumentTypeDefinitionWithNoMetadata(category);
					}

					final List<MetadataGroup> metadataGroups = newArrayList();
					for (final MetadataGroupDefinition metadataGroupDefinition : documentTypeDefinition
							.getMetadataGroupDefinitions()) {
						final List<Metadata> metadataList = newArrayList();
						for (final MetadataDefinition metadataDefinition : metadataGroupDefinition
								.getMetadataDefinitions()) {
							final CmisMetadataDefinition cmisMetadata = (CmisMetadataDefinition) metadataDefinition;
							final PropertyDefinition<?> propertyDefinition = cmisMetadata.getProperty();
							final Property<Object> property = cmisDocument.getProperty(propertyDefinition.getId());
							logger.info(MARKER, "processing property " + property);
							if (property != null && property.getValue() != null) {
								logger.info(MARKER, "Value of property " + property.getValue());
								final Converter converter = converterOf(propertyDefinition);
								final String value = converter.convertFromCmisValue(session, propertyDefinition,
										property.getValue());
								logger.info(MARKER, "After conversion Value of property " + value);
								metadataList.add(new CmisMetadata(cmisMetadata.getName(), value));
							}
						}
						metadataGroups.add(new CmisMetadataGroup(metadataGroupDefinition.getName(), metadataList));
					}

					final StoredDocument storedDocument = new StoredDocument();
					storedDocument.setPath(cmisPath.toString());
					storedDocument.setUuid(cmisDocument.getId());
					storedDocument.setName(cmisDocument.getName());
					storedDocument.setDescription(cmisDocument.getDescription());
					storedDocument.setVersion(cmisDocument.getVersionLabel());
					storedDocument.setCreated(cmisDocument.getCreationDate().getTime());
					storedDocument.setModified(cmisDocument.getLastModificationDate().getTime());
					storedDocument.setAuthor(author);
					storedDocument.setCategory(category);
					storedDocument.setMetadataGroups(metadataGroups);

					results.add(storedDocument);
				} else {
					logger.info(MARKER,
							"Child is not a document: " + child.getName() + " type: " + child.getClass().getName());
				}
			}
		}
		return results;
	}

	@Override
	public DataHandler download(final DocumentDownload document) throws DmsError {
		final Document cmisDocument = getDocument(createSession(), document.getPath(), document.getFileName());
		return (cmisDocument != null) ? new DataHandler(new ContentStreamAdapter(cmisDocument.getContentStream()))
				: null;
	}

	@Override
	public void upload(final StorableDocument document) throws DmsError {
		final Session session = createSession();
		final Folder folder = createFolder(session, document.getPath());
		if (folder != null) {
			final MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
			final String mimeType = mimeTypesMap.getContentType(document.getFileName());

			Document cmisDocument = getDocument(session, document.getPath(), document.getFileName());
			if (cmisDocument == null) {
				logger.info(MARKER, "create document");
				final ContentStream contentStream = session.getObjectFactory()
						.createContentStream(document.getFileName(), -1, mimeType, document.getInputStream());
				final Map<String, Object> properties = getProperties(session, document, null);
				cmisDocument = folder.createDocument(properties, contentStream, VersioningState.MAJOR);
				logger.info(MARKER,
						"Document created : " + cmisDocument + "secondary type " + cmisDocument.getSecondaryTypes());

			} else {
				logger.info(MARKER, "update document");
				final Document pwc = (Document) session.getObject(cmisDocument.checkOut());
				final ContentStream contentStream = session.getObjectFactory()
						.createContentStream(document.getFileName(), -1, mimeType, document.getInputStream());
				final Map<String, Object> properties = getProperties(session, document, pwc);
				try {
					pwc.checkIn(true, properties, contentStream, "");
				} catch (final Exception e) {
					pwc.cancelCheckOut();
					throw DmsError.forward(e);
				} finally {
					try {
						contentStream.getStream().close();
					} catch (final IOException e) {
						throw DmsError.forward(e);
					}
				}
			}
		}

	}

	@Override
	public void updateDescriptionAndMetadata(final DocumentUpdate document) throws DmsError {
		final Session session = createSession();
		final Folder folder = getFolder(session, document.getPath());
		if (folder != null) {
			final Document cmisDocument = getDocument(session, document.getPath(), document.getFileName());
			if (cmisDocument != null) {
				logger.info(MARKER, "Will update document: " + "path: " + cmisDocument.getPaths());
				logger.info(MARKER, "Will get properties for secondary type " + cmisDocument.getSecondaryTypes());
				final Map<String, Object> properties = getProperties(session, document, cmisDocument);
				cmisDocument.updateProperties(properties);
			}
		}
	}

	@Override
	public void delete(final DocumentDelete document) throws DmsError {
		logger.info(MARKER, "Delete dms document " + (document != null ? document.getFileName() : null));
		final Document cmisDocument = getDocument(createSession(), document.getPath(), document.getFileName());
		logger.info(MARKER, "Delete cmis document " + (cmisDocument != null ? document.getFileName() : null));
		if (cmisDocument != null) {
			logger.info(MARKER, "Delete cmis document " + cmisDocument.getName());
			cmisDocument.delete(true);
			logger.info(MARKER, "document deleted: " + cmisDocument.getName());
		} else {
			logger.error(MARKER,
					"No document to delete for " + "path: " + document.getPath() + "name: " + document.getFileName());
		}
	}

	@Override
	public void copy(final StoredDocument document, final DocumentSearch from, final DocumentSearch to)
			throws DmsError {
		final Session session = createSession();
		logger.info(MARKER, "Trying to get cmid document for cmdbuild doc :" + document.getName());
		final Document cmisDocument = getDocument(session, from.getPath(), document.getName());
		logger.debug(MARKER, "cmisDocument :" + cmisDocument);
		final Folder toFolder = createFolder(session, to.getPath());
		logger.debug(MARKER, "folder :" + toFolder);
		if (cmisDocument != null && toFolder != null) {
			logger.debug(MARKER, "folder path" + toFolder.getPath());
			final Map<String, Object> properties = newHashMap();
			for (final Property<?> property : cmisDocument.getProperties()) {
				if (property.getValue() != null) {
					final Converter cmisConverter = converters.get(property.getId());
					logger.info(MARKER, "Property converter for " + property.getLocalName() + " is " + cmisConverter);
					if (cmisConverter != null && cmisConverter.isAsymmetric()) {
						final String value = cmisConverter.convertFromCmisValue(session, property.getDefinition(),
								property.getValue());
						final Object cmisValue = cmisConverter.convertToCmisValue(session, property.getDefinition(),
								value);
						logger.debug(MARKER, "Setting property " + property.getId() + " to " + cmisValue);
						properties.put(property.getId(), cmisValue);
					}
				} else {
					logger.debug(MARKER, "Will not set property " + property.getLocalName() + " as its value is null");
				}
			}
			cmisDocument.copy(toFolder, properties, null, null, null, null, null);
		}
	}

	@Override
	public void move(final StoredDocument document, final DocumentSearch from, final DocumentSearch to)
			throws DmsError {
		final Session session = createSession();

		logger.info(MARKER, "Move document: " + document.getPath() + "|" + document.getName() + " from :"
				+ from.getPath() + " to " + to.getPath());
		final Folder fromFolder = getFolder(session, from.getPath());
		final Folder toFolder = createFolder(session, to.getPath());
		logger.info(MARKER, "Move from Folder: " + fromFolder + "to folder:" + toFolder);
		if (fromFolder != null && toFolder != null) {
			final Document cmisDocument = getDocument(session, from.getPath(), document.getName());
			if (cmisDocument != null) {
				cmisDocument.move(fromFolder, toFolder);
				logger.info(MARKER, "( Move ) document " + cmisDocument.getName() + "has moved from: "
						+ fromFolder.getName() + " to: " + toFolder.getName());
			} else {
				logger.warn(MARKER, "( Move ) unable to move, cmisdocument does not exists :" + document.getName()
						+ "in " + from.getPath());
			}
		} else {
			logger.warn(MARKER, "( Move ) Either from or to cmis folder is null" + "Cmis From: " + fromFolder
					+ "Cmis To:  " + toFolder);
		}
	}

	@Override
	public void delete(final DocumentSearch position) throws DmsError {
		final Folder folder = getFolder(createSession(), position.getPath());
		logger.info(MARKER, "will delete  tree '{}'", position.getPath());
		if (folder != null) {
			final Collection<String> results = folder.deleteTree(true, DELETE, true);
			for (final String result : results) {
				logger.debug(MARKER, "result " + result);
			}
		}
	}

	@Override
	public void create(final DocumentSearch position) throws DmsError {
		createFolder(createSession(), position.getPath());
	}

	private Session createSession() {
		return repository.get().createSession();
	}

	private Folder getFolder(final Session session, final List<String> pathList) {
		CmisObject object = null;
		if (pathList != null) {
			final StringBuilder path = new StringBuilder();
			path.append(configuration.getRepositoryWSPath());
			path.append(configuration.getRepositoryApp());
			for (final String name : pathList) {
				path.append("/");
				path.append(name);
			}
			try {
				object = session.getObjectByPath(path.toString());
			} catch (final CmisObjectNotFoundException e) {
				object = null;
			}
		}
		return object instanceof Folder ? (Folder) object : null;
	}

	private Folder createFolder(final Session session, final List<String> pathList) {
		final StringBuilder path = new StringBuilder();
		path.append(configuration.getRepositoryWSPath());
		path.append(configuration.getRepositoryApp());

		final CmisObject object = session.getObjectByPath(path.toString());
		if (object instanceof Folder && pathList != null) {
			Folder parentFolder = (Folder) object;
			for (final String name : pathList) {
				Folder folder = null;
				try {
					path.append('/');
					path.append(name);
					final CmisObject child = session.getObjectByPath(path.toString());
					if (child instanceof Folder) {
						folder = (Folder) child;
					}
				} catch (final CmisObjectNotFoundException e) {
					final Map<String, String> properties = newHashMap();
					properties.put(OBJECT_TYPE_ID, CMIS_FOLDER);
					properties.put(NAME, name);
					folder = parentFolder.createFolder(properties);
				}
				parentFolder = folder;
			}
			return parentFolder;
		} else {
			return null;
		}
	}

	private Document getDocument(final Session session, final List<String> pathList, final String filename) {
		CmisObject object = null;
		if (pathList != null && filename != null) {
			final StringBuilder path = new StringBuilder();
			path.append(configuration.getRepositoryWSPath());
			path.append(configuration.getRepositoryApp());
			for (final String name : pathList) {
				path.append("/");
				path.append(name);
			}
			path.append("/");
			path.append(filename);
			try {
				object = session.getObjectByPath(path.toString());
			} catch (final CmisObjectNotFoundException e) {
				object = null;
			}
		}
		return object instanceof Document ? (Document) object : null;
	}

	private Map<String, Object> getProperties(final Session session, final DocumentUpdate document,
			final Document cmisDocument) {
		final Map<String, Object> properties = newHashMap();

		final CmisVersion version = session.getRepositoryInfo().getCmisVersion();
		if (version.compareTo(CMIS_1_0) <= 0) {
			logger.warn(MARKER, "secondary types not supported by this protocol version ({})", version);
		} else {
			if (model().getAuthor() != null) {
				final PropertyDefinition<?> propertyDefinition = propertyDefinitions().get(model().getAuthor());
				if (propertyDefinition != null) {
					final Object author = converterOf(propertyDefinition).convertToCmisValue(session,
							propertyDefinition, document.getAuthor());
					properties.put(model().getAuthor(), author);
				}
			}

			properties.put(DESCRIPTION, document.getDescription());

			if (model().getDescription() != null) {
				final PropertyDefinition<?> propertyDefinition = propertyDefinitions().get(model().getDescription());
				logger.info(MARKER, "description property: " + propertyDefinition.getDisplayName() + " updatability "
						+ propertyDefinition.getUpdatability());
				if (propertyDefinition != null) {
					final Object value = converterOf(propertyDefinition).convertToCmisValue(session, propertyDefinition,
							document.getDescription());
					logger.info(MARKER,
							"converted property for : " + propertyDefinition.getDisplayName() + " value: " + value);
					properties.put(model().getDescription(), value);
				}
			}

			String category = document.getCategory();
			logger.info(MARKER, "Category of document  " + document.getFileName() + " is " + category);
			if (category != null) {
				logger.info(MARKER, "CustomModel for  " + category + " " + model().getCategory());
				if (model().getCategory() != null) {
					final PropertyDefinition<?> propertyDefinition = propertyDefinitions().get(model().getCategory());
					if (propertyDefinition != null) {
						final Object value = converterOf(propertyDefinition).convertToCmisValue(session,
								propertyDefinition, document.getCategory());
						properties.put(model().getCategory(), value);
					}
				}
			} else {
				if (model().getCategory() != null) {
					final Property<Object> property = cmisDocument.getProperty(model().getCategory());
					if (property != null) {
						category = converterOf(property.getDefinition()).convertFromCmisValue(session,
								property.getDefinition(), property.getValue());
					}
				}
			}

			if (category != null) {
				logger.info(MARKER, "Processing secondary types  for " + category);
				final List<Object> secondaryTypes = newArrayList();
				if (model().getSecondaryTypeList() != null) {
					logger.info(MARKER, "Secondary type list legth:  " + model().getSecondaryTypeList().size());
					for (final String secondaryType : model().getSecondaryTypeList()) {
						logger.info(MARKER, "Adding secondary types  " + secondaryType);
						secondaryTypes.add(secondaryType);
					}
				} else {
					logger.info(MARKER, "No Secondarytype list   in customModel ");
				}

				final CmisDocumentType documentType = documentTypeDefinitions.get().get(category);
				if (documentType != null) {
					for (final MetadataGroupDefinition group : documentType.getMetadataGroupDefinitions()) {
						final CmisMetadataGroupDefinition cmisGroup = (CmisMetadataGroupDefinition) group;
						if (cmisGroup.getSecondaryType() != null) {
							secondaryTypes.add(cmisGroup.getSecondaryType().getId());
							logger.info(MARKER,
									"Adding secondary types  from metadata" + cmisGroup.getSecondaryType().getId());
						}
					}

					if (document.getMetadataGroups() != null) {
						for (final MetadataGroup group : document.getMetadataGroups()) {
							logger.info(MARKER, "Processing group " + group.getName());
							final CmisMetadataGroupDefinition groupDefinition = documentType
									.getMetadataGroupDefinition(group.getName());
							if (groupDefinition != null && group.getMetadata() != null) {
								for (final Metadata metadata : group.getMetadata()) {
									final CmisMetadataDefinition metadataDefinition = groupDefinition
											.getMetadataDefinition(metadata.getName());
									if (metadataDefinition != null) {
										final PropertyDefinition<?> propertyDefinition = metadataDefinition
												.getProperty();
										final Converter converter = converterOf(propertyDefinition);
										final Object value = converter.convertToCmisValue(session, propertyDefinition,
												metadata.getValue());
										properties.put(metadataDefinition.getProperty().getId(), value);
									}
								}
							} else {
								logger.info(MARKER, "Either group definition or group.getMetadata() is null");
								logger.info(MARKER, " group definition " + groupDefinition + " group.getMetadata :"
										+ group.getMetadata());
							}
						}
					} else {
						logger.info(MARKER, "No group metadata for" + document.getFileName());
					}
				}
				logger.warn(MARKER, "CMISDOCUMENT " + cmisDocument);

				if (cmisDocument != null) {
					logger.info(MARKER, "cmisdocument : " + cmisDocument.getPaths());
					if (cmisDocument.getSecondaryTypes() != null) {
						for (final ObjectType secondaryType : cmisDocument.getSecondaryTypes()) {
							logger.warn(MARKER, "SECONDARY TYPE NAMESPACE" + secondaryType.getLocalNamespace());
							if (!secondaryType.getLocalNamespace().equals(configuration.getAlfrescoCustomUri())) {
								if (!secondaryTypes.contains(secondaryType.getId())) {
									secondaryTypes.add(secondaryType.getId());
								}
							}
						}
					}
				} else {
					logger.info(MARKER, "cmisdocument is null");
				}

				properties.put(SECONDARY_OBJECT_TYPE_IDS, secondaryTypes);
			}
		}

		if (cmisDocument == null) {
			properties.put(OBJECT_TYPE_ID, defaultIfNull(model().getCmisType(), CMIS_DOCUMENT));
			properties.put(NAME, document.getFileName());
		}

		if (logger.isDebugEnabled()) {
			for (final String key : properties.keySet()) {
				logger.debug(MARKER, "property to set '{}': '{}'", key, properties.get(key));
			}
		}

		return properties;
	}

	private Converter converterOf(final PropertyDefinition<?> property) {
		logger.debug(MARKER, "getting converter for '{}'", property.getDisplayName());
		return ofNullable(converters.get(property.getId())).orElse(defaultConverter);
	}

}
