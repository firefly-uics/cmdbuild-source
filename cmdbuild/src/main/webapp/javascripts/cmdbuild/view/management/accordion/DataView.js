(function() {

	Ext.define('CMDBuild.view.management.accordion.DataView', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.common.accordion.DataView'],

		/**
		 * @cfg {CMDBuild.controller.management.accordion.DataView}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.common.accordion.DataView',

		title: CMDBuild.Translation.views
	});

})();