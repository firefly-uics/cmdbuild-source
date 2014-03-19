package org.cmdbuild.cmdbf.xml;

import java.util.HashMap;

import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataGroup;

public class DmsMetadataGroup extends HashMap<String, Metadata> implements MetadataGroup  {

	private static final long serialVersionUID = 1L;
	private String name;
	
	public DmsMetadataGroup(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Iterable<Metadata> getMetadata() {
		return values();
	}	
}
