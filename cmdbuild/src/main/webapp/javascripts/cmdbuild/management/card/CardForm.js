(function() {
	CMDBuild.Management.CardForm = Ext.extend(Ext.form.FormPanel, {		
		border: false,
		frame: false,		
		plugins: [new CMDBuild.FieldSetAddPlugin(), new CMDBuild.FormPlugin(), new CMDBuild.CallbackPlugin()],
		autoScroll: true,
		
		//customFunctions
		activeSubpanel: function(subpanelId) {
			this.getLayout().setActiveItem(subpanelId);
		},
		
		loadCard: function(card) { // a card is a Ext.data.Record
			this.clearForm();
			if (!card) {
				return;
			}
			var data = card.data;
			var mapOfFieldValues = this.getForm().getFieldValues();
			if (mapOfFieldValues) {
				for (var fieldName in mapOfFieldValues) {
					var fields = this.find("name", fieldName);
					for (var i=0, l=fields.length; i<l; ++i) {
						var f = fields[i];
						var value;
						if (f.hiddenName) {
							value = data[f.hiddenName];
						} else {
							value = data[fieldName];
						}
						f.setValue(value);
					}
				}
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
		}
	});
	Ext.reg('cardform', CMDBuild.Management.CardForm);
})();