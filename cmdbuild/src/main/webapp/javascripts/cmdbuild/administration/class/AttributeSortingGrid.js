CMDBuild.Administration.AttributeSortingGrid = Ext.extend(CMDBuild.EditorGrid, {
  translation: CMDBuild.Translation.administration.modClass.attributeProperties,
  filtering: false,
  eventtype : 'class', 
  withPagingBar: false,
  initComponent:function() {
	
	this.classOrderOptions = new Ext.form.ComboBox({ 
		name: 'classOrderOptions',		
		valueField: 'value',
		displayField: 'description',
		lazyRender: true,
		hiddenName: 'classOrderOptions',
		store: new Ext.data.SimpleStore({
			fields: ['value','description'],
			data : [
			        [1,this.translation.direction.asc],
			        [-1,this.translation.direction.desc],
			        [0,this.translation.not_in_use]
			]
		}),
		mode: 'local',
		triggerAction: 'all',
		editable: false,
		allowBlank: false,
		ownerCt: this //allows to fly and calculate the right z-index   
	});
		
  	this.columns = [{
		id : 'index',
		hideable: false,
		hidden: true,
		dataIndex : 'index'
	  },{
		id : 'absoluteClassOrder',
		hideable: false,
		hidden: true,
		dataIndex : 'absoluteClassOrder'
	  },{
        id : 'name',
        header : this.translation.name,
        dataIndex : 'name'
      }, {
      	id : 'description',
        header : this.translation.description,
        dataIndex : 'description'
      },{
        header : this.translation.criterion,
        dataIndex : 'classOrderSign',
        editor: this.classOrderOptions,
        renderer: this.comboRender.createDelegate(this, [], true)
      }];
  	
    
    Ext.apply(this, {
      columns : this.columns,
      baseUrl : 'services/json/schema/modclass/getattributelist',
      enableDragDrop : true,
      ddGroup : 'attributeGridDDGroup'
    });
        
    this.on({
      render: this.ddRender,
      beforedestroy: function(g) { Ext.dd.ScrollManager.unregister(g.getView().getEditorParent()); }
    });
 
    CMDBuild.Administration.AttributeSortingGrid.superclass.initComponent.apply(this, arguments);
    this.getStore().load({
		params: {
			idClass : this.idClass
		}
    });
    
    this.store.setDefaultSort("absoluteClassOrder", 'ASC');
  },
  
  comboRender: function(value, meta, record, rowIndex, colIndex, store){
	  if (value > 0)
		  return '<span>'+ this.translation.direction.asc +'</span>';
	  else if (value < 0)
		  return '<span>'+ this.translation.direction.desc +'</span>';
	  else
		  return '<span>'+ this.translation.not_in_use +'</span>';
  },
  
  
  ddRender: function(g) {
    var ddrow = new Ext.ux.dd.GridReorderDropTarget(g, {
      copy : false
    });
    Ext.dd.ScrollManager.register(g.getView().getEditorParent());
  }
  
 });