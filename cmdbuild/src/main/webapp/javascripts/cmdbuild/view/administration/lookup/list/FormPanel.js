(function() {

	Ext.define('CMDBuild.view.administration.lookup.list.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.lookup.Lookup',
			'CMDBuild.model.lookup.Lookup'
		],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.administration.lookup.List}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		activeCheckbox: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		parentCombobox: undefined,

		/**
		 * @property {CMDBuild.core.buttons.Delete}
		 */
		toggleActiveStateButton: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		cls: 'x-panel-body-default-framed cmbordertop',
		frame: false,
		overflowY: 'auto',
		split: true,

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.Modify', {
								text: CMDBuild.Translation.modifyLookup,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLookupListModifyButtonClick');
								}
							}),
							this.toggleActiveStateButton = Ext.create('CMDBuild.core.buttons.Delete', {
								text: CMDBuild.Translation.disableLookup,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLookupListToggleActiveStateButtonClick');
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
									this.delegate.cmfg('onLookupListSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLookupListAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.Text', {
						name: 'Code',
						fieldLabel: CMDBuild.Translation.code,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH
					}),
					Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: 'Description',
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						allowBlank: false,

						translationFieldConfig: {
							type: CMDBuild.core.proxy.Constants.LOOKUP_VALUE,
							identifier: { sourceType: 'form', key: 'TranslationUuid', source: this },
							field: CMDBuild.core.proxy.Constants.DESCRIPTION
						}
					}),
					this.parentCombobox = Ext.create('Ext.form.field.ComboBox', {
						name: 'ParentId',
						fieldLabel: CMDBuild.Translation.parentDescription,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						displayField: 'ParentDescription',
						valueField: 'ParentId',
						forceSelection: true,
						editable: false,

						store: CMDBuild.core.proxy.lookup.Lookup.getParentStore(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.TextArea', {
						name: 'Notes',
						fieldLabel: CMDBuild.Translation.notes,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH
					}),
					this.activeCheckbox = Ext.create('Ext.form.field.Checkbox', {
						name: 'Active',
						fieldLabel: CMDBuild.Translation.active,
						labelWidth: CMDBuild.LABEL_WIDTH,
						inputValue: true,
						uncheckedValue: false,
						checked: true
					}),
					{
						xtype: 'hiddenfield',
						name: 'Id'
					},
					{ // Used for translations
						xtype: 'hiddenfield',
						name: 'TranslationUuid'
					}
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		}
	});

})();