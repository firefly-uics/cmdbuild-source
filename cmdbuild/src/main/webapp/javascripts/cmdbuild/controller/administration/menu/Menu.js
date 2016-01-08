(function() {

	Ext.define('CMDBuild.controller.administration.menu.Menu', {
		extend: 'CMDBuild.controller.common.abstract.BasePanel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'selectedMenuNameGet'
		],

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
		 * @param {CMDBuild.model.menu.accordion.Administration} node
		 */
		onViewOnFront: function(node) {
			if (!Ext.isEmpty(node)) {
				this.selectedMenuName = node.get(CMDBuild.core.constants.Proxy.ENTITY_ID);
				this.sectionController = Ext.create('CMDBuild.controller.administration.menu.Group', { parentDelegate: this });

				this.view.removeAll(true);
				this.view.add(this.sectionController.getView());

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.TEXT));

				this.sectionController.cmfg('onMenuGroupMenuSelected');
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