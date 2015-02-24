(function () {

	/**
	 * Main widget controller which manage email regeneration methods
	 */
	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.Main', {
		extend: 'CMDBuild.controller.management.common.widgets.CMWidgetController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.EmailTemplates',
			'CMDBuild.core.proxy.Utils'
		],

		mixins: {
			observable: 'Ext.util.Observable'
		},

		/**
		 * @cfg {Number}
		 */
		activityId: undefined,

		/**
		 * @property {CMDBuild.model.CMActivityInstance}
		 */
		card: undefined,

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Grid}
		 */
		controllerGrid: undefined,

		/**
		 * All templates I have in widgetConf and grid
		 *
		 * @property {Array}
		 */
		emailTemplatesObjects: [],

		/**
		 * All templates identifiers I have in widgetConf and grid
		 *
		 * @property {Array}
		 */
		emailTemplatesIdentifiers: [],

		/**
		 * Shorthand to view grid
		 *
		 * @property {CMDBuild.controller.management.common.widgets.manageEmail.Grid}
		 */
		grid: undefined,

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		ownerController: undefined,

		/**
		 * Global attribute change flag
		 *
		 * @cfg {Boolean}
		 */
		relatedAttributeChanged: false,

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.linkCards.LinkCards}
		 */
		view: undefined,

		/**
		 * @cfg {Object}
		 */
		widgetConf: undefined,

		/**
		 * @cfg {Array}
		 */
		widgetConfTemplates: [],

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.manageEmail.MainPanel.WIDGET_NAME,

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
_debug('searchForCqlClientVariables', inspectingVariable + ' ' + inspectingVariableKey + ' ' + searchedVariablesNames + ' ' + foundedKeysArray);
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
			}
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.manageEmail.MainPanel} view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} ownerController
		 * @param {Object} widgetConf
		 * @param {Ext.form.Basic} clientForm
		 * @param {CMDBuild.model.CMActivityInstance} card
		 *
		 * @override
		 */
		constructor: function(view, ownerController, widgetConf, clientForm, card) {
			this.mixins.observable.constructor.call(this);

			this.callParent(arguments);

			this.grid = this.view.grid;
			this.view.delegate = this;
_debug('this.widgetConf', this.widgetConf);
_debug('this.ownerController', this.ownerController);
_debug('this.card', this.card);
			this.widgetConfTemplates = []; // Reset variable

			// Converts widgetConf templates to templates model objects
			Ext.Array.forEach(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.TEMPLATES], function(item, index, allItems) {
				this.widgetConfTemplates.push(this.widgetConfigToModel(item));
			}, this);
