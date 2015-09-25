(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.CustomFormView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.customForm.CustomForm}
		 */
		delegate: undefined,

		border: false,
		frame: false,
		layout: 'fit'
	});

})();