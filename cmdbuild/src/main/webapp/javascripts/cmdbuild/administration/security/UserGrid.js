Ext.define("CMDBuild.Administration.UserGrid", {
  extend: "CMDBuild.Grid",
  alias: "usergrid",
  translation: CMDBuild.Translation.administration.modsecurity.user,
  
  
  initComponent:function() {
    
  	//ADD USER ACTION
    this.addAction = new Ext.Action({	
      	iconCls : 'add',
      	text : this.translation.add_user,
      	handler : function() {
   		    this.publish('cmdb-new-user');
   		    this.getSelectionModel().clearSelections();
      	},
      	scope : this
    });
  	
    var columns = [{
        id : 'username',
        header : this.translation.username,
        dataIndex : 'username',
        sortable: true
      },{
        hideable: false,
        header : this.translation.description,
        dataIndex : 'description',
        sortable: true
      },new Ext.grid.CheckColumn({
         header : this.translation.isactive,
         dataIndex : 'isactive',
	     width: 70,
	     fixed: true,
	     sortable: true
	  })
	];
    
    Ext.apply(this, {      
      columns : columns,
      extraFieldsStore: ['userid'],
      baseUrl : 'services/json/schema/modsecurity/getuserlist',
      enableDragDrop : true
    });
 
    CMDBuild.Administration.UserGrid.superclass.initComponent.apply(this, arguments);
    this.pagingBar.hide();
    this.getSelectionModel().on('rowselect', this.userSelected , this);
    
    this.subscribe('cmdb-init-user', this.loadData, this);
    this.subscribe('cmdb-new-user', this.loadData, this);
    this.subscribe('cmdb-modified-user', this.loadData, this);
  },
 
	loadData: function(params) {
	  if (params)
		  var userid = params.userid
	  this.store.load({
		  params: { start:0, limit:1000 },
		  scope: this,
		  callback: function() {
			  if (userid) {
				  var index = this.store.find('userid', userid)
				  this.getSelectionModel().selectRow(index)
				  CMDBuild.log.info('selected row ', index)
			  }
		  } 
	  });
	},
  
  userSelected:function(sm, row, rec) {
	var eventParams = {
		record: new Ext.data.Record(rec.json)
	}

	this.publish('cmdb-load-user', eventParams);
  }
});