package org.cmdbuild.cmdbf.xml;

import org.cmdbuild.dms.Metadata;

public class DmsMetadata implements Metadata {

	private String name;
	private String value;

	public DmsMetadata(String name, String value) {
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
