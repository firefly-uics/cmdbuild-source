(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Workflow', {
		extend: 'CMDBuild.controller.common.AbstractController',

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWorkflowAbortButtonClick',
			'onWorkflowSaveButtonClick'
		],

		/**
		 * @cfg {String}
		 */
		configFileName: 'workflow',

		/**
		 * @property {CMDBuild.view.administration.configuration.WorkflowPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.configuration.WorkflowPanel', {
				delegate: this
			});

			this.cmfg('onConfigurationRead', {
				configFileName: this.configFileName,
				view: this.view
			});
		},

		onWorkflowAbortButtonClick: function() {
			this.cmfg('onConfigurationRead', {
				configFileName: this.configFileName,
				view: this.view
			});
		},

		onWorkflowSaveButtonClick: function() {
			this.cmfg('onConfigurationSave', {
				configFileName: this.configFileName,
				view: this.view
			});
		}
	});

})();