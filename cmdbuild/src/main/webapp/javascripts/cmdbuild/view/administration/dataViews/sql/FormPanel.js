(function() {

	Ext.define('CMDBuild.view.administration.dataViews.sql.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.administration.dataViews.Sql}
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
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.Modify', {
								text: CMDBuild.Translation.modifyView,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewsSqlModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Delete', {
								text: CMDBuild.Translation.removeView,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewsSqlRemoveButtonClick');
								}
							})
						]
					}),
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
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
									this.delegate.cmfg('onDataViewsSqlSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewsSqlAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.proxy.CMProxyConstants.NAME,
						itemId: CMDBuild.core.proxy.CMProxyConstants.NAME,
						fieldLabel: CMDBuild.Translation.name,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						allowBlank: false,
						cmImmutable: true
					}),
					this.descriptionTextField = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: _CMProxy.parameter.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						allowBlank: false,
						vtype: 'cmdbcomment',
						translationsKeyType: 'View',
						translationsKeyField: 'Description'
					}),
					this.sourceFunctionCombobox = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.proxy.CMProxyConstants.SOURCE_FUNCTION,
						fieldLabel: CMDBuild.Translation.dataSource,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						forceSelection: true,
						editable: false,
						allowBlank: false,

						store: _CMCache.getAvailableDataSourcesStore(),
						queryMode: 'local',
					}),
					{
						xtype: 'hiddenfield',
						name: CMDBuild.core.proxy.CMProxyConstants.ID
					}
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		}
	});

})();