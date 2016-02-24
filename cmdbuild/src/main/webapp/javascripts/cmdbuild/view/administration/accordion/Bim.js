(function() {

	Ext.define('CMDBuild.view.administration.accordion.Bim', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Bim}
		 */
		delegate: undefined,

		disabled: !CMDBuild.configuration.bim.get(CMDBuild.core.constants.Proxy.ENABLED),
		title: CMDBuild.Translation.bim
	});

})();