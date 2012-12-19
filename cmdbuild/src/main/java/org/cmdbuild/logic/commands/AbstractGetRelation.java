package org.cmdbuild.logic.commands;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.LogicDTO.Card;
import org.joda.time.DateTime;

public class AbstractGetRelation {

	// TODO Change Code, Description, Id with something meaningful
	protected static final String ID = org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
	protected static final String CODE = org.cmdbuild.dao.driver.postgres.Const.CODE_ATTRIBUTE;
	protected static final String DESCRIPTION = org.cmdbuild.dao.driver.postgres.Const.DESCRIPTION_ATTRIBUTE;

	protected static final Alias DOM_ALIAS = Alias.as("DOM");
	protected static final Alias DST_ALIAS = Alias.as("DST");

	protected CMDataView view;

	public AbstractGetRelation(final CMDataView view) {
		this.view = view;
	}

	protected QuerySpecsBuilder getRelationQuery(final Card src, final CMDomain domain) {
		final CMClass srcCardType = getCardType(src);
		return view.select(anyAttribute(DOM_ALIAS), attribute(DST_ALIAS, CODE), attribute(DST_ALIAS, DESCRIPTION))
				.from(srcCardType).join(anyClass(), as(DST_ALIAS), over(domain, as(DOM_ALIAS)))
				.where(condition(attribute(srcCardType, ID), eq(src.cardId)));
	}

	protected CMClass getCardType(final Card src) {
		final CMClass type = view.findClassById(src.classId);
		Validate.notNull(type);
		return type;
	}

	public static class RelationInfo {

		private final QueryRelation rel;
		private final CMCard dst;

		protected RelationInfo(final QueryRelation rel, final CMCard dst) {
			this.rel = rel;
			this.dst = dst;
		}

		public String getTargetDescription() {
			return ObjectUtils.toString(dst.get(DESCRIPTION));
		}

		public String getTargetCode() {
			return ObjectUtils.toString(dst.get(CODE));
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

		public DateTime getRelationEndDate() {
			return rel.getRelation().getEndDate();
		}

		public Iterable<Map.Entry<String, Object>> getRelationAttributes() {
			return rel.getRelation().getValues();
		}

		public CMRelation getRelation() {
			return rel.getRelation();
		}
	}
}