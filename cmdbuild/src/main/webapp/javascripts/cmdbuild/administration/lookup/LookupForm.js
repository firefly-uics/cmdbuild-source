CMDBuild.Administration.LookupForm = Ext.extend(Ext.Panel, {
  translation: CMDBuild.Translation.administration.modLookup.lookupForm,
  layout: 'border',
  
  initComponent:function() {
  	    
   	this.modifyAction = new Ext.Action({	
    	iconCls : 'modify',
    	text : this.translation.update_lookup,
    	handler: this.onModifyAction,
    	scope : this
    });
    	
   	this.disabelAction = new Ext.Action({	
    	iconCls : 'delete',
    	text : this.translation.disable_lookup,
    	handler: this.onDisableEnableAction, 
    	scope : this
    });
		
    this.parentStore = new Ext.data.JsonStore({
    	autoLoad:true,
    	url : 'services/json/schema/modlookup/getparentlist',
    	root : "rows",
    	sortInfo : {
    		field : 'ParentDescription',
 			direction : "ASC"
  		},
		fields : ['ParentDescription', 'ParentId']
	 });
    
    this.formPanel = new CMDBuild.ExtendedFormPanel({
    	plugins: [new CMDBuild.CallbackPlugin(), new CMDBuild.FormPlugin()],
    	tbarActions: [this.modifyAction, this.disabelAction],
    	onSave: this.onSave,
    	onAbort: this.onAbort,
    	region: 'center',
    	ownerPanel: this,
    	labelWidth: 150,
    	fields: [{
			xtype: 'hidden',
			name: 'Type'
      	  },{
			xtype: 'hidden',
			name: 'Id'
      	  },{
              xtype:'textfield',
              fieldLabel : this.translation.code,
              name : 'Code',
              width : 200,
              disabled : true 
          },{
      		xtype:'textfield',
            fieldLabel : this.translation.description,
            name : 'Description',
            width : 300,
            allowBlank : false,
            disabled : true 
          },{
            xtype : 'combo',
            fieldLabel : this.translation.parentdescription,
            name : 'ParentDescription',
            hiddenName : 'ParentId',
            width : 300,
            valueField : 'ParentId',
            displayField : 'ParentDescription',
            minChars : 0,
            disabled : true,
            store : this.parentStore
          },{
          	xtype : 'textarea',
            fieldLabel : this.translation.notes,
            name : 'Notes',
            width : 300,
            disabled : true 
          },{
            xtype : 'xcheckbox',
            fieldLabel : this.translation.active,
            name : 'Active',
            id: 'Active',
            checked : true,
            disabled : true 
          }]
    	});

		Ext.apply(this, {      
			border : false,
			frame: false,      
			defaultType : 'textfield',
			monitorValid : false,
			style: {'border-top': '1px '+ CMDBuild.Constants.colors.gray.border +' solid'},    
			items : [this.formPanel]
		});
 
	    CMDBuild.Administration.LookupForm.superclass.initComponent.apply(this, arguments);
	    this.formPanel.disableAllActions();
    
	    this.subscribe('cmdb-init-lookup', this.initForm , this);
	    this.subscribe('cmdb-load-lookup', this.loadRecord, this);
	    this.subscribe('cmdb-new-lookup',  this.onNewLookup, this);
  	},  

	//private
	initForm: function(params) {
		this.formPanel.abortModification();
		this.formPanel.disableAllActions();
		this.setType(params);
	},
	
	//private
	onModifyAction: function() {	  
		this.formPanel.modifyForm();
	},

  	//private
	// scope: formPanel
	onSave: function(){
		CMDBuild.LoadMask.get().show();
		this.getForm().submit({	
			method : 'POST',
			url : 'services/json/schema/modlookup/savelookup',		 
			scope: this.ownerPanel,
			success : function() {
				this.publish('cmdb-modified-lookup', {lookupType: this.type});
	  	  	},
			failure : function() {
				this.loadRecord({record:this.record});
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
				this.formPanel.stopMonitoring();
				this.formPanel.disableAllFields();				
				this.formPanel.disableButtons();

				this.formPanel.clearForm();
			}
	  	});
	},
  
	//private
	// scope: formPanel
	onAbort: function() {
		this.abortModification();
		if (!this.getForm().findField('Id').value) {
			this.disableAllActions();
		}
		this.getForm().reset();
	},
  
	//private  
	onDisableEnableAction: function(){
		var url = 'services/json/schema/modlookup/enablelookup';
		if (this.formPanel.getForm().findField('Active').getValue()) {
			url = 'services/json/schema/modlookup/disablelookup';	  
		}
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			url : url,
			params: {Id: this.formPanel.getForm().findField('Id').value},		
			method : 'POST',
			scope : this,
			success : function(response) {
    			this.publish('cmdb-modified-lookup', {lookupType: this.type});
			},
			callback : function() {	
				CMDBuild.LoadMask.get().hide();
			}
		});
	},
  
  	//private
	onNewLookup: function() {
		this.formPanel.newForm(true);
		this.formPanel.clearForm();
		if (this.type) {
			this.formPanel.getForm().findField('Type').setValue(this.type);
		} else {
			throw new Error('Is setted an undefined time in Lookup form')
		}
		this.formPanel.getForm().findField('Active').setValue(true);
	},
  	
  	//private
	loadRecord: function(params) {
		//if call loadRecord without params releoad the current record
		//is the case of abort modify/add lookup item
		this.formPanel.clearForm();
		this.reloadParentStore();
		if (params && params.record) {
			this.record = params.record;
			this.formPanel.getForm().loadRecord(this.record);
		}
				
		this.formPanel.stopMonitoring();
		this.formPanel.disableAllFields();				
		this.formPanel.disableButtons();

		this.formPanel.enableAllActions();
		this.updateDisableEnableLookup();
	},
  	
  	//private
  	reloadParentStore: function() {
  		CMDBuild.log.info('*****type',this.type)
  		if (this.type) {
  			this.parentStore.baseParams.type = this.type;
  			this.parentStore.load();
  		} else {
  			throw new Error('Reload Parent store with no type in LookupForm')
  		}
  	},
  	
  	//private
	updateDisableEnableLookup: function() {
		if (this.record.data['Active']) {
			this.disabelAction.setText(this.translation.disable_lookup);
			this.disabelAction.setIconClass('delete');
		} else {
			this.disabelAction.setText(this.translation.enable_lookup);
			this.disabelAction.setIconClass('ok');
		}	  
	},
  
  	//private
	setType: function(params) {
		if (params.lookupType) {
			this.type = params.lookupType;
		}
		this.reloadParentStore();
		this.formPanel.clearForm();
	}
});
Ext.reg('lookupform', CMDBuild.Administration.LookupForm);