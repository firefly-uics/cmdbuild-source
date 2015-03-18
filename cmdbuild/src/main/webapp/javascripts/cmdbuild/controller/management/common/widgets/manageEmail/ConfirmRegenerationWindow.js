(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.ConfirmRegenerationWindow', {

		requires: [
			'CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.model.widget.ManageEmail'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Grid}
		 */
		gridDelegate: undefined,

		/**
		 * @property {Ext.data.Store}
		 */
		gridStore: undefined,

		/**
		 * @property {Array}
		 */
		recordsCouldBeRegenerated: undefined,

		/**
		 * @property {Array}
		 */
		templatesCouldBeRegenerated: undefined,

		/**
		 * @property {Mixed} emailWindows
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail} configObject.parentDelegate
		 * @param {CMDBuild.controller.management.common.widgets.manageEmail.Grid} configObject.gridDelegate
		 */
		constructor: function(configObject) {
_debug('configObject', configObject);
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.ConfirmRegenerationWindow', {
				delegate: this
			});

			this.gridStore = this.view.grid.getStore();
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
				case 'onConfirmRegenerationWindowClearStore':
					return this.onConfirmRegenerationWindowClearStore();

				case 'onConfirmRegenerationWindowConfirmButtonClick':
					return this.onConfirmRegenerationWindowConfirmButtonClick();

				case 'onConfirmRegenerationWindowShow':
					return this.onConfirmRegenerationWindowShow();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		addRecordToArray: function(record) {
_debug('addRecordToArray', record);
			this.recordsCouldBeRegenerated.push(record);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.template} template
		 */
		addTemplateToArray: function(template) {
_debug('addTemplateToArray', template);
			this.templatesCouldBeRegenerated.push(template);
		},

		beforeShow: function() {
_debug('beforeShow');
_debug('this.recordsCouldBeRegenerated', this.recordsCouldBeRegenerated);
_debug('this.templatesCouldBeRegenerated', this.templatesCouldBeRegenerated);
			this.gridStore.loadData(this.recordsCouldBeRegenerated);

			this.regenerateAndAddTemplateToStore(this.templatesCouldBeRegenerated);

			this.view.grid.getSelectionModel().deselectAll();

			this.show();
		},

		onConfirmRegenerationWindowClearStore: function() {
			this.gridStore.removeAll();
		},

		/**
		 * Regenerates only selected records
		 */
		onConfirmRegenerationWindowConfirmButtonClick: function() {
			this.parentDelegate.regenerateSelectedEmails(this.view.grid.getSelectionModel().getSelection());

			this.view.hide();
		},

		onConfirmRegenerationWindowShow: function() {
			this.reset();
		},

		/**
		 * Evaluates conditions and adds template to store
		 *
		 * @param {Array} templatesToAdd
		 *
		 * {conditionEvalTrafficLightArray} Implements a trafficLight functionality to manage multiple asynchronous calls and have a global callback
		 * to hide loadMask only at real end of calls.
		 */
		regenerateAndAddTemplateToStore: function(templatesToAdd) {
			var me = this;
			var conditionEvalTrafficLightArray = [];
_debug('addTemplatesToStoreAfterConditionEval templatesToAdd', templatesToAdd);
			if (Ext.isArray(templatesToAdd) && !Ext.isEmpty(templatesToAdd)) {
				CMDBuild.LoadMask.get().show();
				Ext.Array.forEach(templatesToAdd, function(template, i, allTemplates) {
_debug('template', template);
					if (!Ext.Object.isEmpty(template)) {
_debug('template', template);
						CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail.trafficLightSlotBuild(template, conditionEvalTrafficLightArray);

						var xaVars = Ext.apply({}, template.getData(), template.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES));
_debug('addTemplatesToStoreAfterConditionEval xaVars', xaVars);
_debug('addTemplatesToStoreAfterConditionEval template.getData()', template.getData());
						var templateResolver = new CMDBuild.Management.TemplateResolver({
							clientForm: me.parentDelegate.clientForm,
							xaVars: xaVars,
							serverVars: me.parentDelegate.getTemplateResolverServerVars()
						});

						templateResolver.resolveTemplates({
							attributes: Ext.Object.getKeys(xaVars),
							callback: function(values, ctx) {
								emailObject = Ext.create('CMDBuild.model.widget.ManageEmail.email', values);
								emailObject.set(CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID, me.parentDelegate.getActivityId());
								emailObject.set(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, template.get(CMDBuild.core.proxy.CMProxyConstants.KEY));
_debug('addTemplatesToStoreAfterConditionEval values', values);
								me.gridStore.add(emailObject);
_debug('##AA', conditionEvalTrafficLightArray);
								if (
									CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail.trafficLightArrayCheck(template, conditionEvalTrafficLightArray)
									|| Ext.isEmpty(conditionEvalTrafficLightArray)
								) {
									CMDBuild.LoadMask.get().hide();
									me.show();
								}
							}
						});
					}
				}, this);
			}
		},

		reset: function() {
			this.recordsCouldBeRegenerated = [];
			this.templatesCouldBeRegenerated = [];
		},

		show: function() {
_debug('config window show', this.view);
			if(
				!Ext.isEmpty(this.view)
				&& (
					this.gridStore.count() > 0
					|| !Ext.isEmpty(this.templatesCouldBeRegenerated)
				)
			) {
				this.view.show();
			}
		}
	});

})();