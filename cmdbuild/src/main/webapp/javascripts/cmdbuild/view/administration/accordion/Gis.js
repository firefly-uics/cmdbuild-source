(function() {

	Ext.define('CMDBuild.view.administration.accordion.Gis', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		disabled: (
			!CMDBuild.configuration.gis.get(CMDBuild.core.constants.Proxy.ENABLED)
			|| CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN)
		),
		title: CMDBuild.Translation.gis,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			this.getStore().getRootNode().removeAll();
			this.getStore().getRootNode().appendChild([
				{
					cmName: 'gis-icons',
					text: CMDBuild.Translation.manageIcons,
					description: CMDBuild.Translation.manageIcons,
					id: this.delegate.cmfg('accordionBuildId', { components: 'gis-icons' }),
					sectionHierarchy: ['gis-icons'],
					leaf: true
				},
				{
					cmName: 'gis-external-services',
					text: CMDBuild.Translation.externalServices,
					description: CMDBuild.Translation.externalServices,
					id: this.delegate.cmfg('accordionBuildId', { components: 'gis-external-services' }),
					sectionHierarchy: ['gis-external-services'],
					leaf: true
				},
				{
					cmName: 'gis-layers-order',
					text: CMDBuild.Translation.layersOrder,
					description: CMDBuild.Translation.layersOrder,
					id: this.delegate.cmfg('accordionBuildId', { components: 'gis-layers-order' }),
					sectionHierarchy: ['gis-layers-order'],
					leaf: true
				},
				{
					cmName: 'gis-geoserver',
					text: CMDBuild.Translation.geoserverLayers,
					description: CMDBuild.Translation.geoserverLayers,
					id: this.delegate.cmfg('accordionBuildId', { components: 'gis-geoserver' }),
					sectionHierarchy: ['gis-geoserver'],
					leaf: true
				},
				{
					cmName: 'gis-filter-configuration',
					text: CMDBuild.Translation.gisNavigation,
					description: CMDBuild.Translation.gisNavigation,
					id: this.delegate.cmfg('accordionBuildId', { components: 'gis-filter-configuration' }),
					sectionHierarchy: ['gis-filter-configuration'],
					leaf: true
				}
			]);

			this.callParent(arguments);
		}
	});

})();