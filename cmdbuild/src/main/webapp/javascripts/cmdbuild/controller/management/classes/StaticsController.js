(function() {

	Ext.define('CMDBuild.controller.management.classes.StaticsController', {

		singleton: true,

		/**
		 * @param {Ext.form.Basic} form
		 *
		 * @returns {String or null} out
		 */
		getInvalidAttributeAsHTML: function(form) {
			var invalidFields = CMDBuild.controller.management.classes.StaticsController.getInvalidField(form);
			var out = null;

			if (!Ext.Object.isEmpty(invalidFields) && Ext.isObject(invalidFields)) {
				out = '';

				Ext.Object.each(invalidFields, function(name, field, myself) {
					if (!Ext.isEmpty(field)) {
						var fieldLabel = field.getFieldLabel();

						// Strip label required flag
						if (fieldLabel.indexOf('* ') == 0)
							fieldLabel = fieldLabel.replace('* ', '');

						out += '<li>' + fieldLabel + '</li>';
					}
				}, this);

				out = '<ul>' + out + '</ul>';
			}

			return out;
		},

		/**
		 * @param {Ext.form.Basic} form
		 *
		 * @returns {Object} invalidFieldsMap
		 *
		 * @private
		 */
		getInvalidField: function(form) {
			var fieldsArray = form.getFields().getRange();
			var invalidFieldsMap = {};

			if (!Ext.isEmpty(fieldsArray) && Ext.isArray(fieldsArray))
				Ext.Array.each(fieldsArray, function(field, i, allFields) { // Validates all fields (display panel fields and edit panel fields)
					if (Ext.isFunction(field.isValid) && !field.isValid()) {
						invalidFieldsMap[field.name] = field;
					} else if (!Ext.isEmpty(invalidFieldsMap[field.name])) {
						delete invalidFieldsMap[field.name];
					}
				}, this);

			return invalidFieldsMap;
		}
	});

})();