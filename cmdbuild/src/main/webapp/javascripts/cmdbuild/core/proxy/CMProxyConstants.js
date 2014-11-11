(function() {

	/**
	 * Constants with the standard parameter names
	 */
	Ext.define('CMDBuild.core.proxy.CMProxyConstants', {
		alternateClassName: 'CMDBuild.ServiceProxy.parameter', // Legacy class name

		statics: {
			ABSOLUTE_CLASS_ORDER: 'absoluteClassOrder',
			ACCOUNT: 'account',
			ACTIVE: 'active',
			ADDRESS: 'address',
			ADVANCED: 'advanced',
			ALLOW_CARD_EDITING: 'allowCardEditing',
			APPLIED: 'applied',
			ATTACHMENTS_ACTIVE: 'attachmentsActive',
			ATTACHMENTS_CATEGORY: 'attachmentsCategory',
			ATTRIBUTE: 'attribute',
			ATTRIBUTES: 'attributes',
			ATTRIBUTE_MAPPING: 'attributeMapping',
			ATTRIBUTE_SEPARATOR: 'attributeSeparator',
			BASE: 'base',
			BCC: 'bcc',
			BODY: 'body',
			CARD: 'card',
			CARDINALITY: 'cardinality',
			CARDS: 'cards',
			CARD_ID: 'cardId',
			CARD_IDENTIFIER: 'cardIdentifier',
			CARD_SEPARATOR: 'cardSeparator',
			CC: 'cc',
			CHANGE_STATUS: 'changeStatus',
			CLASS: 'class',
			CLASS_ATTRIBUTE: 'classAttribute',
			CLASS_ID: 'classId',
			CLASS_IDENTIFIER: 'classIdentifier',
			CLASS_MAPPING: 'classMapping',
			CLASS_NAME: 'className',
			CLASS_ORDER_SIGN: 'classOrderSign',
			CLASS_TARGET_ID: 'classTargetId',
			CLIENT_FILTER: 'clientFilter',
			CODE: 'code',
			CONFIGURATION: 'configuration',
			CONFIRMED: 'confirmed',
			CREATE: 'create',
			CRON_EXPRESSION: 'cronExpression',
			CRON_INPUT_TYPE: 'cronInputType',
			DATASOURCE: 'dataSourceName',
			DATASOURCE_ADDRESS: 'dataSourceAddress',
			DATASOURCE_CONFIGURATION: 'dataSourceConfiguration',
			DATASOURCE_DB_INSATANCE_NAME: 'dataSourceDbInstance',
			DATASOURCE_DB_NAME: 'dataSourceDbName',
			DATASOURCE_DB_PASSWORD: 'dataSourceDbPassword',
			DATASOURCE_DB_PORT: 'dataSourceDbPort',
			DATASOURCE_DB_TYPE: 'dataSourceDbType',
			DATASOURCE_DB_USERNAME: 'dataSourceDbUsername',
			DATASOURCE_TABLE_VIEW_PREFIX: 'dataSourceTableViewPrefix',
			DATASOURCE_TYPE: 'dataSourceType',
			DAY_OF_MOUNTH: 'dayOfMounth',
			DAY_OF_WEEK: 'dayOfWeek',
			DB: 'db',
			DEFAULT_ACCOUNT: 'defaultAccount',
			DEFAULT_SELECTION: 'defaultSelection',
			DELETE: 'delete',
			DELETE_CARD: 'deleteCard',
			DELETE_TYPE: 'deletionType',
			DESCRIPTION: 'description',
			DESCRIPTION_DEFAULT: 'description_default',
			DISPLAY_IN_GRID: 'isbasedsp',
			DOMAINS: 'domains',
			DOMAIN_DIRECTION: 'domainDirection',
			DOMAIN_ID: 'domainId',
			DOMAIN_LIMIT: 'domainlimit',
			DOMAIN_NAME: 'domainName',
			DOMAIN_SOURCE: 'src',
			EDITOR_TYPE: 'editorType',
			EMAIL_ACCOUNT: 'emailAccount',
			EMAIL_TEMPLATE: 'emailTemplate',
			ENABLE_MAP: 'enableMap',
			ENABLE_MOVE_REJECTED_NOT_MATCHING: 'enableMoveRejectedNotMatching',
			ENTRY_TYPE: 'entryType',
			FIELD_MODE: 'fieldmode',
			FILTER: 'filter',
			FILTER_FROM_ADDRESS: 'filterFromAddress',
			FILTER_SUBJECT: 'filterSubject',
			FK_DESTINATION: 'fkDestination',
			FORCE_FORMAT: 'forceFormat',
			FORMAT: 'format',
			FROM: 'from',
			FUNCTION: 'function',
			GROUP: 'group',
			GROUPS: 'groups',
			GROUP_ID: 'groupId',
			GROUP_NAME: 'groupName',
			HOUR: 'hour',
			ID: 'id',
			IMAP_PORT: 'imapPort',
			IMAP_SERVER: 'imapServer',
			IMAP_SSL: 'imapSsl',
			INCOMING_FOLDER: 'incomingFolder',
			INDEX: 'index',
			IP_TYPE: 'ipType',
			IS_DEFAULT: 'isDefault',
			IS_KEY: 'isKey',
			KEY: 'key',
			KEY_VALUE_SEPARATOR: 'keyValueSeparator',
			LABEL: 'label',
			LATITUDE: 'lat',
			LDAP: 'ldap',
			LENGTH: 'len',
			LOCAL: 'local',
			LONGITUDE: 'lon',
			LOOKUP: 'lookup',
			MAP_LATITATUDE:'mapLatitude',
			MAP_LONGITUDE: 'mapLongitude',
			MAP_ZOOM: 'mapZoom',
			MENU: 'menu',
			METADATA: 'metadata',
			METADATA_OUTPUT: 'metadataOutput',
			MINUTE: 'minute',
			MODE: 'mode',
			MOUNTH: 'mounth',
			MYSQL: 'mysql',
			NAME: 'name',
			NAME_CLASS_1: 'nameClass1',
			NAME_CLASS_2: 'nameClass2',
			NOTIFICATION_ACTIVE: 'notificationActive',
			NOTIFICATION_EMAIL_ACCOUNT: 'notificationEmailAccount',
			NOTIFICATION_EMAIL_TEMPLATE: 'notificationEmailTemplate',
			NOTIFICATION_EMAIL_TEMPLATE_ERROR: 'notificationEmailTemplateError',
			NOT_NULL: 'isnotnull',
			OPERATIONS: 'operations',
			ORACLE: 'oracle',
			OUTPUT: 'output',
			PARENT: 'parent',
			PARSING_ACTIVE: 'parsingActive',
			PARSING_KEY_END: 'parsingKeyEnd',
			PARSING_KEY_INIT: 'parsingKeyInit',
			PARSING_VALUE_END: 'parsingValueEnd',
			PARSING_VALUE_INIT: 'parsingValueInit',
			PASSWORD: 'password',
			PHASE: 'phase',
			PHASE_AFTER_CREATE: 'afterCreate',
			PHASE_AFTER_UPDATE: 'afterUpdate',
			PHASE_BEFORE_DELETE: 'beforeDelete',
			PHASE_BEFORE_UPDATE: 'beforeUpdate',
			POLLING_FREQUENCY: 'pollingFrequency',
			PORT: 'port',
			POSTGRESQL: 'postgresql',
			PRECISION: 'precision',
			PRESET: 'preset',
			PRESETS: 'presets',
			PRESETS_TYPE: 'presetsType',
			PRIVILEGED_OBJ_DESCRIPTION: 'privilegedObjectDescription',
			PRIVILEGED_OBJ_ID: 'privilegedObjectId',
			PROCESSED_FOLDER: 'processedFolder',
			READ_ONLY: 'readOnly',
			READ_ONLY_ATTRIBUTES: 'readOnlyAttributes',
			RECIPIENT_ADDRESS: 'recipientAddress',
			REJECTED_FOLDER: 'rejectedFolder',
			RELATION: 'relation',
			RELATIONS: 'relations',
			RELATIONS_SIZE: 'relations_size',
			RELATION_ID: 'relationId',
			RELATION_MASTER_SIDE: 'master',
			REPORT_CODE: 'reportCode',
			REQUIRED: 'required',
			RETRY_WITHOUT_FILTER: 'retryWithoutFilter',
			SCALE: 'scale',
			SENDER_ACCOUNT: 'senderAccount',
			SINGLE_SELECT: 'singleSelect',
			SMTP_PORT: 'smtpPort',
			SMTP_SERVER: 'smtpServer',
			SMTP_SSL: 'smtpSsl',
			SORT: 'sort',
			SOURCE: 'src',
			SOURCE_ATTRIBUTE: 'sourceAttribute',
			SOURCE_CLASS_NAME: 'sourceClassName',
			SOURCE_FUNCTION: 'sourceFunction',
			SOURCE_NAME: 'sourceName',
			SQLSERVER: 'sqlserver',
			START_MAP_WITH_LATITUDE: 'StartMapWithLatitude',
			START_MAP_WITH_LONGITUDE: 'StartMapWithLongitude',
			START_MAP_WITH_ZOOM: 'StartMapWithZoom',
			SUBJECT: 'subject',
			TABLE_TYPE: 'tableType',
			TEMPLATE: 'template',
			TEMPLATES: 'templates',
			TEXT: 'text',
			TO: 'to',
			TOOLBAR_TOP: 'topToolbar',
			TYPE: 'type',
			UNIQUE: 'isunique',
			UPDATE: 'update',
			USERNAME: 'username',
			VALUE: 'value',
			VARIABLES: 'variables',
			WIDGET: 'widget',
			WIDGET_ID: 'widgetId',
			WORKFLOW: 'workflow',
			WORKFLOW_ACTIVE: 'workflowActive',
			WORKFLOW_ATTRIBUTES: 'workflowAttributes',
			WORKFLOW_CLASS_NAME: 'workflowClassName',
			WORKFLOW_NAME: 'workflowName'
		}
	});

})();
