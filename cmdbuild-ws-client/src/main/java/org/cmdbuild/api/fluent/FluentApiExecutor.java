package org.cmdbuild.api.fluent;

import java.util.List;
import java.util.Map;

public interface FluentApiExecutor {

	enum AdvanceProcess {
		YES, NO
	}

	CardDescriptor create(Card card);

	void update(Card card);

	void delete(Card card);

	Card fetch(Card card);

	List<Card> fetchCards(Card card);

	void create(Relation relation);

	void delete(Relation relation);

	List<Relation> fetch(RelationsQuery query);

	Map<String, Object> execute(Function function);

	DownloadedReport download(Report report);

	ProcessInstanceDescriptor createProcessInstance(Card processCard, AdvanceProcess advance);

}
