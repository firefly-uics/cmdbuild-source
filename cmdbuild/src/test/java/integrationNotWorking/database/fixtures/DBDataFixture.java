package integrationNotWorking.database.fixtures;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.cmdbuild.dao.backend.postgresql.CardQueryBuilder;
import org.cmdbuild.elements.history.TableHistory;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.services.DBService;

@SuppressWarnings("serial")
public class DBDataFixture extends DBFixture {

	protected static final TreeMap<String, String> DESCRIPTION_NULL = new TreeMap<String, String>() {
		{
			put(CardAttributes.Description.toString(), "NULL");
		}
	};

	protected static final String NOT_NULL_TEXT_VALUE = "not null";

	protected static final TreeMap<String, String> DESCRIPTION_NOT_NULL = new TreeMap<String, String>() {
		{
			put(CardAttributes.Description.toString(), "'" + NOT_NULL_TEXT_VALUE + "'");
		}
	};

	protected static final TreeMap<String, String> STATUS_INACTIVE = new TreeMap<String, String>() {
		{
			put(CardAttributes.Status.toString(), "'" + ElementStatus.INACTIVE.value() + "'");
		}
	};

	protected int insertCardRow(final String className) throws SQLException {
		return insertCardRow(className, DESCRIPTION_NULL);
	}

	protected int insertCardRowWithoutClassId(final String className) throws SQLException {
		return insertCardRowWithoutClassId(className, DESCRIPTION_NULL);
	}

	protected int insertCardRow(final String className, final Map<String, String> values) throws SQLException {
		final TreeMap<String, String> valuesWithClassId = new TreeMap<String, String>();
		// TODO: SET A DEFAULT FOR THESE VALUES!
		valuesWithClassId.put(CardAttributes.ClassId.toString(), "'\"" + className + "\"'::regclass"); // TG_RELID
		valuesWithClassId.put(CardAttributes.Status.toString(), "'A'"); // 'A'
																		// on
																		// insert,
																		// 'A'
																		// or
																		// 'N'
																		// on
																		// update
		valuesWithClassId.putAll(values);
		return insertCardRowWithoutClassId(className, valuesWithClassId);
	}

	private int insertCardRowWithoutClassId(final String className, final Map<String, String> values)
			throws SQLException {
		final Connection conn = connection();
		final String insertQuery = String.format(CardQueryBuilder.INSERT, className,
				commaSeparateAddPrefixPostfix(values.keySet(), "\""), commaSeparateAddPrefixPostfix(values.values()));
		final CallableStatement stm = conn.prepareCall(insertQuery);
		final ResultSet rs = stm.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	private String commaSeparateAddPrefixPostfix(final Collection<String> c) {
		return commaSeparateAddPrefixPostfix(c, "");
	}

	private String commaSeparateAddPrefixPostfix(final Collection<String> c, final String pp) {
		final StringBuilder sb = new StringBuilder();
		for (final String s : c) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(pp).append(s).append(pp);
		}
		return sb.toString();
	}

	protected Map<String, Object> getRowCardValues(final String className, final int cardId) throws SQLException {
		final String selectQuery = String.format("SELECT * FROM \"%s\" WHERE \"Id\"=%d", className, cardId);
		return getRowValues(selectQuery);
	}

	protected Map<String, Object> getLastHistoryRowValues(final String className, final int cardId) throws SQLException {
		final String selectQuery = String.format("SELECT * FROM \"%s%s\" WHERE \"CurrentId\"=%d ORDER BY \"%s\" DESC",
				className, TableHistory.HistoryTableSuffix, cardId, TableHistory.EndDateAttribute);
		return getRowValues(selectQuery);
	}

	private Map<String, Object> getRowValues(final String selectQuery) throws SQLException {
		final TreeMap<String, Object> values = new TreeMap<String, Object>();
		final Connection conn = connection();
		final CallableStatement stm = conn.prepareCall(selectQuery);
		final ResultSet rs = stm.executeQuery();
		rs.next();
		final ResultSetMetaData meta = rs.getMetaData();
		for (int i = 1; i <= meta.getColumnCount(); ++i) {
			values.put(meta.getColumnName(i), rs.getObject(i));
		}
		return values;
	}

