package org.cmdbuild.dao.backend.postgresql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.utils.StringUtils;

public class QueryComponents {

	public class QueryAttributeDescriptor {
		private String valueName;
		private String descriptionName;
		private String valueAlias;
		private String descriptionAlias;
		private int valueIndex;
		private int descriptionIndex;

		private QueryAttributeDescriptor(String valueName, String valueAlias, IAttribute attribute) {
			this.valueIndex = QueryComponents.this.getNextIndex();
			this.valueName = valueName;
			this.valueAlias = valueAlias;
			if (attribute == null || attribute.isDisplayable()) {
				this.descriptionName = valueName;
				this.descriptionAlias = valueAlias;
				this.descriptionIndex = valueIndex;
			}
		}

		private QueryAttributeDescriptor(String valueName, String valueAlias, String descriptionName, String descriptionAlias) {
			this.valueIndex = QueryComponents.this.getNextIndex();
			this.valueName = valueName;
			this.valueAlias = valueAlias;
			this.descriptionIndex = QueryComponents.this.getNextIndex();
			this.descriptionName = descriptionName;
			this.descriptionAlias = descriptionAlias;
		}

		public String getValueName() {
			return valueName;
		}

		public String getValueAlias() {
			return valueAlias;
		}

		public String getDescriptionName() {
			return descriptionName;
		}

		public String getDescriptionAlias() {
			return descriptionAlias;
		}

		public int getValueIndex() {
			return valueIndex;
		}

		public int getDescriptionIndex() {
			return descriptionIndex;
		}

		public String getOrderingName() {
			if (descriptionName != null)
				return descriptionName;
			return valueName;
		}
	}

	private static final String DEFAULT_MAPPING = "DEFAULT";
	private int attrIndex = 0;

	private List<String> attrList = new LinkedList<String>();
	private List<String> joinList = new LinkedList<String>();
	private Map<String, Map<String, QueryAttributeDescriptor>> queryMapping = new HashMap<String, Map<String,QueryAttributeDescriptor>>();

	public List<String> getAttributeList() {
		return attrList;
	}

	public int getNextIndex() {
		return ++attrIndex;
	}

	public String getAttributeString() {
		return StringUtils.join(attrList, ", ");
	}

	public List<String> getJoinList() {
		return joinList;
	}

	public String getJoinString() {
		return StringUtils.join(joinList, " ");
	}

	public Map<String, QueryAttributeDescriptor> getQueryMapping() {
		return getQueryMapping(DEFAULT_MAPPING);
	}

	public Map<String, QueryAttributeDescriptor> getQueryMapping(String tableName) {
		if (queryMapping.containsKey(tableName)) {
			return queryMapping.get(tableName);
		} else {
			Map<String, QueryAttributeDescriptor> newQueryMapping = new HashMap<String,QueryAttributeDescriptor>();
			queryMapping.put(tableName, newQueryMapping);
			return newQueryMapping;
		}
	}

	// This class should not exist!
	public void addAttributeForClassId(String attributeName, String attrFullName, String attrAlias) {
		addAttributeToList(attrFullName, attrAlias);
		getQueryMapping(DEFAULT_MAPPING).put(attributeName, new QueryAttributeDescriptor(attrFullName, attrAlias, null));
	}

	public void addAttribute(String attrFullName, String attrAlias, IAttribute attribute) {
		addAttribute(attrFullName, attrAlias, attribute, DEFAULT_MAPPING);
	}

	public void addAttribute(String attrValueName, String attrValueAlias, String attrDescriptionName,
			String attrDescriptionAlias, IAttribute attribute) {
		addAttribute(attrValueName, attrValueAlias, attrDescriptionName, attrDescriptionAlias, attribute, DEFAULT_MAPPING);
	}

	public void addAttribute(String attrFullName, String attrAlias, IAttribute attribute, String tableAlias) {
		addAttributeToList(attrFullName, attrAlias);
		getQueryMapping(tableAlias).put(attribute.getName(), new QueryAttributeDescriptor(attrFullName, attrAlias, attribute));
	}



	public void addAttribute(String attrValueName, String attrValueAlias, String attrDescriptionName,
			String attrDescriptionAlias, IAttribute attribute, String tableAlias) {
		addAttributeToList(attrValueName, attrValueAlias);
		addAttributeToList(attrDescriptionName, attrDescriptionAlias);
		getQueryMapping(tableAlias).put(attribute.getName(),
				new QueryAttributeDescriptor(attrValueName, attrValueAlias, attrDescriptionName, attrDescriptionAlias));
	}

	/*
	 * Used by the Count query attribute. Is the query mapping needed?
	 */
	public void addAttribute(String value, String alias) {
		addAttributeToList(value, alias);
	}

	private void addAttributeToList(String attrName, String attrValue) {
		getAttributeList().add(attrName + " AS \"" + attrValue + "\"");
	}

	/*
	 * Horrible hack to create a fake query mapping
	 */
	public static QueryComponents createFakeQueryMappingForJoinCondition(ITable table, String prefix) {
		QueryComponents queryComponents = new QueryComponents();
		queryComponents.createDetaultQMForTable(table, prefix);
		return queryComponents;
	}

	private void createDetaultQMForTable(ITable table, String prefix) {
		Map<String, QueryAttributeDescriptor> defaultQM = getQueryMapping();
		String tableAlias = String.format("%1$s_%2$s", prefix, table.getDBName());
		for (IAttribute attribute : table.getAttributes().values()) {
			String fullName = String.format("\"%1$s\".\"%2$s\"", tableAlias, attribute.getName());
			defaultQM.put(attribute.getName(), new QueryAttributeDescriptor(fullName, fullName, attribute));
		}
	}
}
