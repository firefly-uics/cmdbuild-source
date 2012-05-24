package org.cmdbuild.workflow.xpdl;

public interface CMActivityVariableToProcess {

	enum Type {
		READ_ONLY,
		READ_WRITE,
		READ_WRITE_REQUIRED
	}

	String getName();
	Type getType();

}
