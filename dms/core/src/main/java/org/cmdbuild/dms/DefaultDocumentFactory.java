package org.cmdbuild.dms;

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

	@Override
	public DocumentSearch createDocumentSearch(final String className, final int cardId) {
		return new DocumentSearch() {

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public int getCardId() {
				return cardId;
			}

			@Override
			public List<String> getPath() {
				return path(cardId);
			}

		};

	}

	@Override
	public StorableDocument createStorableDocument(final String author, final String className, final int cardId,
			final InputStream inputStream, final String fileName, final String category, final String description) {
		return createStorableDocument(author, className, cardId, inputStream, fileName, category, description,
				Collections.<MetadataGroup> emptyList());
	}

	@Override
	public StorableDocument createStorableDocument(final String author, final String className, final int cardId,
			final InputStream inputStream, final String fileName, final String category, final String description,
			final Iterable<MetadataGroup> metadataGroups) {
		return new StorableDocument() {

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
			public List<String> getPath() {
				return path(cardId);
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

			@Override
			public Iterable<MetadataGroup> getMetadataGroups() {
				return metadataGroups;
			}

		};
	}

	@Override
	public DocumentDownload createDocumentDownload(final String className, final int cardId, final String fileName) {
		return new DocumentDownload() {

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public int getCardId() {
				return cardId;
			}

			@Override
			public List<String> getPath() {
				return path(cardId);
			}

			@Override
			public String getFileName() {
				return fileName;
			}

		};
	}

	@Override
	public DocumentDelete createDocumentDelete(final String className, final int cardId, final String fileName) {
		return new DocumentDelete() {

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public int getCardId() {
				return cardId;
			}

			@Override
			public List<String> getPath() {
				return path(cardId);
			}

			@Override
			public String getFileName() {
				return fileName;
			}

		};
	}

	@Override
	public DocumentUpdate createDocumentUpdate(final String className, final int cardId, final String filename,
			final String description) {
		return createDocumentUpdate(className, cardId, filename, description, Collections.<MetadataGroup>emptyList());
	}

	@Override
	public DocumentUpdate createDocumentUpdate(final String className, final int cardId,
			final String filename, final String description,
			final Iterable<MetadataGroup> metadataGroups) {
		return new DocumentUpdate() {

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public int getCardId() {
				return cardId;
			}

			@Override
			public List<String> getPath() {
				return path(cardId);
			}

			@Override
			public String getFileName() {
				return filename;
			}

			@Override
			public String getDescription() {
				return description;
			}

			@Override
			public Iterable<MetadataGroup> getMetadataGroups() {
				return metadataGroups;
			}

		};
	}

	private List<String> path(final int cardId) {
		final List<String> fullPath = new ArrayList<String>(basePath);
		fullPath.add("Id" + cardId);
		return Collections.unmodifiableList(fullPath);
	}

}
