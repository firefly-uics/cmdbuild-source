package org.cmdbuild.logic.dms;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.data.store.lookup.Predicates.lookupActive;
import static org.cmdbuild.logic.dms.Utils.valueForCategory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.dms.DefaultDefinitionsFactory;
import org.cmdbuild.dms.DefinitionsFactory;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreator;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.DocumentUpdate;
import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.exception.DmsError;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.DmsException;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class DefaultDmsLogic implements DmsLogic {

	private final DmsService service;
	private final DefinitionsFactory definitionsFactory;
	private final CMDataView dataView;
	private final DmsConfiguration configuration;
	private final DocumentCreatorFactory documentCreatorFactory;
	private final LookupStore lookupStore;

	public DefaultDmsLogic( //
			final DmsService service, //
			final CMDataView dataView, //
			final DmsConfiguration configuration, //
			final DocumentCreatorFactory documentCreatorFactory, //
			final LookupStore lookupStore //
	) {
		logger.trace("creating new dms logic...");
		this.service = service;
		service.setConfiguration(configuration);
		definitionsFactory = new DefaultDefinitionsFactory();
		this.dataView = dataView;
		this.configuration = configuration;
		this.documentCreatorFactory = documentCreatorFactory;
		this.lookupStore = lookupStore;
	}

	/**
	 * Gets the lookup type that represents attachment categories.
	 * 
	 * @return the name of the lookup type that represents attachment
	 *         categories.
	 */
	@Override
	public String getCategoryLookupType() {
		return service.getConfiguration().getCmdbuildCategory();
	}

	/**
	 * Gets the {@link DocumentTypeDefinition} associated with the specified
	 * category.
	 * 
	 * @param category
	 *            is the {@code Description} of the {@link Lookup}.
	 * 
	 * @return the {@link DocumentTypeDefinition} for the specified category.
	 * 
	 * @throws {@link DmsException} if cannot read definitions.
	 */
	@Override
	public DocumentTypeDefinition getCategoryDefinition(final String category) {
		try {
			if (configuration.isEnabled()) {
				for (final DocumentTypeDefinition typeDefinition : getCategoryDefinitions()) {
					if (typeDefinition.getName().equals(category)) {
						return typeDefinition;
					}
				}
			}
			return definitionsFactory.newDocumentTypeDefinitionWithNoMetadata(category);
		} catch (final DmsError e) {
			throw DmsException.Type.DMS_DOCUMENT_TYPE_DEFINITION_ERROR.createException(category);
		}
	}

	@Override
	public Iterable<DocumentTypeDefinition> getConfiguredCategoryDefinitions() {
		final LookupType lookupType = LookupType.newInstance() //
				.withName(getCategoryLookupType()) //
				.build();
		final Iterable<Lookup> lookupValues = lookupStore.readAll(lookupType);
		return from(lookupValues) //
				.filter(lookupActive()) //
				.transform(new Function<Lookup, DocumentTypeDefinition>() {

					@Override
					public DocumentTypeDefinition apply(final Lookup input) {
						return getCategoryDefinition(valueForCategory(input));
					}

				});
	}

	/**
	 * Gets all {@link DocumentTypeDefinition}s.
	 * 
	 * @return the all {@link DocumentTypeDefinition}s.
	 * 
	 * @throws DmsError
	 */
	@Override
	public Iterable<DocumentTypeDefinition> getCategoryDefinitions() throws DmsError {
		return service.getTypeDefinitions();
	}

	/**
	 * Gets the auto-completion rules for the specified class.
	 * 
	 * @param classname
	 *            the name of the class.
	 * 
	 * @return maps of metadata names and values grouped by metadata group.
	 * 
	 * @throws DmsError
	 */
	@Override
	public Map<String, Map<String, String>> getAutoCompletionRulesByClass(final String classname) throws DmsException {
		try {
			final Map<String, Map<String, String>> rulesByClassname = Maps.newHashMap();
			if (configuration.isEnabled()) {
				final AutocompletionRules rules = service.getAutoCompletionRules();
				for (final String groupName : rules.getMetadataGroupNames()) {
					rulesByClassname.put(groupName, Maps.<String, String> newHashMap());
					for (final String metadataName : rules.getMetadataNamesForGroup(groupName)) {
						final Map<String, String> valuesByClassname = rules.getRulesForGroupAndMetadata(groupName,
								metadataName);
						for (final String _classname : valuesByClassname.keySet()) {
							if (_classname.equals(classname)) {
								rulesByClassname.get(groupName).put(metadataName, valuesByClassname.get(_classname));
							}
						}
					}
				}
			}
			return rulesByClassname;
		} catch (final DmsError e) {
			throw DmsException.Type.DMS_AUTOCOMPLETION_RULES_ERROR.createException(classname);
		}
	}

	@Override
	public List<StoredDocument> search(final String className, final Long cardId) {
		try {
			final DocumentSearch document = createDocumentFactory(className) //
					.createDocumentSearch(className, cardId.toString());
			return service.search(document);
		} catch (final DmsError e) {
			logger.warn("cannot get stored documents", e);
			// TODO
			return Collections.emptyList();
		}
	}

	@Override
	public Optional<StoredDocument> search(final String className, final Long cardId, final String fileName) {
		return from(search(className, cardId)) //
				.filter(new Predicate<StoredDocument>() {

					@Override
					public boolean apply(final StoredDocument input) {
						return input.getName().equals(fileName);
					}

				}).first();
	}

	@Override
	public void upload(final String author, final String className, final Long cardId, final InputStream inputStream,
			final String fileName, final String category, final String description,
			final Iterable<MetadataGroup> metadataGroups) throws IOException, CMDBException {
		final StorableDocument document = createDocumentFactory(className) //
				.createStorableDocument(author, className, cardId.toString(), inputStream, fileName, category,
						description, metadataGroups);
		try {
			service.upload(document);
		} catch (final Exception e) {
			final String message = String.format("error uploading file '%s' to card '%s' with id '%d'", //
					fileName, className, cardId);
			logger.error(message, e);
			throw DmsException.Type.DMS_ATTACHMENT_UPLOAD_ERROR.createException();
		}
	}

	@Override
	public DataHandler download(final String className, final Long cardId, final String fileName) {
		final DocumentDownload document = createDocumentFactory(className) //
				.createDocumentDownload(className, cardId.toString(), fileName);
		try {
			final DataHandler dataHandler = service.download(document);
			return dataHandler;
		} catch (final Exception e) {
			final String message = String.format("error downloading file '%s' for card '%s' with id '%d'", //
					fileName, className, cardId);
			logger.error(message, e);
			throw DmsException.Type.DMS_ATTACHMENT_NOTFOUND
					.createException(fileName, className, String.valueOf(cardId));
		}
	}

	@Override
	public void delete(final String className, final Long cardId, final String fileName) throws DmsException {
		final DocumentDelete document = createDocumentFactory(className) //
				.createDocumentDelete(className, cardId.toString(), fileName);
		try {
			service.delete(document);
		} catch (final Exception e) {
			final String message = String.format("error deleting file '%s' for card '%s' with id '%d'", //
					fileName, className, cardId);
			logger.error(message, e);
			throw DmsException.Type.DMS_ATTACHMENT_DELETE_ERROR.createException();
		}
	}

	@Override
	public void updateDescriptionAndMetadata(final String className, final Long cardId, final String filename,
			final String category, final String description, final Iterable<MetadataGroup> metadataGroups) {
		final DocumentUpdate document = createDocumentFactory(className) //
				.createDocumentUpdate(className, cardId.toString(), filename, category, description, metadataGroups);
		try {
			service.updateDescriptionAndMetadata(document);
		} catch (final Exception e) {
			final String message = String.format("error updating file '%s' for card '%s' with id '%d'", //
					filename, className, cardId);
			logger.error(message, e);
			throw DmsException.Type.DMS_UPDATE_ERROR.createException();
		}
	}

	private DocumentCreator createDocumentFactory(final String className) {
		final CMClass fetchedClass = dataView.findClass(className);
		return documentCreatorFactory.create(fetchedClass);
	}

}
