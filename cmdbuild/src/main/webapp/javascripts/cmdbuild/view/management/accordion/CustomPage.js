(function() {

	Ext.define('CMDBuild.view.management.accordion.CustomPage', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CustomPage'
		],

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.customPages,

		constructor: function(){
			this.callParent(arguments);

			this.updateStore();
		},

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
								text: groupObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
								id: groupObject[CMDBuild.core.proxy.CMProxyConstants.ID],
								iconCls: 'cmdbuild-tree-custompage-icon',
								cmName: this.cmName,
								leaf: true
							});
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