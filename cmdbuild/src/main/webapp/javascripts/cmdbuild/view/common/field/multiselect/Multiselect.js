(function () {

	Ext.define('CMDBuild.view.common.field.multiselect.Multiselect', {
		extend: 'Ext.ux.form.MultiSelect',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @property {CMDBuild.controller.common.field.multiselect.Multiselect}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		defaultSelection: 'none',

		allowBlank: true,
		considerAsFieldToDisable: true, /** @deprecated */

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				delegate: Ext.create('CMDBuild.controller.common.field.multiselect.Multiselect', { view: this })
			});

			this.callParent(arguments);
		},

		listeners: {
			afterrender: function (field, eOpts) {
				this.delegate.cmfg('onFieldMultiselectAfterRender');
			}
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return this.delegate.cmfg('fieldMultiselectGetStore');
		},

		/**
		 * @returns {Void}
		 */
		reset: function () {
			this.delegate.cmfg('fieldMultiselectReset');
		},

		/**
		 * @returns {Void}
		 */
		selectAll: function () {
			this.delegate.cmfg('fieldMultiselectSelectAll');
		},

		/**
		 * Overrides to avoid multiselect native behaviour that sets the field as readonly if disabled
		 *
		 * @override
		 */
		updateReadOnly: Ext.emptyFn
	});

})();
