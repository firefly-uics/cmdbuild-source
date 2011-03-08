CMDBuild.Administration.DomainForm = Ext.extend(Ext.Panel, {
	translation : CMDBuild.Translation.administration.modClass.domainProperties,
	layout: 'fit',
	eventtype : 'class',
	idClass : -1,
	plugins : [new CMDBuild.CallbackPlugin(), new CMDBuild.FormPlugin()],
	initComponent : function() {

		this.modifyAction = new Ext.Action({
			iconCls : 'modify',
			text : this.translation.modify_domain,
			handler : function() {
				this.formPanel.setFieldsEnabled();
				this.saveButton.formBind = true;
				this.saveButton.enable();
				this.abortButton.enable();
				this.modifyAction.disable();
				this.deleteAction.disable();
				this.setMDcheckbox();
			},
			scope : this,
			disabled : true
		});

		this.deleteAction = new Ext.Action({
			iconCls : 'delete',
			text : this.translation.delete_domain,
			handler : function() {
				this.deleteDomain(true);
			},
			scope : this,
			disabled : true
		});

		var card_store = new Ext.data.SimpleStore({
			fields : ['name', 'value'],
			data : [
				['1:1', '1:1'], 
				['1:N', '1:N'], 
				['N:1', 'N:1'],	
				['N:N', 'N:N']
			]
		});

		this.class_store = CMDBuild.Cache.getClassesAndProcessAsStore(); 

		var masterdetail = new Ext.ux.form.XCheckbox({
			xtype : 'xcheckbox',
			fieldLabel : this.translation.master_detail,
			width : 220,
			name : 'md',
			disabled : true
		});

	    cardinality_combo = new Ext.form.ComboBox({
			xtype : 'combo',
			fieldLabel : this.translation.cardinality,
			width : 220,
			name : 'cardinality',
			valueField : 'name',
			displayField : 'value',
			triggerAction: 'all',
			store : card_store,
			allowBlank : false,
			mode : 'local',
			disabled : true,
			CMDBuildReadonly : true
		});

		cardinality_combo.on('select', this.setMDcheckbox, this);
		
		this.domainName = new Ext.form.TextField({			
			fieldLabel : this.translation.name,
			name : 'name',
			width : 220,
			allowBlank : false,
			disabled : true,
			vtype : 'alphanum',
			CMDBuildReadonly : true,
			enableKeyEvents: true
		});
		
		this.domainDescription = new Ext.form.TextField({			
			fieldLabel : this.translation.description,
			width : 220,
			name : 'description',
			disabled : true,
			allowBlank : false,
			vtype : 'cmdbcomment'
		});
		
		this.formPanel = new Ext.form.FormPanel({
			frame: true,
			border: true,			
			defaultType : 'textfield',    
			autoScroll: true,
			style: {padding: '5px', background: '#F0F0F0'},
			labelWidth: 150,
			items: [{
				name : 'idDomain',
				xtype : 'hidden',
				value : -1
			}, {
				xtype : 'hidden',
				name : 'origName',
				width : 220
			}, 	
				this.domainName,
				this.domainDescription,
			{
				xtype: 'combo',
	            fieldLabel: this.translation.class_target,
	            name: 'class1_value',
	            hiddenName: 'class1',
	            width: 220,
	            triggerAction: 'all',
	            valueField: 'id',
	            displayField: 'description',
	            minChars: 0,
	            allowBlank: false,
	            store: this.class_store,
	            disabled: true,
	            CMDBuildReadonly: true,
	            mode: "local"
			}, {
				xtype: 'combo',
	            fieldLabel: this.translation.class_destination,
	            name: 'class2_value',
	            hiddenName: 'class2',
	            width: 220,
	            triggerAction: 'all',
	            valueField: 'id',
	            displayField: 'description',
	            minChars: 0,
	            allowBlank: false,
	            store: this.class_store,
	            disabled: true,
	            CMDBuildReadonly: true,
	            mode: "local"
			}, {
				xtype : 'textfield',
				fieldLabel : this.translation.description_direct,
				width : 220,
				allowBlank : false,
				name : 'descrdir',
				disabled : true,
				vtype : 'cmdbcomment'
			}, {
				xtype : 'textfield',
				fieldLabel : this.translation.description_inverse,
				width : 220,
				allowBlank : false,
				name : 'descrinv',
				disabled : true,
				vtype : 'cmdbcomment'
			}, cardinality_combo, masterdetail, {
				xtype : 'xcheckbox',
				fieldLabel : this.translation.is_active,
				width : 220,
				name : 'active',
				checked : true,
				disabled : true
			}]
		});
		
		this.saveButton = new Ext.Button({
			id : 'saveDomainButton',
			text : CMDBuild.Translation.common.buttons.save,
			formBind : false,
			scope : this,
			handler : function() {
				this.saveDomain();
			},
			disabled : true
		});
		
		this.abortButton = new Ext.Button({
			id : 'abortDomainButton',
			text : CMDBuild.Translation.common.buttons.abort,
			scope : this,
			handler : function() {
				this.loadRecord(this.record);
			},
			disabled : true
		});

		var reader = new Ext.data.JsonReader({
				totalProperty : "results",
				root : "rows"
			}, ['idDomain', 'name', 'descrdir', 'descrinv', 'class1', 'class2', 'cardinality', 'md'
		]);

		Ext.apply(this, {
			monitorValid : true,
			frame: false,
			style: {background: '#F0F0F0'},
			border: false,
			reader : reader,
			autoScroll: false,			
			items : this.formPanel,
			buttonAlign: 'center',
			buttons : [this.saveButton, this.abortButton],
			tbar : [this.modifyAction, this.deleteAction]
		});

		CMDBuild.Administration.DomainForm.superclass.initComponent.apply(this, arguments);

		this.subscribe('cmdb-init-' + this.eventtype, this.setIdClass, this);
		this.subscribe('cmdb-load-' + this.eventtype + 'domain', this.loadRecord, this);
		this.subscribe('cmdb-new-' + this.eventtype + 'domain', this.addDomain, this);

		this.on({
			afterlayout : {
				scope : this,
				single : true,
				fn : function() {
					this.reset();
				}
			}
		});
		
		this.domainName.on('change', function(domainNameField, newValue, oldValue) {
			this.autoComplete(this.domainDescription, newValue, oldValue)
		}, this);
	},

	//private
	getForm: function() {
		return this.formPanel.getForm();
	},
	
	setIdClass : function(params) {
		if (params.idClass) {
			this.idClass = params.idClass;
		}
		this.modifyAction.disable();
		this.deleteAction.disable();
		this.formPanel.setFieldsDisabled();
		this.saveButton.formBind = false;
		this.saveButton.disable();
		this.abortButton.disable();
	},

	reset : function() {
		this.getForm().reset();
		this.setTitle(this.translation.title_add);
	},

	loadRecord : function(params) {
		this.getForm().reset();
		if (params) {
			if (params.record) {
				record = params.record;
			}
			this.record = record;
			this.getForm().loadRecord(record);

			this.formPanel.setFieldsDisabled();
			this.saveButton.formBind = false;
			this.saveButton.disable();
			this.abortButton.disable();
			
			this.modifyAction.enable();
			this.deleteAction.enable();
		}
	},

	manageTypeChange : function(record) {
		CMDBuild.log.info(record);
	},

	saveDomain : function() {
		CMDBuild.LoadMask.get().show();
		this.getForm().submit({
			method : 'POST',
			url : 'services/json/schema/modclass/savedomain',
			params : {
				"idClass" : this.idClass
			},			
			scope : this,
			success : function() {
				CMDBuild.LoadMask.get().hide();
				this.publish('cmdb-modified-' + this.eventtype
								+ 'domain', {
							idClass : this.idClass
						});
				this.formPanel.setFieldsDisabled();
				this.saveButton.formBind = false;
				this.saveButton.disable();
				this.abortButton.disable();
				this.modifyAction.disable();
				this.deleteAction.disable();
				this.reset();
			},
			failure : function() {
				CMDBuild.LoadMask.get().hide();
				if (this.record)
					this.loadRecord(this.record);
			}
		});
	},

	deleteDomain : function() {
		CMDBuild.LoadMask.get().show();
		this.getForm().submit({
			method : 'POST',
			url : 'services/json/schema/modclass/deletedomain',
			params : {
				"idClass" : this.idClass
			},			
			scope : this,
			success : function(form, action) {
				this.modifyAction.disable();
				this.deleteAction.disable();
				this.publish('cmdb-modified-' + this.eventtype + 'domain', {
					idClass : this.idClass
				});
				this.reset();
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	},

	addDomain : function() {
		this.formPanel.enableAllField();
		this.abortButton.enable();
		this.saveButton.formBind = true;
		this.saveButton.enable();
		this.reset();
	},
	
	setMDcheckbox: function() {
		var cardinality_combo = this.getForm().findField('cardinality');
		var masterdetail = this.getForm().findField('md');
		if ( cardinality_combo.getValue() 
			&& !(cardinality_combo.getValue() == '1:N' 
			|| cardinality_combo.getValue() == 'N:1')) {
				
			masterdetail.setValue(false);
			masterdetail.disable();
		} else {
			masterdetail.enable();
		}
	}

});

Ext.reg('domainform', CMDBuild.Administration.DomainForm);