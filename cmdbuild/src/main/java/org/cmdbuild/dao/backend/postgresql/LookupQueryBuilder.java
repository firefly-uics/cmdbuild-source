package org.cmdbuild.dao.backend.postgresql;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.StringUtils;

public class LookupQueryBuilder {

	private static final String SELECT = "SELECT %1$s FROM \"LookUp\"";
	// 1 = table name, 2 = attributes list, 3 = primary key filter
	private static final String UPDATE = "UPDATE \"LookUp\" SET %1$s WHERE \"Id\"=%2$s";
	// 1 = table name, 2 = attributes names, 3 = attributes values
	private static final String INSERT = "INSERT INTO \"LookUp\" (%1$s) VALUES (%2$s) RETURNING \"Id\";";

	private QueryComponents queryComponents = new QueryComponents();

	public QueryComponents getQueryComponents() {
		return queryComponents;
	}

	public String buildSelectQuery() {
		String query = "";
		ITable table;
		try {
			table = UserContext.systemContext().tables().get("LookUp");
			Map<String, IAttribute> attributes = table.getAttributes(); 
			for(IAttribute attribute : attributes.values()){
				String attrFullName = "\"" + table.getName() + "\".\"" + attribute.getName() + "\"";
				if(attribute.getType() == AttributeType.REGCLASS)
					attrFullName += "::int4";
				String attrAlias = table.getName() + "_" + attribute.getName();
				queryComponents.addAttribute(attrFullName, attrAlias, attribute);
			}
			query = String.format(SELECT, queryComponents.getAttributeString());
		} catch (NotFoundException e) {
			// should never happen
		}
		return query;
	}

	public String buildUpdateQuery(Lookup lookup) {
		List<String> attrVector = new LinkedList<String>();
		String attrTemplate = "\"%1$s\" = %2$s";
		Map<String, AttributeValue> values = lookup.getAttributeValueMap();
		for(AttributeValue value : values.values()){
			if(value.isChanged()) {
				attrVector.add(	String.format(
						attrTemplate, value.getSchema().getName(),
						value.quote()
					));
			}
		}
		if(attrVector!=null && attrVector.isEmpty()) return "";
		return String.format(UPDATE, StringUtils.join(attrVector, ","), lookup.getId());
	}

	public String buildInsertQuery(Lookup lookup) {
		Vector<String> attrVector = new Vector<String>();
		Vector<String> valueVector = new Vector<String>();
		Map<String, AttributeValue> values = lookup.getAttributeValueMap();
		for(AttributeValue value : values.values()) {
			if(value.isChanged()) {
				attrVector.add( "\"" + value.getSchema().getName() + "\"");
				valueVector.add( value.quote() );
			}
		}
		return String.format(INSERT, StringUtils.join(attrVector, ","), StringUtils.join(valueVector, ","));
	}
}
