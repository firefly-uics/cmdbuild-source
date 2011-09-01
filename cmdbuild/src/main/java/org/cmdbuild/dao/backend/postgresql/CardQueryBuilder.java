package org.cmdbuild.dao.backend.postgresql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.TableImpl;
import org.cmdbuild.elements.filters.AbstractFilter;
import org.cmdbuild.elements.filters.AttributeFilter;
import org.cmdbuild.elements.filters.FilterOperator;
import org.cmdbuild.elements.filters.FullTextFilter;
import org.cmdbuild.elements.filters.LimitFilter;
import org.cmdbuild.elements.filters.OrderFilter;
import org.cmdbuild.elements.filters.FilterOperator.OperatorType;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.Group;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.StringUtils;

public class CardQueryBuilder {
	// 1 = attributes list, 2 = table db name, 3 = table name, 4 = joins, 5 = where condition, 6 = order by, 7 = limit and offset
	private static final String SELECT = "SELECT %8$s %1$s FROM \"%2$s\" AS \"%3$s\" %4$s %5$s %6$s %7$s";
	// 1 = table name, 2 = table alias, 3 = joins, 4 = where condition
	private static final String COUNT_ATTRIBUTE_QUERY = "(SELECT COUNT(*) FROM (SELECT %5$s \"%2$s\".\"Id\" FROM \"%1$s\" AS \"%2$s\" %3$s %4$s) AS countid)";
	private static final String COUNT_ATTRIBUTE_NAME = "Count";
	
	// 1 = attributes list, 2 = table db name, 3 = table name, 4 = joins, 5 = where condition, 6 = order by, 7 = card id
	public static final String CARD_ZERO_INDEX = "SELECT _cm_zero_rownum_sequence()";
	private static final String CARD_INDEX =
		"SELECT CASE WHEN list2.\"%3$s_Id\" = %7$s THEN list2.rownum-1 ELSE -1 END AS rownum FROM " +
			"(SELECT nextval('rownum') AS rownum, \"%3$s_Id\" FROM "+
				"(SELECT DISTINCT %1$s FROM \"%2$s\" AS \"%3$s\" %4$s %5$s %6$s) AS list1) "+
			"AS list2 "+
		"ORDER BY rownum DESC LIMIT 1";
	// 1 = table name, 2 = attributes list, 3 = primary key filter
	private static final String UPDATE = "UPDATE \"%1$s\" SET %2$s WHERE %3$s";
	// 1 = table name, 2 = attributes names, 3 = attributes values
	// PostgreSQL driver does not support returning of generated keys we need the select workaround :-(
	public static final String INSERT = "INSERT INTO \"%1$s\" (%2$s) VALUES (%3$s) RETURNING \"Id\";";

	// 1 = target name, 2 = target alias, 3 = table alias, 4 = reference attribute name
	private static final String JOIN_TEMPLATE = "LEFT JOIN \"%1$s\" AS \"%2$s\" ON \"%2$s\".\"Id\" = \"%3$s\".\"%4$s\"";

	private static final String RELATION_JOIN_TEMPLATE = 
    	"JOIN \"%1$s\" "+
			"ON \"%1$s\".\"IdClass%2$d\" = \"%4$s\".\"IdClass\" "+
			"AND \"%1$s\".\"IdObj%2$d\" =  \"%4$s\".\"Id\" "+
			"AND \"%1$s\".\"Status\" = 'A' "+
		"JOIN \"%5$s\" AS \"%1$s_%5$s\" "+
			"ON \"%1$s\".\"IdClass%3$d\" = \"%1$s_%5$s\".\"IdClass\" "+
			"AND \"%1$s\".\"IdObj%3$d\" =  \"%1$s_%5$s\".\"Id\" "+
			"AND \"%1$s_%5$s\".\"Status\" = \'A\' "+
		"%6$s";

