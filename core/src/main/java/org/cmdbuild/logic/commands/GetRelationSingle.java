package org.cmdbuild.logic.commands;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.base.Optional;

public class GetRelationSingle extends AbstractGetRelation {

	private static final Optional<RelationInfo> ABSENT = Optional.absent();

	public GetRelationSingle(final CMDataView dataView) {
		super(dataView);
	}

	public Optional<RelationInfo> exec(final CMDomain domain, final Long id) {
		final CMClass source = domain.getClass1();
		final Optional<CMQueryRow> row = from(getRelationQuery(source, domain) //
				.where(condition(attribute(DOM_ALIAS, ID), eq(id))) //
				.run()) //
				.first();
		return row.isPresent() ? Optional.of(relationInfo(row.get())) : ABSENT;
	}

	private RelationInfo relationInfo(final CMQueryRow row) {
		final QueryRelation rel = row.getRelation(DOM_ALIAS);
		final CMCard src = row.getCard(SRC_ALIAS);
		final CMCard dst = row.getCard(DST_ALIAS);
		return new RelationInfo(rel, src, dst);
	}

}
