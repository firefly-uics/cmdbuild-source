package org.cmdbuild.dms.cmis;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.activation.MimetypesFileTypeMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
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
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
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
import org.cmdbuild.dms.cmis.CmisCustomModel.Converter;
import org.cmdbuild.dms.cmis.CmisCustomModel.DocumentType;
import org.cmdbuild.dms.cmis.CmisCustomModel.Parameter;
import org.cmdbuild.dms.exception.DmsError;

public class CmisDmsService implements DmsService, LoggingSupport, ChangeListener {

	private static final AutocompletionRules NULL_AUTOCOMPLETION_RULES = new AutocompletionRules() {

		@Override
		public Iterable<String> getMetadataGroupNames() {
			return Collections.emptyList();
		}

		@Override
		public Iterable<String> getMetadataNamesForGroup(final String groupName) {
			return Collections.emptyList();
		}

		@Override
		public Map<String, String> getRulesForGroupAndMetadata(final String groupName, final String metadataName) {
			return Collections.emptyMap();
		}

	};

	private final CmisDmsConfiguration configuration;
	private DefinitionsFactory definitionsFactory;
	private CmisCustomModel customModel;
	private CmisConverter defaultConverter;
	private Map<String, CmisConverter> propertyConverters;
	private Map<String, PropertyDefinition<?>> cachedPropertyDefinitions;
	private Map<String, DocumentTypeDefinition> cachedDocumentTypeDefinitions;
	private Repository repository;

	public CmisDmsService(final CmisDmsConfiguration configuration) {
		this.configuration = configuration;
		this.configuration.addListener(this);
		configurationChanged();
	}

	@Override
	public void configurationChanged() {
		try {
			customModel = loadCustomModel();
		} catch (Exception e) {
			logger.error("Exception loading CMIS custom model " + configuration.getAlfrescoCustomModelFileName(), e);
		}
		definitionsFactory = new DefaultDefinitionsFactory();
		defaultConverter = new DefaultConverter();
		cachedPropertyDefinitions = null;
		cachedDocumentTypeDefinitions = null;

		propertyConverters = new HashMap<String, CmisConverter>();
		if (customModel != null && customModel.getConverterList() != null) {
			for (Converter converter : customModel.getConverterList()) {
				try {
					CmisConverter cmisConverter = (CmisConverter) Class.forName(converter.getType()).newInstance();
					cmisConverter.setConfiguration(configuration);
					for (String propertyId : converter.getCmisPropertyId()) {
						logger.debug("Property converter for " + propertyId + cmisConverter.getClass().getName());
						propertyConverters.put(propertyId, cmisConverter);
					}
				} catch (Exception e) {
					logger.error("Exception loading CMIS converter " + converter.getType(), e);
				}
			}
		}
	}

	@Override
	public void clearCache() {
		synchronized (this) {
			cachedDocumentTypeDefinitions = null;
		}
	}

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

	public Iterable<DocumentTypeDefinition> getTypeDefinitions() throws DmsError {
		ensureCachedDefinitions();
		return cachedDocumentTypeDefinitions.values();
	}

