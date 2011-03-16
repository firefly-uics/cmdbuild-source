CMDBuild.Management.SaveFilter = Ext.extend(Ext.Panel, {
	title: CMDBuild.Translation.management.findfilter.save_filter,
	layout: 'fit',
	collapsed: true,

	initComponent:function() {
		Ext.apply(this, {
			items: [{xtype: 'tbdpanel'}],
			frame: true
		});
    	// call parent initComponent
    	CMDBuild.Management.SaveFilter.superclass.initComponent.call(this);		
     } // end of function initComponent
});
Ext.reg('cardfiltersavefilter', CMDBuild.Management.SaveFilter);