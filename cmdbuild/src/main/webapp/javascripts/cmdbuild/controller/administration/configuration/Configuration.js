(function() {

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
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.configuration.ConfigurationView', { delegate: this });
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.accordion.Generic} node
		 *
		 * @override
		 */
		onConfigurationModuleInit: function(node) {
			if (!Ext.Object.isEmpty(node)) {
				this.view.removeAll(true);

				switch(node.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]) {
					case 'alfresco': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Dms', { parentDelegate: this });
					} break;

					case 'bim': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Bim', { parentDelegate: this });
					} break;

					case 'gis': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Gis', { parentDelegate: this });
					} break;

					case 'relationGraph': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.RelationGraph', { parentDelegate: this });
					} break;

					case 'server': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Server', { parentDelegate: this });
					} break;

					case 'workflow': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Workflow', { parentDelegate: this });
					} break;

					case 'generalOptions':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.GeneralOptions', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.TEXT));

				this.sectionController.getView().fireEvent('show'); // Manual show event fire

				this.onModuleInit(node); // Custom callParent() implementation
			}
		}
	});

})();