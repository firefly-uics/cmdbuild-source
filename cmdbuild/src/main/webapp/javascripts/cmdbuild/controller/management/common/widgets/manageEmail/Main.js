(function () {

	/**
	 * Main widget controller which manage email regeneration methods
	 */
	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.Main', {
		extend: 'CMDBuild.controller.management.common.widgets.CMWidgetController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
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
		 * WidgetConf convenience shorthand
		 *
		 * @property {Object} variables
		 */
		emailTemplates: undefined,

		/**
		 * Shorthand to view grid
		 *
		 * @property {CMDBuild.controller.management.common.widgets.manageEmail.Grid}
		 */
		grid: undefined,

		/**
		 * Flag used to check first widget load time
		 *
		 * @cfg {Boolean}
		 */
		isFirstRegenerationDone: false,

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
			this.emailTemplates = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.EMAIL_TEMPLATES] || {};
			this.readOnly = !this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY];

			var xaVars = Ext.apply({}, this.emailTemplates, this.extractVariablesForTemplateResolver());

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: xaVars,
				serverVars: this.getTemplateResolverServerVars()
			});

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
			this.controllerGrid.storeLoad();
		},

		/**
		 * Builds templatesToRegenerate array in relation of dirty fields
		 *
		 * @return {Array} templatesToRegenerate
		 */
		checkTemplatesToRegenerate: function() {
_debug('this.emailTemplates', this.emailTemplates);
			var templatesToRegenerate = [];
			var dirtyVariables = Ext.Object.getKeys(this.ownerController.view.mainView.getValues(false, true));

			// Complete dirtyVariables array also with multilevel variables (ex. var1 = '... {client:var2} ...')
			for (var i in this.templateResolver.xaVars) {
				var variable = this.templateResolver.xaVars[i] || [];

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

			// Check templates attributes looking for dirtyVariables as client variables (ex. {client:varName})
			for (var i in this.emailTemplates) {
				var template = this.emailTemplates[i];

				if (!Ext.Object.isEmpty(template))
					for (var j in template) {
						var templateAttribute = template[j] || [];

						if (
							!Ext.isObject(templateAttribute)
							&& typeof templateAttribute == 'string'
						) {
							// Check all types of CQL variables that can contains client variables
							this.self.searchForCqlClientVariables(
								templateAttribute,
								template[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_ID],
								dirtyVariables,
								templatesToRegenerate
							);
						}
					}
			}

			return templatesToRegenerate;
		},

		/**
		 * Extract the variables of each EmailTemplate, add a suffix to them with the index, and put them all in the templates map.
		 * This is needed to be passed as a unique map to the template resolver.
		 *
		 * @return {Object} variables
		 */
		extractVariablesForTemplateResolver: function() {
			var variables = {};

			for (var i = 0; i < this.emailTemplates.length; ++i) {
				var t = this.emailTemplates[i].variables;

				for (var key in t)
					variables[key] = t[key];

				t = this.emailTemplates[i];

				for (var key in t)
					variables[key + (i + 1)] = t[key];
			}

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
			return {
				Updated: this.controllerGrid.getOutgoingEmails(true),
				Deleted: this.controllerGrid.getDeletedEmails()
			};
		},

		/**
		 * @return {Boolean} templateResolverIsBusy
		 *
		 * @override
		 */
		isBusy: function() {
			this.regenerateAllEmails();

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
			this.setActivityId();

			if (!this.grid.getStore().isLoading())
				this.controllerGrid.storeLoad(this.regenerateAllEmails);
		},

		onGlobalRegenerationButtonClick: function() {
			this.regenerateAllEmails(true);
		},

		/**
		 * @WIP TODO
		 *
		 * Regenerates email resolving all internal CQL templates
		 *
		 * @param {Boolean} forceRegeneration
		 */
		regenerateAllEmails: function(forceRegeneration) {
			forceRegeneration = forceRegeneration || false;

			if (
				forceRegeneration
				|| !this.isFirstRegenerationDone // Regenerate first time widget is loaded
				|| this.relatedAttributeChanged
			) {
				var regeneratedEmails = [];
				var objectsToRegenerate = []; // Array with all store New and Draft records and all emailTemplates from widget configuration
				var emailTemplatesRegenerated = [];
				var templatesToRegenerate = this.checkTemplatesToRegenerate();
				var draftEmails = this.controllerGrid.getDraftEmails();
_debug('draftEmails', draftEmails);
				for (var i in draftEmails) {
					var emailTemplateId = draftEmails[i].get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)[CMDBuild.core.proxy.CMProxyConstants.ID];

					if (!Ext.isEmpty(emailTemplateId))
						emailTemplatesRegenerated.push(emailTemplateId);

					objectsToRegenerate.push(draftEmails[i]);
				}

				for (var i in this.emailTemplates) {
					var emailTemplateId = this.emailTemplates[i][CMDBuild.core.proxy.CMProxyConstants.ID];

					if (!Ext.isEmpty(emailTemplateId) && !Ext.Array.contains(emailTemplatesRegenerated, emailTemplateId)) {
						Ext.create('CMDBuild.model.EmailTemplates', );

						objectsToRegenerate.push(this.emailTemplates[i]);
					}
				}
// TODO: rivedere questa parte inserendo l'IF nella funzione di rifenerazione
//				Ext.Array.each(objectsToRegenerate, function(item, index, allItems) {
//					if (item instanceof CMDBuild.model.widget.ManageEmail.email) {
//						if (Ext.Array.contains(templatesToRegenerate, item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)[CMDBuild.core.proxy.CMProxyConstants.ID]) || forceRegeneration)
//							regeneratedEmails.push(this.regenerateEmail(item, item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE))); // Regenerate a grid record
//					} else {
//						if (Ext.Array.contains(templatesToRegenerate, item[CMDBuild.core.proxy.CMProxyConstants.ID]) || forceRegeneration) {
//							item[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = true; // Setup temporary flag
//
//							regeneratedEmails.push(this.regenerateEmail(null, item)); // Regenerate a widget configuration template
//						}
//					}
//				}, this);

				this.relatedAttributeChanged = false;
				this.isFirstRegenerationDone = true;

				this.ownerController.view.mainView.form.initValues(); // Clear form fields dirty state to reset state after regeneration
_debug('regenerateAllEmails regeneratedEmails', regeneratedEmails);
				// Add all templates to store
				for (var i in regeneratedEmails)
					this.controllerGrid.addTemplate(regeneratedEmails[i]);
			}
		},

		/**
		 * @WIP TODO: toReport implementation (asking pop-up)
		 *
		 * @param {CMDBuild.model.widget.ManageEmail.email} emailObject
		 * @param {CMDBuild.model.EmailTemplates.singleTemplate} sourceTemplate
		 * @param {Boolean} toReport
		 *
		 * @return {Array} regeneratedEmailObject
		 */
		regenerateEmail: function(emailObject, sourceTemplate, toReport) {
			var regeneratedEmailObject = {};
_debug(!this.templateResolverIsBusy + ' ' + Ext.Object.isEmpty(sourceTemplate) + ' ' + emailObject.get('@@ autoSync'));
			if (
				!this.templateResolverIsBusy
				&& Ext.Object.isEmpty(sourceTemplate)
				&& emailObject.get('@@ autoSync')
			) {
				var me = this;

				this.templateResolverIsBusy = true;
_debug('this.grid.getStore()', this.grid.getStore());
_debug('emailObject', emailObject);
				this.grid.getStore().remove(emailObject); // Delete old email from store TODO do it with server call

				this.templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(sourceTemplate),
					callback: function(values, ctx) {
						var conditionExpr = values[CMDBuild.core.proxy.CMProxyConstants.CONDITION];

						if (!conditionExpr || me.templateResolver.safeJSEval(conditionExpr)) {
							_msg('Email with subject "' + values[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] + '" regenerated');

							values[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE] = sourceTemplate;

							regeneratedEmailObject.push(values);
						}

						me.templateResolver.bindLocalDepsChange(function() {
							if (!me.relatedAttributeChanged) {
								me.relatedAttributeChanged = true;

								CMDBuild.Msg.warn(null, "@@ Attribute related with email templates changed, some mail could be regenerated.");
							}
						});

						me.templateResolverIsBusy = false;
					}
				});
			}
_debug('regeneratedEmailObject', regeneratedEmailObject);
			return regeneratedEmailObject;
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
		}
	});

})();