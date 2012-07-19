package org.cmdbuild.api.fluent;

abstract class ActiveCard extends Card {

	private final FluentApiExecutor executor;

	public ActiveCard(final FluentApiExecutor executor) {
		this.executor = executor;

	}

	protected FluentApiExecutor executor() {
		return executor;
	}

}
