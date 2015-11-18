(function() {

	/**
	 * Constants with the standard parameter names
	 */
	Ext.define('CMDBuild.core.constants.Proxy', {
		alternateClassName: ['CMDBuild.ServiceProxy.parameter', 'CMDBuild.core.proxy.Constants'], // Legacy class name

		singleton: true,

		ABSOLUTE_CLASS_ORDER: 'absoluteClassOrder',
		ACCORDION: 'accordion',
		ACCOUNT: 'account',
		ACTIVE: 'active',
		ACTIVE_ONLY: 'activeOnly',
		ACTIVITY: 'activity',
		ACTIVITY_ID: 'activityId',
		ACTIVITY_INSTANCE_ID: 'activityInstanceId',
		ACTIVITY_INSTANCE_INFO_LIST: 'activityInstanceInfoList',
		ACTIVITY_NAME: 'activityName',
		ADD_DISABLED: 'addDisabled',
		ADDRESS: 'address',
		ADMIN: 'admin',
		ADVANCED: 'advanced',
		ALL: 'all',
		ALLOW_CARD_EDITING: 'allowCardEditing',
		ALLOW_PASSWORD_CHANGE: 'allowPasswordChange',
		ALREADY_ASSOCIATED: 'alreadyAssociated',
		AND: 'and',
		ANY: 'any',
		APPLIED: 'applied',
		ATTACHMENT: 'attachment',
		ATTACHMENTS: 'attachments',
		ATTACHMENTS_ACTIVE: 'attachmentsActive',
		ATTACHMENTS_CATEGORY: 'attachmentsCategory',
		ATTRIBUTE: 'attribute',
		ATTRIBUTE_CLASS: 'attributeClass',
		ATTRIBUTE_DESCRIPTION: 'attributeDescription',
		ATTRIBUTE_DOMAIN: 'attributeDomain',
		ATTRIBUTE_MAPPING: 'attributeMapping',
		ATTRIBUTE_NAME: 'attributeName',
		ATTRIBUTE_PATH: 'attributePath',
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
		BIM: 'bim',
		BODY: 'body',
		BULK_UPDATE: 'bulkUpdate',
		CAPABILITIES: 'capabilities',
		CARD: 'card',
		CARD_BROWSER_BY_DOMAIN_CONFIGURATION: 'cardBrowserByDomainConfiguration',
		CARD_FORM_RATIO: 'cardFormRatio',
		CARD_ID: 'cardId',
		CARD_IDENTIFIER: 'cardIdentifier',
		CARD_LOCK_TIMEOUT: 'cardLockTimeout',
		CARD_SEPARATOR: 'cardSeparator',
		CARD_TABS_POSITION: 'cardTabsPosition',
		CARDINALITY: 'cardinality',
		CARDS: 'cards',
		CATEGORY: 'category',
		CC: 'cc',
		CC_ADDRESSES: 'ccAddresses',
		CENTER_LATITUDE: 'centerLatitude',
		CENTER_LONGITUDE: 'centerLongitude',
		CHANGE_PASSWORD: 'changePassword',
		CHANGE_STATUS: 'changeStatus',
		CHANGED: 'changed',
		CHECKED_CARDS: 'checkedCards',
		CHILDREN: 'children',
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
		CLASS_WIDGET: 'classwidget',
		CLASSES: 'classes',
		CLIENT_FILTER: 'clientFilter',
		CLONE: 'clone',
		CLONE_DISABLED: 'cloneDisabled',
		CLOUD_ADMIN: 'cloudAdmin',
		CLUSTERING_THRESHOLD: 'clusteringThreshold',
		CODE: 'code',
		COLUMNS: 'columns',
		COMPONENTS: 'components',
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
		DASHBOARDS: 'dashboards',
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
		DAY_OF_MONTH: 'dayOfMonth',
		DAY_OF_WEEK: 'dayOfWeek',
		DB: 'db',
		DEFAULT: 'default',
		DEFAULT_ACCOUNT: 'defaultAccount',
		DEFAULT_FILTER: 'defaultFilter',
		DEFAULT_FOR_GROUPS: 'defaultForGroups',
		DEFAULT_GROUP_DESCRIPTION: 'defaultGroupDescription',
		DEFAULT_GROUP_ID: 'defaultGroupId',
		DEFAULT_GROUP_NAME: 'defaultGroupName',
		DEFAULT_LANGUAGE: 'defaultLanguage',
		DEFAULT_LOCALIZATION: 'defaultLocalization',
		DEFAULT_SELECTION: 'defaultSelection',
		DELAY: 'delay',
		DELETE: 'delete',
		DELETE_CARD: 'deleteCard',
		DELETE_DISABLED: 'deleteDisabled',
		DELETE_TYPE: 'deletionType',
		DESCRIPTION: 'description',
		DESTINATION: 'destination',
		DESTINATION_CLASS_ID: 'destinationClassId',
		DESTINATION_CLASS_NAME: 'destinationClassName',
		DESTINATION_DESCRIPTION: 'destinationDescription',
		DESTINATION_DISABLED_CLASSES: 'destinationDisabledClasses',
		DIRECT_DESCRIPTION: 'directDescription',
		DIRECTION: 'direction',
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
		DISPLAY_CARD_LOCKER_NAME: 'displayCardLockerName',
		DISPLAY_IN_GRID: 'isbasedsp',
		DMS: 'dms',
		DOMAIN: 'domain',
		DOMAIN_DIRECTION: 'domainDirection',
		DOMAIN_ID: 'domainId',
		DOMAIN_LIMIT: 'domainlimit',
		DOMAIN_NAME: 'domainName',
		DOMAIN_SOURCE: 'src',
		DOMAINS: 'domains',
		DRAFT: 'draft',
		EDITOR_TYPE: 'editorType',
		ELEMENTS: 'elements',
		EMAIL: 'email',
		EMAIL_ACCOUNT: 'emailAccount',
		EMAIL_ID: 'emailId',
		EMAIL_TEMPLATE: 'emailTemplate',
		EMAIL_TEMPLATES: 'emailTemplates',
		EMPTY: 'empty',
		ENABLE: 'enable',
		ENABLE_CARD_LOCK: 'enableCardLock',
		ENABLE_MAP: 'enableMap',
		ENABLE_MOVE_REJECTED_NOT_MATCHING: 'enableMoveRejectedNotMatching',
		ENABLED: 'enabled',
		ENABLED_LANGUAGES: 'enabledLanguages',
		ENABLED_PANELS: 'enabledPanels',
		END_DATE: 'endDate',
		ENGINE: 'engine',
		ENTITY: 'entity',
		ENTITY_ID: 'entityId',
		ENTITY_IDENTIFIER: 'entityIdentifier',
		ENTRY_TYPE: 'entryType',
		ERRORS: 'errors',
		EXPORT_CSV: 'exportCsv',
		EXPRESSION: 'expression',
		EXTENSION: 'extension',
		EXTENSION_MAXIMUM_LEVEL: 'extensionMaximumLevel',
		FIELD: 'field',
		FIELD_MODE: 'fieldmode',
		FIELDS: 'fields',
		FILE: 'file',
		FILE_NAME: 'fileName',
		FILE_SERVER_PORT: 'fileServerPort',
		FILE_SERVER_TYPE: 'fileServerType',
		FILE_SERVER_URL: 'fileServerUrl',
		FILTER: 'filter',
		FILTER_FROM_ADDRESS: 'filterFromAddress',
		FILTER_SUBJECT: 'filterSubject',
		FILTERS: 'filters',
		FK_DESTINATION: 'fkDestination',
		FLOW_STATUS: 'flowStatus',
		FOLDER_TYPE: 'folderType',
		FORCE_DOWNLOAD: 'forceDownload',
		FORCE_DOWNLOAD_PARAM_KEY: 'force-download',
		FORCE_FORMAT: 'forceFormat',
		FORM: 'form',
		FORMAT: 'format',
		FROM: 'from',
		FROM_ADDRESS: 'fromAddress',
		FULL_SCREEN_MODE: 'fullScreenMode',
		FUNCTION: 'function',
		FUNCTION_DATA: 'functionData',
		FUNCTIONS: 'functions',
		GENERIC: 'generic',
		GEO_SERVER_LAYERS_MAPPING:'geoServerLayersMapping',
		GEOSERVER: 'geoserver',
		GIS: 'gis',
		GOOGLE: 'google',
		GRAPH: 'graph',
		GRID_CONFIGURATION: 'gridConfiguration',
		GROUP: 'group',
		GROUP_ID: 'groupId',
		GROUP_NAME: 'groupName',
		GROUPS: 'groups',
		HIDDEN: 'hidden',
		HIDE_SIDE_PANEL: 'hideSidePanel',
		HOUR: 'hour',
		ID: 'id',
		ID_DOMAIN: 'idDomain',
		IDENTIFIER: 'identifier',
		IMAP_PORT: 'imapPort',
		IMAP_SERVER: 'imapServer',
		IMAP_SSL: 'imapSsl',
		IMPORT_CSV: 'importCsv',
		IMPORT_DISABLED: 'importDisabled',
		INCOMING_FOLDER: 'incomingFolder',
		INDEX: 'index',
		INITIAL_ZOOM_LEVEL: 'initialZoomLevel',
		INPUT: 'input',
		INSTANCE: 'instance',
		INSTANCE_IDENTIFIER: 'instanceIdentifier',
		INSTANCE_NAME: 'instanceName',
		INVERSE_DESCRIPTION: 'inverseDescription',
		IP_TYPE: 'ipType',
		IS_ACTIVE: 'isActive',
		IS_ADMINISTRATOR: 'isAdministrator',
		IS_CARD: 'isCard',
		IS_CLOUD_ADMINISTRATOR: 'isCloudAdministrator',
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
		LANGUAGE_PROMPT: 'languagePrompt',
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
		LOOKUP_CATEGORY: 'lookupCategory',
		LOOKUP_TYPE: 'lookupType',
		LOOKUP_VALUE: 'lookupValue',
		MANDATORY: 'mandatory',
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
		MODEL: 'model',
		MODEL_FILE_NAME: 'modelFileName',
		MODEL_NAME: 'modelName',
		MODIFY: 'modify',
		MODIFY_DISABLED: 'modifyDisabled',
		MONTH: 'month',
		MYSQL: 'mysql',
		NAME: 'name',
		NAME_CLASS_1: 'nameClass1',
		NAME_CLASS_2: 'nameClass2',
		NAVIGATION_TREE: 'navigationTree',
		NEW: 'new',
		NO_SUBJECT_PREFIX: 'noSubjectPrefix',
		NONE: 'none',
		NORMAL: 'normal',
		NOT_NULL: 'isnotnull',
		NOT_POSITIVES: 'notPositives',
		NOTES: 'notes',
		NOTIFICATION_ACTIVE: 'notificationActive',
		NOTIFICATION_EMAIL_ACCOUNT: 'notificationEmailAccount',
		NOTIFICATION_EMAIL_TEMPLATE: 'notificationEmailTemplate',
		NOTIFICATION_EMAIL_TEMPLATE_ERROR: 'notificationEmailTemplateError',
		NOTIFY_WITH: 'notifyWith',
		NUMBER: 'number',
		OBJECT: 'object',
		ODT: 'odt',
		OPERATIONS: 'operations',
		ORACLE: 'oracle',
		ORIENTED_DESCRIPTION: 'orientedDescription',
		ORIGIN: 'origin',
		ORIGIN_CLASS_ID: 'originClassId',
		ORIGIN_CLASS_NAME: 'originClassName',
		ORIGIN_DISABLED_CLASSES: 'originDisabledClasses',
		OSM: 'osm',
		OUTGOING: 'outgoing',
		OUTPUT: 'output',
		OUTPUT_FOLDER: 'outputFolder',
		OWNER: 'owner',
		PARAMETERS: 'parameters',
		PARAMS: 'params',
		PARENT: 'parent',
		PARENT_DESCRIPTION: 'parentDescription',
		PARENT_ID: 'parentId',
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
		POPUP_HEIGHT_PERCENTAGE: 'popupHeightPercentage',
		POPUP_WIDTH_PERCENTAGE: 'popupWidthPercentage',
		PORT: 'port',
		POSTGRESQL: 'postgresql',
		PRECISION: 'precision',
		PREFIX: 'prefix',
		PRESELECT_IF_UNIQUE: 'preselectIfUnique',
		PRESET: 'preset',
		PRESETS: 'presets',
		PRESETS_TYPE: 'presetsType',
		PRIVILEGED: 'privileged',
		PRIVILEGED_OBJ_DESCRIPTION: 'privilegedObjectDescription',
		PRIVILEGED_OBJ_ID: 'privilegedObjectId',
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
		PROPERTY_IDENTIFIER: 'propertyIdentifier',
		PROPERTY_NAME: 'propertyName',
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
		REFERENCE_COMBO_STORE_LIMIT: 'referenceComboStoreLimit',
		REFERENCED_CLASS_NAME: 'referencedClassName',
		REFERENCED_ELEMENT_ID: 'referencedElementId',
		REJECT_NOT_MATCHING: 'rejectNotMatching',
		REJECTED_FOLDER: 'rejectedFolder',
		RELATION: 'relation',
		RELATION_ID: 'relationId',
		RELATION_LIMIT: 'relationLimit',
		RELATION_MASTER_SIDE: 'master',
		RELATIONS: 'relations',
		RELATIONS_SIZE: 'relations_size',
		REMOVE: 'remove',
		REPORT: 'report',
		REPORT_CODE: 'reportCode',
		REPORT_ID: 'reportId',
		REPOSITORY_APPLICATION: 'repositoryApplication',
		REPOSITORY_FILE_SERVER_PATH: 'repositoryFileServerPath',
		REPOSITORY_WEB_SERVICE_PATH: 'repositoryWebServicePath',
		REQUIRED: 'required',
		RESPONSE: 'response',
		REST_SESSION_TOKEN: 'RestSessionToken',
		RESTRICTED_ADMIN: 'restrictedAdmin',
		RESULTS: 'results',
		RETRY_WITHOUT_FILTER: 'retryWithoutFilter',
		ROLE: 'role',
		ROOT: 'root',
		ROOT_CLASS: 'rootClass',
		ROW_LIMIT: 'rowLimit',
		ROWS: 'rows',
		RTF: 'rtf',
		SCALE: 'scale',
		SCOPE: 'scope',
		SECTION: 'section',
		SECTION_HIERARCHY: 'sectionHierarchy',
		SECTION_ID: 'sectionId',
		SENDER_ACCOUNT: 'senderAccount',
		SENT: 'sent',
		SEPARATOR: 'separator',
		SERVER_URL: 'serverUrl',
		SERVICE: 'service',
		SESSION_TIMEOUT: 'sessionTimeout',
		SHORT: 'short',
		SHOW_COLUMN: 'showColumn',
		SIMPLE: 'simple',
		SIMPLE_HISTORY_MODE_FOR_CARD: 'simpleHistoryModeForCard',
		SIMPLE_HISTORY_MODE_FOR_PROCESS: 'simpleHistoryModeForProcess',
		SINGLE_SELECT: 'singleSelect',
		SKIP_DISABLED_CLASSES: 'skipDisabledClasses',
		SMTP_PORT: 'smtpPort',
		SMTP_SERVER: 'smtpServer',
		SMTP_SSL: 'smtpSsl',
		SORT: 'sort',
		SOURCE: 'source',
		SOURCE_ATTRIBUTE: 'sourceAttribute',
		SOURCE_CLASS_NAME: 'sourceClassName',
		SOURCE_FUNCTION: 'sourceFunction',
		SOURCE_NAME: 'sourceName',
		SPECIFIC_TYPE_VALUES: 'specificTypeValues',
		SQL: 'sql',
		SQLSERVER: 'sqlserver',
		SRC: 'src',
		STANDARD: 'standard',
		START: 'start',
		START_MAP_WITH_LATITUDE: 'StartMapWithLatitude',
		START_MAP_WITH_LONGITUDE: 'StartMapWithLongitude',
		START_MAP_WITH_ZOOM: 'StartMapWithZoom',
		STARTING_CLASS: 'startingClass',
		STARTING_CLASS_ID: 'startingClassId',
		STATUS: 'status',
		SUBJECT: 'subject',
		TABLE: 'table',
		TABLE_TYPE: 'tableType',
		TABS: 'tabs',
		TAG: 'tag',
		TARGET_CLASS: 'targetClass',
		TARGET_CLASS_FIELD: 'targetClassField',
		TARGET_VARIABLE_NAME: 'targetVariableName',
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
		TRANSLATION_UUID: 'translationUuid',
		TRANSLATIONS: 'translations',
		TYPE: 'type',
		UI_CONFIGURATION: 'uiConfiguration',
		UNIQUE: 'unique',
		UPDATE: 'update',
		URL: 'url',
		USER: 'user',
		USER_ID: 'userId',
		USER_INTERFACE: 'userInterface',
		USERNAME: 'username',
		USERS: 'users',
		VALUE: 'value',
		VALUES: 'values',
		VARIABLES: 'variables',
		VIEW: 'view',
		VIEW_TYPE: 'viewType',
		VIEWS: 'views',
		WARNINGS: 'warnings',
		WIDGET: 'widget',
		WIDGET_ID: 'widgetId',
		WORKFLOW: 'workflow',
		WORKFLOW_ACTIVE: 'workflowActive',
		WORKFLOW_ATTRIBUTES: 'workflowAttributes',
		WORKFLOW_CLASS_NAME: 'workflowClassName',
		WORKFLOW_NAME: 'workflowName',
		WORKSPACE: 'workspace',
		WRITABLE: 'writable',
		WRITE: 'write',
		YAHOO: 'yahoo',
		ZIP: 'zip',
		ZOOM_INITIAL_LEVEL: 'zoomInitialLevel',
		ZOOM_MAX: 'zoomMax',
		ZOOM_MIN: 'zoomMin'
	});

})();