	public List<StoredDocument> search(DocumentSearch position) throws DmsError {
		ensureCachedDefinitions();
		Session cmisSession = createCmisSession();
		logger.info("Searching from: " + position.getPath() + "" + position.getClassName() + position.getCardId() + "");
		List<StoredDocument> results = new ArrayList<StoredDocument>();
		Folder folder = getFolder(cmisSession, position.getPath());
		logger.debug("In search : got a folder :" + folder);
		if (folder != null) {
			logger.debug("Got children of " + folder.getPath());
			for (CmisObject child : folder.getChildren()) {
				logger.debug("got a child " + child.getName());

				if (child instanceof Document) {
					Document cmisDocument = (Document) child;
					logger.debug("child is a cmisDocument " + cmisDocument.getDescription());

					logger.debug("getting paths for " + child.getName());
					String cmisPath = null;
					for (String path : cmisDocument.getPaths()) {
						if (cmisPath == null)
							cmisPath = path;
						else if (!cmisPath.startsWith(folder.getPath()) && path.startsWith(folder.getPath()))
							cmisPath = path;
					}

					String category = null;
					if (customModel.getCategory() != null) {
						Property<Object> property = cmisDocument.getProperty(customModel.getCategory());
						if (property != null) {
							category = getConverter(property.getDefinition()).convertFromCmisValue(cmisSession,
									property.getDefinition(), property.getValue());
						}
					}

					String author = null;
					if (customModel.getAuthor() != null) {
						Property<Object> property = cmisDocument.getProperty(customModel.getAuthor());
						if (property != null) {
							author = getConverter(property.getDefinition()).convertFromCmisValue(cmisSession,
									property.getDefinition(), property.getValue());
						}
					}

					DocumentTypeDefinition documentTypeDefinition = null;
					logger.info("Category of searchd document is " + category);
					if (category != null)
						documentTypeDefinition = cachedDocumentTypeDefinitions.get(category);
					if (documentTypeDefinition == null)
						documentTypeDefinition = definitionsFactory.newDocumentTypeDefinitionWithNoMetadata(category);

					final List<MetadataGroup> metadataGroups = new ArrayList<MetadataGroup>();
					for (final MetadataGroupDefinition metadataGroupDefinition : documentTypeDefinition
							.getMetadataGroupDefinitions()) {
						List<Metadata> metadataList = new ArrayList<Metadata>();
						for (final MetadataDefinition metadataDefinition : metadataGroupDefinition
								.getMetadataDefinitions()) {
							CmisMetadataDefinition cmisMetadata = (CmisMetadataDefinition) metadataDefinition;
							PropertyDefinition<?> propertyDefinition = cmisMetadata.getProperty();
							Property<Object> property = cmisDocument.getProperty(propertyDefinition.getId());
							logger.info("processing property " + property);
							if (property != null && property.getValue() != null) {
								logger.info("Value of property " + property.getValue());
								CmisConverter converter = getConverter(propertyDefinition);
								String value = converter.convertFromCmisValue(cmisSession, propertyDefinition,
										property.getValue());
								logger.info("After conversion Value of property " + value);
								metadataList.add(new CmisMetadata(cmisMetadata.getName(), value));
							}
						}
						metadataGroups.add(new CmisMetadataGroup(metadataGroupDefinition.getName(), metadataList));
					}

					StoredDocument storedDocument = new StoredDocument();
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
					logger.info("Child is not a document: " + child.getName() + " type: " + child.getClass().getName());
				}
			}
		}
		return results;
	}

	public DataHandler download(DocumentDownload document) throws DmsError {
		Session cmisSession = createCmisSession();
		Document cmisDocument = getDocument(cmisSession, document.getPath(), document.getFileName());
		return cmisDocument != null ? new DataHandler(new CmisDataSource(cmisDocument.getContentStream())) : null;
	}

	public void upload(StorableDocument document) throws DmsError {
		Session cmisSession = createCmisSession();
		Folder folder = createFolder(cmisSession, document.getPath());
		if (folder != null) {
			MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
			String mimeType = mimeTypesMap.getContentType(document.getFileName());

			Object author = null;
			if (customModel.getAuthor() != null) {
				PropertyDefinition<?> propertyDefinition = cachedPropertyDefinitions.get(customModel.getAuthor());
				if (propertyDefinition != null) {
					author = getConverter(propertyDefinition).convertToCmisValue(cmisSession, propertyDefinition,
							document.getAuthor());
				}
			}

			Document cmisDocument = getDocument(cmisSession, document.getPath(), document.getFileName());
			if (cmisDocument == null) {
				logger.info("create document");
				ContentStream contentStream = cmisSession.getObjectFactory().createContentStream(document.getFileName(),
						-1, mimeType, document.getInputStream());
				Map<String, Object> properties = getProperties(cmisSession, document, null);
				if (customModel.getAuthor() != null)
					properties.put(customModel.getAuthor(), author);
				for (Entry<String, Object> property : properties.entrySet()) {
					logger.debug("Property for document: " + property.toString());
				}
				cmisDocument = folder.createDocument(properties, contentStream, VersioningState.MAJOR);
				logger.info(
						"Document created : " + cmisDocument + "secondary type " + cmisDocument.getSecondaryTypes());

			} else {
				logger.info("update document");
				Document pwc = (Document) cmisSession.getObject(cmisDocument.checkOut());
				ContentStream contentStream = cmisSession.getObjectFactory().createContentStream(document.getFileName(),
						-1, mimeType, document.getInputStream());
				Map<String, Object> properties = getProperties(cmisSession, document, pwc);
				if (customModel.getAuthor() != null)
					properties.put(customModel.getAuthor(), author);
				try {
					for (String p : properties.keySet()) {
						logger.debug("Properties to set " + p + " " + properties.get(p));
					}
					pwc.checkIn(true, properties, contentStream, "");
				} catch (Exception e) {
					pwc.cancelCheckOut();
					throw DmsError.forward(e);
				} finally {
					try {
						contentStream.getStream().close();
					} catch (IOException e) {
						throw DmsError.forward(e);
					}
				}
			}
		}

	}

