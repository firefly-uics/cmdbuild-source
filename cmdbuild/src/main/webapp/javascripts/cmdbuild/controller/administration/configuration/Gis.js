(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Gis', {
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
			'onConfigurationGisAbortButtonClick',
			'onConfigurationGisSaveButtonClick'
		],

		/**
		 * Proxy parameters
		 *
		 * @cfg {Object}
		 */
		params: {
			fileName: 'gis',
			view: undefined
		},

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

			this.params[CMDBuild.core.constants.Proxy.VIEW] = this.view;

			this.cmfg('onConfigurationRead', this.params);
		},

		onConfigurationGisAbortButtonClick: function() {
			this.cmfg('onConfigurationRead', this.params);
		},

		onConfigurationGisSaveButtonClick: function() {
			this.cmfg('onConfigurationSave', this.params);
		}
	});

})();