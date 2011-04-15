(function() {

	Ext.ns("CMDBuild.core.tree");
	var RELATED_MODEL_NAME = CMDBuild.core.model.CMDomainModel.NAME;
	var CLASS_NAME = "CMDBuild.core.tree.CMDomainTreeNode";
	
	CMDBuild.core.tree.CMDomainTreeNode = function(domainModel) {
		checkParams(domainModel);
		
		var node = new Ext.tree.TreeNode({
			id: domainModel.getid(),
			text: domainModel.getdescription(),
			selectable: true
		});

		node.getCMModel = function() {
			return domainModel;
		};
		
		return node;
	};
	
	function checkParams(domainModel) {
		if (!domainModel) {
			throw CMDBuild.core.error.tree.NO_CONFIGURATION(CLASS_NAME);
		}
		if (domainModel.NAME != RELATED_MODEL_NAME) {
			throw CMDBuild.core.error.tree.WRONG_CMDOMAIN_MODEL;
		}
	}
})();