package org.cmdbuild.logic.commands;

import static org.cmdbuild.dao.query.clause.AnyDomain.anyDomain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;

public class GetRelationList extends AbstractGetRelation {

	public GetRelationList(final CMDataView view) {
		super(view);
	}

	public GetRelationListResponse exec(final Card src, final DomainWithSource domainWithSource) {
		Validate.notNull(src);
		final CMDomain domain = getQueryDomain(domainWithSource);
		final CMQueryResult relationList = getRelationQuery(src, domain).run();
		return serializeResponse(relationList);
	}

	private CMDomain getQueryDomain(final DomainWithSource domainWithSource) {
		final CMDomain dom;
		if (domainWithSource != null) {
			dom = view.findDomain(domainWithSource.domainId);
			Validate.notNull(dom);
		} else {
			dom = anyDomain();
		}
		return dom;
	}

	private GetRelationListResponse serializeResponse(final CMQueryResult relationList) {
		final GetRelationListResponse out = new GetRelationListResponse();
		for (CMQueryRow row : relationList) {
			final CMCard dst = row.getCard(DST_ALIAS);
			final QueryRelation rel = row.getRelation(DOM_ALIAS);
			out.addRelation(rel, dst);
		}
		return out;
	}

	public static class GetRelationListResponse implements Iterable<DomainInfo> {
		private final List<DomainInfo> domainInfos;

		private GetRelationListResponse() {
			domainInfos = new ArrayList<DomainInfo>();
		}

		private void addRelation(final QueryRelation rel, final CMCard dst) {
			final RelationInfo ri = new RelationInfo(rel, dst);
			getOrCreateDomainInfo(rel.getQueryDomain()).addRelationInfo(ri);
		}

		private DomainInfo getOrCreateDomainInfo(final QueryDomain qd) {
			for (DomainInfo di : domainInfos) {
				if (di.getQueryDomain().equals(qd)) {
					return di;
				}
			}
			return addDomainInfo(qd);
		}

		private DomainInfo addDomainInfo(final QueryDomain qd) {
			final DomainInfo di = new DomainInfo(qd);
			domainInfos.add(di);
			return di;
		}

		@Override
		public Iterator<DomainInfo> iterator() {
			return domainInfos.iterator();
		}
	}

	public static class DomainInfo implements Iterable<RelationInfo> {
		private QueryDomain querydomain;
		private List<RelationInfo> relations;

		private DomainInfo(final QueryDomain queryDomain) {
			this.querydomain = queryDomain;
			this.relations = new ArrayList<RelationInfo>();
		}

		public QueryDomain getQueryDomain() {
			return querydomain;
		}

		private void addRelationInfo(final RelationInfo ri) {
			relations.add(ri);
		}

		public String getDescription() {
			return querydomain.getDescription();
		}

		@Override
		public Iterator<RelationInfo> iterator() {
			return relations.iterator();
		}
	}
}
