(function($) {
	var CqlTest =  {
		saveMethod: $.Cmdbuild.widgets.SAVEONCARD,
		save: function(form, widget) {
			return {};
		},
		formName: function(form, widget) {
			var name = form + "_" + widget._id + "cqlTest";
			return name;
		},
		cleanData: function(form, widget) {
			var name = this.formName(form, widget);
			$.Cmdbuild.dataModel.cleanForm(name);
		}
	};
	$.Cmdbuild.widgets.CqlTest = CqlTest;
}) (jQuery);