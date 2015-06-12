(function() {

	Ext.define('CMDBuild.controller.administration.localizations.Localizations', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

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
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

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
_debug('CMDBuild.Config', CMDBuild.Config);
			if (!Ext.Object.isEmpty(parameters)) {
_debug('parameters', parameters);
				this.view.removeAll(true);

				switch(parameters.get(CMDBuild.core.proxy.CMProxyConstants.ID)) {
					case 'advancedTranslations': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.localizations.Advanced', {
							parentDelegate: this
						});
					} break;

					case 'advancedTranslationsTable': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.localizations.advancedTable.AdvancedTable', {
							parentDelegate: this
						});
					} break;

					case 'baseTranslations':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.localizations.Base', {
							parentDelegate: this
						});
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(parameters.get(CMDBuild.core.proxy.CMProxyConstants.TEXT));

				this.callParent(arguments);
			}
		},

		/**
		 * Setup view panel title as a breadcrumbs component
		 *
		 * @param {String} titlePart
		 */
		setViewTitle: function(titlePart) {
			if (Ext.isEmpty(titlePart)) {
				this.view.setTitle(this.view.baseTitle);
			} else {
				this.view.setTitle(this.view.baseTitle + this.titleSeparator + titlePart);
			}
		}
	});

})();