CMDBuild.Administration.AttributeForm = Ext.extend(Ext.Panel, {
  translation: CMDBuild.Translation.administration.modClass.attributeProperties,
  id: "attrbuteform",
  layout: "border",
  eventtype : "class",
  hideNotNull: false, // for processes

  initComponent:function() {

   	this.modifyAction = new Ext.Action({	
    	iconCls : "modify",
    	text : this.translation.modify_attribute,
    	handler: this.onModifyAction,
    	scope : this
    });
	
   	this.deleteAction = new Ext.Action({	
    	iconCls : "delete",
    	text : this.translation.delete_attribute,
    	handler: this.onDeleteAction, 
    	scope : this
    });
   	
   	this.fieldMode = new Ext.form.ComboBox({ 
		name: "fieldmode",
		fieldLabel: this.translation.field_visibility,
		valueField: "value",
		displayField: "name",
		hiddenName: "fieldmode",
		mode: "local",
		triggerAction: "all",
		editable: false,
		allowBlank: false,
		disabled: true,
		store: new Ext.data.SimpleStore({
			fields: ["value","name"],
			data : [["write",this.translation.field_write],
			        ["read",this.translation.field_read],
			        ["hidden",this.translation.field_hidden]]
		})
	});
   	
   	this.attributeGroup = new Ext.form.ComboBox({ 
		name: "group",
		fieldLabel: this.translation.group,
		valueField: "value",
		displayField: "value",
		hiddenName: "group",
		mode: "local",
		triggerAction: "all",
		editable: true,
		allowBlank: true,
		disabled: true,
		store: new Ext.data.SimpleStore({
			fields: ["value"],
			data : []
		}),
		listeners: {
   			scope: this,
   			"enable": function(combo) {
   				var idClass = this.idClass;
   				var store = combo.store;
   				var cb = function(attributes) {
   					store.removeAll();
   					var addtributesGroup = {};
   					for (var i=0, len=attributes.length; i<len; ++i) {
   						var  attribute = attributes[i];
   						if (attribute.group) {
   							addtributesGroup[attribute.group] = true;
   						};
   					}
   					var groups = [];
   					for (var g in addtributesGroup) {
   						groups.push([g]);
   					}
   					store.loadData(groups);
   				};
   				CMDBuild.Cache.getAttributeList(idClass, cb);
   			}
	    }
	});
   	
   	this.attributeName = new Ext.form.TextField({
   		fieldLabel : this.translation.name,
        name : "name",
        width : 220,
        disabled : true,
        allowBlank : false,
        vtype: "alphanum",
        CMDBuildReadonly: true
	});
   	
   	this.attributeDescription = new Ext.form.TextField({
   		fieldLabel : this.translation.description,
        width : 220,
        name : "description",
        disabled : true,
        allowBlank : false,
        vtype: "cmdbcomment"
	});

   	var attributeNotNull = new Ext.ux.form.XCheckbox({
	        fieldLabel : this.translation.isnotnull,
	        name : "isnotnull",
	        disabled : true
		});
   	this.attributeNotNull = attributeNotNull;

   	var attributeUnique = new Ext.ux.form.XCheckbox({
	        fieldLabel : this.translation.isunique,
	        name : "isunique",
	        disabled : true
		});
   	this.attributeUnique = attributeUnique;

   	var basePropertiesFields = [
		this.attributeName,
		this.attributeDescription,
		this.attributeGroup,
		//waiting for server side implementation
		/*{
	        xtype : "textfield",
	        fieldLabel : this.translation.isdefault,
	        name : "defaultvalue",
	        disabled : true
		},*/
		
		{
	        xtype : "xcheckbox",
	        fieldLabel : this.translation.isbasedsp,
	        name : "isbasedsp",
	        disabled : true
		},
			attributeUnique
		];
   	if (!this.hideNotNull) {
   		basePropertiesFields = basePropertiesFields.concat([
				attributeNotNull
			]);
   	}
   	basePropertiesFields = basePropertiesFields.concat([{
	        xtype : "xcheckbox",
	        fieldLabel : this.translation.isactive,
	        name : "isactive",
	        disabled : true
		},{
	        xtype : "hidden",
	        name : "meta"
		},
		this.fieldMode]);

   	this.baseProperties = new Ext.form.FieldSet({   		
        title: this.translation.baseProperties,
        autoHeight:true,
        autoScroll: true,
        defaultType: "textfield",        
        region: "west",
        width: "50%",
        split: true,
        labelWidth: 150,
   		items: basePropertiesFields
   	});
    
    this.comboType = new Ext.form.ComboBox({
        fieldLabel : this.translation.type,
        name : "type_value",
        hiddenName : "type",
        width : 220,
        triggerAction : "all",
        valueField : "name",
        displayField : "value",
        minChars : 0,
        allowBlank : false,
        editable: false,
        CMDBuildReadonly: true,
        store : new Ext.data.JsonStore({
  		  autoLoad : false,
  	      url : "services/json/schema/modclass/getattributetypes",
  	      root : "types",
  	      sortInfo : {
  		      field : "value",
  		      direction : "ASC"
  		  },
  		  fields : ["name", "value"]
  		}),
  		disabled : true
  	  });      
	
	this.stringLength = new  Ext.form.NumberField({
		fieldLabel : this.translation.length,
	    minValue: 1,
	    maxValue: Math.pow(2,31)-1,
	    name : "len",
	    allowBlank : false,
	    disabled : true
	});
	
	this.decimalPrecision = new Ext.form.NumberField({      
        fieldLabel : this.translation.precision,
        minValue: 1,
        maxValue: 20,
        name : "precision",
        allowBlank : false,
        disabled : true
	});

   	this.fieldFilter = new Ext.form.TextArea({
   		fieldLabel : this.translation.referencequery,
        name : "fieldFilter",
        width: 200,
        allowBlank : true,
        disabled : true,
        vtype: "cmdbcommentrelaxed",
        invalidText: this.translation.pipeNotAllowed,
        editableOnInherited: true
	});

   	this.addMetadataBtn = new Ext.Button({
   		text: this.translation.meta.title,
   		scope: this,
   		iconCls: "modify",
   		handler: function() {
   			CMDBuild.log.debug("decord", this.record);
   			new CMDBuild.Administration.MetadataWindow({
   				meta: this.record.data.meta,
   				ns: "system.template.",
   				owner: this
   			}).show();
   		}
   	});
   	
	this.decimalScale = new Ext.form.NumberField({
        fieldLabel : this.translation.scale,
        minValue: 1,
        maxValue: 20,
        name : "scale",
        allowBlank : false,
        disabled : true
	});
	
	this.lookupTypes = new Ext.form.ComboBox( {
        plugins: new CMDBuild.SetValueOnLoadPlugin(),
        fieldLabel: this.translation.lookup,
        name: "lookup",
        triggerAction: "all",
        valueField: "type",
        displayField: "type",
        allowBlank: false,
        CMDBuildReadonly: true,
        store: CMDBuild.Cache.getLookupTypeLeavesAsStore(),
        mode: "local",
        disabled: true
    });     
	
	this.referenceDomains = new Ext.form.ComboBox({
        xtype:"combo",
        plugins: new CMDBuild.SetValueOnLoadPlugin(),
        fieldLabel : this.translation.domain,
        name : "idDomain",
        hiddenName : "idDomain",
        valueField : "idDomain",
        displayField : "name",
        triggerAction : "all",
        minChars : 0,
        editable: false,
        allowBlank: false,
        CMDBuildReadonly: true,
        store: new Ext.data.JsonStore({
        	autoLoad: false,
        	url : "services/json/schema/modclass/getreferenceabledomainlist",
        	root : "rows",
        	sortInfo : {
        		field : "name",
        		direction : "ASC"
        	},
        	fields : ["idDomain", "name"]
        }),
        disabled : true
	});
	
	this.foreignKeyDest = new Ext.form.ComboBox({
        xtype:"combo",
        plugins: new CMDBuild.SetValueOnLoadPlugin(),
        fieldLabel : this.translation.destination,
        name : "fkDestination",
        hiddenName : "fkDestination",
        valueField : "id",
        displayField : "description",
        triggerAction : "all",
        minChars : 0,
        editable: false,
        allowBlank: false,
        CMDBuildReadonly: true,
        mode: "local",
        store: CMDBuild.Cache.getClassesAndProcessAsStore(),
        disabled : true
	});
	
	
	this.contextualFields = {
         STRING: [this.stringLength],
         DECIMAL: [this.decimalPrecision,this.decimalScale],
         LOOKUP: [this.lookupTypes],
         FOREIGNKEY: [this.foreignKeyDest],
         REFERENCE: [this.referenceDomains, this.fieldFilter, this.addMetadataBtn]
	};
	
	this.specificProperties = new Ext.form.FieldSet({
        title: this.translation.typeProperties,
        autoHeight:true,
        autoScroll: true,
        region: "center",
        defaultType: "textfield",
        labelWidth: 130,
        items: [this.comboType,
           this.stringLength,
           this.decimalPrecision,
           this.decimalScale,
           this.lookupTypes,
           this.referenceDomains,
           this.foreignKeyDest,
           this.fieldFilter,
           this.addMetadataBtn]
	});
	
	this.reader = new Ext.data.JsonReader(
		 { totalProperty : "results", root : "rows"},
		 ["name", "description", "type", "isbasedsp", "isunique", "isnotnull"]
	);
	
    this.formPanel = new CMDBuild.ExtendedFormPanel({
    	plugins: [new CMDBuild.CallbackPlugin(), new CMDBuild.FormPlugin()],
    	tbarActions: [this.modifyAction, this.deleteAction],
    	onSave: this.onSave,
    	onAbort: this.onAbort,
    	region: "center",
    	ownerPanel: this,
    	panelLayout: "border",
    	reader: this.reader,
    	frame: false,
    	border: false,
    	fields: [this.baseProperties,this.specificProperties]
    });

    Ext.apply(this, {
      labelWidth : 75,
      border : false,
      frame: false,      
      defaultType : "textfield",
      monitorValid : false,      
      items : [this.formPanel]
    });
 
    CMDBuild.Administration.AttributeForm.superclass.initComponent.apply(this, arguments);
    this.formPanel.disableAllActions();
    this.formPanel.on({
      afterlayout:{scope:this, single:true, fn:this.hideContextualFields}
    });
    this.comboType.on("select", this.onSelectComboType, this);
    this.subscribe("cmdb-init-"+this.eventtype, this.onInit , this);
    this.subscribe("cmdb-load-"+this.eventtype+"attribute", this.onLoadAttribute, this);
    this.subscribe("cmdb-new-"+this.eventtype+"attribute",  this.onNewAttribute, this);
    this.subscribe("cmdb-modified-"+this.eventtype+"domain", function() {
    	this.referenceDomains.store.load();
    }, this);
    
    this.attributeName.on("change", function(fieldname, newValue, oldValue) {
		this.formPanel.autoComplete(this.attributeDescription, newValue, oldValue);
	}, this);
  },  

  onInit: function(params) {	  
	  if (params.idClass) {
		this.idClass=params.idClass;
		this.cachedTable = params.cachedNode;
	  }
	  
	  if (params.cachedNode.tableType != CMDBuild.Constants.cachedTableType.simpletable) {
		  this.referenceDomains.store.baseParams = {idClass: this.idClass};
		  this.referenceDomains.store.load();
	  }
	  
	  this.comboType.store.baseParams = {idClass: this.idClass};
	  this.comboType.store.load();
	  
	  var isSuperClass = this.isSuperclass();
	  this.attributeUnique.initialConfig.CMDBuildReadonly = isSuperClass;
	  this.attributeNotNull.initialConfig.CMDBuildReadonly = isSuperClass;
	  this.formPanel.initForm();
  },
  
  isSuperclass: function() {
	  if (this.cachedTable) {
		  return this.cachedTable.superclass;
	  } else {
		  return false;
	  }
  },

  onLoadAttribute: function(params) {
	  this.formPanel.clearForm();
	  if (params && params.record) {
		  var record = params.record;
		  var  fp = this.formPanel;
		  this.record = record;
		  fp.getForm().loadRecord(record);
		  this.modifyAction.enable();
		  this.deleteAction.setDisabled(record.data.inherited);
		  fp.disableAllFields();
		  fp.disableButtons();
		  fp.stopMonitoring();
		  this.hideContextualFields();
		  this.showContextualFieldsByType(record.data.type);
		  //I want send these value only after a modify in the meta data window
		  this.formPanel.getForm().findField("meta").setValue("");
	  }
  },
  
  onNewAttribute: function(params) {
	  var fp = this.formPanel;
	  fp.clearForm();
	  fp.newForm();
	  if (this.isSuperclass()) {
		  this.attributeUnique.disable();
		  this.attributeNotNull.disable();
	  };
	  fp.getForm().findField("isactive").setValue(true);
	  fp.getForm().findField("fieldmode").setValue("write");
	  this.editMode = "new";
	  this.hideContextualFields();
  },
  
  onModifyAction: function() {	  
	  this.formPanel.modifyForm();
	  this.editMode = "modify";
	  
	  var inherited = this.record.data.inherited;
	  this.iterateOverContextualFields(this.record.data.type, function(field) {
		  field.setDisabled((inherited && !field.editableOnInherited) || field.CMDBuildReadonly);
	  });
	  
	  this.addMetadataBtn.enable(); //by hand because the modifyForm enable only the field
  },
  
  onSave: function(){
	  //enable the type field because is a required parameter
	  this.getForm().findField("type").enable();
	  var name = this.getForm().findField("name").getValue();
	  CMDBuild.LoadMask.get().show();
	  this.getForm().submit({
			method : "POST",
			url : "services/json/schema/modclass/saveattribute",
			params : {
				idClass: this.ownerPanel.idClass,
				name: name
			},			
			scope: this,
			success : function(form, action) {
				CMDBuild.LoadMask.get().hide();
				this.initForm();
				this.clearForm();
				this.ownerPanel.addMetadataBtn.disable();
				this.publish("cmdb-modified-"+this.ownerPanel.eventtype+"attribute", {
					  idClass: this.ownerPanel.idClass
				});
			},
			failure: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
  },
  
  onAbort: function() {	  
	  this.getForm().reset();
	  this.disableAllFields();
	  if (this.ownerPanel.editMode == "modify") {
	  	this.enableAllActions();
	  }	  
	  this.ownerPanel.addMetadataBtn.disable();
	  this.disableButtons();
	  this.stopMonitoring();	  
  },
  
  onDeleteAction: function() {
		Ext.Msg.show({
			title: this.translation.delete_attribute,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: {
				yes: true,
				no: true
			},
			fn: function(button){
				if (button == "yes"){
					this.deleteAttribute();
				}
			}
		});
  },
  
  deleteAttribute: function() {
	  var name = this.formPanel.getForm().findField("name").value;
	  this.formPanel.getForm().findField("isactive").value=false;
	  CMDBuild.LoadMask.get().show();
	  CMDBuild.Ajax.request({
		  url : "services/json/schema/modclass/deleteattribute",
		  method: "POST",
		  params: {
			  idClass: this.idClass,
			  name: name
		  },		  
		  scope: this,
		  callback : function() {
			  CMDBuild.LoadMask.get().hide();
			  this.publish("cmdb-modified-"+this.eventtype+"attribute", {
				  idClass: this.idClass
			  });			  
			  this.formPanel.clearForm();
			  this.deleteAction.disable();
			  this.modifyAction.disable();
		  }
	  });
  },
  
  iterateOverContextualFields: function(type, fn) {
	  var typeFields = this.contextualFields[type];
	  if (typeFields) {
		  for (var i=0, len=typeFields.length; i<len; i++) {
			  fn(typeFields[i]);
		  }
	  }
  },

  showAndEnableContextualFieldsByType: function(type) {
	  this.iterateOverContextualFields(type, function(field) {
		  if (field.showContainer) {
			  field.showContainer();
		  } else {
			  //is not a field and has not the showContainer method
			  field.show();
		  }
		  field.enable();
	  });
  },
  
  hideContextualFields: function() {
	  for (var type in this.contextualFields) {		  
		  this.iterateOverContextualFields(type, function(field) {
			  if (field.hideContainer) {
				  field.hideContainer();
			  } else {
				  field.hide();
			  }
			  field.disable();
		  });
	  }
  },
  
  showContextualFieldsByType: function(type) {
	  this.iterateOverContextualFields(type, function(field) {
		  if (field.showContainer) {
			  field.showContainer();
		  } else {
			  field.show();
		  }
		  //disable because the showContainer() enable the field
		  field.disable();
	  });
  },
  
  onSelectComboType: function(combo, record, index) {
	  var type = record.data.value;
	  this.hideContextualFields();
	  this.showAndEnableContextualFieldsByType(type);
  }
});

Ext.reg("attributeform", CMDBuild.Administration.AttributeForm);