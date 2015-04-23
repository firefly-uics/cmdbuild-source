(function() {

	Ext.define('CMDBuild.view.administration.domain.CMDomainForm', {
		extend: 'Ext.form.Panel',
		alias: 'domainform',

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		translation: CMDBuild.Translation.administration.modClass.domainProperties,

		bodyCls: 'cmgraypanel',
		buttonAlign: 'center',
		border: false,
		cls: 'x-panel-body-default-framed',
		frame: false,
		layout: 'border',

		initComponent: function() {
			var me = this;

			// Buttons configuration
			this.modifyButton = Ext.create('Ext.button.Button', {
				iconCls: 'modify',
				text: this.translation.modify_domain,
				scope: this,

				handler: function() {
					this.delegate.cmOn('onModifyButtonClick');
					this.enableModify();
					_CMCache.initModifyingTranslations();
				}
			});

			this.deleteButton = Ext.create('Ext.button.Button', {
				iconCls: 'delete',
				text: this.translation.delete_domain
			});

			this.saveButton = Ext.create('Ext.button.Button', {
				text: CMDBuild.Translation.common.buttons.save,
				scope: this,

				handler: function() {
					this.delegate.cmOn('onSaveButtonClick');
				}
			});

			this.abortButton = Ext.create('Ext.button.Button', {
				text: CMDBuild.Translation.common.buttons.abort,
				scope: this,

				handler: function() {
					this.delegate.cmOn('onAbortButtonClick');
				}
			});
			// END: Buttons configuration

			this.cmTBar = [this.modifyButton, this.deleteButton];
			this.cmButtons = [this.saveButton, this.abortButton];
			this.class_store = _CMCache.getClassesAndProcessesStore();

			this.masterdetail = Ext.create('Ext.ux.form.XCheckbox', {
				fieldLabel: this.translation.master_detail,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: 'isMasterDetail'
			});

			this.masterDetailLabel = Ext.create('CMDBuild.view.common.field.translatable.Text', {
				fieldLabel: this.translation.md_label,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				translationsKeyType: 'Domain',
				translationsKeyField: 'masterDetail',
				name: 'md_label'
			});

			this.active = Ext.create('Ext.ux.form.XCheckbox', {
				fieldLabel: this.translation.is_active,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: 'active',
				checked: true
			});

			this.cardinality_combo = Ext.create('Ext.form.field.ComboBox', {
				xdomainformtype: 'combo',
				fieldLabel: this.translation.cardinality,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_SMALL_FIELD_WIDTH,
				name: 'cardinality',
				valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
				displayField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				triggerAction: 'all',
				allowBlank: false,
				cmImmutable: true,

				store: Ext.create('Ext.data.SimpleStore', {
					fields: [CMDBuild.core.proxy.CMProxyConstants.NAME, CMDBuild.core.proxy.CMProxyConstants.VALUE],
					data: [
						['1:1', '1:1'],
						['1:N', '1:N'],
						['N:1', 'N:1'],
						['N:N', 'N:N']
					]
				}),
				queryMode: 'local'
			});

			this.cardinality_combo.on('select', enableMDCheckBox, this);

			this.domainName = Ext.create('Ext.form.TextField', {
				fieldLabel: this.translation.name,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name: CMDBuild.core.proxy.CMProxyConstants.NAME,
				allowBlank: false,
				vtype: 'alphanum',
				enableKeyEvents: true,
				cmImmutable: true
			});

			this.domainDescription = Ext.create('CMDBuild.view.common.field.translatable.Text', {
				fieldLabel: this.translation.description,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				allowBlank: false,
				translationsKeyType: 'Domain',
				translationsKeyField: 'Description',
				vtype: 'cmdbcomment'
			});

			this.directDescription = Ext.create('CMDBuild.view.common.field.translatable.Text', {
				fieldLabel: this.translation.description_direct,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				allowBlank: false,
				name: 'descr_1', // TODO, change the server side
				translationsKeyType: 'Domain',
				translationsKeyField: 'directDescription',
				vtype: 'cmdbcomment'
			});

			this.inverseDescription = Ext.create('CMDBuild.view.common.field.translatable.Text', {
				fieldLabel: this.translation.description_inverse,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				allowBlank: false,
				name: 'descr_2', // TODO, change the server side
				translationsKeyType: 'Domain',
				translationsKeyField: 'inverseDescription',
				vtype: 'cmdbcomment'
			});

			this.form = Ext.create('Ext.form.FormPanel', {
				region: 'center',
				frame: true,
				border: true,
				autoScroll: true,

				defaults: {
					labelWidth: CMDBuild.LABEL_WIDTH
				},

				items: [
					this.domainName,
					this.domainDescription,
					Ext.create('Ext.form.field.ComboBox', {
						fieldLabel: this.translation.class_target,
						width: CMDBuild.ADM_BIG_FIELD_WIDTH,
						labelWidth: CMDBuild.LABEL_WIDTH,
						name: 'idClass1',
						triggerAction: 'all',
						valueField: CMDBuild.core.proxy.CMProxyConstants.ID,
						displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						minChars: 0,
						allowBlank: false,
						cmImmutable: true,
						forceSelection: true,
						editable: false,

						store: this.class_store,
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.ComboBox', {
						fieldLabel: this.translation.class_destination,
						width: CMDBuild.ADM_BIG_FIELD_WIDTH,
						labelWidth: CMDBuild.LABEL_WIDTH,
						name: 'idClass2',
						triggerAction: 'all',
						valueField: CMDBuild.core.proxy.CMProxyConstants.ID,
						displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						minChars: 0,
						allowBlank: false,
						cmImmutable: true,
						forceSelection: true,
						editable: false,

						store: this.class_store,
						queryMode: 'local'
					}),
					this.directDescription,
					this.inverseDescription,
					this.cardinality_combo,
					this.masterdetail,
					this.masterDetailLabel,
					this.active
				]
			});

			Ext.apply(this, {
				tbar: this.cmTBar,
				buttons: this.cmButtons,
				items: [this.form]
			});

			this.plugins = [new CMDBuild.FormPlugin()];

			this.callParent(arguments);

			this.domainName.on(
				'change',
				function(domainNameField, newValue, oldValue) {
					this.autoComplete(this.domainDescription, newValue, oldValue);
				},
				this
			);

			// show the masterDetailLabel field only when the domain is setted
			// as a masterDetail
			this.masterdetail.setValue = Ext.Function.createInterceptor(this.masterdetail.setValue, function(v) {
				if (v) {
					me.masterDetailLabel.show();
					me.masterDetailLabel.setDisabled(me.masterdetail.isDisabled());
				} else {
					me.masterDetailLabel.hide();
					me.masterDetailLabel.disable();
				}
			});

			this.disableModify();
		},

		onDomainSelected: function(cmDomain) {
			this.disableModify(enableCMTBar = true);
			this.domainDescription.translationsKeyName = cmDomain.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
			this.directDescription.translationsKeyName = cmDomain.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
			this.inverseDescription.translationsKeyName = cmDomain.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
			this.masterDetailLabel.translationsKeyName = cmDomain.get(CMDBuild.core.proxy.CMProxyConstants.NAME);

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
			_CMCache.initAddingTranslations();
		},

		enableModify: function(all) {
			this.mixins.cmFormFunctions.enableModify.call(this, all);
			enableMDCheckBox.call(this);
		}
	});

	// a domain must set MD only if the cardinality is '1:N' or 'N:1'
	function enableMDCheckBox() {
		if (
			this.cardinality_combo.getValue()
			&& !(
				this.cardinality_combo.getValue() == '1:N'
				|| this.cardinality_combo.getValue() == 'N:1'
			)
		) {
			this.masterdetail.setValue(false);
			this.masterdetail.disable();
		} else {
			this.masterdetail.enable();
		}
	}

})();