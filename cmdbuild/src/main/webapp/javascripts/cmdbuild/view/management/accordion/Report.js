(function() {

	Ext.define('CMDBuild.view.management.accordion.Report', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.common.accordion.Report'],

		/**
		 * @cfg {CMDBuild.controller.management.accordion.Report}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.common.accordion.Report',

		title: CMDBuild.Translation.report
	});

})();