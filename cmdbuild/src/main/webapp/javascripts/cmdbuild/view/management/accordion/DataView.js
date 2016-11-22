(function () {

	Ext.define('CMDBuild.view.management.accordion.DataView', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.management.dataView.Accordion'],

		/**
		 * @cfg {CMDBuild.controller.management.accordion.DataView}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.management.dataView.Accordion',

		title: CMDBuild.Translation.views
	});

})();
