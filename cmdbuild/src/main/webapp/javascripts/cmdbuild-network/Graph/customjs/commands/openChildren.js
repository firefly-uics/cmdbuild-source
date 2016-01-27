(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (!$.Cmdbuild.g3d.commands) {
		$.Cmdbuild.g3d.commands = {};
	}
	var openChildren = function(model, params) {
		this.model = model;
		this.params = params;
		this.elements = params.elements;
		this.parentNode = undefined;
		this.compoundNode = undefined;
		var backend = new $.Cmdbuild.g3d.backend.CmdbuildModel();
		this.newElements = [];
		backend.setModel(this.model);
		this.newNodes = [];
		this.newEdges = [];
		this.compoundSavedNode = {
			node: {},
			edge: {}
		};
		this.execute = function(callback, callbackScope) {
			this.compoundNode = this.model.getNode(this.params.id);
			this.compoundEdge = this.model.connectedEdges(this.params.id)[0];
			this.saveForUndoCompoundNode(this.compoundNode, this.compoundEdge);
			var parentId = $.Cmdbuild.g3d.Model.getGraphData(this.compoundNode,
					"previousPathNode");
			this.parentNode = this.model.getNode(parentId);
			this.oldChildren = $.Cmdbuild.g3d.Model.getGraphData(
					this.parentNode, "children");
			backend.openChildren(this.params.id, this.elements, function(
					elements) {
				this.newElements = [];
				for (var i = 0; i < elements.nodes.length; i++) {
					var childId = elements.nodes[i].data.id;
					if (this.model.getNode(childId).length === 0) {
						this.newElements.push(childId);
					}
				}
				this.saveForUndo(elements);
				this.model.pushElements(elements);
				var allChildren = [];
				if (this.oldChildren) {
					allChildren = this.newElements.concat(this.oldChildren);
				}
				$.Cmdbuild.g3d.Model.setGraphData(this.parentNode, "children",
						allChildren);
				this.model.cleanCompoundNode(this.compoundNode);
				this.model.changed();
				callback.apply(callbackScope, []);
			}, this);
		};
		this.undo = function() {
			for (var i = 0; i < this.newEdges.length; i++) {
				var edge = this.newEdges[i];
				this.model.removeEdge({
					source: edge.data.source,
					target: edge.data.target,
					label: edge.data.label
				});
			}
			for (var i = 0; i < this.newNodes.length; i++) {
				var id = this.newNodes[i];
				this.model.remove(id);
			}
			this.model.pushElements({
				nodes: [{
					data: this.compoundSavedNode.node
				}],
				edges: [{
					data: this.compoundSavedNode.edge
				}]
			});
			var children = $.Cmdbuild.g3d.Model.getGraphData(this.parentNode,
					"children");
			children.push(this.compoundSavedNode.node.id);

			// if (this.batch !== true) {
			this.model.changed(true);
			// }
		};
		this.saveForUndoCompoundNode = function(node, edge) {
			var data = {
				className: $.Cmdbuild.g3d.Model.getGraphData(node, "className"),
				id: node.id(),
				label: $.Cmdbuild.g3d.Model.getGraphData(node, "label"),
				color: "#ff0000",
				faveShape: 'triangle',
				position: {
					x: Math.random() * 1000 - 500,
					y: Math.random() * 600 - 300,
					z: 200
				// Math.random() * 800 - 400
				},
				compoundData: $.Cmdbuild.g3d.Model.getGraphData(node,
						"compoundData"),
				previousPathNode: $.Cmdbuild.g3d.Model.getGraphData(node,
						"previousPathNode")
			};
			var node = data;

			var data = {};
			if (edge) {
				data = {
					source: edge.source().id(),
					target: edge.target().id(),
					label: $.Cmdbuild.g3d.Model.getGraphData(node, "label"),
					color: $.Cmdbuild.custom.configuration.edgeColor,
					strength: 90
				}
			};
			var edge = data;
			this.compoundSavedNode = {
				node: node,
				edge: edge
			};
		};
		this.saveForUndo = function(elements) {
			for (var i = 0; i < elements.nodes.length; i++) {
				var childId = elements.nodes[i].data.id;
				if (this.model.getNode(childId).length === 0) {
					this.newNodes.push(childId);
				}
			}
			for (var i = 0; i < elements.edges.length; i++) {
				var edge = elements.edges[i];
				if (this.model.getEdge({
					source: edge.data.source,
					target: edge.data.target,
					label: edge.data.label
				}).length === 0) {
					this.newEdges.push(edge);
				}
			}
		};
		this.chargeModel = function(node, edge) {
			var elements = {
				nodes: [{
					data: this.compoundData
				}],
				edges: [{
					data: this.compoundEdgeData
				}]
			};
			return elements;
		};
	};
	$.Cmdbuild.g3d.commands.openChildren = openChildren;
})(jQuery);