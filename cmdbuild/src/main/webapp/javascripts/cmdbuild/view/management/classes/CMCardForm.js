(function() {
	Ext.define("CMDBuild.view.management.classes.CMCardForm", {
		extend: "Ext.form.Panel",

		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},
		
		// a card is a Ext.data.Record or a id to laod
		loadCard: function(card, idClass) {
			this.reset();
			if (!card) { return; }

			if (typeof card == "object") {
				_fillWithData.call(this, card.raw);
			} else {
				CMDBuild.ServiceProxy.card.get({
					params: {
						Id: card,
						IdClass: idClass
					},
					scope: this,
					success: function(a,b, response) {
						_fillWithData.call(this, response.card, response.referenceAttributes);
					}
				});
			}
		},
		
		getInvalidField: function() {
			var fields = this.getForm().items;
			var invalid = [];
			fields.each(function(field) {
				if (!field.isValid()) {
					invalid.push(field);
				}
			});
			
			return invalid;
		},

		hasDomainAttributes: function() {
			var fields = this.getForm().getFields().items;

			for (var i=0, l=fields.length; i<l; ++i) {
				if (fields[i].cmDomainAttribute) {
					return true;
				}
			};

			return false;
		},

		getInvalidAttributeAsHTML: function() {
			var fields = this.getInvalidField();
			if (fields.length == 0) {
				return null;
			} else {
				var out = "<ul>";
				for (var i=0, l=fields.length; i<l; ++i) {
					var attribute = fields[i].CMAttribute;
					var item="";
					if (attribute.group) {
						item = attribute.group + " - ";
					}
					out += "<li>" + item + attribute.description + "</li>";
				}
				
				return out+"</ul>";
			}
		},
		
		toString: function() {
			return "CMCardForm";
		}
	});

	function _fillWithData(data, referenceAttributes) {
		var fields = this.getForm().getFields();
		addReferenceAttrsToData(data, referenceAttributes);

		if (fields) {
			fields.each(function(f) {
				try {
					f.setValue(data[f.name]);
				} catch (e){
					_debug("I can not set the value for " + f.name);
				}
			});
		}
	}

	function addReferenceAttrsToData(data, referenceAttributes) {
		for (var referenceName in referenceAttributes || {}) {
			var attributes = referenceAttributes[referenceName];
			
			for (var attributeName in attributes) {
				var fullName = "_" + referenceName + "_" + attributeName,
					value = attributes[attributeName];

				data[fullName] = value;
			}
		}
	}
})();