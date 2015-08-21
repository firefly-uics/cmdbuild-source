(function() {

	Ext.define('CMDBuild.view.administration.accordion.Report', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.report,

		constructor: function() {
			this.callParent(arguments);

			this.updateStore();
		},

		/**
		 * @override
		 */
		updateStore: function() {
			this.getStore().getRootNode().removeAll();
			this.getStore().getRootNode().appendChild([
				{
					id: 'Jasper',
					cmName: this.cmName,
					leaf: true,
					text: CMDBuild.Translation.reportMenuJasper
				}
			]);
		}
	});

})();