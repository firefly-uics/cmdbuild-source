(function() {

	Ext.define('CMDBuild.core.configurations.Timeout', {

		singleton: true,

		config: {
			base:  90, // (seconds)
			report: 7200000 // 2h (milliseconds)
		},

		constructor: function(config) {
			this.initConfig(config);
		}
	});

})();