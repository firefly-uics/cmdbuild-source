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
					text: CMDBuild.Translation.accounts,
					description: CMDBuild.Translation.accounts,
					cmName: this.cmName,
					sectionHierarchy: ['accounts'],
					leaf: true,
				},
				{
					text: CMDBuild.Translation.templates,
					description: CMDBuild.Translation.templates,
					cmName: this.cmName,
					sectionHierarchy: ['templates'],
					leaf: true,
				},
				{
					text: CMDBuild.Translation.queue,
					description: CMDBuild.Translation.queue,
					cmName: this.cmName,
					sectionHierarchy: ['queue'],
					leaf: true,
				}
			]);

			this.callParent(arguments);
		}
	});

})();