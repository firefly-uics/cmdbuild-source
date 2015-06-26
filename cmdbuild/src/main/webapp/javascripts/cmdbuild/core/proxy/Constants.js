(function() {

	/**
	 * Constants with the standard parameter names
	 */
	Ext.define('CMDBuild.core.proxy.Constants', {
		alternateClassName: ['CMDBuild.ServiceProxy.parameter', 'CMDBuild.core.proxy.Constants'], // Legacy class name

		singleton: true,

		ABSOLUTE_CLASS_ORDER: 'absoluteClassOrder',
		ACCOUNT: 'account',
		ACTIVE: 'active',
		ACTIVITY: 'activity',
		ACTIVITY_ID: 'activityId',
		ACTIVITY_INSTANCE_INFO_LIST: 'activityInstanceInfoList',
		ACTIVITY_NAME: 'activityName',
		ADDRESS: 'address',
		ADVANCED: 'advanced',
		ALLOW_CARD_EDITING: 'allowCardEditing',
		APPLIED: 'applied',
		ATTACHMENTS: 'attachments',
		ATTACHMENTS_ACTIVE: 'attachmentsActive',
		ATTACHMENTS_CATEGORY: 'attachmentsCategory',
		ATTRIBUTE: 'attribute',
		ATTRIBUTES: 'attributes',
		ATTRIBUTES_NODE: 'attributesNode',
		ATTRIBUTE_CLASS: 'attributeClass',
		ATTRIBUTE_DESCRIPTION: 'attributeDescription',
		ATTRIBUTE_DOMAIN: 'attributeDomain',
		ATTRIBUTE_MAPPING: 'attributeMapping',
		ATTRIBUTE_NAME: 'attributeName',
		ATTRIBUTE_SEPARATOR: 'attributeSeparator',
		BASE: 'base',
		BASE_LEVEL: 'baseLevel',
		BCC: 'bcc',
		BCC_ADDRESSES: 'bccAddresses',
		BEGIN_DATE: 'beginDate',
		BODY: 'body',
		CARD: 'card',
		CARDINALITY: 'cardinality',
		CARDS: 'cards',
		CARD_ID: 'cardId',
		CARD_IDENTIFIER: 'cardIdentifier',
		CARD_SEPARATOR: 'cardSeparator',
		CC: 'cc',
		CC_ADDRESSES: 'ccAddresses',
		CHANGED: 'changed',
		CHANGE_STATUS: 'changeStatus',
		CHILDREN: 'children',
		CLASS: 'class',
		CLASSES: 'classes',
		CLASS_ATTRIBUTE: 'classAttribute',
		CLASS_ID: 'classId',
		CLASS_IDENTIFIER: 'classIdentifier',
		CLASS_MAPPING: 'classMapping',
		CLASS_NAME: 'className',
		CLASS_ORDER_SIGN: 'classOrderSign',
		CLASS_TARGET_ID: 'classTargetId',
		CLASS_WIDGET: 'classwidget',
		CLIENT_FILTER: 'clientFilter',
		CLUSTERING_THRESHOLD: 'clusteringThreshold',
		CODE: 'code',
		COLUMNS: 'columns',
		CONDITION: 'condition',
		CONFIGURATION: 'configuration',
		CONFIRMATION: 'confirmation',
		CONFIRMED: 'confirmed',
		CONTENT: 'content',
		CREATE: 'create',
		CRON_EXPRESSION: 'cronExpression',
		CRON_INPUT_TYPE: 'cronInputType',
		CSV: 'csv',
		CUSTOM: 'custom',
		DATA: 'data',
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
		DATA_INDEX: 'dataIndex',
		DATE: 'date',
		DAY_OF_MOUNTH: 'dayOfMounth',
		DAY_OF_WEEK: 'dayOfWeek',
		DB: 'db',
		DEFAULT: 'default',
		DEFAULT_ACCOUNT: 'defaultAccount',
		DEFAULT_LOCALIZATION: 'defaultLocalization',
		DEFAULT_SELECTION: 'defaultSelection',
		DELAY: 'delay',
		DELETE: 'delete',
		DELETE_CARD: 'deleteCard',
		DELETE_TYPE: 'deletionType',
		DESCRIPTION: 'description',
		DESTINATION_DESCRIPTION: 'destinationDescription',
		DIRECT_DESCRIPTION: 'directDescription',
		DIRTY: 'dirty',
		DISABLE: 'disable',
		DISABLE_ADD_ROW: 'disableAddRow',
		DISABLE_DELETE_ROW: 'disableDeleteRow',
		DISABLE_GRID_FILTER_TOGGLER: 'disableGridFilterToggler',
		DISABLE_IMPORT_FROM_CSV: 'disableImportFromCsv',
		DISPLAY_IN_GRID: 'isbasedsp',
		DOMAIN: 'domain',
		DOMAINS: 'domains',
		DOMAIN_DIRECTION: 'domainDirection',
		DOMAIN_ID: 'domainId',
		DOMAIN_LIMIT: 'domainlimit',
		DOMAIN_NAME: 'domainName',
		DOMAIN_SOURCE: 'src',
		DRAFT: 'draft',
		EDITOR_TYPE: 'editorType',
		EMAIL: 'email',
		EMAIL_ACCOUNT: 'emailAccount',
		EMAIL_ID: 'emailId',
		EMAIL_TEMPLATE: 'emailTemplate',
		EMAIL_TEMPLATES: 'emailTemplates',
		ENABLED: 'enabled',
		ENABLED_LANGUAGES: 'enabledLanguages',
		ENABLE_MAP: 'enableMap',
		ENABLE_MOVE_REJECTED_NOT_MATCHING: 'enableMoveRejectedNotMatching',
		END_DATE: 'endDate',
		ENGINE: 'engine',
		ENTITY: 'entity',
		ENTITY_IDENTIFIER: 'entityIdentifier',
		ENTRY_TYPE: 'entryType',
		EXTENSION: 'extension',
		EXTENSION_MAXIMUM_LEVEL: 'extensionMaximumLevel',
		FIELD: 'field',
		FIELDS: 'fields',
		FIELD_MODE: 'fieldmode',
		FILE: 'file',
		FILE_NAME: 'fileName',
		FILTER: 'filter',
		FILTER_FROM_ADDRESS: 'filterFromAddress',
		FILTER_SUBJECT: 'filterSubject',
		FK_DESTINATION: 'fkDestination',
		FLOW_STATUS: 'flowStatus',
		FOLDER_TYPE: 'folderType',
		FORCE_DOWNLOAD: 'forceDownload',
		FORCE_DOWNLOAD_PARAM_KEY: 'force-download',
		FORCE_FORMAT: 'forceFormat',
		FORMAT: 'format',
		FROM: 'from',
		FROM_ADDRESS: 'fromAddress',
		FUNCTION: 'function',
		GROUP: 'group',
		GROUPS: 'groups',
		GROUP_ID: 'groupId',
		GROUP_NAME: 'groupName',
		HOUR: 'hour',
		ID: 'id',
		IDENTIFIER: 'identifier',
		IMAP_PORT: 'imapPort',
		IMAP_SERVER: 'imapServer',
		IMAP_SSL: 'imapSsl',
		INCOMING_FOLDER: 'incomingFolder',
		INDEX: 'index',
		INITIAL_ZOOM_LEVEL: 'initialZoomLevel',
		INSTANCE_IDENTIFIER: 'instanceIdentifier',
		INSTANCE_NAME: 'instanceName',
		INVERSE_DESCRIPTION: 'inverseDescription',
		IP_TYPE: 'ipType',
		IS_ACTIVE: 'isActive',
		IS_CARD: 'isCard',
		IS_DEFAULT: 'isDefault',
		IS_ID_TEMPORARY: 'isIdTemporary',
		IS_KEY: 'isKey',
		IS_MASTER_DETAIL: 'isMasterDetail',
		IS_RELATION: 'isRelation',
		JRXML: 'jrxml',
		KEEP_SYNCHRONIZATION: 'keepSynchronization',
		KEY: 'key',
		KEY_VALUE_SEPARATOR: 'keyValueSeparator',
		LABEL: 'label',
		LANGUAGE: 'language',
		LANGUAGES: 'languages',
		LATITUDE: 'lat',
		LDAP: 'ldap',
		LEAF: 'leaf',
		LENGTH: 'len',
		LOCAL: 'local',
		LOCALIZATION: 'localization',
		LOCALIZED: 'localized',
		LOCALIZED_HEADER_KEY: 'CMDBuild-Localized',
		LONGITUDE: 'lon',
		LOOKUP: 'lookup',
		LOOKUP_VALUE: 'lookupvalue',
		MAP_LATITATUDE:'mapLatitude',
		MAP_LONGITUDE: 'mapLongitude',
		MAP_ZOOM: 'mapZoom',
		MASTER_DETAIL: 'masterDetail',
		MASTER_DETAIL_LABEL: 'masterDetailLabel',
		MENU: 'menu',
		MENU_ITEM: 'menuItem',
		META: 'meta',
		METADATA: 'metadata',
		METADATA_OUTPUT: 'metadataOutput',
		MINUTE: 'minute',
		MODE: 'mode',
		MOUNTH: 'mounth',
		MYSQL: 'mysql',
		NAME: 'name',
		NAME_CLASS_1: 'nameClass1',
		NAME_CLASS_2: 'nameClass2',
		NEW: 'new',
		NOTIFICATION_ACTIVE: 'notificationActive',
		NOTIFICATION_EMAIL_ACCOUNT: 'notificationEmailAccount',
		NOTIFICATION_EMAIL_TEMPLATE: 'notificationEmailTemplate',
		NOTIFICATION_EMAIL_TEMPLATE_ERROR: 'notificationEmailTemplateError',
		NOTIFY_WITH: 'notifyWith',
		NOT_NULL: 'isnotnull',
		NOT_POSITIVES: 'notPositives',
		NO_SUBJECT_PREFIX: 'noSubjectPrefix',
		OBJECT: 'object',
		ODT: 'odt',
		OPERATIONS: 'operations',
		ORACLE: 'oracle',
		OUTGOING: 'outgoing',
		OUTPUT: 'output',
		OUTPUT_FOLDER: 'outputFolder',
		OWNER: 'owner',
		PARENT: 'parent',
		PARSING_ACTIVE: 'parsingActive',
		PARSING_KEY_END: 'parsingKeyEnd',
		PARSING_KEY_INIT: 'parsingKeyInit',
		PARSING_VALUE_END: 'parsingValueEnd',
		PARSING_VALUE_INIT: 'parsingValueInit',
		PASSWORD: 'password',
		PDF: 'pdf',
		PERFORMERS: 'performers',
		PERFORMER_NAME: 'performerName',
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
		PRIVILEGED_OBJ_DESCRIPTION: 'privilegedObjectDescription',
		PRIVILEGED_OBJ_ID: 'privilegedObjectId',
		PROCESS: 'process',
		PROCESSED_FOLDER: 'processedFolder',
		PROCESS_IDENTIFIER: 'processIdentifier',
		PROMPT_SYNCHRONIZATION: 'promptSynchronization',
		PROPERTY: 'property',
		PROPERTY_IDENTIFIER: 'propertyIdentifier',
		QUERY: 'query',
		READ: 'read',
		READ_ONLY: 'readOnly',
		READ_ONLY_ATTRIBUTES: 'readOnlyAttributes',
		RECEIVED: 'received',
		RECIPIENT_ADDRESS: 'recipientAddress',
		RECORD: 'record',
		REFERENCE: 'reference',
		REJECTED_FOLDER: 'rejectedFolder',
		REJECT_NOT_MATCHING: 'rejectNotMatching',
		RELATION: 'relation',
		RELATIONS: 'relations',
		RELATIONS_SIZE: 'relations_size',
		RELATION_ID: 'relationId',
		RELATION_MASTER_SIDE: 'master',
		REPORT: 'report',
		REPORT_CODE: 'reportCode',
		REPORT_ID: 'reportId',
		REQUIRED: 'required',
		RETRY_WITHOUT_FILTER: 'retryWithoutFilter',
		ROLE: 'role',
		RTF: 'rtf',
		SCALE: 'scale',
		SCOPE: 'scope',
		SECTION_ID: 'sectionId',
		SENDER_ACCOUNT: 'senderAccount',
		SENT: 'sent',
		SERVICE: 'service',
		SHORT: 'short',
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
		SQL: 'sql',
		SQLSERVER: 'sqlserver',
		STARTING_CLASS: 'startingClass',
		START_MAP_WITH_LATITUDE: 'StartMapWithLatitude',
		START_MAP_WITH_LONGITUDE: 'StartMapWithLongitude',
		START_MAP_WITH_ZOOM: 'StartMapWithZoom',
		STATUS: 'status',
		SUBJECT: 'subject',
		TABLE: 'table',
		TABLE_TYPE: 'tableType',
		TAG: 'tag',
		TARGET_CLASS: 'targetClass',
		TEMPLATE: 'template',
		TEMPLATES: 'templates',
		TEMPLATE_ID: 'templateId',
		TEMPLATE_NAME: 'templateName',
		TEMPORARY: 'temporary',
		TEMPORARY_ID: 'temporaryId',
		TEXT: 'text',
		TIME: 'time',
		TITLE: 'title',
		TO: 'to',
		TOOLBAR_BOTTOM: 'bottomToolbar',
		TOOLBAR_TOP: 'topToolbar',
		TO_ADDRESSES: 'toAddresses',
		TRANSLATIONS: 'translations',
		TYPE: 'type',
		UNIQUE: 'isunique',
		UPDATE: 'update',
		URL: 'url',
		USER: 'user',
		USERNAME: 'username',
		VALUE: 'value',
		VALUES: 'values',
		VARIABLES: 'variables',
		VIEW: 'view',
		VIEW_TYPE: 'viewType',
		WIDGET: 'widget',
		WIDGET_ID: 'widgetId',
		WORKFLOW: 'workflow',
		WORKFLOW_ACTIVE: 'workflowActive',
		WORKFLOW_ATTRIBUTES: 'workflowAttributes',
		WORKFLOW_CLASS_NAME: 'workflowClassName',
		WORKFLOW_NAME: 'workflowName',
		ZIP: 'zip'
	});

})();