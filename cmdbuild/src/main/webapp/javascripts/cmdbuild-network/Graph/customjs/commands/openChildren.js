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
		this.parentNode = undefined;
		this.compoundNode = undefined;
		var backend = new $.Cmdbuild.g3d.backend.CmdbuildModel();
		this.newElements = [];
		backend.setModel(this.model);
		this.execute = function(callback, callbackScope) {
			this.compoundNode = this.model.getNode(this.params.id);
			this.compoundEdge = this.model.connectedEdges(this.params.id)[0];
			var parentId = $.Cmdbuild.g3d.Model.getGraphData(this.compoundNode, "previousPathNode"); 
			this.parentNode = this.model.getNode(parentId);
			this.oldChildren = $.Cmdbuild.g3d.Model.getGraphData(this.parentNode, "children");
			this.saveNodeData(this.compoundNode);
			this.saveEdgeData(this.compoundNode, this.compoundEdge);
			backend.openChildren(this.params.id, this.elements, function(elements) {
				this.newElements = [];
				for (var i = 0; i < elements.nodes.length; i++) {
					var childId = elements.nodes[i].data.id;
					if (this.model.getNode(childId).length === 0) {
						this.newElements.push(childId);
					}
				}
				this.model.pushElements(elements);
				var allChildren = [];
				if (this.oldChildren) {
					allChildren = this.newElements.concat(this.oldChildren);
				}
				$.Cmdbuild.g3d.Model.setGraphData(this.parentNode, "children", allChildren);
				this.model.cleanCompoundNode(this.compoundNode);
				this.model.changed();	
				callback.apply(callbackScope, []);
			}, this);
		};
		this.undo = function() {
			for (var i = 0; i < this.newElements.length; i++) {
				var id = this.newElements[i];
				this.model.remove(id);
			}
			$.Cmdbuild.g3d.Model.setGraphData(this.parentNode, "children", this.oldChildren);
			var newNode = this.chargeModel(this.compoundNode, this.compoundEdge);
			this.model.pushElements(newNode);
			if (this.batch !== true) {
				this.model.changed(true);
			}
		};
		this.saveNodeData = function(node) {
			var sourceId = this.parentNode.id();
			var targetId = node.id();
			this.compoundData = {
					className: $.Cmdbuild.g3d.Model.getGraphData(node, "className"),
					id: targetId,
					label: $.Cmdbuild.g3d.Model.getGraphData(node, "label"),
					color: "#ff0000",
					faveShape: 'triangle',
					position: {x: 0, y: 0, z:0},
					compoundData: $.Cmdbuild.g3d.Model.getGraphData(node, "compoundData").slice(),
					previousPathNode: sourceId
			}
		};
		this.saveEdgeData = function(node, edge) {
			var sourceId = this.parentNode.id();
			var targetId = node.id();
			this.compoundEdgeData = {
					source: sourceId,
					target: targetId,
					id: edge.id(),
					label: $.Cmdbuild.g3d.Model.getGraphData(edge, "label"),
					color: $.Cmdbuild.g3d.colors.edge,
					strength: 90
				};
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
