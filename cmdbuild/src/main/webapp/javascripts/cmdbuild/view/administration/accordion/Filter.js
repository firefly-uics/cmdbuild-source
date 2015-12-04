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
					cmName: this.cmName,
					text: CMDBuild.Translation.filtersForGroups,
					description: CMDBuild.Translation.filtersForGroups,
					id: this.delegate.cmfg('accordionBuildId', { components: 'groups' }),
					sectionHierarchy: ['groups'],
					leaf: true
				}
			]);

			this.callParent(arguments);
		}
	});

})();