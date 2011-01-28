CMDBuild.Management.ActivityOptionTab = Ext.extend(Ext.Panel, {
    translation : CMDBuild.Translation.management.modworkflow,

    initComponent: function() {
    	
    	Ext.apply(this,{
    		hideMode: 'offsets',
    		layout: 'card',    	   
    		frame: false,
    		border: false
    	});
    	
    	CMDBuild.Management.ActivityOptionTab.superclass.initComponent.apply(this, arguments);
    	this.subscribe('cmdb-load-activity', this.loadForActivity, this);
    	this.subscribe('cmdb-extattr-instanced', this.extAttrInstanced, this);
    },

    reset: function() {
    	CMDBuild.log.info('Removing XAs');
    	this.removeAll(true);
    },

    extAttrInstanced: function(evtParams) {
    	if (evtParams.bottomButtons) {
    		Ext.each(evtParams.bottomButtons, function(btn) {
    		    this.addButton(btn);
                btn.hide();
    		}, this);
    	}
    },
    
    loadForActivity: function(eventParams) {
    	this.reset();
    	var activity = eventParams.record.data;
    	this.cmdbExtAttrDefs = activity.CmdbuildExtendedAttributes;
    	if (!this.cmdbExtAttrDefs) {
    		return;
    	}
    	Ext.each(this.cmdbExtAttrDefs, function(item) {
            CMDBuild.log.info('Adding XA ' + item.extattrtype);
            this.add({
    	       xtype: item.extattrtype,
    	       id: item.identifier,
    	       extAttrDef: item,
    	       activity: activity,
    	       processInstanceId: eventParams.record.data.ProcessInstanceId,
    	       workItemId: eventParams.record.data.WorkItemId
    	   });
    	}, this);
    	this.doLayout(true);
    }    
});

Ext.reg('activityoptiontab', CMDBuild.Management.ActivityOptionTab);