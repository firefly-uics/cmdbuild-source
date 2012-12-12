(function() {
	
var tr = CMDBuild.Translation.administration.modsecurity.privilege;
	
Ext.define("CMDBuild.view.administration.group.CMGroupPrivilegeGrid", {
  extend: "Ext.grid.Panel",
  alias: "privilegegrid",
  
  recordToChange: undefined,
  enableDragDrop : false,
  
  initComponent:function() {
    
    this.permissionNone = new Ext.ux.CheckColumn({
       header: tr.none_privilege,
       dataIndex: 'none_privilege',
       width: 70,
       fixed: true
	});
	
	this.permissionRead = new Ext.ux.CheckColumn({
       header: tr.read_privilege,
       dataIndex: 'read_privilege',
       width: 70,
       fixed: true
	});
	
    this.permissionWrite = new Ext.ux.CheckColumn({
       header: tr.write_privilege,
       dataIndex: 'write_privilege',
       width: 70,
       fixed: true
	}); 
    
    
    var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
        clicksToEdit: 1
    });
    
    Ext.apply(this, {      
		columns : [{
				hideable: false,
				header : tr.classname,
				dataIndex : 'classname',
				flex: 1,
				sortable: true
			}, 
			this.permissionNone, 
      		this.permissionRead, 
      		this.permissionWrite
    	],
      	store: CMDBuild.ServiceProxy.group.getPrivilegesGridStore(),
      	viewConfig: { forceFit:true },
      	sm:  new Ext.selection.RowModel(),
      	plugins: [cellEditing],
      	frame: false,
      	border: false
    });
    
    this.callParent(arguments);

    this.permissionNone.on("checkchange", this.clickPrivileges, this);
    this.permissionRead.on("checkchange", this.clickPrivileges, this);
    this.permissionWrite.on("checkchange", this.clickPrivileges, this);
  },
 
  onGroupSelected: function(group) {
  	this.currentGroup = group.get("id") || -1;
  	this.loadStore();
  },
  
  loadStore: function() {
  	if (this.currentGroup 
  			&& this.currentGroup > 0) {
  		this.getStore().load({
  			params: {
  				groupId: this.currentGroup
  			}
  		});
  	}
  },
  
  clickPrivileges: function(cell, recordIndex, checked) {
    this.recordToChange = this.store.getAt(recordIndex);
	
	var params = {
		privilege_mode: cell.dataIndex,
		groupId: this.recordToChange.get('groupId'),
		classid: this.recordToChange.get('classid')
	};

	CMDBuild.Ajax.request({
		url: 'services/json/schema/modsecurity/saveprivilege',
		params: params,
		scope: this,		
		success: function(response) {
			this.loadStore();
		},
		failure: function(response) {
			this.loadStore();
		}
	});
  }
  
});

})();