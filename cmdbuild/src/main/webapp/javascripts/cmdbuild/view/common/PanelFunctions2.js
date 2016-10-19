(function () {

	/**
	 * Service class to be used as mixin for Ext.form.Panel or some methods are compatible also with Ext.panel.Panel
	 *
	 * Specific properties:
	 * 	- {Boolean} considerAsFieldToDisable: enable setDisable function on processed item also if it's not inherits from Ext.form.Field
	 * 	- {Boolean} disableEnableFunctions: disable enable/setDisabled(false) on processed item (ex. cmImmutable)
	 * 	- {Boolean} disablePanelFunctions: disable PanelFunctions class actions on processed item
	 * 	- {Boolean} forceDisabledState: force item to be disabled
	 *
	 * @version 2
	 *
	 * TODO: move to PanelFunctions class on complete refactor
	 */
	Ext.define('CMDBuild.view.common.PanelFunctions2', {

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @param {Object} field
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isManagedField: function (field) {
			return (
				field instanceof Ext.form.Field
				|| field instanceof Ext.form.field.Base
				|| field instanceof Ext.form.field.HtmlEditor
				|| field instanceof Ext.form.FieldContainer
			);
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.includeDisabled
		 *
		 * @returns {Object}
		 */
		panelFunctionDataGet: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.includeDisabled = Ext.isBoolean(parameters.includeDisabled) ? parameters.includeDisabled : false;

			var values = Ext.isFunction(this.getForm) ? this.getForm().getValues() : {};

			if (parameters.includeDisabled) {
				var data = {};

				this.cascade(function (item) {
					if (
						Ext.isObject(item) && !Ext.Object.isEmpty(item)
						&& Ext.isFunction(item.getValue) && Ext.isFunction(item.getName)
						&& this.isManagedField(item)
						&& !item.disablePanelFunctions
					) {
						data[item.getName()] = item.getValue();
					}
				}, this);

				return Ext.apply(values, data);
			}

			return values;
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.includeDisabled
		 * @param {String} parameters.propertyName
		 *
		 * @returns {Mixed}
		 */
		panelFunctionValueGet: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.includeDisabled = Ext.isBoolean(parameters.includeDisabled) ? parameters.includeDisabled : true;

			// Error handling
				if (!Ext.isString(parameters.propertyName) || Ext.isEmpty(parameters.propertyName))
					return _error('panelFunctionValueGet(): unmanaged propertyName parameter', this, parameters.propertyName);
			// END: Error handling

			return this.panelFunctionDataGet({ includeDisabled: parameters.includeDisabled })[parameters.propertyName];
		}
	});

})();
