(function() {
	var configUpdated = function(updatedVars) {
		this.treePanel.setDisabled('true' != updatedVars.enabled);    	
    };
    
    CMDBuild.WorkflowTreePanelController = Ext.extend(CMDBuild.TreePanelController, {
    	deselectOn: "cmdb-addprocess-action",
		initComponent : function() {
			CMDBuild.ClassTreePanelController.superclass.initComponent.apply(this, arguments);
			this.listen('cmdb-config-update-workflow', configUpdated);
		}
	});

	CMDBuild.Administration.WorkflowTree = Ext.extend(CMDBuild.TreePanel, {
		title:  CMDBuild.Translation.administration.modWorkflow.tree_title, 
		initComponent: function() {
			var tree = CMDBuild.TreeUtility.getTree(CMDBuild.Constants.cachedTableType.processclass, 
					undefined, undefined, sorted = true);
			this.root = tree;
			this.rootVisible = false;
			this.border = false;
			this.fakeNodeEventName = CMDBuild.Constants.cachedTableType.processclass;
			CMDBuild.Administration.WorkflowTree.superclass.initComponent.apply(this, arguments);
			this.setDisabled(!CMDBuild.Config.workflow.enabled);
		}
	});
	
	
	CMDBuild.Administration.ModWorkflow =  Ext.extend(CMDBuild.ModPanel, {
	    id: 'modWorkflow',
	    newMod: true,
	    modtype:'processclass',
	    hideMode: "offsets",
	    translation: CMDBuild.Translation.administration.modWorkflow,
	
	    initComponent: function() {
	    	
			this.addProcessAction = new Ext.Action({
	            iconCls : 'add',
	            text : this.translation.add_process,
	            handler : function() {
	                this.tabPanel.setActiveTab('process_panel');
	                this.activateTabs(false);
	                var form = Ext.getCmp("processform");
	                if (form) {
	                	form.onNewClass({
	                        idClass: -1
	                    });
	                }
	                this.publish("cmdb-addprocess-action");
	            },
	            position: 'left',
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
	                id : 'process_panel',
	                layout : 'fit',
	                items :[{
	                    id: 'processform',
	                    xtype: 'classform',
	                    eventtype: this.modtype,
			        	superclassurl: 'services/json/schema/modclass/getprocesssuperclasses',
			        	defaultParent: "Activity"
	                }]
	            }, {
	            	disabled: true,
	                title : this.translation.tabs.attributes,
	                id : 'process_attr_panel',
	                layout : 'border',
	                items : [{
	                    id: 'processattributegrid',
	                    xtype: 'attributegrid',
	                    region: 'center',
	                    eventtype: this.modtype,
	                    hideNotNull: true
	                },{
	                    id: 'processattributeform',
	                    xtype: 'attributeform',
	                    height: '50%',
	                    border : false,                 
	                    region: 'south',
	                    autoScroll:true,
	                    split:true,
	                    eventtype: this.modtype,
	                    hideNotNull: true
	                }]
	             }, {
	            	 disabled: true,
	                title : this.translation.tabs.domains,
	                id : 'process_dom_panel',
	                layout : 'border',
	                items : [{
	                    id: 'processdomaingrid',
	                    xtype: 'domaingrid',
	                    region: 'center',
	                    eventtype: this.modtype
	
	                },{
	                    id: 'processdomainform',
	                    xtype: 'domainform',
	                    height: '50%',
	                    border : false,
	                    region: 'south',
	                    autoScroll:true,
	                    split:true,
	                    eventtype: this.modtype
	                    
	                }]
	            }, {
	            	disabled: true,
	                title : this.translation.tabs.xpdl,
	                id : 'process_xpdl_panel',
	                layout : 'border',
	                items :[{
	                    xtype : 'xpdluploadform',
	                   	region: 'center',
	                   	split: true,
	                   	border : false,
	                   	frame: true
	                },{
	                    xtype: 'xpdldownloadform',
	                    region: 'south',
	                    height: '50%',
	                    split: true,
	                    border : false,
	                    frame: true                  
	                }]
	            }, {
	            	disabled: true,
	            	xtype: 'schedulerpanel',
	                id : 'process_scheduling_panel',
	                title : this.translation.tabs.scheduling
	            }]
	        });
	
	        Ext.apply(this,{
	            modtype: 'processclass',
	            title: this.translation.title,
	            tbar:[this.addProcessAction],            
	            basetitle : this.translation.title+ ' - ',
	            layout: 'fit',
	            id : this.id + '_panel',
	            items: this.tabPanel
	        });
	
	        var disabled = (!CMDBuild.Config.workflow.enabled) || false;
	        this.subscribe('cmdb-modified-'+this.modtype, function(){this.activateTabs(true);}, this);
	        this.subscribe('cmdb-abortmodify-'+this.modtype, function(p) {
	        	this.activateTabs(p.idClass > 0);
	        }, this);
	        this.subscribe('cmdb-select-'+this.modtype, this.selectProcess, this);
	        
	        this.subscribe('cmdb-init-'+this.modtype, function(params){
	        	this.loadXpdlInfo(params.idClass);
	        	this.activateTabs(true);
	        }, this);
	        
	        this.subscribe('cmdb-new-'+this.modtype, function(params){
	        	this.loadXpdlInfo(params.idClass);
	        	this.activateTabs(false);
	        }, this);
	                
	        CMDBuild.Administration.ModWorkflow.superclass.initComponent.apply(this, arguments);
	    },

	    activateTabs: function(bool){
	        this.tabPanel.getItem('process_attr_panel').setDisabled(!bool);
	        this.tabPanel.getItem('process_dom_panel').setDisabled(!bool);
	    	this.tabPanel.getItem('process_xpdl_panel').setDisabled(!bool || this.currentClassParams.superItem);
	    	this.tabPanel.getItem('process_scheduling_panel').setDisabled(!bool || this.currentClassParams.superItem);
	    },
	
	    loadXpdlInfo: function(idClass) {
	    	if (idClass > 0) {
		    	CMDBuild.Ajax.request({
		    	   url : 'services/json/schema/modworkflow/xpdlinfo',
		    	   method: 'POST',
		    	   params: {idClass : idClass},
		    	   scope: this,
		    	   success: function(response, options, xpdlInfo) {
		    		   this.publish('cmdb-xpdl-loaded', xpdlInfo.data);    	   
		    	   }
		    	});
	    	}
	    },
	    
	    /**
	     * @param {} params (optional) contains the selected class id and name
	     */
	    selectProcess: function(table) {
	    	this.currentClassParams = table;
	    	if (table && table.id != "fakeNode") {
	            this.publish('cmdb-init-processclass', {
	                    idClass: table.id,
	                    cachedNode: table
	            });
	        }
	    }
	});
})();