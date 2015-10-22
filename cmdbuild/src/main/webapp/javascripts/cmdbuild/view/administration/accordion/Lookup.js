(function() {

	Ext.define('CMDBuild.view.administration.accordion.Lookup', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.lookup.Type',
			'CMDBuild.model.common.accordion.Lookup'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Lookup}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.common.accordion.Lookup',

		title: CMDBuild.Translation.lookupTypes,

		/**
		 * @param {String} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			nodeIdToSelect = Ext.isString(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.core.proxy.lookup.Type.readAll({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					if (!Ext.isEmpty(decodedResponse)) {
						var nodes = [];
						var nodesMap = {};

						// Build nodes map
						Ext.Array.forEach(decodedResponse, function(lookupTypeObject, i, allLookupTypeObjects) {
							nodesMap[lookupTypeObject[CMDBuild.core.constants.Proxy.ID]] = {
								text: lookupTypeObject[CMDBuild.core.constants.Proxy.TEXT],
								description: lookupTypeObject[CMDBuild.core.constants.Proxy.TEXT],
								id: lookupTypeObject[CMDBuild.core.constants.Proxy.ID],
								parent: lookupTypeObject[CMDBuild.core.constants.Proxy.PARENT],
								cmName: this.cmName,
								leaf: true
							};
						}, this);

						// Build tree nodes hierarchy
						Ext.Object.each(nodesMap, function(id, node, myself) {
							if (Ext.isEmpty(node[CMDBuild.core.constants.Proxy.PARENT])) {
								nodes.push(node);
							} else {
								var parentNode = nodesMap[node[CMDBuild.core.constants.Proxy.PARENT]];

								if (!Ext.isEmpty(parentNode)) {
									parentNode.children = parentNode.children || [];
									parentNode.children.push(node);
									parentNode.leaf = false;
								}
							}
						}, this);

						this.getStore().getRootNode().removeAll();
						this.getStore().getRootNode().appendChild(nodes);
						this.getStore().sort();

						// Alias of this.callParent(arguments), inside proxy function doesn't work
						if (!Ext.isEmpty(this.delegate))
							this.delegate.cmfg('onAccordionUpdateStore', nodeIdToSelect);
					}
				}
			});
		}
	});

})();