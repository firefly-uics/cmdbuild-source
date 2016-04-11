package org.cmdbuild.data.store.task;

public interface TaskDefinitionVisitor {

	void visit(AsynchronousEventTaskDefinition taskDefinition);

	void visit(ConnectorTaskDefinition connectorTaskDefinition);

	void visit(ReadEmailTaskDefinition taskDefinition);

	void visit(StartWorkflowTaskDefinition taskDefinition);

	void visit(SynchronousEventTaskDefinition taskDefinition);

}