	private static final String NOT_IN_RELATION_JOIN_TAMPLATE = 
    	"LEFT JOIN \"%1$s\" "+
			"ON \"%1$s\".\"IdClass%2$d\" = \"%3$s\".\"IdClass\" "+
			"AND \"%1$s\".\"IdObj%2$d\" = \"%3$s\".\"Id\" "+
			"AND \"%1$s\".\"Status\" = \'A\' "+
			"AND \"%1$s\".\"IdClass%4$d\" IN (%5$s)";

	private static final String UPDATE_ATTRIBUTE_TEMPLATE = "\"%1$s\" = %2$s";

	private QueryComponents queryComponents = new QueryComponents();

	private boolean skipCount = false;
	
	public CardQueryBuilder skipCount() {
		skipCount = true;
		return this;
	}

	public String buildSelectQueryAddingAttributes(CardQuery cardQuery) {
		Set<String> attributes = cardQuery.getAttributes();
		// Automatically add all table attributes to the query if not specified
		// otherwise
		if (attributes.isEmpty()) {
			attributes.addAll(cardQuery.getTable().getAttributes().keySet());
		}
		// Add ClassId because it is needed to create the card
		if (!attributes.contains(ICard.CardAttributes.ClassId.toString())) {
			attributes.add(ICard.CardAttributes.ClassId.toString());
		}
		return buildSelectQuery(cardQuery);
	}

	public String buildSelectQuery(CardQuery cardQuery) {
		ITable table = cardQuery.getTable();
		String tableName = table.getName();
		String tableDBName = table.getDBName();

		mapAttributesAndJoin(cardQuery);
		mapRelationJoin(cardQuery);

		String distinctPart = getDistinctPart();

		String whereCondition = buildWhereCondition(cardQuery, "WHERE", queryComponents.getQueryMapping());
		if (cardQuery.needsCount() && !skipCount) {
			String countQuery = String.format(COUNT_ATTRIBUTE_QUERY,
					tableDBName,
					tableName,
					queryComponents.getJoinString(),
					whereCondition,
					distinctPart
				);
			queryComponents.addAttribute(countQuery, COUNT_ATTRIBUTE_NAME);
		}
		String orderFields = buildOrdering(cardQuery);
		String limitOffset = buildLimitOffset(cardQuery);

		String query = String.format(SELECT,
				queryComponents.getAttributeString(),
				tableDBName,
				tableName,
				queryComponents.getJoinString(),
				whereCondition,
				orderFields,
				limitOffset,
				distinctPart
			);
		Log.SQL.debug(query);
		return query;
	}

	/*
	 * Distinct is needed for some reason when there are joins, otherwise can be omitted
	 */
	private String getDistinctPart() {
		if (queryComponents.getJoinList().isEmpty()) {
			return "";
		} else {
			return "DISTINCT";
		}
	}

	public String buildPositionQuery(CardQuery cardQuery, int cardId) {
		ITable table = cardQuery.getTable();
		String tableName = table.getName();
		String tableDBName = table.getDBName();

		mapAttributesAndJoin(cardQuery);
		String whereCondition = buildWhereCondition(cardQuery, "WHERE", queryComponents.getQueryMapping());
		String orderFields = buildOrdering(cardQuery);

		String query = String.format(CARD_INDEX,
				queryComponents.getAttributeString(),
				tableDBName,
				tableName,
				queryComponents.getJoinString(),
				whereCondition,
				orderFields,
				cardId
			);
		Log.SQL.debug(query);
		return query;
	};

	public String buildUpdateQuery(ICard card) throws ORMException {
		String query = String.format(UPDATE, card.getSchema().getDBName(), buildUpdateAttrString(card), card.getPrimaryKeyCondition());
		Log.SQL.debug(query);
		return query;
	}

	public String buildUpdateQuery(CardQuery cardQuery, ICard cardTemplate) {
		String query = String.format(UPDATE,
				cardTemplate.getSchema().getDBName(),
				buildUpdateAttrString(cardTemplate),
				buildWhereConditionForUpdate(cardQuery)
			);
		Log.SQL.debug(query);
		return query;
	}

