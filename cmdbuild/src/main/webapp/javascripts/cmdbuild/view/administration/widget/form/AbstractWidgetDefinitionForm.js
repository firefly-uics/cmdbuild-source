(function() {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.view.administration.widget.form.AbstractWidgetDefinitionForm', {
		extend: 'Ext.form.Panel',

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		alwaysEnabled: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		active: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		buttonLabel: undefined,

		/**
		 * @property {Array}
		 */
		commonFields: [],

		bodyCls: 'cmgraypanel-nopadding',
		border: false,
		frame: false,

		layout: {
			type: 'hbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.baseProperties,
						flex: 1,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: this.widgetDefinitionFormBasePropertiesGet()
					}),
					{ xtype: 'splitter' },
					Ext.create('Ext.container.Container', {
						flex: 1,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: this.widgetDefinitionFormCustomPropertiesGet()
					}),
				]
			});

			this.callParent(arguments);
		},

//		/**
//		 * @abstract
//		 */
//		disableNonFieldElements: Ext.emptyFn,
//
//		/**
//		 * @abstract
//		 */
//		enableNonFieldElements: Ext.emptyFn,

//		/**
//		 * @abstract
//		 */
//		getWidgetDefinition: Ext.emptyFn,

		/**
		 * @abstract
		 */
		fillWithModel: Ext.emptyFn,

		/**
		 * @returns {Array}
		 *
		 * @abstract
		 */
		widgetDefinitionFormBasePropertiesGet: Ext.emptyFn,

		/**
		 * @returns {Array}
		 */
		widgetDefinitionFormCommonBasePropertiesGet: function() {
			return [
				this.buttonLabel = Ext.create('CMDBuild.view.common.field.translatable.Text', {
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
				this.active = Ext.create('Ext.form.field.Checkbox', {
					name: CMDBuild.core.constants.Proxy.ACTIVE,
					fieldLabel: CMDBuild.Translation.administration.modClass.widgets.commonFields.active,
					labelWidth: CMDBuild.LABEL_WIDTH
				}),
				this.alwaysEnabled = Ext.create('Ext.form.field.Checkbox', {
					name: 'alwaysenabled',
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