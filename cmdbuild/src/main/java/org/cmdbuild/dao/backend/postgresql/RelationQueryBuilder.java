package org.cmdbuild.dao.backend.postgresql;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.CardImpl;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.filters.AbstractFilter;
import org.cmdbuild.elements.filters.AttributeFilter;
import org.cmdbuild.elements.filters.FilterOperator;
import org.cmdbuild.elements.filters.LimitFilter;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.FilterOperator.OperatorType;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.RelationQuery;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.utils.StringUtils;

public class RelationQueryBuilder {

	// 1 = attributes list,
	// 2 = domain name,
	// 3 = table name 1
	// 4 = table name 2
	// 5 = joins, 6 = where condition, 7 = order by, 8 = limit and offset
	private static final String SELECT = "SELECT %1$s FROM \"%2$s\" " + 
		"JOIN \"%3$s\" AS \"Table1\" ON \"%2$s\".\"IdObj1\" = \"Table1\".\"Id\" " +
		"JOIN \"%4$s\" AS \"Table2\" ON \"%2$s\".\"IdObj2\" = \"Table2\".\"Id\" " +
		"%5$s %6$s %7$s %8$s";
	// 1 = target name, 2 = target alias, 3 = table alias, 4 = reference attribute name
	private static final String REFERENCE_JOIN_TEMPLATE = 
		"LEFT JOIN \"%1$s\" AS \"%2$s\" ON \"%2$s\".\"Id\" = \"%3$s\".\"%4$s\"";
	private static final String FULLCARDS_JOIN_TEMPLATE = 
		"JOIN \"%1$s\" ON \"%1$s\".\"IdClass\" = r.idclass2 AND \"%1$s\".\"Id\"=r.idobj2";
	// 1 = class 1 id, 2 = class 2 id
	private static final String SELECT_WHERE_TEMPLATE = 
		"WHERE \"%1$s\".\"Status\"='A' AND \"Table1\".\"Status\"='A' AND \"Table2\".\"Status\"='A'";
	
	// classdescription is needed by the report
	private static final String SELECT_BY_CARD =
		"SELECT id, iddomain, direct, idclass1, idobj1, idclass2, idobj2, fieldcode, fielddescription, begindate %5$s, classdescription, domaindescription" +
		" FROM system_relationlist AS r %6$s WHERE (idclass1, idobj1) IN (%1$s) AND \"status\"='A' %2$s %3$s %4$s";
	private static final String SELECT_BY_CARD_DOMAINCOUNTED =
		"SELECT DISTINCT count, c.iddomain AS iddomain, c.direct AS direct, id, idclass1, idobj1, idclass2, idobj2, fieldcode, fielddescription, begindate %5$s" +
		" FROM system_relationlist AS r %6$s" +
		" JOIN (SELECT COUNT(*) AS count, iddomain, direct FROM system_relationlist" +
		" WHERE (idclass1, idobj1) IN (%1$s) AND status='A' GROUP BY iddomain, direct, idclass1, idobj1) AS c" +
		" ON r.iddomain=c.iddomain AND r.direct=c.direct" +
		" WHERE (idclass1, idobj1) IN (%1$s) AND status='A' %2$s %3$s %4$s";
	private static final String SELECT_BY_CARD_DOMAINLIMITED =
		"SELECT DISTINCT count, c.iddomain AS iddomain, c.direct AS direct, idclass1, idobj1," +
		" CASE WHEN count > %5$d THEN NULL ELSE r.id END AS id," +
		" CASE WHEN count > %5$d THEN NULL ELSE r.idclass2 END AS idclass2," +
		" CASE WHEN count > %5$d THEN NULL ELSE r.idobj2 END AS idobj2," +
		" CASE WHEN count > %5$d THEN NULL ELSE r.fieldcode END AS fieldcode," +
		" CASE WHEN count > %5$d THEN NULL ELSE r.fielddescription END AS fielddescription," +
		" CASE WHEN count > %5$d THEN NULL ELSE r.begindate END AS begindate" +
		" FROM system_relationlist AS r" +
		" JOIN (SELECT COUNT(*) AS count, iddomain, direct FROM system_relationlist" +
		" WHERE (idclass1, idobj1) IN (%1$s) AND status='A' GROUP BY iddomain, direct, idclass1, idobj1) AS c" +
		" ON r.iddomain=c.iddomain AND r.direct=c.direct WHERE (idclass1, idobj1) IN (%1$s) AND status='A' %2$s %3$s %4$s";
	private static final String SELECT_HISTORY_BY_CARD = "SELECT id, iddomain, direct, idclass1, idobj1, idclass2, idobj2, " +
		"fieldcode, fielddescription, begindate, enddate, username FROM system_relationlist_history " +
		"WHERE (idclass1, idobj1) IN (%1$s) %2$s AND NOT \"status\"='A' %3$s %4$s";

