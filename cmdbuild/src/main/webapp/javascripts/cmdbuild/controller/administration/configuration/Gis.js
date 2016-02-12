(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Gis', {
		extend: 'CMDBuild.controller.common.AbstractController',

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGisAbortButtonClick',
			'onGisSaveButtonClick'
		],

		/**
		 * @cfg {String}
		 */
		configFileName: 'gis',

		/**
		 * @property {CMDBuild.view.administration.configuration.GisPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.configuration.GisPanel', {
				delegate: this
			});

			this.cmfg('onConfigurationRead', {
				configFileName: this.configFileName,
				view: this.view
			});
		},

		onGisAbortButtonClick: function() {
			this.cmfg('onConfigurationRead', {
				configFileName: this.configFileName,
				view: this.view
			});
		},

		onGisSaveButtonClick: function() {
			this.cmfg('onConfigurationSave', {
				configFileName: this.configFileName,
				view: this.view
			});
		}
	});

})();