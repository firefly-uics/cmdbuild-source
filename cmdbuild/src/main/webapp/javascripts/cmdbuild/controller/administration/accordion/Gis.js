(function() {

	Ext.define('CMDBuild.controller.administration.accordion.Gis', {
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
		 * @property {CMDBuild.view.administration.accordion.Gis}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.accordion.Gis', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		accordionUpdateStore: function(nodeIdToSelect) {
			this.view.getStore().getRootNode().removeAll();
			this.view.getStore().getRootNode().appendChild([
				{
					cmName: 'gis-icons',
					text: CMDBuild.Translation.manageIcons,
					description: CMDBuild.Translation.manageIcons,
					id: this.cmfg('accordionBuildId', { components: 'gis-icons' }),
					sectionHierarchy: ['gis-icons'],
					leaf: true
				},
				{
					cmName: 'gis-external-services',
					text: CMDBuild.Translation.externalServices,
					description: CMDBuild.Translation.externalServices,
					id: this.cmfg('accordionBuildId', { components: 'gis-external-services' }),
					sectionHierarchy: ['gis-external-services'],
					leaf: true
				},
				{
					cmName: 'gis-layers-order',
					text: CMDBuild.Translation.layersOrder,
					description: CMDBuild.Translation.layersOrder,
					id: this.cmfg('accordionBuildId', { components: 'gis-layers-order' }),
					sectionHierarchy: ['gis-layers-order'],
					leaf: true
				},
				{
					cmName: 'gis-geoserver',
					text: CMDBuild.Translation.geoserverLayers,
					description: CMDBuild.Translation.geoserverLayers,
					id: this.cmfg('accordionBuildId', { components: 'gis-geoserver' }),
					sectionHierarchy: ['gis-geoserver'],
					leaf: true
				},
				{
					cmName: 'gis-filter-configuration',
					text: CMDBuild.Translation.gisNavigation,
					description: CMDBuild.Translation.gisNavigation,
					id: this.cmfg('accordionBuildId', { components: 'gis-filter-configuration' }),
					sectionHierarchy: ['gis-filter-configuration'],
					leaf: true
				}
			]);

			// Alias of this.callParent(arguments), inside proxy function doesn't work
			this.updateStoreCommonEndpoint(nodeIdToSelect);

			this.callParent(arguments);
		}
	});

})();