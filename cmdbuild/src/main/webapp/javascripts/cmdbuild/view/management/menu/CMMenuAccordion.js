(function() {

	var FILTER = "FILTER";
	var SQL = "SQL";
	var DATA_VIEW = "dataView";

	Ext.define("CMDBuild.view.administration.accordion.CMMenuAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.management.modmenu.menu,
		cmName: "menu",
		buildTreeStructure: function(menu) {
			var out = [];
			if (menu) {
				var tree = adapt(menu);
				if (tree.children) {
					out = tree.children;
				}
			}

			// override to look for the
			// real children nodes
			this.isEmpty = function() {
				return out.length == 0;
			};

			return out;
		},

	});

	function adapt(menu) {
		var out = adaptSingleNode(menu);
		if (menu.children || out.type == "folder") {
			out.leaf = false;
			out.children = [];
			out.expanded = false;

			var children = menu.children || [];
			for (var i=0, l=children.length; i<l; ++i) {
				var child = children[i];
				out.children.push(adapt(child));
			}
		} else {
			out.leaf = true;
		}

		return out;
	}

	/*
	 * a node from the server has this shape:
	 * 
	 * {
	 * 	description: a description
		index: for the sort
		referencedClassName: the class to opent
		referencedElementId: eventually the id
		type: "class | processclass | dashboard | reportcsv | reportpdf | view"
		}
	 */
	function adaptSingleNode(node) {
		var type = node.type;
		var entryType = null;
		var superClass = false;
		var tableType = "";
		var classIdentifier = node.referencedClassName;

		if (type == "class" 
			|| type == "processclass") {

			entryType = _CMCache.getEntryTypeByName(node.referencedClassName);
			if (entryType) {
				superClass = entryType.isSuperClass();
				classIdentifier = entryType.getId();
				tableType = entryType.getTableType();
			}
		}

		var out = {
			id: classIdentifier,
			idClass: classIdentifier,

			text: node.description,
			tableType: tableType,
			leaf: type != "folder",
			cmName: node.type == "processclass" ? "process" : node.type, //ugly compatibility hack
			iconCls: "cmdbuild-tree-" + (superClass ? "super" : "") + type +"-icon",
			cmIndex: node.index,
			type: node.type
		};

		if (isAReport(node)) {
			addReportStuff(out, node);
		}

		if (isADashboard(node)) {
			out.id = node.referencedElementId;
		}

		if (isView(node)) {
			out.tableType = "standard";
			out.leaf = true;
			out.iconCls = "cmdbuild-tree-class-icon";

			if (node.soecificTypeValues.type == FILTER) {
				node.viewType = FILTER;

				var entryTypeName = node.soecificTypeValues.sourceClassName;
				var entryType = _CMCache.getEntryTypeByName(entryTypeName);

				out.id = entryType.getId();
				out.filter = node.soecificTypeValues.filter;
				out.cmName = "class"; // To act as a regular class node
			} else {
				out.viewType = SQL;
				out.sourceFunction = node.soecificTypeValues.sourceFunction;
				out.cmName = DATA_VIEW;
			}
		}

		return out;
	}

	function isADashboard(node) {
		return node.type == "dashboard";
	}

	function isAReport(node) {
		return node.type.indexOf("report") > -1;
	}

	function isView(node) {
		return node.type == "view";
	}

	function addReportStuff(n, node) {
		n.cmName = "report";
		n.id = node.referencedElementId;
		n.subtype = "custom";
	}
})();