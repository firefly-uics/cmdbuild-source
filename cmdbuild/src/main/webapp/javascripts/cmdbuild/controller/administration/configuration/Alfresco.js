(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Alfresco', {
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
			'onConfigurationAlfrescoSaveButtonClick',
			'onConfigurationAlfrescoAbortButtonClick'
		],

		/**
		 * Proxy parameters
		 *
		 * @cfg {Object}
		 */
		params: {
			fileName: 'dms',
			view: undefined
		},

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

			this.params[CMDBuild.core.constants.Proxy.VIEW] = this.view;

			this.cmfg('onConfigurationRead', this.params);
		},

		onConfigurationAlfrescoAbortButtonClick: function() {
			this.cmfg('onConfigurationRead', this.params);
		},

		onConfigurationAlfrescoSaveButtonClick: function() {
			this.cmfg('onConfigurationSave', this.params);
		}
	});

})();