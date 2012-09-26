package org.cmdbuild.dms;

import java.util.Map;

public class MetadataAutocompletion {

	private MetadataAutocompletion() {
		// prevents instantiation
	}

	public interface AutocompletionRules {

		Iterable<String> getMetadataGroupNames();

		Iterable<String> getMetadataNamesForGroup(String groupName);

		Map<String, String> getRulesForGroupAndMetadata(String groupName, String metadataName);

	}

	public interface Reader {

		public AutocompletionRules read();

	}

}
