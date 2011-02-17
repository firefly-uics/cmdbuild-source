CMDBuild.Management.OpenNote = Ext.extend(CMDBuild.Management.BaseExtendedAttribute, {
    initialize: function(extAttrDef) {},
    
    tabpanel_id: 'activitynotes_tab',

    onExtAttrShow: function(extAttr) {
    	var acttabpnl = this.findParentByType('activitytabpanel');
    	var actnotestab = acttabpnl.getComponent('activitynotes_tab');
        acttabpnl.setActiveTab('activitynotes_tab');
        actnotestab.enableModify();
    }
});
Ext.reg("openNote", CMDBuild.Management.OpenNote);