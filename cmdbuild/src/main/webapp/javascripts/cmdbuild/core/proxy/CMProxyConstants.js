(function() {

	/**
	 * Constants with the standard parameter names
	 */
	Ext.define('CMDBuild.core.proxy.CMProxyConstants', {
		alternateClassName: 'CMDBuild.ServiceProxy.parameter', // Legacy class name

		singleton: true,

		ABSOLUTE_CLASS_ORDER: 'absoluteClassOrder',
		ACCOUNT: 'account',
		ACTIVE: 'active',
		ACTIVITY: 'activity',
		ACTIVITY_ID: 'activityId',
		ACTIVITY_INSTANCE_ID: 'activityInstanceId',
		ACTIVITY_INSTANCE_INFO_LIST: 'activityInstanceInfoList',
		ACTIVITY_NAME: 'activityName',
		ADD_DISABLED: 'addDisabled',
		ADDRESS: 'address',
		ADMIN: 'admin',
		ADVANCED: 'advanced',
		ALLOW_CARD_EDITING: 'allowCardEditing',
		ALREADY_ASSOCIATED: 'alreadyAssociated',
		APPLIED: 'applied',
		ATTACHMENTS: 'attachments',
		ATTACHMENTS_ACTIVE: 'attachmentsActive',
		ATTACHMENTS_CATEGORY: 'attachmentsCategory',
		ATTRIBUTE: 'attribute',
		ATTRIBUTE_DESCRIPTION: 'attributeDescription',
		ATTRIBUTE_MAPPING: 'attributeMapping',
		ATTRIBUTE_NAME: 'attributeName',
		ATTRIBUTE_SEPARATOR: 'attributeSeparator',
		ATTRIBUTES: 'attributes',
		ATTRIBUTES_NODE: 'attributesNode',
		ATTRIBUTES_PRIVILEGES: 'attributesPrivileges',
		AUTHORIZATION_HEADER_KEY: 'cmdbuild-authorization',
		BASE: 'base',
		BASE_LEVEL: 'baseLevel',
		BCC: 'bcc',
		BCC_ADDRESSES: 'bccAddresses',
		BEGIN_DATE: 'beginDate',
		BODY: 'body',
		CAPABILITIES: 'capabilities',
		CARD: 'card',
		CARD_ID: 'cardId',
		CARD_IDENTIFIER: 'cardIdentifier',
		CARD_SEPARATOR: 'cardSeparator',
		CARDINALITY: 'cardinality',
		CARDS: 'cards',
		CATEGORY: 'category',
		CC: 'cc',
		CC_ADDRESSES: 'ccAddresses',
		CHANGE_PASSWORD: 'changePassword',
		CHANGE_STATUS: 'changeStatus',
		CHANGED: 'changed',
		CLASS: 'class',
		CLASS_ATTACHMENT_TAB: 'classAttachmentTab',
		CLASS_ATTRIBUTE: 'classAttribute',
		CLASS_DETAIL_TAB: 'classDetailTab',
		CLASS_EMAIL_TAB: 'classEmailTab',
		CLASS_HISTORY_TAB: 'classHistoryTab',
		CLASS_ID: 'classId',
		CLASS_IDENTIFIER: 'classIdentifier',
		CLASS_MAPPING: 'classMapping',
		CLASS_NAME: 'className',
		CLASS_NOTE_TAB: 'classNoteTab',
		CLASS_ORDER_SIGN: 'classOrderSign',
		CLASS_RELATION_TAB: 'classRelationTab',
		CLASS_TARGET_ID: 'classTargetId',
		CLASSES: 'classes',
		CLIENT_FILTER: 'clientFilter',
		CLONE: 'clone',
		CLOUD_ADMIN: 'cloudAdmin',
		CLUSTERING_THRESHOLD: 'clusteringThreshold',
		CODE: 'code',
		COLUMNS: 'columns',
		CONDITION: 'condition',
		CONFIGURATION: 'configuration',
		CONFIRMATION: 'confirmation',
		CONFIRMED: 'confirmed',
		CONTENT: 'content',
		CONTEXT: 'context',
		CREATE: 'create',
		CRON_EXPRESSION: 'cronExpression',
		CRON_INPUT_TYPE: 'cronInputType',
		CSV: 'csv',
		CUSTOM: 'custom',
		CUSTOM_PAGES: 'customPages',
		DASHBOARD: 'dashboard',
		DATA: 'data',
		DATA_INDEX: 'dataIndex',
		DATA_VIEW:'dataView',
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
		DATE: 'date',
		DAY_OF_MOUNTH: 'dayOfMounth',
		DAY_OF_WEEK: 'dayOfWeek',
		DB: 'db',
		DEFAULT: 'default',
		DEFAULT_ACCOUNT: 'defaultAccount',
		DEFAULT_FILTER: 'defaultFilter',
		DEFAULT_FOR_GROUPS: 'defaultForGroups',
		DEFAULT_LOCALIZATION: 'defaultLocalization',
		DEFAULT_SELECTION: 'defaultSelection',
		DELAY: 'delay',
		DELETE: 'delete',
		DELETE_CARD: 'deleteCard',
		DELETE_DISABLED: 'deleteDisabled',
		DELETE_TYPE: 'deletionType',
		DESCRIPTION: 'description',
		DESTINATION_DESCRIPTION: 'destinationDescription',
		DIRTY: 'dirty',
		DISABLE: 'disable',
		DISABLE_ADD_ROW: 'disableAddRow',
		DISABLE_DELETE_ROW: 'disableDeleteRow',
		DISABLE_GRID_FILTER_TOGGLER: 'disableGridFilterToggler',
		DISABLE_IMPORT_FROM_CSV: 'disableImportFromCsv',
		DISABLED: 'disabled',
		DISABLED_CARD_TABS: 'disabledCardTabs',
		DISABLED_MODULES: 'disabledModules',
		DISABLED_PROCESS_TABS: 'disabledProcessTabs',
		DISPLAY_IN_GRID: 'isbasedsp',
		DOMAIN: 'domain',
		DOMAIN_DIRECTION: 'domainDirection',
		DOMAIN_ID: 'domainId',
		DOMAIN_LIMIT: 'domainlimit',
		DOMAIN_NAME: 'domainName',
		DOMAIN_SOURCE: 'src',
		DOMAINS: 'domains',
		DRAFT: 'draft',
		EDITOR_TYPE: 'editorType',
		EMAIL: 'email',
		EMAIL_ACCOUNT: 'emailAccount',
		EMAIL_ID: 'emailId',
		EMAIL_TEMPLATE: 'emailTemplate',
		EMAIL_TEMPLATES: 'emailTemplates',
		ENABLE: 'enable',
		ENABLE_MAP: 'enableMap',
		ENABLED: 'enabled',
		END_DATE: 'endDate',
		ENGINE: 'engine',
		ENTITY: 'entity',
		ENTRY_TYPE: 'entryType',
		EXPRESSION: 'expression',
		EXTENSION: 'extension',
		EXTENSION_MAXIMUM_LEVEL: 'extensionMaximumLevel',
		FIELD_MODE: 'fieldmode',
		FILE: 'file',
		FILE_NAME: 'fileName',
		FILTER: 'filter',
		FILTER_FROM_ADDRESS: 'filterFromAddress',
		FILTER_SUBJECT: 'filterSubject',
		FILTERS: 'filters',
		FK_DESTINATION: 'fkDestination',
		FLOW_STATUS: 'flowStatus',
		FORCE_DOWNLOAD_PARAM_KEY: 'force-download',
		FORCE_FORMAT: 'forceFormat',
		FORM: 'form',
		FORMAT: 'format',
		FROM: 'from',
		FROM_ADDRESS: 'fromAddress',
		FULL_SCREEN_MODE: 'fullScreenMode',
		FUNCTION: 'function',
		GRID_CONFIGURATION: 'gridConfiguration',
		GROUP: 'group',
		GROUP_ID: 'groupId',
		GROUP_NAME: 'groupName',
		GROUPS: 'groups',
		HIDDEN: 'hidden',
		HIDE_SIDE_PANEL: 'hideSidePanel',
		HOUR: 'hour',
		ID: 'id',
		IMAP_PORT: 'imapPort',
		IMAP_SERVER: 'imapServer',
		IMAP_SSL: 'imapSsl',
		IMPORT_DISABLED: 'importDisabled',
		INCOMING_FOLDER: 'incomingFolder',
		INDEX: 'index',
		INITIAL_ZOOM_LEVEL: 'initialZoomLevel',
		INSTANCE_IDENTIFIER: 'instanceIdentifier',
		IP_TYPE: 'ipType',
		IS_ACTIVE: 'isActive',
		IS_ADMINISTRATOR: 'isAdministrator',
		IS_CARD: 'isCard',
		IS_CLOUD_ADMINISTRATOR: 'isCloudAdministrator',
		IS_DEFAULT: 'isDefault',
		IS_ID_TEMPORARY: 'isIdTemporary',
		IS_KEY: 'isKey',
		IS_RELATION: 'isRelation',
		KEEP_SYNCHRONIZATION: 'keepSynchronization',
		KEY: 'key',
		KEY_VALUE_SEPARATOR: 'keyValueSeparator',
		LABEL: 'label',
		LANGUAGE: 'language',
		LANGUAGES: 'languages',
		LANGUAGES_TAGS: 'languagesTags',
		LANGUAGES_WITH_LOCALIZATIONS: 'languagesWithLocalizations',
		LANGUAGES_WITH_LOCALIZATIONS_TAGS: 'languagesWithLocalizationsTags',
		LATITUDE: 'lat',
		LAYOUT: 'layout',
		LDAP: 'ldap',
		LEAF: 'leaf',
		LENGTH: 'length',
		LIMIT: 'limit',
		LOCAL: 'local',
		LOCALIZATION: 'localization',
		LOCALIZED: 'localized',
		LOCALIZED_HEADER_KEY: 'CMDBuild-Localized',
		LONGITUDE: 'lon',
		LOOKUP: 'lookup',
		LOOKUP_TYPE: 'lookupType',
		MANDATORY: 'mandatory',
		MAP_LATITATUDE:'mapLatitude',
		MAP_LONGITUDE: 'mapLongitude',
		MAP_ZOOM: 'mapZoom',
		MENU: 'menu',
		META: 'meta',
		METADATA: 'metadata',
		METADATA_OUTPUT: 'metadataOutput',
		MINUTE: 'minute',
		MODE: 'mode',
		MODEL: 'model',
		MODIFY: 'modify',
		MODIFY_DISABLED: 'modifyDisabled',
		MOUNTH: 'mounth',
		MYSQL: 'mysql',
		NAME: 'name',
		NAME_CLASS_1: 'nameClass1',
		NAME_CLASS_2: 'nameClass2',
		NEW: 'new',
		NO_SUBJECT_PREFIX: 'noSubjectPrefix',
		NONE: 'none',
		NORMAL: 'normal',
		NOT_NULL: 'isnotnull',
		NOT_POSITIVES: 'notPositives',
		NOTIFICATION_ACTIVE: 'notificationActive',
		NOTIFICATION_EMAIL_ACCOUNT: 'notificationEmailAccount',
		NOTIFICATION_EMAIL_TEMPLATE: 'notificationEmailTemplate',
		NOTIFICATION_EMAIL_TEMPLATE_ERROR: 'notificationEmailTemplateError',
		NOTIFY_WITH: 'notifyWith',
		OBJECT: 'object',
		ODT: 'odt',
		OPERATIONS: 'operations',
		ORACLE: 'oracle',
		OUTGOING: 'outgoing',
		OUTPUT: 'output',
		OUTPUT_FOLDER: 'outputFolder',
		PARENT: 'parent',
		PARSING_ACTIVE: 'parsingActive',
		PARSING_KEY_END: 'parsingKeyEnd',
		PARSING_KEY_INIT: 'parsingKeyInit',
		PARSING_VALUE_END: 'parsingValueEnd',
		PARSING_VALUE_INIT: 'parsingValueInit',
		PASSWORD: 'password',
		PDF: 'pdf',
		PERFORMER_NAME: 'performerName',
		PERFORMERS: 'performers',
		PHASE: 'phase',
		PHASE_AFTER_CREATE: 'afterCreate',
		PHASE_AFTER_UPDATE: 'afterUpdate',
		PHASE_BEFORE_DELETE: 'beforeDelete',
		PHASE_BEFORE_UPDATE: 'beforeUpdate',
		POLLING_FREQUENCY: 'pollingFrequency',
		PORT: 'port',
		POSTGRESQL: 'postgresql',
		PRECISION: 'precision',
		PRESELECT_IF_UNIQUE: 'preselectIfUnique',
		PRESET: 'preset',
		PRESETS: 'presets',
		PRESETS_TYPE: 'presetsType',
		PRIVILEGED: 'privileged',
		PROCESS: 'process',
		PROCESS_ATTACHMENT_TAB: 'processAttachmentTab',
		PROCESS_EMAIL_TAB: 'processEmailTab',
		PROCESS_HISTORY_TAB: 'processHistoryTab',
		PROCESS_IDENTIFIER: 'processIdentifier',
		PROCESS_INSTANCE_ID: 'processInstanceId',
		PROCESS_NOTE_TAB: 'processNoteTab',
		PROCESS_RELATION_TAB: 'processRelationTab',
		PROCESS_WIDGET_ALWAYS_ENABLED: 'processWidgetAlwaysEnabled',
		PROCESSED_FOLDER: 'processedFolder',
		PROMPT_SYNCHRONIZATION: 'promptSynchronization',
		PROPERTY: 'property',
		QUERY: 'query',
		READ: 'read',
		READ_ONLY: 'readOnly',
		READ_ONLY_ATTRIBUTES: 'readOnlyAttributes',
		READ_ONLY_SEARCH_WINDOW: 'readOnlySearchWindow',
		REASON: 'reason',
		RECEIVED: 'received',
		RECIPIENT_ADDRESS: 'recipientAddress',
		RECORD: 'record',
		REFERENCE: 'reference',
		REJECT_NOT_MATCHING: 'rejectNotMatching',
		REJECTED_FOLDER: 'rejectedFolder',
		RELATION: 'relation',
		RELATION_ID: 'relationId',
		RELATION_MASTER_SIDE: 'master',
		RELATIONS: 'relations',
		RELATIONS_SIZE: 'relations_size',
		REMOVE: 'remove',
		REPORT: 'report',
		REPORT_CODE: 'reportCode',
		REQUIRED: 'required',
		REST_SESSION_TOKEN: 'RestSessionToken',
		RESTRICTED_ADMIN: 'restrictedAdmin',
		RETRY_WITHOUT_FILTER: 'retryWithoutFilter',
		ROLE: 'role',
		ROWS: 'rows',
		RTF: 'rtf',
		SCALE: 'scale',
		SCOPE: 'scope',
		SENDER_ACCOUNT: 'senderAccount',
		SENT: 'sent',
		SEPARATOR: 'separator',
		SERVICE: 'service',
		SHORT: 'short',
		SIMPLE_HISTORY_MODE_FOR_CARD: 'simpleHistoryModeForCard',
		SIMPLE_HISTORY_MODE_FOR_PROCESS: 'simpleHistoryModeForProcess',
		SINGLE_SELECT: 'singleSelect',
		SKIP_DISABLED_CLASSES: 'skipDisabledClasses',
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
		STANDARD: 'standard',
		START: 'start',
		START_MAP_WITH_LATITUDE: 'StartMapWithLatitude',
		START_MAP_WITH_LONGITUDE: 'StartMapWithLongitude',
		START_MAP_WITH_ZOOM: 'StartMapWithZoom',
		STARTING_CLASS: 'startingClass',
		STATUS: 'status',
		SUBJECT: 'subject',
		TABLE: 'table',
		TABLE_TYPE: 'tableType',
		TAG: 'tag',
		TARGET_CLASS: 'targetClass',
		TEMPLATE: 'template',
		TEMPLATE_ID: 'templateId',
		TEMPLATE_NAME: 'templateName',
		TEMPLATES: 'templates',
		TEMPORARY: 'temporary',
		TEMPORARY_ID: 'temporaryId',
		TEXT: 'text',
		TIME: 'time',
		TITLE: 'title',
		TO: 'to',
		TO_ADDRESSES: 'toAddresses',
		TOKEN: 'token',
		TOOLBAR_BOTTOM: 'bottomToolbar',
		TOOLBAR_TOP: 'topToolbar',
		TRANSLATIONS: 'translations',
		TYPE: 'type',
		UI_CONFIGURATION: 'uiConfiguration',
		UNIQUE: 'unique',
		UPDATE: 'update',
		URL: 'url',
		USER: 'user',
		USER_INTERFACE: 'userInterface',
		USERNAME: 'username',
		USERS: 'users',
		VALUE: 'value',
		VALUES: 'values',
		VARIABLES: 'variables',
		VIEW_TYPE: 'viewType',
		WIDGET: 'widget',
		WIDGET_ID: 'widgetId',
		WORKFLOW: 'workflow',
		WORKFLOW_ACTIVE: 'workflowActive',
		WORKFLOW_ATTRIBUTES: 'workflowAttributes',
		WORKFLOW_CLASS_NAME: 'workflowClassName',
		WORKFLOW_NAME: 'workflowName',
		WRITABLE: 'writable',
		WRITE: 'write'
	});

})();