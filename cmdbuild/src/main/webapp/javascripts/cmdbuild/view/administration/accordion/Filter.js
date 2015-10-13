(function() {

	Ext.define('CMDBuild.view.administration.accordion.Filter', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.searchFilters,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			this.getStore().getRootNode().removeAll();
			this.getStore().getRootNode().appendChild([
				{
					text: CMDBuild.Translation.filtersForGroups,
					cmName: this.cmName,
					sectionHierarchy: ['groups'],
					leaf: true
				}
			]);

			this.callParent(arguments);
		}
	});

})();