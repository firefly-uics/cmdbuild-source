(function() {

	Ext.define('CMDBuild.controller.administration.dataView.DataViews', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Mixed}
		 */
		sectionController: undefined,

		/**
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

		/**
		 * @cfg {CMDBuild.view.administration.dataView.DataViewsView}
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
_debug('onViewOnFront', parameters);
			if (!Ext.Object.isEmpty(parameters)) {
				this.view.removeAll(true);

				switch(parameters.get(CMDBuild.core.proxy.CMProxyConstants.ID)) {
					case 'sql': {
//						this.sectionController = Ext.create('CMDBuild.controller.administration.email.templates.Templates', { parentDelegate: this }); // TODO
					} break;

					case 'filter':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.dataView.Filter', { parentDelegate: this });
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