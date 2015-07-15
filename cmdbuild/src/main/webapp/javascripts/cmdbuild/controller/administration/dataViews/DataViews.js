(function() {

	Ext.define('CMDBuild.controller.administration.dataViews.DataViews', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Mixed}
		 */
		sectionController: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.dataViews.DataViewsView}
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
			if (!Ext.Object.isEmpty(parameters)) {
				this.view.removeAll(true);

				switch(parameters.get(CMDBuild.core.proxy.Constants.ID)) {
					case 'sql': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.dataViews.Sql', { parentDelegate: this });
					} break;

					case 'filter':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.dataViews.Filter', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(parameters.get(CMDBuild.core.proxy.Constants.TEXT));

				this.callParent(arguments);
			}
		}
	});

})();