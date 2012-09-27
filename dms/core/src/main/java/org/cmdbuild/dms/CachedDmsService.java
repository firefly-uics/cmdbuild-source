package org.cmdbuild.dms;

import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;

public class CachedDmsService extends ForwardingDmsService {

	private final DmsService dmsService;

	private Iterable<DocumentTypeDefinition> cachedDocumentTypeDefinitions;
	private AutocompletionRules cachedAutocompletionRules;

	public CachedDmsService(final DmsService dmsService) {
		super(dmsService);
		this.dmsService = dmsService;
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
	public AutocompletionRules getAutoCompletionRules() {
		synchronized (this) {
			if (cachedAutocompletionRules == null) {
				cachedAutocompletionRules = super.getAutoCompletionRules();
			}
			return cachedAutocompletionRules;
		}
	}

	@Override
	public void clearCache() {
		synchronized (this) {
			/*
			 * it's so bad to store a reference to the real DMS service, but
			 * actually we need to do it because Alfresco DMS service uses an
			 * internal cache
			 */
			dmsService.clearCache();
			cachedDocumentTypeDefinitions = null;
			cachedAutocompletionRules = null;
		}
	}

}