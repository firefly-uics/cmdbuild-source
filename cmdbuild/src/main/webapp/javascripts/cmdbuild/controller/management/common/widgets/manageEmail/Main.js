(function () {

	/**
	 * Main widget controller which manage email regeneration methods
	 */
	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.Main', {
		extend: 'CMDBuild.controller.management.common.widgets.CMWidgetController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.EmailTemplates',
			'CMDBuild.core.proxy.Utils',
			'CMDBuild.model.EmailTemplates'
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
		emailTemplatesObjects: [], // TODO

		/**
		 * All templates identifiers I have in widgetConf and grid
		 *
		 * @property {Array}
		 */
		emailTemplatesIdentifiers: [], // TODO

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
		 * @cfg {Boolean}
		 */
		readOnly: undefined,

		/**
		 * @cfg {Boolean}
		 */
		relatedAttributeChanged: false,

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * @property {Boolean}
		 */
		templateResolverIsBusy: false,

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
			this.readOnly = !this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY]; // TODO: refactorizzare senza negazioni

			// Converts widgetConf templates to templates model objects
			Ext.Array.forEach(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.EMAIL_TEMPLATES], function(item, index, allItems) {
				this.widgetConfTemplates.push(this.widgetConfigToModel(item));
			}, this);

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
				case 'onGlobalRegenerationButtonClick':
					return this.onGlobalRegenerationButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

// TODO cancellare robe inutili
//		/**
//		 * Resolve the template only if there are no draft mails, because the draft mails are saved from this step, and assume that
//		 * the user has already modified the template for this step.
//		 */
//		addEmailFromTemplateIfNeeded: function() {
//			this.checkTemplatesToRegenerate();
//
//			if (
//				(
//					this.thereAreTemplates()
//					&& !this.controllerGrid.hasDraftEmails()
//					&& !this.emailsWereGenerated
//				)
//				|| this.forceRegeneration
//			) {
//				this.createEmailFromTemplate();
//			}
//		},

		/**
		 * @override
		 */
		beforeActiveView: function() {
_debug('BEFORE ACTIVE VIEW');
			this.controllerGrid.storeLoad(this.templateResolverInit, this);
		},

		/**
		 * Builds templatesToRegenerate array in relation of dirty fields
		 *
		 * @return {Array} templatesToRegenerate
		 */
		checkTemplatesToRegenerate: function() {
			var templatesToRegenerate = [];
			var dirtyVariables = Ext.Object.getKeys(this.ownerController.view.mainView.getValues(false, true));
_debug('dirtyVariables', dirtyVariables);
			// Complete dirtyVariables array also with multilevel variables (ex. var1 = '... {client:var2} ...')
var xaVars = Ext.apply({}, this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.TEMPLATES], this.extractVariablesForTemplateResolver()); // TODO
_debug('xaVars', xaVars);
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
_debug('dirtyVariables', dirtyVariables);
			// Check templates attributes looking for dirtyVariables as client variables (ex. {client:varName})
			Ext.Array.forEach(this.emailTemplatesObjects, function(template, templateIndex, allTemplatesItems) {
				if (!Ext.Object.isEmpty(template))
					Ext.Object.each(template.getData(), function(key, value, myself) {
						if (typeof value == 'string') { // Check all types of CQL variables that can contains client variables
							this.self.searchForCqlClientVariables(
								value,
								template.get(CMDBuild.core.proxy.CMProxyConstants.NAME),
								dirtyVariables,
								templatesToRegenerate
							);
						}
					}, this);
			}, this);
