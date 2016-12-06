(function () {

	Ext.define('CMDBuild.view.common.field.translatable.window.FormPanel', {
		extend: 'Ext.form.Panel',

		mixins: ['CMDBuild.view.common.PanelFunctions2'],

		/**
		 * @cfg {CMDBuild.controller.common.field.translatable.Window}
		 */
		delegate: undefined,

		frame: true,
		border: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		}
	});

})();
