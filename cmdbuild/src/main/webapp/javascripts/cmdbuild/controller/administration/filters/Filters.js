(function() {

	Ext.define('CMDBuild.controller.administration.filters.Filters', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

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
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

		/**
		 * @cfg {CMDBuild.view.administration.filters.FiltersView}
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
					case 'groups':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.filters.Groups', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(parameters.get(CMDBuild.core.proxy.Constants.TEXT));

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