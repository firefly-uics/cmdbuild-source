(function() {

	Ext.define('CMDBuild.view.administration.accordion.Email', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.email,

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
					text: CMDBuild.Translation.accounts,
					description: CMDBuild.Translation.accounts,
					id: this.delegate.cmfg('accordionBuildId', { components: 'accounts' }),
					sectionHierarchy: ['accounts'],
					leaf: true
				},
				{
					cmName: this.cmName,
					text: CMDBuild.Translation.templates,
					description: CMDBuild.Translation.templates,
					id: this.delegate.cmfg('accordionBuildId', { components: 'templates' }),
					sectionHierarchy: ['templates'],
					leaf: true
				},
				{
					cmName: this.cmName,
					text: CMDBuild.Translation.queue,
					description: CMDBuild.Translation.queue,
					id: this.delegate.cmfg('accordionBuildId', { components: 'queue' }),
					sectionHierarchy: ['queue'],
					leaf: true
				}
			]);

			this.callParent(arguments);
		}
	});

})();