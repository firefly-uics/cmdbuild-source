(function() {
	var tr = CMDBuild.Translation.administration.modLookup.lookupTypeForm;
	
	Ext.define('CMLookupTypeFormModel', {
		extend: 'Ext.data.Model',
		fields: [
			{name: "orig_type", type: "string"},
			{name: "description", type: "string"},
			{name: "parent", type: "string"}
		]
	});
	
	Ext.define("CMDBuild.view.administration.lookup.CMLookupTypeForm", {
		extend: "Ext.form.Panel",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},
		alias: "widget.lookuptypeform",
		frame: true,
		constructor:function() {
	 		this.modifyButton = new Ext.button.Button({
				iconCls: 'modify',
				text: tr.modify_lookuptype,
				handler: function() {
	 				this.enableModify(all=false)
	 			},
				scope: this,
				disabled: true
			}),
			
			this.saveButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.btns.save,
				disabled: true
			})

			this.abortButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.btns.abort,
				disabled: true
			})
			
			this.cmButtons = [this.saveButton, this.abortButton];
			this.cmTBar = [this.modifyButton];

			var parentStore = _CMCache.getLookupTypeAsStore();
			
			this.parentCombo = Ext.create('Ext.form.ComboBox', {
				fieldLabel: tr.parent,
                labelWidth: CMDBuild.CM_LABEL_WIDTH,
				queryMode : 'local',
				displayField : 'type',
				valueField : 'type',
				store: parentStore,
				disabled: true,
				name: 'parent',
				hiddenName: 'parent',
				cmImmutable: true
			});

	 		this.items = [{
	 			xtype: "panel",
	 			frame: true,
	 			region: "center",
	 			items: [{
					xtype: 'textfield',
					fieldLabel: tr.description,
                    labelWidth: CMDBuild.CM_LABEL_WIDTH,
					name: 'description',
					allowBlank: false,
					disabled: true
				}, 
				this.parentCombo
				]
	 		}];
	 		this.layout = "border",
	 		this.buttonAlign = "center";
	 		this.buttons = this.cmButtons;
	 		this.tbar = this.cmTBar;
	 		
	 		this.callParent(arguments);
		},
		
		onSelectLookupType : function(lookupType) {
			if (lookupType) {
				var _lt = Ext.ModelManager.create({
					orig_type : lookupType.id,
					description : lookupType.text,
					parent : lookupType.parent
				}, 'CMLookupTypeFormModel');

				this.getForm().loadRecord(_lt);
				this.parentCombo.syncSelection();
			}
			this.disableModify(enableCMTBar=true);
		},
		
		onNewLookupType: function(params) {
			this.getForm().reset();	  
			this.enableModify(all=true);
		}
	});
})();