_debug('this.widgetConfTemplates', this.widgetConfTemplates);
			// Build controllers
			this.controllerGrid = Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.Grid', {
				parentDelegate: this,
				view: this.grid
			});
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'getWidgetConf':
					return this.getWidgetConf();

				case 'getWidgetController':
					return this.getWidgetController();

				case 'onGlobalRegenerationButtonClick':
					return this.onGlobalRegenerationButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @override
		 */
		beforeActiveView: function() {
_debug('BEFORE ACTIVE VIEW');
_debug('grid store', this.grid.getStore());
			this.controllerGrid.storeLoad(true);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 * @param {CMDBuild.Management.TemplateResolver} templateResolver
		 * @param {Object} scope
		 */
		bindLocalDepsChangeEvent: function(record, templateResolver, scope) {
_debug('bindLocalDepsChangeEvent', record);
			templateResolver.bindLocalDepsChange(function() {
				if (!Ext.Object.isEmpty(record)) {
					if (record.get(CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION)) {
						Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.ConfirmRegenerationWindow', {
							parentDelegate: scope,
							gridDelegate: scope.controllerGrid
						});
					} else if (!scope.relatedAttributeChanged) {
						scope.relatedAttributeChanged = true;

						CMDBuild.Msg.warn(null, CMDBuild.Translation.warnings.emailTemplateRelatedAttributeEdited);
					}
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
_debug('checkCondition conditionExpr', conditionExpr);
			return Ext.isEmpty(conditionExpr) || templateResolver.safeJSEval(conditionExpr);
		},

		/**
		 * Builds templatesToRegenerate array in relation of dirty fields
		 *
		 * @return {Array} templatesToRegenerate
		 */
		checkTemplatesToRegenerate: function() {
_debug('### checkTemplatesToRegenerate');
			var templatesToRegenerate = [];
			var dirtyVariables = Ext.Object.getKeys(this.ownerController.view.mainView.getValues(false, true));
			var xaVars = this.extractVariablesForTemplateResolver();

			this.ownerController.view.mainView.form.initValues(); // Clear form fields dirty state to reset state after regeneration
_debug('checkTemplatesToRegenerate dirtyVariables', dirtyVariables);
_debug('checkTemplatesToRegenerate xaVars', xaVars);
			// Complete dirtyVariables array also with multilevel variables (ex. var1 = '... {client:var2} ...')
			for (var i in xaVars) {
				var variable = xaVars[i] || [];

				if (
					!Ext.isEmpty(variable)
					&& !Ext.isObject(variable)
					&& typeof variable == 'string'
				) {
					this.self.searchForCqlClientVariables(
						variable,
						i,
						dirtyVariables,
						dirtyVariables
					);
				}
			}
_debug('checkTemplatesToRegenerate this.emailTemplatesObjects', this.emailTemplatesObjects);
_debug('checkTemplatesToRegenerate dirtyVariables', dirtyVariables);
			// Check templates attributes looking for dirtyVariables as client variables (ex. {client:varName})
			Ext.Array.forEach(this.emailTemplatesObjects, function(template, templateIndex, allTemplatesItems) {
				if (!Ext.Object.isEmpty(template))
					Ext.Object.each(template.getData(), function(key, value, myself) {
						if (typeof value == 'string') { // Check all types of CQL variables that can contains client variables
							this.self.searchForCqlClientVariables(
								value,
								template.get(CMDBuild.core.proxy.CMProxyConstants.KEY) || template.get(CMDBuild.core.proxy.CMProxyConstants.NAME),
								dirtyVariables,
								templatesToRegenerate
							);
						}
					}, this);
			}, this);
_debug('checkTemplatesToRegenerate templatesToRegenerate', templatesToRegenerate);
			return templatesToRegenerate;
		},

		/**
		 * Extract the variables of each EmailTemplate object, add a suffix to them with the index, and put them all in the templates map.
		 * This is needed to be passed as a unique map to the template resolver.
		 *
		 * @return {Object} variables
		 */
		extractVariablesForTemplateResolver: function() {
			var variables = {};
_debug('extractVariablesForTemplateResolver this.emailTemplatesObjects', this.emailTemplatesObjects);
			Ext.Array.forEach(this.emailTemplatesObjects, function(item, index, allItems) {
				var templateObject = item.getData();
_debug('extractVariablesForTemplateResolver templateObject', templateObject);
				var templateVariables = item.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES);

				for (var key in templateVariables)
					variables[key] = templateVariables[key];

				for (var key in templateObject)
					variables[key + (index + 1)] = templateObject[key];
			}, this);

			return variables;
		},

		/**
		 * @return {Number}
		 */
		getActivityId: function() {
			return this.activityId;
		},

		/**
		 * @param {Boolean} regenerateAllEmails
		 * @param {Boolean} forceRegeneration
		 */
		getAllTemplatesData: function(regenerateAllEmails, forceRegeneration) {
_debug('#### getAllTemplatesData ' + regenerateAllEmails + ' ' + forceRegeneration);
			regenerateAllEmails = regenerateAllEmails || false;
			forceRegeneration = forceRegeneration || false;
//			var me = this;

			// Reset local storage arrays
			this.emailTemplatesObjects = [];
			this.emailTemplatesIdentifiers = [];

			// Loads widgetConf templates to local array and push key in emailTemplatesIdentifiers array
			Ext.Array.forEach(this.widgetConfTemplates, function(template, index, allItems) {
_debug('this.widgetConfTemplates item', template);
				if (!Ext.isEmpty(template) && !Ext.Array.contains(this.emailTemplatesIdentifiers, template.get(CMDBuild.core.proxy.CMProxyConstants.KEY))) {
					this.emailTemplatesObjects.push(template);
					this.emailTemplatesIdentifiers.push(template.get(CMDBuild.core.proxy.CMProxyConstants.KEY));
				}
			}, this);

			// Load grid's draft templates names to local array
			Ext.Array.forEach(this.controllerGrid.getDraftEmails(), function(record, index, allItems) {
_debug('this.widgetConfTemplates item', template);
				var templateIdentifier = null;
				var template = record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);
_debug('this.getDraftEmails item', template);
				if (Ext.isObject(template)) {
					templateIdentifier = template.get(CMDBuild.core.proxy.CMProxyConstants.KEY) || template.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
				} else if (!Ext.isEmpty(template)) {
					templateIdentifier = template;
				}
_debug('templateIdentifier', templateIdentifier);
				if (!Ext.isEmpty(templateIdentifier) && !Ext.Array.contains(this.emailTemplatesIdentifiers, templateIdentifier))
					this.emailTemplatesIdentifiers.push(templateIdentifier);
			}, this);

			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.EmailTemplates.getAll({
				params: {
					templates: Ext.encode(this.emailTemplatesIdentifiers)
				},
				scope: this,
				failure: function(response, options, decodedResponse) {
					CMDBuild.Msg.error(
						CMDBuild.Translation.common.failure,
						Ext.String.format(CMDBuild.Translation.errors.getTemplateWithNameFailure, this.selectedName),
						false
					);
				},
				success: function(response, options, decodedResponse) {
					var template = decodedResponse.response.elements;
_debug('decodedResponse.response', decodedResponse.response.elements);
					// Load grid's templates to local array
					Ext.Array.forEach(template, function(item, index, allItems) {
_debug('loadTemplates item', item);
						this.emailTemplatesObjects.push(Ext.create('CMDBuild.model.widget.ManageEmail.template', item));
					}, this);
				},
				callback: function(options, success, response) {
					CMDBuild.LoadMask.get().hide();

					if (regenerateAllEmails)
						this.regenerateAllEmails(forceRegeneration);
				}
			});
		},

		/**
		 * @return {Object}
		 *
		 * @override
		 */
		getData: function() {
			var out = {};
			out[CMDBuild.core.proxy.CMProxyConstants.OUTPUT] = this.getActivityId();
_debug('getData', out);
			return out;
		},

		/**
		 * @return {Object}
		 */
		getWidgetConf: function() {
			return this.widgetConf;
		},

		/**
		 * @return {CMDBuild.controller.management.common.widgets.manageEmail.Main}
		 */
		getWidgetController: function() {
			return this;
		},

		/**
		 * @return {Boolean}
		 *
		 * @override
		 */
		isValid: function() {
			if (!Ext.isEmpty(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.REQUIRED]) && this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.REQUIRED])
				return this.controllerGrid.getDraftEmails().length > 0;

			return this.callParent(arguments);
		},

		/**
		 * Initialize widget on widget configuration to apply all events on form fields
		 *
		 * @override
		 */
		onEditMode: function() {
_debug('onEditMode');
			this.setActivityId();

			if (!this.grid.getStore().isLoading())
				this.controllerGrid.storeLoad(true, true);
		},

		onGlobalRegenerationButtonClick: function() {
			this.getAllTemplatesData(true, true);
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
_debug('regenerateAllEmails forceRegeneration', forceRegeneration);
_debug('regenerateAllEmails this.relatedAttributeChanged', this.relatedAttributeChanged);
			if (forceRegeneration || this.relatedAttributeChanged) {
				// Reset all store before email regeneration
				if (forceRegeneration)
					this.controllerGrid.storeReset();

				var regeneratedTemplatesIdentifiers = [];
				var emailTemplatesToRegenerate = this.checkTemplatesToRegenerate();
_debug('draft emails', this.controllerGrid.getDraftEmails());
				// Launch regeneration of all grid records
				Ext.Array.forEach(this.controllerGrid.getDraftEmails(), function(item, index, allItems) {
					var recordTemplate = item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);
_debug('checking item', item);
_debug(!Ext.isEmpty(recordTemplate) + ' ' + Ext.Array.contains(emailTemplatesToRegenerate, recordTemplate) + ' ' + item.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION));
					if (
						(
							!Ext.isEmpty(recordTemplate)
							&& Ext.Array.contains(emailTemplatesToRegenerate, recordTemplate)
							&& item.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION)
						)
						|| forceRegeneration
					) {
						if(this.regenerateEmail(item, regenerationTrafficLightArray))
							regeneratedTemplatesIdentifiers.push(recordTemplate);
					}
				}, this);

				// Launch regeneration of all widgetConf templates
