(function($) {
	var NO_OPEN_NODE = "__NoOpenNode";
	var tree = function() {
		this.param = undefined;
		this.relations4Classes = {};
		this.oldNodes = {};
		this.refresh = function() {
			if ($.Cmdbuild.customvariables.commandInExecution === true) {
				return;
			}
			this.param.backend.init();
		};
		this.init = function(param) {
			try {
				this.param = param;
				this.show();
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.div.init");
				throw e;
			}
		};
		this.show = function() {
			try {
				var xmlForm = $.Cmdbuild.elementsManager.getElement(this.param.form);
				$.Cmdbuild.eventsManager.deferEvents();
				this.id = $.Cmdbuild.elementsManager.getXmlElementId(xmlForm);
				var htmlStr = "<div id='" + this.id + "' class='ClasseTree'/>";
				var htmlContainer = $("#" + this.param.container)[0];
				htmlContainer.innerHTML = htmlStr;					
				$.Cmdbuild.elementsManager.initialize();
				$.Cmdbuild.eventsManager.unDeferEvents();
				var me = this;
				$('#' + this.id).jstree({
					'core' : {
						'check_callback': true
					},
					"checkbox" : {
		      			"keep_selected_style" : false,
		      			"whole_node": true
		    		},
		            "types": {
		                "disabled" : { 
		                	"icon":$.Cmdbuild.appRootUrl + "libraries/images/cancel.png",
		                      "check_node" : false,
		                      "uncheck_node" : false 
		                    } 
		            },
		    		"plugins" : [ "checkbox", "actions", "types" ]
			  	}).bind("select_node.jstree", function (event, data) {
			  		//
			  	}).bind("open_node.jstree", function (event, data) {
			  		var val = (data.node.data) ? data.node.data.className : "--";
			  		var children = $('#' + me.id).jstree().get_children_dom(data.node);
			        var nodes = [];
			  		for (var i = 0; i < children.length; i++) {
			  			var child = children[i];
			  			nodes.push(child.id);
			  		}
			  		me.fillNodes(nodes);
			  	});
				var theTree = this;
				$('#' + this.id).jstree(true).add_action("all", {
				    "id": "action_remove",
				    "class": "openCardsFromClass  pull-right",
				    "text": "",
				    "after": true,
				    "selector": "a",
				    "event": "click",
				    "callback": function(node_id, node, action_id, action_el) {
				    	theTree.openClassesPath(node_id, node, action_id, action_el);
				    }

				});
				var backendFn = $.Cmdbuild.utilities.getBackend(this.param.backend);
				var backend = new backendFn(this.param, function(response) {
					this.param.backend = response;
					this.toCreate = false;
					this.param.backend.model.observe(this);
					this.showCB();
				}, this);
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.div.show");
				throw e;
			}
		};		
		this.deleteClasses = function(data) {
			var toDelete = [];
			for (var key in this.oldNodes) {
				var found = false;
				for (var i = 0; i < data.length; i++) {
					var node = data[i];
					if (node.className === key) {
						found = true;
						break;
					}
				}
				if (! found) {
					toDelete.push(key);
				}
			}
			for (var i = 0; i < toDelete.length; i++) {
				var node = $('#' + this.id).jstree().get_node(this.oldNodes[toDelete[i]]);
				$('#' + this.id).jstree().delete_node(node);
				delete this.oldNodes[toDelete[i]];
			}
		};		
		this.showCB = function() {
			try {
		 		$.Cmdbuild.dataModel.dispatchChange("classesForm");
				var data = this.param.backend.getData();
				var newNodes = [];
				this.deleteClasses(data);
				
				for (var i = 0; i < data.length; i++) {
					var node = data[i];
					var img = $.Cmdbuild.SpriteArchive.class2Sprite(node.className);
					var strCmd = "$.Cmdbuild.customvariables.selected.selectByClassName(event, '" + node.className + "');";
					var strOnClick = "onClick=\"" + strCmd + " return true;\"";
					var imgHtml = '<img src="' + img + '" ' + strOnClick + ' alt="' + node.className+ '" height="20" width="20">';
					if (this.oldNodes[node.className]) {
						$('#' + this.id).jstree().rename_node(this.oldNodes[node.className], imgHtml + " [" + node.qt + "] - " + node.className);
						continue;
					}
			 		var str = $('#' + this.id).jstree().create_node("#", imgHtml + " [" + node.qt + "] - " + node.className);
					this.oldNodes[node.className] = str;
					$('#' + this.id).jstree().get_node(str).data = node;
					newNodes.push(str);
				}
		 		this.fillNodes(newNodes);
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.div.showCB");
				throw e;
			}
		};		
		this.data2Nodes = function(node, data) {
			for (var i = 0; i < data.length; i++) {
				var newNode = data[i];
				var img = $.Cmdbuild.SpriteArchive.class2Sprite(newNode.className);
				var strCmd = "$.Cmdbuild.customvariables.selected.selectByClassName(event, '" + newNode.className + "');";
				var strOnClick = "onClick=\"" + strCmd + " return true;\"";
				var imgHtml = '<img src="' + img + '" ' + strOnClick + ' alt="' + newNode.className+ '" height="20" width="20">';
				var str = $('#' + this.id).jstree().create_node("#" + node, imgHtml + " " + newNode.className + "(" + newNode.label + ")");
				$('#' + this.id).jstree().get_node(str).data = newNode;
			}
		};
		this.getPathRelations = function(node) {
			if (! node.parent) return [];
			var pathToRoot = [];
			do {
				pathToRoot.push(node.data);
				node = $('#' + this.id).jstree().get_node(node.parent);
			} while (node.id != "#");
			return pathToRoot;
		};
		this.openClass = function(treeNode, node, path, index, arExplosions) {
			if (index >= -1 && node != null) {
					var domains = this.getDomainsFromTreeNode(treeNode);
					arExplosions.push({
						command: "explode",
						id: node.id(),
						domainList: domains
					}); 
			}
			else {
				var className = path[index].className;
				var nodes = (node == null) ?
						this.param.backend.getNodesByClassName(className) :
						this.param.backend.getChildrenByClassName(node, className);
				var me = this;
				for (var i = 0; i < nodes.length; i++) {
					var el = nodes[i];
					me.openClass(treeNode, el, path, index -1, arExplosions);
				}
			}
		};
		this.getDomainsFromTreeNode = function(node) {
			var domains = [];
			for (var i = 0; i < node.children.length; i++) {
				var el = $('#' + this.id).jstree().get_node(node.children[i]);
				if (el.state.selected) {
					domains.push({
						domainId: el.data._id
					});
				}
			};
			return domains;			
		};
		this.openClassesPath = function(node_id, node, action_id, action_el) {
			var pathToRoot = this.getPathRelations(node);
			var arExplosions = [];
			this.openClass(node, null, pathToRoot, pathToRoot.length - 1, arExplosions);
			var macroCommand = new $.Cmdbuild.g3d.commands.macroCommand(this.param.backend.model, arExplosions);
			$.Cmdbuild.customvariables.commandsManager.execute(macroCommand, {}, function() {
			}, this);
	    };
		this.fillNodes = function(nodes) {
			if (nodes.length == 0) {
				return;
			}
			var node = nodes[0];
			nodes.splice(0, 1);
			var objNode = $('#' + this.id).jstree().get_node(node);
			if (! (objNode && objNode.data && objNode.data.className)) {
				this.fillNodes(nodes);
				return;
			}
			var className = objNode.data.className;
			var nodeObj = $('#' + this.id).jstree().get_node(node);
			var pathToRoot = this.getPathRelations(nodeObj);
			if (pathToRoot.length > 2) { //LIMIT: only for the beta version
				this.fillNodes(nodes);
				return;
			}
			if (nodeObj.data.source == NO_OPEN_NODE) {
				$("#" + node).find(".jstree-checkbox").hide();
				$("#" + node).find(".processesPreviousPage").hide();
				$('#' + this.id).jstree(true).set_type(node, "disabled");
				this.relations4Classes[className] = [];
				this.fillNodes(nodes);
				return;
			}
			this.param.backend.getRelation4Class(pathToRoot, className, node, function(parentId, data) {
					this.data2Nodes(node, data);
				this.relations4Classes[className] = data;
				this.fillNodes(nodes);
			}, this);
		};		
	};
	$.Cmdbuild.custom.tree = tree;
}) (jQuery);
