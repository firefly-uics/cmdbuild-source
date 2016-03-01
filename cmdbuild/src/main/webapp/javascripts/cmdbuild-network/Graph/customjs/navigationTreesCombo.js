(function($) {
	var navigationTreesCombo = function() {
		var control = "navigationTrees";
		this.chargeCombo = function(classId) {
			var options = [];
			var selectMenu = $("#" + control);
			var values = $.Cmdbuild.customvariables.cacheTrees.getTreesFromClass(classId);
			if (values.length == 0) {
				options.push("<option></option>");
			}
			for (var i = 0; i < values.length; i++) {
				var id = values[i]._id;
				var val = values[i].description;
				options.push("<option value='" + id + "'>" + val + "</option>");
			}
			selectMenu.empty();
			selectMenu.append(options).selectmenu();
			selectMenu.selectmenu("refresh");
		};
		this.refreshSelected = function() {
			var current = $.Cmdbuild.customvariables.selected.getCurrent();
			if (current) {
				var currentNode = $.Cmdbuild.customvariables.model.getNode(current);
				var currentClassId = $.Cmdbuild.g3d.Model.getGraphData(currentNode, "classId");
				this.chargeCombo(currentClassId);
			}
		};
		$.Cmdbuild.customvariables.selected.observe(this);
	};
	$.Cmdbuild.g3d.navigationTreesCombo = navigationTreesCombo;
})(jQuery);
