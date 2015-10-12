(function() {

	Ext.define('CMDBuild.view.administration.accordion.DataView', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.views,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			this.getStore().getRootNode().removeAll();
			this.getStore().getRootNode().appendChild([
				{
					text: CMDBuild.Translation.filterView,
					description: CMDBuild.Translation.filterView,
					cmName: this.cmName,
					sectionHierarchy: ['filter'],
					leaf: true,
				},
				{
					text: CMDBuild.Translation.sqlView,
					description: CMDBuild.Translation.sqlView,
					cmName: this.cmName,
					sectionHierarchy: ['sql'],
					leaf: true,
				}
			]);

			this.callParent(arguments);
		}
	});

})();