	private String buildWhereConditionForUpdate(CardQuery cardQuery) {
		String whereCondition = String.format("(\"%s\",\"%s\") IN (%s)",
				ICard.CardAttributes.ClassId.toString(), ICard.CardAttributes.Id.toString(), buildUpdateSubSelect(cardQuery)
			);
		return whereCondition;
	}

	private String buildUpdateSubSelect(CardQuery cardQuery) {
		if (cardQuery.getFullTextQuery() == null) {
			cardQuery.attributes(ICard.CardAttributes.ClassId.toString(), ICard.CardAttributes.Id.toString());
			return skipCount().buildSelectQuery(cardQuery);
		} else {
			CardQueryBuilder subQueryBuilder = new CardQueryBuilder();
			final String subQuery = subQueryBuilder.skipCount().buildSelectQueryAddingAttributes(cardQuery);
			final Map<String, QueryAttributeDescriptor> qm = subQueryBuilder.getQueryComponents().getQueryMapping();
			final String idAlias = qm.get(ICard.CardAttributes.Id.toString()).getValueAlias();
			final String classIdAlias = qm.get(ICard.CardAttributes.ClassId.toString()).getValueAlias();
			return String.format("SELECT sq.\"%s\", sq.\"%s\" FROM (%s) AS sq", classIdAlias, idAlias, subQuery);
		}
	}

	private String buildUpdateAttrString(ICard card) {
		Map<String, AttributeValue> values = card.getAttributeValueMap();
		List<String> attrVector = new ArrayList<String>();
		for(AttributeValue value : values.values()){
		    //we don't want to allow the update of classid
		    if(value.getSchema().getName().equals(ICard.CardAttributes.ClassId.toString()))
				continue;
		    if(value.isChanged()) {
				switch (value.getSchema().getType()) {
				case REFERENCE:
				case LOOKUP:
					Object v = (value.isValidId()) ? value.getId() : "NULL";
					attrVector.add(	String.format(
							UPDATE_ATTRIBUTE_TEMPLATE, value.getSchema().getName(),
							v
						));
					break;
				default:
					attrVector.add(	String.format(
							UPDATE_ATTRIBUTE_TEMPLATE, value.getSchema().getName(),
							value.quote()
						));
				}
			}
		}
		return StringUtils.join(attrVector, ",");
	}

	public String buildInsertQuery(ICard card) {
		Map<String, AttributeValue> values = card.getAttributeValueMap();
		List<String> attrVector = new ArrayList<String>();
		List<String> valueVector = new ArrayList<String>();
		for (AttributeValue value : values.values()) {
			// value.isChanged() did not work for privileges because they come from a view
			// thus having Id == -1 BUT with some fields (ClassId, etc.) already filled 
			if (!value.isNull()) {
				IAttribute schema = value.getSchema();
				if (schema.getType() == AttributeType.REFERENCE) {
					if (value.getReference() != null) {
						attrVector.add("\"" + schema.getDBName() + "\"");
						valueVector.add(String.valueOf(value.getReference().getId()));
					}
				} else if (schema.getType() == AttributeType.LOOKUP) {
					if (value.getLookup() != null) {
						attrVector.add("\"" + schema.getDBName() + "\"");
						valueVector.add(String.valueOf(value.getLookup().getId()));
					}
				} else {
					String attrName = schema.getDBName();
					if (!ICard.CardAttributes.Id.toString().equals(attrName)) {
						attrVector.add("\"" + attrName + "\"");
						valueVector.add(value.quote());
					}
				}
			}
		}
		String query = String.format(INSERT, card.getSchema().getDBName(),
				StringUtils.join(attrVector, ","), StringUtils.join(
						valueVector, ","));
		Log.SQL.debug(query);
		return query;
	}

	public QueryComponents getQueryComponents() {
		return queryComponents;
	}

