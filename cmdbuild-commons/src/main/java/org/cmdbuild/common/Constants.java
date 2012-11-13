package org.cmdbuild.common;

public interface Constants {

	String BASE_CLASS_NAME = "Class";

	String CODE_ATTRIBUTE = "Code";
	String DESCRIPTION_ATTRIBUTE = "Description";

	String ID_ATTRIBUTE = "Id";
	String CLASS_ID_ATTRIBUTE = "IdClass";

	String DATETIME_FOUR_DIGIT_YEAR_FORMAT = "dd/MM/yyyy HH:mm:ss";
	String DATETIME_TWO_DIGIT_YEAR_FORMAT = "dd/MM/yy HH:mm:ss";
	String DATETIME_PRINTING_PATTERN = DATETIME_FOUR_DIGIT_YEAR_FORMAT;
	String DATETIME_PARSING_PATTERN = DATETIME_TWO_DIGIT_YEAR_FORMAT;

	String TIME_FORMAT = "HH:mm:ss";
	String TIME_PRINTING_PATTERN = TIME_FORMAT;
	String TIME_PARSING_PATTERN = TIME_FORMAT;

	String DATE_FOUR_DIGIT_YEAR_FORMAT = "dd/MM/yyyy";
	String DATE_TWO_DIGIT_YEAR_FORMAT = "dd/MM/yy";

	String DATE_PRINTING_PATTERN = DATE_FOUR_DIGIT_YEAR_FORMAT;
	/**
	 * The two-digit pattern accepts four digits as well! The four-digit one
	 * would interpret 12 as 0012 instead of 2012!
	 */
	String DATE_PARSING_PATTERN = DATE_TWO_DIGIT_YEAR_FORMAT;

	String SOAP_ALL_DATES_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	String SOAP_ALL_DATES_PRINTING_PATTERN = SOAP_ALL_DATES_FORMAT;
	String SOAP_ALL_DATES_PARSING_PATTERN = SOAP_ALL_DATES_FORMAT;

	interface Webservices {

		String BOOLEAN_TYPE_NAME = "BOOLEAN";
		String CHAR_TYPE_NAME = "CHAR";
		String DATE_TYPE_NAME = "DATE";
		String DECIMAL_TYPE_NAME = "DECIMAL";
		String DOUBLE_TYPE_NAME = "DOUBLE";
		String FOREIGNKEY_TYPE_NAME = "FOREIGNKEY";
		String INET_TYPE_NAME = "INET";
		String INTEGER_TYPE_NAME = "INTEGER";
		String LOOKUP_TYPE_NAME = "LOOKUP";
		String REFERENCE_TYPE_NAME = "REFERENCE";
		String STRING_TYPE_NAME = "STRING";
		String TEXT_TYPE_NAME = "TEXT";
		String TIMESTAMP_TYPE_NAME = "TIMESTAMP";
		String TIME_TYPE_NAME = "TIME";
		String UNKNOWN_TYPE_NAME = "UNKNOWN";

	}

}
