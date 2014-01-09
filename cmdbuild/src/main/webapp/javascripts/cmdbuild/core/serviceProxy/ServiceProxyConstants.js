Ext.ns("CMDBuild.ServiceProxy");

/**
 * Constants with the standard
 * parameter names
 */
CMDBuild.ServiceProxy.parameter = {
	// Common
	ACTIVE: "active",
	ATTRIBUTES: "attributes",
	CARDS: "cards",
	CARD_ID: "cardId",
	CONFIRMED: "confirmed",
	CLASS_NAME: "className",
	CLASS_ID: "classId",
	DATASOURCE: "dataSourceName",
	DESCRIPTION: "description",
	ENTRY_TYPE: "entryType",
	FILTER: "filter",
	FORMAT: "format",
	GROUP_NAME: "groupName",
	GROUP_ID: "groupId",
	ID: "id",
	INDEX: "index",
	LOOKUP: "lookup",
	NAME: "name",
	MENU: "menu",
	RETRY_WITHOUT_FILTER: "retryWithoutFilter",
	SORT: "sort",
	TABLE_TYPE: "tableType",
	WIDGET: "widget",
	WIDGET_ID: "widgetId",

	// Attributes
	DISPLAY_IN_GRID: "isbasedsp",
	GROUP: "group",
	EDITOR_TYPE: "editorType",
	FIELD_MODE: "fieldmode",
	FK_DESTINATION: "fkDestination",
	LENGTH: "len",
	NOT_NULL: "isnotnull",
	PRECISION: "precision",
	SCALE: "scale",
	TYPE: "type",
	UNIQUE: "isunique",

	// DataView
	SOURCE_CLASS_NAME: "sourceClassName",
	SOURCE_FUNCTION: "sourceFunction",

	// Domain
	DOMAIN_ID: "domainId",
	DOMAIN_NAME: "domainName",
	DOMAIN_LIMIT: "domainlimit",
	DOMAIN_SOURCE: "src",

	// EmailTemplate
	BODY: "body",
	BCC: "bcc",
	CC: "cc",
	EMAIL_TEMPLATE_NAME: "templateName",
	FROM: "from",
	SUBJECT: "subject",
	TO: "to",

	// Privilege
	PRIVILEGED_OBJ_DESCRIPTION: "privilegedObjectDescription",
	PRIVILEGED_OBJ_ID: "privilegedObjectId",

	// Relation
	RELATION_ID: "relationId",
	RELATION_MASTER_SIDE: "master"
};