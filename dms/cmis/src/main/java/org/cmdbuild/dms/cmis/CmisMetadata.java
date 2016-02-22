package org.cmdbuild.dms.cmis;

import org.cmdbuild.dms.Metadata;

public class CmisMetadata implements Metadata {
	private String name;
	private String value;

	public CmisMetadata(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}
}
