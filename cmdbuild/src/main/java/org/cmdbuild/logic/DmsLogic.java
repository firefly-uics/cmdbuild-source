package org.cmdbuild.logic;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;
import org.cmdbuild.config.DmsProperties;
import org.cmdbuild.dms.DefaultDocumentFactory;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentFactory;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.DocumentUpdate;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;

import com.google.common.collect.Maps;

public class DmsLogic {

	private static Logger logger = Log.DMS;

	private final DmsService service;
	private UserContext userContext;

	public DmsLogic(final DmsService service) {
		logger.info("creating new dms logic...");
		this.service = service;
		service.setConfiguration(DmsProperties.getInstance());
	}

	public UserContext getUserContext() {
		return userContext;
	}

	public void setUserContext(final UserContext userContext) {
		this.userContext = userContext;
	}

	/**
	 * Gets the lookup type that represents attachment categories.
	 * 
	 * @return the name of the lookup type that represents attachment
	 *         categories.
	 */
	public String getCategoryLookupType() {
		return service.getConfiguration().getCmdbuildCategory();
	}

	/**
	 * Gets the {@link DocumentTypeDefinition} associated with the specified
	 * category.
	 * 
	 * @param category
	 *            is the {@code Code} of the {@link Lookup}.
	 * 
	 * @return the {@link DocumentTypeDefinition} for the specified category.
	 */
	public DocumentTypeDefinition getCategoryDefinition(final String category) {
		for (final DocumentTypeDefinition typeDefinition : service.getTypeDefinitions()) {
			if (typeDefinition.getName().equals(category)) {
				return typeDefinition;
			}
		}
		return typeDefinitinWithNoMetadata(category);
	}

	/**
	 * Gets the autocompletion rules for the specified class.
	 * 
	 * @param classname
	 *            the name of the class.
	 * 
	 * @return maps of metadata names and values grouped by metadata group.
	 */
	public Map<String, Map<String, String>> getAutoCompletionRulesByClass(final String classname) {
		final Map<String, Map<String, String>> rulesByClassname = Maps.newHashMap();
		final AutocompletionRules rules = service.getAutoCompletionRules();
		for (final String groupName : rules.getMetadataGroupNames()) {
			rulesByClassname.put(groupName, Maps.<String, String> newHashMap());
			for (final String metadataName : rules.getMetadataNamesForGroup(groupName)) {
				final Map<String, String> valuesByClassname = rules
						.getRulesForGroupAndMetadata(groupName, metadataName);
				for (final String _classname : valuesByClassname.keySet()) {
					if (_classname.equals(classname)) {
						rulesByClassname.get(groupName).put(metadataName, valuesByClassname.get(_classname));
					}
				}
			}
		}
		return rulesByClassname;
	}

	// TODO put in an abstract factory
	private DocumentTypeDefinition typeDefinitinWithNoMetadata(final String type) {
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

	public List<StoredDocument> search(final String className, final int cardId) {
		final DocumentSearch document = createDocumentFactory(className) //
				.createDocumentSearch(className, cardId);
		return service.search(document);
	}

	public void upload(final String author, final String className, final int cardId, final InputStream inputStream,
			final String fileName, final String category, final String description,Iterable<MetadataGroup> metadataGroups) throws IOException, CMDBException {
		final StorableDocument document = createDocumentFactory(className) //
				.createStorableDocument(author, className, cardId, inputStream, fileName, category, description, metadataGroups);
		assureWritePrivilege(className);
		try {
			service.upload(document);
		} catch (final Exception e) {
			final String message = String.format("error uploading file '%s' to card '%s' with id '%d'", //
					fileName, className, cardId);
			logger.error(message, e);
			throw ORMException.ORMExceptionType.ORM_ATTACHMENT_UPLOAD_FAILED.createException();
		}
	}

	public DataHandler download(final String className, final int cardId, final String fileName) {
		final DocumentDownload document = createDocumentFactory(className) //
				.createDocumentDownload(className, cardId, fileName);
		try {
			final DataHandler dataHandler = service.download(document);
			return dataHandler;
		} catch (final Exception e) {
			final String message = String.format("error downloading file '%s' for card '%s' with id '%d'", //
					fileName, className, cardId);
			logger.error(message, e);
			throw NotFoundExceptionType.ATTACHMENT_NOTFOUND
					.createException(fileName, className, String.valueOf(cardId));
		}
	}

	public void delete(final String className, final int cardId, final String fileName) throws NotFoundException {
		final DocumentDelete document = createDocumentFactory(className) //
				.createDocumentDelete(className, cardId, fileName);
		assureWritePrivilege(className);
		try {
			service.delete(document);
		} catch (final Exception e) {
			final String message = String.format("error deleting file '%s' for card '%s' with id '%d'", //
					fileName, className, cardId);
			logger.error(message, e);
			throw ORMException.ORMExceptionType.ORM_ATTACHMENT_DELETE_FAILED.createException();
		}
	}

	public void updateDescription(final String className, final int cardId, final String filename,
			final String description,Iterable<MetadataGroup> metadataGroups) {
		final DocumentUpdate document = createDocumentFactory(className) //
				.createDocumentUpdate(className, cardId, filename, description,metadataGroups);
		assureWritePrivilege(className);
		try {
			service.updateDescription(document);
		} catch (final Exception e) {
			final String message = String.format("error updating description to file '%s' for card '%s' with id '%d'", //
					filename, className, cardId);
			logger.error(message, e);
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
	}

	private DocumentFactory createDocumentFactory(final String className) {
		final Collection<String> path = userContext.tables().tree().path(className);
		return new DefaultDocumentFactory(path);
	}

	private void assureWritePrivilege(final String className) {
		final ITable schema = userContext.tables().get(className);
		userContext.privileges().assureWritePrivilege(schema);
	}

	public void clearCache() {
		service.clearCache();
	}

}
