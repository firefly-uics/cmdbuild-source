package org.cmdbuild.dms.documents;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;

public class DefaultDocumentFactory implements DocumentFactory {

	private final Collection<String> basePath;

	public DefaultDocumentFactory(final Collection<String> basePath) {
		Validate.notNull(basePath, "null path");
		this.basePath = basePath;
	}

	public DocumentSearch createDocumentSearch(final String className, final int cardId) {
		return new DocumentSearch() {

			public String getClassName() {
				return className;
			}

			public int getCardId() {
				return cardId;
			}

			public List<String> getPath() {
				return path(cardId);
			}

		};

	}

	public StorableDocument createStorableDocument(final String author, final String className, final int cardId,
			final InputStream inputStream, final String fileName, final String category, final String description) {
		return new StorableDocument() {

			public String getAuthor() {
				return author;
			}

			public String getClassName() {
				return className;
			}

			public int getCardId() {
				return cardId;
			}

			public List<String> getPath() {
				return path(cardId);
			}

			public InputStream getInputStream() {
				return inputStream;
			}

			public String getFileName() {
				return fileName;
			}

			public String getCategory() {
				return category;
			}

			public String getDescription() {
				return description;
			}

		};
	}

	public DocumentDownload createDocumentDownload(final String className, final int cardId, final String fileName) {
		return new DocumentDownload() {

			public String getClassName() {
				return className;
			}

			public int getCardId() {
				return cardId;
			}

			public List<String> getPath() {
				return path(cardId);
			}

			public String getFileName() {
				return fileName;
			}

		};
	}

	public DocumentDelete createDocumentDelete(final String className, final int cardId, final String fileName) {
		return new DocumentDelete() {

			public String getClassName() {
				return className;
			}

			public int getCardId() {
				return cardId;
			}

			public List<String> getPath() {
				return path(cardId);
			}

			public String getFileName() {
				return fileName;
			}

		};
	}

	public DocumentUpdate createDocumentUpdate(final String className, final int cardId, final String filename,
			final String description) {
		return new DocumentUpdate() {

			public String getClassName() {
				return className;
			}

			public int getCardId() {
				return cardId;
			}

			public List<String> getPath() {
				return path(cardId);
			}

			public String getFileName() {
				return filename;
			}

			public String getDescription() {
				return description;
			}

		};
	}

	private List<String> path(final int cardId) {
		final List<String> fullPath = new ArrayList<String>(basePath);
		fullPath.add("Id" + cardId);
		return Collections.unmodifiableList(fullPath);
	}

}
