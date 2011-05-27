(function() {
	CMDBuild.UnconfiguredModPanel = Ext.extend(CMDBuild.ModPanel, {
		modtype: "notconfiguredpanel",
		bodyCssClass: "cmdbuild_unconfigured_modpanel",
		afterBringToFront: function(params) {
			_debug(params.msg);
			this.update(params.msg);
			return true;
		},
		initComponent : function() {
			CMDBuild.UnconfiguredModPanel.superclass.initComponent.call(this, arguments);		
		}
	});
})();