	public void updateDescriptionAndMetadata(DocumentUpdate document) throws DmsError {
		Session cmisSession = createCmisSession();
		Folder folder = getFolder(cmisSession, document.getPath());
		if (folder != null) {
			Document cmisDocument = getDocument(cmisSession, document.getPath(), document.getFileName());
			if (cmisDocument != null) {
				logger.info("Will update document: " + "path: " + cmisDocument.getPaths());
				logger.info("Will get properties for secondary type " + cmisDocument.getSecondaryTypes());
				Map<String, Object> properties = getProperties(cmisSession, document, cmisDocument);
				cmisDocument.updateProperties(properties);
			}
		}
	}

	public void delete(DocumentDelete document) throws DmsError {
		Session cmisSession = createCmisSession();
		logger.info("Delete dms document " + (document != null ? document.getFileName() : null));
		Document cmisDocument = getDocument(cmisSession, document.getPath(), document.getFileName());
		logger.info("Delete cmis document " + (cmisDocument != null ? document.getFileName() : null));
		if (cmisDocument != null) {
			logger.info("Delete cmis document " + cmisDocument.getName());
			cmisDocument.delete(true);
			logger.info("document deleted: " + cmisDocument.getName());
		} else {
			logger.error(
					"No document to delete for " + "path: " + document.getPath() + "name: " + document.getFileName());
		}
	}

	public void copy(StoredDocument document, DocumentSearch from, DocumentSearch to) throws DmsError {
		Session cmisSession = createCmisSession();
		logger.info("Trying to get cmid document for cmdbuild doc :" + document.getName());
		Document cmisDocument = getDocument(cmisSession, from.getPath(), document.getName());
		logger.debug("cmisDocument :" + cmisDocument);
		Folder toFolder = createFolder(cmisSession, to.getPath());
		logger.debug("folder :" + toFolder);
		if (cmisDocument != null && toFolder != null) {
			logger.debug("folder path" + toFolder.getPath());
			Map<String, Object> properties = new HashMap<String, Object>();
			for (Property<?> property : cmisDocument.getProperties()) {
				if (property.getValue() != null) {
					CmisConverter cmisConverter = propertyConverters.get(property.getId());
					logger.info("Property converter for " + property.getLocalName() + " is " + cmisConverter);
					if (cmisConverter != null && cmisConverter.isAsymmetric()) {
						String value = cmisConverter.convertFromCmisValue(cmisSession, property.getDefinition(),
								property.getValue());
						Object cmisValue = cmisConverter.convertToCmisValue(cmisSession, property.getDefinition(),
								value);
						logger.debug("Setting property " + property.getId() + " to " + cmisValue);
						properties.put(property.getId(), cmisValue);
					}
				} else {
					logger.debug("Will not set property " + property.getLocalName() + " as its value is null");
				}
			}
			cmisDocument.copy(toFolder, properties, null, null, null, null, null);
		}
	}

