package org.cmdbuild.logic.commands;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.AnyDomain.anyDomain;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause.Operator;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.joda.time.DateTime;

public class GetRelationList {

	// TODO Change Code, Description, Id with something meaningful
	private static final String ID = org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
	private static final String CODE = org.cmdbuild.dao.driver.postgres.Const.CODE_ATTRIBUTE;
	private static final String DESCRIPTION = org.cmdbuild.dao.driver.postgres.Const.DESCRIPTION_ATTRIBUTE;

	private static final Alias DOM_ALIAS = Alias.as("DOM");
	private static final Alias DST_ALIAS = Alias.as("DST");

	final CMDataView view;

	public GetRelationList(final CMDataView view) {
		this.view = view;
	}

	public GetRelationListResponse exec(final Card src, final DomainWithSource dom) {
		final CMClass srcCardType = getCardType(src);
		final CMDomain domain = getQueryDomain(dom); // TODO

		final CMQueryResult relationList = view
			.select(
				anyAttribute(DOM_ALIAS),
				attribute(DST_ALIAS, CODE), attribute(DST_ALIAS, DESCRIPTION))
			.from(srcCardType)
			.join(anyClass(), as(DST_ALIAS), over(domain, as(DOM_ALIAS)))
			.where(attribute(srcCardType, ID), Operator.EQUALS, src.cardId)
			.run();

		final GetRelationListResponse out = new GetRelationListResponse();
		for (CMQueryRow row : relationList) {
			final CMCard dst = row.getCard(DST_ALIAS);
			final QueryRelation rel = row.getRelation(DOM_ALIAS);
			out.addRelation(rel, dst);
		}
		return out;
	}

	private CMDomain getQueryDomain(final DomainWithSource domainWithSource) {
		final CMDomain dom;
		if (domainWithSource != null) {
			dom = view.findDomain(domainWithSource.domainId);
		} else {
			dom = anyDomain();
		}
		Validate.notNull(dom);
		return dom;
	}

	private CMClass getCardType(final Card src) {
		final CMClass type = view.findClass(src.classId);
		Validate.notNull(type);
		return type;
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

	public static class RelationInfo {

		private final QueryRelation rel;
		private final CMCard dst;

		private RelationInfo(final QueryRelation rel, final CMCard dst) {
			this.rel = rel;
			this.dst = dst;
		}

		public String getTargetDescription() {
			return ObjectUtils.toString(dst.get("Description"));
		}

		public String getTargetCode() {
			return ObjectUtils.toString(dst.get("Code"));
		}

		public Object getTargetId() {
			return dst.getId();
		}

		public CMClass getTargetType() {
			return dst.getType();
		}

		public Object getRelationId() {
			return rel.getRelation().getId();
		}

		public DateTime getRelationBeginDate() {
			return rel.getRelation().getBeginDate();
		}

		public Iterable<Map.Entry<String, Object>> getRelationAttributes() {
			return rel.getRelation().getValues();
		}
	}
}
