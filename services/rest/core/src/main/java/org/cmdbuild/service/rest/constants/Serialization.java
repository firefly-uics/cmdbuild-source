package org.cmdbuild.service.rest.constants;

public class Serialization {

	public static final String NAMESPACE = "http://cmdbuild.org/services/rest/";

	public static final String //
			ID = "id", //
			ID_CAPITALIZED = "Id";

	public static final String //
			ACTIVE = "active", //
			ADVANCE = "advance", //
			ATTRIBUTE = "attribute", //
			ATTRIBUTE_DETAIL = ATTRIBUTE + "Detail", //
			ATTRIBUTES = "attributes", //
			CARD = "card", //
			CARD_DETAIL = CARD + "Detail", //
			CARD_ID = CARD + ID_CAPITALIZED, //
			CARDINALITY = "cardinality", //
			CHILDREN = "children", //
			CLASS = "class", //
			CLASS_DESTINATION = CLASS + "Destination", //
			CLASS_ID = CLASS + ID_CAPITALIZED, //
			CLASS_SOURCE = CLASS + "Source", //
			CODE = "code", //
			CODE_CAPITALIZED = "Code", //
			CREDENTIALS = "credentials", //
			DATA = "data", //
			DEFAULT = "default", //
			DEFAULT_VALUE = DEFAULT + "Value", //
			DESCRIPTION = "description", //
			DESCRIPTION_CAPITALIZED = "Description", //
			DESCRIPTION_ATTRIBUTE_NAME = DESCRIPTION + "_attribute_name", //
			DESCRIPTION_DIRECT = DESCRIPTION + "Direct", //
			DESCRIPTION_INVERSE = DESCRIPTION + "Inverse", //
			DESCRIPTION_MASTER_DETAIL = DESCRIPTION + "MasterDetail", //
			DESTINATION = "destination", //
			DISPLAYABLE_IN_LIST = "displayableInList", //
			DOMAIN = "domain", //
			DOMAIN_ID = DOMAIN + ID_CAPITALIZED, //
			DOMAIN_SOURCE = DOMAIN + "Source", //
			EDITOR_TYPE = "editorType", //
			FILTER = "filter", //
			FULL_CLASS_DETAIL = "fullClassDetail", //
			FULL_DOMAIN_DETAIL = "fullDomainDetail", //
			GROUP = "group", //
			INDEX = "index", //
			INHERITED = "inherited", //
			INSTRUCTIONS = "instructions", //
			LENGTH = "length", //
			LIMIT = "limit", //
			LIST_RESPONSE = "listResponse", //
			MANDATORY = "mandatory", //
			MENU = "menu", //
			MENU_DETAIL = MENU + "Detail", //
			MENU_TYPE = MENU + "Type", //
			NAME = "name", //
			NUMBER = "number", //
			PARAMS = "params", //
			PROTOTYPE = "prototype", //
			OBJECT_DESCRIPTION = "objectDescription", //
			OBJECT_ID = "objectId", //
			OBJECT_TYPE = "objectType", //
			PARENT_ID = "parent_id", //
			PARENT = "parent", //
			PARENT_TYPE = "parent_type", //
			PASSWORD = "password", //
			PRECISION = "precision", //
			RELATION = "relation", //
			RESPONSE_METADATA = "meta", //
			SCALE = "scale", //
			SIMPLE_CLASS_DETAIL = "simpleClassDetail", //
			SIMPLE_DOMAIN_DETAIL = "simpleDomainDetail", //
			SIMPLE_PROCESS_DETAIL = "simpleProcessDetail", //
			SIMPLE_RESPONSE = "simpleResponse", //
			SOURCE = "source", //
			START = "start", //
			TARGET_CLASS = "targetClass", //
			TEXT = "text", //
			TOKEN = "token", //
			TOTAL = "total", //
			TYPE = "type", //
			TYPE_CAPITALIZED = "Type", //
			UNIQUE = "unique", //
			USERNAME = "username", //
			VALUE = "value", //
			VALUES = "values", //
			WRITABLE = "writable";

	public static final String //
			LOOKUP = "lookup", //
			LOOKUP_DETAIL = LOOKUP + "Detail", //
			LOOKUP_TYPE = LOOKUP + "Type", //
			LOOKUP_TYPE_ID = LOOKUP_TYPE + ID_CAPITALIZED, //
			LOOKUP_TYPE_DETAIL = LOOKUP_TYPE + "Detail", //
			LOOKUP_VALUE = LOOKUP + "Value", //
			LOOKUP_VALUE_ID = LOOKUP_VALUE + ID_CAPITALIZED;

	public static final String //
			PROCESS = "process", //
			PROCESS_ACTIVITY = PROCESS + "Activity", //
			PROCESS_ACTIVITY_DEFINITION = PROCESS_ACTIVITY + "Definition", //
			PROCESS_ACTIVITY_ID = PROCESS_ACTIVITY + ID_CAPITALIZED, //
			PROCESS_ID = PROCESS + ID_CAPITALIZED, //
			PROCESS_INSTANCE = PROCESS + "Instance", //
			PROCESS_INSTANCE_ADVANCE = PROCESS_INSTANCE + "Advance", //
			PROCESS_INSTANCE_ID = PROCESS_INSTANCE + ID_CAPITALIZED;

	public static final String //
			TYPE_BOOLEAN = "boolean", //
			TYPE_CHAR = "char", //
			TYPE_DATE = "date", //
			TYPE_DATE_TIME = "dateTime", //
			TYPE_DECIMAL = "decimal", //
			TYPE_DOUBLE = "double", //
			TYPE_ENTRY_TYPE = "entryType", //
			TYPE_FOREIGN_KEY = "foreignKey", //
			TYPE_INTEGER = "integer", //
			TYPE_IP_ADDRESS = "ipAddress", //
			TYPE_LOOKUP = "lookup", //
			TYPE_REFERENCE = "reference", //
			TYPE_STRING_ARRAY = "stringArray", //
			TYPE_STRING = "string", //
			TYPE_TEXT = "text", //
			TYPE_TIME = "time";

	public static final String //
			UNDERSCORED_ACTIVITY = "_activity", //
			UNDERSCORED_ADVANCE = "_" + ADVANCE, //
			UNDERSCORED_DESTINATION = "_" + DESTINATION, //
			UNDERSCORED_DESTINATION_CODE = UNDERSCORED_DESTINATION + CODE_CAPITALIZED, //
			UNDERSCORED_DESTINATION_DESCRIPTION = UNDERSCORED_DESTINATION + DESCRIPTION_CAPITALIZED, //
			UNDERSCORED_DESTINATION_ID = UNDERSCORED_DESTINATION + ID_CAPITALIZED, //
			UNDERSCORED_DESTINATION_TYPE = UNDERSCORED_DESTINATION + TYPE_CAPITALIZED, //
			UNDERSCORED_ID = "_" + ID, //
			UNDERSCORED_NAME = "_" + NAME, //
			UNDERSCORED_SOURCE = "_" + SOURCE, //
			UNDERSCORED_SOURCE_CODE = UNDERSCORED_SOURCE + CODE_CAPITALIZED, //
			UNDERSCORED_SOURCE_DESCRIPTION = UNDERSCORED_SOURCE + DESCRIPTION_CAPITALIZED, //
			UNDERSCORED_SOURCE_ID = UNDERSCORED_SOURCE + ID_CAPITALIZED, //
			UNDERSCORED_SOURCE_TYPE = UNDERSCORED_SOURCE + TYPE_CAPITALIZED, //
			UNDERSCORED_TYPE = "_" + TYPE;

	private Serialization() {
		// prevents instantiation
	}

}
