(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var navigationManager = function(tree) {
		this.tree = tree;
		this.getNode = function(id) {
			for (var i = 0; i < this.tree.nodes.length; i++) {
				var node = this.tree.nodes[i];
				if (node._id === id) {
					return node;
				}
			}
			return null;
		};
		this.getChildren = function(id) {
			var nodes = [];
			for (var i = 0; i < this.tree.nodes.length; i++) {
				var node = this.tree.nodes[i];
				if (node.parent === id) {
					nodes.push(node);
				}
			}
			return nodes;
		};
		this.getChildOfClass = function(node, classId) {
			var children = this.getChildren(node._id);
			for (var i = 0; i < children.length; i++) {
				if (this.sameClass(children[i].metadata.targetClass, classId)) {
					return children[i];
				}
			}
			return null;
		};
		this.calculatePathFromNode = function(path, node, originalPath) {
			for (var i = originalPath.length - 1; i > 0; i--) {
				node = this.getChildOfClass(node, originalPath[i - 1].classId);
			}
			return (node) ? this.getChildren(node._id) : null;
		};

		this.sameClass = function(superClass, currentClass) {
			if (superClass === currentClass) {
				return true;
			}
			classAttributes = $.Cmdbuild.customvariables.cacheClasses
					.getClass(currentClass);
			if (! (classAttributes && classAttributes.parent)) {
				return false;
			}
			return this.sameClass(superClass, classAttributes.parent);
		};
		this.getClassPathInTree = function(originalPath) {
			if (originalPath.length === 0) {
				return null;
			}
			var root = null;
			var classRootId = originalPath[originalPath.length - 1].classId;
			for (var i = 0; i < this.tree.nodes.length; i++) {
				var node = this.tree.nodes[i];
				if (this.sameClass(node.metadata.targetClass, classRootId)) {
					root = node;
					break;
				}
			}
			return (root) ? this.calculatePathFromNode([], root, originalPath)
					: null;
		};
	};
	$.Cmdbuild.g3d.navigationManager = navigationManager;

})(jQuery);