_debug('this.widgetConfTemplates', this.widgetConfTemplates);
_debug('regeneratedTemplatesIdentifiers', regeneratedTemplatesIdentifiers);
				Ext.Array.forEach(this.widgetConfTemplates, function(item, index, allItems) {
					var templateIdentifier = item.get(CMDBuild.core.proxy.CMProxyConstants.KEY);

					if (
						(
							!Ext.isEmpty(templateIdentifier)
							&& Ext.Array.contains(emailTemplatesToRegenerate, templateIdentifier)
							&& !Ext.Array.contains(regeneratedTemplatesIdentifiers, templateIdentifier) // Avoid to generate already regenerated templates
						)
						|| forceRegeneration
					) {
						if(this.regenerateTemplate(item, regenerationTrafficLightArray))
							regeneratedTemplatesIdentifiers.push(templateIdentifier);
					}
				}, this);

				this.relatedAttributeChanged = false; // Reset attribute changed flag
			}
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 * @param {Array} regenerationTrafficLightArray
		 *
		 * @return {Boolean} emailRegenerationStatus
		 */
		regenerateEmail: function(record, regenerationTrafficLightArray) {
_debug('regenerateEmail', record);
			var emailRegenerationStatus = false;

			if (
				!Ext.Object.isEmpty(record)
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
_debug('regenerateEmail recordTemplate', recordTemplate);
				var templateData = Ext.apply({}, recordTemplate.getData(), recordTemplate.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES));
_debug('regenerateEmail templateData', templateData);
				var xaVars = Ext.apply({}, templateData, record.getData());
_debug('regenerateEmail apply', [templateData, record.getData()]);
_debug('regenerateEmail xaVars', xaVars);
				var templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: this.clientForm,
					xaVars: xaVars,
					serverVars: this.getTemplateResolverServerVars()
				});
