(function() {

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationBIM", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		title: "@@ BIM",
		configFileName: 'bim',

		constructor: function() {
			this.items = [{
				xtype: 'xcheckbox',
				name: 'enabled',
				fieldLabel: "@@ Enabled"
			}, {
				xtype : 'textfield',
				fieldLabel : "@@ URL",
				name : 'url'
			}, {
				xtype : 'textfield',
				fieldLabel : "@@ Username",
				name : 'username'
			}, {
				xtype : 'textfield',
				fieldLabel : "@@ Password",
				inputType : 'password',
				name : 'password'
			} ];

			this.callParent(arguments);
		}
	});

})();