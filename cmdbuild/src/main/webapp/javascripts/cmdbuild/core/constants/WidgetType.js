(function () {

	Ext.define('CMDBuild.core.constants.WidgetType', {

		singleton: true,

		config: {
			createModifyCard: '.CreateModifyCard',
			openNote: '.OpenNote'
		},

		/**
		 * @param {Object} config
		 *
		 * @returns {Void}
		 */
		constructor: function (config) {
			this.initConfig(config);
		}
	});

})();
