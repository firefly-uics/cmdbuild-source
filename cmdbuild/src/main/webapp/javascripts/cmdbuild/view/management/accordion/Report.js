(function () {

	Ext.define('CMDBuild.view.management.accordion.Report', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.management.report.Accordion'],

		/**
		 * @cfg {CMDBuild.controller.management.accordion.Report}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.management.report.Accordion',

		title: CMDBuild.Translation.report
	});

})();
