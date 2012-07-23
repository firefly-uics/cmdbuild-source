package org.cmdbuild.api.fluent;

import java.util.List;
import java.util.Map;

public interface FluentApiExecutor {

	CardDescriptor create(NewCard newCard);

	void update(ExistingCard existingCard);

	void delete(ExistingCard existingCard);

	Card fetch(ExistingCard existingCard);

	void create(NewRelation newRelation);

	void delete(ExistingRelation existingRelation);

	List<CardDescriptor> fetch(QueryClass classQuery);

	Map<String, String> execute(CallFunction callFunction);

}
