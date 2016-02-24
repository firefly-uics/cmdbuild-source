(function() {

	Ext.define('CMDBuild.core.constants.Server', {

		singleton: true,

		/**
		 * @cfg {Object}
		 *
		 * @private
		 */
		config: {
			maxInteger: 2147483647
		},

		/**
		 * @param {Object} config
		 */
		constructor: function(config) {
			this.initConfig(config);
		}
	});

})();