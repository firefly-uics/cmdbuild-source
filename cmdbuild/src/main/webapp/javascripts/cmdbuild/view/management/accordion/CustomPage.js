(function() {

	Ext.define('CMDBuild.view.management.accordion.CustomPage', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.CustomPage'
		],

		/**
		 * @cfg {CMDBuild.controller.common.abstract.Accordion}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.customPages,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			CMDBuild.core.proxy.CustomPage.readForCurrentUser({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					if (!Ext.isEmpty(decodedResponse)) {
						var nodes = [];

						Ext.Array.forEach(decodedResponse, function(pageObject, i, allGroupObjects) {
							var nodeObject = {};
							nodeObject['cmName'] = this.cmName;
							nodeObject['iconCls'] = 'cmdbuild-tree-custompage-icon';
							nodeObject[CMDBuild.core.constants.Proxy.TEXT] = pageObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
							nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = pageObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
							nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = pageObject[CMDBuild.core.constants.Proxy.ID];
							nodeObject[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', { components: pageObject[CMDBuild.core.constants.Proxy.ID] });
							nodeObject[CMDBuild.core.constants.Proxy.NAME] = pageObject[CMDBuild.core.constants.Proxy.NAME];
							nodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;

							nodes.push(nodeObject);
						}, this);

						this.getStore().getRootNode().removeAll();
						this.getStore().getRootNode().appendChild(nodes);
						this.getStore().sort();
					}
				}
			});
		}
	});

})();