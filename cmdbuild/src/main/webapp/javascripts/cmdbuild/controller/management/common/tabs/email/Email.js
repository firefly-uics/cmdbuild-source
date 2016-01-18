(function () {

	/**
	 * Main controller which manage email regeneration methods
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.management.common.tabs.email.Email', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.controller.management.common.widgets.CMWidgetController',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Utils',
			'CMDBuild.core.proxy.email.Templates',
			'CMDBuild.core.Message'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Boolean}
		 */
		busyState: false,

		/**
		 * Form where to get fields data
		 *
		 * @cfg {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'busyStateGet',
			'busyStateSet',
			'configurationGet',
			'configurationSet',
			'configurationTemplatesSet',
			'editModeGet',
			'getAllTemplatesData',
			'getGlobalLoadMask',
			'getMainController',
			'onEmailPanelShow',
			'onGlobalRegenerationButtonClick',
			'onModifyCardClick',
			'regenerateAllEmailsSet',
			'regenerateSelectedEmails',
			'regenerationEndPointCallbackGet',
			'regenerationEndPointCallbackSet',
			'selectedEntityGet',
			'selectedEntityIdGet',
			'sendAll -> controllerGrid',
			'sendAllOnSaveGet',
			'sendAllOnSaveSet',
			'setUiState -> controllerGrid',
			'storeLoad -> controllerGrid'
		],

		/**
		 * @cfg {Object}
		 */
		configuration: {},

		/**
		 * Template objects array
		 *
		 * @cfg {Array}
		 */
		configurationTemplates: [],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Grid}
		 */
		controllerGrid: undefined,

		/**
		 * @cfg {Object}
		 */
		defaultConfiguration: {
			noSubjectPrefix: false,
			readOnly: true,
			required: false,
			templates: []
		},

		/**
		 * All templates I have in configuration and grid
		 *
		 * @property {Array}
		 */
		emailTemplatesObjects: [],

		/**
		 * All templates identifiers I have in configuration and grid
		 *
		 * @property {Array}
		 */
		emailTemplatesIdentifiers: [],

		/**
		 * @property {Boolean}
		 */
		flagEditMode: false,

		/**
		 * @cfg {Boolean}
		 */
		flagForceRegeneration: false,

		/**
		 * @cfg {Boolean}
		 */
		flagRegenerateAllEmails: false,

		/**
		 * If true send all draft emails on save action
		 *
		 * @cfg {Boolean}
		 */
		flagSendAllOnSave: false,

		/**
		 * Shorthand to view grid
		 *
		 * @property {CMDBuild.view.management.common.tabs.email.GridPanel}
		 */
		grid: undefined,

		/**
		 * @cfg {Boolean}
		 */
		globalLoadMask: true,

		/**
		 * Executed on regeneration end-point, works also as flagSave
		 *
		 * @cfg {Function} or null
		 */
		regenerationEndPointCallback: null,

		/**
		 * Global attribute change flag
		 *
		 * @cfg {Boolean}
		 */
		relatedAttributeChanged: false,

		/**
		 * Actually selected Card/Activity
		 *
		 * @cfg {CMDBuild.model.common.tabs.email.SelectedEntity}
		 */
		selectedEntity: undefined,

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.EmailView}
		 */
		view: undefined,

		statics: {
			/**
			 * Searches for CQL variables resolved by client
			 *
			 * @param {String} inspectingVariable - variable where to check presence of CQL variables
			 * @param {Mixed} inspectingVariableKey - identifier of inspecting variable
			 * @param {Array} searchedVariablesNames - searched variables names
			 * @param {Array} foundedKeysArray - where to push keys of variables witch contains CQL
			 *
			 * @return {Boolean} found
			 */
			searchForCqlClientVariables: function(inspectingVariable, inspectingVariableKey, searchedVariablesNames, foundedKeysArray) {
				var found = false;
				var cqlTags = ['{client:', '{cql:', '{xa:', '{js:'];

				for (var y in searchedVariablesNames) {
					for (var i in cqlTags) {
						if (
							inspectingVariable.indexOf(cqlTags[i] + searchedVariablesNames[y]) > -1
							&& !Ext.Array.contains(foundedKeysArray, inspectingVariableKey)
						) {
							foundedKeysArray.push(inspectingVariableKey);
							found = true;
						}
					}
				}

				return found;
			},

			/**
			 * @param {Mixed} record
			 * @param {Array} regenerationTrafficLightArray
			 *
			 * @return {Boolean} storeLoadEnabled
			 */
			trafficLightArrayCheck: function(record, regenerationTrafficLightArray) {
				if (!Ext.isEmpty(regenerationTrafficLightArray) && Ext.isArray(regenerationTrafficLightArray)) {
					var storeLoadEnabled = true;

					Ext.Array.forEach(regenerationTrafficLightArray, function(item, index, allItems) {
						if (Ext.Object.equals(item[CMDBuild.core.proxy.CMProxyConstants.RECORD], record))
							item[CMDBuild.core.proxy.CMProxyConstants.STATUS] = true;

						if (!item[CMDBuild.core.proxy.CMProxyConstants.STATUS])
							storeLoadEnabled = false;
					}, this);

					// Array reset on store load
					if (storeLoadEnabled)
						regenerationTrafficLightArray = [];

					return storeLoadEnabled;
				}

				return true;
			},

			/**
			 * @param {Mixed} record
			 * @param {Array} trafficLightArray
			 */
			trafficLightSlotBuild: function(record, trafficLightArray) {
				if (!Ext.isEmpty(record) && Ext.isArray(trafficLightArray)) {
					var trafficLight = {};
					trafficLight[CMDBuild.core.proxy.CMProxyConstants.STATUS] = false;
					trafficLight[CMDBuild.core.proxy.CMProxyConstants.RECORD] = record; // Reference to record

					trafficLightArray.push(trafficLight);
				}
			}
		},

		/**
		 * Abstract constructor that must be extended implementing creation and setup of view
		 *
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate - CMModCardController or CMModWorkflowController
		 *
		 * @abstract
		 */
		constructor: function(configurationObject) {
			this.configurationSet();

			this.callParent(arguments);

			// Build controllers
			this.controllerGrid = Ext.create('CMDBuild.controller.management.common.tabs.email.Grid', { parentDelegate: this });
			this.grid = this.controllerGrid.getView();

			this.controllerConfirmRegenerationWindow = Ext.create('CMDBuild.controller.management.common.tabs.email.ConfirmRegenerationWindow', {
				parentDelegate: this,
				gridDelegate: this.controllerGrid
			});

			this.selectedEntitySet(); // Setup empty object by default

			// Extends to create view
		},

		/**
		 * @param {Mixed} record
		 * @param {CMDBuild.Management.TemplateResolver} templateResolver
		 * @param {Object} scope
		 */
		bindLocalDepsChangeEvent: function(record, templateResolver, scope) {
			templateResolver.bindLocalDepsChange(function() {
				if (
					!Ext.Object.isEmpty(record)
					&& !scope.relatedAttributeChanged
				) {
					scope.relatedAttributeChanged = true;

					if (!record.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION) && !record.get(CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION))
						CMDBuild.core.Message.warn(null, CMDBuild.Translation.warnings.emailTemplateRelatedAttributeEdited);
				}
			});
		},

		// BusyState property functions
			/**
			 * @return {Boolean}
			 */
			busyStateGet: function() {
				return this.busyState;
			},

			/**
			 * @param {Boolean} state
			 */
			busyStateSet: function(state) {
				this.busyState = Ext.isBoolean(state) ? state : false;
			},

		/**
		 * @param {Array} data
		 * @param {CMDBuild.Management.TemplateResolver} templateResolver
		 *
		 * @return {Boolean}
		 */
		checkCondition: function(data, templateResolver) {
			var conditionExpr = data[CMDBuild.core.proxy.CMProxyConstants.CONDITION];

			return Ext.isEmpty(conditionExpr) || templateResolver.safeJSEval(conditionExpr);
		},

		/**
		 * Builds templatesToRegenerate array in relation of dirty fields
		 *
		 * @return {Array} templatesToRegenerate
		 */
		checkTemplatesToRegenerate: function() {
			var templatesToRegenerate = [];
			var clientForm = this.parentDelegate.getFormForTemplateResolver();
			var dirtyVariables = Ext.Object.getKeys(clientForm.getValues(false, true));
			var xaVars = this.extractVariablesForTemplateResolver();

			clientForm.owner.initValues(); // Clear form fields dirty state to reset state after regeneration

			// Complete dirtyVariables array also with multilevel variables (ex. var1 = '... {client:var2} ...')
			for (var i in xaVars) {
				var variable = xaVars[i] || [];

				if (
					!Ext.isEmpty(variable)
					&& !Ext.isObject(variable)
					&& Ext.isString(variable)
				) {
					CMDBuild.controller.management.common.tabs.email.Email.searchForCqlClientVariables(
						variable,
						i,
						dirtyVariables,
						dirtyVariables
					);
				}
			}

			// Check templates attributes looking for dirtyVariables as client variables (ex. {client:varName})
			Ext.Array.forEach(this.emailTemplatesObjects, function(template, templateIndex, allTemplatesItems) {
				if (!Ext.Object.isEmpty(template))
					var mergedTemplate = Ext.apply(template.getData(), template.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES));

					Ext.Object.each(mergedTemplate, function(key, value, myself) {
						if (Ext.isString(value)) { // Check all types of CQL variables that can contains client variables
							CMDBuild.controller.management.common.tabs.email.Email.searchForCqlClientVariables(
								value,
								mergedTemplate[CMDBuild.core.proxy.CMProxyConstants.KEY] || mergedTemplate[CMDBuild.core.proxy.CMProxyConstants.NAME],
								dirtyVariables,
								templatesToRegenerate
							);
						}
					}, this);
			}, this);

			return templatesToRegenerate;
		},

		// Configuration property functions
			/**
			 * @return {Object}
			 */
			configurationGet: function() {
				return this.configuration;
			},

			/**
			 * Set configure object and enable UI and all contained components
			 *
			 * @param {Object} configuration
			 */
			configurationSet: function(configuration) {
				if (Ext.Object.isEmpty(configuration)) {
					this.configuration = this.defaultConfiguration;
				} else {
					// Setup class configuration applying configuration attributes to defaultConfiguration
					this.configuration = Ext.apply({}, configuration, this.defaultConfiguration);
				}
			},

		// ConfigurationTemplates property functions
			/**
			 * @param {Array} configurationTemplatesArray
			 */
			configurationTemplatesSet: function(configurationTemplatesArray) {
				if (Ext.isArray(configurationTemplatesArray) && !Ext.isEmpty(configurationTemplatesArray)) {
					this.configurationTemplates = configurationTemplatesArray;
				} else {
					this.configurationTemplates = [];
				}
			},

		// EditMode property functions
			/**
			 * @return {Boolean}
			 */
			editModeGet: function() {
				return this.flagEditMode;
			},

			/**
			 * @param {Boolean} mode
			 */
			editModeSet: function(mode) {
				this.flagEditMode = Ext.isBoolean(mode) ? mode : false;
			},

		/**
		 * Extract the variables of each EmailTemplate object, add a suffix to them with the index, and put them all in the templates map.
		 * This is needed to be passed as a unique map to the template resolver.
		 *
		 * @return {Object} variables
		 */
		extractVariablesForTemplateResolver: function() {
			var variables = {};

			Ext.Array.forEach(this.emailTemplatesObjects, function(item, index, allItems) {
				var templateObject = item.getData();
				var templateVariables = item.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES);

				for (var key in templateVariables)
					variables[key] = templateVariables[key];

				for (var key in templateObject)
					variables[key + (index + 1)] = templateObject[key];
			}, this);

			return variables;
		},

		// ForceRegeneration property functions
			/**
			 * @return {Boolean}
			 */
			forceRegenerationGet: function() {
				return this.flagForceRegeneration;
			},

			/**
			 * @param {Boolean} mode
			 */
			forceRegenerationSet: function(mode) {
				this.flagForceRegeneration = Ext.isBoolean(mode) ? mode : false;
			},

		getAllTemplatesData: function() {
			// Reset local storage arrays
			this.emailTemplatesObjects = [];
			this.emailTemplatesIdentifiers = [];

			// Loads configuration templates to local array and push key in emailTemplatesIdentifiers array
			Ext.Array.forEach(this.configurationTemplates, function(template, index, allItems) {
				if (!Ext.isEmpty(template) && !Ext.Array.contains(this.emailTemplatesIdentifiers, template.get(CMDBuild.core.proxy.CMProxyConstants.KEY))) {
					this.emailTemplatesObjects.push(template);
					this.emailTemplatesIdentifiers.push(template.get(CMDBuild.core.proxy.CMProxyConstants.KEY));
				}
			}, this);

			// Load grid's draft templates names to local array
			Ext.Array.forEach(this.controllerGrid.getDraftEmails(), function(record, index, allItems) {
				var templateIdentifier = null;
				var template = record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);

				if (Ext.isObject(template)) {
					templateIdentifier = template.get(CMDBuild.core.proxy.CMProxyConstants.KEY) || template.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
				} else if (!Ext.isEmpty(template)) {
					templateIdentifier = template;
				}

				if (!Ext.isEmpty(templateIdentifier) && !Ext.Array.contains(this.emailTemplatesIdentifiers, templateIdentifier))
					this.emailTemplatesIdentifiers.push(templateIdentifier);
			}, this);

			CMDBuild.core.proxy.email.Templates.getAll({
				params: {
					templates: Ext.encode(this.emailTemplatesIdentifiers)
				},
				scope: this,
				loadMask: this.globalLoadMask,
				failure: function(response, options, decodedResponse) {
					CMDBuild.core.Message.error(
						CMDBuild.Translation.common.failure,
						Ext.String.format(CMDBuild.Translation.errors.getTemplateWithNameFailure),
						false
					);
				},
				success: function(response, options, decodedResponse) {
					var templates = decodedResponse.response.elements;

					// Load grid's templates to local array
					Ext.Array.forEach(templates, function(template, i, allTemplates) {
						this.emailTemplatesObjects.push(Ext.create('CMDBuild.model.common.tabs.email.Template', template));
					}, this);
				},
				callback: function(options, success, response) {
					this.regenerateAllEmails();
				}
			});
		},

		/**
		 * @return {Boolean}
		 */
		getGlobalLoadMask: function() {
			return this.globalLoadMask;
		},

		/**
		 * @return {CMDBuild.controller.management.common.tabs.email.Email}
		 */
		getMainController: function() {
			return this;
		},

		onAddCardButtonClick: function() {
			this.editModeSet(true);
		},

		/**
		 * Reload store every time panel is showed
		 */
		onEmailPanelShow: function() {
			this.view.setDisabled(
				Ext.isEmpty(this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ENTITY))
				&& !this.editModeGet() // Evaluate also editMode to enable onAddCardButtonClick
			);

			if (this.view.isVisible()) {
				this.cmfg('setUiState');

				// Regenerate all widgets only if editMode otherwise simple store load
				this.regenerateAllEmailsSet(this.editModeGet());
				this.cmfg('storeLoad');
			}
		},

		onGlobalRegenerationButtonClick: function() {
			this.regenerateAllEmailsSet(true);
			this.forceRegenerationSet(true);
			this.cmfg('storeLoad');
		},

		/**
		 * Base implementation to force email regeneration and editMode setup
		 */
		onModifyCardClick: function() {
			this.editModeSet(true);

			if (!this.grid.getStore().isLoading())
				this.onGlobalRegenerationButtonClick();
		},

		// RegenerateAllEmails property functions
			/**
			 * Launch regeneration of all grid records if needed.
			 *
			 * {regenerationTrafficLightArray} Implements a trafficLight functionality to manage multiple asynchronous calls and have a global callback
			 * to reload grid only at real end of calls and avoid to have multiple and useless store load calls.
			 */
			regenerateAllEmails: function() {
				var isRegenerationStarted = false; // Marks that regeneration process is started

				if (this.regenerateAllEmailsGet()) {
					var regenerationTrafficLightArray = [];

					this.controllerConfirmRegenerationWindow.reset();

					if (this.forceRegenerationGet() || this.relatedAttributeChanged) {
						var templatesCheckedForRegenerationIdentifiers = [];
						var emailTemplatesToRegenerate = this.checkTemplatesToRegenerate();

						// Build records to regenerate array
						Ext.Array.forEach(this.controllerGrid.getDraftEmails(), function(item, i, allItems) {
							var recordTemplate = item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);

							if (
								this.controllerGrid.isRegenerable(item)
								&& (
									Ext.Array.contains(emailTemplatesToRegenerate, recordTemplate)
									|| this.forceRegenerationGet()
								)
								&& item.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION)
							) {
								if (item.get(CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION) && !this.forceRegenerationGet()) { // PromptSynch implementation
									this.controllerConfirmRegenerationWindow.addRecordToArray(item);
								} else {
									isRegenerationStarted = true;

									this.regenerateEmail(item, regenerationTrafficLightArray);
								}
							}

							templatesCheckedForRegenerationIdentifiers.push(recordTemplate);
						}, this);

						// Build template to regenerate array
						Ext.Array.forEach(this.configurationTemplates, function(item, i, allItems) {
							var templateIdentifier = item.get(CMDBuild.core.proxy.CMProxyConstants.KEY);

							if (
								!Ext.isEmpty(templateIdentifier)
								&& (
									Ext.Array.contains(emailTemplatesToRegenerate, templateIdentifier)
									|| this.forceRegenerationGet()
								)
								&& !Ext.Array.contains(templatesCheckedForRegenerationIdentifiers, templateIdentifier) // Avoid to generate already regenerated templates
							) {
								if (item.get(CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION) && !this.forceRegenerationGet()) { // PromptSynch implementation
									this.controllerConfirmRegenerationWindow.addTemplateToArray(item);
								} else {
									isRegenerationStarted = true;

									this.regenerateTemplate(item, regenerationTrafficLightArray);
								}
							}

							templatesCheckedForRegenerationIdentifiers.push(templateIdentifier);
						}, this);

						this.controllerConfirmRegenerationWindow.beforeShow();

						this.relatedAttributeChanged = false; // Reset attribute changed flag
						this.forceRegenerationSet(); // Reset force regeneration flag
					}

					this.regenerateAllEmailsSet(); // Reset regenerate all emails flag
				}

				// Set all email as outgoing on save card
				if (this.sendAllOnSaveGet()) {
					this.sendAllOnSaveSet();

					this.cmfg('sendAll');
				} else if (!isRegenerationStarted && Ext.isFunction(this.regenerationEndPointCallback)) { // Executed if no regeneration was performed
					Ext.callback(this.regenerationEndPointCallback, this);
				}

				this.busyStateSet(); // Reset widget busy state to false
			},

			/**
			 * @return {Boolean}
			 */
			regenerateAllEmailsGet: function() {
				return this.flagRegenerateAllEmails;
			},

			/**
			 * @param {Boolean} mode
			 */
			regenerateAllEmailsSet: function(mode) {
				this.flagRegenerateAllEmails = Ext.isBoolean(mode) ? mode : false;
			},

		/**
		 * @param {Mixed} record
		 * @param {Array} regenerationTrafficLightArray
		 */
		regenerateEmail: function(record, regenerationTrafficLightArray) {
			if (
				!Ext.Object.isEmpty(record)
				&& Ext.isArray(regenerationTrafficLightArray)
				&& !Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE))
				&& record.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION)
			) {
				var me = this;

				// Find record template in emailTemplatesObjects
				var recordTemplate = record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);
				recordTemplate = Ext.Array.findBy(this.emailTemplatesObjects, function(item, index) {
					if (
						recordTemplate == item.get(CMDBuild.core.proxy.CMProxyConstants.KEY)
						|| recordTemplate == item.get(CMDBuild.core.proxy.CMProxyConstants.NAME)
					) {
						return true;
					}

					return false;
				}, this);

				if (!Ext.isEmpty(recordTemplate)) {
					var templateData = Ext.apply({}, recordTemplate.getData(), recordTemplate.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES));
					var xaVars = Ext.apply({}, templateData, record.getData());

					var templateResolver = new CMDBuild.Management.TemplateResolver({
						clientForm: this.parentDelegate.getFormForTemplateResolver(),
						xaVars: xaVars,
						serverVars: CMDBuild.controller.management.common.widgets.CMWidgetController.getTemplateResolverServerVars(
							this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ENTITY)
						)
					});

					templateResolver.resolveTemplates({
						attributes: Ext.Object.getKeys(xaVars),
						callback: function(values, ctx) {
							for (var key in values)
								record.set(key, values[key]);

							if (me.checkCondition(values, templateResolver)) {
								_msg('Email with subject "' + values[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] + '" regenerated');

								CMDBuild.controller.management.common.tabs.email.Email.trafficLightSlotBuild(record, regenerationTrafficLightArray);

								me.controllerGrid.editRecord(record, regenerationTrafficLightArray);
							} else {
								me.controllerGrid.removeRecord(record);
							}

							me.bindLocalDepsChangeEvent(record, templateResolver, me);
						}
					});
				}
			}
		},

		/**
		 * Launch regeneration only of selected grid records
		 *
		 * {regenerationTrafficLightArray} Implements a trafficLight functionality to manage multiple asynchronous calls and have a global callback
		 * to reload grid only at real end of calls and avoid to have multiple and useless store load calls.
		 *
		 * @param {Array} records
		 */
		regenerateSelectedEmails: function(records) {
			if (!Ext.isEmpty(records)) {
				var regenerationTrafficLightArray = [];

				Ext.Array.forEach(records, function(item, i, allItems) {
					var recordTemplate = item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);

					if (!Ext.isEmpty(recordTemplate)) {
						if (Ext.isEmpty(item.get(CMDBuild.core.proxy.CMProxyConstants.ID))) { // If there isn't an id the record is a new email generated from template
							this.regenerateTemplate(item, regenerationTrafficLightArray);
						} else {
							this.regenerateEmail(item, regenerationTrafficLightArray);
						}
					}
				}, this);

				this.relatedAttributeChanged = false; // Reset attribute changed flag
			}
		},

		/**
		 * @param {CMDBuild.model.common.tabs.email.Template} template
		 * @param {Array} regenerationTrafficLightArray
		 */
		regenerateTemplate: function(template, regenerationTrafficLightArray) {
			if (
				!Ext.Object.isEmpty(template)
				&& Ext.isArray(regenerationTrafficLightArray)
			) {
				var me = this;
				var xaVars = Ext.apply({}, template.getData(), template.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES));

				var templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: this.parentDelegate.getFormForTemplateResolver(),
					xaVars: xaVars,
					serverVars: CMDBuild.controller.management.common.widgets.CMWidgetController.getTemplateResolverServerVars(
						this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ENTITY)
					)
				});

				templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(xaVars),
					callback: function(values, ctx) {
						var emailObject = null;

						// Find record witch has been created from this template
						var record = Ext.Array.findBy(me.controllerGrid.getDraftEmails(), function(item, index) {
							if (item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE) == template.get(CMDBuild.core.proxy.CMProxyConstants.KEY))
								return true;

							return false;
						});

						// Update record data with values
						if (!Ext.Object.isEmpty(record))
							values = Ext.Object.merge(record.getData(), values);

						emailObject = Ext.create('CMDBuild.model.common.tabs.email.Email', values);
						emailObject.set(CMDBuild.core.proxy.CMProxyConstants.REFERENCE, me.cmfg('selectedEntityIdGet'));
						emailObject.set(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, template.get(CMDBuild.core.proxy.CMProxyConstants.KEY));
						emailObject.set(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY, me.cmfg('selectedEntityIdGet') < 0); // Setup temporary parameter

						if (me.checkCondition(values, templateResolver)) {
							_msg('Template with subject "' + values[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] + '" regenerated');

							CMDBuild.controller.management.common.tabs.email.Email.trafficLightSlotBuild(emailObject, regenerationTrafficLightArray);

							if (Ext.isEmpty(record)) {
								me.controllerGrid.addRecord(emailObject, regenerationTrafficLightArray);
							} else {
								me.controllerGrid.editRecord(emailObject, regenerationTrafficLightArray);
							}
						} else {
							me.controllerGrid.removeRecord(record);
						}

						me.bindLocalDepsChangeEvent(emailObject, templateResolver, me);
					}
				});
			}
		},

		// RegenerationEndPointCallback property functions
			/**
			 * @return {Function} or null
			 */
			regenerationEndPointCallbackGet: function() {
				return this.regenerationEndPointCallback;
			},

			/**
			 * @param {Function} callbackFunction
			 */
			regenerationEndPointCallbackSet: function(callbackFunction) {
				this.regenerationEndPointCallback = Ext.isFunction(callbackFunction) ? callbackFunction : null;
			},

		/**
		 * Reset configuration attributes
		 */
		reset: function() {
			this.configurationSet();
			this.configurationTemplatesSet();
		},

		// SelectedEntity property functions
			/**
			 * @return {Number}
			 */
			selectedEntityIdGet: function() {
				if (Ext.Object.isEmpty(this.selectedEntity))
					return null;

				return this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ID);
			},

			/**
			 * @return {CMDBuild.model.common.tabs.email.SelectedEntity}
			 */
			selectedEntityGet: function() {
				return this.selectedEntity;
			},

			/**
			 * Creates SelectedEntity object and bind relative original object
			 *
			 * @param {Mixed} selectedEntity
			 * @param {Function} callbackFunction
			 */
			selectedEntitySet: function(selectedEntity, callbackFunction) {
				if (!Ext.isFunction(callbackFunction))
					callbackFunction = undefined;

				if (Ext.isEmpty(selectedEntity)) {
					var params = {};
					params[CMDBuild.core.proxy.CMProxyConstants.NOT_POSITIVES] = true;

					CMDBuild.core.proxy.Utils.generateId({
						params: params,
						scope: this,
						success: function(response, options, decodedResponse) {
							this.selectedEntity = Ext.create('CMDBuild.model.common.tabs.email.SelectedEntity', {
								id: decodedResponse.response
							});
						},
						callback: callbackFunction || Ext.emptyFn
					});
				} else if (Ext.isEmpty(selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ID))) {
					var params = {};
					params[CMDBuild.core.proxy.CMProxyConstants.NOT_POSITIVES] = true;

					CMDBuild.core.proxy.Utils.generateId({
						params: params,
						scope: this,
						success: function(response, options, decodedResponse) {
							this.selectedEntity = Ext.create('CMDBuild.model.common.tabs.email.SelectedEntity', {
								id: decodedResponse.response,
								entity: selectedEntity
							});
						},
						callback: callbackFunction || Ext.emptyFn
					});
				} else {
					this.selectedEntity = Ext.create('CMDBuild.model.common.tabs.email.SelectedEntity', {
						id: selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ID),
						entity: selectedEntity
					});

					if (Ext.isFunction(callbackFunction))
						callbackFunction();
				}
			},

		// SendAllOnSave property functions
			/**
			 * @return {Boolean}
			 */
			sendAllOnSaveGet: function() {
				return this.flagSendAllOnSave;
			},

			/**
			 * @param {Boolean} mode
			 */
			sendAllOnSaveSet: function(mode) {
				this.flagSendAllOnSave = Ext.isBoolean(mode) ? mode : false;
			}
	});

})();