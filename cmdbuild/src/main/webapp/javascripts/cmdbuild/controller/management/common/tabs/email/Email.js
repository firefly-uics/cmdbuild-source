(function () {

	/**
	 * Main controller which manage email regeneration methods
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.management.common.tabs.email.Email', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.EmailTemplates',
			'CMDBuild.core.proxy.Utils'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * Form where to get fields data
		 *
		 * @cfg {Mixed}
		 */
		clientForm: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'getConfiguration',
			'getGlobalLoadMask',
			'getMainController',
			'getSelectedEntityId',
			'onEmailPanelShow',
			'onGlobalRegenerationButtonClick'
		],

		/**
		 * @cfg {Object}
		 */
		configuration: {},

		/**
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
		 * Flag to mark when performing save action
		 *
		 * @cfg {Boolean}
		 */
		flagPerformSaveAction: false,

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
		 * @cfg {Boolean}
		 */
		isWidgetBusy: false,

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
		selectedEntity: {},

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.EmailPanel}
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
				if (!Ext.isEmpty(regenerationTrafficLightArray) && regenerationTrafficLightArray.length > 0) {
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

				return false;
			},

			/**
			 * @param {Mixed} record
			 * @param {Array} trafficLightArray
			 */
			trafficLightSlotBuild: function(record, trafficLightArray) {
				if (!Ext.isEmpty(trafficLightArray)) {
					var trafficLight = [];
					trafficLight[CMDBuild.core.proxy.CMProxyConstants.STATUS] = false;
					trafficLight[CMDBuild.core.proxy.CMProxyConstants.RECORD] = record; // Reference to record

					trafficLightArray.push(trafficLight);
				}
			}
		},

		/**
		 * Abstract constructor that must be extended implementing creation and setup of view
		 *
		 * @param {Object} configObject
		 * @param {Mixed} configObject.parentDelegate - CMModCardController or CMModWorkflowController
		 * @param {Mixed} configObject.selectedEntity - Card or Activity in edit
		 * @param {Mixed} configObject.ownerEntityobject
		 * @param {Mixed} configObject.widgetConf
		 *
		 * @abstract
		 */
		constructor: function(configObject) {
			// Setup configurationObject applying defaultConfiguration attributes to widgetConf
			Ext.apply(this.configuration, configObject.widgetConf, this.defaultConfiguration);

			this.setSelectedEntity(configObject.selectedEntity);

			delete configObject.selectedEntity;
			delete configObject.widgetConf;

			Ext.apply(this, configObject); // Apply config

			// Converts configuration templates to templates model objects
			this.configurationTemplates = []; // Reset variable

			Ext.Array.forEach(this.configuration[CMDBuild.core.proxy.CMProxyConstants.TEMPLATES], function(item, index, allItems) {
				this.configurationTemplates.push(this.configurationTemplatesToModel(item));
			}, this);

			// Build controllers
			this.controllerGrid = Ext.create('CMDBuild.controller.management.common.tabs.email.Grid', {
				parentDelegate: this
			});
			this.grid = this.controllerGrid.getView();

			this.controllerConfirmRegenerationWindow = Ext.create('CMDBuild.controller.management.common.tabs.email.ConfirmRegenerationWindow', {
				parentDelegate: this,
				gridDelegate: this.controllerGrid
			});

//			this.view = Ext.create('CMDBuild.view.management.common.tabs.email.EmailPanel', {
//				delegate: this
//			});
//
//			this.view.add(this.grid);
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

					if (!record.get(CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION))
						CMDBuild.Msg.warn(null, CMDBuild.Translation.warnings.emailTemplateRelatedAttributeEdited);
				}
			});
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
			var dirtyVariables = Ext.Object.getKeys(this.clientForm.getValues(false, true));
			var xaVars = this.extractVariablesForTemplateResolver();

			this.clientForm.owner.initValues(); // Clear form fields dirty state to reset state after regeneration

			// Complete dirtyVariables array also with multilevel variables (ex. var1 = '... {client:var2} ...')
			for (var i in xaVars) {
				var variable = xaVars[i] || [];

				if (
					!Ext.isEmpty(variable)
					&& !Ext.isObject(variable)
					&& typeof variable == 'string'
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
						if (typeof value == 'string') { // Check all types of CQL variables that can contains client variables
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

		/**
		 * @param {Object} template
		 *
		 * @return {CMDBuild.model.common.tabs.email.Template} or null
		 */
		configurationTemplatesToModel: function(template) { // TODO: forse sarà da delegare al controller del vero widget
			if (Ext.isObject(template) && !Ext.Object.isEmpty(template)) {
				var model = Ext.create('CMDBuild.model.common.tabs.email.Template');
				model.set(CMDBuild.core.proxy.CMProxyConstants.ACCOUNT, template[CMDBuild.core.proxy.CMProxyConstants.ACCOUNT]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.BCC, template[CMDBuild.core.proxy.CMProxyConstants.BCC_ADDRESSES]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.BODY, template[CMDBuild.core.proxy.CMProxyConstants.CONTENT]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.CC, template[CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.CONDITION, template[CMDBuild.core.proxy.CMProxyConstants.CONDITION]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.FROM, template[CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION, template[CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.KEY, template[CMDBuild.core.proxy.CMProxyConstants.KEY]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH, template[CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX, template[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION, template[CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.SUBJECT, template[CMDBuild.core.proxy.CMProxyConstants.SUBJECT]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.TO, template[CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.VARIABLES, template[CMDBuild.core.proxy.CMProxyConstants.VARIABLES]);

				return model;
			}

			return null;
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

		/**
		 * @param {Boolean} regenerateAllEmails
		 * @param {Boolean} forceRegeneration
		 */
		getAllTemplatesData: function(regenerateAllEmails, forceRegeneration) {
			regenerateAllEmails = regenerateAllEmails || false;
			forceRegeneration = forceRegeneration || false;

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

			CMDBuild.core.proxy.EmailTemplates.getAll({
				params: {
					templates: Ext.encode(this.emailTemplatesIdentifiers)
				},
				scope: this,
				loadMask: this.globalLoadMask,
				failure: function(response, options, decodedResponse) {
					CMDBuild.Msg.error(
						CMDBuild.Translation.common.failure,
						Ext.String.format(CMDBuild.Translation.errors.getTemplateWithNameFailure, this.selectedName),
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
					if (regenerateAllEmails) {
						this.regenerateAllEmails(forceRegeneration);
					} else { // Reset widget busy state to false
						this.isWidgetBusy = false;

						// Set all email as outgoing because i've regenerated all of them and i'm saving card
						if (this.flagPerformSaveAction) {
							this.flagPerformSaveAction = false;

							if (!Ext.isEmpty(this.controllerGrid.getDraftEmails()))
								this.controllerGrid.sendAll();
						}
					}
				}
			});
		},

		/**
		 * @return {Object}
		 */
		getConfiguration: function() {
			return this.configuration;
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

		/**
		 * @return {Number}
		 */
		getSelectedEntityId: function() {
			if (Ext.isEmpty(this.selectedEntity)) {
				_msg('WARNING CMDBuild.controller.management.common.tabs.email.Email: Selected entity object is empty');

				return null;
			}

			return this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ID);
		},

		/**
		 * Used to mark widget as busy during regenerations, especially useful for getData() regeneration
		 *
		 * @return {Boolean}
		 *
		 * @override
		 */
		isBusy: function() {
			return this.isWidgetBusy;
		},

		/**
		 * @return {Boolean}
		 *
		 * @override
		 */
		isValid: function() {
			if (this.configuration[CMDBuild.core.proxy.CMProxyConstants.REQUIRED])
				return this.controllerGrid.getDraftEmails().length > 0;

			return this.callParent(arguments);
		},

		onGlobalRegenerationButtonClick: function() {
			this.getAllTemplatesData(true, true);
		},

		/**
		 * Reload store every time panel is showed
		 */
		onEmailPanelShow: function() {
			this.controllerGrid.storeLoad();
		},

		/**
		 * Launch regeneration of all grid records if needed.
		 *
		 * {regenerationTrafficLightArray} Implements a trafficLight functionality to manage multiple asynchronous calls and have a global callback
		 * to reload grid only at real end of calls and avoid to have multiple and useless store load calls.
		 *
		 * @param {Boolean} forceRegeneration
		 */
		regenerateAllEmails: function(forceRegeneration) {
			forceRegeneration = forceRegeneration || false;

			var regenerationTrafficLightArray = [];

			this.controllerConfirmRegenerationWindow.reset();

			if (forceRegeneration || this.relatedAttributeChanged) {
				var templatesCheckedForRegenerationIdentifiers = [];
				var emailTemplatesToRegenerate = this.checkTemplatesToRegenerate();

				// Build records to regenerate array
				Ext.Array.forEach(this.controllerGrid.getDraftEmails(), function(item, i, allItems) {
					var recordTemplate = item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);

					if (
						this.controllerGrid.isRegenerable(item)
						&& (
							Ext.Array.contains(emailTemplatesToRegenerate, recordTemplate)
							|| forceRegeneration
						)
						&& item.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION)
					) {
						if (item.get(CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION) && !forceRegeneration) { // PromptSynch implementation
							this.controllerConfirmRegenerationWindow.addRecordToArray(item);
						} else {
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
							|| forceRegeneration
						)
						&& !Ext.Array.contains(templatesCheckedForRegenerationIdentifiers, templateIdentifier) // Avoid to generate already regenerated templates
					) {
						if (item.get(CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION) && !forceRegeneration) { // PromptSynch implementation
							this.controllerConfirmRegenerationWindow.addTemplateToArray(item);
						} else {
							this.regenerateTemplate(item, regenerationTrafficLightArray);
						}
					}

					templatesCheckedForRegenerationIdentifiers.push(templateIdentifier);
				}, this);

				this.controllerConfirmRegenerationWindow.beforeShow();

				this.relatedAttributeChanged = false; // Reset attribute changed flag
			} else { // Reset widget busy state to false
				this.isWidgetBusy = false;
			}
		},

		/**
		 * @param {Mixed} record
		 * @param {Array} regenerationTrafficLightArray
		 */
		regenerateEmail: function(record, regenerationTrafficLightArray) {
			regenerationTrafficLightArray = regenerationTrafficLightArray || [];

			if (
				!Ext.Object.isEmpty(record)
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

				var templateData = Ext.apply({}, recordTemplate.getData(), recordTemplate.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES));
				var xaVars = Ext.apply({}, templateData, record.getData());

				var templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: this.clientForm,
					xaVars: xaVars,
					serverVars: this.getTemplateResolverServerVars()
				});

				templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(xaVars),
					callback: function(values, ctx) {
						for (var key in values)
							record.set(key, values[key]);

						if (me.checkCondition(values, templateResolver)) {
							_msg('Email with subject "' + values[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] + '" regenerated');

							me.self.trafficLightSlotBuild(record, regenerationTrafficLightArray);

							me.controllerGrid.editRecord(record, regenerationTrafficLightArray);
						} else {
							me.controllerGrid.removeRecord(record);
						}

						me.bindLocalDepsChangeEvent(record, templateResolver, me);
					}
				});
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
			regenerationTrafficLightArray = regenerationTrafficLightArray || [];

			if (!Ext.Object.isEmpty(template)) {
				var me = this;
				var xaVars = Ext.apply({}, template.getData(), template.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES));

				var templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: me.clientForm,
					xaVars: xaVars,
					serverVars: this.getTemplateResolverServerVars()
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
						emailObject.set(CMDBuild.core.proxy.CMProxyConstants.REFERENCE, me.getSelectedEntityId());
						emailObject.set(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, template.get(CMDBuild.core.proxy.CMProxyConstants.KEY));
						emailObject.set(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY, this.cmfg('getSelectedEntityId') < 0); // Setup temporary parameter

						me.self.trafficLightSlotBuild(emailObject, regenerationTrafficLightArray);

						if (me.checkCondition(values, templateResolver)) {
							_msg('Template with subject "' + values[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] + '" regenerated');

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

		/**
		 * Rebuild all widget with new configuration object
		 *
		 * @param {Object} configuration
		 */
		setConfiguration: function(configuration) {
			if (!Ext.Object.isEmpty(configuration)) {
				// Setup configurationObject applying defaultConfiguration attributes to widgetConf
				Ext.apply(this.configuration, configuration, this.defaultConfiguration);

				this.view.removeAll();

				this.controllerGrid = Ext.create('CMDBuild.controller.management.common.tabs.email.Grid', {
					parentDelegate: this
				});

				this.grid = this.controllerGrid.getView();

				this.view.add(this.grid);
			}
		},

		/**
		 * Creates SelectedEntity object and bind relative original object
		 *
		 * @param {Mixed} selectedEntity
		 */
		setSelectedEntity: function(selectedEntity) {
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
					}
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
					}
				});
			} else {
				this.selectedEntity = Ext.create('CMDBuild.model.common.tabs.email.SelectedEntity', {
					id: selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ID),
					entity: selectedEntity
				});
			}
		}
	});

})();