(function() {

	Ext.define('CMDBuild.view.administration.accordion.NavigationTree', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.NavigationTree',
			'CMDBuild.model.common.accordion.NavigationTree'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.NavigationTree}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.common.accordion.NavigationTree',

		title: CMDBuild.Translation.navigationTrees,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.core.proxy.NavigationTree.readAll({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE] || [];

					var nodes = [];

					if (!Ext.isEmpty(decodedResponse)) {
						Ext.Array.forEach(decodedResponse, function(treeObject, i, allTreeObjects) {
							var nodeObject = {};
							nodeObject['cmName'] = this.cmName;
							nodeObject[CMDBuild.core.constants.Proxy.TEXT] = treeObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
							nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = treeObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
							nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = treeObject[CMDBuild.core.constants.Proxy.NAME];
							nodeObject[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', treeObject[CMDBuild.core.constants.Proxy.NAME]);
							nodeObject[CMDBuild.core.constants.Proxy.NAME] = treeObject[CMDBuild.core.constants.Proxy.NAME];
							nodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;

							nodes.push(nodeObject);
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