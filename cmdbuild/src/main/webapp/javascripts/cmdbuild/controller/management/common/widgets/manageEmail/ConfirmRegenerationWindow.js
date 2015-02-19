(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.ConfirmRegenerationWindow', {

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Main}
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
		 * @param {CMDBuild.controller.management.common.widgets.manageEmail.Main} configObject.parentDelegate
		 * @param {CMDBuild.controller.management.common.widgets.manageEmail.Grid} configObject.gridDelegate
		 */
		constructor: function(configObject) {
_debug('configObject', configObject);
			Ext.apply(this, configObject); // Apply config

			var emailTemplatesToRegenerate = this.parentDelegate.checkTemplatesToRegenerate();

			this.recordsCouldBeRegenerated = [];

			// Get all records witch will be regenerated
			Ext.Array.forEach(this.gridDelegate.getDraftEmails(), function(item, index, allItems) {
				if (
					this.gridDelegate.isRegenerable(item)
					&& this.gridDelegate.recordIsEditable(item)
					&& Ext.Array.contains(emailTemplatesToRegenerate, item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE))
					&& this.parentDelegate.resolveTemplateCondition(item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE))
				) {
					this.recordsCouldBeRegenerated.push(item);
				}
			}, this);
_debug('this.recordsCouldBeRegenerated', this.recordsCouldBeRegenerated);
			this.view = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.ConfirmRegenerationWindow', {
				delegate: this
			});

			this.view.show();
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

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Regenerates only selected records
		 */
		onConfirmRegenerationWindowConfirmButtonClick: function() {
			this.parentDelegate.regenerateSelectedEmails(this.view.grid.getSelectionModel().getSelection());

			this.view.destroy();
		}
	});

})();