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
					text: CMDBuild.Translation.manageIcons,
					description: CMDBuild.Translation.manageIcons,
					cmName: 'gis-icons',
					leaf: true
				},
				{
					text: CMDBuild.Translation.externalServices,
					description: CMDBuild.Translation.externalServices,
					cmName: 'gis-external-services',
					leaf: true
				},
				{
					text: CMDBuild.Translation.layersOrder,
					description: CMDBuild.Translation.layersOrder,
					cmName: 'gis-layers-order',
					leaf: true
				},
				{
					text: CMDBuild.Translation.geoserverLayers,
					description: CMDBuild.Translation.geoserverLayers,
					cmName: 'gis-geoserver',
					leaf: true
				},
				{
					text: CMDBuild.Translation.gisNavigation,
					description: CMDBuild.Translation.gisNavigation,
					cmName: 'gis-filter-configuration',
					leaf: true
				}
			]);

			this.callParent(arguments);
		}
	});

})();