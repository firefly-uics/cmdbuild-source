(function() {

	Ext.define("CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm", {
		extend: "Ext.panel.Panel",
		isWidgetDefinition: true,

		statics: {
			WIDGET_NAME: undefined
		},

		initComponent: function() {
			this.buildForm();

			this.callParent(arguments);

			if (this.self.WIDGET_NAME) {
				this.WIDGET_NAME = this.self.WIDGET_NAME;
			} else {
				throw "You must define a WIDGET_NAME in the CMBaseWidgetDefinitionForm subclass";
			}
		},

		// template method, must be implemented in subclasses
		buildForm: function() {
			throw "you must implement buildForm";
		},

		getWidgetDefinition: function() {
			throw "you must implement getWidgetDefinition";
		},

		disableNonFieldElements: Ext.emptyFn,
		enableNonFieldElements: Ext.emptyFn
	});

})();