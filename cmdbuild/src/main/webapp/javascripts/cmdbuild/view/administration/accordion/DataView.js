(function() {

	Ext.define('CMDBuild.view.administration.accordion.DataView', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.common.abstract.Accordion}
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
					cmName: this.cmName,
					text: CMDBuild.Translation.filterView,
					description: CMDBuild.Translation.filterView,
					id: this.delegate.cmfg('accordionBuildId', { components: 'filter' }),
					sectionHierarchy: ['filter'],
					leaf: true
				},
				{
					cmName: this.cmName,
					text: CMDBuild.Translation.sqlView,
					description: CMDBuild.Translation.sqlView,
					id: this.delegate.cmfg('accordionBuildId', { components: 'sql' }),
					sectionHierarchy: ['sql'],
					leaf: true
				}
			]);

			this.callParent(arguments);
		}
	});

})();