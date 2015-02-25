(function() {

	Ext.define('CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel', {
		extend: 'Ext.tab.Panel',

		requires: [
//			'CMDBuild.core.proxy.CMProxyConstants' // TODO
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.AdvancedTranslationsTable}
		 */
		delegate: undefined,

		activeTab: 0,
		border: false,
		frame: true
	});

})();
