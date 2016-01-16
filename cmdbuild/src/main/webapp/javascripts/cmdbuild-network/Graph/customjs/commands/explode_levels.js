(function($) {
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (! $.Cmdbuild.g3d.commands) {
		$.Cmdbuild.g3d.commands = {};
	}
	var explode_levels = function(model, params) {
		this.model = model;
		this.params = params;
		this.domainList = params.domainList;
		var batch = params.batch;
		var backend = new $.Cmdbuild.g3d.backend.CmdbuildModel();
		backend.setModel(this.model);
		this.execute = function(callback, callbackScope) {
			this.newElements = [];
			var levels = parseInt(params.levels);//$.Cmdbuild.customvariables.options["explosionLevels"]) - 1;
			if ($.Cmdbuild.customvariables.commandsManager.stopped) {
				$.Cmdbuild.customvariables.commandsManager.stopped = false;
				callback.apply(callbackScope, []);
				return;
			}
			this.explodeNode(this.params.id, levels - 1, function() {
				callback.apply(callbackScope, []);
			}, this);
		};
		this.explodeNode = function(id, levels, callback, callbackScope) {
			if ($.Cmdbuild.customvariables.commandsManager.stopped) {
				$.Cmdbuild.customvariables.commandsManager.stopped = false;
				callback.apply(callbackScope, []);
				return;
			}
			var parentNode = this.model.getNode(id);
			var explodedChildren = $.Cmdbuild.g3d.Model.getGraphData(parentNode, "exploded_children");
			var oldChildren = $.Cmdbuild.g3d.Model.getGraphData(parentNode, "children");
			if (explodedChildren) {
				var emptyBunch = { nodes: [], edges: []};
				this.explodeMyChildren(parentNode, emptyBunch, oldChildren, batch, levels, callback, callbackScope);
			} else {
				$.Cmdbuild.g3d.Model.setGraphData(parentNode, "exploded_children", true);
				backend.getANodesBunch(id, this.domainList, function(elements) {
					this.explodeMyChildren(parentNode, elements, oldChildren, batch, levels, callback, callbackScope);
				}, this);
			}
		};
		this.explodeMyChildren = function(parentNode, elements, oldChildren, batch, levels, callback, callbackScope) {
			if ($.Cmdbuild.customvariables.commandsManager.stopped) {
				callback.apply(callbackScope, []);
				return;
			}
			var newElements = [];
			for (var i = 0; i < elements.nodes.length; i++) {
				var childId = elements.nodes[i].data.id;
				if (this.model.getNode(childId).length === 0) {
					this.newElements.push(childId);
				}
			}
			this.model.pushElements(elements);
			for (var i = 0; i < elements.nodes.length; i++) {
				var childId = elements.nodes[i].data.id;
				newElements.push(childId);
			}
			if (oldChildren) {
				newElements = newElements.concat(oldChildren);
			}
			$.Cmdbuild.g3d.Model.setGraphData(parentNode, "children", newElements);
			if (! batch) {
				this.model.changed();	
			}
			if (levels > 0) {
				var children = newElements.slice();
				this.explodeChildren(children, levels, callback, callbackScope);
			} else {
				callback.apply(callbackScope, []);				
			}
		};
//		---------------------------------------------		
		this.explodeChildren = function(children, levels, callback, callbackScope) {
			if (children.length == 0 || $.Cmdbuild.customvariables.commandsManager.stopped) {
				
				callback.apply(callbackScope, []);
				return;
			}
			var child = children[0];
			children.splice(0, 1);
			this.explodeNode(child, levels - 1, function() {
				this.explodeChildren(children, levels, callback, callbackScope);				
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
	$.Cmdbuild.g3d.commands.explode_levels = explode_levels;
})(jQuery);
