(function() {

	var tr = CMDBuild.Translation.administration.modClass.widgets;

	Ext.define('CMDBuild.view.administration.widget.form.CMCreateModifyCardDefinitionForm', {
		extend: 'CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		statics: {
			WIDGET_NAME: '.CreateModifyCard'
		},

		/**
		 * @property {Ext.panel.Panel}
		 */
		defaultFields: undefined,

		initComponent: function() {
			this.callParent(arguments);
		},

		/**
		 * @override
		 */
		buildForm: function() {
			var me = this;

			this.callParent(arguments);

			this.targetClass = Ext.create('CMDBuild.field.ErasableCombo', {
				fieldLabel: tr[this.self.WIDGET_NAME].fields.target,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.core.proxy.CMProxyConstants.TARGET_CLASS,
				valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
				displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				editable: false,

				validator: function(value) {
					if (Ext.isEmpty(me.targetClass.getValue()) && Ext.isEmpty(me.cqlText.getValue()))
						return CMDBuild.Translation.errors.targetClassOrCqlFilterRequired;

					return true;
				},

				store: _CMCache.getClassesAndProcessesStore(),
				queryMode: 'local'
			});

			this.cqlText = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr[this.self.WIDGET_NAME].fields.cql,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name: 'idcardcqlselector',

				validator: function(value) {
					if (Ext.isEmpty(me.targetClass.getValue()) && Ext.isEmpty(me.cqlText.getValue()))
						return CMDBuild.Translation.errors.targetClassOrCqlFilterRequired;

					return true;
				}
			});

			this.readOnlyCheck = Ext.create('Ext.form.field.Checkbox', {
				name: 'readonly',
				fieldLabel: tr[this.self.WIDGET_NAME].fields.readOnly,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			this.defaultFields.add(this.readOnlyCheck, this.targetClass, this.cqlText);
		},

		/**
		 * @param {CMDBuild.model.widget.WidgetDefinition} model
		 *
		 * @override
		 */
		fillWithModel: function(model) {
			this.callParent(arguments);

			this.targetClass.setValue(model.get(CMDBuild.core.proxy.CMProxyConstants.TARGET_CLASS));
			this.cqlText.setValue(model.get('idcardcqlselector'));
			this.readOnlyCheck.setValue(model.get('readonly'));
		},

		/**
		 * @override
		 */
		getWidgetDefinition: function() {
			return Ext.apply(this.callParent(arguments), {
				targetClass: this.targetClass.getValue(),
				idcardcqlselector: this.cqlText.getValue(),
				readonly: this.readOnlyCheck.getValue()
			});
		}
	});

})();