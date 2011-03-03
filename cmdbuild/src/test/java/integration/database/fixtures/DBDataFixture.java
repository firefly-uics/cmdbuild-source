package integration.database.fixtures;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.cmdbuild.dao.backend.postgresql.CardQueryBuilder;
import org.cmdbuild.elements.history.TableHistory;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.services.DBService;

@SuppressWarnings("serial")
public class DBDataFixture extends DBFixture {

	protected static final TreeMap<String,String> DESCRIPTION_NULL = new TreeMap<String,String>() {{
		put(CardAttributes.Description.toString(), "NULL");
	}};

	protected static final String NOT_NULL_TEXT_VALUE = "not null";

	protected static final TreeMap<String,String> DESCRIPTION_NOT_NULL = new TreeMap<String,String>() {{
		put(CardAttributes.Description.toString(), "'"+NOT_NULL_TEXT_VALUE+"'");
	}};

	protected static final TreeMap<String,String> STATUS_INACTIVE = new TreeMap<String,String>() {{
		put(CardAttributes.Status.toString(), "'"+ElementStatus.INACTIVE.value()+"'");
	}};

	protected int insertCardRow(String className) throws SQLException {
		return insertCardRow(className, DESCRIPTION_NULL);
	}

	protected int insertCardRowWithoutClassId(String className) throws SQLException {
		return insertCardRowWithoutClassId(className, DESCRIPTION_NULL);
	}

	protected int insertCardRow(String className, Map<String,String> values) throws SQLException {
		TreeMap<String,String> valuesWithClassId = new TreeMap<String,String>();
		// TODO: SET A DEFAULT FOR THESE VALUES!
		valuesWithClassId.put(CardAttributes.ClassId.toString(), "'\""+className+"\"'::regclass"); // TG_RELID
		valuesWithClassId.put(CardAttributes.Status.toString(), "'A'"); // 'A' on insert, 'A' or 'N' on update
		valuesWithClassId.putAll(values);
		return insertCardRowWithoutClassId(className, valuesWithClassId);
	}

	private int insertCardRowWithoutClassId(String className, Map<String, String> values) throws SQLException {
		Connection conn = DBService.getConnection();
		String insertQuery = String.format(CardQueryBuilder.INSERT,
				className, commaSeparateAddPrefixPostfix(values.keySet(), "\""), commaSeparateAddPrefixPostfix(values.values()));
		CallableStatement stm = conn.prepareCall(insertQuery);
		ResultSet rs = stm.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	private String commaSeparateAddPrefixPostfix(Collection<String> c) {
		return commaSeparateAddPrefixPostfix(c, "");
	}

	private String commaSeparateAddPrefixPostfix(Collection<String> c, String pp) {
		StringBuilder sb = new StringBuilder();
		for (String s : c) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(pp).append(s).append(pp);
		}
		return sb.toString();
	}

	protected Map<String, Object> getRowCardValues(String className, int cardId) throws SQLException {
		String selectQuery = String.format("SELECT * FROM \"%s\" WHERE \"Id\"=%d", className, cardId);
		return getRowValues(selectQuery);
	}

	protected Map<String, Object> getLastHistoryRowValues(String className, int cardId) throws SQLException {
		String selectQuery = String.format("SELECT * FROM \"%s%s\" WHERE \"CurrentId\"=%d ORDER BY \"%s\" DESC",
				className, TableHistory.HistoryTableSuffix, cardId, TableHistory.EndDateAttribute);
		return getRowValues(selectQuery);
	}

	private Map<String, Object> getRowValues(String selectQuery) throws SQLException {
		TreeMap<String, Object> values = new TreeMap<String, Object>();
		Connection conn = DBService.getConnection();
		CallableStatement stm = conn.prepareCall(selectQuery);
		ResultSet rs = stm.executeQuery();
		rs.next();
		ResultSetMetaData meta = rs.getMetaData();
		for (int i=1; i<=meta.getColumnCount(); ++i) {
			values.put(meta.getColumnName(i), rs.getObject(i));
		}
		return values;
	}

