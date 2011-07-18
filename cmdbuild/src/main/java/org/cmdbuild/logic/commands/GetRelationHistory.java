package org.cmdbuild.logic.commands;

import static org.cmdbuild.dao.query.clause.AnyDomain.anyDomain;
import static org.cmdbuild.dao.query.clause.DomainHistory.history;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.LogicDTO.Card;

public class GetRelationHistory extends AbstractGetRelation {

	public GetRelationHistory(final CMDataView view) {
		super(view);
	}

	public GetRelationHistoryResponse exec(final Card src) {
		Validate.notNull(src);
		final CMDomain domain = history(anyDomain());
		final CMQueryResult relationList = getRelationQuery(src, domain).run();
		return serializeResponse(relationList);
	}

	private GetRelationHistoryResponse serializeResponse(CMQueryResult relationList) {
		final GetRelationHistoryResponse out = new GetRelationHistoryResponse();
		for (CMQueryRow row : relationList) {
			final QueryRelation rel = row.getRelation(DOM_ALIAS);
			if (rel.getRelation().getEndDate() == null) { // Skip active relations
				// TODO Use "HISTORY ONLY" to exclude them in the query
				continue;
			}
			final CMCard dst = row.getCard(DST_ALIAS);
			out.addRelation(rel, dst);
		}
		return out;
	}

	public static class GetRelationHistoryResponse implements Iterable<RelationInfo> {

		private List<RelationInfo> relations;

		private GetRelationHistoryResponse() {
			this.relations = new ArrayList<RelationInfo>();
		}

		private void addRelation(final QueryRelation rel, final CMCard dst) {
			final RelationInfo ri = new RelationInfo(rel, dst);
			relations.add(ri);
		}

		@Override
		public Iterator<RelationInfo> iterator() {
			return relations.iterator();
		}
	}
}
