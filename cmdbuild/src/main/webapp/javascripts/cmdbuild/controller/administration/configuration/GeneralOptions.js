(function() {

	Ext.define('CMDBuild.controller.administration.configuration.GeneralOptions', {
		extend: 'CMDBuild.controller.common.AbstractController',

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
		 * @cfg {String}
		 */
		configFileName: 'cmdbuild',

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

			this.cmfg('onConfigurationRead', {
				configFileName: this.configFileName,
				view: this.view
			});
		},

		onGeneralOptionsAbortButtonClick: function() {
			this.cmfg('onConfigurationRead', {
				configFileName: this.configFileName,
				view: this.view
			});
		},

		onGeneralOptionsSaveButtonClick: function() {
			this.cmfg('onConfigurationSave', {
				configFileName: this.configFileName,
				view: this.view
			});
		},
	});

})();