CMDBuild.Administration.ClassForm = Ext.extend(Ext.Panel, {
	translation: CMDBuild.Translation.administration.modClass.classProperties,
	superclassurl: 'services/json/schema/modclass/getsuperclasses',
	eventtype: 'class',
	defaultParent: "Class",
	
	layout: 'fit',
	colorsConst: CMDBuild.Constants.colors.gray,
	plugins: [new CMDBuild.FormPlugin()],
	
	initComponent:function() {
  	      
		this.deleteAction = new Ext.Action({
			iconCls: 'delete',
			disabled: true,
			text: this.translation.remove_class,
			handler: this.onDelete,
			scope : this
		}),
       
	    this.modifyAction = new Ext.Action({
	    	iconCls: 'modify',
	    	text: this.translation.modify_class,
	    	disabled: true,
	    	handler: this.onModify,
	    	scope : this
	    }),
    
	    this.saveButton = new Ext.Button({
	        text: CMDBuild.Translation.common.buttons.save,
            name: 'saveClassButton',
            formBind: false,
            scope: this,
            handler: this.onSave,
            disabled: true
	    });
    
	    this.abortButton = new Ext.Button({
	      	text: CMDBuild.Translation.common.buttons.abort,
	        name: 'abortClassButton',
	        scope: this,
	        handler: this.onAbort,
	       	disabled: true
	    });
  	
	    this.inheriteCombo = new Ext.form.ComboBox({
			plugins: new CMDBuild.SetValueOnLoadPlugin(),
	        fieldLabel: this.translation.inherits,
	        name: 'inherits_value',
	        hiddenName: 'inherits',
	        width: 220,
	        triggerAction: 'all',
	        valueField: 'value',
	        displayField: 'description',
	        minChars: 0,
	        editable: false,
	        disabled: true,
	        CMDBuildReadonly: true,
	        defaultParent: this.defaultParent,
	        cmdb_hideWithContainer: true,
	        mode: "local",
	        store: new Ext.data.JsonStore( {
	            autoLoad: true,
	            url: this.superclassurl,
	            root: "superclasses",
	            sortInfo: {
	                field: 'value',
	                direction: "ASC"
	            },
	            fields: [ "value", "description", "classname" ]
	        })
	    });
	    
	    this.className = new Ext.form.TextField({
	    	fieldLabel : this.translation.name,
	    	name : 'name',
	    	width : 220,
	    	allowBlank : false,
	    	disabled : true,
	    	vtype: 'alphanum',
	    	CMDBuildReadonly: true
	    });
	      
	    this.classDescription = new Ext.form.TextField({
	    	fieldLabel : this.translation.description,
	    	width : 220,
	    	name : 'description',
	    	allowBlank : false,
	    	disabled : true,
	    	vtype: 'cmdbcomment'
	    });
		
		this.printClass = new CMDBuild.PrintMenuButton({      		
			text : this.translation.print_class,
			callback: this.onPrintClass,
			formatList: ['pdf', 'odt'],
			scope: this,
			disabled: true
		});
	    
		this.isSuperClass = new Ext.ux.form.XCheckbox({
      		fieldLabel : this.translation.superclass,
            name : 'superclass',
            disabled : true,
            CMDBuildReadonly: true,
            cmdb_hideWithContainer: true
      	});
		
		var formItems = [{
	            name : 'idClass',
	            xtype : 'hidden',
	            value : -1
	      	},
	      	this.className,
	      	this.classDescription
	    ];
      	
      	if (this.eventtype == "class") {
		    this.typeCombo = new Ext.form.ComboBox( {
	            fieldLabel: this.translation.type,
	            name: 'tableType',
	            hiddenName: 'tableType',
	            width: 220,
	            triggerAction: 'all',
	            valueField: 'value',
	            displayField: 'name',
	            minChars: 0,
	            editable: false,
	            disabled: true,
	            mode: "local",
	            CMDBuildReadonly: true,
	            store: new Ext.data.SimpleStore( {
	                fields: [ "value", "name" ],
	                data: [ [ "standard", this.translation.standard ],
	                        [ CMDBuild.Constants.cachedTableType.simpletable, this.translation.simple ] ]
	            })
	        });
		    formItems.push(this.typeCombo);
	    } else {
	    	this.typeCombo = {
	    			setValue: function(){}
	    	};
	    }
      	
      	formItems = formItems.concat([
	      	this.inheriteCombo,
	      	this.isSuperClass, 
	      	{
	            xtype : 'xcheckbox',
	            fieldLabel : this.translation.active,
	            name : 'active',
	            checked : true,
	            disabled : true 
	      	}
	    ]);
		
		this.form = new Ext.form.FormPanel({
			frame: true,
			border: true,
			plugins: [new CMDBuild.CallbackPlugin()],
			style: {background: this.colorsConst.background, padding: '5px'},
			layout: 'form',			
			defaultType : 'textfield',
			monitorValid: true,   
			autoScroll: true,
	      	items: formItems     	
		});
		
	    Ext.apply(this, {
	      title: this.translation.title_add,
	      labelWidth : 75,
	      border : false,
	      frame: false,
	      style: {background: this.colorsConst.background, 'border-top': '1px '+this.colorsConst.border+' solid'},
	      defaultType : 'textfield',
	      tbar : [this.modifyAction, this.deleteAction, this.printClass],
	      items : [this.form],
	      buttonAlign: 'center',
	      buttons : [this.saveButton, this.abortButton]
	    });
	 
	    CMDBuild.Administration.ClassForm.superclass.initComponent.apply(this, arguments);
	    
	    this.subscribe('cmdb-init-'+this.eventtype, this.loadData, this);
	    this.subscribe("cmdb-addclassAction", this.onNewClass, this);
	    
	    this.form.on('clientvalidation', function(form, valid){
			this.saveButton.setDisabled(!(valid && this.saveButton.formBind));
		}, this);
		
		this.className.on('change', function(fieldName, newValue, oldValue) {
			this.autoComplete(this.classDescription, newValue, oldValue);
		}, this);
	    
	    this.typeCombo.setValue = this.typeCombo.setValue.createInterceptor(function(value) {
			if (value == CMDBuild.Constants.cachedTableType.simpletable) {
				this.inheriteCombo.hide();
				this.isSuperClass.hide();
			} else {
				this.inheriteCombo.show();
				this.isSuperClass.show();
			}
		}, this);
	},
	
	//private
	getForm: function() {
		return this.form.getForm();
	},
   
	//private
	loadData: function(params) {		
		this.form.setFieldsDisabled();
        this.getForm().reset();
        if (!params.idClass) {
            return;
        }
        this.idClass = params.idClass;
        if (params.cachedNode) {
        	this.cachedNode = params.cachedNode;
	        var recordTemplate = Ext.data.Record.create ([
				{name: "idClass", mapping: "idClass"},
				{name: "tableType", mapping: "tableType"},
				{name: "description", mapping: "description"},
				{name: "name", mapping: "name"},
				{name: "superclass", mapping: "superclass"},
				{name: "inherits", mapping: "inherits"},
				{name: "active", mapping: "active"}
			]);
	        var attr = params.cachedNode
	        var rec = new recordTemplate({
	        	idClass: attr.id,
	        	tableType: attr.tableType,
				description: attr.text,
				name: attr.name,
				superclass: attr.superclass,
				inherits: attr.parent,
				active: attr.active
			});
	        this.form.getForm().loadRecord(rec);	        
        } else {
        	this.cachedNode = undefined;
        }
        
        this.modifyAction.enable();
        this.deleteAction.enable();
        this.printClass.enable();
	}, 
  
	onNewClass: function(params) {
		this.idClass = -1;
		this.cachedNode = undefined;
		this.getForm().reset();
    	this.modifyAction.setDisabled(true);
    	this.deleteAction.setDisabled(true);
    	this.saveButton.formBind = true;
    	this.abortButton.enable();
    	this.printClass.disable();
    	
    	this.typeCombo.setValue("standard");
    	//set class as default of inheritCombo
    	(function(combo) {
    		var store = combo.store;
    		var i = store.findExact("classname", combo.defaultParent);
    		if (i >= 0) {
    			var record = store.getAt(i);
    			var idClass = record.data.value;
    			combo.setValue(idClass);
    		}
    	})(this.inheriteCombo);
    	this.form.setFieldsEnabled(true);
	},
	
	//private
	onSave: function() {
		CMDBuild.LoadMask.get().show();
		var isprocess = this.eventtype != 'class';
		this.getForm().submit({
			method : 'POST',
			url : CMDBuild.ServiceProxy.administration.saveTable,
			params: {isprocess: isprocess},	
			scope: this,
			success : function(form, action) {
				CMDBuild.LoadMask.get().hide();
				form.findField('inherits').store.reload();
				var table = action.result.table;
				
				var cachedTable = CMDBuild.Cache.getTableById(table.id);
				if (cachedTable) {
					this.publish("cmdb-modify-node", table);
				} else {
					this.publish("cmdb-new-node", table);
				}
				
				this.form.setFieldsDisabled();
				this.onAbort();
				this.inheriteCombo.store.reload();
			},
			failure : function(form, action) {
				CMDBuild.LoadMask.get().hide();
				this.modifyAction.disable();
				this.deleteAction.disable();
				this.publish('cmdb-abortmodify-'+this.eventtype, {idClass: this.idClass});
			}
		});
	},
	
	//private
	onAbort: function() {
	  	var id = this.getForm().findField('idClass').value;
    	if (id > 0) {
    		this.modifyAction.setDisabled(false);
 			this.deleteAction.setDisabled(false);
 			this.printClass.setDisabled(false);
 			this.loadData({
 				idClass:id,
 				cachedNode: this.cachedNode
 			});
 		} else {
 			this.getForm().reset();
 		}
    	this.form.setFieldsDisabled();
    	this.saveButton.formBind = false;
    	this.saveButton.disable();
    	this.abortButton.disable();
    	
 		this.publish('cmdb-abortmodify-'+this.eventtype, {idClass: this.idClass});
	},
	  
	//private
	onModify:  function(evt) {
		this.form.setFieldsEnabled();
		this.modifyAction.disable();
		this.deleteAction.disable();
		this.printClass.disable();
		this.saveButton.formBind = true;
		this.abortButton.enable();
	},
  
	//private
	onDelete: function() {
		Ext.Msg.show({
			title: this.translation.remove_class,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: {
				yes: true,
				no: true
			},
			fn: function(button){
				if (button == 'yes'){
					var id = this.getForm().findField('idClass').value;						
					this.deleteClass(id);
				}
			}	
		});
	},
  
	//private
	deleteClass: function(idClass) {
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			url : CMDBuild.ServiceProxy.administration.deleteTable,
			params:{idClass: idClass},			
			method : 'POST',
			scope : this,
			success : function(response) {
				CMDBuild.LoadMask.get().hide();
				this.getForm().reset();
				this.modifyAction.disable();
				this.deleteAction.disable();
				this.printClass.disable();
				
				this.saveButton.disable(),
				this.abortButton.disable();
				
				CMDBuild.log.info('publish cmdb-deleted-'+this.eventtype, idClass);
				this.publish('cmdb-deleted-node', {
					id:idClass
				});
				this.inheriteCombo.store.reload();
			},
			failure: function(response) {			
				CMDBuild.LoadMask.get().hide();
			}
		});
	},
  
	//private
	onPrintClass: function(format) {
	  CMDBuild.LoadMask.get().show();
	  CMDBuild.Ajax.request({
		  url : CMDBuild.ServiceProxy.administration.printSchema,
		  method : 'POST',
		  scope : this,
		  params: {
		  	idClass: this.idClass,
		  	format: format
	  	  },
	  	  success: function(response) {
	  		  CMDBuild.LoadMask.get().hide();
	  		  var popup = window.open("services/json/management/modreport/printreportfactory", "Report", "height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable");
	  		  if (!popup) {
	  			  CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
	  		  }
	  	  },
	  	  failure: function(response) {			
	  		  CMDBuild.LoadMask.get().hide();
	  	  }
	  });
  }
});
Ext.reg('classform', CMDBuild.Administration.ClassForm );