	public void move(StoredDocument document, DocumentSearch from, DocumentSearch to) throws DmsError {
		Session cmisSession = createCmisSession();

		logger.info("Move document: " + document.getPath() + "|" + document.getName() + " from :" + from.getPath()
				+ " to " + to.getPath());
		Folder fromFolder = getFolder(cmisSession, from.getPath());
		Folder toFolder = createFolder(cmisSession, to.getPath());
		logger.info("Move from Folder: " + fromFolder + "to folder:" + toFolder);
		if (fromFolder != null && toFolder != null) {
			Document cmisDocument = getDocument(cmisSession, from.getPath(), document.getName());
			if (cmisDocument != null) {
				cmisDocument.move(fromFolder, toFolder);
				logger.info("( Move ) document " + cmisDocument.getName() + "has moved from: " + fromFolder.getName()
						+ " to: " + toFolder.getName());
			} else {
				logger.warn("( Move ) unable to move, cmisdocument does not exists :" + document.getName() + "in "
						+ from.getPath());
			}
		} else {
			logger.warn("( Move ) Either from or to cmis folder is null" + "Cmis From: " + fromFolder + "Cmis To:  "
					+ toFolder);
		}
	}

	public void delete(DocumentSearch position) throws DmsError {
		Session cmisSession = createCmisSession();
		Folder folder = getFolder(cmisSession, position.getPath());
		logger.info("Will delete  tree" + folder.getName());
		if (folder != null) {
			List<String> results = folder.deleteTree(true, UnfileObject.DELETE, true);
			for (String result : results) {
				logger.debug("result " + result);
			}
		}
	}

	public void create(DocumentSearch position) throws DmsError {
		Session cmisSession = createCmisSession();
		createFolder(cmisSession, position.getPath());
	}

	private Session createCmisSession() {
		synchronized (this) {
			if (repository == null) {
				logger.info("initializing Alfresco client");
				SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
				Map<String, String> parameter = new HashMap<String, String>();
				parameter.put(SessionParameter.ATOMPUB_URL, configuration.getServerURL());
				parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
				parameter.put(SessionParameter.AUTH_HTTP_BASIC, "true");
				parameter.put(SessionParameter.USER, configuration.getAlfrescoUser());
				logger.info("Cmis server url: " + configuration.getAlfrescoUser());
				parameter.put(SessionParameter.PASSWORD, configuration.getAlfrescoPassword());
				parameter.put(SessionParameter.CONNECT_TIMEOUT, Integer.toString(10000));
				logger.info("Connect timeoutl: " + Integer.toString(10000));
				parameter.put(SessionParameter.READ_TIMEOUT, Integer.toString(30000));
				logger.info("Read timeoutl: " + Integer.toString(30000));

				if (customModel != null && customModel.getSessionParameters() != null) {
					for (Parameter param : customModel.getSessionParameters()) {
						parameter.put(param.getName(), param.getValue());
					}
				}

				List<Repository> repositories = sessionFactory.getRepositories(parameter);
				logger.info("got a repository list, length: " + repositories);
				repository = repositories.get(0);
				logger.info("will use repository" + repository.getName());
			}
		}
		return repository.createSession();
	}

	private CmisCustomModel loadCustomModel() throws Exception {
		logger.info("Loading custom model from " + configuration.getAlfrescoCustomModelFileName());
		final String content = configuration.getAlfrescoCustomModelFileContent();
		logger.info("content is " + content);
		final JAXBContext jaxbContext = JAXBContext.newInstance(CmisCustomModel.class);
		final StreamSource xml = new StreamSource(new StringReader(content));
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		logger.info("unmarsjalling with " + unmarshaller.getClass().getName());
		return (CmisCustomModel) unmarshaller.unmarshal(xml);
	}

