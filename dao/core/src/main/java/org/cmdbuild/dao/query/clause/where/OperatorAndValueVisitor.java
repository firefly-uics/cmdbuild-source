package org.cmdbuild.dao.query.clause.where;

public interface OperatorAndValueVisitor {

	void visit(EqualsOperatorAndValue operatorAndValue);

	void visit(GreatherThanOperatorAndValue operatorAndValue);

	void visit(LessThanOperatorAndValue operatorAndValue);

}
