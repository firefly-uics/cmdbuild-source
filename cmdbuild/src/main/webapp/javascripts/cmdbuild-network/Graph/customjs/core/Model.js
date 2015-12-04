(function($) {
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var Model = function() {
		this.observers = [];
		this.erase = function() {
			cy.remove("*");		
		};
		this.erase();
		this.doLayout = function(options) {
			this.layout.clean();
			this.changed();
			//this.layout.layoutPositions(cy.nodes());			
		};
		this.changeLayout = function(options) {
			this.layout = cy.elements().makeLayout(options);
			this.layout.run();			
		};
		this.observe = function(observer) {
			this.observers.push(observer);
		};
		this.changed = function(params) {
			for (var i = 0; i < this.observers.length; i++) {
				this.observers[i].refresh(params);
			}
			$.Cmdbuild.dataModel.dispatchChange("viewer");
		};
		this.nodesLength = function() {
			return cy.nodes().length;
		};
		this.edgesLength = function() {
			return cy.edges().length;
		};
		this.getNodes = function() {
			return cy.nodes();
		};
		this.getNodesFromIdsArray = function(idsArray) {
			var nodes = [];
			for (var key in idsArray) {
				var node = this.getNode(key);
				nodes.push(node);
			}
			return nodes;
		};
		this.getEdges = function() {
			return cy.edges();
		};
		this.connectedEdges = function(idNode) {
			var source = this.getNode(idNode);
			var nodes = (cy.collection([source]).connectedEdges());
			return nodes;
		};
		this.getNode = function(id) {
			return cy.getElementById(id	);
		};
		this.insertNode = function(node) {
			var cyNode = cy.add({
			    group: "nodes",
			    data: {
		    		id : node.data.id,
		    		className: node.data.className,
		    		label: node.data.label,
		    		color: node.data.color,
		    		compoundData: node.data.compoundData,
		    		children: node.data.children,
		    		previousPathNode: node.data.previousPathNode
			    },
			    position: { x: node.data.position.x, y: node.data.position.y, z: node.data.position.z }
			});
			return cyNode;
		};
		this.insertEdge = function(edge) {
			var exist = cy.edges("[source='" + edge.data.source + "'][target='" + edge.data.target + "']");
			if (exist.length > 0) {
				return;
			}
			var exist = cy.edges("[target='" + edge.data.source + "'][source='" + edge.data.target + "']");
			if (exist.length > 0) {
				return;
			}
			cy.add({
			    group: "edges",
			    data: {source: edge.data.source, target: edge.data.target, label: edge.data.label },
			});
		};
		this.modifyPosition = function(id, value) {
			var node = this.getNode(id);
			node.position({
				x: value.x,
				y: value.y,
				z: value.z
			});
		};
		this.removeParentFromOpenChildren = function(children) {
			if (! children) {
				return;
			}
			for (var i = 0; i < children.length; i++) {
				var node = this.getNode(children[i]);
				$.Cmdbuild.g3d.Model.removeGraphData([node], "previousPathNode");
			}
		};
		this.removeOpenChildren = function(parentId, id) {
			var node = this.getNode(parentId);
			var children = $.Cmdbuild.g3d.Model.getGraphData(node, "children");//node.data("children");
			var newChildren = [];
			if (! children) {
				return;
			}
			for (var j = 0; j < children.length; j++) {
				var childId = children[j];
				if (childId != id) {
					newChildren.push(childId);
				}
			}
			$.Cmdbuild.g3d.Model.setGraphData(node, "children", newChildren);
		};
		this.remove = function(id) {
			var node = this.getNode(id);
			var parentId = $.Cmdbuild.g3d.Model.getGraphData(node, "previousPathNode");
			var children = $.Cmdbuild.g3d.Model.getGraphData(node, "children");
			this.removeParentFromOpenChildren(children);
			if (parentId) {
				this.removeOpenChildren(parentId, id);
			}
			cy.remove("node#" + id);
		};
		this.pushElements = function(elements) {
			for (var i = 0; i < elements.nodes.length; i++) {
				var node = elements.nodes[i];
				this.insertNode(node);
			}
			for (var i = 0; i < elements.edges.length; i++) {
				var edge = elements.edges[i];
				this.insertEdge(edge);
			}
		};
		this.getDistinctClasses = function() {
			this.classes = {};
			var nodes = this.getNodes();
			for (var i = 0; i < nodes.length; i++) {
				var node = nodes[i];
				var className = $.Cmdbuild.g3d.Model.getGraphData(node, "className");
				if (! this.classes[className]) {
					this.classes[className] = {
							className: className,
							qt: 1
					}
				}
				else {
					this.classes[className].qt += 1;
				}
			}
			var arClasses = [];
			for (var key in this.classes) {
				arClasses.push(this.classes[key]);
			}
			var retCards = [];
			for (var i = 0; i < arClasses.length; i++) {
				retCards.push(arClasses[i]);
			}
			return {
				total: retCards.length,
				rows: retCards
			};
		};
		this.getCards = function(first, rows, filter) {
			first = parseInt(first);
			rows = parseInt(rows);
			var arClasses = [];
			var nodes = this.getNodes();
			for (var i = 0; i < nodes.length; i++) {
				var node = nodes[i];
				var className = $.Cmdbuild.g3d.Model.getGraphData(node, "className");
				var label = $.Cmdbuild.g3d.Model.getGraphData(node, "label");
				if (filter.query && label.toLowerCase().indexOf(filter.query) < 0) {
					continue;
				}
				var id = node.id();
				arClasses.push({
					id: id,
					className: className,
					label: label
				});
			}
			var retCards = [];
			if (first >= arClasses.length) {
				first = 0;
			}
			for (var i = first; i < first + rows && i < arClasses.length; i++) {
				retCards.push(arClasses[i]);
			}
			return {
				total: arClasses.length,
				rows: retCards
			};
		};
		this.getNodesByClassName = function(className) {
			return cy.filter(function(i, element){
				  if( element.isNode() && $.Cmdbuild.g3d.Model.getGraphData(element, "className") == className ){
				    return true;
				  }
				  return false;
				});
		};
		this.getChildrenByClassName = function(node, className) {
			return $.Cmdbuild.g3d.Model.getChildrenByFunct(node, function(element) {
				return ($.Cmdbuild.g3d.Model.getGraphData(element, "className") == className);
			});
		};
		this.collection = function() {
			return cy.collection();
		};
		this.kruskal = function(params) {
			return cy.elements().kruskal(params);
		};
		this.aStar = function(params) {
			return cy.elements().aStar(params);
		};
		this.dijkstra = function(params) {
			return cy.elements().dijkstra(params);
		};
		this.depthFirstSearch = function(params) {
			return cy.elements().breadthFirstSearch(params);
		};
	};
	$.Cmdbuild.g3d.Model = Model;

	$.Cmdbuild.g3d.Model.getChildrenByFunct = function(node, f) {
		var nodes = node.children();
		var children2Return = [];
		for (var i = 0; i < nodes.length; i++) {
			var element = nodes[i];
			if (f(element)) {
				children2Return.push(element);
			}
		}
		return children2Return;
		
	};
	$.Cmdbuild.g3d.Model.removeGraphData = function(nodes, key) {
		for (var i = 0; i < nodes.length; i++) {
			delete nodes[i]._private.data[key];
		}
	};
	$.Cmdbuild.g3d.Model.getGraphData = function(node, key) {
		return (! (node._private && node._private.data)) ? undefined : node._private.data[key];
	};
	$.Cmdbuild.g3d.Model.setGraphData = function(node, key, value) {
		if (! (node._private && node._private.data)) {
			node.data(key, value);
		}
		else {
			node._private.data[key] = value;
		}
	};

	
})(jQuery);
