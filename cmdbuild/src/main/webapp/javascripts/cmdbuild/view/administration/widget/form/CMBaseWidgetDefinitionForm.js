(function() {

	var tr = CMDBuild.Translation.administration.modClass.widgets;

	/**
	 * @deprecated (CMDBuild.view.administration.widget.form.AbstractWidgetDefinitionForm)
	 */
	Ext.define('CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		statics: {
			WIDGET_NAME: undefined
		},

		/**
		 * @cfg {Boolean}
		 */
		isWidgetDefinition: true,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		alwaysenabled: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		active: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		buttonLabel: undefined,

		/**
		 * @property {Ext.panel.Panel}
		 */
		defaultFields: undefined,

		initComponent: function() {
			this.buildForm();

			this.callParent(arguments);

			if (this.self.WIDGET_NAME) {
				this.WIDGET_NAME = this.self.WIDGET_NAME;
			} else {
				throw 'You must define a WIDGET_NAME in the CMBaseWidgetDefinitionForm subclass';
			}
		},

		/**
		 * Template method, must be implemented in subclasses
		 *
		 * @abstract
		 */
		buildForm: function() {
			this.buttonLabel = Ext.create('CMDBuild.view.common.field.translatable.Text', {
				name: CMDBuild.core.constants.Proxy.LABEL,
				fieldLabel: tr.commonFields.buttonLabel,
				maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false,

				translationFieldConfig: {
					type: CMDBuild.core.constants.Proxy.CLASS_WIDGET,
					owner: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
					identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
					field: CMDBuild.core.constants.Proxy.DESCRIPTION
				}
			});

			this.active = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.core.constants.Proxy.ACTIVE,
				fieldLabel: tr.commonFields.active,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			this.alwaysenabled = Ext.create('Ext.form.field.Checkbox', {
				name: 'alwaysenabled',
				fieldLabel: tr.commonFields.alwaysenabled,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			this.defaultFields = Ext.create('Ext.panel.Panel', {
				frame: true,
				border: true,
				flex: 1,

				layout: {
					type: 'vbox',
					align: 'stretch'
				},

				defaults: {
					maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH
				},

				items: [
					this.buttonLabel,
					this.active,
					this.alwaysenabled
				]
			});

			Ext.apply(this, {
				items: [this.defaultFields]
			});
		},

		disableNonFieldElements: Ext.emptyFn,

		enableNonFieldElements: Ext.emptyFn,

		/**
		 * @return {Object}
		 */
		getWidgetDefinition: function() {
			return {
				type: this.self.WIDGET_NAME,
				label: this.buttonLabel.getValue(),
				active: this.active.getValue(),
				alwaysenabled: this.alwaysenabled.getValue()
			};
		},

		/**
		 * @param {CMDBuild.model.widget.WidgetDefinition} model
		 */
		fillWithModel: function(model) {
			this.buttonLabel.setValue(model.get(CMDBuild.core.constants.Proxy.LABEL));
			this.active.setValue(model.get(CMDBuild.core.constants.Proxy.ACTIVE));
			this.alwaysenabled.setValue(model.get('alwaysenabled'));
		}
	});

})();