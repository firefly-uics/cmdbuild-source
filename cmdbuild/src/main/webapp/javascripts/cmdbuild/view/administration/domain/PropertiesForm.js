(function() {

	Ext.define('CMDBuild.view.administration.domain.PropertiesForm', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Domain'
		],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.administration.domain.Properties}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		activeCheckbox: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		cardinalityCombo: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		directDescription: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		domainDescription: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		inverseDescription: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		masterDetailCheckbox: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		masterDetailLabel: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		frame: false,
		title: CMDBuild.Translation.properties,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		defaults: {
			maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.Modify', {
								text: CMDBuild.Translation.modifyDomain,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDomainModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Delete', {
								text: CMDBuild.Translation.deleteDomain,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDomainRemoveButtonClick');
								}
							})
						]
					}),
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDomainSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDomainAbortButtonClick');
								}
							})
						]
					})
				],
				plugins: [new CMDBuild.FormPlugin()],
				items: [
					Ext.create('Ext.form.TextField', {
						name: CMDBuild.core.proxy.Constants.NAME,
						fieldLabel: CMDBuild.Translation.name,
						labelWidth: CMDBuild.LABEL_WIDTH,
						allowBlank: false,
						vtype: 'alphanum',
						cmImmutable: true,

						listeners: {
							scope: this,
							change: function(field, newValue, oldValue) {
								this.autoComplete(this.domainDescription, newValue, oldValue);
							}
						}
					}),
					this.domainDescription = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: CMDBuild.core.proxy.Constants.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.LABEL_WIDTH,
						allowBlank: false,
						vtype: 'cmdbcomment',

						translationFieldConfig: {
							type: CMDBuild.core.proxy.Constants.DOMAIN,
							identifier: { sourceType: 'form', key: CMDBuild.core.proxy.Constants.NAME, source: this },
							field: CMDBuild.core.proxy.Constants.DESCRIPTION
						}
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: 'idClass1',
						fieldLabel: CMDBuild.Translation.origin,
						labelWidth: CMDBuild.LABEL_WIDTH,
						valueField: CMDBuild.core.proxy.Constants.ID,
						displayField: CMDBuild.core.proxy.Constants.DESCRIPTION,
						allowBlank: false,
						cmImmutable: true,
						forceSelection: true,
						editable: false,

						store: _CMCache.getClassesAndProcessesStore(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: 'idClass2',
						fieldLabel: CMDBuild.Translation.destination,
						labelWidth: CMDBuild.LABEL_WIDTH,
						valueField: CMDBuild.core.proxy.Constants.ID,
						displayField: CMDBuild.core.proxy.Constants.DESCRIPTION,
						allowBlank: false,
						cmImmutable: true,
						forceSelection: true,
						editable: false,

						store: _CMCache.getClassesAndProcessesStore(),
						queryMode: 'local'
					}),
					this.directDescription = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: 'descr_1', // TODO, change the server side
						fieldLabel: CMDBuild.Translation.directDescription,
						labelWidth: CMDBuild.LABEL_WIDTH,
						allowBlank: false,
						vtype: 'cmdbcomment',

						translationFieldConfig: {
							type: CMDBuild.core.proxy.Constants.DOMAIN,
							identifier: { sourceType: 'form', key: CMDBuild.core.proxy.Constants.NAME, source: this },
							field: CMDBuild.core.proxy.Constants.DIRECT_DESCRIPTION
						}
					}),
					this.inverseDescription = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: 'descr_2', // TODO, change the server side
						fieldLabel: CMDBuild.Translation.inverseDescription,
						labelWidth: CMDBuild.LABEL_WIDTH,
						allowBlank: false,
						vtype: 'cmdbcomment',

						translationFieldConfig: {
							type: CMDBuild.core.proxy.Constants.DOMAIN,
							identifier: { sourceType: 'form', key: CMDBuild.core.proxy.Constants.NAME, source: this },
							field: CMDBuild.core.proxy.Constants.INVERSE_DESCRIPTION
						}
					}),
					this.cardinalityCombo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.proxy.Constants.CARDINALITY,
						fieldLabel: CMDBuild.Translation.cardinality,
						labelWidth: CMDBuild.LABEL_WIDTH,
						width: CMDBuild.ADM_SMALL_FIELD_WIDTH,
						valueField: CMDBuild.core.proxy.Constants.NAME,
						displayField: CMDBuild.core.proxy.Constants.VALUE,
						allowBlank: false,
						cmImmutable: true,

						store: CMDBuild.core.proxy.Domain.getCardinalityStore(),
						queryMode: 'local',

						listeners: {
							scope: this,
							select:	function(combo, records, eOpts) {
								this.delegate.cmfg('onDomainPropertiesCardinalitySelect');
							}
						}
					}),
					this.masterDetailCheckbox = Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.proxy.Constants.IS_MASTER_DETAIL,
						fieldLabel: CMDBuild.Translation.masterDetail,
						labelWidth: CMDBuild.LABEL_WIDTH,

						listeners: {
							scope: this,
							change: function(field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onDomainPropertiesMasterDetailCheckboxChange');
							}
						}
					}),
					this.masterDetailLabel = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: 'md_label',
						fieldLabel: CMDBuild.Translation.masterDetailLabel,
						labelWidth: CMDBuild.LABEL_WIDTH,
						hidden: true, // Hidden by default

						translationFieldConfig: {
							type: CMDBuild.core.proxy.Constants.DOMAIN,
							identifier: { sourceType: 'form', key: CMDBuild.core.proxy.Constants.NAME, source: this },
							field: CMDBuild.core.proxy.Constants.MASTER_DETAIL
						}
					}),
					this.activeCheckbox = Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.proxy.Constants.ACTIVE,
						fieldLabel: CMDBuild.Translation.active,
						labelWidth: CMDBuild.LABEL_WIDTH
					})
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true);
		}
	});

})();