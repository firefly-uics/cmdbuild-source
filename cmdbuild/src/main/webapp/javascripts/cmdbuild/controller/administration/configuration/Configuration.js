(function () {

	Ext.define('CMDBuild.controller.administration.configuration.Configuration', {
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
			'onConfigurationModuleInit = onModuleInit'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.configuration.ConfigurationView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.configuration.ConfigurationView', { delegate: this });
		},

		/**
		 * @param {String} identifier
		 *
		 * @returns {Mixed}
		 *
		 * @private
		 */
		buildSectionController: function (identifier) {
			switch(identifier) {
				case 'bim':
					return Ext.create('CMDBuild.controller.administration.configuration.Bim', { parentDelegate: this });

				case 'dms':
					return Ext.create('CMDBuild.controller.administration.configuration.Dms', { parentDelegate: this });

				case 'gis':
					return Ext.create('CMDBuild.controller.administration.configuration.Gis', { parentDelegate: this });

				case 'notifications':
					return Ext.create('CMDBuild.controller.administration.configuration.notifications.Notifications', { parentDelegate: this });

				case 'relationGraph':
					return Ext.create('CMDBuild.controller.administration.configuration.RelationGraph', { parentDelegate: this });

				case 'server':
					return Ext.create('CMDBuild.controller.administration.configuration.Server', { parentDelegate: this });

				case 'workflow':
					return Ext.create('CMDBuild.controller.administration.configuration.Workflow', { parentDelegate: this });

				case 'generalOptions':
				default:
					return Ext.create('CMDBuild.controller.administration.configuration.GeneralOptions', { parentDelegate: this });
			}
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {Object} parameters
		 * @param {CMDBuild.model.common.Accordion} parameters.node
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onConfigurationModuleInit: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			if (Ext.isObject(parameters.node) && !Ext.Object.isEmpty(parameters.node)) {
				this.view.removeAll(true);

				this.sectionController = this.buildSectionController(parameters.node.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]);

				this.view.add(this.sectionController.getView());

				this.setViewTitle(parameters.node.get(CMDBuild.core.constants.Proxy.TEXT));

				this.sectionController.getView().fireEvent('show'); // Manual show event fire

				this.onModuleInit(parameters); // Custom callParent() implementation
			}
		}
	});

})();
