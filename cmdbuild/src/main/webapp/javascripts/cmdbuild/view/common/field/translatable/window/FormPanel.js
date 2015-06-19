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
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		defaults: {
			labelWidth: CMDBuild.LABEL_WIDTH,
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