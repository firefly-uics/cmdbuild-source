(function() {

CMDBuild.Administration.SecurityTree = Ext.extend(CMDBuild.TreePanel, {	
	initComponent: function() {
		this.groupTree = CMDBuild.TreeUtility.getTree(CMDBuild.Constants.cachedTableType.group, 
				CMDBuild.Constants.cachedTableType.group,
				undefined, 
				sorted=true);
		this.groupTree.text = CMDBuild.Translation.administration.modsecurity.groups;
		// Build a tree with two children, the root of the groups tree and a fake
		// node to show the CMDBuild.ModUser
		this.root = new Ext.tree.TreeNode();
		this.root.appendChild([this.groupTree, new Ext.tree.TreeNode({
			text: CMDBuild.Translation.administration.modsecurity.users,
			type: "user",
			leaf: true,
			iconCls: "cmdbuild-tree-user-icon"
		})]);
		
		this.title = CMDBuild.Translation.administration.modsecurity.title,
		this.rootVisible = false;
		this.border = false;
		CMDBuild.Administration.SecurityTree.superclass.initComponent.apply(this, arguments);
	}
});

CMDBuild.SecurityTreePanelController = Ext.extend(CMDBuild.TreePanelController, {	
	deselectOn: "cmdb-addgroup-action",
	onNewNode: function(parameters) {
		if (parameters) {
			var node = CMDBuild.TreeUtility.buildNodeFromTable(parameters);
			var parent = this.treePanel.appendNewNode(node, CMDBuild.Constants.cachedTableType.group);
			if (!this.silentListener) {
				this.treePanel.selectNodeById(node.id);
			}
		}
	},
	onSelectNode: function(node) {
		if (node) {
			var attributes = CMDBuild.Cache.getTableById(node.id) || node.attributes;
			var eventType = attributes.type || "group";
			this.publish("cmdb-select-"+eventType, attributes);
		}
	}
});

CMDBuild.Administration.ModSecurity = Ext.extend(CMDBuild.ModPanel, {
	id: 'modgroup_panel',
	modtype: 'group',
	hideMode: 'offsets', //fix a render bug of combobox
	layout: 'fit',
	translation: CMDBuild.Translation.administration.modsecurity,
  
	initComponent: function() {
	
	    this.addGroupAction = new Ext.Action({
	          iconCls:'add',
	          text: this.translation.group.add_group,
	          handler : this.onAddGroup,
	      	  position: 'left',
	      	  scope : this
	    });
	    
	    this.groupForm = new CMDBuild.Administration.GroupForm({
	    	id: 'groupform',
	    	region: 'center',
	    	frame: false,
	    	border: false
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
	    		id : 'group_panel',
	    		layout : 'border',
	    		items :[
	    			this.groupForm
		    	]
	  		},{
	  			title: this.translation.users,
	  			xtype: 'userpergroup',
	  			id : 'user_per_group_panel'
	  		},{
	    		title : this.translation.tabs.permissions,
	    		id : 'privilege_panel',
	    		layout : 'border',
	    		items : [{
		        	id: 'privilegegrid',
		        	xtype: 'privilegegrid',
		        	region: 'center',
		        	frame: false,
		        	border: false,
		        	style: {'border-bottom':'1px '+CMDBuild.Constants.colors.gray.border+' solid'}
		    	}]
	  		}]
		});
	    
	    this.subscribe('cmdb-select-group', this.selectGroup, this);
	    this.subscribe('cmdb-abortmodify-group', this.abortModity, this);
	    
		Ext.apply(this, {
			tbar:[this.addGroupAction],
			title : this.translation.group.title,
			basetitle : this.translation.group.title+ ' - ',
			items: this.tabPanel,
			frame: false
		});
	 
	    CMDBuild.Administration.ModSecurity.superclass.initComponent.apply(this, arguments);   
	},
  
	selectGroup: function(eventParams) {
		if (eventParams && eventParams.selectable) {
			this.enableTabs();
			this.publish('cmdb-init-group', {
				groupId: eventParams.id
			});
			this.groupForm.setCurrentSelection("group");
		} else {
			this.disableTabs();
			this.groupForm.setCurrentSelection("groupfolder");
			this.groupForm.reset();
		}
	},
	
	disableTabs: function() {
		Ext.ComponentMgr.get('privilege_panel').disable();
		Ext.ComponentMgr.get('group_panel').disable();
		Ext.ComponentMgr.get('user_per_group_panel').disable();
	},
	
	enableTabs: function() {
		Ext.ComponentMgr.get('privilege_panel').enable();
		Ext.ComponentMgr.get('group_panel').enable();
		Ext.ComponentMgr.get('user_per_group_panel').enable();
	},
	
	onAddGroup: function() {
		this.enableTabs();
		this.tabPanel.setActiveTab(0);
		Ext.ComponentMgr.get('privilege_panel').disable();
		Ext.ComponentMgr.get('user_per_group_panel').disable();
		this.groupForm.newGroup();
		this.publish("cmdb-addgroup-action");
	},
	
	abortModity: function(p) {
		if  (this.groupForm.currentSelection == "group" 
				&& p.groupId > 0) { // the abort comes after the try to add a group
			this.enableTabs();
		} else {
			this.disableTabs();
		}
	}
});

})();