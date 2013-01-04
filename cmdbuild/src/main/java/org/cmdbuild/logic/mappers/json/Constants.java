package org.cmdbuild.logic.mappers.json;

public final class Constants {

	private Constants(){
		//empty...created only to prevent instantiation
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
	 * Operators
	 */
	public static final String EQUAL_OPERATOR = "equal";
	public static final String NOT_EQUAL_OPERATOR = "notequal";
	public static final String NULL_OPERATOR = "null";
	public static final String NOT_NULL_OPERATOR = "notnull";
	public static final String GREATER_THAN_OPERATOR = "greater";
	public static final String LESS_THAN_OPERATOR = "less";
	public static final String BETWEEN_OPERATOR = "between";
	public static final String LIKE_OPERATOR = "like";
	public static final String CONTAIN_OPERATOR = "contain";
	public static final String NOT_CONTAIN_OPERATOR = "notcontain";
	public static final String BEGIN_OPERATOR = "begin";
	public static final String NOT_BEGIN_OPERATOR = "notbegin";
	public static final String END_OPERATOR = "end";
	public static final String NOT_END_OPERATOR = "notend";
	
	/**
	 * JSON sorters keys
	 */
	public static final String PROPERTY_KEY = "property";
	public static final String DIRECTION_KEY = "direction";
	
}
