package org.cmdbuild.api.fluent;

public class FluentApi {

	private final FluentApiExecutor executor;

	public FluentApi(final FluentApiExecutor executor) {
		this.executor = executor;
	}

	public FluentApiExecutor getExecutor() {
		return executor;
	}

	public NewCard newCard(final String className) {
		return new NewCard(this, className);
	}

	public ExistingCard existingCard(final String className, final int id) {
		return new ExistingCard(this, className, id);
	}

	public NewRelation newRelation(final String domainName) {
		return new NewRelation(this, domainName);
	}

	public ExistingRelation existingRelation(final String domainName) {
		return new ExistingRelation(this, domainName);
	}

	public QueryClass queryClass(final String className) {
		return new QueryClass(this, className);
	}

	public CallFunction callFunction(final String functionName) {
		return new CallFunction(this, functionName);
	}

	public CreateReport createReport(final String title, final String format) {
		return new CreateReport(this, title, format);
	}

	public ActiveQueryRelations queryRelations(final String className, final int id) {
		return new ActiveQueryRelations(this, className, id);
	}

}
