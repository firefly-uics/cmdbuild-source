package org.cmdbuild.dms;

public interface DocumentUpdate extends Document {

	String getFileName();

	String getDescription();

	Iterable<MetadataGroup> getMetadataGroups();

}
