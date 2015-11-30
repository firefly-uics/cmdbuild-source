(function($) {
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (! $.Cmdbuild.g3d.algorithms) {
		$.Cmdbuild.g3d.algorithms = {};
	}
	var dijkstra = function(model, selected) {
		this.model = model;
		var selectedEles = selected.getData();
		this.execute = function() {
			var nodes = this.model.getNodes();
			var eles = this.model.collection();
			for (var key in selectedEles) {
				eles.add(this.model.getNode(key));
			}
			var path = this.model.dijkstra(eles[0]);
			console.log("dijkstra ", path);
//		    selected.erase();
//		    for (var i = 0; i < path.path.length; i++) {
//			    if (path.path[i].group() === 'nodes') {
//			    	selected.select(path.path[i].id());
//			    }
//		    }
//			selected.changed();
		};
		this.execute();
	};
	$.Cmdbuild.g3d.algorithms.dijkstra = dijkstra;
})(jQuery);
