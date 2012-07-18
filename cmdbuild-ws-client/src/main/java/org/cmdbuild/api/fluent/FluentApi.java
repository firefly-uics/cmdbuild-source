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

}
