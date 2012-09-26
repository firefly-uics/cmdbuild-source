package org.cmdbuild.dms;

import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;

public class CachedDmsService extends ForwardingDmsService {

	private Iterable<DocumentTypeDefinition> cachedDocumentTypeDefinitions;
	private AutocompletionRules cachedAutocompletionRules;

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
			cachedDocumentTypeDefinitions = null;
			cachedAutocompletionRules = null;
		}
	}

}