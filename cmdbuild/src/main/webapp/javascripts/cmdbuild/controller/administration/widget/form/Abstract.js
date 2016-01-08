(function() {

	Ext.define('CMDBuild.controller.administration.widget.form.Abstract', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.Widget}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 *
		 * @private
		 */
		definitionModelName: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.widget.Widget} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			if (Ext.isEmpty(this.definitionModelName) || !Ext.isString(this.definitionModelName))
				_error('widgetDefinitionModelName property not configured', this);
		},

		classTabWidgetAdd: function() {
			this.view.reset();
			this.view.setDisabledModify(false, true);
			this.view.loadRecord(Ext.create(this.classTabWidgetDefinitionModelNameGet()));
		},

		// DefinitionModelName property methods
			/**
			 * @returns {String}
			 */
			classTabWidgetDefinitionModelNameGet: function() {
				if (!this.classTabWidgetDefinitionModelNameIsEmpty())
					return this.definitionModelName;

				return '';
			},

			/**
			 * @returns {Boolean}
			 */
			classTabWidgetDefinitionModelNameIsEmpty: function() {
				return (
					Ext.isEmpty(this.definitionModelName)
					|| !Ext.isString(this.definitionModelName)
				);
			},

			/**
			 * @param {String} modelName
			 */
			classTabWidgetDefinitionModelNameSet: function(modelName) {
				if (!Ext.isEmpty(modelName) && Ext.isString(modelName))
					this.definitionModelName = modelName;
			}
	});

})();