package org.cmdbuild.dao.query.clause;

public class OrderByClause {
	public static enum Direction {
		ASC,
		DESC;
	}

	private QueryAttribute attribute;
	private Direction direction;

	public OrderByClause(QueryAttribute attribute, Direction direction) {
		this.attribute = attribute;
		this.direction = direction;
	}

	public QueryAttribute getAttribute() {
		return attribute;
	}

	public void setAttribute(QueryAttribute attribute) {
		this.attribute = attribute;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}
}
