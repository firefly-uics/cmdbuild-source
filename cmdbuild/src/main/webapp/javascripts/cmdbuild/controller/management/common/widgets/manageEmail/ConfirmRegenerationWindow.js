(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.ConfirmRegenerationWindow', {

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Grid}
		 */
		gridDelegate: undefined,

		/**
		 * @property {Array}
		 */
		recordsCouldBeRegenerated: undefined,

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
				case 'onConfirmRegenerationWindowConfirmButtonClick':
					return this.onConfirmRegenerationWindowConfirmButtonClick();

				case 'onConfirmRegenerationWindowBeforeShow':
					return this.onConfirmRegenerationWindowBeforeShow();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return {CMDBuild.view.management.common.widgets.manageEmail.ConfirmRegenerationWindow}
		 */
		getView: function() {
			return this.view;
		},

		/**
		 * Regenerates only selected records
		 */
		onConfirmRegenerationWindowConfirmButtonClick: function() {
			this.parentDelegate.regenerateSelectedEmails(this.view.grid.getSelectionModel().getSelection());

			this.view.hide();
		},

		onConfirmRegenerationWindowBeforeShow: function() {
_debug('onConfirmRegenerationWindowBeforeShow');
			var emailTemplatesToRegenerate = this.parentDelegate.checkTemplatesToRegenerate();

			this.recordsCouldBeRegenerated = [];
_debug('emailTemplatesToRegenerate', emailTemplatesToRegenerate);
			// Get all records witch will be regenerated
			Ext.Array.forEach(this.gridDelegate.getDraftEmails(), function(item, index, allItems) {
_debug('item', item);
				if (
					this.gridDelegate.isRegenerable(item)
					&& this.gridDelegate.recordIsEditable(item)
					&& item.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION)
					&& item.get(CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION)
					&& Ext.Array.contains(emailTemplatesToRegenerate, item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE))
//					&& this.parentDelegate.resolveTemplateCondition(item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE))
				) {
					this.recordsCouldBeRegenerated.push(item);
				}
_debug('this.recordsCouldBeRegenerated',
this.gridDelegate.isRegenerable(item)
+ ' ' + this.gridDelegate.recordIsEditable(item)
+ ' ' + item.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION)
+ ' ' + Ext.Array.contains(emailTemplatesToRegenerate, item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE))
+ ' ' + this.parentDelegate.resolveTemplateCondition(item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)));
			}, this);
_debug('this.recordsCouldBeRegenerated', this.recordsCouldBeRegenerated);

			this.view.grid.getStore().loadData(this.recordsCouldBeRegenerated);
			this.view.grid.getSelectionModel().deselectAll();

			if (Ext.isEmpty(this.recordsCouldBeRegenerated))
				return false;
		}
	});

})();