	private void mapAttributesAndJoin(CardQuery cardQuery) {
		boolean isHistory = cardQuery.isHistory();
		ITable table = cardQuery.getTable();
		String tableName = table.getName();
		for (String attributeName : cardQuery.getAttributes()) {
			IAttribute attribute;
			try {
				attribute = table.getAttribute(attributeName);
			} catch (NotFoundException e) {
				Log.PERSISTENCE.error(String.format("Inexistent attribute \"%s\" for table \"%s\"", attributeName, table.getName()));
				continue;
			}
			if (attribute.getType() == AttributeType.LOOKUP){
			    buildLookupJoin(attribute, queryComponents);
			} else if (attribute.getType() == AttributeType.REFERENCE){
			    buildReferenceJoin(attribute, queryComponents);
			} else {
				String attrFullName;
				if (isHistory && CardAttributes.Id.toString().equals(attributeName)) {
					attrFullName = "\"" + tableName + "\".\"CurrentId\"";
				} else if (CardAttributes.ClassId.toString().equals(attributeName)) {
					if (table.isSuperClass()) {
						attrFullName = "\"" + tableName + "\".tableoid";
					} else {
						// tableoid is not present in views like system_privilegecatalog
						attrFullName = "'\"" + tableName + "\"'::regclass";
					}
				} else {
					attrFullName = "\"" + tableName + "\".\"" + attributeName + "\"";
				}
				
				if (attribute.getType() == AttributeType.REGCLASS) {
					attrFullName += "::int4";
				}
				String attrAlias = tableName + "_" + attributeName;
				queryComponents.addAttribute(attrFullName, attrAlias, attribute);
			}
		}
		addNextExecutorJoin(cardQuery, queryComponents);
	}

	private void mapRelationJoin(CardQuery cardQuery){
		Map<DirectedDomain, CardQuery> relationMap = cardQuery.getRelationFilter();
		for (DirectedDomain domain : relationMap.keySet()) {
			buildRelationFilterJoin(domain, cardQuery.getTable(), relationMap.get(domain));
		}
		Map<DirectedDomain, ITable> notInRelationMap = cardQuery.getNotInRelation();
		for (DirectedDomain domain : notInRelationMap.keySet()) {
			buildNotInRelationFilterJoin(domain, cardQuery.getTable(), notInRelationMap.get(domain));
		}
	}

	private AbstractFilter getFilterWithStatus(CardQuery cardQuery) {
		AbstractFilter originalFilter = cardQuery.getFilter();
		ITable table = cardQuery.getTable();
		if (cardQuery.isIgnoreStatus())
			return originalFilter;
		try {
			IAttribute statusAttribute = table.getAttribute(ICard.CardAttributes.Status.toString());
			AbstractFilter statusFilter = new AttributeFilter(statusAttribute,
					AttributeFilter.AttributeFilterType.EQUALS, ElementStatus.ACTIVE.value());
	    	if (originalFilter == null) {
	    		return statusFilter;
	    	} else {
	    		List<AbstractFilter> subFilters = new ArrayList<AbstractFilter>();
	    		subFilters.add(originalFilter);
	    		subFilters.add(statusFilter);
	    		return new FilterOperator(OperatorType.AND, subFilters);
	    	}
		} catch (NotFoundException e) {
			// Simple classes don't have status attribute, bazinga!
			return originalFilter;
		}
	}

	public static void buildReferenceJoin (IAttribute referenceAttribute, QueryComponents queryComponents) {
		IDomain domain = referenceAttribute.getReferenceDomain();
		int idx = referenceAttribute.isReferenceDirect() ? 2 : 1;
		ITable targetTable = domain.getTables()[idx - 1];

		String targetName = targetTable.getDBName();
		String targetAlias = targetTable.getName() + "_" + referenceAttribute.getName();

		String attrValueName = "\"" + targetAlias + "\".\"Id\"";
		String attrDescriptionName = "\"" + targetAlias + "\".\"Description\"";
		String attrValueAlias = referenceAttribute.getSchema().getName() + "_" + referenceAttribute.getName();
		String attrDescriptionAlias = attrValueAlias + "_Description";
		queryComponents.addAttribute(attrValueName, attrValueAlias, attrDescriptionName, attrDescriptionAlias, referenceAttribute);
		queryComponents.getJoinList().add(String.format(JOIN_TEMPLATE,
				targetName,
				targetAlias,
				referenceAttribute.getSchema().getName(),
				referenceAttribute.getName()
			));
	}

