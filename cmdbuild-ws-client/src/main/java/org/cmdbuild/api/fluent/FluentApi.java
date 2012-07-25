package org.cmdbuild.api.fluent;

public class FluentApi {

	private final FluentApiExecutor executor;

	public FluentApi(final FluentApiExecutor executor) {
		this.executor = executor;
	}

	public NewCard newCard(final String className) {
		return new NewCard(executor, className);
	}

	public ExistingCard existingCard(final String className, final int id) {
		return new ExistingCard(executor, className, id);
	}

	public NewRelation newRelation(final String domainName) {
		return new NewRelation(executor, domainName);
	}

	public ExistingRelation existingRelation(final String domainName) {
		return new ExistingRelation(executor, domainName);
	}

	public QueryClass queryClass(final String className) {
		return new QueryClass(executor, className);
	}

	public CallFunction callFunction(final String functionName) {
		return new CallFunction(executor, functionName);
	}

	public CreateReport createReport(final String title, final String format) {
		return new CreateReport(executor, title, format);
	}

}
