(function() {

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