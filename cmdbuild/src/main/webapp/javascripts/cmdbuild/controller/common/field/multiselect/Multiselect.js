(function () {

	Ext.define('CMDBuild.controller.common.field.multiselect.Multiselect', {
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
			'fieldMultiselectGetStore',
			'fieldMultiselectReset',
			'fieldMultiselectSelectAll',
			'onFieldMultiselectAfterRender'
		],

		/**
		 * @property {CMDBuild.view.common.field.multiselect.Multiselect}
		 */
		view: undefined,

		/**
		 * Forwarder method
		 *
		 * @returns {Ext.data.Store}
		 */
		fieldMultiselectGetStore: function () {
			return this.view.boundList.getStore();
		},

		/**
		 * @returns {Void}
		 */
		fieldMultiselectReset: function () {
			this.view.setValue();
		},

		/**
		 * @returns {Void}
		 */
		fieldMultiselectSelectAll: function () {
			var arrayGroups = [];

			Ext.Array.forEach(this.view.getStore().getRange(), function (record, i, allRecords) {
				arrayGroups.push(record.get(this.view.valueField));
			}, this);

			this.view.setValue(arrayGroups);
		},

		/**
		 * @returns {Array or null}
		 */
		onFieldMultiselectAfterRender: function () {
			switch (this.view.defaultSelection) {
				case 'all': {
					return this.cmfg('fieldMultiselectSelectAll');
				} break;

				case 'none':
					return null;

				default:
					return this.view.value = this.view.defaultSelection;
			}
		}
	});

})();
