(function() {

	Ext.define('CMDBuild.view.administration.filter.groups.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.filter.Groups}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		classesCombobox: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		descriptionTextField: undefined,

		/**
		 * @property {CMDBuild.view.common.field.CMFilterChooser}
		 */
		filterChooser: undefined,

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
								text: CMDBuild.Translation.modifyFilter,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFilterGroupsModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Delete', {
								text: CMDBuild.Translation.removeFilter,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFilterGroupsRemoveButtonClick');
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
									this.delegate.cmfg('onFilterGroupsSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFilterGroupsAbortButtonClick');
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
						name: _CMProxy.parameter.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						allowBlank: false,
						vtype: 'cmdbcomment',

						translationFieldConfig: {
							type: CMDBuild.core.constants.Proxy.FILTER,
							identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
							field: CMDBuild.core.constants.Proxy.DESCRIPTION
						}
					}),
					this.classesCombobox = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.ENTRY_TYPE,
						fieldLabel: CMDBuild.Translation.targetClass,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						forceSelection: true,
						editable: false,
						allowBlank: false,

						store: _CMCache.getClassesAndProcessesAndDahboardsStore(),
						queryMode: 'local',

						listeners: {
							scope: this,
							select: function(combo, records, eOpts) {
								this.delegate.cmfg('onFilterGroupsClassesComboSelect', combo.getValue());
							}
						}
					}),
					this.filterChooser = Ext.create('CMDBuild.view.common.field.CMFilterChooser', {
						name: CMDBuild.core.constants.Proxy.FILTER,
						fieldLabel: CMDBuild.Translation.filter,
						labelWidth: CMDBuild.LABEL_WIDTH,
						filterTabToEnable: {
							attributeTab: true,
							relationTab: true,
							functionTab: false
						}
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