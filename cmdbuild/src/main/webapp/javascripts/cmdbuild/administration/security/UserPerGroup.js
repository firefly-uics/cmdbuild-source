CMDBuild.Administration.UserPerGroup = Ext.extend(Ext.Panel, {
	translation: CMDBuild.Translation.administration.modsecurity,
	initComponent: function() {
		this.availableUsersStore = new Ext.data.JsonStore({
			url : 'services/json/schema/modsecurity/getgroupuserlist',
			baseParams: {
				alreadyAssociated: false
			},
			sortInfo : {
				field : 'username',
				direction : "ASC"
			},
			root : "users",
			fields : ['description', 'username']
		});
			
		this.assignedUsersStore = new Ext.data.JsonStore({
			url : 'services/json/schema/modsecurity/getgroupuserlist',
			baseParams: {
				alreadyAssociated: true
			},
			sortInfo : {
				field : 'username',
				direction : "ASC"
			},
			root : "users",
			fields : ['description', 'username']
		});
		
		var availableUsersGrid =  new Ext.grid.GridPanel({			
			ddGroup: 'assignedDDGroup',
			store: this.availableUsersStore,
			margins: '0 15 0 0',
			columns: [
				{header: this.translation.user.username, sortable: true, dataIndex: 'username'},
				{header: this.translation.user.description, sortable: true, dataIndex: 'description'}
			],
			enableDragDrop: true,
			stripeRows: true,
			title: this.translation.group.availableusers,
			viewConfig: { forceFit:true },
			autoScroll: true,
			frame: false,
			border: false,
			style: {border: "1px " + CMDBuild.Constants.colors.gray.border + " solid"},
			flex: 1
		});
		
		var assignedUsersGrid =  new Ext.grid.GridPanel({
			ddGroup: 'avaiableDDGroup',
			store: this.assignedUsersStore,
			margins: '0 0 0 0',
			columns: [
				{header: this.translation.user.username, sortable: true, dataIndex: 'username'},
				{header: this.translation.user.description, sortable: true, dataIndex: 'description'}
			],
			enableDragDrop: true,
			stripeRows: true,
			title: this.translation.group.groupchoice,
			viewConfig: { forceFit:true },
			autoScroll: true,
			frame: false,
			border: false,
			style: {border: "1px " + CMDBuild.Constants.colors.gray.border + " solid"},
			flex: 1
		});
		
		var _this = this;
		
		var concatUsersIdOfAssignedUsers = function() {
			var out = "";
			var users = _this.assignedUsersStore.getRange();
			for (var i=0, len=users.length; i<len ; ++i) {
				if (i > 0) {
					out = out.concat(",");
				}
				var u = users[i];
				out = out.concat(u.json.userid);
			}
			return out;
		};
		
		var saveGroupUsers = function() {
			CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request({
				url : 'services/json/schema/modsecurity/savegroupuserlist',
				params:{
					groupId: this.groupId,
					users: concatUsersIdOfAssignedUsers()
				},
				method : 'POST',
				scope : this,
				success : function(response, options, decoded) {
					CMDBuild.log.info('modified group', decoded);
					CMDBuild.LoadMask.get().hide();
					CMDBuild.Msg.success(arguments);
				}
			});
		};
		
		Ext.apply(this, {
			layout: 'hbox',
			frame: true,
			layoutConfig: {           
				align:'stretch'            
	        },
			items: [availableUsersGrid,assignedUsersGrid],
			buttonAlign: "center",
			buttons: [{
				text: CMDBuild.Translation.common.btns.confirm,
				scope: this,
				handler: saveGroupUsers
			}]
		});
		CMDBuild.Administration.UserPerGroup.superclass.initComponent.apply(this, arguments);
		this.subscribe('cmdb-init-group', this.loadData, this);
		
		var onAvaiableGridRender = function() {
			var availableDropTargetEl =  availableUsersGrid.getView().scroller.dom;
	        var availableDropTarget = new Ext.dd.DropTarget(availableDropTargetEl, {
	            ddGroup    : 'avaiableDDGroup',
	            notifyDrop : function(ddSource, e, data){
	                var records =  ddSource.dragData.selections;
	                Ext.each(records, ddSource.grid.store.remove, ddSource.grid.store);
	                availableUsersGrid.store.add(records);
	                availableUsersGrid.store.sort('name', 'ASC');
	                return true
	            }
	        });
		};
		
		var onAssignedGridRender = function() {
	        var assignedDropTargetEl = assignedUsersGrid.getView().scroller.dom;
	        var assignedDropTarget = new Ext.dd.DropTarget(assignedDropTargetEl, {
	            ddGroup: 'assignedDDGroup',
	            notifyDrop : function(ddSource, e, data){
	                var records =  ddSource.dragData.selections;
	                Ext.each(records, ddSource.grid.store.remove, ddSource.grid.store);
	                assignedUsersGrid.store.add(records);
	                assignedUsersGrid.store.sort('name', 'ASC');
	                return true
	            }
	        });
		};
		
		availableUsersGrid.on('render', onAvaiableGridRender, this);
		assignedUsersGrid.on('render', onAssignedGridRender, this);
	        
	        
	},
	
	loadData: function(params) {
		  if (params.groupId) {
			  this.groupId = params.groupId;
		  } else { 
			  this.groupId=-1;
		  }

		  if (this.groupId > 0) {
			  this.availableUsersStore.load({
				  params:{groupId:this.groupId} //alreadyAssociated=true for search existing users for this group			  
			  });
			  this.assignedUsersStore.load({
				  params:{groupId:this.groupId} //alreadyAssociated=false for search users not associated to this group
			  });
		  }
	  }
});
Ext.reg('userpergroup', CMDBuild.Administration.UserPerGroup );