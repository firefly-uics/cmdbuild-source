Ext.define("CMDBuild.Administration.PrivilegeGrid", {
  extend: "Ext.grid.Panel", // TODO extjs 3 to 4 migration @@ editorgrid
  alias: "privilegegrid",
  translation: CMDBuild.Translation.administration.modsecurity.privilege,
  recordToChange: undefined,
  enableDragDrop : false,
  
  initComponent:function() {
    
    this.addAction = new Ext.Action({	
      	iconCls : 'add',
      	text : this.translation.add_privilege,
      	handler : function() {
   		    this.publish('cmdb-new-group-privilege');
   		    this.getSelectionModel().clearSelections();
      	},
      	scope : this
    });
    
    var store = new Ext.data.JsonStore({
        root:'rows',
        url: 'services/json/schema/modsecurity/getprivilegelist',       
        fields:[
           {name:'classname'},
           {name:'none_privilege'},
           {name:'read_privilege'},
           {name:'write_privilege'}
        ],
        sortInfo: {
            field: 'classname',
            direction: 'ASC'
        }
    });
     
    this.permissionNone = new Ext.grid.CheckColumn({
       header: this.translation.none_privilege,
       dataIndex: 'none_privilege',
       width: 70,
       fixed: true
	});
	
	this.permissionRead = new Ext.grid.CheckColumn({
       header: this.translation.read_privilege,
       dataIndex: 'read_privilege',
       width: 70,
       fixed: true
	});
	
    this.permissionWrite = new Ext.grid.CheckColumn({
       header: this.translation.write_privilege,
       dataIndex: 'write_privilege',
       width: 70,
       fixed: true
	}); 
    
    var columns = [{
        id : 'classname',
        hideable: false,
        header : this.translation.classname,
        dataIndex : 'classname',
        width: 300,
        fixed: false,
        sortable: true
      }, 
      this.permissionNone, 
      this.permissionRead, 
      this.permissionWrite
    ];
    
    Ext.apply(this, {      
      columns : columns,
      store: store,
      viewConfig: { forceFit:true },
      sm:  new Ext.grid.RowSelectionModel(),
      plugins: [
      	this.permissionNone,
       	this.permissionRead, 
      	this.permissionWrite
      ]      
    });
    
    CMDBuild.Administration.PrivilegeGrid.superclass.initComponent.apply(this, arguments);

    this.getSelectionModel().on('rowselect', this.privilegeSelected , this);
   	this.subscribe('cmdb-init-group', this.selectedGroup, this);
   	this.subscribe('cmdb-modified-group-privilege', this.selectedGroup, this);
   	
   	this.permissionNone.onMouseDown = this.clickPrivileges;
   	this.permissionRead.onMouseDown = this.clickPrivileges; 
    this.permissionWrite.onMouseDown = this.clickPrivileges;
  },
 
  selectedGroup: function(params) {
  	if(params && params.groupId) {
  		this.getStore().baseParams['groupId'] = params.groupId;
  	} else { 
  		this.getStore().baseParams['groupId'] = -1;
  	}   	
  	this.getStore().load();
  },
  
  privilegeSelected:function(sm, row, rec) {
	var eventParams = {
		record: new Ext.data.Record(rec.json)
	}
	this.publish('cmdb-load-group-privilege', eventParams);	
  },
  
  clickPrivileges: function(e,t){
    if(t.className && t.className.indexOf('x-grid3-cc-'+this.id) != -1){
        e.stopEvent();
        var index = this.grid.getView().findRowIndex(t);
        this.recordToChange = this.grid.store.getAt(index);
        this.grid.clearCheck(this.recordToChange);
        //this set the checked box to true
        this.recordToChange.set(this.dataIndex, !this.recordToChange.data[this.dataIndex]);
        this.recordToChange.set('privilege_mode', this.dataIndex)
        
        var params = {
        	privilege_mode: this.recordToChange.data['privilege_mode'],
        	groupId: this.recordToChange.json['groupId'],
        	classid: this.recordToChange.json['classid']
        };
        var record = this.recordToChange;
        CMDBuild.Ajax.request({
			url: 'services/json/schema/modsecurity/saveprivilege',
	   		params: params,
			scope: this.grid,		
			success: function(response) {
				this.publish('cmdb-load-group-privilege', {record: record});				
			},
			failure: function(response) {
				this.getStore().load();
			}
		});
    }
  },
  
  clearCheck: function(record){
  	for (var i = 0, len = this.plugins.length ; i<len ; i++){
  		var dataindex = this.plugins[i].dataIndex;
  		record.set(dataindex, false);
  	}
  	
  }
});