(function() {

	Ext.define('CMDBuild.view.management.customPage.SinglePagePanel', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.customPage.SinglePage}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.customPages,

		border: true,
		frame: false,
		layout: 'fit'
	});

})();