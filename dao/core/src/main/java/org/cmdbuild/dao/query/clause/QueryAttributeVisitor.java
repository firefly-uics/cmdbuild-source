package org.cmdbuild.dao.query.clause;

public interface QueryAttributeVisitor {

	void accept(AnyAttribute value);

	void visit(NamedAttribute value);

	void visit(QueryAliasAttribute value);

}
