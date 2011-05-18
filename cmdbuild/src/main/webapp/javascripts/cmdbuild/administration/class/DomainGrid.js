CMDBuild.Administration.DomainGrid = Ext.extend(CMDBuild.Grid, {
  translation: CMDBuild.Translation.administration.modClass.domainProperties,
  eventtype : 'class', 
  initComponent:function() {
    
    this.addAction = new Ext.Button({	
      	iconCls : 'add',
      	text : this.translation.add_domain,
      	handler : function() {
   		    this.publish('cmdb-new-'+this.eventtype+'domain');
   		    this.getSelectionModel().clearSelections();
      	},
      	scope : this
    });
  	
    var columns = [{
        id : 'name',
        hideable: false,
        header : this.translation.name,
        dataIndex : 'name'
      }, {
        header : this.translation.description,
        dataIndex : 'description'
      }, {
        header : this.translation.description_direct,
        dataIndex : 'descrdir'
      }, {
        header : this.translation.description_inverse,
        dataIndex : 'descrinv'
      }, {
        header : this.translation.class_target,
        dataIndex : 'class1'
      }, {
        header : this.translation.class_destination,
        dataIndex : 'class2'
      }, {
        header : this.translation.cardinality,
        dataIndex : 'cardinality'
      }, new Ext.grid.CheckColumn({
        header : this.translation.m_d,
        dataIndex : 'md'
      })
    ];
    
    Ext.apply(this, {      
      tbar:[this.addAction, '->', {
        			xtype: 'checkbox',
        			boxLabel: CMDBuild.Translation.administration.modClass.include_inherited,
        			checked: true,
        			handler: function(obj, checked) {
       	  				this.filterInherited(!checked);
        			},
        		scope: this}],
      columns : columns,
      baseUrl : 'services/json/schema/modclass/getdomainlist',
      enableDragDrop : true
    });
 
    CMDBuild.Administration.DomainGrid.superclass.initComponent.apply(this, arguments);
    this.pagingBar.hide();
    this.getSelectionModel().on('rowselect', this.domainSelected , this);
    
    this.subscribe('cmdb-init-'+this.eventtype, this.loadData, this);
    this.subscribe('cmdb-modified-'+this.eventtype+'domain', this.loadData, this);
  },
 
  loadData: function(params) {
	  if (params.idClass) {
		  var table = CMDBuild.Cache.getTableById(params.idClass);
		  if (table.tableType == CMDBuild.Constants.cachedTableType.simpletable) {
			  return;
		  }
	  }
	  
	  this.getStore().load( {
		  params: {
		  	idClass: params.idClass || -1
	  	  },
	  	  callback: function() {
	  		  this.filterInherited(this.filtering);
	  	  },
	  	  scope: this
	  });
  },
  
  filterInherited: function(filter) {
    this.filtering = filter;
    if	(filter)
		this.getStore().filterBy(function(record) {return ! record.json.inherited});
    else
		this.getStore().filterBy(function(record) {return true});
  },
  
  domainSelected:function(sm, row, rec) {
	var eventParams = {
		record: new Ext.data.Record(rec.json)
	}

	this.publish('cmdb-load-'+this.eventtype+'domain', eventParams);
  }


});
Ext.reg('domaingrid', CMDBuild.Administration.DomainGrid);