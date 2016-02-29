(function() {

	Ext.define('CMDBuild.controller.administration.accordion.DataView', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.accordion.DataView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.accordion.DataView', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		accordionUpdateStore: function (nodeIdToSelect) {
			this.view.getStore().getRootNode().removeAll();
			this.view.getStore().getRootNode().appendChild([
				{
					cmName: this.cmfg('accordionIdentifierGet'),
					text: CMDBuild.Translation.filterView,
					description: CMDBuild.Translation.filterView,
					id: this.cmfg('accordionBuildId', { components: 'filter' }),
					sectionHierarchy: ['filter'],
					leaf: true
				},
				{
					cmName: this.cmfg('accordionIdentifierGet'),
					text: CMDBuild.Translation.sqlView,
					description: CMDBuild.Translation.sqlView,
					id: this.cmfg('accordionBuildId', { components: 'sql' }),
					sectionHierarchy: ['sql'],
					leaf: true
				}
			]);

			// Alias of this.callParent(arguments), inside proxy function doesn't work
			this.updateStoreCommonEndpoint(nodeIdToSelect);

			this.callParent(arguments);
		}
	});

})();