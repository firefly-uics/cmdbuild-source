package org.cmdbuild.api.fluent;

public interface FluentApiExecutor {

	CardDescriptor create(NewCard newCard);

	void update(ExistingCard existingCard);

	void delete(ExistingCard existingCard);

}
