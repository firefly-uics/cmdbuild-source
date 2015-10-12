(function() {

	Ext.define('CMDBuild.view.administration.accordion.Report', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.report,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			this.getStore().getRootNode().removeAll();
			this.getStore().getRootNode().appendChild([
				{
					text: CMDBuild.Translation.reportMenuJasper,
					cmName: this.cmName,
					sectionHierarchy: ['jasper'],
					leaf: true,
				}
			]);

			this.callParent(arguments);
		}
	});

})();