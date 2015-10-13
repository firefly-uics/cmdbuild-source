(function() {

	Ext.define('CMDBuild.controller.administration.configuration.RelationGraph', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationRelationGraphAbortButtonClick',
			'onConfigurationRelationGraphSaveButtonClick'
		],

		/**
		 * Proxy parameters
		 *
		 * @cfg {Object}
		 */
		params: {
			fileName: 'graph',
			view: undefined
		},

		/**
		 * @property {CMDBuild.view.administration.configuration.RelationGraphPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.configuration.Configuration} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.configuration.RelationGraphPanel', {
				delegate: this
			});

			this.params[CMDBuild.core.constants.Proxy.VIEW] = this.view;

			this.cmfg('onConfigurationRead', this.params);
		},

		onConfigurationRelationGraphAbortButtonClick: function() {
			this.cmfg('onConfigurationRead', this.params);
		},

		onConfigurationRelationGraphSaveButtonClick: function() {
			this.cmfg('onConfigurationSave', this.params);
		}
	});

})();