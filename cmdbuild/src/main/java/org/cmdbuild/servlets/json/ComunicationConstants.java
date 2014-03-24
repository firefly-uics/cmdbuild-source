package org.cmdbuild.servlets.json;

public class ComunicationConstants {

	public static final String //
			ACTIVE = "active", //
			ACTIVE_CAPITAL = "Active", //
			ADDRESS = "address", //
			ADMIN_PASSWORD = "admin_password", //
			ADMIN_USER = "admin_user", //
			ALREADY_ASSOCIATED = "alreadyAssociated", //
			ATTACHMENTS = "attachments", //
			ATTRIBUTE = "attribute", //
			ATTRIBUTES = "attributes", //
			ATTRIBUTES_PRIVILEGES = "attributesPrivileges", //
			BCC = "bcc", //
			BODY = "body", //
			CARD = "card", //
			CARDS = "cards", //
			CARD_ID = "cardId", //
			CC = "cc", //
			CLASS_NAME = "className", //
			CLASS_ID = "classId", //
			CLASS_ID_CAPITAL = "IdClass", //
			CODE = "code", //
			CODE_CAPITAL = "Code", //
			CONFIGURATION = "configuration", //
			CONFIRMATION = "confirmation", //
			CONFIRMED = "confirmed", //
			COUNT = "count", //
			CRON_EXPRESSION = "cronExpression", //
			DATA = "data", //
			DB_NAME = "db_name", //
			DB_TYPE = "db_type", //
			DEFAULT = "Default", //
			DEFAULT_GROUP = "defaultgroup", //
			DEFAULT_VALUE = "defaultvalue", //
			DESCRIPTION = "description", //
			DESCRIPTION_CAPITAL = "Description", //
			DISABLE = "disable", //
			DISABLED_ATTRIBUTES = "disabledAttributes", //
			DETAIL_CARD_ID = "detailCardId", //
			DETAIL_CLASS_NAME = "detailClassName", //
			DOMAIN = "domain", //
			DOMAINS = "domains", //
			DOMAIN_CARDINALITY = "cardinality", //
			DOMAIN_DESCRIPTION_STARTING_AT_THE_FIRST_CLASS = "descr_1", //
			DOMAIN_DESCRIPTION_STARTING_AT_THE_SECOND_CLASS = "descr_2", //
			DOMAIN_FIRST_CLASS_ID = "idClass1", //
			DOMAIN_ID = "domainId", //
			DOMAIN_IS_MASTER_DETAIL = "isMasterDetail", //
			DOMAIN_LIMIT = "domainlimit", //
			DOMAIN_MASTER_DETAIL_LABEL = "md_label", //
			DOMAIN_NAME = "domainName", //
			DOMAIN_SECOND_CLASS_ID = "idClass2", //
			DOMAIN_SOURCE = "src", //
			ELEMENTS = "elements", //
			EMAIL = "email", //
			EMAIL_ID = "emailId", //
			ENTRY_TYPE = "entryType", //
			EDITOR_TYPE = "editorType", //
			ENABLE_MOVE_REJECTED_NOT_MATCHING = "enableMoveRejectedNotMatching", //
			EXTENSION = "extension", //
			FIELD_MODE = "fieldmode", //
			FILE = "file", //
			FILE_NAME = "fileName", //
			FILE_CSV = "filecsv", //
			FILTER = "filter", //
			FILTERS = "filters", //
			FORCE_CREATION = "forceCreation", //
			FORMAT = "format", //
			FROM = "from", //
			FUNCTION = "function", //
			FK_DESTINATION = "fkDestination", //
			GROUP = "group", //
			GROUP_ID = "groupId", //
			GROUPS = "groups", //
			GROUP_NAME = "groupName", //
			HOST = "host", //
			ID = "id", //
			ID_CAPITAL = "Id", //
			IMAP_PORT = "imapPort", //
			IMAP_SERVER = "imapServer", //
			IMAP_SSL = "imapSsl", //
			INCOMING_FOLDER = "incomingFolder", //
			INDEX = "index", //
			INHERIT = "inherits", //
			INHERITED = "inherited", //
			IS_ACTIVE = "isActive", //
			IS_ADMINISTRATOR = "isAdministrator", //
			IS_DEFAULT = "isDefault", //
			IS_PROCESS = "isprocess", //
			JRXML = "jrxml", //
			LANGUAGE = "language", //
			LANGUAGE_PROMPT = "language_prompt", //
			LENGTH = "len", //
			LIMIT = "limit", //
			LIM_PASSWORD = "lim_password", //
			LIM_USER = "lim_user", //
			LOOKUP = "lookup", //
			LOOKUP_LIST = "lookuplist", //
			MANAGEMENT_DATABASE = "postgres", //
			MASTER = "master", //
			MASTER_CARD_ID = "masterCardId", //
			MASTER_CLASS_NAME = "masterClassName", //
			MENU = "menu", //
			META_DATA = "meta", //
			NAME = "name", //
			NEW_PASSWORD = "newpassword", //
			NOT_NULL = "isnotnull", //
			NOTES = "Notes", //
			NUMBER = "Number", //
			OLD_PASSWORD = "oldpassword", //
			ORIG_TYPE = "orig_type", //
			OUT_OF_FILTER = "outOfFilter", //
			PARAMS = "params", //
			PARENT = "parent", //
			PARENT_ID = "ParentId", //
			PASSWORD = "password", //
			PATCHES = "patches", //
			PRIVILEGES = "privileges", //
			PRIVILEGE_FILTER = "privilegeFilter", //
			PRIVILEGE_MODE = "privilege_mode", //
			PRIVILEGE_NONE = "none_privilege", //
			PRIVILEGE_OBJ_ID = "privilegedObjectId", //
			PRIVILEGE_OBJ_NAME = "privilegedObjectName", //
			PRIVILEGE_OBJ_DESCRIPTION = "privilegedObjectDescription", //
			PRIVILEGE_READ = "read_privilege", //
			PRIVILEGE_WRITE = "write_privilege", //
			PROCESS_ID = "ProcessId", //
			PROCESSED_FOLDER = "processedFolder", //
			PORT = "port", //
			POSITION = "position", //
			PRECISION = "precision", //
			REJECTED_FOLDER = "rejectedFolder", //
			REJECT_NOT_MATCHING = "rejectNotMatching", //
			RELATION_ID = "relationId", //
			RESULT = "result", //
			ROOT = "root", //
			SCALE = "scale", //
			SHARK_SCHEMA = "shark_schema", //
			STARTING_CLASS = "startingClass", //
			TABLE = "table", //
			TABLE_TYPE = "tableType", //
			TEMPLATE = "template", //
			TEMPLATES = "templates", //
			TO = "to", //
			TYPE = "type", //
			TYPES = "types", //
			TYPE_CAPITAL = "Type", //
			RETRY_WITHOUT_FILTER = "retryWithoutFilter", //
			REPORT_ID = "reportId", //
			RESULTS = "results", //
			ROWS = "rows", //
			SHOW_IN_GRID = "isbasedsp", //
			SHORT = "short", //
			SMTP_PORT = "smtpPort", //
			SMTP_SERVER = "smtpServer", //
			SMTP_SSL = "smtpSsl", //
			SOURCE_CLASS_NAME = "sourceClassName", //
			SOURCE_FUNCTION = "sourceFunction", //
			SORT = "sort", //
			START = "start", //
			STATE = "state", //
			SEPARATOR = "separator", //
			SUPERCLASS = "superclass", //
			SUBJECT = "subject", //
			SUCCESS = "success", //
			UI_CONFIGURATION = "uiConfiguration", //
			UNIQUE = "isunique", //
			USER = "user", //
			USERS = "users", //
			USER_ID = "userid", //
			USER_NAME = "username", //
			USER_STOPPABLE = "userstoppable", //
			USER_TYPE = "user_type", //
			TASK_READ_EMAIL = "email", //
			TASK_START_WORKFLOW = "workflow", //
			TASK_SYNCHRONOUS_EVENT = "synchronousEvent", //
			TEMPORARY_ID = "temporaryId", //
			VIEWS = "views", //
			WIDGET = "widget", //
			WIDGET_ID = "widgetId";

}
