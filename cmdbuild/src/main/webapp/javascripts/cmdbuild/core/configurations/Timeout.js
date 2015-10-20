(function() {

	Ext.define('CMDBuild.core.configurations.Timeout', {

		singleton: true,

		config: {
			base: 90, // (seconds)
			patchManager: 600000, // 10m (milliseconds)
			report: 7200000 // 2h (milliseconds)
		},

		constructor: function(config) {
			this.initConfig(config);
		}
	});

})();