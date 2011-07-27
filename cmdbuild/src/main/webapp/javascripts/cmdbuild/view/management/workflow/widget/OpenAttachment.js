CMDBuild.Management.OpenAttachment = Ext.extend(CMDBuild.Management.BaseExtendedAttribute, {
    initialize: function(extAttrDef) {},
    
    tabpanel_id: 'activityattachments_tab',
    
    onExtAttrShow: function(extAttr) {
    	var acttabpnl = this.findParentByType('activitytabpanel');
        var actattachtab = acttabpnl.getComponent('activityattachments_tab');
        acttabpnl.setActiveTab('activityattachments_tab');
        actattachtab.backToActivityButton.show();
        actattachtab.backToActivityButton.enable();
    }
});
Ext.reg("openAttachment", CMDBuild.Management.OpenAttachment);