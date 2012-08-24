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
	
}
