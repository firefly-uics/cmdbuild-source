package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.BeginDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId1;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId2;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainQuerySource;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.EndDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.IdClass;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Row;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.User;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForSystemAttribute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.constants.Cardinality;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.logging.LoggingSupport;
import org.cmdbuild.dao.driver.postgres.query.ColumnMapper;
import org.cmdbuild.dao.driver.postgres.query.ColumnMapper.EntryTypeAttribute;
import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entry.CardReference;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBFunctionCallOutput;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.DBQueryResult;
import org.cmdbuild.dao.query.DBQueryRow;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

class EntryQueryCommand implements LoggingSupport {

	private final DBDriver driver;
	private final JdbcTemplate jdbcTemplate;
	private final QuerySpecs querySpecs;
	private static final String EXTERNAL_REFERENCE_ATTRIBUTE_ALIAS_PATTERN = "%s#%s#%s#%s";

	EntryQueryCommand(final DBDriver driver, final JdbcTemplate jdbcTemplate, final QuerySpecs querySpecs) {
		this.driver = driver;
		this.jdbcTemplate = jdbcTemplate;
		this.querySpecs = querySpecs;
	}

	public CMQueryResult run() {
		sqlLogger.debug("executing query from '{}'", QuerySpecs.class);
		final QueryCreator qc = new QueryCreator(querySpecs);
		final String query = qc.getQuery();
		final Object[] params = qc.getParams();
		final ResultFiller rch = new ResultFiller(qc.getColumnMapper());
		sqlLogger.debug("query: {}", query);
		sqlLogger.debug("params: {}", Arrays.asList(params));
		jdbcTemplate.query(query, params, rch);
		return rch.getResult();
	}

	private class ResultFiller implements RowCallbackHandler {

		final ColumnMapper columnMapper;

		final long start;
		final long end;

		final DBQueryResult result;

		public ResultFiller(final ColumnMapper columnMapper) {
			this.columnMapper = columnMapper;
			result = new DBQueryResult();
			start = (querySpecs.getOffset() != null) ? querySpecs.getOffset() : 0;
			end = (querySpecs.getLimit() != null) ? start + querySpecs.getLimit() : Integer.MAX_VALUE;
		}

		@Override
		public void processRow(final ResultSet rs) throws SQLException {
			final int rowNum = result.getAndIncrementTotalSize();
			if (start <= rowNum && rowNum < end) {
				final DBQueryRow row = new DBQueryRow();
				createRowNumber(rs, row);
				createBasicCards(rs, row);
				createBasicRelations(rs, row);
				createFunctionCallOutput(rs, row);
				result.add(row);
			}
		}

		private void createRowNumber(final ResultSet rs, final DBQueryRow row) {
			try {
				row.setNumber(rs.getLong(nameForSystemAttribute(querySpecs.getFromClause().getAlias(), Row)));
			} catch (final SQLException e) {
				// ignored
			}
		}

		private void createFunctionCallOutput(final ResultSet rs, final DBQueryRow row) throws SQLException {
			for (final Alias a : columnMapper.getFunctionCallAliases()) {
				final DBFunctionCallOutput out = new DBFunctionCallOutput();
				final CMEntryType hackSinceAFunctionCanAppearOnlyInTheFromClause = querySpecs.getFromClause().getType();
				for (final EntryTypeAttribute eta : columnMapper.getAttributes(a,
						hackSinceAFunctionCanAppearOnlyInTheFromClause)) {
					final Object sqlValue = rs.getObject(eta.index);
					out.set(eta.name, eta.sqlType.sqlToJavaValue(sqlValue));
				}
				row.setFunctionCallOutput(a, out);
			}
		}

		private void createBasicCards(final ResultSet rs, final DBQueryRow row) throws SQLException {
			sqlLogger.trace("creating cards");
			for (final Alias alias : columnMapper.getClassAliases()) {
				sqlLogger.trace("creating card for alias '{}'", alias);
				// Always extract a Long for the Id even if it's integer
				final Long id = rs.getLong(nameForSystemAttribute(alias, Id));
				final Long classId = rs.getLong(nameForSystemAttribute(alias, IdClass));
				final DBClass realClass = driver.findClass(classId);
				if (realClass == null) {
					sqlLogger.trace("class not found for id '{}', skipping creation", classId);
					continue;
				}
				sqlLogger.trace("real class for id '{}' is '{}'", classId, realClass.getIdentifier());
				final DBCard card = DBCard.newInstance(driver, realClass, id);

				card.setUser(rs.getString(nameForSystemAttribute(alias, User)));
				card.setBeginDate(getDateTime(rs, nameForSystemAttribute(alias, BeginDate)));
				card.setEndDate(getDateTime(rs, nameForSystemAttribute(alias, EndDate)));

				addUserAttributes(alias, card, rs);
				row.setCard(alias, card);
			}
		}

