package org.cmdbuild.dms;

public class CachedDmsService extends ForwardingDmsService {

	private Iterable<DocumentTypeDefinition> cachedDocumentTypeDefinitions;

	public CachedDmsService(final DmsService dmsService) {
		super(dmsService);
	}

	@Override
	public Iterable<DocumentTypeDefinition> getTypeDefinitions() {
		synchronized (this) {
			if (cachedDocumentTypeDefinitions == null) {
				cachedDocumentTypeDefinitions = super.getTypeDefinitions();
			}
			return cachedDocumentTypeDefinitions;
		}
	}

	@Override
	public void clearCache() {
		synchronized (this) {
			cachedDocumentTypeDefinitions = null;
		}
	}

}