(function() {

	/**
	 * This is the implementation of the CMCardBrowserTreeDelegate for the
	 * CMMapController.
	 */
	Ext.define("CMDBuild.controller.management.classes.map.CMCardBrowserDelegate", {
		extend: "CMDBuild.view.management.CMCardBrowserTreeDelegate",

		constructor: function(master) {
			this.master = master;
		},

		// Hide or show the feature[s] for the node
		// from the map.
		// the action has effect over all the branch that start with the
		// passed node.
		// So, if the node was never opened,
		// there aren't the info to show/hide the features.
		// For this reason, act like an expand, loading the
		// branch at all, and then show/hide the features.
		onCardBrowserTreeCheckChange: function(tree, node, checked, deeply) {
		},


		onCardBrowserTreeCardSelected: function(cardBaseInfo) {
		},


		onCardBrowserTreeNodeAppend: function(cardBrowserTree, node) {
		},

		onCardBrowserTreeActivate: function(cardBrowserTree, activationCount) {},
		onCardBrowserTreeItemExpand: function(tree, node) {}
	});

})();