	private void ensureCachedDefinitions() throws DmsError {
		synchronized (this) {
			if ((cachedPropertyDefinitions == null || cachedDocumentTypeDefinitions == null) && customModel != null) {
				logger.info("intializing internal cache for document type definitions");
				try {
					Session cmisSession = createCmisSession();

					List<ObjectType> types = new ArrayList<ObjectType>();
					if (customModel.getCmisType() != null) {
						ObjectType type = cmisSession.getTypeDefinition(customModel.getCmisType());
						logger.info("Caching cmis type definition " + type.getDisplayName());
						if (type != null && type.getPropertyDefinitions() != null)
							types.add(type);
						if (customModel.getSecondaryTypeList() != null) {
							for (String name : customModel.getSecondaryTypeList()) {
								logger.info("Caching secondary cmis type definition " + name);
								ObjectType secondaryType = cmisSession.getTypeDefinition(name);
								if (secondaryType != null && secondaryType.getPropertyDefinitions() != null)
									types.add(secondaryType);
							}
						}
					}

					final Map<String, PropertyDefinition<?>> cmisPropertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
					for (String name : Arrays.asList(customModel.getCategory(), customModel.getAuthor(),
							customModel.getDescription())) {
						if (name != null) {
							PropertyDefinition<?> property = null;
							for (ObjectType baseType : types) {
								if (property == null)
									property = baseType.getPropertyDefinitions().get(name);
							}
							if (property != null)
								cmisPropertyDefinitions.put(name, property);
						}
					}

					final Map<String, DocumentTypeDefinition> cmisDocumentTypes = new HashMap<String, DocumentTypeDefinition>();
					for (DocumentType documentType : customModel.getDocumentTypeList()) {
						logger.info("Processing document type defined in customModel " + documentType.getName());
						List<CmisMetadataGroupDefinition> cmisMetadataGroupDefinitions = new ArrayList<CmisMetadataGroupDefinition>();
						for (CmisCustomModel.MetadataGroup metadataGroup : documentType.getGroupList()) {
							List<CmisMetadataDefinition> cmisMetadataDefinitions = new ArrayList<CmisMetadataDefinition>();
							ObjectType secondaryType = null;
							if (metadataGroup.getCmisSecondaryTypeId() != null) {
								logger.info("caching " + metadataGroup.getCmisSecondaryTypeId());
								secondaryType = cmisSession.getTypeDefinition(metadataGroup.getCmisSecondaryTypeId());
								logger.info("Getting secondary type definition from server for "
										+ metadataGroup.getCmisSecondaryTypeId());
							}
							if (metadataGroup.getMetadataList() != null) {
								for (CmisCustomModel.Metadata metadata : metadataGroup.getMetadataList()) {
									PropertyDefinition<?> property = null;
									if (secondaryType != null && secondaryType.getPropertyDefinitions() != null) {
										property = secondaryType.getPropertyDefinitions()
												.get(metadata.getCmisPropertyId());
									}
									for (ObjectType baseType : types) {
										if (property == null)
											property = baseType.getPropertyDefinitions()
													.get(metadata.getCmisPropertyId());
									}
									if (property != null) {
										CmisConverter converter = getConverter(property);
										CmisMetadataDefinition cmisMetadata = new CmisMetadataDefinition(
												metadata.getName(), property, converter.getType(property));
										cmisMetadataDefinitions.add(cmisMetadata);
									}
								}
							} else if (secondaryType != null && secondaryType.getPropertyDefinitions() != null) {
								logger.info("Processing property definitions for " + secondaryType.getDisplayName());
								logger.info("alfrescocustomuri (configuration) is "
										+ configuration.getAlfrescoCustomUri());
								for (PropertyDefinition<?> property : secondaryType.getPropertyDefinitions().values()) {
									String localNamespace = property.getLocalNamespace();
									logger.info("Processing property : " + property.getDisplayName() + " ,namespace : "
											+ localNamespace + " ,localname " + property.getLocalName() + " ,queryname "
											+ property.getQueryName());
									if (property.getLocalNamespace()
											.equals(configuration.getAlfrescoCustomUri())) {
										CmisConverter converter = getConverter(property);
										CmisMetadataDefinition cmisMetadata = new CmisMetadataDefinition(
												property.getDisplayName(), property, converter.getType(property));
										cmisMetadataDefinitions.add(cmisMetadata);
									}
								}
							}
							if (cmisMetadataDefinitions != null) {
								CmisMetadataGroupDefinition cmisGroup = new CmisMetadataGroupDefinition(
										metadataGroup.getName(), secondaryType, cmisMetadataDefinitions);
								cmisMetadataGroupDefinitions.add(cmisGroup);
							}
						}
						if (cmisMetadataGroupDefinitions != null) {
							CmisDocumentType cmisDocumentType = new CmisDocumentType(documentType.getName(),
									cmisMetadataGroupDefinitions);
							cmisDocumentTypes.put(cmisDocumentType.getName(), cmisDocumentType);

						}
					}
					cachedPropertyDefinitions = cmisPropertyDefinitions;
					cachedDocumentTypeDefinitions = cmisDocumentTypes;
				} catch (final Exception e) {
					logger.error("error getting document type definitions", e);
					throw DmsError.forward(e);
				}
			}
		}
	}

