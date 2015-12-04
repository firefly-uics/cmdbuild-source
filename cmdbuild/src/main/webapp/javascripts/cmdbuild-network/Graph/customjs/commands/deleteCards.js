(function($) {
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (! $.Cmdbuild.g3d.commands) {
		$.Cmdbuild.g3d.commands = {};
	}
	var deleteCards = function(model, selected, deleteSelection) {
		this.model = model;
		this.selected = selected;
		var selectedEles = selected.getData();
		this.saved4Undo = [];
		this.execute = function(callback, callbackScope) {
			var me = this;
			//this for give control at the mask to start
//			setTimeout(function() { me.deleteInBackground(deleteSelection, callback, callbackScope); }, 10);
			//			$("#" + "cmdbuilForm").waitMe({
//				effect : 'stretch',
//				color : "#1c94c4"
//			});
			 me.deleteInBackground(deleteSelection, callback, callbackScope);
		};
		this.deleteInBackground = function(deleteSelection, callback, callbackScope) {
			if (deleteSelection == "true") {
				this.deleteSelected(selectedEles);
			}
			else {
				this.deleteUnselected(selectedEles);
			}
			this.model.changed(true);
			callback.apply(callbackScope, []);
			
		};
		this.insertRelations = function(saved) {
			this.model.insertEdge({
				data: {
					source: saved.previousPathNode,
					target: saved.id
				}
			});
			for (var i = 0; saved.children && i < saved.children.length; i++) {
				this.model.insertEdge({
					data: {
						source: saved.id,
						target: saved.children[i]
					}
				});
			}
		};
		this.insertNode = function(saved) {
			var data = {
				className: saved.className,
				id: saved.id,
				label: saved.description,
				color: saved.color,
				faveShape: 'triangle',
				position: saved.position,
				compoundData: {},
				previousPathNode: saved.parentId
			};
			var node = {
					data: data
			};
			return this.model.insertNode(node);
		};
		this.childPosition = function(parentId, id) {
			if (! parentId) {
				return -1;
			}
			var parent = this.model.getNode(parentId);
			var children = $.Cmdbuild.g3d.Model.getGraphData(parent, "children");
			if (! children) {
				console.log("A node with parent that have no children!!!!");
				return -1;
			}
			var index = children.indexOf(id);
			if (index == -1) {
				console.log("A node with parent that have no this child!!!!");
				return -1;
			}
			return index;
		};
		this.save4Undo = function(id) {
			var node = this.model.getNode(id);
			var parentId = $.Cmdbuild.g3d.Model.getGraphData(node, "previousPathNode");
			var childPosition = this.childPosition(parentId, id);
			this.saved4Undo.push({
				id: id,
				parentId: parentId,
				children: $.Cmdbuild.g3d.Model.getGraphData(node, "children"),
				className: $.Cmdbuild.g3d.Model.getGraphData(node, "className"),
				color: $.Cmdbuild.g3d.Model.getGraphData(node, "color"),
				label: $.Cmdbuild.g3d.Model.getGraphData(node, "label"),
				position: node.position(),
				previousPathNode: $.Cmdbuild.g3d.Model.getGraphData(node, "previousPathNode"),
				childPosition: childPosition
			});
		};
		this.undoSingleSaved = function(saved) {
			var children = saved.children;
			for (var i = 0; children && i < children.length; i++) {
				var child = this.model.getNode(children[i]);
				$.Cmdbuild.g3d.Model.setGraphData(child, "previousPathNode", saved.id);
			}
			var cyNode = this.insertNode(saved);
			$.Cmdbuild.g3d.Model.setGraphData(cyNode, "children", children);
			this.insertRelations(saved);
		};
		this.undo = function() {
			var saved = this.saved4Undo.pop();
			while (saved) {
				this.undoSingleSaved(saved);
				if (saved.parentId) {
					var parent = this.model.getNode(saved.parentId);
					var parentChildren = $.Cmdbuild.g3d.Model.getGraphData(parent, "children");
					if (! parentChildren) {
						parentChildren = [];
					}
					parentChildren.splice(saved.childPosition, 0, saved.id);
					var node = this.model.getNode(saved.id);
					$.Cmdbuild.g3d.Model.setGraphData(parent, "children", parentChildren);
					$.Cmdbuild.g3d.Model.setGraphData(node, "previousPathNode", saved.parentId);
				}
				saved = this.saved4Undo.pop();
			}
			this.model.changed();
		};
		this.deleteSelected = function(selectedEles) {
			$.Cmdbuild.customvariables.viewer.clearSelection();
			for (var key in selectedEles) {
				this.save4Undo(key);
				this.model.remove(key);
			}
		};
		this.deleteUnselected = function(selectedEles) {
			var nodes = this.model.getNodes();
			for (var i = 0; i < nodes.length; i++) {
				var key = nodes[i].id();
				if (! selectedEles[key]) {
					this.save4Undo(key);
					this.model.remove(key);
				}
			}
		};
	};
	$.Cmdbuild.g3d.commands.deleteCards = deleteCards;
})(jQuery);
