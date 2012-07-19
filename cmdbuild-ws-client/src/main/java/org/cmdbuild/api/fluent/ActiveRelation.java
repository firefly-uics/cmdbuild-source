package org.cmdbuild.api.fluent;

abstract class ActiveRelation extends Relation {

	private final FluentApiExecutor executor;

	public ActiveRelation(final FluentApiExecutor executor) {
		this.executor = executor;

	}

	protected FluentApiExecutor executor() {
		return executor;
	}

}
