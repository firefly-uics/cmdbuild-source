package org.cmdbuild.api.fluent;

public class FluentApi {

	private final FluentApiExecutor executor;

	public FluentApi(final FluentApiExecutor executor) {
		this.executor = executor;
	}

	public NewCard newCard() {
		return new NewCard(executor);
	}

	public ExistingCard existingCard() {
		return new ExistingCard(executor);
	}

	public NewRelation newRelation() {
		return new NewRelation(executor);
	}

	public ExistingRelation existingRelation() {
		return new ExistingRelation(executor);
	}

}
