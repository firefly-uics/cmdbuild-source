(function() {

	Ext.define('CMDBuild.view.administration.accordion.Lookup', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Lookup}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.lookupTypes,

		/**
		 * @return {Array} out
		 */
		buildTreeStructure: function() {
			var nodesMap = {};
			var out = [];

			Ext.Object.each(_CMCache.getLookupTypes(), function(key, value, myself) {
				nodesMap[value.get(CMDBuild.core.proxy.Constants.ID)] = {
					id: value.get(CMDBuild.core.proxy.Constants.ID),
					text: value.get(CMDBuild.core.proxy.Constants.TEXT),
					leaf: true,
					cmName: 'lookuptype',
					parent: value.get(CMDBuild.core.proxy.Constants.PARENT)
				};
			}, this);

			Ext.Object.each(nodesMap, function(id, node, myself) {
				if (node[CMDBuild.core.proxy.Constants.PARENT]) {
					var parentNode = nodesMap[node[CMDBuild.core.proxy.Constants.PARENT]];

					if (!Ext.isEmpty(parentNode)) {
						parentNode.children = parentNode.children || [];
						parentNode.children.push(node);
						parentNode.leaf = false;
					}
				} else {
					out.push(node);
				}
			}, this);
			return out;
		}
	});

})();