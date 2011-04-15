(function() {
CMDBuild.AdminViewportController = Ext.extend(CMDBuild.MainViewportController, {
	initSubControllers: function() {
		for (var i=0, len=this.treePanels.length; i<len; ++i) {
			var t = this.treePanels[i];
			// TODO change this with something better
			var controllerClass = t.controllerType || "TreePanelController";
			var c = new CMDBuild[controllerClass]({
				treePanel: t,
				eventType: t.eventType
			});
			this.treePanelControllers.push(c);
		}
	}
});
})();