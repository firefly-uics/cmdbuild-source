(function() {

	Ext.define('CMDBuild.view.management.widget.customForm.CustomFormView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.widget.customForm.CustomForm}
		 */
		delegate: undefined,

		border: false,
		frame: false,
		layout: 'fit'
	});

})();