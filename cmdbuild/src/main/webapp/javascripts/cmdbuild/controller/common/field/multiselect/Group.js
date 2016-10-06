(function () {

	Ext.define('CMDBuild.controller.common.field.multiselect.Group', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFieldMultiselectGroupReset',
			'onFieldMultiselectGroupStoreGet',
			'onFieldMultiselectGroupSelectAll',
			'onFieldMultiselectGroupValueGet'
		],

		/**
		 * @property {CMDBuild.view.common.field.multiselect.Group}
		 */
		view: undefined,

		/**
		 * @returns {Void}
		 */
		onFieldMultiselectGroupReset: function () {
			this.view.setValue();
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Ext.data.Store}
		 */
		onFieldMultiselectGroupStoreGet: function () {
			return this.view.boundList.getStore();
		},

		/**
		 * @returns {Void}
		 */
		onFieldMultiselectGroupSelectAll: function () {
			var arrayGroups = [];

			Ext.Array.each(this.view.getStore().getRange(), function (record, i, allRecords) {
				if (Ext.isObject(record) && !Ext.Object.isEmpty(record) && Ext.isFunction(record.get))
					arrayGroups.push(record.get(CMDBuild.core.constants.Proxy.NAME));
			}, this);

			this.view.setValue(arrayGroups);
		},

		/**
		 * @param {String} value
		 *
		 * @returns {Array}
		 */
		onFieldMultiselectGroupValueGet: function (value) {
			return Ext.isString(value[0]) ? value : [];
		}
	});

})();
