(function() {

	Ext.define('CMDBuild.view.administration.localizations.panels.AdvancedTranslationsTableGrid', {
		extend: 'Ext.tree.Panel',

		requires: [
//			'CMDBuild.core.proxy.CMProxyConstants' // TODO
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.AdvancedTranslationsTable}
		 */
		delegate: undefined,

		header: false,
		autoScroll: true,
		border: false,
		frame: false,
		enableColumnHide: false,
		hideCollapseTool: true,
		collapsible: true,
		rootVisible: false,
		columnLines: true,

		// TODO
	});

})();
