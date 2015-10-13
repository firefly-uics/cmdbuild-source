(function() {

	Ext.define('CMDBuild.view.management.accordion.CustomPage', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.CustomPage'
		],

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
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
					decodedResponse = decodedResponse.response;

					if (!Ext.isEmpty(decodedResponse)) {
						var nodes = [];

						Ext.Array.forEach(decodedResponse, function(groupObject, i, allGroupObjects) {
							nodes.push({
								text: groupObject[CMDBuild.core.constants.Proxy.DESCRIPTION],
								iconCls: 'cmdbuild-tree-custompage-icon',
								cmName: this.cmName,
								leaf: true
							});
						}, this);

						this.getStore().getRootNode().removeAll();
						this.getStore().getRootNode().appendChild(nodes);
					}
				}
			});
		}
	});

})();