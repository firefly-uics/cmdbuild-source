(function() {

	Ext.define('CMDBuild.view.common.field.translatable.window.FormPanel', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.field.translatable.Window}
		 */
		delegate: undefined,

		/**
		 * @property {Object}
		 */
		oldValues: {},

		frame: true,
		border: false,

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		defaults: {
			maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH
		},

		/**
		 * @return {Object}
		 */
		getOldValues: function() {
			return this.oldValues;
		}
	});

})();