(function () {

	Ext.define('CMDBuild.view.administration.accordion.Menu', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.administration.menu.Accordion'],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Menu}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.administration.menu.Accordion',

		title: CMDBuild.Translation.menu
	});

})();