_debug('regenerateEmail this.templateResolver', templateResolver);
				templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(xaVars),
					callback: function(values, ctx) {
_debug('regenerateEmail values', values);
						if (me.checkCondition(values, templateResolver)) {
							_msg('Email with subject "' + values[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] + '" regenerated');

							for (var key in values)
								record.set(key, values[key]);
_debug('regenerateEmail record', record);
							// TrafficLight slot build
							var trafficLight = [];
							trafficLight[CMDBuild.core.proxy.CMProxyConstants.STATUS] = false;
							trafficLight[CMDBuild.core.proxy.CMProxyConstants.RECORD] = record; // Reference to record

							regenerationTrafficLightArray.push(trafficLight);

							me.controllerGrid.editRecord(record, regenerationTrafficLightArray);

							emailRegenerationStatus = true;

							me.bindLocalDepsChangeEvent(record, templateResolver, me);
						} else {
_debug('regenerateEmail remove record', record);
							me.controllerGrid.removeRecord(record);
						}
					}
				});
			}

			return emailRegenerationStatus;
		},

		/**
		 * Launch regeneration only of selected grid records
		 *
		 * @param {Array} records
		 */
		regenerateSelectedEmails: function(records) {
			if (!Ext.isEmpty(records)) {

				Ext.Array.forEach(records, function(item, index, allItems) {
					var recordTemplate = item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);

					if (
						!Ext.isEmpty(recordTemplate)
						&& item.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION)
					) {
						this.regenerateEmail(item);
					}
				}, this);

				this.relatedAttributeChanged = false; // Reset attribute changed flag

				this.controllerGrid.storeLoad(); // Load at end of all changes
			}
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.template} template
		 * @param {Array} regenerationTrafficLightArray
		 *
		 * @return {Boolean} templateRegenerationStatus
		 */
		regenerateTemplate: function(template, regenerationTrafficLightArray) {
_debug('regenerateTemplate', template);
			var templateRegenerationStatus = false;

			if (!Ext.Object.isEmpty(template)) {
				var me = this;
				var xaVars = Ext.apply({}, template.getData(), template.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES));
_debug('regenerateTemplate apply', [xaVars]);
				var templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: me.clientForm,
					xaVars: xaVars,
					serverVars: this.getTemplateResolverServerVars()
				});
