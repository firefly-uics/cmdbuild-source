package org.cmdbuild.dao.driver.postgres.query;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.join;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.BeginDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId1;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId2;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainQuerySource;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.EndDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.IdClass;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.User;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForSystemAttribute;
import static org.cmdbuild.dao.query.clause.alias.NameAlias.as;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.quote.AliasQuoter;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.alias.Alias;

public class SelectPartCreator extends PartCreator {

	private static final String SELECT = "SELECT";
	private static final String DISTINCT_ON = "DISTINCT ON";
	public static final String ATTRIBUTES_SEPARATOR = ", ";
	private static final String LF = "\n";

	private final QuerySpecs querySpecs;
	private final SelectAttributesExpressions selectAttributesExpressions;

	public SelectPartCreator(final QuerySpecs querySpecs, final ColumnMapper columnMapper,
			final SelectAttributesExpressions selectAttributesExpressions) {
		this.querySpecs = querySpecs;
		this.selectAttributesExpressions = selectAttributesExpressions;

		for (final Alias alias : columnMapper.getClassAliases()) {
			addToSelect(alias, IdClass);
			addToSelect(alias, Id);
			addToSelect(alias, User);
			addToSelect(alias, BeginDate);
			if (querySpecs.getFromClause().isHistory()) {
				/**
				 * aliases for join clauses are not added here (e.g. the EndDate
				 * attribute is not present in a referenced table / lookup table
				 * when there is one or more direct join)
				 */
				if (alias.toString().equals(querySpecs.getFromClause().getType().getName())) {
					addToSelect(alias, EndDate);
				}
			}
		}

		for (final Alias alias : columnMapper.getDomainAliases()) {
			addToSelect(alias, DomainId);
			addToSelect(alias, DomainQuerySource);
			addToSelect(alias, Id);
			addToSelect(alias, User);
			addToSelect(alias, BeginDate);
			addToSelect(alias, EndDate);
			addToSelect(alias, DomainId1);
			addToSelect(alias, DomainId2);
		}

		sb.append(SELECT) //
				.append(distinct()) //
				.append(LF) //
				.append(join(selectAttributesExpressions.getExpressions().iterator(), ATTRIBUTES_SEPARATOR));
	}

	private void addToSelect(final Alias typeAlias, final SystemAttributes systemAttribute) {
		selectAttributesExpressions.add( //
				typeAlias, //
				systemAttribute.getDBName(), //
				systemAttribute.getCastSuffix(), //
				as(nameForSystemAttribute(typeAlias, systemAttribute)));
	}

	private String distinct() {
		return querySpecs.distinct() ? //
		format(" %s (%s) ", //
				DISTINCT_ON, //
				AliasQuoter.quote(as(nameForSystemAttribute(querySpecs.getFromClause().getAlias(), Id)))) //
				: EMPTY;
	}

}
