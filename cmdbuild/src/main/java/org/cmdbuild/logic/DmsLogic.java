package org.cmdbuild.logic;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.DocumentUpdate;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.services.auth.UserContext;

public class DmsLogic {

	private final DmsService service;
	private UserContext userContext;
	
	public DmsLogic(final DmsService service) {
		this.service = service;
	}

	public UserContext getUserContext() {
		return userContext;
	}

	public void setUserContext(final UserContext userContext) {
		this.userContext = userContext;
	}

	public List<StoredDocument> search(final String className, final int cardId) {
		final ITable schema = userContext.tables().get(className);
		return service.search(new DocumentSearch() {

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public int getCardId() {
				return cardId;
			}

		});
	}

	public void upload(final String author, final String className, final int cardId, final InputStream inputStream,
			final String fileName, final String category, final String description) throws IOException, CMDBException {
		final ITable schema = userContext.tables().get(className);
		userContext.privileges().assureWritePrivilege(schema);
		final boolean uploaded = service.upload(new StorableDocument() {

			@Override
			public String getAuthor() {
				return author;
			}

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public int getCardId() {
				return cardId;
			}

			@Override
			public InputStream getInputStream() {
				return inputStream;
			}

			@Override
			public String getFileName() {
				return fileName;
			}

			@Override
			public String getCategory() {
				return category;
			}

			@Override
			public String getDescription() {
				return description;
			}

		});

		if (!uploaded) {
			throw ORMException.ORMExceptionType.ORM_ATTACHMENT_UPLOAD_FAILED.createException();
		}
	}

	public DataHandler download(final String className, final int cardId, final String fileName) {
		final ITable schema = userContext.tables().get(className);
		final DataHandler dataHandler = service.download(new DocumentDownload() {

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public int getCardId() {
				return cardId;
			}
			
			@Override
			public String getFileName() {
				return fileName;
			}

		});
		if (dataHandler == null) {
			throw NotFoundExceptionType.ATTACHMENT_NOTFOUND
					.createException(fileName, className, String.valueOf(cardId));
		}
		return dataHandler;
	}

	public void delete(final String className, final int cardId, final String fileName) throws NotFoundException {
		final ITable schema = userContext.tables().get(className);
		userContext.privileges().assureWritePrivilege(schema);
		final boolean deleted = service.delete(new DocumentDelete() {

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public int getCardId() {
				return cardId;
			}

			@Override
			public String getFileName() {
				return fileName;
			}

		});

		if (!deleted) {
			throw ORMException.ORMExceptionType.ORM_ATTACHMENT_DELETE_FAILED.createException();
		}
	}
	
	public boolean updateDescription(final String className, final int cardId, final String filename, final String description) {
		final ITable schema = userContext.tables().get(className);
		userContext.privileges().assureWritePrivilege(schema);
		return service.updateDescription(new DocumentUpdate() {

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public int getCardId() {
				return cardId;
			}

			@Override
			public String getFileName() {
				return filename;
			}

			@Override
			public String getDescription() {
				return description;
			}

		});
	}

}
