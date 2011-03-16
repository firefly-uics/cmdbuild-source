CMDBuild.Administration.UserForm = Ext.extend(Ext.form.FormPanel, {

  translation: CMDBuild.Translation.administration.modsecurity.user,   
  plugins: [new CMDBuild.CallbackPlugin(), new CMDBuild.FormPlugin()],

  initComponent:function() {
  	  	
   	this.modifyAction = new Ext.Action({	
    	iconCls : 'modify',
    	text : this.translation.modify_user,
    	handler : function() {
       		 	this.setFieldsEnabled();
       		 	this.saveButton.formBind = true;
       		 	this.disableAllFieldsOfFieldset(this.userPassword);
       		 	this.getForm().findField('username').disable();
       		 	this.modifyPassword.setDisabled(true);
       		 	this.modifyAction.setDisabled(true);
       		 	this.disableUser.setDisabled(true);
    	},
    	scope : this,
    	disabled: true
    });
    
   	this.modifyPassword = new Ext.Action({	
    	iconCls : 'password',
    	text : this.translation.change_password,
    	handler : function() {
       		 	this.setFieldsEnabled();
       		 	this.saveButton.formBind = true;
       		 	this.disableAllFieldsOfFieldset(this.userInfo);
       		 	this.modifyAction.setDisabled(true);
       		 	this.modifyPassword.setDisabled(true);
       		 	this.disableUser.setDisabled(true);
    	},
    	scope : this,
    	disabled: true
    });
   	
   	this.disableUser = new Ext.Action({	
    	iconCls : 'delete',
    	text : this.translation.disable_user,
    	handler : this.onDisableUser,
    	scope : this,    	
    	disabled: true
    });

    var userName = new Ext.form.TextField({
        xtype : 'textfield',
        id: 'username',
        fieldLabel : this.translation.username,
        width : 220,
        allowBlank : false,
        name : 'username',
        disabled : true,
        CMDBuildReadonly: true
		});
    var userDescription = new Ext.form.TextField({
	        xtype : 'textfield',
	        fieldLabel : this.translation.description,
	        width : 220,
	        allowBlank : false,
	        name : 'description',
	        disabled : true
		});
    var userEmail = new Ext.form.TextField({
	        xtype : 'textfield',
	        vtype : 'emailOrBlank',
	        fieldLabel : this.translation.email,
	        width : 220,
	        allowBlank : true,
	        name : 'email',
	        disabled : true
		});
    
    this.defaultGroupStore = new Ext.data.JsonStore({
		autoLoad : false,
		url :  'services/json/schema/modsecurity/getusergrouplist',
		root : "result",
		sortInfo : {
			field : 'description',
			direction : "ASC"
		},
		fields : ['id', 'description', 'isdefault']
	});
    
    this.defaultGroup = new Ext.form.ComboBox({
    	name : 'defaultgroup',
    	fieldLabel : this.translation.defaultgroup,
    	hiddenName : 'defaultgroup',
    	triggerAction : 'all',
    	valueField : 'id',
    	displayField : 'description',
    	editable: false,
    	allowBlank : true,
    	disabled : true,
    	lazyInit: false,
    	store : this.defaultGroupStore,
    	mode: 'local',
    	width: 200
    });

   	this.userInfo = new Ext.form.FieldSet({
   		title: this.translation.user_info,
   		region: 'west',
   		margins: '5 0 5 5',
   		split: true,
   		autoHeight:true,
   		items: [
   		     userName,
   		     userDescription,
   		     userEmail,
   		     this.defaultGroup,
   		     {
	   			xtype : 'xcheckbox',
	   			fieldLabel : this.translation.isactive,   			
	   			name : 'isactive',
	   			checked : true,
	   			disabled : true 
	   		}]
   		});
   	
   	this.userPassword = new Ext.form.FieldSet({
   		title: this.translation.password,   		
   		region: 'center',
   		autoHeight:true,
   		autoScroll: true,
   		margins: '5 5 5 5',
   		items: [{
   			xtype : 'textfield',
   			inputType : 'password',
   			id: 'user_password',
   			name : 'password',
   	        fieldLabel : this.translation.password,
   	        width : 220,
   	        allowBlank : false,
   	        disabled : true
   		},{
   			xtype : 'textfield',
   			inputType : 'password',
   	        fieldLabel : this.translation.confirmation,
   	        width : 220,
   	        allowBlank : false,
   	        name : 'confirmation',
   	        disabled : true,
   	        vtype: 'password',
   			initialPassField: 'user_password'
   		}]
   	});

    this.saveButton= new Ext.Button({
      	id: 'saveUserButton',
        text : CMDBuild.Translation.common.buttons.save,
        formBind : false,
        scope : this,
        handler : function() {
          	this.saveUser(); 
         },
        disabled : true
      });
    
      this.abortButton= new Ext.Button({
       id: 'abortUserButton',
       text : CMDBuild.Translation.common.buttons.abort,
       scope : this,
       handler : function() {
    	  this.reset();
    	  this.saveButton.disable()
    	  this.abortButton.disable();
    	  this.setFieldsDisabled();
    	  this.saveButton.formBind = false;
    	  if (this.record) {  
	        	this.getForm().loadRecord(this.record);
	        	this.modifyAction.enable();
	        	this.modifyPassword.enable();
	        	this.disableUser.enable()
	        }
        },
        disabled : true
      });
	
      //vtype for passwords validation
	   Ext.apply(Ext.form.VTypes, {
		    password : function(val, field) {
		        if (field.initialPassField) {
		            var pwd = Ext.getCmp(field.initialPassField);
		            return (val == pwd.getValue());
		        }
		        return true;
		    },
		    passwordText : CMDBuild.Translation.configure.step2.msg.pswnomatch
		});

    Ext.apply(this, {
      labelWidth: 150,
      monitorValid : true,
      layout: 'fit',
	  autoScroll: false,
	  tbar : [this.modifyAction, this.modifyPassword, this.disableUser],
      items: [{
      	xtype: 'panel',      		
		layout: 'border',
		defaultType : 'textfield',    
		autoScroll: true,
		style: {padding: '5px', background: CMDBuild.Constants.colors.gray.background},	
      	items:  [{
	        name : 'userid',
	        xtype : 'hidden',
	        value : -1
   		},
	   		this.userInfo, 
	   		this.userPassword
	   	]
      }],
      buttonAlign: 'center',
      buttons : [this.saveButton, this.abortButton]
    });
 
    CMDBuild.Administration.UserForm.superclass.initComponent.apply(this, arguments);
   
    this.subscribe('cmdb-load-user', this.onLoadUser, this);
    this.subscribe('cmdb-new-user', this.onNewUser, this);

    userName.on('change', function(fieldName, newValue, oldValue) {
		this.autoComplete(userDescription, newValue, oldValue);
	}, this);
  },
  
  reset: function() {
    this.getForm().reset();
    this.setTitle(this.translation.title);
  },

  onLoadUser: function(params) {
  	this.getForm().reset();
  	if (params && params.record) {
  		this.record = params.record;
  		this.loadDefaultGroupStore(params.record.data.userid);
  	}
  },

  loadDefaultGroupStore: function(userid) {
	  this.defaultGroupStore.load({
		  params: { userid: userid },
		  callback: this.onGroupStoreLoad,
		  scope: this
	  });
   },

  	onGroupStoreLoad: function(store, records, options) {
	    var store = this.defaultGroupStore;
		var index = store.find('isdefault', true);
	  	store.loadData({ result: [{
				id: '0',
				description: this.translation.defaultonlogin,
				isdefault: (index < 0)
			}]}, true);
  		var combo = this.defaultGroup;
  		var defaultGroupId = 0;
  		if (index >= 0) {
  			var groupRecord = this.defaultGroupStore.getAt(index);
  			defaultGroupId = groupRecord.data.id;
  		}
  		this.record.data.defaultgroup = defaultGroupId;
  		this.loadCurrentRecord();
	},

  	loadCurrentRecord: function() {
		this.getForm().loadRecord(this.record);
		this.updateDisableActionTextAndIconClass();
		this.setFieldsDisabled();
		this.modifyPassword.enable();
		this.modifyAction.enable();
		this.disableUser.enable();
	},

	saveUser: function() {
		var params = this.getForm().getValues();
	  	// if I'm not passing the "username" and the "description" I'm changing the password
	  	// and i need to know the value of the "isactive" field to not set it to false anyway
	  	if (params.description === undefined && params.username === undefined) {
			params['isactive'] = this.record.data.isactive;
		}
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			method : 'POST',
			url : 'services/json/schema/modsecurity/saveuser',
			params : params,
			scope : this,
			success : function(result, options, decodedResult) {
				var userid = decodedResult.rows.userid;
				this.publish('cmdb-modified-user', {
					userid : userid
				});
				this.setFieldsDisabled();
				this.modifyPassword.setDisabled(false);
				this.modifyAction.setDisabled(false);
				this.disableUser.setDisabled(false);
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	},

	onDisableUser: function() {		
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			method : 'POST',
			url : 'services/json/schema/modsecurity/disableuser',
			params: {
				userid: this.record.data.userid,
				disable: this.record.data.isactive
			},			
			scope: this,
			success : function(result, options, decodedResult) {
		  		var userid = decodedResult.rows.userid;
				this.publish('cmdb-modified-user', {userid: userid});
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
				this.loadRecord(this.record);
			}
		});
	},
  
	onNewUser: function() {
		this.record = undefined;
		this.getForm().findField('userid').setValue(-1);
		this.setFieldsEnabled(true);
		this.saveButton.formBind = true;
		this.modifyAction.disable();
		this.modifyPassword.disable();
		this.disableUser.disable();
		this.defaultGroup.disable();
		this.reset();
	},

  disableAllFieldsOfFieldset: function(fieldset){
	  var items = fieldset.items.items;
	  for (var i = 0, ln = items.length; i< ln; ++i ) {
		  if (items[i])
			  items[i].disable();
	  }
  },
  

  	updateDisableActionTextAndIconClass: function(){	  	
		if (this.record.data.isactive) {
			this.disableUser.setText(this.translation.disable_user);
			this.disableUser.setIconClass('delete');
	    } else {
	    	this.disableUser.setText(this.translation.enable_user);
	    	this.disableUser.setIconClass('ok');
	    }
	}
});

Ext.reg('userform', CMDBuild.Administration.UserForm );