	private Folder getFolder(Session cmisSession, List<String> pathList) {
		CmisObject object = null;
		if (pathList != null) {
			final StringBuilder path = new StringBuilder();
			path.append(configuration.getRepositoryWSPath());
			path.append(configuration.getRepositoryApp());
			for (String name : pathList) {
				path.append("/");
				path.append(name);
			}
			try {
				object = cmisSession.getObjectByPath(path.toString());
			} catch (CmisObjectNotFoundException e) {
				object = null;
			}
		}
		return object instanceof Folder ? (Folder) object : null;
	}

	private Folder createFolder(Session cmisSession, List<String> pathList) {
		final StringBuilder path = new StringBuilder();
		path.append(configuration.getRepositoryWSPath());
		path.append(configuration.getRepositoryApp());

		CmisObject object = cmisSession.getObjectByPath(path.toString());
		if (object instanceof Folder && pathList != null) {
			Folder parentFolder = (Folder) object;
			for (String name : pathList) {
				Folder folder = null;
				try {
					path.append('/');
					path.append(name);
					CmisObject child = cmisSession.getObjectByPath(path.toString());
					if (child instanceof Folder)
						folder = (Folder) child;
				} catch (CmisObjectNotFoundException e) {
					Map<String, String> properties = new HashMap<String, String>();
					properties.put("cmis:objectTypeId", "cmis:folder");
					properties.put("cmis:name", name);
					folder = parentFolder.createFolder(properties);
				}
				parentFolder = folder;
			}
			return parentFolder;
		} else
			return null;
	}

	private Document getDocument(Session cmisSession, List<String> pathList, String filename) {
		CmisObject object = null;
		if (pathList != null && filename != null) {
			final StringBuilder path = new StringBuilder();
			path.append(configuration.getRepositoryWSPath());
			path.append(configuration.getRepositoryApp());
			for (String name : pathList) {
				path.append("/");
				path.append(name);
			}
			path.append("/");
			path.append(filename);
			try {
				object = cmisSession.getObjectByPath(path.toString());
			} catch (CmisObjectNotFoundException e) {
				object = null;
			}
		}
		return object instanceof Document ? (Document) object : null;
	}

