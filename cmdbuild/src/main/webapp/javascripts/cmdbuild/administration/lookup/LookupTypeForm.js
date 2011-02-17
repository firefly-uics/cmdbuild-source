CMDBuild.Administration.LookupTypeForm = Ext.extend(Ext.Panel, {
	translation: CMDBuild.Translation.administration.modLookup.lookupTypeForm,
	layout: 'border',
	
	initComponent:function() {
 		this.modifyAction = new Ext.Action({
		   	iconCls: 'modify',
		   	text: this.translation.modify_lookuptype,
		   	handler: this.onModityAction,
		   	scope : this
		}),
	   
	   this.formPanel = new CMDBuild.ExtendedFormPanel({
		   plugins: [new CMDBuild.CallbackPlugin(), new CMDBuild.FormPlugin()],
		   tbarActions: [this.modifyAction],
		   onSave: this.onSave,
		   onAbort: this.onAbort,
		   region: 'center',
		   ownerPanel: this,
		   fields: [{
				xtype: 'hidden',
				name: 'orig_type'
		   },{
			   xtype: 'textfield',
			   fieldLabel : this.translation.description,
			   name : 'description',
			   width : 220,
			   allowBlank : false,
			   disabled: true
		   },{
			   xtype : 'combo',
			   fieldLabel : this.translation.parent,
			   name : 'parent',
			   width : 220,
			   triggerAction : 'all',
			   valueField : 'type',
			   displayField : 'type',
			   minChars : 0,
			   disabled: true,
			   store : new Ext.data.JsonStore({
				   autoLoad : true,
				   url : 'services/json/schema/modlookup/getlookuptypelist',
				   root : "rows",
				   sortInfo : {
					   field : 'type',
					   direction : "ASC"
				   },
			   fields : ['type']
			   }),
			   CMDBuildReadonly: true
		   }]
	   }),	   
	   Ext.apply(this, {
		   items: [this.formPanel]
	   });
	   CMDBuild.Administration.LookupTypeForm.superclass.initComponent.apply(this, arguments);    
	   this.subscribe('cmdb-init-lookup', this.initLookup, this);
	   this.subscribe('cmdb-addlookuptype-action', this.newLookup, this);
  },
  
  onModityAction: function(evt) {
	  this.formPanel.modifyForm();
  },
	
  onAbort: function() {
	  //the scope is the FormPanel
	  this.getForm().reset();	  
	  this.abortModification();
	  if (this.getForm().findField('orig_type').value == '') {
		  //abort after add Lookuptype
		  this.publish('cmdb-abort-newltype');
		  this.disableAllActions();
	  }
  },
  
  onSave: function(){
  	CMDBuild.LoadMask.get().show();
	  this.getForm().submit({
		method : 'POST',
		url : 'services/json/schema/modlookup/savelookuptype',		
		scope: this,
		params: {
		  orig_type: this.getForm().findField('orig_type').value
	  	},
	  	success: this.ownerPanel.onSuccess,
		callback: function() {
			CMDBuild.LoadMask.get().hide();
			this.disableAllFields();
			this.stopMonitoring();
			this.enableAllActions();
			this.disableButtons();
		}
	  });
  },
  
  onSuccess: function(form, action) {
	    var lType = action.result.lookup;
	    form.findField('parent').store.reload();
	
	    var cachedLType = CMDBuild.Cache.getTableById(lType.id);
	    if (action.result.isNew) {
	        this.publish("cmdb-new-node", lType);
	    } else {
	        CMDBuild.Cache.syncLookupTypesParent(lType);
	        this.publish("cmdb-modify-node", lType);
	    }
	    //need to manage the reload after abort	
	    this.getForm().findField('orig_type').setValue(lType.description);
	    this.disableButtons();
	},
    
  initLookup: function(params) {
	this.formPanel.initForm();
	var lookupType = CMDBuild.Cache.getTableById(params.lookupType);
	if (lookupType) {
		var recordTemplate = Ext.data.Record.create ([
			{name: "orig_type", mapping: "orig_type"},
			{name: "description", mapping: "description"},
			{name: "parent", mapping: "parent"}
		]);
		var rec = new recordTemplate({
			orig_type: lookupType.id,
			description: lookupType.text,
			parent: lookupType.parent
		});
		this.formPanel.loadRecord(rec);
	}
	this.formPanel.enableAllActions();
	this.formPanel.disableAllFields();
  },
  
  newLookup: function(params) {
	  this.formPanel.clearForm();	  
	  this.formPanel.newForm();
	  this.formPanel.getForm().findField('orig_type').setValue('');
  }
});
Ext.reg('lookuptypeform', CMDBuild.Administration.LookupTypeForm);