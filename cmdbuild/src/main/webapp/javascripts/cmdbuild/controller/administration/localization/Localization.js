(function() {

	Ext.define('CMDBuild.controller.administration.localization.Localization', {
		extend: 'CMDBuild.controller.common.abstract.BasePanel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Mixed}
		 */
		sectionController: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.LocalizationView}
		 */
		view: undefined,

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.accordion.Generic} node
		 *
		 * @public
		 * @override
		 */
		onViewOnFront: function(node) {
			if (!Ext.Object.isEmpty(node)) {
				this.view.removeAll(true);

				switch(node.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]) {
					case 'advancedTranslationsTable': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.localization.advancedTable.AdvancedTable', { parentDelegate: this });
					} break;

					case 'configuration':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.localization.Configuration', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.TEXT));

				this.callParent(arguments);
			}
		}
	});

})();