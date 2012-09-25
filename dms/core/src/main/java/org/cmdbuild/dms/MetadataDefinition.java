package org.cmdbuild.dms;

public interface MetadataDefinition {

	String getName();

	String getType();

	String getDescription();

	boolean isMandatory();

}