(function() {

	Ext.define('CMDBuild.view.administration.localizations.AdvancedTranslationsGridPanel', {
		extend: 'Ext.tab.Panel',

		requires: [
//			'CMDBuild.core.proxy.CMProxyConstants' // TODO
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.AdvancedTranslationsGrid}
		 */
		delegate: undefined,

		border: false,
		frame: true
	});

})();
