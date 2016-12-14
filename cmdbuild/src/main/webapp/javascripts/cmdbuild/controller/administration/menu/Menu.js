(function () {

	Ext.define('CMDBuild.controller.administration.menu.Menu', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onMenuModuleInit = onModuleInit',
			'selectedMenuNameGet'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

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
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.MainViewport} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.menu.MenuView', { delegate: this });
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {Object} parameters
		 * @param {CMDBuild.model.administration.menu.Accordion} parameters.node
		 *
		 * @override
		 */
		onMenuModuleInit: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			if (Ext.isObject(parameters.node) && !Ext.isEmpty(parameters.node)) {
				this.selectedMenuName = parameters.node.get(CMDBuild.core.constants.Proxy.ENTITY_ID);
				this.sectionController = Ext.create('CMDBuild.controller.administration.menu.Group', { parentDelegate: this });

				this.view.removeAll(true);
				this.view.add(this.sectionController.getView());

				this.setViewTitle(parameters.node.get(CMDBuild.core.constants.Proxy.TEXT));

				this.sectionController.cmfg('onMenuGroupMenuSelected');

				this.onModuleInit(parameters); // Custom callParent() implementation
			}
		},

		/**
		 * @returns {String}
		 */
		selectedMenuNameGet: function () {
			return this.selectedMenuName;
		}
	});

})();
