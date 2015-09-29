(function() {

	Ext.define('CMDBuild.core.constants.Server', {

		singleton: true,

		config: {
			maxInteger: 2147483647
		},

		constructor: function(config) {
			this.initConfig(config);
		}
	});

})();