		private void createBasicRelations(final ResultSet rs, final DBQueryRow row) throws SQLException {
			for (final Alias alias : columnMapper.getDomainAliases()) {
				final Long id = rs.getLong(nameForSystemAttribute(alias, Id));
				final Long domainId = rs.getLong(nameForSystemAttribute(alias, DomainId));
				final String querySource = rs.getString(nameForSystemAttribute(alias, DomainQuerySource));
				final DBDomain realDomain = driver.findDomain(domainId);
				if (realDomain == null) {
					sqlLogger.trace("domain not found for id '{}', skipping creation", domainId);
					continue;
				}
				final DBRelation relation = DBRelation.newInstance(driver, realDomain, id);

				relation.setUser(rs.getString(nameForSystemAttribute(alias, User)));
				relation.setBeginDate(getDateTime(rs, nameForSystemAttribute(alias, BeginDate)));
				relation.setEndDate(getDateTime(rs, nameForSystemAttribute(alias, EndDate)));
				Long idObject1 = rs.getLong(nameForSystemAttribute(alias, DomainId1));
				Long idObject2 = rs.getLong(nameForSystemAttribute(alias, DomainId2));
				relation.setCard1Id(idObject1);
				relation.setCard2Id(idObject2);

				addUserAttributes(alias, relation, rs);

				final QueryRelation queryRelation = QueryRelation.newInstance(relation, querySource);
				row.setRelation(alias, queryRelation);
			}
		}

		private DateTime getDateTime(final ResultSet rs, final String attributeAlias) throws SQLException {
			try {
				final java.sql.Timestamp ts = rs.getTimestamp(attributeAlias);
				if (ts != null) {
					return new DateTime(ts.getTime());
				} else {
					return null;
				}
			} catch (final SQLException ex) {
				return null;
			}
		}

		private void addUserAttributes(final Alias typeAlias, final DBEntry entry, final ResultSet rs)
				throws SQLException {
			sqlLogger.trace("adding user attributes for entry of type '{}' with alias '{}'", //
					entry.getType().getIdentifier(), typeAlias);
			for (final EntryTypeAttribute attribute : columnMapper.getAttributes(typeAlias, entry.getType())) {
				if (attribute.name != null) {
					final DBAttribute dbAttribute = entry.getType().getAttribute(attribute.name);
					if (isExternalReference(dbAttribute)) {
						Long externalReferenceId = rs.getLong(attribute.index);
						final String referenceAttributeAlias = buildReferenceAttributeAlias(dbAttribute);
						String externalReferenceDescription = rs.getString(referenceAttributeAlias);
						final CardReference cardReference = new CardReference(externalReferenceId,
								externalReferenceDescription);
						entry.setOnly(attribute.name, cardReference);
					} else {
						final Object sqlValue = rs.getObject(attribute.index);
						entry.setOnly(attribute.name, attribute.sqlType.sqlToJavaValue(sqlValue));
					}
				} else {
					// skipping, not belonging to this entry type
				}
			}
		}

		private boolean isExternalReference(final DBAttribute attribute) {
			final CMAttributeType<?> attributeType = attribute.getType();
			return attributeType instanceof LookupAttributeType || //
					attributeType instanceof ReferenceAttributeType || //
					attributeType instanceof ForeignKeyAttributeType;
		}

		/**
		 * TODO: create a visitor and use it anywhere
		 */
		private String buildReferenceAttributeAlias(final DBAttribute attribute) {
			final CMAttributeType<?> attributeType = attribute.getType();
			final String referencedClassName;
			if (attributeType instanceof LookupAttributeType) {
				referencedClassName = Constants.LOOKUP_CLASS_NAME;
			} else if (attributeType instanceof ReferenceAttributeType) {
				final ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType) attributeType;
				final DBDomain domain = driver.findDomain(referenceAttributeType.getDomainName());
				if (domain.getCardinality().equals(Cardinality.CARDINALITY_1N.value())) {
					referencedClassName = domain.getClass1().getName();
				} else {
					referencedClassName = domain.getClass2().getName();
				}
			} else if (attributeType instanceof ForeignKeyAttributeType) {
				ForeignKeyAttributeType foreignKeyAttributeType = (ForeignKeyAttributeType) attributeType;
				referencedClassName = foreignKeyAttributeType.getForeignKeyDestinationClassName();
			} else { // should never happen
				referencedClassName = StringUtils.EMPTY;
			}
			return String.format(EXTERNAL_REFERENCE_ATTRIBUTE_ALIAS_PATTERN, referencedClassName, querySpecs.getFromClause().getType().getName(),
					attribute.getName(), Constants.DESCRIPTION_ATTRIBUTE);
		}

		private CMQueryResult getResult() {
			return result;
		}
	}

}
