(function() {

	Ext.define('CMDBuild.core.constants.Global', {

		singleton: true,

		config: {
			errorMsgCss: 'cmdb-error-msg',
			mandatoryLabelFlag: '* ',
			tableTypeClass: 'class',
			tableTypeProcessClass: 'processclass',
			tableTypeSimpleTable: 'simpletable',
			titleSeparator: ' - '
		},

		constructor: function(config) {
			this.initConfig(config);
		}
	});

})();