(function() {

	Ext.define('CMDBuild.controller.administration.configuration.RelationGraph', {
		extend: 'CMDBuild.controller.common.AbstractController',

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onRelationGraphAbortButtonClick',
			'onRelationGraphSaveButtonClick'
		],

		/**
		 * @cfg {String}
		 */
		configFileName: 'graph',

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

			this.cmfg('onConfigurationRead', {
				configFileName: this.configFileName,
				view: this.view
			});
		},

		onRelationGraphAbortButtonClick: function() {
			this.cmfg('onConfigurationRead', {
				configFileName: this.configFileName,
				view: this.view
			});
		},

		onRelationGraphSaveButtonClick: function() {
			this.cmfg('onConfigurationSave', {
				configFileName: this.configFileName,
				view: this.view
			});
		}
	});

})();