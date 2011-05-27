CMDBuild.Administration.LookupGrid = Ext.extend(Ext.grid.GridPanel, {
	
	translation: CMDBuild.Translation.administration.modLookup.lookupGrid,
	remoteSort: false,
  	
	initComponent:function() {
		this.pageSize = parseInt(CMDBuild.Config.cmdbuild.referencecombolimit);
	  	
		this.addAction = new Ext.Action({	
	      	iconCls : 'add',
	      	text : this.translation.add_lookup,
	      	handler : function() {
	   		    this.publish('cmdb-new-lookup');
	   		    this.getSelectionModel().clearSelections();
	      	},
	      	scope : this
	    });

		var LOOKUP_FIELDS = CMDBuild.ServiceProxy.LOOKUP_FIELDS;
		var store = new Ext.data.JsonStore({
	        root:'rows',
	        url: 'services/json/schema/modlookup/getlookuplist',
	        fields:[
	           {name: LOOKUP_FIELDS.Index },
	           {name: LOOKUP_FIELDS.Code },
	           {name: LOOKUP_FIELDS.Description },
	           {name: LOOKUP_FIELDS.ParentDescription },
	           {name: LOOKUP_FIELDS.Active },
	           {name: LOOKUP_FIELDS.Id }
	        ],
	        remoteSort: true
	    });
  	
	  	var columns = [{
	  		hideable: false,
			hidden: true,
	        dataIndex : LOOKUP_FIELDS.Index
	      },{
	        header : this.translation.code,
	        dataIndex : LOOKUP_FIELDS.Code,
	        width: 150,
	        fixed: true
	      },{
	        header : this.translation.description,
	        dataIndex : LOOKUP_FIELDS.Description
	      }, {
	        header : this.translation.parentdescription,
	        dataIndex : LOOKUP_FIELDS.ParentDescription
	      }, new Ext.grid.CheckColumn({
	        header : this.translation.active,
	        dataIndex : LOOKUP_FIELDS.Active,
	        width: 50,
	        fixed: true
	      },{
	        header : this.translation.description,
	        hidden: true,
	        dataIndex : LOOKUP_FIELDS.Id
	      })
	    ];

	    var bbar = new Ext.PagingToolbar({
	  		store: store,
	  		displayInfo:true,
	  		pageSize: this.pageSize
	  	});
    
	    Ext.apply(this, {
	    	viewConfig: { forceFit:true },
	    	tbar:[this.addAction],
	    	store: store,
	      	columns : columns,
	      	bbar: bbar,
	      	enableDragDrop : true,
	      	ddGroup : 'lookupGridDDGroup'     	
	    });
	   
	    this.on({
	      render: this.ddRender,
	      beforedestroy: function(g) { Ext.dd.ScrollManager.unregister(g.getView().getEditorParent()); }
	    });
	    
	    CMDBuild.Administration.LookupGrid.superclass.initComponent.apply(this, arguments);

	    this.getSelectionModel().on('rowselect', this.lookupSelected , this);
	    this.getSelectionModel().on('rowdeselect', function(){
	    	this.row = undefined;
	    	CMDBuild.log.info('deselect');
	    }, this);
	    this.subscribe('cmdb-init-lookup', this.initGrid, this);
	    this.subscribe('cmdb-modified-lookup', this.loadData, this);
  	},
  
	initGrid: function(params) {
		var sm = this.getSelectionModel();
		if (sm.getSelections().length > 0) {
			sm.clearSelections();
		}
		delete(sm);
		
		var lookupType;
		if (params.lookupType) {
			this.store.baseParams.type = params.lookupType;
		}
		
		this.loadData(params);
	},
  
	ddRender: function(g) {
		var ddrow = new Ext.ux.dd.GridReorderDropTarget(g, {
			copy : false,
			listeners : {
				beforerowmove : function(objThis, oldIndex, newIndex, records) {
					var g = objThis.getGrid();
					var gStore = g.getStore();

					//change the number of moved record
					var recordMoved = records[0];
					var rowList = [{
						id: recordMoved.json.Id, 
						index: gStore.getAt(newIndex).json.Number
					}];

					//change the number of the records between the new and old index
					var fillRowList = function(firstIndex, lastIndex, direction) {
						for (var i=firstIndex; i<lastIndex; ++i) {
							var rec = gStore.getAt(i);
							var index = rec.json.Number;
							rowList.push({
								id: rec.json.Id, 
								index: index + direction
							});
						}
					};
					if (oldIndex > newIndex) {
						fillRowList(newIndex, oldIndex, 1);
					} else {
						fillRowList(oldIndex+1, newIndex+1, -1);
					}

					var savePosition = function(type, rowList) {
						CMDBuild.Ajax.request({
							url: 'services/json/schema/modlookup/reorderlookup',
							method: 'POST',
							params: {
								type: type ,
								lookuplist: Ext.util.JSON.encode(rowList)
							},
							callback: function(response,options) {
								gStore.load();
								g.getEl().unmask();
								g.getView().restoreScroll(scrollState);
							}
						});
					}(records[0].json.Type, rowList);
					
					//manage scrollState
					var scrollState = g.getView().getScrollState();
					g.getView().restoreScroll(scrollState);
		            
				}
			}
		});
    
    Ext.dd.ScrollManager.register(g.getView().getEditorParent());
  },
  
  
  loadData: function(params) {
	  var lookupType;
	  if (params.lookupType) {
		  lookupType = params.lookupType;
	  }
	  if (lookupType && lookupType != '') {
		  this.store.load({
			  scope: this,
			  params: {
			  	start: 0,
			  	limit: this.pageSize
		  	  },
		  	  callback: function() {
		  		  if (this.row || this.row >= 0) {
		  			  this.getSelectionModel().selectRow(this.row);
		  		  }
		  	  }
		  });
	  }
  },
  
  lookupSelected:function(sm, row, rec) {
	var eventParams = {
		record: new Ext.data.Record(rec.json)
	};
	this.row = row; 
	this.publish('cmdb-load-lookup', eventParams);
  }
  
});
Ext.reg('lookupgrid', CMDBuild.Administration.LookupGrid);