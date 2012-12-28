package org.cmdbuild.dao.query.clause.where;

public interface OperatorAndValueVisitor {

	void visit(EqualsOperatorAndValue operatorAndValue);

	void visit(GreatherThanOperatorAndValue operatorAndValue);

	void visit(LessThanOperatorAndValue operatorAndValue);

	void visit(ContainsOperatorAndValue operatorAndValue);

	void visit(BeginsWithOperatorAndValue operatorAndValue);

	void visit(EndsWithOperatorAndValue operatorAndValue);

	void visit(NullOperatorAndValue operatorAndValue);

	void visit(InOperatorAndValue operatorAndValue);

}
