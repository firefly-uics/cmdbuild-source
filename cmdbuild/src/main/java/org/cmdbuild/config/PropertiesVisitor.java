package org.cmdbuild.config;

public interface PropertiesVisitor {

	void visit(final AuthProperties properties);
	void visit(final CmdbfProperties properties);
	void visit(final CmdbuildProperties properties);
	void visit(final DatabaseProperties properties);
	void visit(final DmsProperties properties);
	void visit(final EmailProperties properties);
	void visit(final GisProperties properties);
	void visit(final GraphProperties properties);
	void visit(final WorkflowProperties properties);

}
