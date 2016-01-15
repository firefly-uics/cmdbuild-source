(function() {

	Ext.define('CMDBuild.controller.administration.accordion.Email', {
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
		 * @property {CMDBuild.view.administration.accordion.Email}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.accordion.Email', { delegate: this });

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
					cmName: this.cmfg('accordionIdentifierGet'),
					text: CMDBuild.Translation.accounts,
					description: CMDBuild.Translation.accounts,
					id: this.cmfg('accordionBuildId', { components: 'accounts' }),
					sectionHierarchy: ['accounts'],
					leaf: true
				},
				{
					cmName: this.cmfg('accordionIdentifierGet'),
					text: CMDBuild.Translation.templates,
					description: CMDBuild.Translation.templates,
					id: this.cmfg('accordionBuildId', { components: 'templates' }),
					sectionHierarchy: ['templates'],
					leaf: true
				},
				{
					cmName: this.cmfg('accordionIdentifierGet'),
					text: CMDBuild.Translation.queue,
					description: CMDBuild.Translation.queue,
					id: this.cmfg('accordionBuildId', { components: 'queue' }),
					sectionHierarchy: ['queue'],
					leaf: true
				}
			]);

			// Alias of this.callParent(arguments), inside proxy function doesn't work
			this.updateStoreCommonEndpoint(nodeIdToSelect);

			this.callParent(arguments);
		}
	});

})();