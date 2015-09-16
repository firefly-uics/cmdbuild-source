(function() {

	Ext.define('CMDBuild.controller.administration.dataView.DataView', {
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
		 * @cfg {CMDBuild.view.administration.dataView.DataViewView}
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

				switch(parameters.get(CMDBuild.core.constants.Proxy.ID)) {
					case 'sql': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.dataView.Sql', { parentDelegate: this });
					} break;

					case 'filter':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.dataView.Filter', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(parameters.get(CMDBuild.core.constants.Proxy.TEXT));

				this.callParent(arguments);
			}
		}
	});

})();