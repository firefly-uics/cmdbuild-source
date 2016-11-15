(function () {

	Ext.define('CMDBuild.view.management.accordion.Navigation', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.management.menu.Accordion'],

		/**
		 * @cfg {CMDBuild.controller.management.accordion.Navigation}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.management.menu.Accordion',

		title: CMDBuild.Translation.navigation
	});

})();
