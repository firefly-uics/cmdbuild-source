(function() {
	Ext.define("CMDBuild.view.administration.widget.form.CMCalendarDefinitionForm", {
		extend: "CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm",

		statics: {
			WIDGET_NAME: ".Calendar"
		},

		// override
		buildForm: function() {
			var me = this,
				widgetName = this.self.WIDGET_NAME,
				tr = CMDBuild.Translation.administration.modClass.widgets[widgetName];

			this.callParent(arguments);

			this.targetClass = new CMDBuild.field.ErasableCombo({
				fieldLabel : tr.fields.target,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : 'targetClass',
				valueField : 'id',
				displayField : 'description',
				editable : false,
				store : _CMCache.getClassesAndProcessesStore(),
				queryMode : 'local'
			});

			this.defaultFields.add(this.targetClass);
		},

		// override
		fillWithModel: function(model) {
			this.callParent(arguments);
			this.targetClass.setValue(model.get("targetClass"));
		},

		//override
		getWidgetDefinition: function() {
			var me = this;

			return Ext.apply(me.callParent(arguments), {
				targetClass: me.targetClass.getValue()
			});
		}
	});
})();