	private Map<String, Object> getProperties(Session cmisSession, DocumentUpdate document, Document cmisDocument)
			throws DmsError {
		ensureCachedDefinitions();

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("cmis:description", document.getDescription());

		if (customModel.getDescription() != null) {
			PropertyDefinition<?> propertyDefinition = cachedPropertyDefinitions.get(customModel.getDescription());
			logger.info("description property: " + propertyDefinition.getDisplayName() + " updatability "
					+ propertyDefinition.getUpdatability());
			if (propertyDefinition != null) {
				Object value = getConverter(propertyDefinition).convertToCmisValue(cmisSession, propertyDefinition,
						document.getDescription());
				logger.info("converted property for : " + propertyDefinition.getDisplayName() + " value: " + value);
				properties.put(customModel.getDescription(), value);
			}
		}

		String category = document.getCategory();
		logger.info("Category of document  " + document.getFileName() + " is " + category);
		if (category != null) {
			logger.info("CustomModel for  " + category + " " + customModel.getCategory());
			if (customModel.getCategory() != null) {
				PropertyDefinition<?> propertyDefinition = cachedPropertyDefinitions.get(customModel.getCategory());
				if (propertyDefinition != null) {
					Object value = getConverter(propertyDefinition).convertToCmisValue(cmisSession, propertyDefinition,
							document.getCategory());
					properties.put(customModel.getCategory(), value);
				}
			}
		} else {
			if (customModel.getCategory() != null) {
				Property<Object> property = cmisDocument.getProperty(customModel.getCategory());
				if (property != null) {
					category = getConverter(property.getDefinition()).convertFromCmisValue(cmisSession,
							property.getDefinition(), property.getValue());
				}
			}
		}

		if (category != null) {
			logger.info("Processing secondary types  for " + category);
			List<Object> secondaryTypes = new ArrayList<Object>();
			if (customModel.getSecondaryTypeList() != null) {
				logger.info("Secondary type list legth:  " + customModel.getSecondaryTypeList().size());
				for (String secondaryType : customModel.getSecondaryTypeList()) {
					logger.info("Adding secondary types  " + secondaryType);
					secondaryTypes.add(secondaryType);
				}
			} else {
				logger.info("No Secondarytype list   in customModel ");
			}

			CmisDocumentType documentType = (CmisDocumentType) cachedDocumentTypeDefinitions.get(category);
			if (documentType != null) {
				for (MetadataGroupDefinition group : documentType.getMetadataGroupDefinitions()) {
					CmisMetadataGroupDefinition cmisGroup = (CmisMetadataGroupDefinition) group;
					if (cmisGroup.getSecondaryType() != null) {
						secondaryTypes.add(cmisGroup.getSecondaryType().getId());
						logger.info("Adding secondary types  from metadata" + cmisGroup.getSecondaryType().getId());
					}
				}

				if (document.getMetadataGroups() != null) {
					for (MetadataGroup group : document.getMetadataGroups()) {
						logger.info("Processing group " + group.getName());
						CmisMetadataGroupDefinition groupDefinition = documentType
								.getMetadataGroupDefinition(group.getName());
						if (groupDefinition != null && group.getMetadata() != null) {
							for (Metadata metadata : group.getMetadata()) {
								CmisMetadataDefinition metadataDefinition = groupDefinition
										.getMetadataDefinition(metadata.getName());
								if (metadataDefinition != null) {
									PropertyDefinition<?> propertyDefinition = metadataDefinition.getProperty();
									CmisConverter converter = getConverter(propertyDefinition);
									Object value = converter.convertToCmisValue(cmisSession, propertyDefinition,
											metadata.getValue());
									properties.put(metadataDefinition.getProperty().getId(), value);
								}
							}
						} else {
							logger.info("Either group definition or group.getMetadata() is null");
							logger.info(" group definition " + groupDefinition + " group.getMetadata :"
									+ group.getMetadata());
						}
					}
				} else {
					logger.info("No group metadata for" + document.getFileName());
				}
			}
			logger.warn("CMISDOCUMENT " + cmisDocument);

			if (cmisDocument != null) {
				logger.info("cmisdocument : " + cmisDocument.getPaths());
				if (cmisDocument.getSecondaryTypes() != null) {
					for (ObjectType secondaryType : cmisDocument.getSecondaryTypes()) {
						logger.warn("SECONDARY TYPE NAMESPACE" + secondaryType.getLocalNamespace());
						if (!secondaryType.getLocalNamespace().equals(configuration.getAlfrescoCustomUri())) {
							if (!secondaryTypes.contains(secondaryType.getId()))
								secondaryTypes.add(secondaryType.getId());
						}
					}
				}
			} else {
				logger.info("cmisdocument is null");
			}

			properties.put("cmis:secondaryObjectTypeIds", secondaryTypes);
		}

		if (cmisDocument == null) {
			properties.put("cmis:objectTypeId",
					customModel.getCmisType() != null ? customModel.getCmisType() : "cmis:document");
			properties.put("cmis:name", document.getFileName());
		}

		return properties;
	}

	private CmisConverter getConverter(PropertyDefinition<?> property) {
		logger.debug("Getting converter for: " + property.getDisplayName());
		CmisConverter converter = propertyConverters.get(property.getId());
		final CmisConverter result = converter != null ? converter : defaultConverter;
		logger.debug("Getconverter for:" + property.getDisplayName() + " is " + result);
		return result;
	}
}
