(function() {

	Ext.define('CMDBuild.view.administration.accordion.Bim', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.common.abstract.Accordion}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		disabled: !CMDBuild.configuration.bim.get(CMDBuild.core.constants.Proxy.ENABLED),
		title: CMDBuild.Translation.bim,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			this.getStore().getRootNode().removeAll();
			this.getStore().getRootNode().appendChild([
				{
					cmName: 'bim-project',
					text: CMDBuild.Translation.projects,
					description: CMDBuild.Translation.projects,
					id: this.delegate.cmfg('accordionBuildId', { components: 'bim-project' }),
					sectionHierarchy: ['bim-project'],
					leaf: true
				},
				{
					cmName: 'bim-layers',
					text: CMDBuild.Translation.layers,
					description: CMDBuild.Translation.layers,
					id: this.delegate.cmfg('accordionBuildId', { components: 'bim-layers' }),
					sectionHierarchy: ['bim-layers'],
					leaf: true
				}
			]);

			this.callParent(arguments);
		}
	});

})();