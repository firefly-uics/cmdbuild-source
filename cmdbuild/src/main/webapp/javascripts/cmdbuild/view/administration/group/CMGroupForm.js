(function() {

var   tr = CMDBuild.Translation.administration.modsecurity.group;

Ext.define("CMDBuild.view.administration.group.CMGroupForm", {
	extend: "Ext.form.Panel",
	mixins: {
		cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
	},
	alias: "groupform",
  
	constructor: function() {

		this.enableGroupButton = new Ext.button.Button({
			iconCls: 'delete',
			text: tr.delete_group
		});
		
		this.modifyButton = new Ext.button.Button({
			iconCls: 'modify',
			text: tr.modify_group
		});
		
		this.saveButton = new CMDBuild.buttons.SaveButton();
		this.abortButton = new CMDBuild.buttons.AbortButton(); 
		
		this.groupName = new Ext.form.field.Text({
			fieldLabel : tr.group_name,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name : 'name',
			allowBlank : false,
			vtype: 'alphanum',
			cmImmutable: true
		});
		
		this.groupDescription = new Ext.form.field.Text({
			fieldLabel : tr.group_description,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name : 'description',
			allowBlank : false
		});
		
		this.groupEmail = new Ext.form.field.Text({
			vtype : 'emailOrBlank',
			fieldLabel : tr.email,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name : 'email',
			allowBlank : true
		});
		
		this.activeCheck = new Ext.ux.form.XCheckbox({
			xtype : 'xcheckbox',
			fieldLabel : tr.is_active,
			labelWidth: CMDBuild.LABEL_WIDTH,
			name : 'isActive',
			checked : true 
		}); 
		
		this.startingClass = new CMDBuild.field.ErasableCombo({
			xtype : 'combo',
			fieldLabel : tr.starting_class,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name : 'startingClass',
			valueField : 'id',
			displayField : 'description',
			editable: false,
			store : _CMCache.getClassesAndProcessesAndDahboardsStore(),
			queryMode: 'local'
		});
		
		this.propertiesFieldset = new Ext.form.FieldSet({
			title: CMDBuild.Translation.administration.modClass.attributeProperties.baseProperties,
			padding: "5 5 20 5",
			autoScroll: true,
			defaults: {
				labelWidth: CMDBuild.LABEL_WIDTH
			},
			items: [
				this.groupName,
				this.groupDescription,
				this.groupEmail,
			{
				xtype : 'xcheckbox',
				fieldLabel : tr.is_administrator,
				name : 'isAdministrator'
			}
				,this.startingClass
				,this.activeCheck
			],
			flex: 1,
			margins:'0 5 0 0'
		})

		this.modulesCheckInput = readModulesFromStructure();

		this.modulsFieldset = new Ext.form.FieldSet({
			title: tr.disabled_modules,
			autoScroll: true,
			padding: "5 5 20 5",
			items: this.modulesCheckInput.toArray,
			flex: 1
		});

		this.cmTBar = [this.modifyButton, this.enableGroupButton ];
		this.cmButtons = [this.saveButton, this.abortButton];

		this.callParent(arguments);
	},
	
	initComponent: function() {
		Ext.apply(this, {
			tbar: this.cmTBar,
			items: [{
				xtype: "panel",
				region: "center",
				frame: true,
				layout: "hbox",
				items: [this.propertiesFieldset, this.modulsFieldset]
			}],
			buttonAlign: 'center',
			buttons : this.cmButtons,
			frame: false,
			border: false,
			cls: "x-panel-body-default-framed",
			bodyCls: 'cmgraypanel',
			layout: "border"
		});

		this.callParent(arguments);
		this.disableModify(enableTBar = false);
	},
	
	updateDisableEnableGroup : function() {
		if (this.activeCheck.getValue()) {
			this.enableGroupButton.setText(tr.delete_group);
			this.enableGroupButton.setIconCls('delete');
		} else {
			this.enableGroupButton.setText(tr.enable_group);
			this.enableGroupButton.setIconCls('ok');
		}
	},
		
	loadGroup: function(g) {
		this.reset();
		var modulesToDisable = g.data.disabledModules || [];

		this.getForm().loadRecord(g);
		for ( var i = 0, len = modulesToDisable.length; i < len; ++i) {
			var moduleName = modulesToDisable[i];
			var moduleCheck = this.modulesCheckInput.toMap[moduleName];
			if (moduleCheck) {
				moduleCheck.setValue(true);
			}
		}
		
		this.updateDisableEnableGroup();
	}
});

function readModulesFromStructure() {
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
}

})();