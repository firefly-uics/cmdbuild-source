(function() {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.view.administration.widget.form.AbstractWidgetDefinitionPanel', {
		extend: 'Ext.form.Panel',

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {Mixed}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.container.Container}
		 */
		additionalProperties: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		baseProperties: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		cls: 'x-panel-body-default-framed cmbordertop',
		frame: false,
		overflowY: 'auto',
		split: true,
		region: 'center',

		layout: {
			type: 'hbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyWidget,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onClassTabWidgetModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Delete', {
								text: CMDBuild.Translation.removeWidget,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onClassTabWidgetRemoveButtonClick');
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
									this.delegate.cmfg('onClassTabWidgetSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onClassTabWidgetAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.baseProperties = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.baseProperties,
						flex: 1,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: this.widgetDefinitionFormBasePropertiesGet()
					}),
					{ xtype: 'splitter' },
					this.additionalProperties = Ext.create('Ext.container.Container', {
						flex: 1,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: this.widgetDefinitionFormCustomPropertiesGet()
					})
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		},

		/**
		 * @returns {Array}
		 */
		widgetDefinitionFormBasePropertiesGet: function() {
			return [
				Ext.create('Ext.form.field.Hidden', { name: CMDBuild.core.constants.Proxy.ID }),
				Ext.create('CMDBuild.view.common.field.translatable.Text', {
					name: CMDBuild.core.constants.Proxy.LABEL,
					fieldLabel: CMDBuild.Translation.administration.modClass.widgets.commonFields.buttonLabel,
					maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
					labelWidth: CMDBuild.LABEL_WIDTH,
					allowBlank: false,

					translationFieldConfig: {
						type: CMDBuild.core.constants.Proxy.CLASS_WIDGET,
						owner: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
						identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
						field: CMDBuild.core.constants.Proxy.DESCRIPTION
					}
				}),
				Ext.create('Ext.form.field.Checkbox', {
					name: CMDBuild.core.constants.Proxy.ACTIVE,
					fieldLabel: CMDBuild.Translation.administration.modClass.widgets.commonFields.active,
					labelWidth: CMDBuild.LABEL_WIDTH
				}),
				Ext.create('Ext.form.field.Checkbox', {
					name: CMDBuild.core.constants.Proxy.ALWAYS_ENABLED,
					fieldLabel: CMDBuild.Translation.administration.modClass.widgets.commonFields.alwaysenabled,
					labelWidth: CMDBuild.LABEL_WIDTH
				})
			];
		},

		/**
		 * @returns {Array}
		 *
		 * @abstract
		 */
		widgetDefinitionFormCustomPropertiesGet: Ext.emptyFn
	});

})();