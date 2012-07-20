package org.cmdbuild.api.fluent;

public class FluentApi {

	private final FluentApiExecutor executor;

	public FluentApi(final FluentApiExecutor executor) {
		this.executor = executor;
	}

	public NewCard newCard(final String className) {
		return new NewCard(className, executor);
	}

	public ExistingCard existingCard(final String className, final int id) {
		return new ExistingCard(className, id, executor);
	}

	public NewRelation newRelation() {
		return new NewRelation(executor);
	}

	public ExistingRelation existingRelation() {
		return new ExistingRelation(executor);
	}

	public QueryClass queryClass(final String className) {
		return new QueryClass(className, executor);
	}

}
