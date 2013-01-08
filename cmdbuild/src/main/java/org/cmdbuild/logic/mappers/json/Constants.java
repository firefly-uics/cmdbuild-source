package org.cmdbuild.logic.mappers.json;

public final class Constants {

	private Constants(){
		//empty...created only to prevent instantiation
	}
	
	/**
	 * Filter operators
	 */
	public enum FilterOperator {
		EQUAL("equal"),
		NOT_EQUAL("notequal"),
		NULL("isnull"),
		NOT_NULL("isnotnull"),
		GREATER_THAN("greater"),
		LESS_THAN("less"),
		BETWEEN("between"),
		LIKE("like"),
		CONTAIN("contain"),
		NOT_CONTAIN("notcontain"),
		BEGIN("begin"),
		NOT_BEGIN("notbegin"), 
		END("end"),
		NOT_END("notend");
		
		private String toString;
		
		private FilterOperator(String toString) {
			this.toString = toString;
		}
		
		@Override
		public String toString() {
			return toString;
		}
	}
	
	/**
	 * JSON filter keys
	 */
	public static final String FILTER_KEY = "filter";
	public static final String ATTRIBUTE_KEY = "attribute";
	public static final String FULL_TEXT_QUERY_KEY = "query";
	public static final String RELATION_KEY = "relation";
	public static final String CQL_KEY = "CQL";
	public static final String SIMPLE_KEY = "simple";
	public static final String AND_KEY = "and";
	public static final String OR_KEY = "or";
	public static final String OPERATOR_KEY = "operator";
	public static final String VALUE_KEY = "value";
	
	/**
	 * JSON sorters keys
	 */
	public static final String PROPERTY_KEY = "property";
	public static final String DIRECTION_KEY = "direction";
	
}
