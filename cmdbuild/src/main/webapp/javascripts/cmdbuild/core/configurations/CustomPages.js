(function() {

	Ext.define('CMDBuild.core.configurations.CustomPages', {

		singleton: true,

		config: {
			version: '1.0' // GuiFramework version (used to build folder name)
		},

		constructor: function(config) {
			this.initConfig(config);
		}
	});

})();