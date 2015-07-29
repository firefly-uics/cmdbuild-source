(function() {

	Ext.define('CMDBuild.view.administration.filters.groups.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.administration.filters.Groups}
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
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyFilter,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFiltersGroupsModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Delete', {
								text: CMDBuild.Translation.removeFilter,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFiltersGroupsRemoveButtonClick');
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
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFiltersGroupsSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFiltersGroupsAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.proxy.Constants.NAME,
						itemId: CMDBuild.core.proxy.Constants.NAME,
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
							type: CMDBuild.core.proxy.Constants.FILTER,
							identifier: { sourceType: 'form', key: CMDBuild.core.proxy.Constants.NAME, source: this },
							field: CMDBuild.core.proxy.Constants.DESCRIPTION
						}
					}),
					this.classesCombobox = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.proxy.Constants.ENTRY_TYPE,
						fieldLabel: CMDBuild.Translation.targetClass,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						valueField: CMDBuild.core.proxy.Constants.NAME,
						displayField: CMDBuild.core.proxy.Constants.DESCRIPTION,
						forceSelection: true,
						editable: false,
						allowBlank: false,

						store: _CMCache.getClassesAndProcessesAndDahboardsStore(),
						queryMode: 'local',

						listeners: {
							scope: this,
							select: function(combo, records, eOpts) {
								this.delegate.cmfg('onFiltersGroupsClassesComboSelect', combo.getValue());
							}
						}
					}),
					this.filterChooser = Ext.create('CMDBuild.view.common.field.CMFilterChooser', {
						name: CMDBuild.core.proxy.Constants.FILTER,
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
						name: CMDBuild.core.proxy.Constants.ID
					}
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		}
	});

})();