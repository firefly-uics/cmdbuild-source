package org.cmdbuild.logic.taskmanager;

public interface TaskVistor {

	void visit(ConnectorTask task);

	void visit(ReadEmailTask task);

	void visit(StartWorkflowTask task);

	void visit(SynchronousEventTask task);

}
