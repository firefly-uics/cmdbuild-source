(function() {

	/**
	 * Module's identifiers used to get module controllers and accordions
	 */
	Ext.define('CMDBuild.core.constants.ModuleIdentifiers', {

		singleton: true,

		/**
		 * @cfg {Object}
		 *
		 * @private
		 */
		config: {
			configuration: 'configuration',
			customPage: 'custompage',
			dataView: 'dataview',
			domain: 'domain',
			email: 'email',
			filter: 'filter',
			localization: 'localization',
			lookupType: 'lookuptype',
			menu: 'menu',
			report: 'report',
			userAndGroup: 'userandgroup',
			workflow: 'workflow'
		},

		/**
		 * @param {Object} config
		 */
		constructor: function(config) {
			this.initConfig(config);
		}
	});

})();