	protected int countHistoryItems(final String className, final int cardId) throws SQLException {
		final Connection conn = connection();
		final String selectQuery = String.format("SELECT COUNT(*) FROM \"%s%s\" WHERE \"CurrentId\"=%d", className,
				TableHistory.HistoryTableSuffix, cardId);
		final CallableStatement stm = conn.prepareCall(selectQuery);
		final ResultSet rs = stm.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	protected void updateCardRow(final String className, final int cardId, final TreeMap<String, String> values)
			throws SQLException {
		final Connection conn = connection();
		final String updateQuery = String.format("UPDATE \"%s\" SET %s WHERE \"Id\"=%d", className,
				createUpdateValuePart(values), cardId);
		final CallableStatement stm = conn.prepareCall(updateQuery);
		stm.execute();
	}

	private String createUpdateValuePart(final Map<String, String> values) {
		final StringBuilder sb = new StringBuilder();
		for (final Entry<String, String> entry : values.entrySet()) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append("\"").append(entry.getKey()).append("\" = ").append(entry.getValue());
		}
		return sb.toString();
	}

	protected void deleteCardRow(final String className, final int cardId) throws SQLException {
		final Connection conn = connection();
		final String updateQuery = String.format("DELETE FROM \"%s\" WHERE \"Id\"=%d", className, cardId);
		final CallableStatement stm = conn.prepareCall(updateQuery);
		stm.execute();
	}

	protected int insertRelation(final DomainInfo domain, final String class1Name, final int card1Id,
			final String class2Name, final int card2Id) throws SQLException {
		return insertCardRowWithoutClassId(domain.getDBName(),
				createRelationCard(domain, class1Name, card1Id, class2Name, card2Id));
	}

	protected int insertReferenceRelation(final DomainInfo domain, final String sourceClassName,
			final int sourceCardId, final String targetClassName, final int targetCardId) throws SQLException {
		if (domain.getReferenceDirection()) {
			return insertRelation(domain, sourceClassName, sourceCardId, targetClassName, targetCardId);
		} else {
			return insertRelation(domain, targetClassName, targetCardId, sourceClassName, sourceCardId);
		}
	}

	private Map<String, String> createRelationCard(final DomainInfo domain, final String class1Name, final int card1Id,
			final String class2Name, final int card2Id) {
		// TODO: Replace these names...
		final Map<String, String> values = createBasicRelationValues(class1Name, card1Id, class2Name, card2Id);
		values.put("IdDomain", "'\"" + domain.getDBName() + "\"'::regclass");
		values.put(CardAttributes.Status.toString(), "'A'");
		return values;
	}

	private TreeMap<String, String> createBasicRelationValues(final String class1Name, final int card1Id,
			final String class2Name, final int card2Id) {
		final TreeMap<String, String> values = new TreeMap<String, String>();
		values.put("IdClass1", "'\"" + class1Name + "\"'::regclass");
		values.put("IdObj1", Integer.toString(card1Id));
		values.put("IdClass2", "'\"" + class2Name + "\"'::regclass");
		values.put("IdObj2", Integer.toString(card2Id));
		return values;
	}

	protected void updateRelation(final DomainInfo domain, final int relId, final String class1Name, final int card1Id,
			final String class2Name, final int card2Id) throws SQLException {
		updateCardRow(domain.getDBName(), relId, createBasicRelationValues(class1Name, card1Id, class2Name, card2Id));
	}

	protected void updateReferenceRelation(final DomainInfo domain, final int relId, final String sourceClassName,
			final int sourceCardId, final String targetClassName, final int targetCardId) throws SQLException {
		if (domain.getReferenceDirection()) {
			updateRelation(domain, relId, sourceClassName, sourceCardId, targetClassName, targetCardId);
		} else {
			updateRelation(domain, relId, targetClassName, targetCardId, sourceClassName, sourceCardId);
		}
	}

	protected void deleteRelation(final DomainInfo domain, final int relId) throws SQLException {
		deleteCardRow(domain.getDBName(), relId);
	}

	protected void truncateDomain(final DomainInfo domain) throws SQLException {
		truncateTable(domain.getDBName());
	}

	protected void truncateClass(final String className) throws SQLException {
		truncateTable(className);
	}

	private void truncateTable(final String tableName) throws SQLException {
		final Connection conn = connection();
		final CallableStatement stm = conn.prepareCall(String.format("TRUNCATE \"%s\", \"%s%s\"", tableName, tableName,
				TableHistory.HistoryTableSuffix));
		stm.execute();
	}

}
