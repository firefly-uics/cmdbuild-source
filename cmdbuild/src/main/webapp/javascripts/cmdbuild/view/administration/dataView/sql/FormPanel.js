(function() {

	Ext.define('CMDBuild.view.administration.dataView.sql.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.dataView.Sql}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		descriptionTextField: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		sourceFunctionCombobox: undefined,

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
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyView,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewSqlModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Delete', {
								text: CMDBuild.Translation.removeView,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewSqlRemoveButtonClick');
								}
							})
						]
					}),
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewSqlSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewSqlAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.NAME,
						itemId: CMDBuild.core.constants.Proxy.NAME,
						fieldLabel: CMDBuild.Translation.name,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						allowBlank: false,
						cmImmutable: true
					}),
					this.descriptionTextField = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						allowBlank: false,
						vtype: 'cmdbcomment',

						translationFieldConfig: {
							type: CMDBuild.core.constants.Proxy.VIEW,
							identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
							field: CMDBuild.core.constants.Proxy.DESCRIPTION
						}
					}),
					this.sourceFunctionCombobox = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.SOURCE_FUNCTION,
						fieldLabel: CMDBuild.Translation.dataSource,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						displayField: CMDBuild.core.constants.Proxy.NAME,
						forceSelection: true,
						editable: false,
						allowBlank: false,

						store: _CMCache.getAvailableDataSourcesStore(),
						queryMode: 'local',
					}),
					{
						xtype: 'hiddenfield',
						name: CMDBuild.core.constants.Proxy.ID
					}
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		}
	});

})();