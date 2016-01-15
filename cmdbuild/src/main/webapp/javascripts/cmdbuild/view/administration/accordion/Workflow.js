(function() {

	Ext.define('CMDBuild.view.administration.accordion.Workflow', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Workflow}
		 */
		delegate: undefined,

		disabled: !CMDBuild.configuration.workflow.get(CMDBuild.core.constants.Proxy.ENABLED),
		title: CMDBuild.Translation.processes
	});

})();