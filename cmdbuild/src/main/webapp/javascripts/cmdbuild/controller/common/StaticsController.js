(function() {

	Ext.define('CMDBuild.controller.common.WorkflowStaticsController', {

		singleton: true,

		/**
		 * Iterates all process attributes and take only ones defined as variables for this step
		 *
		 * @param {Array} attributes
		 * @param {Array} variables
		 * 		Structure: {
		 * 			{String} name
		 * 			{Boolean} mandatory
		 * 			{Boolean} writable
		 * 		}
		 *
		 * @returns {Array} out
		 */
		filterAttributesInStep: function(attributes, variables) {
			var out = [];

			if (
				!Ext.isEmpty(attributes) && Ext.isArray(attributes)
				&& !Ext.isEmpty(variables) && Ext.isArray(variables)
			) {
				Ext.Array.each(variables, function(variableObject, i, allVariableObjects) {
					Ext.Array.each(attributes, function(attributeObject, i, allAttributeObjects) {
						if (attributeObject['name'] == variableObject['name']) {
							attributeObject['isnotnull'] = variableObject['mandatory'];
							attributeObject['fieldmode'] = variableObject['writable'] ? 'write' : 'read';

							out.push(attributeObject);

							return false;
						}
					}, this);
				}, this);
			}

			return out;
		}
	});

	Ext.define('CMDBuild.controller.common.CardStaticsController', {
		statics: {
			getInvalidField: function(cmForm) {
				var fields = cmForm.getFields(),
					invalid = [];

				fields.each(function(field) {
					if (!field.isValid()) {
						invalid.push(field);
					}
				});

				return invalid;
			},
			getInvalidAttributeAsHTML: function(cmForm) {
				var fields = CMDBuild.controller.common.CardStaticsController.getInvalidField(cmForm);
				var alreadyAdded = {};

				if (fields.length == 0) {
					return null;
				} else {
					var out = '<ul>';
					for (var i=0, l=fields.length; i<l; ++i) {
						var attribute = fields[i].CMAttribute;
						var item='';
						if (attribute) {
							if (alreadyAdded[attribute.description]) {
								continue;
							} else {
								alreadyAdded[attribute.description] = true;
								if (attribute.group) {
									item = attribute.group + ' - ';
								}
								out += '<li>' + item + attribute.description + '</li>';
							}
						}
					}

					return out+'</ul>';
				}
			}
		}
	});

})();