_debug('templatesToRegenerate', templatesToRegenerate);
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
		 * @return {Number}
		 */
		getActivityId: function() {
			return this.activityId;
		},

		/**
		 * @return {Object}
		 *
		 * @override
		 */
		getData: function() {
			var out = {};
			out[CMDBuild.core.proxy.CMProxyConstants.OUTPUT] = this.getActivityId();

			return out;
		},

		/**
		 * @return {Boolean} templateResolverIsBusy
		 *
		 * @override
		 */
		isBusy: function() {
			this.templateResolverInit(this.regenerateAllEmails, this, [true]);

			return this.templateResolverIsBusy;
		},

		/**
		 * @return {Boolean}
		 *
		 * @override
		 */
		isValid: function() {
			return !(
				this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.REQUIRED]
				&& this.controllerGrid.getOutgoingEmails().length == 0
			);
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
				this.controllerGrid.storeLoad(function() {
					this.templateResolverInit(this.regenerateAllEmails, this, [true]);
				}, this);
		},

		onGlobalRegenerationButtonClick: function() {
			this.templateResolverInit(this.regenerateAllEmails, this, [true]);
		},

		/**
		 * Launch regeneration of all grid records if needed
		 *
		 * @param {Boolean} forceRegeneration
		 */
		regenerateAllEmails: function(forceRegeneration) {
			forceRegeneration = forceRegeneration || false;

			if (
				forceRegeneration
				|| this.relatedAttributeChanged
			) {
//				var regeneratedEmails = [];
//				var objectsToRegenerate = []; // Array with all store records and emailTemplates from widget configuration which needs to be regenerated
				var regeneratedTemplatesIdentifiers = [];
				var emailTemplatesToRegenerate = this.checkTemplatesToRegenerate();

				// Launch regeneration of all grid records
				Ext.Array.forEach(this.controllerGrid.getDraftEmails(), function(item, index, allItems) {
					var recordTemplate = item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);
_debug('item', item);
					if (
						!Ext.isEmpty(recordTemplate)
						&& Ext.Array.contains(emailTemplatesToRegenerate, recordTemplate)
						&& item.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION)
					) {
_debug('if');
						if(this.regenerateEmail(item))
							regeneratedTemplatesIdentifiers.push(recordTemplate);
					}
				}, this);

				// Launch regeneration of all widgetConf templates
				Ext.Array.forEach(this.widgetConfTemplates, function(item, index, allItems) {
					var itemIdentifier = item.get(CMDBuild.core.proxy.CMProxyConstants.KEY);

					if (
						!Ext.isEmpty(itemIdentifier)
						&& Ext.Array.contains(emailTemplatesToRegenerate, itemIdentifier)
						&& !Ext.Array.contains(regeneratedTemplatesIdentifiers, itemIdentifier)
					) {
						if(this.regenerateTemplate(item))
							regeneratedTemplatesIdentifiers.push(itemIdentifier);
					}
				}, this);

