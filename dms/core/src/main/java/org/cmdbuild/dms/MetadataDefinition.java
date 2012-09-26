package org.cmdbuild.dms;

public interface MetadataDefinition {

	String getName();

	MetadataType getType();

	String getDescription();

	boolean isMandatory();

}