	// 1 = domain name, 2 = attributes list, 3 = primary key filter 
	private static final String UPDATE = "UPDATE \"%1$s\" SET %2$s WHERE %3$s AND \"Status\"='A'";
	// 1 = table name, 2 = attributes names, 3 = attributes values
	public static final String INSERT = "INSERT INTO \"%1$s\" (%2$s) VALUES (%3$s) RETURNING \"Id\"";
	// 1 = table name, 2 = primary key filter
	private static final String DELETE = "DELETE FROM \"%1$s\" WHERE %2$s;";

	private QueryComponents queryComponents = new QueryComponents();

	public QueryComponents getQueryComponents() {
		return queryComponents;
	}

	public String buildSelectQuery(IDomain domain, int id) {
		final AttributeFilter domainFilter = new AttributeFilter(domain.getAttribute("Id"), AttributeFilterType.EQUALS, String.valueOf(id));
		return buildSelectQuery(domain, domainFilter, null, null);
	}

	public String buildSelectQuery(IDomain domain, int card1Id, int card2Id) {
		AttributeFilter filter1 = null;
		AttributeFilter filter2 = null;
		if (card1Id > 0) {
			filter1 = new AttributeFilter(domain.getAttribute("IdObj1"), AttributeFilterType.EQUALS, String.valueOf(card1Id));
		}
		if (card2Id > 0) {
			filter2 = new AttributeFilter(domain.getAttribute("IdObj2"), AttributeFilterType.EQUALS, String.valueOf(card2Id));
		}
		return buildSelectQuery(domain, null, filter1, filter2);
	}

	public String buildSelectQuery(IDomain domain, AbstractFilter domainFilter, AbstractFilter filter1,
			AbstractFilter filter2) {
		final ITable table1 = domain.getClass1();
		final ITable table2 = domain.getClass2();

		for (IAttribute attribute : domain.getAttributes().values()) {
			String attrFullName = "\"" + domain.getDBName() + "\".\"" + attribute.getName() + "\"";
			if(attribute.getType() == AttributeType.REGCLASS)
				attrFullName += "::int4";
			String attrAlias = domain.getDBName() + "_" + attribute.getName();
			queryComponents.addAttribute(attrFullName, attrAlias, attribute, "Map");
		}

		StringBuilder whereCondition = new StringBuilder(String.format(SELECT_WHERE_TEMPLATE, domain.getDBName()));
		List<AbstractFilter> list = new LinkedList<AbstractFilter>();
		String latestMappingAdded = null;
		buildAttributesQuery(table1, "Table1");
		if (filter1 != null) {
			filter1.setQueryMapping(queryComponents.getQueryMapping("Table1"));
			latestMappingAdded = "Table1";
			list.add(filter1);
		}
		buildAttributesQuery(table2, "Table2");
		if (filter2 != null) {
			filter2.setQueryMapping(queryComponents.getQueryMapping("Table2"));
			latestMappingAdded = "Table2";
			list.add(filter2);
		}
		if(domainFilter != null) {
			domainFilter.setQueryMapping(queryComponents.getQueryMapping(domain.getDBName()));
			latestMappingAdded = domain.getDBName();
			list.add(domainFilter);
		}
		if (list.size() == 1) {
			whereCondition.append(" AND ").append(list.get(0).toString(queryComponents.getQueryMapping(latestMappingAdded)));
		} else if (list.size() > 1) {
			whereCondition.append(" AND ").append(new FilterOperator(OperatorType.AND, list).toString());
		}

		return String.format(SELECT,
				StringUtils.join(queryComponents.getAttributeList(), ", "),
				domain.getDBName(),
				table1.getName(),
				table2.getName(),
				StringUtils.join(queryComponents.getJoinList(), " "),
				whereCondition.toString(),
				"",
				""
			);
	}
	
