(function($) {
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (! $.Cmdbuild.g3d.commands) {
		$.Cmdbuild.g3d.commands = {};
	}
	var openChildren = function(model, params) {
		this.model = model;
		this.params = params;
		this.elements = params.elements;
		var backend = new $.Cmdbuild.g3d.backend.CmdbuildTest();
		this.newElements = [];
		backend.setModel(this.model);
		this.execute = function(callback, callbackScope) {
			var compoundNode = this.model.getNode(this.params.id);
			var parentId = $.Cmdbuild.g3d.Model.getGraphData(compoundNode, "previousPathNode"); 
			var parentNode = this.model.getNode(parentId);
			var oldChildren = $.Cmdbuild.g3d.Model.getGraphData(parentNode, "children");
			backend.openChildren(this.params.id, this.elements, function(elements) {
				this.model.pushElements(elements);
				for (var i = 0; i < elements.nodes.length; i++) {
					var childId = elements.nodes[i].data.id;
					this.newElements.push(childId);
				}
				if (oldChildren) {
					this.newElements = this.newElements.concat(oldChildren);
				}
				$.Cmdbuild.g3d.Model.setGraphData(parentNode, "children", this.newElements);
				this.model.changed();	
				callback.apply(callbackScope, []);
			}, this);
		};
		this.undo = function() {
			for (var i = 0; i < this.newElements.length; i++) {
				var id = this.newElements[i];
				this.model.remove(id);
			}
			if (this.batch !== true) {
				this.model.changed(true);
			}
		};
	};
	$.Cmdbuild.g3d.commands.openChildren = openChildren;
})(jQuery);
