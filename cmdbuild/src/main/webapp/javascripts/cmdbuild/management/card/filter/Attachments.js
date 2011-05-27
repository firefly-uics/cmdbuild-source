CMDBuild.Management.Attachments = Ext.extend(Ext.Panel, {
	title: CMDBuild.Translation.management.findfilter.attachments,
	layout: 'fit',
	height: 200,
	collapsed: true,

	initComponent:function() {
		Ext.apply(this, {
			items: [{xtype: 'tbdpanel'}],
			frame: true
		});
    	CMDBuild.Management.Attachments.superclass.initComponent.call(this);

    	this.setDisabled(CMDBuild.Config.dms.enabled == "false");
     }
});
Ext.reg('cardfilterattachments', CMDBuild.Management.Attachments);