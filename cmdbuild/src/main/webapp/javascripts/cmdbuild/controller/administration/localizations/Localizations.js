(function() {

	Ext.define('CMDBuild.controller.administration.localizations.Localizations', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

//		/**
//		 * @cfg {Array}
//		 */
//		cmfgCatchedFunctions: [],

		/**
		 * @property {Mixed}
		 */
		sectionController: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.LocalizationsView}
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

				switch(parameters.get(CMDBuild.core.proxy.Constants.ID)) {
					case 'advancedTranslationsTable': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.localizations.advancedTable.AdvancedTable', {
							parentDelegate: this
						});
					} break;

					case 'configurations':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.localizations.Configurations', {
							parentDelegate: this
						});
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(parameters.get(CMDBuild.core.proxy.Constants.TEXT));

				this.callParent(arguments);
			}
		}
	});

})();