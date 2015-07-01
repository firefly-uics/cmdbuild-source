(function() {

	Ext.define('CMDBuild.controller.administration.configuration.GeneralOptions', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGeneralOptionsAbortButtonClick',
			'onGeneralOptionsSaveButtonClick'
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

			this.params[CMDBuild.core.proxy.Constants.VIEW] = this.view;

			this.cmfg('onConfigurationRead', this.params);
		},

		onGeneralOptionsAbortButtonClick: function() {
			this.cmfg('onConfigurationRead', this.params);
		},

		onGeneralOptionsSaveButtonClick: function() {
			this.cmfg('onConfigurationSave', this.params);
		}
	});

})();