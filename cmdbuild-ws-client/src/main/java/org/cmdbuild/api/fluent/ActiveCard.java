package org.cmdbuild.api.fluent;

abstract class ActiveCard extends Card {

	private final FluentApiExecutor executor;

	public ActiveCard(final String className, final Integer id, final FluentApiExecutor executor) {
		super(className, id);
		this.executor = executor;
	}

	protected FluentApiExecutor executor() {
		return executor;
	}

}
