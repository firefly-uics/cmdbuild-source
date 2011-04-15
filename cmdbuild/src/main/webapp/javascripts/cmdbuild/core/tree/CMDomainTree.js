(function() {

	Ext.ns("CMDBuild.core.tree");
	
	var RELATED_LIBRARY_NAME = "CMDBuild.core.model.CMDomainModelLibrary";
	var CLASS_NAME = "CMDBuild.core.tree.CMDomainTree";
	
	CMDBuild.core.tree.CMDomainTree = function(domainLibrary) {
		checkParams(domainLibrary);
		var root = new Ext.tree.TreeNode("domain");
		for (var key in domainLibrary.map) {
			var domain = domainLibrary.map[key];
			var domainNode = CMDBuild.core.tree.CMDomainTreeNode(domain);
			root.appendChild(domainNode);
		}
		return root;
	};
	
	function checkParams(domainLibrary) {
		if (!domainLibrary) {
			throw CMDBuild.core.error.tree.NO_CONFIGURATION(CLASS_NAME);
		}

		if (domainLibrary.NAME != RELATED_LIBRARY_NAME) {
			throw CMDBuild.core.error.tree.WRONG_CMMODEL_LIBRARY;
		}
	}

})();