	public static void buildLookupJoin(IAttribute lookupAttribute, QueryComponents queryComponents) {
		String targetName = "LookUp";
		String targetAlias = targetName + "_" + lookupAttribute.getName();
		String attrValueName = "\"" + targetAlias + "\".\"Id\"";
		String attrDescriptionName = "\"" + targetAlias + "\".\"Description\"";
		String attrValueAlias = lookupAttribute.getSchema().getName() + "_" + lookupAttribute.getName();
		String attrDescriptionAlias = attrValueAlias + "_Description";
		queryComponents.addAttribute(attrValueName, attrValueAlias, attrDescriptionName, attrDescriptionAlias, lookupAttribute);
		queryComponents.getJoinList().add(String.format(JOIN_TEMPLATE,
				targetName,
				targetAlias,
				lookupAttribute.getSchema().getName(),
				lookupAttribute.getName()
			));
	}
	
	private void buildRelationFilterJoin(DirectedDomain domain, ITable sourceTable, CardQuery cardQuery){
		int target, source;
		String sourceTableName = sourceTable.getDBName();
		String destTableName = cardQuery.getTable().getDBName();
		QueryComponents fakeQueryComponents = QueryComponents.createFakeQueryMappingForJoinCondition(cardQuery.getTable(), domain.getDomain().getDBName());
		String joinCondition = buildWhereCondition(cardQuery, "AND", fakeQueryComponents.getQueryMapping());
		if (domain.getDirectionValue()) {
			target = 2;
			source = 1;
		}else{
			source = 2;
			target = 1;
		}
		queryComponents.getJoinList().add(String.format(RELATION_JOIN_TEMPLATE,
				domain.getDomain().getDBName(), 
				source,
				target,
				sourceTableName,
				destTableName,
				joinCondition
		));
	}	

	private void buildNotInRelationFilterJoin(DirectedDomain domain, ITable table, ITable destinationClass){
		int source, target;
		if (domain.getDirectionValue()) {
			source = 1;
			target = 2;
		} else {
			source = 2;
			target = 1;
		}
		// TODO Add the class filter only if the domain destination is not the specified destination class 
		List<Integer> destinationClassIds = new ArrayList<Integer>();
		for (ITable t : TableImpl.tree().branch(destinationClass.getName())) {
			destinationClassIds.add(t.getId());
		}
		queryComponents.getJoinList().add(String.format(NOT_IN_RELATION_JOIN_TAMPLATE,
				domain.getDomain().getDBName(), 
				source, 
				table.getDBName(), 
				target,
				StringUtils.join(destinationClassIds,",")
		));
	}

	private String buildWhereCondition(CardQuery cardQuery, String prefix, Map<String, QueryAttributeDescriptor> queryMapping) {
		AbstractFilter filter = getFilterWithStatus(cardQuery);
		String fullTextQuery = cardQuery.getFullTextQuery();
		if ((fullTextQuery != null) && (fullTextQuery.trim().length() > 0)) {
			AbstractFilter fullTextFilter = new FullTextFilter(cardQuery.getFullTextQuery());
			List<AbstractFilter> subFilters = new ArrayList<AbstractFilter>();
			subFilters.add(filter);
			subFilters.add(fullTextFilter);
			filter = new FilterOperator(OperatorType.AND, subFilters);
		}

		List<String> whereParts = new ArrayList<String>();
		if (filter != null) {
			whereParts.add(filter.toString(queryMapping));
		}
		addNextExecutorWherePart(cardQuery, whereParts);

		whereParts.addAll(buildNotInRelationFilterWhereParts(cardQuery));

		if (whereParts.isEmpty()) {
			return "";
		} else {
			return prefix + " " + StringUtils.join(whereParts, " AND ");
		}
	}

	private Collection<String> buildNotInRelationFilterWhereParts(CardQuery cardQuery) {
		List<String> notInRelationParts = new ArrayList<String>();
		for (DirectedDomain domain : cardQuery.getNotInRelation().keySet()) {
			notInRelationParts.add("\""+domain.getDomain().getDBName()+"\""+".\"IdDomain\" IS NULL");
		}
		return notInRelationParts;  
	}

