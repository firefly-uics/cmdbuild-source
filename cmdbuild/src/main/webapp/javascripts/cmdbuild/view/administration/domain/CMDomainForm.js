(function() {

	Ext.define("CMDBuild.view.administration.domain.CMDomainForm", {
		extend: "Ext.form.Panel",
		alias: "domainform",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},
		translation : CMDBuild.Translation.administration.modClass.domainProperties,

		initComponent : function() {
			this.modifyButton = new Ext.button.Button({
				iconCls : 'modify',
				text: this.translation.modify_domain,
				scope: this,
				handler: function() {
					this.enableModify();
				}
			});

			this.deleteButton = new Ext.button.Button({
				iconCls : 'delete',
				text: this.translation.delete_domain
			});

			this.saveButton = new Ext.button.Button( {
				text : CMDBuild.Translation.common.buttons.save
			});

			this.abortButton = new Ext.button.Button( {
				text : CMDBuild.Translation.common.buttons.abort
			});

			this.cmTBar = [this.modifyButton, this.deleteButton];
			this.cmButtons = [this.saveButton, this.abortButton];

			this.class_store = _CMCache.getClassesStore(); 

			this.masterdetail = new Ext.ux.form.XCheckbox({
				xtype: 'xcheckbox',
				fieldLabel: this.translation.master_detail,
				name: "isMasterDetail",
				cmImmutable: true
			});

			this.active = new Ext.ux.form.XCheckbox({
				fieldLabel: this.translation.is_active,
				name: "active",
				checked: true
			});

			this.cardinality_combo = new Ext.form.ComboBox({
				xdomainformtype: 'combo',
				fieldLabel: this.translation.cardinality,
				name: "cardinality",
				valueField: 'name',
				displayField: 'value',
				triggerAction: 'all',
				store: new Ext.data.SimpleStore({
					fields: ['name', 'value'],
					data: [
						['1:1', '1:1'],
						['1:N', '1:N'],
						['N:1', 'N:1'],
						['N:N', 'N:N']
					]
				}),
				allowBlank: false,
				queryMode: 'local',
				cmImmutable: true
			});

			this.cardinality_combo.on('select', enableMDCheckBox, this);

			this.domainName = new Ext.form.TextField({
				fieldLabel : this.translation.name,
				name : "name",
				allowBlank : false,
				vtype : 'alphanum',
				enableKeyEvents: true,
				cmImmutable: true
			});

			this.domainDescription = new Ext.form.TextField({
				fieldLabel : this.translation.description,
				name : "description",
				allowBlank : false,
				vtype : 'cmdbcomment'
			});

			this.form = new Ext.form.FormPanel( {
				region : "center",
				frame : true,
				border : true,
				autoScroll : true,
				items : [
					this.domainName,
					this.domainDescription,
				{
					xtype: 'combo',
					fieldLabel: this.translation.class_target,
					name: "idClass1",
					triggerAction: 'all',
					valueField: 'id',
					displayField: 'description',
					minChars: 0,
					allowBlank: false,
					store: this.class_store,
					queryMode: "local",
					cmImmutable: true
				}, {
					xtype: 'combo',
					fieldLabel: this.translation.class_destination,
					name: "idClass2",
					triggerAction: 'all',
					valueField: 'id',
					displayField: 'description',
					minChars: 0,
					allowBlank: false,
					store: this.class_store,
					queryMode: "local",
					cmImmutable: true
				}, {
					xtype: 'textfield',
					fieldLabel: this.translation.description_direct,
					allowBlank: false,
					name: "directDescription",
					vtype: 'cmdbcomment'
				}, {
					xtype: 'textfield',
					fieldLabel: this.translation.description_inverse,
					allowBlank: false,
					name: "reverseDescription",
					vtype: 'cmdbcomment'
				},
					this.cardinality_combo,
					this.masterdetail,
					this.active
				]
			});

			Ext.apply(this, {
				tbar: this.cmTBar,
				buttons: this.cmButtons,
				frame: true,
				border: false,
				layout: "border",
				items: [this.form]
			});
			
			this.plugins = [new CMDBuild.FormPlugin()];
			this.callParent(arguments);

			this.domainName.on('change', function(domainNameField, newValue, oldValue) {
				this.autoComplete(this.domainDescription, newValue, oldValue);
			}, this);

		},

		onDomainSelected: function(cmDomain) {
			this.disableModify(enableCMTBar = true);
			this.getForm().loadRecord(cmDomain);
		},

		setDefaultValues: function() {
			this.active.setValue(true);
		},
		
		onAddButtonClick: function() {
			this.reset();
			this.enableModify(all = true);
			this.setDefaultValues();
		}

	});

	// a domain must set MD only if the cardinality is "1:N" or "N:1" 
	function enableMDCheckBox() {
		if ( this.cardinality_combo.getValue() 
			&& !(this.cardinality_combo.getValue() == '1:N' 
			|| this.cardinality_combo.getValue() == 'N:1')) {
				
			this.masterdetail.setValue(false);
			this.masterdetail.disable();
		} else {
			this.masterdetail.enable();
		}
	}
})();