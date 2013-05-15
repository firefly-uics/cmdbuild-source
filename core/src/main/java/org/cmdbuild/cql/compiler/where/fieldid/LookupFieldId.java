package org.cmdbuild.cql.compiler.where.fieldid;

import org.cmdbuild.cql.CQLBuilderListener.LookupOperator;
import org.cmdbuild.cql.compiler.from.FromElement;

/**
 * An identifier which contains a lookup operator, e.g. parent(),
 * Foo.parent().Code = 'bar' <br>
 * read: the Code of the parent of the Foo lookup field is equals to 'bar'"
 */
public class LookupFieldId implements FieldId {
	FromElement from;
	String id;
	LookupOperator[] operators;
	public LookupFieldId( String id, LookupOperator[] operators, FromElement from ) {
		this.id = id;
		this.operators = operators;
		this.from = from;
	}
	public String getId() {
		return id;
	}
	public LookupOperator[] getOperators() {
		return operators;
	}
	public FromElement getFrom() {
		return from;
	}
	public LookupOperatorTree getTree() {
		LookupOperatorTree out = null;
		for(LookupOperator op : operators) {
			if(out == null){out = new LookupOperatorTree(op.getOperator(),op.getAttributeName());}
			else {
				out.setNext(op.getOperator(), op.getAttributeName());
			}
		}
		return out;
	}
	
	public class LookupOperatorTree {
		LookupOperatorTree child = null;
		String operator = null;
		String attributeName = null;
		public LookupOperatorTree(String operator, String attribute) {
			this.operator = operator;
			this.attributeName = attribute;
		}
		private void setNext(String operator, String attribute) {
			if(child == null) {
				child = new LookupOperatorTree(operator,attribute);
			} else {
				child.setNext(operator, attribute);
			}
		}
		public String getOperator() {
			return operator;
		}
		public String getAttributeName() {
			return attributeName;
		}
		public boolean hasChild() {
			return child != null;
		}
		public LookupOperatorTree getChild() {
			return child;
		}
	}
}
