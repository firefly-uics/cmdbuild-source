(function() {

	Ext.define('CMDBuild.core.configurations.Timeout', {

		singleton: true,

		config: {
			base:  90, // (seconds)
			cache: 300000, // 5m (milliseconds)
			report: 7200000 // 2h (milliseconds)
		},

		constructor: function(config) {
			this.initConfig(config);
		}
	});

})();