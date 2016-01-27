(function() {

	Ext.define('CMDBuild.core.configurations.Timeout', {

		singleton: true,

		config: {
			base: 90, // (seconds)
			cache: 300000, // 5m (milliseconds)
			configurationSetup: 12000000, // 200m (milliseconds)
			patchManager: 600000, // 10m (milliseconds)
			report: 7200000 // 2h (milliseconds)
		},

		/**
		 * @param {Object} config
		 */
		constructor: function(config) {
			this.initConfig(config);
		}
	});

})();