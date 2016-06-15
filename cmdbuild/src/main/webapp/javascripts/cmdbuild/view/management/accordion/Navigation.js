(function () {

	Ext.define('CMDBuild.view.management.accordion.Navigation', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.menu.accordion.Management'],

		/**
		 * @cfg {CMDBuild.controller.management.accordion.Navigation}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.menu.accordion.Management',

		title: CMDBuild.Translation.navigation
	});

})();