	protected int countHistoryItems(String className, int cardId) throws SQLException {
		Connection conn = DBService.getConnection();
		String selectQuery = String.format("SELECT COUNT(*) FROM \"%s%s\" WHERE \"CurrentId\"=%d",
				className, TableHistory.HistoryTableSuffix, cardId);
		CallableStatement stm = conn.prepareCall(selectQuery);
		ResultSet rs = stm.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	protected void updateCardRow(String className, int cardId, TreeMap<String, String> values) throws SQLException {
		Connection conn = DBService.getConnection();
		String updateQuery = String.format("UPDATE \"%s\" SET %s WHERE \"Id\"=%d",
				className, createUpdateValuePart(values), cardId);
		CallableStatement stm = conn.prepareCall(updateQuery);
		stm.execute();
	}

	private String createUpdateValuePart(Map<String, String> values) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String,String> entry : values.entrySet()) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append("\"").append(entry.getKey()).append("\" = ").append(entry.getValue());
		}
		return sb.toString();
	}

	protected void deleteCardRow(String className, int cardId) throws SQLException {
		Connection conn = DBService.getConnection();
		String updateQuery = String.format("DELETE FROM \"%s\" WHERE \"Id\"=%d",
				className, cardId);
		CallableStatement stm = conn.prepareCall(updateQuery);
		stm.execute();
	}

	protected int insertRelation(DomainInfo domain, String class1Name, int card1Id, String class2Name, int card2Id) throws SQLException {
		return insertCardRowWithoutClassId(domain.getDBName(),
				createRelationCard(domain, class1Name, card1Id, class2Name, card2Id));
	}

	protected int insertReferenceRelation(DomainInfo domain, String sourceClassName, int sourceCardId, String targetClassName, int targetCardId) throws SQLException {
		if (domain.getReferenceDirection()) {
			return insertRelation(domain, sourceClassName, sourceCardId, targetClassName, targetCardId);
		} else {
			return insertRelation(domain, targetClassName, targetCardId, sourceClassName, sourceCardId);
		}
	}

	private Map<String,String> createRelationCard(DomainInfo domain, String class1Name, int card1Id, String class2Name, int card2Id) {
		// TODO: Replace these names...
		Map<String, String> values = createBasicRelationValues(class1Name, card1Id, class2Name, card2Id);
		values.put("IdDomain", "'\""+domain.getDBName()+"\"'::regclass");
		values.put(CardAttributes.Status.toString(), "'A'");
		return values;
	}

	private TreeMap<String, String> createBasicRelationValues(String class1Name, int card1Id, String class2Name,
			int card2Id) {
		TreeMap<String,String> values = new TreeMap<String,String>();
		values.put("IdClass1", "'\""+class1Name+"\"'::regclass");
		values.put("IdObj1", Integer.toString(card1Id));
		values.put("IdClass2", "'\""+class2Name+"\"'::regclass");
		values.put("IdObj2", Integer.toString(card2Id));
		return values;
	}

	protected void updateRelation(DomainInfo domain, int relId, String class1Name, int card1Id, String class2Name, int card2Id) throws SQLException {
		updateCardRow(domain.getDBName(), relId, createBasicRelationValues(class1Name, card1Id, class2Name, card2Id));
	}

	protected void updateReferenceRelation(DomainInfo domain, int relId, String sourceClassName, int sourceCardId, String targetClassName, int targetCardId) throws SQLException {
		if (domain.getReferenceDirection()) {
			updateRelation(domain, relId, sourceClassName, sourceCardId, targetClassName, targetCardId);
		} else {
			updateRelation(domain, relId, targetClassName, targetCardId, sourceClassName, sourceCardId);
		}
	}

	protected void deleteRelation(DomainInfo domain, int relId) throws SQLException {
		deleteCardRow(domain.getDBName(), relId);
	}

	protected void truncateDomain(DomainInfo domain) throws SQLException {
		truncateTable(domain.getDBName());
	}

	protected void truncateClass(String className) throws SQLException {
		truncateTable(className);
	}

	private void truncateTable(String tableName) throws SQLException {
		Connection conn = DBService.getConnection();
		CallableStatement stm = conn.prepareCall(String.format("TRUNCATE \"%s\", \"%s%s\"",
				tableName, tableName, TableHistory.HistoryTableSuffix));
		stm.execute();
	}
}
