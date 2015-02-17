(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.ConfirmRegenerationWindow', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
		],

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
		recordsToRegenerate: undefined,

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
			Ext.apply(this, configObject); // Apply config

			var emailTemplatesToRegenerate = this.parentDelegate.checkTemplatesToRegenerate();

			this.recordsToRegenerate = [];

			// TODO no è sbagliato tenere presente che i record che cerco quì sono tutti i possibili rigenerabili e che dipendono dal parametro appena cambiato
			// mentre i record che devo dare in pasto alla funzione di rigenerazione del main controller sono solo quelli selezionati
			Ext.Array.forEach(this.gridDelegate.getDraftEmails(), function(item, index, allItems) {
				if (
					this.gridDelegate.isRegenerable(item)
					&& this.gridDelegate.recordIsEditable(item)
					&& Ext.Array.contains(emailTemplatesToRegenerate, item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE))
				) {
					this.recordsToRegenerate.push(item);
				}
			}, this);

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
				case 'onConfirmRegenerationWindowAbortButtonClick':
					return this.onConfirmRegenerationWindowAbortButtonClick();

				case 'onConfirmRegenerationWindowConfirmButtonClick':
					return this.onConfirmRegenerationWindowConfirmButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Destroy email window object
		 */
		onConfirmRegenerationWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		onConfirmRegenerationWindowConfirmButtonClick: function() {
			this.parentDelegate.regenerateSelectedEmails(this.recordsToRegenerate);

			this.onConfirmRegenerationWindowAbortButtonClick();
		}
	});

})();