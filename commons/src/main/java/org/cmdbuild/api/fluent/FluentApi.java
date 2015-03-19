package org.cmdbuild.api.fluent;

public class FluentApi {

	private final FluentApiExecutor executor;

	public FluentApi(final FluentApiExecutor executor) {
		this.executor = executor;
	}

	public NewCard newCard(final String className) {
		return new NewCard(executor, className);
	}

	public ExistingCard existingCard(final CardDescriptor descriptor) {
		return new ExistingCard(executor, descriptor.getClassName(), descriptor.getId());
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

	public FunctionCall callFunction(final String functionName) {
		return new FunctionCall(executor, functionName);
	}

	public CreateReport createReport(final String title, final String format) {
		return new CreateReport(executor, title, format);
	}

	public ActiveQueryRelations queryRelations(final CardDescriptor descriptor) {
		return new ActiveQueryRelations(executor, descriptor.getClassName(), descriptor.getId());
	}

	public ActiveQueryRelations queryRelations(final String className, final int id) {
		return new ActiveQueryRelations(executor, className, id);
	}

	public NewProcessInstance newProcessInstance(final String processClassName) {
		return new NewProcessInstance(executor, processClassName);
	}

	public ExistingProcessInstance existingProcessInstance(final String processClassName, final int processId) {
		return new ExistingProcessInstance(executor, processClassName, processId);
	}

	public QueryAllLookup queryLookup(final String type) {
		return new QueryAllLookup(executor, type);
	}

}
