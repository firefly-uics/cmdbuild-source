(function($) {
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var Selected = function(model) {
		this.observers = [];
		this.model = model;
		this.selected = {};
		this.getData = function() {
			return this.selected;
		};
		this.observe = function(observer) {
			this.observers.push(observer);
		};
		this.changed = function(params) {
			for (var i = 0; i < this.observers.length; i++) {
				this.observers[i].refreshSelected(params);
			}
			$.Cmdbuild.dataModel.dispatchChange("selected");
		};
		this.isSelect = function(nodeId) {
			return (this.selected[nodeId] == true) ? true : false;
		};
		this.length = function() {
			return Object.keys(this.selected).length;
		};
		this.select = function(nodeId, noRefresh) {
			this.selected[nodeId] = true;
			if (! noRefresh) {
				this.changed({});
			}
		};
		this.unSelect = function(nodeId) {
			delete this.selected[nodeId];
			this.changed({});
		};
		this.erase = function() {
			this.selected = {};
		};
		this.selectByClassName =  function(event, className) {
			if (! event.ctrlKey) {
				this.erase();
			}
			var nodes = this.model.getNodes();
			for (var i = 0; i < nodes.length; i++) {
				if ($.Cmdbuild.g3d.Model.getGraphData(nodes[i], "className") == className) {
					if (this.selected[nodes[i].id()] === true && event.ctrlKey) {
						delete this.selected[nodes[i].id()];
					} else {
						this.selected[nodes[i].id()] = true;
					}
				}
			}
			this.changed({});
		};
		this.getCards = function(first, rows) {
			first = parseInt(first);
			rows = parseInt(rows);
			var arClasses = [];
			for (var key in this.selected) {
				var node = this.model.getNode(key);
				var className = $.Cmdbuild.g3d.Model.getGraphData(node, "className");
				var label = $.Cmdbuild.g3d.Model.getGraphData(node, "label");
				var id = node.id();
				arClasses.push({
					id: id,
					className: className,
					label: label
				});
			}
			var retCards = [];
			for (var i = first; i < first + rows && i < arClasses.length; i++) {
				retCards.push(arClasses[i]);
			}
			return {
				total: arClasses.length,
				rows: retCards
			};
		};
	};
	$.Cmdbuild.g3d.Selected = Selected;
	
})(jQuery);