(function() {
	var ATTR_TO_SKIP = "Notes";
	var translation = CMDBuild.Translation.administration.modClass.attributeProperties;
	
Ext.define("CMDBuild.Administration.AttributeGrid", {
	extend: "CMDBuild.Grid",
	alias: "attributegrid",
	
  remoteSort: false,
  filtering: false,
  eventtype : 'class', 

  hideNotNull: false, // for processes

  initComponent:function() {

    this.addAction = new Ext.Action({	
      	iconCls : 'add',
      	text : translation.add_attribute,
      	handler : function() {
   		    this.publish('cmdb-new-'+this.eventtype+'attribute');
   		    this.getSelectionModel().clearSelections();
      	},
      	scope : this
    });
  	
    this.orderAction = new Ext.Action({	
      	iconCls : 'order',
      	text : translation.set_sorting_criteria,
      	handler : this.onOrder,
      	scope : this
    });
    
  	var columns = [{
		id : 'index',
		hideable: false,
		hidden: true,
		dataIndex : 'index'
	  },{
        id : 'name',
        header : translation.name,
        dataIndex : 'name'
      }, {
      	id : 'description',
        header : translation.description,
        dataIndex : 'description'
      }, {
        header : translation.type,
        dataIndex : 'type'
      }, new Ext.grid.CheckColumn({
        header : translation.isbasedsp,
        dataIndex : 'isbasedsp'
      }), new Ext.grid.CheckColumn({
        header : translation.isunique,
        dataIndex : 'isunique'
      })];
      if (!this.hideNotNull) {
    	  columns = columns.concat([
    			new Ext.grid.CheckColumn({
    				header : translation.isnotnull,
			        dataIndex : 'isnotnull'
    			}
    		)]);
      }
      columns = columns.concat([
	      new Ext.grid.CheckColumn({
				header : translation.inherited,
				hidden: true,
				dataIndex : 'inherited'
	      }), new Ext.grid.CheckColumn({
		        header : translation.isactive,
		        dataIndex : 'isactive'
	      }),{
		        header : translation.field_visibility,
		        dataIndex : 'fieldmode',
		        renderer: renderEditingMode
	      }, {
	        	id: 'group',
	            header : translation.group,
	            dataIndex : 'group',
	            hidden: true
          }]);

    this.inheriteFlag = new Ext.form.Checkbox({
		boxLabel: CMDBuild.Translation.administration.modClass.include_inherited,
		checked: true,		
		scope: this,
		handler: function(obj, checked) {			
			this.filterInherited(!checked );			
		}
	});
    
    Ext.apply(this, {
      tbar:[this.addAction,
        this.orderAction,
      	'->',
      	this.inheriteFlag
      ],
      columns : columns,
      baseUrl : 'services/json/schema/modclass/getattributelist',
      enableDragDrop : true,
      ddGroup : 'attributeGridDDGroup'
    });
        
    this.on({
      render: this.ddRender,
      beforedestroy: function(g) { Ext.dd.ScrollManager.unregister(g.getView().getEditorParent()); }     
    });
 
    CMDBuild.Administration.AttributeGrid.superclass.initComponent.apply(this, arguments);
    
    this.pagingBar.hide();
    
    this.store.setDefaultSort("index");
    
    this.getStore().on('load', function(store, records, opt) {
      this.filterInherited(this.filtering);
      for (var i=0, l=records.length; i<l; ++i) {
    	  var r = records[i];
    	  if (r.data.name == ATTR_TO_SKIP) {
    		  store.removeAt(i);
    	  }
      }
    }, this);
    
   this.getSelectionModel().on('rowselect', this.attributeSelected , this);
    
    this.subscribe('cmdb-init-'+this.eventtype, this.loadData, this);
    this.subscribe('cmdb-modified-'+this.eventtype+'attribute', this.loadData, this);
  },
  
  ddRender: function(g) {
    var ddrow = new Ext.ux.dd.GridReorderDropTarget(g, {
      copy : false,
      listeners : {
      	beforerowmove : function(objThis, oldIndex, newIndex, records) {
      		var g = objThis.getGrid();
          	var canDrop = g.inheriteFlag.checked;
          	if (canDrop) {
      			return ! objThis.filtering;
          	} else {
          		CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,
          				CMDBuild.Translation.administration.modClass.attributeProperties.msg_warning_drop_row);
          		return false;
          	}
      	},
        afterrowmove : function(objThis, oldIndex, newIndex, records) {
          var g = objThis.getGrid();
          var canDrop = g.inheriteFlag.checked
          if (canDrop) {
          	  var scrollState = g.getView().getScrollState();
        
	          g.getEl().mask(CMDBuild.Translation.common.wait_title,'x-mask-loading');
	          g.getView().restoreScroll(scrollState);
	                    
	          var rowList = [];
	          var gStore = g.getStore();

	          for (var i=0; i<gStore.getCount(); i++) {
	            var rec = gStore.getAt(i);
	            rowList.push({ name: rec.json.name, idx: i+1 });
	          }

	          CMDBuild.Ajax.request({
	            url: 'services/json/schema/modclass',
	            method: 'POST',
	            params: {
	              method: 'reorderAttribute',
	              idClass: records[0].json.idClass,
	              attributes: Ext.util.JSON.encode(rowList)
	            }, 
	            callback: function() {
	              g.getEl().unmask();
	              g.getView().restoreScroll(scrollState);
	            }
	          });
          } else {          	
          	return false
          }
        }
      }
    });
    
    Ext.dd.ScrollManager.register(g.getView().getEditorParent());
  },
  
  loadData: function(params) {
	  
  	if(params && params.idClass) {
  		idClass = params.idClass;
  	CMDBuild.log.info('lissen modified class attribute ', params.idClass)
  	} else { 
  		idClass=-1;
  	}
  	this.classId = idClass;
    this.getStore().load({
		params: {
			idClass : idClass
		},
		callback: function(records, opt, success) {
			this.filterInherited(this.filtering);
			this.selectFirstRow();
		},
		scope: this
    });
  },
  
  filterInherited: function(filter) {
    this.filtering = filter;
    if (filter) {
      this.getStore().filterBy(function(record){return ! record.json.inherited});     
    } else {
      this.getStore().filterBy(function(record){return true});
    }
  },
  
  attributeSelected:function(sm, row, rec) {
	var eventParams = {
		record: new Ext.data.Record(rec.json)
	}
	this.publish('cmdb-load-'+this.eventtype+'attribute', eventParams);
  },
  
  onOrder: function() {
	  var win = new CMDBuild.Administration.SetOrderWindow({
		  idClass: this.classId
	  }).show(); 
  },
  
  selectFirstRow: function() {
	  var _this = this;
	  (function() {
		  if (_this.store.getCount() > 0 && _this.isVisible()) {
			  var sm = _this.getSelectionModel();			 
			  if (! sm.hasSelection()) {
				  sm.selectFirstRow();
			  }
		  }
	  }).defer(200);
  }

});

function renderEditingMode(val) {
	return translation["field_" + val];
}

})();