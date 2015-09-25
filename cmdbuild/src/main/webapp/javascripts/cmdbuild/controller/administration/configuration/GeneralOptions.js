(function() {

	Ext.define('CMDBuild.controller.administration.configuration.GeneralOptions', {
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
			'onConfigurationGeneralOptionsAbortButtonClick',
			'onConfigurationGeneralOptionsSaveButtonClick'
		],

		/**
		 * Proxy parameters
		 *
		 * @cfg {Object}
		 */
		params: {
			fileName: 'cmdbuild',
			view: undefined
		},

		/**
		 * @property {CMDBuild.view.administration.configuration.GeneralOptionsPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.configuration.GeneralOptionsPanel', {
				delegate: this
			});

			this.params[CMDBuild.core.constants.Proxy.VIEW] = this.view;

			this.cmfg('onConfigurationRead', this.params);
		},

		onConfigurationGeneralOptionsAbortButtonClick: function() {
			this.cmfg('onConfigurationRead', this.params);
		},

		onConfigurationGeneralOptionsSaveButtonClick: function() {
			this.cmfg('onConfigurationSave', this.params);
		}
	});

})();