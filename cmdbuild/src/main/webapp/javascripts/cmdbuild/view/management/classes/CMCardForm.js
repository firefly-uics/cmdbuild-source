(function() {
	Ext.define("CMDBuild.view.management.classes.CMCardForm", {
		extend: "Ext.form.Panel",

		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		loadCard: function(card) { // a card is a Ext.data.Record
			this.reset();
			if (!card) { return; }

			var data = card.raw;
			var fields = this.getForm().getFields();
			
			if (fields) {
				fields.each(function(f) {
					f.setValue(data[f.name]);
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

})();