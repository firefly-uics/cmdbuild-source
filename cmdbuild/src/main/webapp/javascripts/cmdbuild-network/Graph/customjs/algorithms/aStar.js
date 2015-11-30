(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (!$.Cmdbuild.g3d.algorithms) {
		$.Cmdbuild.g3d.algorithms = {};
	}
	var aStar = function(model, selected) {
		this.model = model;
		var selectedEles = selected.getData();
		this.execute = function() {
			var nodes = this.model.getNodesFromIdsArray(selectedEles);
			if (nodes.length != 2) {
				alert("aStar works on two parameters");
				return;
			}
			var path = this.model.aStar({
				root: 'node#' + nodes[0].id(),
				goal: 'node#' + nodes[1].id()
			});
			selected.erase();
			for (var i = 0; i < path.path.length; i++) {
				if (path.path[i].group() === 'nodes') {
					selected.select(path.path[i].id(), true);
				}
			}
			selected.changed();
		};
		this.execute();
	};
	$.Cmdbuild.g3d.algorithms.aStar = aStar;
})(jQuery);