//				// Load grid records which need regeneration
//				Ext.Array.forEach(this.controllerGrid.getDraftEmails(), function(item, index, allItems) {
//					var recordTemplateIdentifier = null;
//					var recordTemplate = item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);
//
//					// Apply templateObject to record
//					if (!Ext.isObject(recordTemplate) && Ext.Array.contains(this.emailTemplatesIdentifiers, recordTemplate)) {
//						recordTemplate = Ext.Array.findBy(this.emailTemplatesObjects, function(templateItem, templateIndex) {
//							if (
//								recordTemplate == templateItem.get(CMDBuild.core.proxy.CMProxyConstants.KEY)
//								|| recordTemplate == templateItem.get(CMDBuild.core.proxy.CMProxyConstants.NAME)
//							) {
//								item.set(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, templateItem);
//
//								return true;
//							}
//
//							return false;
//						}, this);
//
//						recordTemplateIdentifier = recordTemplate.get(CMDBuild.core.proxy.CMProxyConstants.KEY) || recordTemplate.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
//
//						if (!Ext.Array.contain(objectsToRegenerateTemplatesIdentifiers, recordTemplateIdentifier))
//							objectsToRegenerateTemplatesIdentifiers.push(recordTemplateIdentifier);
//					}
//
//					if (
//						!Ext.isEmpty(recordTemplateIdentifier)
//						&& Ext.Array.contains(emailTemplatesToRegenerate, recordTemplateIdentifier)
//						&& item.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION)
//					) {
//						objectsToRegenerate.push(item);
//					}
//				}, this);
//
//				// Load widgetConf templates only if not already added inside record
//				Ext.Array.forEach(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.EMAIL_TEMPLATES], function(item, index, allItems) {
//					var configurationtemplateIdentifier = item[CMDBuild.core.proxy.CMProxyConstants.KEY];
//
//					if (!Ext.isEmpty(configurationtemplateIdentifier) && Ext.Array.contains(objectsToRegenerateTemplatesIdentifiers, configurationtemplateIdentifier))
//						objectsToRegenerate.push(this.widgetConfigToModel(item));
//				}, this);
//// TODO: rivedere questa parte inserendo l'IF nella funzione di rigenerazione
//				Ext.Array.forEach(objectsToRegenerate, function(item, index, allItems) {
////					if (item instanceof CMDBuild.model.widget.ManageEmail.email) {
////						if (Ext.Array.contains(templatesToRegenerate, item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)[CMDBuild.core.proxy.CMProxyConstants.ID]) || forceRegeneration)
////							regeneratedEmails.push(this.regenerateEmail(item, item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE))); // Regenerate a grid record
////					} else {
////						if (Ext.Array.contains(templatesToRegenerate, item[CMDBuild.core.proxy.CMProxyConstants.ID]) || forceRegeneration) {
////							item[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = true; // Setup temporary flag
////
////							regeneratedEmails.push(this.regenerateEmail(null, item)); // Regenerate a widget configuration template
////						}
////					}
//					this.regenerateEmail(item);
//				}, this);
//
//				this.relatedAttributeChanged = false;
//_debug('objectsToRegenerate', objectsToRegenerate);
//				this.ownerController.view.mainView.form.initValues(); // Clear form fields dirty state to reset state after regeneration
//_debug('regenerateAllEmails regeneratedEmails', regeneratedEmails);
////				// Adds all templates to store
////				for (var i in regeneratedEmails)
////					this.controllerGrid.addTemplate(regeneratedEmails[i]);
			}
		},

		/**
		 * @WIP TODO: toReport implementation (asking pop-up)
		 *
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 * @param {Boolean} toReport
		 *
		 * @return {Boolean} emailRegenerationStatus
		 */
		regenerateEmail: function(record, toReport) {
_debug('regenerateEmail', record);
_debug('!this.templateResolverIsBusy', !this.templateResolverIsBusy);
			var emailRegenerationStatus = false;

			if (
				!this.templateResolverIsBusy
				&& !Ext.Object.isEmpty(record)
			) {
				var me = this;

				this.templateResolverIsBusy = true;

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
				var xaVars = Ext.apply({}, recordTemplate.getData(), record.getData());

				this.templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: me.clientForm,
					xaVars: xaVars,
					serverVars: this.getTemplateResolverServerVars()
				});
