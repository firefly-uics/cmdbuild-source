package org.cmdbuild.dao.driver.postgres;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.join;

import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.quote.EntryTypeQuoter;
import org.cmdbuild.dao.driver.postgres.quote.IdentQuoter;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.google.common.collect.Lists;

public class EntryInsertCommand extends EntryCommand {

	/**
	 * NOTE: this field is misleading. If we are inserting a relation, also some
	 * system attributes are included
	 * 
	 * @see {@link EntryCommand#userAttributesFor(DBEntry)}
	 */
	private final List<AttributeValueType> attributesToBeInserted;
	private PreparedStatement ps;
	private int i = 1;

	public EntryInsertCommand(final JdbcTemplate jdbcTemplate, final DBEntry entry) {
		super(jdbcTemplate, entry);
		attributesToBeInserted = userAttributesFor(entry);
		// Note: don't change this order
		systemDomainAttributes = Lists.newArrayList(SystemAttributes.DomainId1, //
				SystemAttributes.ClassId1, //
				SystemAttributes.DomainId2, //
				SystemAttributes.ClassId2);
	}

	private List<String> userAttributeNames() {
		final List<String> realUserAttributes = Lists.newArrayList();
		for (final AttributeValueType attributeValueType : attributesToBeInserted) {
			if (!isSystemDomainAttribute(attributeValueType.getName())) {
				realUserAttributes.add(attributeValueType.getName());
			}
		}
		return realUserAttributes;
	}

	private boolean isSystemDomainAttribute(final String attributeName) {
		for (final SystemAttributes sysAttr : systemDomainAttributes) {
			if (attributeName.equals(sysAttr.getDBName())) {
				return true;
			}
		}
		return false;
	}

	public Long executeAndReturnKey() {
		final String insertStatement = buildInsertStatement();

		final KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate().update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
				ps = connection.prepareStatement(insertStatement, new String[] { "Id" });
				for (final AttributeValueType avt : attributesToBeInserted) {
					avt.getType().accept(new PreparedStatementParametersFiller());
				}
				return ps;
			}
		}, keyHolder);
		return keyHolder.getKey().longValue();
	}

	private String buildInsertStatement() {
		final String insertStatement = format("INSERT INTO %s (%s) VALUES (%s)", //
				EntryTypeQuoter.quote(entry().getType()), //
				buildQuotedIfNeededAttributeNamesList(), //
				buildQuestionMarkValuesList());
		return insertStatement;
	}

	private String buildQuestionMarkValuesList() {
		final List<String> questionMarksWithSqlCast = Lists.newArrayList();
		for (final String attributeName : userAttributeNames()) {
			final CMEntryType entryType = entry().getType();
			String sqlCast;
			final CMAttributeType<?> userAttributeType = entryType.getAttribute(attributeName).getType();
			sqlCast = SqlType.getSqlType(userAttributeType).sqlCast();
			if (sqlCast != null) {
				sqlCast = "::" + sqlCast;
			} else {
				sqlCast = "";
			}
			questionMarksWithSqlCast.add("?" + sqlCast);
		}
		String questionMarkList = join(questionMarksWithSqlCast, ", ");

		if (entry().getType() instanceof CMDomain) {
			for (final SystemAttributes domSysAttribute : systemDomainAttributes) {
				questionMarkList = (!questionMarkList.isEmpty()) ? questionMarkList + ", " : questionMarkList + "";
				questionMarkList = questionMarkList + "?";
				if (domSysAttribute.getCastSuffix() != null) {
					questionMarkList = questionMarkList + "::" + domSysAttribute.getCastSuffix();
				}
			}
		}
		return questionMarkList;
	}

	private String buildQuotedIfNeededAttributeNamesList() {
		final List<String> userAttributeNames = Lists.newArrayList();
		for (final String attributeName : userAttributeNames()) {
			userAttributeNames.add(IdentQuoter.quote(attributeName));
		}
		String namesList = join(userAttributeNames, ", ");
		if (entry().getType() instanceof CMDomain) {
			for (final SystemAttributes domSysAttribute : systemDomainAttributes) {
				namesList = (!namesList.isEmpty()) ? namesList + ", " : namesList + "";
				namesList = namesList + IdentQuoter.quote(domSysAttribute.getDBName());
			}
		}
		return namesList;
	}

	private class PreparedStatementParametersFiller implements CMAttributeTypeVisitor {

		@Override
		public void visit(final BooleanAttributeType attributeType) {
			try {
				final Object value = attributesToBeInserted.get(i - 1).getValue();
				if (value != null) {
					final Boolean castValue = (Boolean) value;
					ps.setBoolean(i, castValue);
				} else {
					ps.setObject(i, null);
				}
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final CharAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final DateAttributeType attributeType) {
			try {
				final Object value = attributesToBeInserted.get(i - 1).getValue();
				if (value != null) {
					final Date castValue = (Date) value;
					ps.setDate(i, castValue);
				} else {
					ps.setObject(i, null);
				}
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final DateTimeAttributeType attributeType) {
			try {
				final Object value = attributesToBeInserted.get(i - 1).getValue();
				if (value != null) {
					final Date castValue = (Date) value;
					ps.setDate(i, castValue);
				} else {
					ps.setObject(i, null);
				}
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final DecimalAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final DoubleAttributeType attributeType) {
			try {
				final Object value = attributesToBeInserted.get(i - 1).getValue();
				if (value != null) {
					final Double castValue = (Double) value;
					ps.setDouble(i, castValue);
				} else {
					ps.setObject(i, null);
				}
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final EntryTypeAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final ForeignKeyAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final IntegerAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final IpAddressAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final LookupAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final ReferenceAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final StringArrayAttributeType attributeType) {
			try {
				final Connection connection = ps.getConnection();
				String[] value = attributeType.convertValue(attributesToBeInserted.get(i - 1).getValue());
				if (value == null) {
					value = new String[0];
				}
				final Array array = connection.createArrayOf(SqlType.varchar.name(), value);
				ps.setArray(i, array);
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final StringAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final TextAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(final TimeAttributeType attributeType) {
			try {
				final Object value = attributesToBeInserted.get(i - 1).getValue();
				if (value != null) {
					final java.sql.Time castValue = (java.sql.Time) value;
					ps.setTime(i, castValue);
				} else {
					ps.setObject(i, null);
				}
				i++;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

	}
}
