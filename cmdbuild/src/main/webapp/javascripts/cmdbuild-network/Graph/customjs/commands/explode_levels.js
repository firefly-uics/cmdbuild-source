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
		var backend = new $.Cmdbuild.g3d.backend.CmdbuildTest();
		this.newElements = [];
		backend.setModel(this.model);
		var justExploded = {};
		this.execute = function(callback, callbackScope) {
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
			if (justExploded[id]) {
				callback.apply(callbackScope, []);
				return;
			}
			else {
				justExploded[id] = true;
			}
			var parentNode = this.model.getNode(id);
			var oldChildren = $.Cmdbuild.g3d.Model.getGraphData(parentNode, "children");
			backend.getANodesBunch(id, this.domainList, function(elements) {
				var newElements = [];
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
				this.newElements = this.newElements.concat(newElements);
				if (levels > 0) {
					var children = this.newElements.slice();
					this.explodeChildren(children, levels - 1, function() {
						callback.apply(callbackScope, []);
					}, this);
				} else {
					callback.apply(callbackScope, []);
					
				}
			}, this);
			
		};
		this.explodeChildren = function(children, levels, callback, callbackScope) {
			if (children.length == 0 || $.Cmdbuild.customvariables.commandsManager.stopped) {
				
				callback.apply(callbackScope, []);
				return;
			}
			var child = children[0];
			children.splice(0, 1);
			this.explodeNode(child, levels, function() {
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
