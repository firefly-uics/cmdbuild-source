(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Alfresco', {
		extend: 'CMDBuild.controller.common.AbstractController',

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onAlfrescoAbortButtonClick',
			'onAlfrescoSaveButtonClick'
		],

		/**
		 * @cfg {String}
		 */
		configFileName: 'dms',

		/**
		 * @property {CMDBuild.view.administration.configuration.AlfrescoPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.configuration.AlfrescoPanel', {
				delegate: this
			});

			this.cmfg('onConfigurationRead', {
				configFileName: this.configFileName,
				view: this.view
			});
		},

		onAlfrescoAbortButtonClick: function() {
			this.cmfg('onConfigurationRead', {
				configFileName: this.configFileName,
				view: this.view
			});
		},

		onAlfrescoSaveButtonClick: function() {
			this.cmfg('onConfigurationSave', {
				configFileName: this.configFileName,
				view: this.view
			});
		}
	});

})();