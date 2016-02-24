(function() {

	Ext.define('CMDBuild.core.constants.Global', {

		singleton: true,

		/**
		 * @cfg {Object}
		 *
		 * @private
		 */
		config: {
			errorMsgCss: 'cmdb-error-msg',
			mandatoryLabelFlag: '* ',
			tableTypeClass: 'class',
			tableTypeProcessClass: 'processclass',
			tableTypeSimpleTable: 'simpletable',
			titleSeparator: ' - '
		},

		/**
		 * @param {Object} config
		 */
		constructor: function(config) {
			this.initConfig(config);
		}
	});

})();