	private void buildAttributesQuery(ITable table, String tableAlias) {
		for(IAttribute attribute : table.getAttributes().values()){
			if (attribute.getType() == AttributeType.REFERENCE) {
				buildReferenceJoin(attribute, tableAlias);
			} else {
				String attrFullName = "\"" + tableAlias + "\".\"" + attribute.getName() + "\"";
				if (attribute.getType() == AttributeType.REGCLASS)
					attrFullName += "::int4";
				String attrAlias = tableAlias + "_" + attribute.getName();
				queryComponents.addAttribute(attrFullName, attrAlias, attribute, tableAlias);
			}
		}
	}

	private void buildReferenceJoin(IAttribute referenceAttribute, String tableAlias) {
		IDomain domain = referenceAttribute.getReferenceDomain();
		int idx = referenceAttribute.isReferenceDirect() ? 2 : 1;
		ITable targetTable = domain.getTables()[idx - 1];

		String targetName = targetTable.getDBName();
		String targetAlias = tableAlias + "_" + referenceAttribute.getName();

		String attrValueName = "\"" + targetAlias + "\".\"Id\"";
		String attrDescriptionName = "\"" + targetAlias + "\".\"Description\"";
		String attrValueAlias = tableAlias + "_" + referenceAttribute.getName();
		String attrDescriptionAlias = attrValueAlias + "_Description";
		queryComponents.addAttribute(attrValueName, attrValueAlias, attrDescriptionName, attrDescriptionAlias, referenceAttribute, tableAlias);
		queryComponents.getJoinList().add(String.format(REFERENCE_JOIN_TEMPLATE,
				targetName,
				targetAlias,
				tableAlias,
				referenceAttribute.getName()
			));
	}

	public String buildUpdateQuery(IRelation relation) {
		String attrTemplate = "\"%1$s\" = %2$s";
		Map<String, AttributeValue> values = relation.getAttributeValueMap();
		Vector<String> attrVector = new Vector<String>();
		for(AttributeValue value : values.values()){
			if(value.isChanged()) {
				attrVector.add( String.format(attrTemplate, value.getSchema().getName(), value.quote()) );
			}
		}
		String query = String.format(UPDATE, relation.getSchema().getDBName(), StringUtils.join(attrVector, ","), relation.getPrimaryKeyCondition()); 
		Log.SQL.debug(query);
		return query;
	}
	
	public String buildInsertQuery(IRelation relation) {
		Vector<String> attrVector = new Vector<String>();
		Vector<String> valueVector = new Vector<String>();
		Map<String, AttributeValue> values = relation.getAttributeValueMap();
		for(AttributeValue value : values.values()){
			attrVector.add( "\"" + value.getSchema().getName() + "\"");
			valueVector.add( value.quote() );
		}
		String query = String.format(INSERT, relation.getSchema().getDBName(), StringUtils.join(attrVector, ","), StringUtils.join(valueVector, ","));
		Log.SQL.debug(query);
		return query;
	}
	
	public String buildDeleteQuery(CardImpl card) {
		return String.format(DELETE, card.getSchema().getName(), card.getPrimaryKeyCondition());
	}

