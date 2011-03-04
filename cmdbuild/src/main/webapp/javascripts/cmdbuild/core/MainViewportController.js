(function() {	
/**
 * p = {
 * 		table: the table of the card to open
 * 		cardId: the id of the card to open
 * 		tabToOpen: (optional) the name of the tab to open
 * 			after the selection of the card
 * } 
 **/
var onOpenCard = function(p) {
	var expandedTreePanel = getExpandedTreePanel(this.treePanels);
	var selected = false;
	if (expandedTreePanel) {
		var controllerOfExTP = getControllerOfTreePanel(expandedTreePanel.id, this.treePanelControllers);
		selected = controllerOfExTP.openCard(p);
	}	

	for (var i=0, l=this.treePanels.length; i<l; ++i) {
		var treePanel = this.treePanels[i];
		var controller = getControllerOfTreePanel(treePanel.id, this.treePanelControllers);
		selected |= controller.openCard(p, silent = selected);			
	}
	
};

var getExpandedTreePanel = function(treePanels) {
	for (var i=0; i<treePanels.length; ++i) {
		var tp = treePanels[i];
		if (!tp.collapsed) {
			return tp;
		}
	}
	return undefined;
};

var getControllerOfTreePanel = function(treePanelId, treeControllers) {
	for (var i=0; i<treeControllers.length; ++i) {
		if (treeControllers[i].treePanel.id == treePanelId) {
			return treeControllers[i];
		}
	}
	return undefined;
};

CMDBuild.MainViewportController = Ext.extend(Ext.Component, {
	viewport: undefined, // passed in construction
	initComponent : function() {
		CMDBuild.MainViewportController.superclass.initComponent.apply(this, arguments);		
		this.treePanels = this.viewport.getTreePanels();
		this.treePanelControllers = [];		
		this.initSubControllers();		
		var selected = this.selectStartingClass();
		if (!selected) {
			this.expandFirstPanel();
		}
		
		this.subscribe("cmdb-opencard", onOpenCard, this);
	},
	
	initSubControllers: function() {		
		for (var i=0, len=this.treePanels.length; i<len; ++i) {
			var t = this.treePanels[i];
			var c = new CMDBuild.TreePanelController({
				treePanel: t
			});			
			c.on("selectionchange", function(p) {
				for (var i=0, len=this.treePanels.length; i<len; ++i) {
					var t = this.treePanels[i];
					var node = t.searchNodeById(p.selection.id);
					if ((t.id != p.controllerId) && node) {
						t.silentSelectNodeById(node.id);
					}
				}
			}, this);
			
			this.treePanelControllers.push(c);
		}
	},

	selectStartingClass: function() {
		var startingClass = CMDBuild.Runtime.StartingClassId;
		var selected = false;
		for (var i=0; i<this.treePanels.length; ++i) {
			var treePanel =  this.treePanels[i];
			var treeRoot = treePanel.root;
			if (treePanel.selectNodeById(startingClass, expandAfter = true)) {
				selected = true;
				break;
			}
		}
		return selected;
	},
	
	expandFirstPanel: function() {
		if (this.treePanels.length > 0) {
            var panel = this.treePanels[0];
            panel.on("render", function() {
                (function() {
                    panel.expand();
                }).defer(1, this);
            }, panel);
        }	
	}
});
})();