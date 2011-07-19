(function() {

	Ext.define("CMDBuild.view.administration.domain.CMDomainForm", {
		extend: "Ext.form.Panel",
		alias: "domainform",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},
		translation : CMDBuild.Translation.administration.modClass.domainProperties,

		initComponent : function() {
			var me = this;

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

			this.class_store = _CMCache.getClassesAndProcessesStore();

			this.masterdetail = new Ext.ux.form.XCheckbox({
				fieldLabel: this.translation.master_detail,
				labelWidth: CMDBuild.CM_LABEL_WIDTH,
				name: "isMasterDetail",
				cmImmutable: true
			});

			this.masterDetailLabel = new Ext.form.field.Text({
				fieldLabel: this.translation.md_label,
				labelWidth: CMDBuild.CM_LABEL_WIDTH,
				width: CMDBuild.CM_BIG_FIELD_WIDTH,
				name: "mdlabel"
			});

			this.active = new Ext.ux.form.XCheckbox({
				fieldLabel: this.translation.is_active,
                labelWidth: CMDBuild.CM_LABEL_WIDTH,
				name: "active",
				checked: true
			});

			this.cardinality_combo = new Ext.form.ComboBox({
				xdomainformtype: 'combo',
				fieldLabel: this.translation.cardinality,
				labelWidth: CMDBuild.CM_LABEL_WIDTH,
				width: CMDBuild.CM_SMALL_FIELD_WIDTH,
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
				labelWidth: CMDBuild.CM_LABEL_WIDTH,
				width: CMDBuild.CM_BIG_FIELD_WIDTH,
				name : "name",
				allowBlank : false,
				vtype : 'alphanum',
				enableKeyEvents: true,
				cmImmutable: true
			});

			this.domainDescription = new Ext.form.TextField({
				fieldLabel : this.translation.description,
				labelWidth: CMDBuild.CM_LABEL_WIDTH,
				width: CMDBuild.CM_BIG_FIELD_WIDTH,
				name : "description",
				allowBlank : false,
				vtype : 'cmdbcomment'
			});

			this.form = new Ext.form.FormPanel( {
				region : "center",
				frame : true,
				border : true,
				autoScroll : true,
				defaults: {
					labelWidth: CMDBuild.CM_LABEL_WIDTH
				},
				items : [
					this.domainName,
					this.domainDescription,
				{
					xtype: 'combo',
					fieldLabel: this.translation.class_target,
					width: CMDBuild.CM_BIG_FIELD_WIDTH,
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
					width: CMDBuild.CM_BIG_FIELD_WIDTH,
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
					width: CMDBuild.CM_BIG_FIELD_WIDTH,
					allowBlank: false,
					name: "descr_1", //TODO, change the server side
					vtype: 'cmdbcomment'
				}, {
					xtype: 'textfield',
					fieldLabel: this.translation.description_inverse,
					width: CMDBuild.CM_BIG_FIELD_WIDTH,
					allowBlank: false,
					name: "descr_2", //TODO, change the server side
					vtype: 'cmdbcomment'
				},
					this.cardinality_combo,
					this.masterdetail,
					this.masterDetailLabel,
					this.active
				]
			});

			Ext.apply(this, {
				tbar: this.cmTBar,
				buttonAlign: "center",
				buttons: this.cmButtons,
				frame: false,
				border: false,
				layout: "border",
				cls: "x-panel-body-default-framed",
				bodyCls: 'cmgraypanel',
				items: [this.form]
			});
			
			this.plugins = [new CMDBuild.FormPlugin()];
			this.callParent(arguments);

			this.domainName.on('change', function(domainNameField, newValue, oldValue) {
				this.autoComplete(this.domainDescription, newValue, oldValue);
			}, this);
			
            // show the masterDetailLabel field only when the domain is setted as a masterDetail
            this.masterdetail.setValue = Ext.Function.createInterceptor(this.masterdetail.setValue, 
                function(v) {
                    if (v) {
                        me.masterDetailLabel.show();
                        me.masterDetailLabel.setDisabled(me.masterdetail.isDisabled());
                    } else {
                        me.masterDetailLabel.hide();
                        me.masterDetailLabel.disable();
                    }
                }
            );
            
			this.disableModify();
		},

		onDomainSelected: function(cmDomain) {
			this.disableModify(enableCMTBar = true);
			if (cmDomain) {
                this.reset();
				this.getForm().loadRecord(cmDomain);
			}
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