(function() {

	Ext.define('CMDBuild.controller.administration.localization.Localization', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

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
		 * @cfg {CMDBuild.view.administration.localization.LocalizationView}
		 */
		view: undefined,

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} parameters
		 *
		 * @override
		 */
		onViewOnFront: function(parameters) {
_debug('CMDBuild', CMDBuild);
			if (!Ext.Object.isEmpty(parameters)) {
_debug('parameters', parameters);
				this.view.removeAll(true);

				switch(parameters.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]) {
					case 'advancedTranslationsTable': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.localization.advancedTable.AdvancedTable', { parentDelegate: this });
					} break;

					case 'configuration':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.localization.Configuration', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(parameters.get(CMDBuild.core.constants.Proxy.TEXT));

				this.callParent(arguments);
			}
		}
	});

})();