	public String buildSelectQuery(RelationQuery relationQuery) {
		Collection<String> cardPairsCollection = new LinkedList<String>();
		for (ICard card : relationQuery.getCards())
			cardPairsCollection.add(String.format("(%d,%d)", card.getIdClass(), card.getId()));
		String cardPairs = StringUtils.join(cardPairsCollection, ",");
		String query;
		String limitPortion = buildLimitOffset(relationQuery);
		addFullCardsJoin(relationQuery);
		addFullCardsAttributes(relationQuery);

		if (relationQuery.isHistory()) {
			if (relationQuery.isDomainCounted() || relationQuery.isDomainLimited() || relationQuery.isFullCards())
				throw ORMExceptionType.ORM_FILTER_CONFLICT.createException();
			query =  String.format(SELECT_HISTORY_BY_CARD, cardPairs, getDomainFilter(relationQuery), getOrdering(relationQuery), limitPortion);
		} else {
			if (relationQuery.isDomainLimited()) {
				if (relationQuery.isFullCards())
					throw ORMExceptionType.ORM_FILTER_CONFLICT.createException();
				query = String.format(SELECT_BY_CARD_DOMAINLIMITED, cardPairs, getDomainFilter(relationQuery), getOrdering(relationQuery), limitPortion, relationQuery.getDomainLimit());
			} else if (relationQuery.isDomainCounted())
				query = String.format(SELECT_BY_CARD_DOMAINCOUNTED, cardPairs, getDomainFilter(relationQuery), getOrdering(relationQuery), limitPortion, additionalAttributes(), queryComponents.getJoinString());
			else
				query = String.format(SELECT_BY_CARD, cardPairs, getDomainFilter(relationQuery), getOrdering(relationQuery), limitPortion, additionalAttributes(), queryComponents.getJoinString());
		}
		Log.SQL.debug(query);
		return query;
	}

	private void addFullCardsAttributes(RelationQuery relationQuery) {
		if (relationQuery.isFullCards()) {
			ITable destTable = relationQuery.getFullCardsDomain().getDestTable();
			mapAttributesAndJoin(destTable);
		}
	}

	private void mapAttributesAndJoin(ITable table) {
		String tableName = table.getName();
		for(IAttribute attribute : table.getAttributes().values()) {
			String attributeName = attribute.getName();
			if (attribute.getType() == AttributeType.LOOKUP){
			    CardQueryBuilder.buildLookupJoin(attribute, queryComponents);
			} else if (attribute.getType() == AttributeType.REFERENCE){
				CardQueryBuilder.buildReferenceJoin(attribute, queryComponents);
			} else {
				String attrFullName = "\"" + tableName + "\".\"" + attributeName + "\"";
				if(attribute.getType() == AttributeType.REGCLASS)
					attrFullName += "::int4";
				String attrAlias = tableName + "_" + attributeName;
				queryComponents.addAttribute(attrFullName, attrAlias, attribute);
			}
		}
	}

	private String additionalAttributes() {
		if (queryComponents.getAttributeList().isEmpty())
			return "";
		else
			return ", "+queryComponents.getAttributeString();
	}

	private void addFullCardsJoin(RelationQuery relationQuery) {
		if (relationQuery.isFullCards()) {
			String fullCardsJoin = String.format(FULLCARDS_JOIN_TEMPLATE, relationQuery.getFullCardsDomain().getDestTable().getDBName());
			queryComponents.getJoinList().add(fullCardsJoin);
		}
	}

	private String getDomainFilter(RelationQuery relationQuery) {
		Set<DirectedDomain> domains = relationQuery.getDomains();
		if (domains.size() > 0) {
			Collection<String> domainPairsCollection = new LinkedList<String>();
			for (DirectedDomain domain : domains)
				domainPairsCollection.add(String.format("(%d,%s)", domain.getDomain().getId(), domain.getDirectionValue()?"TRUE":"FALSE"));
			return String.format("AND (%1$siddomain, %1$sdirect) IN (%2$s)",
							relationQuery.isDomainCounted() ? "r." : "",
							StringUtils.join(domainPairsCollection, ",")
						);
		} else {
			return "";
		}
	}

	private String getOrdering(RelationQuery relationQuery) {
		if (relationQuery.isOrderedByDomain())
			return "ORDER BY iddomain, direct";
		else
			return "";
	}
	
	private String buildLimitOffset(RelationQuery relationQuery) {
		LimitFilter limit = relationQuery.getLimit();
		if(limit != null){
			return limit.toString();
		} else {
			return "";
		}
	}
}
