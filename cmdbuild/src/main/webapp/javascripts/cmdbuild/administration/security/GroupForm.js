CMDBuild.Administration.GroupForm = Ext.extend(Ext.form.FormPanel, {
  translation: CMDBuild.Translation.administration.modsecurity.group,
  plugins: [new CMDBuild.CallbackPlugin(), new CMDBuild.FormPlugin()],
  id: 'groupform',
  currentSelection: undefined,
  
  initComponent:function() {
     this.groupId= -1,
  
     this.enableDisableGroupBtn = new Ext.Action({
        iconCls: 'delete',
        text: this.translation.delete_group,
        handler: this.onEnableDisableGroup,
        scope : this
    });
   
    this.modifyAction = new Ext.Action({
        iconCls: 'modify',
        text: this.translation.modify_group,
        handler: this.onModify,
        scope : this
    });
    
    this.saveButton = new CMDBuild.buttons.SaveButton({
        id: 'saveGroupButton',
        name: 'saveGroupButton',
        formBind : true,
        scope: this,
        handler: this.onSave,
        disabled: true
    });
      
	this.abortButton = new CMDBuild.buttons.AbortButton({
		id: 'abortGroupButton',
		name: 'abortGroupButton',
		scope : this,
		handler : this.onAbort,
		disabled: true
	}); 
    
	var groupName = new Ext.form.TextField({
        fieldLabel : this.translation.group_name,
        name : 'name',
        width : 220,
        allowBlank : false,
        vtype: 'alphanum',
        disabled : true,
        CMDBuildReadonly: true
	});
    
	var groupDescription = new Ext.form.TextField({
        fieldLabel : this.translation.group_description,
        name : 'description',
        width : 220,
        allowBlank : false,
        disabled : true
	});
    
	var groupEmail = new Ext.form.TextField({
        vtype : 'emailOrBlank',
        fieldLabel : this.translation.email,
        width : 220,	        
        name : 'email',
        disabled : true,
        allowBlank : true
	});
	
	this.barButtons = [this.modifyAction, this.enableDisableGroupBtn ];

	var groupStore = CMDBuild.Cache.getClassesAndProcessAsStoreWithEmptyOption();
	
	this.modulesCheckInput = (function readModulesFromStructure() {
		var map = {};
		var array = [];
		var structure = CMDBuild.Structure;			
		for (var tree in structure) {
			var m = structure[tree];
			var ckeckBox;
			if (m.submodules) {
				for (var sub in m.submodules) {
					checkBox = new Ext.ux.form.XCheckbox({
						 fieldLabel: '',
						 labelSeparator: '',
						 boxLabel: m.title+" - "+m.submodules[sub].title,
						 name: 'modules',
						 module: sub
					});
					array.push(checkBox);
					map[sub] = checkBox;
				}
			} else {
				if (m.title) {
					checkBox = new Ext.ux.form.XCheckbox({
						 fieldLabel: '',
						 labelSeparator: '',
						 boxLabel: m.title,
						 name: 'modules',
						 module: tree
					});
					array.push(checkBox);
					map[tree] = checkBox;
				}
			}
		}
		return {
			toArray: array,
			toMap: map
		};
	})();
	
	var _this = this;
	var container = new Ext.Panel({
      	frame: true,
      	style: {padding: '5px', background: CMDBuild.Constants.colors.gray.background},
		border: false,
		defaultType : 'textfield',    
		autoScroll: true,
		labelWidth : 200,
		layout: 'hbox',
		layoutConfig: {           
            align:'top'
        },
      	items: [{
      		xtype: 'panel',
      		layout: 'form',
      		margins: '6 0 0 5',
      		items: [{
	            name : 'id',
	            xtype : 'hidden',
	            value : -1
	          },
	          groupName,
	          groupDescription,
	          groupEmail,
	          {
	            xtype : 'xcheckbox',
	            fieldLabel : this.translation.is_administrator,
	            name : 'isAdministrator',
	            disabled : true
	          },{
	            xtype : 'combo',
	            fieldLabel : this.translation.starting_class,
	            name : 'startingClass_value',
	            hiddenName : 'startingClass',
	            width : 220,
	            triggerAction : 'all',
	            valueField : 'id',
	            displayField : 'description',
	            minChars : 0,
	            editable: false,
	            disabled : true,
	            store : groupStore,
	            mode: 'local'
	          },{
	            xtype : 'xcheckbox',
	            fieldLabel : this.translation.is_active,
	            name : 'isActive',
	            checked : true,
	            disabled : true 
	          }]
      	}, {
			xtype: 'fieldset',
			title: this.translation.disabled_modules,
			labelWidth : 1,
			defaultType: 'xcheckbox',
			margins: '0 0 0 15',
			items: _this.modulesCheckInput.toArray
		}]
	});
	
	Ext.apply(this, {
		title: this.translation.title_add,
		layout: 'fit',
		monitorValid : true,
		tbar : this.barButtons,
		items : container,
		buttonAlign: 'center',
		buttons : [this.saveButton, this.abortButton]
    });
 
    CMDBuild.Administration.GroupForm.superclass.initComponent.apply(this, arguments);
    
    this.subscribe('cmdb-init-group', this.loadData, this);    
    this.getForm().on('actioncomplete', function(form, action) {    	
    	if (action.type == "load") {
    		this.updateEnableDisableGroupLabelIcon();
    	}
    }, this);

    groupName.on('change', function(fieldName, newValue, oldValue) {
		this.autoComplete(groupDescription, newValue, oldValue);
	}, this);
  },
  
  loadData : function(params) {
		this.getForm().reset();		
		if (params.groupId) {
			this.groupId = params.groupId;
		} else {
			this.groupId = -1;
		}

		if (this.groupId > 0) {
			this.enableBarButtons(this.barButtons);
			this.setFieldsDisabled();
			CMDBuild.LoadMask.get().show();
			this.getForm().load({
				url : 'services/json/schema/modsecurity/getgroup',
				params : {
					groupId : this.groupId
				},
				scope: this,
				success : function(form, res) {
					var isAdministrator = res.result.data.isAdministrator;
					if (isAdministrator) {
						// really ugly
						Ext.ComponentMgr.get('privilege_panel').disable();
					}
					CMDBuild.LoadMask.get().hide();
		
					var modulesToDisable = res.result.data.disabledModules || [];
					for ( var i = 0, len = modulesToDisable.length; i < len; ++i) {
						var moduleName = modulesToDisable[i];
						var moduleCheck = this.modulesCheckInput.toMap[moduleName];
						if (moduleCheck) {
							moduleCheck.setValue(true);
						}
					}
				},
				failure : function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	},

	reset: function() {
		this.getForm().reset();
	},

	newGroup: function() {
		this.reset();
		this.groupId = -1;
		this.setFieldsEnabled(true);
		this.disableBarButtons(this.barButtons);
	},
  
	onAbort : function() {
		this.setFieldsDisabled();
		if (this.groupId > 0) {
			this.enableBarButtons(this.barButtons);
			this.loadData( {
				groupId : this.groupId
			});
		} else {
			this.reset();
		}
		if (this.currentSelection == "groupfolder") {
			this.ownerCt.disable();
		}
		this.publish('cmdb-abortmodify-group', {groupId: this.groupId});
	},

	onEnableDisableGroup : function(groupId) {
		var groupId = this.getForm().findField('id').value;
		var isActive = this.getForm().findField('isActive').getValue();
		if (groupId < 0) {
			this.getForm().reset();
		} else {
			this.getForm().findField('isActive').value = false;
			CMDBuild.Ajax.request( {
				url : 'services/json/schema/modsecurity/enabledisablegroup',
				params : {
					groupId : this.groupId,
					isActive : !isActive
				},
				waitMsg : CMDBuild.Translation.common.wait_title,
				method : 'POST',
				scope : this,
				success : function(response, options, decoded) {
					this.publish('cmdb-modify-node', decoded.group);
				}
			});
		}
	},
  
	onSave : function() {
		var modules = this.modulesCheckInput.toArray;
		var disabledModules = (function() {
			var out = [];
			for ( var i = 0, len = modules.length; i < len; ++i) {
				var module = modules[i];
				if (module.checked) {
					out.push(module.module);
				}
			}
			return out;
		})();

		this.getForm().submit( {
			method : 'POST',
			url : 'services/json/schema/modsecurity/savegroup',
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			scope : this,
			params : (function() {
				if (disabledModules.length > 0) {
					return {
						disabledModules : disabledModules
					};
				} else {
					return {};
				}
			})(),
			success : function(form, action) {
				var group = action.result.group;
				if (CMDBuild.Cache.getTableById(group.id)) {
					this.publish('cmdb-modify-node', group);
				} else {
					this.publish('cmdb-new-node', group);
				}
				this.setFieldsDisabled();
				this.enableBarButtons(this.barButtons);
			},
			failure : function(form, action) {
				this.disableBarButtons(this.barButtons);
				this.publish('cmdb-abortmodify-group');
			}
		});
	},
    
	onModify:  function(evt) {
	    var id = this.getForm().findField('id').value;
	    var name = this.getForm().findField('name').value;
	    this.setFieldsEnabled();
	    this.disableBarButtons(this.barButtons);
	},
	
	disableBarButtons: function(bar) {    	
		for (var i = 0, len = bar.length; i<len ; i++ ) {
			bar[i].disable();
		}
	},
	
	enableBarButtons: function(bar) {
		for (var i = 0, len = bar.length; i<len ; i++ ) {
			bar[i].enable();
		}
	},
	
	setCurrentSelection: function(value) {
		this.currentSelection = value;
	},

	updateEnableDisableGroupLabelIcon: function() {
		var groupActive = this.getForm().findField('isActive').getValue();
		if (groupActive) {
			this.enableDisableGroupBtn.setText(this.translation.delete_group);
			this.enableDisableGroupBtn.setIconClass('delete');
		} else {
			this.enableDisableGroupBtn.setText(this.translation.enable_group);
			this.enableDisableGroupBtn.setIconClass('ok');
		}
	}
});

Ext.reg('groupform', CMDBuild.Administration.GroupForm );