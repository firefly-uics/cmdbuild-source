(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.Main', {
		extend: 'CMDBuild.controller.management.common.widgets.CMWidgetController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		mixins: {
			observable: 'Ext.util.Observable'
		},

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
		 * @property {Object} variables
		 */
		emailTemplatesData: undefined,

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
			 * Generates temporary id from time in milliseconds
			 *
			 * @param {CMDBuild.model.widget.ManageEmail.email} targetObject
			 */
			generateTemporaryId: function(targetObject) {
				if (
					Ext.isEmpty(targetObject.get(CMDBuild.core.proxy.CMProxyConstants.ID))
					|| targetObject.get(CMDBuild.core.proxy.CMProxyConstants.ID) == 0
				) {
					targetObject.set(CMDBuild.core.proxy.CMProxyConstants.ID, new Date().valueOf());
					targetObject.set(CMDBuild.core.proxy.CMProxyConstants.IS_ID_TEMPORARY, true);
				}
			},

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
			// Generate templates id
			// TODO: quando implementerò le chiamate al server i template avranno già tutti un ID
			this.emailTemplates = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.EMAIL_TEMPLATES] || [];
			for (var index in this.emailTemplates) {
				var template = this.emailTemplates[index];

				if (Ext.isEmpty(template[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_ID]))
					template[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_ID] = new Date().valueOf() + index;
			}

			this.emailTemplatesData = this.extractVariablesForTemplateResolver();
			this.readOnly = !this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY];

			var xaVars = Ext.apply({}, this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.TEMPLATES] || {}, this.emailTemplatesData);

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
				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

//		/*
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
		 * If grid store is already loaded regenerate all email if needed, otherwise load grid and do it
		 *
		 * @override
		 */
		beforeActiveView: function() {
			if (this.controllerGrid.isStoreLoaded()) {
				this.checkToRegenerateAllEmails();
			} else {
				this.onEditMode();
			}
		},

		/**
		 * Builds templatesToRegenerate array in relation of dirty fields
		 *
		 * @return {Array} templatesToRegenerate
		 */
		checkTemplatesToRegenerate: function() {
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
		 * @WIP TODO
		 *
		 * Regenerates email resolving all internal CQL templates
		 *
		 * @param {Boolean} forceRegeneration
		 */
		checkToRegenerateAllEmails: function(forceRegeneration) {
			forceRegeneration = forceRegeneration || false;

			if (
				forceRegeneration
				|| !this.isFirstRegenerationDone // Regenerate first time widget is loaded
				|| this.relatedAttributeChanged
			) {
				var regeneratedEmails = [];
				var templatesToRegenerate = this.checkTemplatesToRegenerate();

				// Array with all store New and Draft records and all emailTemplates from widget configuration
				var objectsToRegenerate = [];
				var emailTemplatesRegenerated = [];
				var newEmails = this.controllerGrid.getNewEmails();
				var draftEmails = this.controllerGrid.getDraftEmails();
_debug('newEmails', newEmails);
_debug('draftEmails', draftEmails);
				for (var i in newEmails) {
					var emailTemplateId = newEmails[i].get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)[CMDBuild.core.proxy.CMProxyConstants.ID];

					if (!Ext.isEmpty(emailTemplateId))
						emailTemplatesRegenerated.push(emailTemplateId);

					objectsToRegenerate.push(newEmails[i]);
				}

				for (var i in draftEmails) {
					var emailTemplateId = draftEmails[i].get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)[CMDBuild.core.proxy.CMProxyConstants.ID];

					if (!Ext.isEmpty(emailTemplateId))
						emailTemplatesRegenerated.push(emailTemplateId);

					objectsToRegenerate.push(draftEmails[i]);
				}

				for (var i in this.emailTemplates) {
					var emailTemplateId = this.emailTemplates[i][CMDBuild.core.proxy.CMProxyConstants.ID];

					if (!Ext.isEmpty(emailTemplateId) && !Ext.Array.contains(emailTemplatesRegenerated, emailTemplateId))
						objectsToRegenerate.push(this.emailTemplates[i]);
				}

				Ext.Array.each(objectsToRegenerate, function(item, index, allItems) {
					if (item instanceof CMDBuild.model.widget.ManageEmail.email) {
						if (Ext.Array.contains(templatesToRegenerate, item.get(CMDBuild.core.proxy.CMProxyConstants.ID)[CMDBuild.core.proxy.CMProxyConstants.ID]) || forceRegeneration)
							regeneratedEmails.push(this.regenerateEmail(item, item.get('@@ emailObjectTemplate'))); // Regenerate a grid record
					} else {
						if (Ext.Array.contains(templatesToRegenerate, item[CMDBuild.core.proxy.CMProxyConstants.ID]) || forceRegeneration)
							regeneratedEmails.push(this.regenerateEmail(null, item)); // Regenerate a widget configuration template
					}
				}, this);

				this.relatedAttributeChanged = false;
				this.isFirstRegenerationDone = true;

				this.ownerController.view.mainView.form.initValues(); // Clear form fields dirty state to reset state after regeneration
_debug('checkToRegenerateAllEmails regeneratedEmails', regeneratedEmails);
				// Add all templates to store
				for (var i in regeneratedEmails)
					this.controllerGrid.addTemplateToStore(regeneratedEmails[i]);
			}
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
			this.checkToRegenerateAllEmails();

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
			if (!this.controllerGrid.isStoreLoaded() && !this.grid.getStore().isLoading()) {
				var pi = _CMWFState.getProcessInstance();

				this.view.setLoading(true);
				this.grid.getStore().load({
					params: {
						ProcessId: pi.getId()
					},
					scope: this,
					callback: function(records, operation, success) {
						this.view.setLoading(false);
						this.checkToRegenerateAllEmails();
					}
				});
			}
		},

		/**
		 * @WIP TODO: toReport implementation (asking pop-up)
		 *
		 * @param {CMDBuild.model.widget.ManageEmail.email} emailObject
		 * @param {CMDBuild.model.CMModelEmailTemplates.singleTemplate} sourceTemplate
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
				this.grid.getStore().remove(emailObject); // Delete old email from store

				this.templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(sourceTemplate),
					callback: function(values, ctx) {
						var conditionExpr = values[CMDBuild.core.proxy.CMProxyConstants.CONDITION];

						if (!conditionExpr || me.templateResolver.safeJSEval(conditionExpr)) {
							_msg('Email with subject "' + values[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] + '" regenerated');

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
		}
	});

})();