	private String buildOrdering(CardQuery cardQuery) {
		Collection<OrderFilter> ordering = cardQuery.getOrdering();
		List<String> orderList = new ArrayList<String>();
		for (OrderFilter orderItem : ordering) {
			orderList.add(orderItem.toString(queryComponents.getQueryMapping()));
		}
		addDefaultIdOrdering(cardQuery, orderList);
		if (orderList.isEmpty())
			return "";
		else
			return "ORDER BY " + StringUtils.join(orderList, ", ");
	}

	/*
	 * Needed to guarantee the same ordering on every query when the
	 * sorting field has the same value (ignored for simple classes)
	 */
	private void addDefaultIdOrdering(CardQuery cardQuery, List<String> orderList) {
		if (cardQuery.getTable().getTableType() != CMTableType.SIMPLECLASS) {
			try {
				IAttribute idAttribute = cardQuery.getTable().getAttribute(ICard.CardAttributes.Id.name());
				OrderFilter orderById = new OrderFilter(idAttribute, OrderFilterType.ASC);
				String orderString = orderById.toString(queryComponents.getQueryMapping());
				orderList.add(orderString);
			} catch (Exception e) {
				// ignored on some views, but it is needed only for classes
			}
		}
	}

	private String buildLimitOffset(CardQuery cardQuery) {
		LimitFilter limit = cardQuery.getLimit();
		if(limit != null){
			return limit.toString();
		} else {
			return "";
		}
	}

	/*
	 * Next Executor
	 */

	private void addNextExecutorJoin(CardQuery cardQuery, QueryComponents queryComponents) {
		if (cardQuery.needsNextExecutorFilter()) {
			String tableAlias = cardQuery.getTable().getName() + "_NextExecutor";
			String nextExecutorJoin = String.format("LEFT JOIN %1$s AS \"%2$s\""+
					" ON \"%2$s\".\"CurrentId\" = \"%3$s\".\"Id\" AND"+
					" \"%2$s\".\"NextExecutor\" = %4$s",
					historyTableOrSubquery(cardQuery.getTable()),
					tableAlias, cardQuery.getTable().getDBName(),
					nextExecutorGroupCondition(cardQuery));
			queryComponents.getJoinList().add(nextExecutorJoin);
		}
	}

	private String historyTableOrSubquery(ITable table) {
		List<ITable> subClasses = UserContext.systemContext().processTypes().tree().branch(table.getName()).getLeaves();
		if (subClasses.size() > 0) {
			List<String> unionQueries = new ArrayList<String>();
			for (ITable subClass : subClasses) {
				unionQueries.add(String.format("SELECT \"Id\",\"CurrentId\",\"NextExecutor\" FROM \"%s_history\"",
						subClass.getDBName()));
			}
			return "(" + StringUtils.join(unionQueries, " UNION ") + ")";
		} else {
			return String.format("\"%s_history\"", table.getDBName());
		}
	}

	private void addNextExecutorWherePart(CardQuery cardQuery, List<String> whereParts) {
		if (cardQuery.needsNextExecutorFilter()) {
			String tableAlias = cardQuery.getTable().getName();
			String historyTableAlias = cardQuery.getTable().getName() + "_NextExecutor";
			String nextExecutorWherePart = String.format(
					"(\"%1$s\".\"NextExecutor\" = %3$s OR \"%2$s\".\"Id\" IS NOT NULL)",
					tableAlias, historyTableAlias, nextExecutorGroupCondition(cardQuery));
			whereParts.add(nextExecutorWherePart);
		}
	}

	private String nextExecutorGroupCondition(CardQuery cardQuery) {
		Collection<Group> groups = cardQuery.getExecutorFilterGroups();
		if (groups.size() > 1) {
			return String.format("ANY (string_to_array('%s',','))", StringUtils.join(groups, ","));
		} else {
			return String.format("'%s'", groups.iterator().next());
		}
	}
}
