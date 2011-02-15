CMDBuild.LookupTreePanelController = Ext.extend(CMDBuild.TreePanelController, {
	deselectOn: "cmdb-addlookuptype-action"
});

CMDBuild.Administration.ModLookup = Ext.extend(CMDBuild.ModPanel, {
	modtype: 'lookuptype',
	translation: CMDBuild.Translation.administration.modLookup,
	layout: 'fit',

    initComponent: function() {

		this.addLookupTypeAction = new Ext.Action({
			iconCls:'add',
			text: this.translation.add_lookuptype,
			handler: this.onAddLookupTypeAction,
			scope : this
		});
		
		this.tabPanel = new Ext.TabPanel({
      		border : false,
      		activeTab : 0,
      		layoutOnTabChange : true,
      		defaults : { 
      			layout : 'fit'
      		},
      		items : [{
       		 	title : this.translation.tabs.properties,
        		id : 'lookuptype_panel',
        		layout : 'fit',
        		items :[{
		        	id: 'lookuptypeform',
		        	xtype: 'lookuptypeform'
		    	}]
      		},{
        		title : this.translation.tabs.lookuplist,
        		id : 'lookup_panel',
        		layout : 'border',
        		items : [{		        	
		        	xtype: 'lookupgrid',
		        	region: 'center',
		        	border: false,
		        	frame: false,
		        	style: {'border-bottom': '1px #D0D0D0 solid'}
		    	},{
					xtype: 'lookupform',
					height: '50%',
		    		border : false,		    		
					region: 'south',
					autoScroll:true,
					split: true
      		    }]
      		 }]
    	});
		
    	Ext.apply(this, {
    		hideMode: 'offsets',//fix a render bug of combobox
      		title : this.translation.title,      		
      		basetitle : this.translation.title+ ' - ',
      		layout: 'fit',
      		id : this.id + '_panel',
      		tbar:[this.addLookupTypeAction],
    		items: [this.tabPanel]
    	});
    	
    	CMDBuild.Administration.ModLookup.superclass.initComponent.apply(this, arguments);
    	this.subscribe('cmdb-select-lookuptype', this.onSelectLookupType, this);
    	this.subscribe('cmdb-abort-newltype', this.onAbortNewLtype, this);
   	},
   	
   	onAddLookupTypeAction: function() {
   		this.tabPanel.activate('lookuptype_panel');
   		this.tabPanel.getItem('lookup_panel').disable();
   		this.addLookupTypeAction.disable();
		this.publish('cmdb-addlookuptype-action');
   	},
   	
   	onSelectLookupType: function(eventParams) {		
		if (eventParams) {
			this.publish('cmdb-init-lookup', {
				lookupType: eventParams.id
			});
			this.addLookupTypeAction.enable();
		}
		this.tabPanel.getItem('lookup_panel').enable();
  	},
  	
  	onAbortNewLtype: function() {
  		this.addLookupTypeAction.enable(); 		
  	}
});