_debug('regenerateTemplate this.templateResolver', templateResolver);
				templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(xaVars),
					callback: function(values, ctx) {
_debug('regenerateTemplate values', values);
						var emailObject = null;

						if (me.checkCondition(values, templateResolver)) {
_debug('regenerateTemplate me.controllerGrid.getDraftEmails()', me.controllerGrid.getDraftEmails());
							var record = Ext.Array.findBy(me.controllerGrid.getDraftEmails(), function(item, index) {
								if (item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE) == template.get(CMDBuild.core.proxy.CMProxyConstants.KEY))
									return true;

								return false;
							});
_debug('regenerateTemplate record', record);
							// Update record data with values
							if (!Ext.Object.isEmpty(record))
								values = Ext.Object.merge(record.getData(), values);

							emailObject = Ext.create('CMDBuild.model.widget.ManageEmail.email', values);
							emailObject.set(CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID, me.getActivityId());
							emailObject.set(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, template.get(CMDBuild.core.proxy.CMProxyConstants.KEY));

							_msg('Template with subject "' + values[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] + '" regenerated');
_debug('regenerateTemplate emailObject', emailObject);
							if (Ext.isEmpty(record)) {
								me.controllerGrid.addRecord(emailObject, regenerationTrafficLightArray);
							} else {
								me.controllerGrid.editRecord(emailObject, regenerationTrafficLightArray);
							}

							templateRegenerationStatus = true;

							me.bindLocalDepsChangeEvent(emailObject, templateResolver, me);
						}
					}
				});
			}

			return templateRegenerationStatus;
		},

		/**
		 * @param {String} templateIdentifier
		 *
		 * @return {Boolean} response
		 */
		resolveTemplateCondition: function(templateIdentifier) {
			var me = this;
			var templateObject = null;
			var response = false;

			if (!Ext.isEmpty(templateIdentifier) && typeof templateIdentifier == 'string') {
				Ext.Array.forEach(this.emailTemplatesObjects, function(item, index, allItems) {
					if (item.get(CMDBuild.core.proxy.CMProxyConstants.NAME) == templateIdentifier)
						templateObject = item;
				}, this);

				var xaVars = Ext.apply({}, templateObject.getData(), templateObject.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES));
_debug('resolveTemplateCondition xaVars', xaVars);
_debug('resolveTemplateCondition templateObject.getData()', templateObject.getData());
				var templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: me.clientForm,
					xaVars: xaVars,
					serverVars: this.getTemplateResolverServerVars()
				});

				templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(xaVars),
					callback: function(values, ctx) {
						if (me.checkCondition(values, templateResolver))
							response = true;
					}
				});
			}

			return response;
		},

		/**
		 * Setup activityId from WorkFlowState module or requires it from server
		 */
		setActivityId: function() {
_debug('setActivityId');
			if (Ext.isEmpty(this.activityId)) {
				if (_CMWFState.getProcessInstance().getId()) {
					this.activityId = _CMWFState.getProcessInstance().getId();
				} else {
					var params = {};
					params[CMDBuild.core.proxy.CMProxyConstants.NOT_POSITIVES] = true;

					CMDBuild.core.proxy.Utils.generateId({
						params: params,
						scope: this,
						success: function(response, options, decodedResponse) {
							this.activityId = decodedResponse.response;
_debug('setActivityId this.activityId', this.activityId);
						}
					});
				}
			}
		},

		/**
		 * @param {Object} template
		 *
		 * @return {CMDBuild.model.widget.ManageEmail.template} or null
		 */
		widgetConfigToModel: function(template) {
			if (Ext.isObject(template) && !Ext.Object.isEmpty(template)) {
				var model = Ext.create('CMDBuild.model.widget.ManageEmail.template');
				model.set(CMDBuild.core.proxy.CMProxyConstants.BCC, template[CMDBuild.core.proxy.CMProxyConstants.BCC_ADDRESSES]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.BODY, template[CMDBuild.core.proxy.CMProxyConstants.CONTENT]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.CC, template[CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.CONDITION, template[CMDBuild.core.proxy.CMProxyConstants.CONDITION]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.FROM, template[CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION, template[CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.KEY, template[CMDBuild.core.proxy.CMProxyConstants.KEY]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX, template[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION, template[CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.SUBJECT, template[CMDBuild.core.proxy.CMProxyConstants.SUBJECT]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.TO, template[CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.VARIABLES, template[CMDBuild.core.proxy.CMProxyConstants.VARIABLES]);

				return model;
			}

			return null;
		},
	});

})();