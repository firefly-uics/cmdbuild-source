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
		baseTitle: '@@ Custom pages',

		border: true,
		frame: false,
		layout: 'fit'
	});

})();