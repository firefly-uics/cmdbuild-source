package org.cmdbuild.dao.driver.postgres;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.join;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteIdent;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteType;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.GeometryAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
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
		List<String> realUserAttributes = Lists.newArrayList();
		for (AttributeValueType attributeValueType : attributesToBeInserted) {
			if (!isSystemDomainAttribute(attributeValueType.getName())) {
				realUserAttributes.add(attributeValueType.getName());
			}
		}
		return realUserAttributes;
	}

	private boolean isSystemDomainAttribute(String attributeName) {
		for (SystemAttributes sysAttr : systemDomainAttributes) {
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
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				ps = connection.prepareStatement(insertStatement, new String[] { "Id" });
				for (AttributeValueType avt : attributesToBeInserted) {
					avt.getType().accept(new AttributeTypeVisitor());
				}
				return ps;
			}
		}, keyHolder);
		return keyHolder.getKey().longValue();
	}

	private String buildInsertStatement() {
		final String insertStatement = format("INSERT INTO %s (%s) VALUES (%s)", //
				quoteType(entry().getType()), //
				buildAttributeNamesList(), //
				buildQuestionMarkValuesList());
		return insertStatement;
	}

	private String buildQuestionMarkValuesList() {
		List<String> questionMarksWithSqlCast = Lists.newArrayList();
		for (String attributeName : userAttributeNames()) {
			CMEntryType entryType = entry().getType();
			String sqlCast;
			CMAttributeType<?> userAttributeType = entryType.getAttribute(attributeName).getType();
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
			for (SystemAttributes domSysAttribute : systemDomainAttributes) {
				questionMarkList = (!questionMarkList.isEmpty()) ? questionMarkList + ", " : questionMarkList + "";
				questionMarkList = questionMarkList + "?";
				if (domSysAttribute.getCastSuffix() != null) {
					questionMarkList = questionMarkList + "::" + domSysAttribute.getCastSuffix();
				}
			}
		}
		return questionMarkList;
	}

	private String buildAttributeNamesList() {
		List<String> userAttributeNames = Lists.newArrayList();
		for (String attributeName : userAttributeNames()) {
			userAttributeNames.add(quoteIdent(attributeName));
		}
		String namesList = join(userAttributeNames, ", ");
		if (entry().getType() instanceof CMDomain) {
			for (SystemAttributes domSysAttribute : systemDomainAttributes) {
				namesList = (!namesList.isEmpty()) ? namesList + ", " : namesList + "";
				namesList = namesList + quoteIdent(domSysAttribute.getDBName());
			}
		}
		return namesList;
	}

	private class AttributeTypeVisitor implements CMAttributeTypeVisitor {

		@Override
		public void visit(BooleanAttributeType attributeType) {
			try {
				Object value = attributesToBeInserted.get(i - 1).getValue();
				if (value != null) {
					Boolean castValue = (Boolean) value;
					ps.setBoolean(i, castValue);
				} else {
					ps.setObject(i, null);
				}
				i++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(EntryTypeAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(DateTimeAttributeType attributeType) {
			try {
				Object value = attributesToBeInserted.get(i - 1).getValue();
				if (value != null) {
					Date castValue = (Date) value;
					ps.setDate(i, castValue);
				} else {
					ps.setObject(i, null);
				}
				i++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(DateAttributeType attributeType) {
			try {
				Object value = attributesToBeInserted.get(i - 1).getValue();
				if (value != null) {
					Date castValue = (Date) value;
					ps.setDate(i, castValue);
				} else {
					ps.setObject(i, null);
				}
				i++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(DecimalAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(DoubleAttributeType attributeType) {
			try {
				Object value = attributesToBeInserted.get(i - 1).getValue();
				if (value != null) {
					Double castValue = (Double) value;
					ps.setDouble(i, castValue);
				} else {
					ps.setObject(i, null);
				}
				i++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(ForeignKeyAttributeType attributeType) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void visit(GeometryAttributeType attributeType) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void visit(IntegerAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(IpAddressAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(LookupAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(ReferenceAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(StringAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(TextAttributeType attributeType) {
			try {
				ps.setObject(i, attributesToBeInserted.get(i - 1).getValue());
				i++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void visit(TimeAttributeType attributeType) {
			try {
				Object value = attributesToBeInserted.get(i - 1).getValue();
				if (value != null) {
					Date castValue = (Date) value;
					ps.setDate(i, castValue);
				} else {
					ps.setObject(i, null);
				}
				i++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}
}
