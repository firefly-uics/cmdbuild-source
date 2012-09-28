package org.cmdbuild.dms.alfresco;

import static java.lang.String.format;

import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.dms.BaseDmsService;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService.LoggingSupport;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.DocumentUpdate;
import org.cmdbuild.dms.MetadataAutocompletion;
import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.alfresco.ftp.AlfrescoFtpService;
import org.cmdbuild.dms.alfresco.utils.XmlAutocompletionReader;
import org.cmdbuild.dms.alfresco.webservice.AlfrescoWsService;
import org.cmdbuild.dms.exception.DmsException;
import org.cmdbuild.dms.exception.WebserviceException;

public class AlfrescoDmsService extends BaseDmsService implements LoggingSupport {

	private AlfrescoFtpService ftpService;
	private AlfrescoWsService wsService;

	@Override
	public void setConfiguration(final DmsConfiguration configuration) {
		super.setConfiguration(configuration);

		logger.info("initializing Alfresco inner services for ftp/ws");
		ftpService = new AlfrescoFtpService(configuration);
		wsService = new AlfrescoWsService(configuration);
	}

	@Override
	public Iterable<DocumentTypeDefinition> getTypeDefinitions() {
		return wsService.getDocumentTypeDefinitions();
	}

	@Override
	public void delete(final DocumentDelete document) throws DmsException {
		ftpService.delete(document);
	}

	@Override
	public DataHandler download(final DocumentDownload document) throws DmsException {
		return ftpService.download(document);
	}

	@Override
	public List<StoredDocument> search(final DocumentSearch document) {
		return wsService.search(document);
	}

	@Override
	public void updateDescriptionAndMetadata(final DocumentUpdate document) throws DmsException {
		wsService.updateDescription(document);
	}

	@Override
	public void upload(final StorableDocument document) throws DmsException {
		ftpService.upload(document);
		waitForSomeTimeBetweenFtpAndWebserviceOperations();
		try {
			wsService.updateCategory(document);
			wsService.updateProperties(document);
		} catch (final Exception e) {
			final String message = format("error updating metadata for file '%s' at path '%s'", //
					document.getFileName(), document.getPath());
			logger.error(message, e);
			ftpService.delete(documentDeleteFrom(document));
			throw new WebserviceException(e);
		}
	}

	/**
	 * This is very ugly! Old tests shows some problems if Webservice operations
	 * follows immediately FTP operations, so this delay was introduced.
	 */
	private void waitForSomeTimeBetweenFtpAndWebserviceOperations() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			logger.warn("should never happen... so why?", e);
		}
	}

	private DocumentDelete documentDeleteFrom(final StorableDocument document) {
		return new DocumentDelete() {

			@Override
			public List<String> getPath() {
				return document.getPath();
			}

			@Override
			public String getClassName() {
				return document.getClassName();
			}

			@Override
			public int getCardId() {
				return document.getCardId();
			}

			@Override
			public String getFileName() {
				return document.getFileName();
			}

		};
	}

	@Override
	public AutocompletionRules getAutoCompletionRules() {
		final String content = getConfiguration().getMetadataAutocompletionFileContent();
		final MetadataAutocompletion.Reader reader = new XmlAutocompletionReader(content);
		return reader.read();
	}

	@Override
	public void clearCache() {
		wsService.clearCache();
	}

}
