(function() {

	Ext.define('CMDBuild.controller.administration.menu.Menu', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: ['selectedMenuNameGet'],

		/**
		 * @property {CMDBuild.controller.administration.menu.Groups}
		 */
		sectionController: undefined,

		/**
		 * @property {String}
		 */
		selectedMenuName: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.menu.MenuView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.menu.MenuView} view
		 */
		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange
			this.view = view;
			this.view.delegate = this;
		},

		/**
		 * @param {CMDBuild.model.common.AccordionStore} parameters
		 *
		 * @override
		 */
		onViewOnFront: function(parameters) {
			if (!Ext.isEmpty(parameters)) {
				this.selectedMenuName = parameters.get(CMDBuild.core.proxy.Constants.NAME);
				this.sectionController = Ext.create('CMDBuild.controller.administration.menu.Group', { parentDelegate: this });

				this.view.removeAll(true);
				this.view.add(this.sectionController.getView());

				this.setViewTitle(parameters.get(CMDBuild.core.proxy.Constants.TEXT));

				if (!Ext.isEmpty(this.sectionController) && Ext.isFunction(this.sectionController.onViewOnFront))
					this.sectionController.onViewOnFront();

				this.callParent(arguments);
			}
		},

		/**
		 * @returns {String}
		 */
		selectedMenuNameGet: function() {
			return this.selectedMenuName;
		}
	});

})();