_debug('this.templateResolver', this.templateResolver);
				this.templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(recordTemplate.getData()),
					callback: function(values, ctx) {
_debug('values', values);
						var conditionExpr = values[CMDBuild.core.proxy.CMProxyConstants.CONDITION];

						if (!conditionExpr || me.templateResolver.safeJSEval(conditionExpr)) {
							_msg('Email with subject "' + values[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] + '" regenerated');

							for (var key in values)
								record.set(key, values[key]);
_debug('record', record);
							me.controllerGrid.editRecord(record);

							emailRegenerationStatus = true;
						}

						me.templateResolver.bindLocalDepsChange(function() { // TODO
							if (!me.relatedAttributeChanged) {
								me.relatedAttributeChanged = true;

								CMDBuild.Msg.warn(null, "@@ Attribute related with email templates changed, some mail could be regenerated.");
							}
						});

						me.templateResolverIsBusy = false;
					}
				});
			}

			return emailRegenerationStatus;
		},

		/**
		 * @WIP TODO: toReport implementation (asking pop-up)
		 *
		 * @param {CMDBuild.model.EmailTemplates.singleTemplate} template
		 * @param {Boolean} toReport
		 *
		 * @return {Boolean} emailRegenerationStatus
		 */
		regenerateTemplate: function(template, toReport) {
			var emailRegenerationStatus = false;

			if (
				!this.templateResolverIsBusy
				&& !Ext.Object.isEmpty(template)
			) {
				var me = this;

				this.templateResolverIsBusy = true;

				this.templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(sourceTemplate), // TODO
					callback: function(values, ctx) {
_debug('values', values);
						var conditionExpr = values[CMDBuild.core.proxy.CMProxyConstants.CONDITION];

						if (!conditionExpr || me.templateResolver.safeJSEval(conditionExpr)) {
							_msg('Template with subject "' + values[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] + '" regenerated');

							me.controllerGrid.addRecord(Ext.create('CMDBuild.model.widget.ManageEmail.email', values));

							emailRegenerationStatus = true;
						}

						me.templateResolver.bindLocalDepsChange(function() { // TODO
							if (!me.relatedAttributeChanged) {
								me.relatedAttributeChanged = true;

								CMDBuild.Msg.warn(null, "@@ Attribute related with email templates changed, some mail could be regenerated.");
							}
						});

						me.templateResolverIsBusy = false;
					}
				});
			}

			return emailRegenerationStatus;
		},

		/**
		 * Setup activityId from WorkFlowState module or requires it from server
		 */
		setActivityId: function() {
			if (Ext.isEmpty(this.activityId)) {
				if (_CMWFState.getProcessInstance().getId()) {
					this.activityId = _CMWFState.getProcessInstance().getId();
				} else {
					CMDBuild.core.proxy.Utils.generateId({
						scope: this,
						success: function(response, options, decodedResponse) {
							this.activityId = decodedResponse.response;
						}
					});
				}
			}
		},

		/**
		 * Initialize templateResolver joining grid and widgetConf template objects // TODO: cambiare
		 *
		 * @param {Function} callbackFunction
		 * @param {Object} scope
		 * @param {Array} args
		 */
		templateResolverInit: function(callbackFunction, scope, args) { // TODO: cambiare nome
			var me = this;

			// Reset local storage arrays
			this.emailTemplatesObjects = [];
			this.emailTemplatesIdentifiers = [];
_debug('templateResolverInit', this);
			// Loads widgetConf templates to local array and push key in emailTemplatesIdentifiers array TODO usare il forEach
			for (var i in this.widgetConfTemplates) {
				var template = this.widgetConfTemplates[i];

				if (!Ext.isEmpty(template) && !Ext.Array.contains(this.emailTemplatesIdentifiers, template.get(CMDBuild.core.proxy.CMProxyConstants.KEY))) {
					this.emailTemplatesObjects.push(template);
					this.emailTemplatesIdentifiers.push(template.get(CMDBuild.core.proxy.CMProxyConstants.KEY));
				}
			}

			// Load grid's draft templates names to local array TODO usare il forEach
			var storeItems = this.controllerGrid.getDraftEmails();
			for (var i in storeItems) {
				var templateIdentifier = null;
				var template = storeItems[i].get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);

				if (Ext.isObject(template)) {
					templateIdentifier = template.get(CMDBuild.core.proxy.CMProxyConstants.KEY) || template.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
				} else if (!Ext.isEmpty(template)) {
					templateIdentifier = template;
				}

				if (!Ext.Array.contains(this.emailTemplatesIdentifiers, templateIdentifier))
					this.emailTemplatesIdentifiers.push(templateIdentifier);
			}

			this.view.setLoading(true);
			CMDBuild.core.proxy.EmailTemplates.getAll({
				params: {
					templates: Ext.encode(this.emailTemplatesIdentifiers)
				},
				scope: this,
				failure: function(response, options, decodedResponse) {
					CMDBuild.Msg.error(CMDBuild.Translation.common.failure, '@@ ManageEmail controller error: get template call failure', false);
				},
				success: function(response, options, decodedResponse) {
					var template = decodedResponse.response.elements;
_debug('decodedResponse.response', decodedResponse.response.elements);
					// Load grid's templates to local array
					Ext.Array.forEach(template, function(item, index, allItems) {
						this.emailTemplatesObjects.push(Ext.create('CMDBuild.model.EmailTemplates.singleTemplate', item));
					}, this);
//_debug('me.clientForm', me.clientForm);
//					var xaVars = Ext.apply({}, this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.TEMPLATES], me.extractVariablesForTemplateResolver());
//
//					this.templateResolver = new CMDBuild.Management.TemplateResolver({
//						clientForm: me.clientForm,
//						xaVars: xaVars,
//						serverVars: this.getTemplateResolverServerVars()
//					});
//_debug('this.emailTemplatesObjects', this.emailTemplatesObjects);
				},
				callback: function(options, success, response) {
					this.view.setLoading(false);

					Ext.callback(callbackFunction, scope, args);
				}
			});
		},

		/**
		 * @param {Object} template
		 *
		 * @return {CMDBuild.model.EmailTemplates.singleTemplate} or null
		 */
		widgetConfigToModel: function(template) {
			if (Ext.isObject(template) && !Ext.Object.isEmpty(template)) {
				var model = Ext.create('CMDBuild.model.EmailTemplates.singleTemplate');
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