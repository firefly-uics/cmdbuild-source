(function() {

	Ext.define('CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController', {
		extend: 'Ext.Base',

		mixins: {
			observable: 'Ext.util.Observable'
		},

		/**
		 * @param {Object} config
		 */
		constructor: function(config) {
			this.callParent(arguments);

			Ext.apply(this, config);

			if (Ext.isEmpty(this.self.WIDGET_NAME)) {
				throw 'You have to set the static WIDGET_NAME property to the controller implementation';
			} else {
				this.WIDGET_NAME = this.self.WIDGET_NAME;
			}

			if (Ext.isEmpty(this.view))
				throw 'You have to pass a view when create an instance of ' + this.$className;
		},

		afterEnableEditing: Ext.emptyFn,

		disableNonFieldElements: Ext.emptyFn,

		enableNonFieldElements: Ext.emptyFn,

		/**
		 * @param {CMDBuild.model.widget.WidgetDefinition} model
		 */
		fillFormWithModel: function(model) {
			if (!Ext.isEmpty(model) && model.$className == 'CMDBuild.model.widget.WidgetDefinition')
				this.view.fillWithModel(model);
		},

		setDefaultValues: function() {
			this.view.active.setValue(true);
		}
	});

})();